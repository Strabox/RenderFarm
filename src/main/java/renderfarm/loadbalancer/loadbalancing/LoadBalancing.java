package renderfarm.loadbalancer.loadbalancing;

import java.util.ArrayList;
import java.util.List;

import dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.InstanceCantReceiveMoreRequestsException;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequestException;
import renderfarm.loadbalancer.exceptions.RedirectFailedException;
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
	protected abstract RenderFarmInstance getFitestMachineAlgorithm(RenderFarmInstanceManager im,Request req) throws NoInstancesToHandleRequestException;
	
	/**
	 * Method to get the machine IP to handle the request
	 * @param im Manager of render farms instances
	 * @param req Actual request
	 * @return Render farm instance IP selected to handle the request
	 * @throws NoInstancesToHandleRequestException 
	 * @throws RedirectFailedException 
	 */
	public RenderFarmInstance getFitestMachine(RenderFarmInstanceManager im,Request req) 
			throws NoInstancesToHandleRequestException, RedirectFailedException {
		RenderFarmInstance chosenInstance;
		estimateRequestComputationalCost(req);
		chosenInstance = getFitestMachineAlgorithm(im, req);
		try {
			chosenInstance.addRequest(req);
		} catch (InstanceCantReceiveMoreRequestsException e) {
			throw new RedirectFailedException();
		}
		return chosenInstance;
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
		System.out.println("[GetCostLevel]ComplexityLog:" + complexityLog);
		int res = (int) Math.round(10 - ((complexityLog / constant) * 10));
		if(res < 0) {		//Prevent weight that will turn our instance lighter :)
			res = 0;
		}
		System.out.println("[GetCostLevel]Complexity level: " + res);
		return res;
	}
	
	/**
	 * Calculate our estimation for the computation cost of the request based
	 * on our information stored in the dynamo.
	 * @param req Request to be processed
	 */
	private void estimateRequestComputationalCost(Request req) {
		 List<Metric> metricsStored = dynamoDB.getIntersectiveItems(req.getFile(), req.getNormalizedWindow().getX(),
				req.getNormalizedWindow().getY(), req.getNormalizedWindow().getWidth(),
				req.getNormalizedWindow().getHeight());
		Metric fitestMetric = null;
		float fitestMetricScaleMultiplicationFactor = 0,fitestMetricPercentageAreaOverlappingMetric = 0,
				fitestMetricPercentageAreaOverlappingRequest = 0;
		long metricBasicBlock = 0, metricStoreCount = 0, metricLoadCount = 0;
		System.out.println("[EstimateRequestCost]Searching in " + metricsStored.size() + " metrics");
		for(Metric metric : metricsStored) {
			float requestScaleMultiplicationFactor = req.getScenePixelsResolution() / (float) metric.getScenePixelsResolution();
			float normalizedAreaOverlapping = req.getNormalizedWindow().normalizedAreaOverlapping(metric.getNormalizedWindow());
			float percentageAreaOverlappingRequest = normalizedAreaOverlapping / req.getNormalizedWindow().getArea();
			float percentageAreaOverlappingMetric = normalizedAreaOverlapping / metric.getNormalizedWindow().getArea();
			if(percentageAreaOverlappingRequest > fitestMetricPercentageAreaOverlappingRequest) {
				System.out.println("Metric to be fitest: " + System.lineSeparator() + metric);
				fitestMetric = metric;
				fitestMetricPercentageAreaOverlappingMetric = percentageAreaOverlappingMetric;
				fitestMetricPercentageAreaOverlappingRequest = percentageAreaOverlappingRequest;
				fitestMetricScaleMultiplicationFactor = requestScaleMultiplicationFactor;
			}
		}
		if(metricsStored.isEmpty() || fitestMetric == null) {		//No metrics that have overlapping windows with ours
			req.setWeight(4);
			System.out.println("[EstimateRequestCost]Result: 4 (Default no entries)");
			return;
		}
		System.out.println("[EstimateRequestCost]%Area of metric:"+fitestMetricPercentageAreaOverlappingMetric);
		System.out.println("[EstimateRequestCost]%Area of request:"+fitestMetricPercentageAreaOverlappingRequest);
		System.out.println("[EstimateRequestCost]Scale Factor:"+fitestMetricScaleMultiplicationFactor);
		//Obtain the measures from the selected metric and multiply it by the scale
		metricBasicBlock = (long) (fitestMetric.getMeasures().getBasicBlockCount() * fitestMetricScaleMultiplicationFactor);
		metricStoreCount = (long) (fitestMetric.getMeasures().getStorecount() * fitestMetricScaleMultiplicationFactor);
		metricLoadCount = (long) (fitestMetric.getMeasures().getLoadcount() * fitestMetricScaleMultiplicationFactor);
		
		final int defaultCost = 4;
		float finalWeight = (getCostLevel(metricBasicBlock,metricLoadCount,metricStoreCount) * fitestMetricPercentageAreaOverlappingMetric)
				+ (defaultCost * (1 - fitestMetricPercentageAreaOverlappingRequest));
		System.out.println("[EstimateRequestCost]Result: " + Math.round(finalWeight));
		req.setWeight(Math.round(finalWeight));
	}
	
}
