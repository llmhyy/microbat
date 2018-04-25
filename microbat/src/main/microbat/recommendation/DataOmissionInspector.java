package microbat.recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.InstructionHandle;

import microbat.codeanalysis.bytecode.MethodNode;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * This detail inspector provides a more intelligent way to recommend 
 * @author Yun Lin
 *
 */
public class DataOmissionInspector extends DetailInspector {

	public DataOmissionInspector(){
	}
	
	@Override
	public TraceNode recommendDetailNode(TraceNode currentNode, Trace trace, VarValue wrongValue) {
		List<TraceNode> controlDominators = analyze(wrongValue);
		if(controlDominators.isEmpty()){
			return null;			
		}
		else{
			return controlDominators.get(0);
		}
	}

	@Override
	public DetailInspector clone() {
		DetailInspector inspector = new DataOmissionInspector();
		if(this.inspectingRange != null){
			inspector.setInspectingRange(this.inspectingRange.clone());			
		}
		return inspector;
	}
	
	public List<TraceNode> analyze(VarValue wrongValue){	
		
		if(this.inspectingRange == null){
			return new ArrayList<>();
		}
		
		TraceNode start = this.inspectingRange.startNode;
		TraceNode end = this.inspectingRange.endNode;
		
//		VarValue specificVar = findSpecificWrongVar(start, end);
		
		SeedStatementFinder seedFinder = new SeedStatementFinder();
		Map<MethodNode, List<InstructionHandle>> seeds = seedFinder.findSeedStatemets(wrongValue, start, end);
		if(seeds==null){
			return new ArrayList<>();
		}
		
		Trace trace = start.getTrace();
		SeedControlDominatorFinder controlFinder = new SeedControlDominatorFinder();
		List<BreakPoint> controlPoints = controlFinder.findSeedControlDominators(trace.allLocations(), seeds);
		
		List<TraceNode> controlDominators = new ArrayList<>();
		for(int i=end.getOrder(); i>=start.getOrder(); i--){
			TraceNode node = trace.getTraceNode(i);
			
//			if(node.getOrder()==72027){
//				System.out.println(controlPoints.contains(node.getBreakPoint()));
//			}
			
			if(controlPoints.contains(node.getBreakPoint())){
				if(!isExecutedPotentialSeeds(node, seeds, end.getOrder())){
					controlDominators.add(node);						
				}
//				List<TraceNode> allControlDominatees = node.findAllControlDominatees();
//				if(!contains(allControlDominatees, controlDominators)){
//					if(!isExecutedPotentialSeeds(node, seeds, end.getOrder())){
//						controlDominators.add(node);						
//					}
//				}
			}
		}
		return controlDominators;
	}
	
	private boolean isExecutedPotentialSeeds(TraceNode controlDominator,
			Map<MethodNode, List<InstructionHandle>> seeds, int endOrder) {
		
		for(MethodNode method: seeds.keySet()){
			if(method.getMethodSign().equals(controlDominator.getMethodSign())){
				LineNumberTable table = method.getMethod().getLineNumberTable();
				List<InstructionHandle> sites = seeds.get(method);
				for(InstructionHandle site: sites){
					int siteLine = table.getSourceLine(site.getPosition());
					//TODO need to find whether siteLine need to revert the boolean
					//evaluation of the controlDominator.
					if(isExecute(siteLine, controlDominator.getControlDominatees(), endOrder)){
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private boolean isExecute(int siteLine, List<TraceNode> controlDominatees, int endOrder) {
		for(TraceNode node: controlDominatees){
			if(node.getLineNumber()==siteLine && node.getOrder()<=endOrder){
				return true;
			}
		}
		return false;
	}

	private boolean contains(List<TraceNode> controlDominatees, List<TraceNode> controlDominators) {
		for(TraceNode controlDominatee: controlDominatees){
			for(TraceNode controlDominator: controlDominators){
				if(controlDominatee.equals(controlDominator)){
					return true;
				}
			}
		}
		return false;
	}

//	private VarValue findSpecificWrongVar(TraceNode start, TraceNode end){
//		VarValue writtenVar = start.getWrittenVariables().get(0);
//		List<VarValue> wrongReadVarList = end.getWrongReadVars(Settings.interestedVariables);
//		List<VarValue> correctWrittenVarList = new ArrayList<>();
//		correctWrittenVarList.add(writtenVar);
//		List<VarValue> writtenChildrenVarList = writtenVar.getAllDescedentChildren();
//		correctWrittenVarList.addAll(writtenChildrenVarList);
//		
////		VarValue specificWrongVar = null;
////		for(VarValue wrongReadVar: wrongReadVarList){
////			for(VarValue correctWrittenVar: correctWrittenVarList){
////				if(wrongReadVar.equals(correctWrittenVar)){
////					specificWrongVar = wrongReadVar;
////				}
////			}
////		}
//		
//		return wrongReadVarList.get(0);
//	}

}
