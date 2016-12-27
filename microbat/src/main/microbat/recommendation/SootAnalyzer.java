package microbat.recommendation;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import microbat.util.MicroBatUtil;
import sav.strategies.dto.AppJavaClassPath;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.UnitGraph;

public class SootAnalyzer {

	public void analyze() {
		AppJavaClassPath appClassPath = MicroBatUtil.constructClassPaths();
		
		String classPathString = appClassPath.getClasspathStr();
		String rtJar = appClassPath.getJavaHome() + File.separator + "jre" + File.separator + "lib" + File.separator + "rt.jar";
		classPathString += File.pathSeparator + rtJar;
		
		Scene.v().setSootClassPath(classPathString);
		
		SootClass c = Scene.v().loadClassAndSupport(appClassPath.getLaunchClass());
		c.setApplicationClass();
		
		SootMethod method = c.getMethodByName("qSort");
		Body body = method.retrieveActiveBody();
		
		UnitGraph graph = new ExceptionalUnitGraph(body);
		
		MHGDominatorsFinder<Unit> dominatorFinder = new MHGDominatorsFinder<>(graph);
		
		Iterator<Unit> iterator = graph.iterator();
		while(iterator.hasNext()){
			Unit unit = iterator.next();
			List<Unit> dominators = dominatorFinder.getDominators(unit);
			
			System.currentTimeMillis();
		}
	}

}
