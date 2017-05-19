package renderfarm.loadbalancer.loadbalancing;

import java.util.List;

import renderfarm.dynamo.AmazonDynamoDB;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
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

	private static final float OVERLAPPING_AREA_WEIGHT = 0.7f;
	
	private static final float OVERLAPPING_AREA_PERCENTAGE_IN_METRIC_WEIGHT = 0.3f;
	
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
			throws RedirectFailedException;
	
	/**
	 * Method to get the machine IP to handle the request
	 * @param im Manager of render farms instances
	 * @param req Actual request
	 * @return Render farm instance IP selected to handle the request
	 * @throws NoInstancesToHandleRequestException 
	 * @throws RedirectFailedException 
	 */
	public RenderFarmInstance getFitestMachine(RenderFarmInstanceManager im,Request req) 
			throws RedirectFailedException {
		RenderFarmInstance chosenInstance;
		req.setWeight(estimateRequestComputationalCost(req));
		chosenInstance = getFitestMachineAlgorithm(im, req);
		return chosenInstance;
	}
	
	/**
	 * Calculate our estimation for the computation cost of the request based
	 * on our information stored in the dynamo.
	 * @param req Request to be processed
	 */
	private int estimateRequestComputationalCost(Request req) {
		 final List<Metric> metricsStored = dynamoDB.getIntersectiveItems(req.getFile(), req.getNormalizedWindow().getX(),
				req.getNormalizedWindow().getY(), req.getNormalizedWindow().getWidth(),
				req.getNormalizedWindow().getHeight());
		Metric fitestMetric = null;
		float fitestSelectionFactor = 0;
		if(metricsStored.isEmpty()) {	//No metrics that have overlapping windows with ours
			System.out.println("[EstimateRequestCost]Result: " + getDefaultRequestWeight(req.getwindowResolution()) + " [Default] no entries");
			return getDefaultRequestWeight(req.getwindowResolution());
		}
		System.out.println("[EstimateRequestCost]Searching in " + metricsStored.size() + " metrics");
		for(Metric metric : metricsStored) {
			float overlappingArea = req.getNormalizedWindow().normalizedAreaOverlapping(metric.getNormalizedWindow());
			if(overlappingArea < 0.00001) {
				System.out.println("[EstimateRequestCost]ERROOOO returning metrics that don't overlapp");
			}
			float selectionFactor = ((float) OVERLAPPING_AREA_WEIGHT * overlappingArea) + 
					((float) OVERLAPPING_AREA_PERCENTAGE_IN_METRIC_WEIGHT * 
					(overlappingArea / (float) metric.getNormalizedWindow().getArea()));
			if(selectionFactor > fitestSelectionFactor) {
				fitestSelectionFactor = selectionFactor;
				fitestMetric = metric;
			}
		}
		System.out.println("Fitest Metric: " + fitestMetric);
		float fitestMetricOverlappingArea = req.getNormalizedWindow().normalizedAreaOverlapping(fitestMetric.getNormalizedWindow());
		float overlappingAreaDivideByMetricArea = fitestMetricOverlappingArea / (float) fitestMetric.getNormalizedWindow().getArea();
		float overlappingAreaDividedByRequestArea = fitestMetricOverlappingArea / (float) req.getNormalizedWindow().getArea();
		float fitestMetricScaleFactor = req.getScenePixelsResolution() / (float) fitestMetric.getScenePixelsResolution(); 
		//Obtain the measures from the selected metric and multiply it by the resolution scale and
		//multiply it by the ratio of overlapping in metric 
		long metricBasicBlockAdjustedScaleAndArea = (long) (fitestMetric.getMeasures().getBasicBlockCount() * fitestMetricScaleFactor
				* overlappingAreaDivideByMetricArea);
		
		int costLevelOfOvelappingAreaInMetric = getCostLevel2(metricBasicBlockAdjustedScaleAndArea);
		
		System.out.println("[EstimateRequestCost]FitestMetricOvelappingArea: " + fitestMetricOverlappingArea);
		System.out.println("[EstimateRequestCost]FitestMetricArea: " + fitestMetric.getNormalizedWindow().getArea());
		System.out.println("[EstimateRequestCost]ScaleFactor: " + fitestMetricScaleFactor);
		System.out.println("[EstimateRequestCost]%OfOverlapingAreaInMetric: " + overlappingAreaDivideByMetricArea);
		System.out.println("[EstimateRequestCost]%OfOverlapingAreaInRequest: " + overlappingAreaDividedByRequestArea);
		
		float finalWeight = (overlappingAreaDividedByRequestArea * costLevelOfOvelappingAreaInMetric) + 
				((req.getNormalizedWindow().getArea() - fitestMetricOverlappingArea) / (float) req.getNormalizedWindow().getArea()) * 
				getDefaultRequestWeight(req.getwindowResolution());
		int result = Math.round(finalWeight);
		System.out.println("[EstimateRequestCost]Result: " + finalWeight + " => " + result);
		return result;
	}
	
	/**
	 * Empirical calculation of a request cost based on estimation of
	 * various request examples (i.e: professor examples) using the measures from the 
	 * selected metric.
	 * @param basicBlocks Basic blocks counts
	 * @return [0,10] depending on the weight of the request that generated the inputs
	 */
	private int getCostLevel2(long basicBlocks) {
		int res = Math.round((basicBlocks - 61007736)/(41782640027f - 61007736) * 10);
		if(res <= 0) {
			res = 1;
		}
		else if(res > 10) {
			res = 10;
		}
		return res;
	}
	
	/**
	 * Empirical calculation of a request cost based on estimation of
	 * various request examples (i.e: professor examples) using the measures from the 
	 * selected metric.
	 * @param basicBlocks Basic blocks counts
	 * @param loadCounts Load counts
	 * @param storeCounts Store counts
	 * @return [0,10] depending on the weight of the request that generated the inputs
	 */
	@Deprecated
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
	
	/**
	 * Return the default weight for the slice of the request (or all the request)
	 * that we have no idea about. 
	 * Empirical calculated based on some examples including professor ones.
	 * Rationale: More pixels to render heavier the request
	 * @return [0,10]
	 */
	private int getDefaultRequestWeight(long windowResolution) {
		if(windowResolution <= (250 * 250)) {
			return 1;
		}
		else if(windowResolution <= (500 * 500)) {
			return 2;
		}
		else if(windowResolution <= (750 * 750)) {
			return 3;
		}
		else if(windowResolution <= (1250 * 1250)) {
			return 5;
		}
		else if(windowResolution <= (1750 * 1750)) {
			return 6;
		}
		else if(windowResolution <= (2500 * 2500)) {
			return 7;
		}
		else {
			return 8;
		}
	}
	
}
