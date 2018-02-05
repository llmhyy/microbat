package microbat.recommendation;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import microbat.codeanalysis.bytecode.CallGraph;
import microbat.codeanalysis.bytecode.MethodNode;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.FieldVar;
import microbat.model.variable.Variable;
import sav.strategies.dto.AppJavaClassPath;

public class SeedStatementFinder {

	public List<ClassLocation> findSeedStatemets(VarValue specificVar, TraceNode start, TraceNode end) {
		Trace trace = start.getTrace();
		AppJavaClassPath appClassPath = trace.getAppJavaClassPath();
		
		List<BreakPoint> collectedBreakPoints = trace.allLocations();
		
		long t1 = System.currentTimeMillis();
		CallGraph graph = new CallGraph(appClassPath);
		
		List<MethodNode> callSites = new ArrayList<>();
		for(BreakPoint location: collectedBreakPoints){
			MethodNode node = graph.findOrCreateMethodNode(location);
			callSites.add(node);
		}
		
		long t2 = System.currentTimeMillis();
		System.out.println("Time for building call graph: " + (t2-t1));
		
		List<MethodNode> definingMethods = matchDefinition(graph, specificVar);
		
		List<ClassLocation> variableDefs = findVariableDefLocation(definingMethods, callSites);
		
		return variableDefs;
	}

	private List<ClassLocation> findVariableDefLocation(List<MethodNode> definingMethods, List<MethodNode> callSites) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<MethodNode> matchDefinition(CallGraph callGraph, VarValue value) {
		Variable var = value.getVariable();
		if(var instanceof FieldVar){
			FieldVar field = (FieldVar)var;
			List<MethodNode> fieldDefMethods = matchField(callGraph, field);
			return fieldDefMethods;
		}
		
		return null;
	}

	private List<MethodNode> matchField(CallGraph callGraph, FieldVar field) {
		List<MethodNode> list = new ArrayList<>();
		for(String methodSign: callGraph.getMethodMaps().keySet()){
			MethodNode method = callGraph.getMethodMaps().get(methodSign);
			if(method.getMethod().getCode()!=null){
				List<InstructionHandle> defs = method.findFieldDefinition(field);
				if(!defs.isEmpty()){
					list.add(method);
				}
			}
			
		}
		return list;
	}

}
