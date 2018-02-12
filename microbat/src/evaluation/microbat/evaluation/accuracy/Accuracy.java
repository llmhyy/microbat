package microbat.evaluation.accuracy;

public class Accuracy {
	private double precision;
	private double recall;

	public Accuracy(double precision, double recall) {
		super();
		this.precision = precision;
		this.recall = recall;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(precision);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(recall);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Accuracy other = (Accuracy) obj;
		if (Double.doubleToLongBits(precision) != Double
				.doubleToLongBits(other.precision))
			return false;
		if (Double.doubleToLongBits(recall) != Double
				.doubleToLongBits(other.recall))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Accuracy [precision=" + precision + ", recall=" + recall + "]";
	}

	
}
