package microbat.model.trace;

import java.util.ArrayList;
import java.util.List;

public class StepVariableRelationEntry {
	private String varID;
	
	/**
	 * Note that a consumer's producer will be its nearest producer on certain variable.
	 */
	private List<TraceNode> producers = new ArrayList<>();
	private List<TraceNode> consumers = new ArrayList<>();
	
	public StepVariableRelationEntry(String varID) {
		super();
		this.varID = varID;
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((varID == null) ? 0 : varID.hashCode());
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
		StepVariableRelationEntry other = (StepVariableRelationEntry) obj;
		if (varID == null) {
			if (other.varID != null)
				return false;
		} else if (!varID.equals(other.varID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StepVariableRelationEntry [varID=" + varID + "]";
	}

	public String getVarID() {
		return varID;
	}

	public void setVarID(String varID) {
		this.varID = varID;
	}

	public List<TraceNode> getProducers() {
		return producers;
	}

	public void setProducers(List<TraceNode> producers) {
		this.producers = producers;
	}

	public List<TraceNode> getConsumers() {
		return consumers;
	}

	public void setConsumers(List<TraceNode> consumer) {
		this.consumers = consumer;
	}

	public void addConsumer(TraceNode node) {
		if(!consumers.contains(node)){
			consumers.add(node);
		}
	}
	
	public void addProducer(TraceNode node) {
		
		if(node.getOrder() == 4){
			System.currentTimeMillis();
		}
		
		if(!producers.contains(node)){
			producers.add(node);
		}
	}
	
}
