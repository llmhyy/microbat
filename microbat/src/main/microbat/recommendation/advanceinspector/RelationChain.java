package microbat.recommendation.advanceinspector;

import java.util.ArrayList;
import java.util.List;

import microbat.model.value.VarValue;
import soot.Local;

public class RelationChain {
	public static final int FIELD = 1;
	public static final int ARRAY_ELEMENT = 2;
	
	public int searchingIndex = 0;
	
	public Local topLocal;
	
	/**
	 * vars.get(0) represents the leaf var
	 */
	public List<VarValue> vars = new ArrayList<>();
	public List<Integer> relation = new ArrayList<>();
	
	public boolean isSingleElement(){
		return vars.size()==1;
	}
	
	public VarValue getTopVar(){
		return vars.get(vars.size()-1);
	}
	
	public VarValue getLeafVar(){
		return vars.get(0);
	}
	
	public VarValue getWorkingVariable() {
		return vars.get(searchingIndex);
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(int i=vars.size()-1; i>=0; i--){
			buffer.append(vars.get(i));
			if(i>0){
				buffer.append("(~");
				
				String relName = new String();
				int rel = relation.get(i-1);
				if(rel == RelationChain.FIELD){
					relName = "field";
				}
				else if(rel == RelationChain.ARRAY_ELEMENT){
					relName = "arr-element";
				}
				
				buffer.append(relName);			
				buffer.append("~)");
			}
		}
		return buffer.toString();
	}

	
}
