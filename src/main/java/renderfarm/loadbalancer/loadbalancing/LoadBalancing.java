package renderfarm.loadbalancer.loadbalancing;

import java.util.ArrayList;
import java.util.List;

import dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.InstanceCantReceiveMoreRequests;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequest;
import renderfarm.loadbalancer.exceptions.RedirectFailed;
import renderfarm.util.Measures;
import renderfarm.util.Metric;
import renderfarm.util.NormalizedWindow;

/**
 * Class that implements the interface to the load balancing algorithm.
 * (Strategy + Template Method Design Pattern)
 * @author Andre
 *
 */
public abstract class LoadBalancing {

	/**
	 * DynamoDB client
	 */
	private AmazonDynamoDB dynamoDB;
	
	public LoadBalancing(AmazonDynamoDB dynamoDB) {
		this.dynamoDB = dynamoDB;
	}
	
	/**
	 * Override this method in subclasses to implement the instance selection algorithm.
	 * @param im Manager of render farms instances
	 * @param req Actual request
	 * @return Render farm instance selected to handle the request
	 */
	protected abstract RenderFarmInstance getFitestMachine(RenderFarmInstanceManager im,Request req) throws NoInstancesToHandleRequest;
	
	/**
	 * Method to get the machine IP to handle the request
	 * @param im Manager of render farms instances
	 * @param req Actual request
	 * @return Render farm instance IP selected to handle the request
	 * @throws NoInstancesToHandleRequest 
	 * @throws RedirectFailed 
	 */
	public String getFitestMachineIp(RenderFarmInstanceManager im,Request req) 
			throws NoInstancesToHandleRequest, RedirectFailed {
		RenderFarmInstance chosenInstance;
		estimateRequestComputationalCost(req);
		chosenInstance = getFitestMachine(im, req);
		try {
			chosenInstance.addRequest(req);
		} catch (InstanceCantReceiveMoreRequests e) {
			throw new RedirectFailed();
		}
		return chosenInstance.getIp();
	}
	
	private List<Metric> getOverlappingMetricsRequest() {
		List<Metric> metrics = new ArrayList<Metric>();
		Metric metric2 = new Metric(null, new NormalizedWindow(0.3f,0.25f,0.25f,0.25f), 100, 
				new Measures(100,50,25), 0);
		Metric metric1 = new Metric(null, new NormalizedWindow(0.25f,0.25f,0.25f,0.25f), 100, 
				new Measures(100,50,25), 0);
		Metric metric3 = new Metric(null, new NormalizedWindow(0.3f,0.3f,0.25f,0.25f), 100, 
				new Measures(100,50,25), 0);
		metrics.add(metric2);
		metrics.add(metric1);
		metrics.add(metric3);
		return metrics;
	}
	
	/**
	 * Directly based on Joao calculation.
	 * @param basicBlocks
	 * @param loadCounts
	 * @param storeCounts
	 * @return
	 */
	private int getCostLevel(long basicBlocks, long loadCounts, long storeCounts) {
		final long basicBlockReference = 42000000000L;
		final long storeCountReference = 20000000000L;
		final long loadCountReference = 20000000000L;
		final float constant = 8.82905f;
		double bb = Math.log10(basicBlockReference / (double) basicBlocks);
		double lc = Math.log10(loadCountReference / (double) loadCounts);
		double sc = Math.log10(storeCountReference / (double) storeCountReference);
		double complexityLog = bb + lc + sc;
		int res = (int) Math.round(10 - ((complexityLog / constant) * 10));
		System.out.println("[GetCostLevel]Complexity level: " + res);
		return res;
	}
	
	/**
	 * Calculate our estimation for the computation cost of the request based
	 * on our information stored in the dynamo.
	 * @param req Request to be processed
	 */
	private void estimateRequestComputationalCost(Request req) {
		/*
		 * Getting the metrics from dynamoDB:
		 * List<Metric> metricsStored = dynamoDB.getIntersectiveItems(req.getFile(), req.getNormalizedWindow().getX(),
				req.getNormalizedWindow().getY(), req.getNormalizedWindow().getWidth(),
				req.getNormalizedWindow().getHeight()); */
		List<Metric> metricsStored = getOverlappingMetricsRequest();
		Metric fitestMetric = null;
		float fitestMetricScaleMultiplicationFactor = 0,fitestMetricPercentageAreaOverlappingMetric = 0,
				fitestMetricPercentageAreaOverlappingRequest = 0;
		long metricBasicBlock = 0, metricStoreCount = 0, metricLoadCount = 0;
		if(metricsStored.isEmpty()) {		//No metrics that have overlapping windows with ours
			req.setWeight(4);
			return;
		}
		for(Metric metric : metricsStored) {
			float requestScaleMultiplicationFactor = req.getTotalPixelsRendered() / (float) metric.getTotalPixelsRendered();
			float normalizedAreaOverlapping = req.getNormalizedWindow().normalizedAreaOverlapping(metric.getNormalizedWindow());
			float percentageAreaOverlappingRequest = normalizedAreaOverlapping / req.getNormalizedWindow().getArea();
			float percentageAreaOverlappingMetric = normalizedAreaOverlapping / metric.getNormalizedWindow().getArea();
			if(percentageAreaOverlappingRequest > fitestMetricPercentageAreaOverlappingRequest) {
				System.out.println("Metric to be fitest: " + System.lineSeparator() + metric);
				fitestMetric = metric;
				fitestMetricPercentageAreaOverlappingMetric = percentageAreaOverlappingMetric;
				fitestMetricPercentageAreaOverlappingRequest = percentageAreaOverlappingRequest;
				fitestMetricScaleMultiplicationFactor = requestScaleMultiplicationFactor;
				System.out.println(fitestMetricPercentageAreaOverlappingMetric);
				System.out.println(fitestMetricPercentageAreaOverlappingRequest);
				System.out.println(fitestMetricScaleMultiplicationFactor);
			}
		}
		
		//Obtain the measures from the selected metric and multiply it by the scale
		metricBasicBlock = (long) (fitestMetric.getMeasures().getBasicBlockCount() * fitestMetricScaleMultiplicationFactor);
		metricStoreCount = (long) (fitestMetric.getMeasures().getStorecount() * fitestMetricScaleMultiplicationFactor);
		metricLoadCount = (long) (fitestMetric.getMeasures().getLoadcount() * fitestMetricScaleMultiplicationFactor);
		
		final int defaultCost = 4;
		float finalWeight = (getCostLevel(metricBasicBlock,metricLoadCount,metricStoreCount) * fitestMetricPercentageAreaOverlappingMetric)
				+ (defaultCost * (1 - fitestMetricPercentageAreaOverlappingRequest));
		System.out.println("[EstimateRequestCost]" + Math.round(finalWeight));
		req.setWeight(Math.round(finalWeight));
	}
	
}
