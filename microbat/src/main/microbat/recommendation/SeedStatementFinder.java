package microbat.recommendation;

import java.util.List;

import microbat.codeanalysis.bytecode.CallGraph;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.strategies.dto.AppJavaClassPath;

public class SeedStatementFinder {

	public List<ClassLocation> findSeedStatemets(VarValue specificVar, TraceNode start, TraceNode end) {
		Trace trace = start.getTrace();
		AppJavaClassPath appClassPath = trace.getAppJavaClassPath();
		
		List<BreakPoint> collectedBreakPoints = trace.allLocations();
		
		long t1 = System.currentTimeMillis();
		CallGraph callGraph = buildCallGraph(collectedBreakPoints, appClassPath);
		long t2 = System.currentTimeMillis();
		System.out.println("Time for building call graph: " + (t2-t1));
		
		List<ClassLocation> variableDefs = matchDefinition(callGraph, specificVar);
		
		return variableDefs;
	}

	private List<ClassLocation> matchDefinition(CallGraph callGraph, VarValue specificVar) {
		// TODO Auto-generated method stub
		return null;
	}

	private CallGraph buildCallGraph(List<BreakPoint> collectedBreakPoints, AppJavaClassPath appClassPath) {
		CallGraph graph = new CallGraph(appClassPath);
		
		for(BreakPoint location: collectedBreakPoints){
			graph.findOrCreateMethodNode(location);
		}
		
		return graph;
	}

}
