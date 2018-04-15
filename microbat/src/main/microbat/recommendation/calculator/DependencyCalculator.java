package microbat.recommendation.calculator;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Method;

import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.codeanalysis.bytecode.MethodFinderBySignature;
import microbat.model.BreakPoint;
import sav.strategies.dto.AppJavaClassPath;

public class DependencyCalculator {
	private AppJavaClassPath appPath;
	
	public DependencyCalculator(AppJavaClassPath appJavaClassPath) {
		this.appPath = appJavaClassPath;
	}

	public Dependency calculateDependency(BreakPoint testPoint, BreakPoint avoidPoint) {
		String methodSign = testPoint.getMethodSign();
		String sign = methodSign.substring(methodSign.indexOf("#")+1, methodSign.length());
		MethodFinderBySignature finder = new MethodFinderBySignature(sign);
		ByteCodeParser.parse(testPoint.getClassCanonicalName(), finder, appPath);
		Method method = finder.getMethod();
		
		CFGConstructor constructor = new CFGConstructor();
		CFG cfg = constructor.buildCFGWithControlDomiance(method.getCode());
		
		List<CFGNode> beforeList = getBeforeList(cfg, testPoint);
		List<CFGNode> afterList = getAfterList(cfg, testPoint, avoidPoint);
		
		int controlDependency = 0;
		for(CFGNode beforeNode: beforeList){
			for(CFGNode afterNode: afterList){
				if(beforeNode.getControlDependentees().contains(afterNode)){
					controlDependency++;
				}
			}
		}
		
		constructor.constructDataDependency(cfg);
		
		int dataDependency = 0;
		for(CFGNode beforeNode: beforeList){
			for(CFGNode afterNode: afterList){
				if(beforeNode.getUseSet().contains(afterNode)){
					dataDependency++;
				}
			}
		}
		
		return new Dependency(dataDependency, controlDependency);
		
	}

	private List<CFGNode> getAfterList(CFG cfg, BreakPoint testPoint, BreakPoint avoidPoint) {
		
		int upperBound = avoidPoint.getLineNumber();
		
		if(!testPoint.getMethodSign().equals(avoidPoint.getMethodSign())) {
			upperBound = cfg.getEndLine();
		}
		
		List<CFGNode> list = new ArrayList<>();
		for(CFGNode node: cfg.getNodeList()){
			int line = cfg.getLineNumber(node);
			if(line>=testPoint.getLineNumber() &&
					line<=upperBound){
				list.add(node);
			}
		}
		
		return list;
	}

	private List<CFGNode> getBeforeList(CFG cfg, BreakPoint testPoint) {
		List<CFGNode> list = new ArrayList<>();
		for(CFGNode node: cfg.getNodeList()){
			if(cfg.getLineNumber(node)<testPoint.getLineNumber()){
				list.add(node);
			}
		}
		
		return list;
	}
}
