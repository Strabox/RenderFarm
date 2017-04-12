package measures;

public class Measures {
	
	private long i_count = 0, b_count = 0, m_count = 0;
	private String file;
	private int scolumns, srows, width, height, columnOffset, rowOffset;
	private long loadcount;
	private long storecount;
	private long fieldloadcount;
	private long fieldstorecount;
	private long newcount;
	private long newarraycount;
	private long anewarraycount;
	private long multianewarraycount;

	
	public Measures() {
		super();
		this.i_count = 0;
		this.b_count = 0;
		this.m_count = 0;
		this.setLoadcount(0);
		this.setStorecount(0);
		this.setFieldloadcount(0);
		this.setFieldstorecount(0);
		this.setNewcount(0);
		this.setNewarraycount(0);
		this.setAnewarraycount(0);
		this.setMultianewarraycount(0);
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
		this.setLoadcount(0);
		this.setStorecount(0);
		this.setFieldloadcount(0);
		this.setFieldstorecount(0);
		this.setNewcount(0);
		this.setNewarraycount(0);
		this.setAnewarraycount(0);
		this.setMultianewarraycount(0);
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

	public long getLoadcount() {
		return loadcount;
	}

	public void setLoadcount(long loadcount) {
		this.loadcount = loadcount;
	}

	public long getStorecount() {
		return storecount;
	}

	public void setStorecount(long storecount) {
		this.storecount = storecount;
	}

	public long getFieldloadcount() {
		return fieldloadcount;
	}

	public void setFieldloadcount(long fieldloadcount) {
		this.fieldloadcount = fieldloadcount;
	}

	public long getFieldstorecount() {
		return fieldstorecount;
	}

	public void setFieldstorecount(long fieldstorecount) {
		this.fieldstorecount = fieldstorecount;
	}

	public long getNewcount() {
		return newcount;
	}

	public void setNewcount(long newcount) {
		this.newcount = newcount;
	}

	public long getNewarraycount() {
		return newarraycount;
	}

	public void setNewarraycount(long newarraycount) {
		this.newarraycount = newarraycount;
	}

	public long getAnewarraycount() {
		return anewarraycount;
	}

	public void setAnewarraycount(long anewarraycount) {
		this.anewarraycount = anewarraycount;
	}

	public long getMultianewarraycount() {
		return multianewarraycount;
	}

	public void setMultianewarraycount(long multianewarraycount) {
		this.multianewarraycount = multianewarraycount;
	}
}
