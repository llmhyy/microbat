package microbat.recommendation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSACFG.BasicBlock;

import microbat.codeanalysis.bytecode.WALAByteCodeAnalyzer;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.util.MicroBatUtil;
import sav.strategies.dto.AppJavaClassPath;

public class AdvancedDetailInspector extends DetailInspector {

	@Override
	public TraceNode recommendDetailNode(TraceNode currentNode, Trace trace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DetailInspector clone() {
		DetailInspector inspector = new AdvancedDetailInspector();
		if(this.inspectingRange != null){
			inspector.setInspectingRange(this.inspectingRange.clone());			
		}
		return inspector;
	}
	
	public void analysis() throws ClassHierarchyException{
		AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
		AnalysisScope scope = WALAByteCodeAnalyzer.makeJ2SEAnalysisScope(appClassPath);
		IClassHierarchy cha = ClassHierarchy.make(scope);
		
		List<BreakPoint> entryPoints = WALAByteCodeAnalyzer.parseLanuchPoints(appClassPath);
		Iterable<Entrypoint> entrypoints = WALAByteCodeAnalyzer.makeEntrypoints(scope.getApplicationLoader(), cha, entryPoints);
		
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		SSAPropagationCallGraphBuilder builder = Util.makeNCFABuilder(1, options, new AnalysisCache(), cha, scope);
		
		builder.setInstanceKeys(new ZeroXInstanceKeys(options, cha, builder.getContextInterpreter(), ZeroXInstanceKeys.ALLOCATIONS
				 | ZeroXInstanceKeys.SMUSH_MANY /* | ZeroXInstanceKeys.SMUSH_PRIMITIVE_HOLDERS */ | ZeroXInstanceKeys.SMUSH_STRINGS
				 | ZeroXInstanceKeys.SMUSH_THROWABLES));
		
		System.out.println("builder is set.");
		
		try {
			CallGraph callGraph = builder.makeCallGraph(options, null);
			
			for (Iterator<? extends CGNode> it = callGraph.iterator(); it.hasNext();) {
				CGNode n = it.next();
				IMethod method = n.getMethod();
				
				if(method instanceof ShrikeBTMethod && n.getIR()!=null){
					IR ir = n.getIR();
					SSACFG cfg = ir.getControlFlowGraph();
					
					ShrikeBTMethod btMethod = (ShrikeBTMethod)n.getMethod();
					SSAInstruction[] instructions = ir.getInstructions();
					
					List<Statement> stmts = new ArrayList<Statement>();
					for (int i = 0; i <= cfg.getMaxNumber(); i++) {
						BasicBlock bb = cfg.getNode(i);
						
						for(SSAInstruction instruction: bb.getAllInstructions()){
							try {
								if(instruction.iindex != -1){
									int bcIdx = btMethod.getBytecodeIndex(instruction.iindex);
									int lineNumber = btMethod.getLineNumber(bcIdx);
									
									
									System.currentTimeMillis();
									
								}
								
							} catch (InvalidClassFileException e) {
								e.printStackTrace();
							}
						}
					}
				}
				
			}
			
			
		} catch (IllegalArgumentException | CallGraphBuilderCancelException e) {
			e.printStackTrace();
		}
		
		System.out.println("call graph is built!");
	}

}
