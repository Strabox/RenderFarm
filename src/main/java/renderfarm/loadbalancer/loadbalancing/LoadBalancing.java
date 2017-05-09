package renderfarm.loadbalancer.loadbalancing;

import java.util.List;

import dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.InstanceCantReceiveMoreRequestsException;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequestException;
import renderfarm.loadbalancer.exceptions.RedirectFailedException;
import renderfarm.util.Metric;

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
	protected abstract RenderFarmInstance getFitestMachineAlgorithm(RenderFarmInstanceManager im,Request req)
			throws NoInstancesToHandleRequestException;
	
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
		req.setWeight(estimateRequestComputationalCost(req));
		chosenInstance = getFitestMachineAlgorithm(im, req);
		try {
			chosenInstance.addRequest(req);
		} catch (InstanceCantReceiveMoreRequestsException e) {
			throw new RedirectFailedException();
		}
		return chosenInstance;
	}
	
	/**
	 * Calculate our estimation for the computation cost of the request based
	 * on our information stored in the dynamo.
	 * @param req Request to be processed
	 */
	private int estimateRequestComputationalCost(Request req) {
		 List<Metric> metricsStored = dynamoDB.getIntersectiveItems(req.getFile(), req.getNormalizedWindow().getX(),
				req.getNormalizedWindow().getY(), req.getNormalizedWindow().getWidth(),
				req.getNormalizedWindow().getHeight());
		Metric fitestMetric = null;
		float fitestMetricScaleFactor = 0, fitestMetricOverlappingArea = 0;
		long metricBasicBlockAdjustedScaleAndArea = 0, metricStoreCountAdjustedScaleAndArea = 0,
				metricLoadCountAdjustedScaleAndArea = 0;
		final int defaultWeight = 5;
		//No metrics that have overlapping windows with ours
		if(metricsStored.isEmpty()) {
			System.out.println("[EstimateRequestCost]Result: " + defaultWeight + " [Default] no entries");
			return defaultWeight;
		}
		System.out.println("[EstimateRequestCost]Searching in " + metricsStored.size() + " metrics");
		for(Metric metric : metricsStored) {
			float requestScaleFactor = req.getScenePixelsResolution() / (float) metric.getScenePixelsResolution();
			float overlappingArea = req.getNormalizedWindow().normalizedAreaOverlapping(metric.getNormalizedWindow());
			if(overlappingArea >= fitestMetricOverlappingArea) {
				System.out.println("Metric to be fitest: " + System.lineSeparator() + metric);
				fitestMetric = metric;
				fitestMetricOverlappingArea = overlappingArea;
				fitestMetricScaleFactor = requestScaleFactor;
			}
		}
		float overlappingAreaDivideByMetricArea = fitestMetricOverlappingArea / (float) fitestMetric.getNormalizedWindow().getArea();
		float overlappingAreaDividedByRequestArea = fitestMetricOverlappingArea / (float) req.getNormalizedWindow().getArea();
		//Obtain the measures from the selected metric and multiply it by the resolution scale and
		//multiply it by the ratio of overlapping in metric 
		metricBasicBlockAdjustedScaleAndArea = (long) (fitestMetric.getMeasures().getBasicBlockCount() * fitestMetricScaleFactor
				* overlappingAreaDivideByMetricArea);
		metricStoreCountAdjustedScaleAndArea = (long) (fitestMetric.getMeasures().getStorecount() * fitestMetricScaleFactor
				* overlappingAreaDivideByMetricArea);
		metricLoadCountAdjustedScaleAndArea = (long) (fitestMetric.getMeasures().getLoadcount() * fitestMetricScaleFactor 
				* overlappingAreaDivideByMetricArea);
		
		int costLevelOfOvelappingAreaInMetric = getCostLevel(metricBasicBlockAdjustedScaleAndArea,
				metricLoadCountAdjustedScaleAndArea, metricStoreCountAdjustedScaleAndArea);
		
		float finalWeight = (overlappingAreaDividedByRequestArea * costLevelOfOvelappingAreaInMetric) + 
				((req.getNormalizedWindow().getArea() - fitestMetricOverlappingArea) / (float) req.getNormalizedWindow().getArea()) * 
				defaultWeight;
		int result = Math.round(finalWeight);
		System.out.println("[EstimateRequestCost]Result: " + finalWeight);
		return result;
	}
	
	/**
	 * Empirical calculation of a request cost based on estimation of
	 * various request examples (i.e: professor examples)
	 * @param basicBlocks Basic blocks counts
	 * @param loadCounts Load counts
	 * @param storeCounts Store counts
	 * @return [0,10] depending on the weight of the request that generated the inputs
	 */
	private int getCostLevel(long basicBlocks, long loadCounts, long storeCounts) {
		final long basicBlockReference = 42000000000L;
		final long storeCountReference = 20000000000L;
		final long loadCountReference = 200000000000L;
		final float constant = 8.82905f;
		double bb = Math.log10(basicBlockReference / (double) basicBlocks);
		double lc = Math.log10(loadCountReference / (double) loadCounts);
		double sc = Math.log10(storeCountReference / (double) storeCounts);
		double complexityLog = bb + lc + sc;
		int res = (int) Math.round(10 - ((complexityLog / constant) * 10));
		if(res < 0) {			//Prevent weight that will turn our instance lighter :)
			res = 0;
		}
		else if(res >= 10 ) { 	//Makes 10 our maximum weight
			res = 10;
		}
		System.out.println("[GetCostLevel]Complexity level: " + res);
		return res;
	}
	
	
}
