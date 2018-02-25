package microbat.recommendation.calculator;

public class Dependency {
	int dataDependency;
	int controlDependency;

	public Dependency(int dataDependency, int controlDependency) {
		super();
		this.dataDependency = dataDependency;
		this.controlDependency = controlDependency;
	}

	public int getDataDependency() {
		return dataDependency;
	}

	public void setDataDependency(int dataDependency) {
		this.dataDependency = dataDependency;
	}

	public int getControlDependency() {
		return controlDependency;
	}

	public void setControlDependency(int controlDependency) {
		this.controlDependency = controlDependency;
	}

	
}
