package microbat.vectorization.vector;

public abstract class HistogramVector {

	protected int[] vector;
	
	public HistogramVector() {
		this.vector = new int[0];
	}
	
	public HistogramVector(final int[] histogram) {
		this.vector =  histogram;
	}
	
	public int[] getVector() {
		return this.vector;
	}
	
	public int getSize() {
		return this.vector.length;
	}
	
	@Override
	public String toString() {
		if (this.vector.length <= 0) {
			return "";
		}
		
		StringBuilder strBuilder = new StringBuilder();
		for (int count : this.vector) {
			strBuilder.append(count);
			strBuilder.append(",");
		}
		strBuilder.deleteCharAt(strBuilder.length()-1);
		return strBuilder.toString();
	}
	
}
