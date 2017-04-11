package measures;

public class Measures {
	
	private long i_count = 0, b_count = 0, m_count = 0;
	private String file;
	private int scolumns, srows, width, height, columnOffset, rowOffset;
	
	public Measures() {
		super();
		this.i_count = 0;
		this.b_count = 0;
		this.m_count = 0;
	}
	
	public long getI_count() {
		return i_count;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getScolumns() {
		return scolumns;
	}

	public void setScolumns(int scolumns) {
		this.scolumns = scolumns;
	}

	public int getSrows() {
		return srows;
	}

	public void setSrows(int srows) {
		this.srows = srows;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getColumnOffset() {
		return columnOffset;
	}

	public void setColumnOffset(int columnOffset) {
		this.columnOffset = columnOffset;
	}

	public int getRowOffset() {
		return rowOffset;
	}

	public void setRowOffset(int rowOffset) {
		this.rowOffset = rowOffset;
	}

	public void setI_count(long i_count) {
		this.i_count = i_count;
	}

	public long getB_count() {
		return b_count;
	}

	public void setB_count(long b_count) {
		this.b_count = b_count;
	}

	public long getM_count() {
		return m_count;
	}

	public void setM_count(long m_count) {
		this.m_count = m_count;
	}
	public void reset(){
		this.i_count = 0;
		this.b_count = 0;
		this.m_count = 0;
		this.file=null;
		this.scolumns=0; 
		this.srows=0; 
		this.width=0; 
		this.height=0;
		this.columnOffset=0;
		this.rowOffset=0;	
	}
	public void raytracerInput(String file, int scolumns, int srows,int width,int height,int columnOffset,int rowOffset){
		this.file=file;
		this.scolumns=scolumns; 
		this.srows=srows; 
		this.width=width; 
		this.height=height;
		this.columnOffset=columnOffset;
		this.rowOffset=rowOffset;
	}
}
