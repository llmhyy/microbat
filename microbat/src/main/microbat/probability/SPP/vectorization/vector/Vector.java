package microbat.probability.SPP.vectorization.vector;

import java.util.Arrays;

public class Vector {
	
	protected float[] vector;
	protected final float eps = 0.0001f;
	
	public Vector() {
		this.vector = new float[0];
	}
	
	public Vector(final int size) {
		this.vector = new float[size];
		Arrays.fill(this.vector, 0.0f);
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vector) {
			Vector otherVec = (Vector) obj;
			if (otherVec.vector.length != this.vector.length) {
				return false;
			}
			for (int idx=0; idx<this.vector.length; idx++) {
				final float myElement = this.vector[idx];
				final float otherElement = otherVec.vector[idx];
				if (Math.abs(myElement - otherElement) > this.eps) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
