package metrics;

/**
 * Normalized window to easily compare windows.
 * (0,0) is in top-left corner.
 * @author Andre
 * 
 */
public class NormalizedWindow {

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
	public int hashCode() {
		return super.hashCode();
	}
	
}
