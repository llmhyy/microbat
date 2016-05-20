package microbat.evaluation.util;

import java.util.List;

import microbat.algorithm.graphdiff.HierarchyGraphDiffer;
import microbat.model.trace.TraceNode;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.value.VirtualValue;

public class TraceNodeSimilarityComparator {

	public double compute(TraceNode traceNode1, TraceNode traceNode2) {
		
		if(traceNode1.getOrder() == 103 && traceNode2.getOrder() == 95){
			System.currentTimeMillis();
		}
		if(traceNode1.getOrder() == 103 && traceNode2.getOrder() == 194){
			System.currentTimeMillis();
		}
		
		if(traceNode1.hasSameLocation(traceNode2)){
			double commonReadVarWithSameValue = findCommonVarWithSameValue(traceNode1.getReadVariables(), traceNode1,
					traceNode2.getReadVariables(), traceNode2);
			double commonWrittenVarWithSameValue = findCommonVarWithSameValue(traceNode1.getWrittenVariables(), traceNode1,
					traceNode2.getWrittenVariables(), traceNode2);
			
			int totalVars = traceNode1.getReadVariables().size() + traceNode1.getWrittenVariables().size() +
					traceNode2.getWrittenVariables().size() + traceNode2.getReadVariables().size();
			
			double simVarScore; 
			if(totalVars == 0){
				simVarScore = 1;
			}
			else{
				simVarScore = (2*(double)commonReadVarWithSameValue+2*commonWrittenVarWithSameValue)/totalVars;
			}
			
//			int len1 = traceNode1.findTraceLength();
//			int len2 = traceNode2.findTraceLength();
//			int len = (len1 > len2) ? len1 : len2;
//			double diffLoc = Math.abs(traceNode1.getOrder() - traceNode2.getOrder());
//			double simLocationScore = 1 - diffLoc/len;
			
			/**
			 * give a value for same location similarity
			 */
//			return 0.05 + 0.5*simVarScore + 0.45*simLocationScore;
			return 0.05 + 0.95*simVarScore;
		}
		
		return 0;
	}

	private double findCommonVarWithSameValue(List<VarValue> variables1, TraceNode node1, 
			List<VarValue> variables2, TraceNode node2) {
		double common = 0;
		for(VarValue var1: variables1){
			for(VarValue var2: variables2){
				double commonness = findCommonness(var1, node1, var2, node2);
				if(commonness > 0){
					common += commonness;
					break;
				}
//				if(isCommon(var1, var2)){
//					common++;
//					break;
//				}
			}
		}
		return common;
	}

//	private boolean hasSameReadVars(TraceNode node1, TraceNode node2){
//		List<VarValue> var1s = node1.getReadVariables();
//		List<VarValue> var2s = node2.getReadVariables();
//		
//		boolean isExactlyTheSame = isExactlyTheSame(var1s, node1, var2s, node2);
//		return isExactlyTheSame;
//	}
	
//	@SuppressWarnings("unchecked")
//	private boolean isExactlyTheSame(List<VarValue> var1s, TraceNode node1, List<VarValue> var2s, TraceNode node2) {
//		ArrayList<VarValue> clonedVar1s = (ArrayList<VarValue>) ((ArrayList<VarValue>)var1s).clone();
//		ArrayList<VarValue> clonedVar2s = (ArrayList<VarValue>) ((ArrayList<VarValue>)var2s).clone();
//		
//		Iterator<VarValue> iter1 = clonedVar1s.iterator();
//		while(iter1.hasNext()){
//			VarValue var1 = iter1.next();
//			
//			Iterator<VarValue> iter2 = clonedVar2s.iterator();
//			while(iter2.hasNext()){
//				VarValue var2 = iter2.next();
//				
//				if(isCommon(var1, var2)){
//					iter1.remove();
//					iter2.remove();
//					break;
//				}
//			}
//		}
//		
//		return clonedVar1s.isEmpty() && clonedVar2s.isEmpty();
//	}

	private double findCommonness(VarValue var1, TraceNode node1, VarValue var2, TraceNode node2) {
		double commonness = 0;
		
		if(var1 instanceof VirtualValue && var2 instanceof VirtualValue){
			if(var1.getStringValue().equals(var2.getStringValue())){
				commonness = 1;
			}
		}
		else{
			boolean isSameName = var1.getVarName().equals(var2.getVarName());
			if(isSameName){
				commonness += 0.5;
				if(!(var1 instanceof ReferenceValue) && !(var2 instanceof ReferenceValue)){
					String str1 = var1.getStringValue();
					String str2 = var2.getStringValue();
					if(str1 == null && str2 == null){
						commonness += 0.5;
					}
					else if(str1 != null && str2 != null){
						if(str1.equals(str2)){
							commonness += 0.5;
						}						
					}
				}
				else if((var1 instanceof ReferenceValue) && (var2 instanceof ReferenceValue)){
					
					System.currentTimeMillis();
					
					ReferenceValue refVar1 = (ReferenceValue)var1;
					setChildren(refVar1, node1);
					ReferenceValue refVar2 = (ReferenceValue)var2;
					setChildren(refVar2, node2);
					
					if(refVar1.getChildren() != null && refVar2.getChildren() != null){
						System.currentTimeMillis();
						
						HierarchyGraphDiffer differ = new HierarchyGraphDiffer();
						differ.diff(var1, var2);
						if(differ.getDiffs().isEmpty()){
							commonness += 0.5;						
						}							
					}
					else if(refVar1.getChildren() == null && refVar2.getChildren() == null){
						commonness += 0.5;	
					}
				}
			}
		}
		
		return commonness;
	}

	private void setChildren(ReferenceValue refVar, TraceNode node){
		if(refVar.getChildren()==null){
			if(node.getProgramState() != null){
				
				String varID = refVar.getVarID();
				varID = varID.substring(0, varID.indexOf(":"));
				
				VarValue vv = node.getProgramState().findVarValue(varID);
				if(vv != null){
					refVar.setChildren(vv.getChildren());
				}				
			}
		}
	}
}
