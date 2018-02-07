package microbat.recommendation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;

import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CallGraph;
import microbat.codeanalysis.bytecode.MethodFinderByLine;
import microbat.codeanalysis.bytecode.MethodNode;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import sav.strategies.dto.AppJavaClassPath;

public class SeedStatementFinder {

	private CallGraph graph;
	
	public Map<MethodNode, List<InstructionHandle>> findSeedStatemets(VarValue specificVar, TraceNode start, TraceNode end) {
		Trace trace = start.getTrace();
		Variable var = specificVar.getVariable();
		if(var instanceof FieldVar || var instanceof ArrayElementVar){
			Map<MethodNode, List<InstructionHandle>> seeds = matchVarWithCallGraph(trace, specificVar);
			return seeds;
		}
		else if(var instanceof LocalVar){
			Map<MethodNode, List<InstructionHandle>> seeds = matchVarLocally(start, specificVar);
			return seeds;
		}
		
		return null;
		
	}

	private Map<MethodNode, List<InstructionHandle>> matchVarLocally(TraceNode start, VarValue specificVar) {
		Map<MethodNode, List<InstructionHandle>> map = new HashMap<>();
		
		Method method = new CallGraph(start.getTrace().getAppJavaClassPath()).findByteCodeMethod(start.getBreakPoint());
		
		MethodNode methodNode = new MethodNode(start.getMethodSign(), method);
		List<InstructionHandle> list = methodNode.findVariableDefinition(specificVar.getVariable());
		
		System.currentTimeMillis();
		
		map.put(methodNode, list);
		
		return map;
	}

	private Map<MethodNode, List<InstructionHandle>> matchVarWithCallGraph(Trace trace, VarValue specificVar) {
		AppJavaClassPath appClassPath = trace.getAppJavaClassPath();
		
		List<BreakPoint> collectedBreakPoints = trace.allLocations();
		Set<MethodNode> executedAppMethods = new HashSet<>();
		
		long t1 = System.currentTimeMillis();
		if(graph==null){
			graph = new CallGraph(appClassPath);
			
			for(BreakPoint location: collectedBreakPoints){
				MethodNode node = graph.findOrCreateMethodNode(location);
				executedAppMethods.add(node);
			}
		}
		
		long t2 = System.currentTimeMillis();
		System.out.println("Time for building call graph: " + (t2-t1));
		
		List<MethodNode> seeds = matchVariableDefinition(graph, specificVar.getVariable());
		Map<MethodNode, List<InstructionHandle>> allSeeds = 
				findAllSeedMethods(seeds, executedAppMethods, specificVar.getVariable());
		
		return allSeeds;
	}

	private Map<MethodNode, List<InstructionHandle>> findAllSeedMethods(
			List<MethodNode> seedMethods, Set<MethodNode> executedAppMethods, Variable var) {
		Map<MethodNode, List<InstructionHandle>> allSeedMethods = new HashMap<>();
		for(MethodNode node: seedMethods){
			if(executedAppMethods.contains(node)){
				List<InstructionHandle> defs = node.findVariableDefinition(var);
				allSeedMethods.put(node, defs);
			}
			
			System.currentTimeMillis();
			Map<MethodNode, List<InstructionHandle>> allCallers = node.getAllCallers();
			for(MethodNode caller: allCallers.keySet()){
				if(executedAppMethods.contains(caller)){
					List<InstructionHandle> hList = allCallers.get(caller);
					allSeedMethods.put(caller, hList);
				}
			}
		}
		
		Iterator<MethodNode> iter = allSeedMethods.keySet().iterator();
		while(iter.hasNext()){
			MethodNode node = iter.next();
			if(!executedAppMethods.contains(node)){
				iter.remove();
			}
		}
		
		return allSeedMethods;
	}

	
	
	private List<MethodNode> matchVariableDefinition(CallGraph callGraph, Variable var) {
		List<MethodNode> list = new ArrayList<>();
		for(String methodSign: callGraph.getMethodMaps().keySet()){
			MethodNode method = callGraph.getMethodMaps().get(methodSign);
			if(method.getMethod().getCode()!=null){
				List<InstructionHandle> defs = method.findVariableDefinition(var);
				if(!defs.isEmpty()){
					list.add(method);
				}
			}
			
		}
		return list;
	}

}
