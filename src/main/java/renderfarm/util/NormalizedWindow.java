package renderfarm.util;

/**
 * Normalized window to easily compare windows.
 * (0,0) is in the bottom-left corner.
 *  ________________________
 * |(0,1)			   (1,1)|
 * |						|
 * |	height				|
 * |	|					|
 * |	(x,y)--width		|
 * |						|
 * |(0,0)______________(1,0)|
 * 
 * @author Andre
 * 
 */
public class NormalizedWindow {

	private static final double WINDOW_ERRORS = 0.000001;
	
	private float x;
	
	private float y;
	
	private float width;
	
	private float height;
	
	public NormalizedWindow(float x,float y,float width,float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public NormalizedWindow(){
		this.x = 0;
		this.y = 0;
		this.width = 0;
		this.height = 0;
	}
	
	/**
	 * Built a normalized window based on the scene and viewport size/position
	 * @param sceneWidth
	 * @param sceneHeight
	 * @param windowWidth
	 * @param windowHeight
	 * @param collumnOffset
	 * @param rowOffset
	 * @return
	 */
	public static NormalizedWindow BuildNormalizedWindow(long sceneWidth,long sceneHeight,
			long windowWidth,long windowHeight,long collumnOffset,long rowOffset) {
		float x = collumnOffset / (float) sceneWidth;
		float y = rowOffset / (float) sceneHeight;
		float width = windowWidth / (float) sceneWidth;
		float height = windowHeight / (float) sceneHeight;
		return new NormalizedWindow(x, y, width, height);
	}
	
	/**
	 * Normalized area that window overlapp the this window.
	 * @param window
	 * @return Normalized overlapping area
	 */
	public float normalizedAreaOverlapping(NormalizedWindow window) {
		float maxX = Math.max(this.getX(), window.getX());
		float maxY = Math.max(this.getY(), window.getY());
		
		float newWidth = Math.min(this.getX() + this.getWidth(), window.getX() + window.getWidth()) - maxX;
		float newHeight = Math.min(this.getY() + this.getHeight(), window.getY() + window.getHeight()) - maxY;
		
		if (newWidth <= 0f || newHeight <= 0f) {
			return 0;
		} else {
			return (newWidth * newHeight);
		}
	}
	
	/**
	 * Compute the area of the normalized window
	 * @return Normalized area of the window
	 */
	public float getArea() {
		return getWidth() * getHeight();
	}
	
	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public void setHeight(float height) {
		this.height = height;
	}
	
	@Override
	public String toString(){
		return "X: " + x + " Y: " + y + " Width: " + width + " Height: " + height;
	}
	
	@Override
	public boolean equals(Object obj) {
		NormalizedWindow nw = (NormalizedWindow) obj;
		boolean x = Math.abs(this.getX() - nw.getX()) < WINDOW_ERRORS;
		boolean y = Math.abs(this.getY() - nw.getY()) < WINDOW_ERRORS;
		boolean w = Math.abs(this.getHeight() - nw.getHeight()) < WINDOW_ERRORS;
		boolean h = Math.abs(this.getWidth() - nw.getWidth()) < WINDOW_ERRORS;
		return x && y && w && h;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
}
