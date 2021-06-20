package microbat.model;

import java.util.ArrayList;
import java.util.List;

import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * This class represents the attribution relation among variables. If a variable is considered wrong, it is possible
 * caused by other wrong variable. If it is the case, we say that the incorrectness of the variable is attributed to
 * other wrong variable. Based on such relation, we are able to build a graph in which each node is a variable and each
 * edge is an attribution relation. Note that, given that each variable will be assigned only once (see SSA form), thus,
 * the graph we build should be acyclic.
 * @author "linyun"
 *
 */
public class AttributionVar {
	private String varID;
	private int checkTime;
	private VarValue value;
	
	/**
	 * record the latest trace node reading this variable in the process of user's debugging.
	 */
	private TraceNode latesetReadTraceNode;
	
	/**
	 * parent variable means the variable used to define this variable.
	 */
	private List<AttributionVar> parents = new ArrayList<>();
	/**
	 * child variable means the variable is defined by using this variable.
	 */
	private List<AttributionVar> children = new ArrayList<>();
	
	public AttributionVar(int checkTime, VarValue value) {
		super();
		this.varID = value.getVarID();
		this.checkTime = checkTime;
		this.setValue(value);
	}
	
	@Override
	public String toString(){
		return this.varID;
	}
	
	public AttributionVar findChild(String varID){
		if(this.varID.equals(varID)){
			return this;
		}
		else{
			for(AttributionVar var: this.children){
				AttributionVar foundVar = var.findChild(varID);
				if(foundVar != null){
					return foundVar;
				}
			}
		}
		
		return null;
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
		AttributionVar other = (AttributionVar) obj;
		if (varID == null) {
			if (other.varID != null)
				return false;
		} else if (!varID.equals(other.varID))
			return false;
		return true;
	}

	public String getVarID() {
		return varID;
	}

	public void setVarID(String varID) {
		this.varID = varID;
	}

	public List<AttributionVar> getParents() {
		return parents;
	}

	public void setParents(List<AttributionVar> parents) {
		this.parents = parents;
	}

	public List<AttributionVar> getChildren() {
		return children;
	}

	public void setChildren(List<AttributionVar> children) {
		this.children = children;
	}
	
	public void addChild(AttributionVar child){
		if(!this.children.contains(child)){
			this.children.add(child);
		}
	}
	
	public void addParent(AttributionVar parent){
		if(!this.parents.contains(parent)){
			this.parents.add(parent);
		}
	}

	public int getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(int checkTime) {
		this.checkTime = checkTime;
	}

	public TraceNode getReadTraceNode() {
		return latesetReadTraceNode;
	}

	public void setReadTraceNode(TraceNode readTraceNode) {
		if(readTraceNode.isReadVariablesContains(varID)){
			this.latesetReadTraceNode = readTraceNode;			
		}
	}

	public VarValue getValue() {
		return value;
	}

	public void setValue(VarValue value) {
		this.value = value;
	}
}	
