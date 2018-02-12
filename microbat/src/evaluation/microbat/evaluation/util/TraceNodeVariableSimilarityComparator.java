package microbat.evaluation.util;

import java.util.Comparator;
import java.util.List;

import microbat.Activator;
import microbat.algorithm.graphdiff.HierarchyGraphDiffer;
import microbat.algorithm.graphdiff.SortedGraphMatcher;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.GraphNode;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.value.VirtualValue;
import microbat.model.variable.Variable;
import microbat.util.MicroBatUtil;

/**
 * This class is used to compare the variable difference between two trace node. If two
 * trace nodes in the same source code location have the same read and written variables,
 * their commonality, or similarity, is 1. Otherwise, a similarity value ranging from 0
 * to 1, indicating how similar they are, will be returned.
 * 
 * @author Yun Lin
 *
 */
public class TraceNodeVariableSimilarityComparator implements TraceNodeSimilarityComparator{

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
			//TODO I may need to distinguish virtual variable of primitive type or object type.
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
					ReferenceValue refVar1 = (ReferenceValue)var1;
					setChildren(refVar1, node1);
					ReferenceValue refVar2 = (ReferenceValue)var2;
					setChildren(refVar2, node2);
					
					if(refVar1.getChildren() != null && refVar2.getChildren() != null){
						HierarchyGraphDiffer differ = new HierarchyGraphDiffer();
						SortedGraphMatcher sortedMatcher = new SortedGraphMatcher(new Comparator<GraphNode>() {
							@Override
							public int compare(GraphNode o1, GraphNode o2) {
								if(o1 instanceof VarValue && o2 instanceof VarValue){
									return ((VarValue)o1).getVarName().compareTo(((VarValue)o2).getVarName());									
								}
								return 0;
							}
						});
						
						differ.diff(var1, var2, true, sortedMatcher, /*EvaluationSettings.variableComparisonDepth*/-1);
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
//				varID = varID.substring(0, varID.indexOf(":"));
				varID = Variable.truncateSimpleID(varID);
				
				VarValue vv = node.getProgramState().findVarValue(varID);
				if(vv != null){
					List<VarValue> retrievedChildren = vv.getAllDescedentChildren();
					
					MicroBatUtil.assignWrittenIdentifier(retrievedChildren, node);
					
					refVar.setChildren(vv.getChildren());
				}				
			}
		}
	}
}
