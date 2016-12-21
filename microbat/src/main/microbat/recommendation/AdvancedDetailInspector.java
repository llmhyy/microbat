package microbat.recommendation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.HeapExclusions;
import com.ibm.wala.ipa.slicer.PDG;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.intset.OrdinalSet;

import microbat.codeanalysis.bytecode.WALAByteCodeAnalyzer;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.util.MicroBatUtil;
import sav.strategies.dto.AppJavaClassPath;

public class AdvancedDetailInspector extends DetailInspector {

	class LineDependency{
		
		int dominatorLine;
		int dominateeLine;
		String type;
		
		public LineDependency(int dominatorLine, int dominateeLine, String type){
			this.dominatorLine = dominatorLine;
			this.dominateeLine = dominateeLine;
			this.type = type;
		}
		
		public String toString(){
			StringBuffer buffer = new StringBuffer();
			buffer.append("dominator: " + dominatorLine + "; ");
			buffer.append("dominatee: " + dominateeLine + "; ");
			buffer.append("type: " + type + " ");
			return buffer.toString();
		}
		
		public boolean equals(Object obj){
			if(obj instanceof LineDependency){
				LineDependency other = (LineDependency)obj;
				if(other.dominatorLine==this.dominatorLine && other.dominateeLine==this.dominateeLine
						&& other.type.equals(this.type)){
					return true;
				}
			}
			
			return false;
		}
	}
	
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
	
	public void analysis() throws ClassHierarchyException, InvalidClassFileException{
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
			PointerAnalysis<InstanceKey> pointerAnalysis = builder.getPointerAnalysis();
			HeapExclusions heapExclude = null;
			ModRef modRef = ModRef.make();
			
			Map<CGNode, OrdinalSet<PointerKey>> mod = modRef.computeMod(callGraph, pointerAnalysis, heapExclude);
			Map<CGNode, OrdinalSet<PointerKey>> ref = modRef.computeRef(callGraph, pointerAnalysis, heapExclude); 

			Set<IMethod> analyzedCloneMethods = HashSetFactory.make();
			
			ArrayList<LineDependency> list = new ArrayList<>();
			
			for (Iterator<? extends CGNode> it = callGraph.iterator(); it.hasNext();) {
				CGNode n = it.next();
				
				IMethod method = n.getMethod();
				
				if(method instanceof ShrikeBTMethod && n.getIR()!=null){
					IR ir = n.getIR();
					SSACFG cfg = ir.getControlFlowGraph();
					
					ShrikeBTMethod btMethod = (ShrikeBTMethod)n.getMethod();
					
					if(!btMethod.getDeclaringClass().getReference().getClassLoader().equals(ClassLoaderReference.Application)){
						continue;
					}
					
					PDG pdg = new PDG(n, pointerAnalysis, mod, ref, DataDependenceOptions.FULL, 
							ControlDependenceOptions.FULL, heapExclude, callGraph, modRef);
					
					System.out.println(n.getMethod().getName());
					Iterator<Statement> iterator1 = pdg.iterator();
					while(iterator1.hasNext()){
						Statement startStatement = iterator1.next();
						
						
						Iterator<Statement> successorIterator = pdg.getSuccNodes(startStatement);
						
						while(successorIterator.hasNext()){
							Statement destStatement = successorIterator.next();
							
							int startLineNumber = -1;
							if(startStatement instanceof StatementWithInstructionIndex){
								int startBCIdx = btMethod.getBytecodeIndex(((StatementWithInstructionIndex) startStatement).getInstructionIndex());
								startLineNumber = method.getLineNumber(startBCIdx);								
							}
							
							int endLineNumber = -1;
							if(destStatement instanceof StatementWithInstructionIndex){
								int endBCIdx = btMethod.getBytecodeIndex(((StatementWithInstructionIndex) destStatement).getInstructionIndex());
								endLineNumber = method.getLineNumber(endBCIdx);								
							}
							
							if(startLineNumber != endLineNumber){
								LineDependency dependency;
								if(pdg.isControlDependend(startStatement, destStatement)){
									dependency = new LineDependency(startLineNumber, endLineNumber, "control");
								}
								else{
									dependency = new LineDependency(startLineNumber, endLineNumber, "data");
								}
								if(!list.contains(dependency)){
									list.add(dependency);
									System.out.println(dependency);										
								}
							}
						}
					}
					System.out.println("==========================");
					
					DefUse defUseRelation = n.getDU();
					
					int localVariableNumber = btMethod.getMaxLocals();
					
					int startNum = 2;
					if(method.isStatic()){
						startNum = 1;
					}
					
					/*for(int localVarID=startNum; localVarID<=localVariableNumber; localVarID++){
						SSAInstruction defInstruction = defUseRelation.getDef(localVarID);
						Iterator<SSAInstruction> iterator = defUseRelation.getUses(localVarID);
						
						if(defInstruction!=null && defInstruction.iindex != -1){
							int bcIdx = btMethod.getBytecodeIndex(defInstruction.iindex);
							int lineNumber = method.getLineNumber(bcIdx);
//							String variableName = method.getLocalVariableName(bcIdx, localVarID);
							String[] variableNames = ir.getLocalNames(defInstruction.iindex, localVarID);
							
							StringBuffer buffer = new StringBuffer();
							if(variableNames != null){
								for(String str: variableNames){
									buffer.append(str+";");
								}								
							}
							String vnames = buffer.toString();
							
							System.out.println("method name: " + method + ", line number: " + lineNumber + "; variable name: " + vnames);
							System.currentTimeMillis();
						}
					}*/
				}
				
			}
			
			
		} catch (IllegalArgumentException | CallGraphBuilderCancelException e) {
			e.printStackTrace();
		} /*catch (InvalidClassFileException e) {
			e.printStackTrace();
		}*/
		
		System.out.println("call graph is built!");
	}

}
