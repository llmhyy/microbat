package microbat.recommendation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import microbat.model.ClassLocation;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.MicroBatUtil;
import microbat.util.Settings;
import sav.strategies.dto.AppJavaClassPath;
import soot.Scene;
import soot.options.Options;

/**
 * This detail inspector provides a more intelligent way to recommend 
 * @author Yun Lin
 *
 */
public class AdvancedDetailInspector extends DetailInspector {

	@Override
	public TraceNode recommendDetailNode(TraceNode currentNode, Trace trace) {
		analysis();
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
	
	public void analysis(){
		
		if(this.inspectingRange == null){
			return;
		}
		
		TraceNode start = this.inspectingRange.startNode;
		TraceNode end = this.inspectingRange.endNode;
		
		VarValue specificVar = findSpecificWrongVar(start, end);
		
		List<ClassLocation> seedStatements = findSeedStatemets(specificVar, start, end);
		
		System.out.println("call graph is built!");
	}
	
	private VarValue findSpecificWrongVar(TraceNode start, TraceNode end){
		VarValue writtenVar = start.getWrittenVariables().get(0);
		List<VarValue> wrongReadVarList = end.getWrongReadVars(Settings.interestedVariables);
		List<VarValue> correctWrittenVarList = new ArrayList<>();
		correctWrittenVarList.add(writtenVar);
		List<VarValue> writtenChildrenVarList = writtenVar.getAllDescedentChildren();
		correctWrittenVarList.addAll(writtenChildrenVarList);
		
		VarValue specificWrongVar = null;
		for(VarValue wrongReadVar: wrongReadVarList){
			for(VarValue correctWrittenVar: correctWrittenVarList){
				if(wrongReadVar.equals(correctWrittenVar)){
					specificWrongVar = wrongReadVar;
				}
			}
		}
		
		return specificWrongVar;
	}


	private List<ClassLocation> findSeedStatemets(VarValue specificVar, TraceNode start, TraceNode end) {
		AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
		
		String classPathString = appClassPath.getClasspathStr();
		String rtJar = appClassPath.getJavaHome() + File.separator + "jre" + File.separator + "lib" + File.separator + "rt.jar";
		classPathString += File.pathSeparator + rtJar;
		
		Scene.v().setSootClassPath(classPathString);
		Options.v().set_keep_line_number(true);
		Options.v().set_debug(true);
		Options.v().set_via_shimple(true);
		Options.v().setPhaseOption("jb", "use-original-names");
		
		
		
		return null;
	}

}
