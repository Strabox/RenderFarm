package metrics;

/**
 * Metric for each request. 
 * @author Andre
 *
 */
public class Metric {

	private String fileName;
	
	private NormalizedWindow normalizedWindow;
	
	private long totalPixelsRendered;
	
	private Measures measures;

	public Metric(String fileName, NormalizedWindow normalizedWindow, long totalPixelsRendered, Measures measures) {
		this.fileName = fileName;
		this.normalizedWindow = normalizedWindow;
		this.totalPixelsRendered = totalPixelsRendered;
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

	public long getTotalPixelsRendered() {
		return totalPixelsRendered;
	}

	public Measures getMeasures() {
		return measures;
	}

	public void setNormalizedWindow(NormalizedWindow normalizedWindow) {
		this.normalizedWindow = normalizedWindow;
	}

	public void setTotalPixelsRendered(long totalPixelsRendered) {
		this.totalPixelsRendered = totalPixelsRendered;
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
		res += "Total Pixels Rendered: " + totalPixelsRendered + System.lineSeparator();
		res += "Measures: " + System.lineSeparator() + measures + System.lineSeparator();
		res += "===============================================================" + System.lineSeparator();
		return res;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
}
