package microbat.probability.SPP.vectorization.vector;

public abstract class Vector {
	
	protected float[] vector;
	
	public Vector() {
		this.vector = new float[0];
	}
	
	public Vector(float[] vector) {
		this.vector = vector;
	}
	
	public int getSize() {
		return this.vector.length;
	}
	
	public float[] getVector() {
		return this.vector;
	}
	
	protected void set(final int idx) {
		this.vector[idx] = 1.0f;
	}
	
	@Override
	public String toString() {
		if (this.vector == null || this.vector.length == 0) {
			return "";
		}
		
		StringBuilder strBuilder = new StringBuilder();
		for (float element : this.vector) {
			strBuilder.append(element);
			strBuilder.append(",");
		}
		
		strBuilder.deleteCharAt(strBuilder.length()-1);
		return strBuilder.toString();
	}
//	protected ArrayList<E> vector;
//	
//	public Vector() {
//		this.vector = null;
//	}
//	
//	public Vector(ArrayList<E> vector) {
//		this.vector = vector;
//	}
//	
//	public int getSize() {
//		return this.vector.size();
//	}
//	
//	public ArrayList<E> getVector() {
//		return this.vector;
//	}
//	
//	abstract protected float[] toFloat();
//	
//	@Override
//	public String toString() {
//		if (this.vector == null || this.vector.isEmpty()) {
//			return "";
//		}
//		StringBuilder strBuilder = new StringBuilder();
//		for (float element : this.toFloat()) {
//			strBuilder.append(element);
//			strBuilder.append(",");
//		}
//		strBuilder.deleteCharAt(strBuilder.length()-1);
//		return strBuilder.toString();
//		
//	}
}
