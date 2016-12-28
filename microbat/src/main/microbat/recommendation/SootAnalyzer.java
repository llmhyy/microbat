package microbat.recommendation;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import microbat.util.MicroBatUtil;
import sav.strategies.dto.AppJavaClassPath;
import soot.Body;
import soot.Local;
import soot.MethodSource;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.options.Options;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

public class SootAnalyzer {

	public void analyze() {
		AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
		
		String classPathString = appClassPath.getClasspathStr();
		String rtJar = appClassPath.getJavaHome() + File.separator + "jre" + File.separator + "lib" + File.separator + "rt.jar";
		classPathString += File.pathSeparator + rtJar;
		
		Scene.v().setSootClassPath(classPathString);
		Options.v().set_keep_line_number(true);
		Options.v().set_debug(true);
		Options.v().set_via_shimple(true);
		Options.v().setPhaseOption("jb", "use-original-names");
		
		SootClass c = Scene.v().loadClassAndSupport(appClassPath.getLaunchClass());
		c.setApplicationClass();
		
		SootMethod method = c.getMethodByName("qSort");
		Body body = method.retrieveActiveBody();
		Chain<Local> chain = body.getLocals();
		for(Local local: chain){
			System.currentTimeMillis();
		}
		
		UnitGraph graph = new ExceptionalUnitGraph(body);
		
		MHGDominatorsFinder<Unit> dominatorFinder = new MHGDominatorsFinder<>(graph);
		
		Iterator<Unit> iterator = graph.iterator();
		while(iterator.hasNext()){
			Unit unit = iterator.next();
			List<Tag> tagList = unit.getTags();
			System.out.println(unit);
			System.out.println("line number: " + tagList.get(0));
			
			List<ValueBox> valueBoxList = unit.getDefBoxes();
			for(ValueBox valueBox: valueBoxList){
				Value value = valueBox.getValue();
				List<Tag> valueTags = valueBox.getTags();
				
				System.currentTimeMillis();
			}
			
			List<ValueBox> usedValueBoxList = unit.getUseBoxes();
			for(ValueBox valueBox: usedValueBoxList){
				Value value = valueBox.getValue();
				List<Tag> valueTags = valueBox.getTags();
				
				System.currentTimeMillis();
			}
			
			List<Unit> dominators = dominatorFinder.getDominators(unit);
			
			MethodSource source = method.getSource();
			
			System.currentTimeMillis();
		}
	}

}
