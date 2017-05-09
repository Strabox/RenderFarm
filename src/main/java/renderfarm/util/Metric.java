package renderfarm.util;

/**
 * Metric for a given scene and window rendered. 
 * @author Andre
 *
 */
public class Metric {

	/**
	 * File name that represents the scene of the metric
	 */
	private String fileName;
	
	/**
	 * Normalized window that the metric correspond
	 */
	private NormalizedWindow normalizedWindow;
	
	/**
	 * Scene pixels resolution of the metric
	 */
	private long scenePixelsResolution;
	
	/**
	 * All the measures for this metric
	 */
	private Measures measures;


	public Metric(String fileName, NormalizedWindow normalizedWindow, 
			long scenePixelsResolution, Measures measures) {
		this.fileName = fileName;
		this.normalizedWindow = normalizedWindow;
		this.scenePixelsResolution = scenePixelsResolution;
		this.measures = measures;
	}
	
	public Metric() {
		this.fileName = "FIleNameNotInitialized";
		this.normalizedWindow = new NormalizedWindow();
		setTotalPixelsRendered(0);
		this.measures = new Measures();
	}

	public void reset(){
		this.fileName = null;
		this.normalizedWindow = null;
		setTotalPixelsRendered(0);;
		this.measures.reset();
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public NormalizedWindow getNormalizedWindow() {
		return normalizedWindow;
	}

	public long getScenePixelsResolution() {
		return this.scenePixelsResolution;
	}

	public Measures getMeasures() {
		return measures;
	}

	public void setNormalizedWindow(NormalizedWindow normalizedWindow) {
		this.normalizedWindow = normalizedWindow;
	}

	public void setTotalPixelsRendered(long totalPixelsRendered) {
		this.scenePixelsResolution = totalPixelsRendered;
	}

	public void setMeasures(Measures measures) {
		this.measures = measures;
	}
	
	@Override
	public String toString() {
		String res = "";
		res += "===============================================================" + System.lineSeparator();
		res += "File Name: " + fileName + System.lineSeparator();
		res += "Normalized Window: " + normalizedWindow + System.lineSeparator();
		res += "Total Pixels Rendered: " + scenePixelsResolution + System.lineSeparator();
		res += "Measures: " + System.lineSeparator() + measures + System.lineSeparator();
		res += "===============================================================" + System.lineSeparator();
		return res;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
}
