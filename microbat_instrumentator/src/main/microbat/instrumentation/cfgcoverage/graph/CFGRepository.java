package microbat.instrumentation.cfgcoverage.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Method;

import microbat.codeanalysis.bytecode.ByteCodeMethodFinder;
import microbat.codeanalysis.bytecode.ByteCodeParser;
import microbat.codeanalysis.bytecode.CFG;
import microbat.codeanalysis.bytecode.CFGConstructor;
import microbat.codeanalysis.bytecode.CFGNode;
import microbat.codeanalysis.bytecode.MethodFinderByLine;
import microbat.instrumentation.cfgcoverage.InstrumentationUtils;
import microbat.model.ClassLocation;
import sav.strategies.dto.AppJavaClassPath;

public class CFGRepository {
	private Map<String, CFGInstance> cachedCFGs = new HashMap<>();
	
	public CFGInstance createCfgInstance(ClassLocation methodLocation, AppJavaClassPath appJavaClassPath) {
		CFGInstance cfgInstance = findCfg(methodLocation, appJavaClassPath);
		CFG cfg = cfgInstance.getCfg();
		CFG cloneCfg = new CFG();
		cloneCfg.setStartNode(cfg.getStartNode());
		cloneCfg.setMethod(cfg.getMethod());
		for (CFGNode exitNode : cfg.getExitList()) {
			cloneCfg.addExitNode(exitNode);
		}
		for (CFGNode node : cfg.getNodeList()) {
			cloneCfg.addNode(node);
		}
		return new CFGInstance(cloneCfg, cfgInstance.getNodeList(), cfgInstance.getUnitCfgNodeIds());
	}

	public CFGInstance findCfg(ClassLocation methodLocation, AppJavaClassPath appJavaClassPath) {
		String methodId = InstrumentationUtils.getMethodId(methodLocation.getClassCanonicalName(), methodLocation.getMethodSign());
		CFGInstance cfgInstance = cachedCFGs.get(methodId);
		if (cfgInstance != null) {
			return cfgInstance;
		}
		ByteCodeMethodFinder finder;
		if (methodLocation.getLineNumber() >= 0) {
			finder = new MethodFinderByLine(methodLocation);
		} else {
			finder = new MethodFinderByMethodSignature(methodLocation);
		}
		ByteCodeParser.parse(methodLocation.getClassCanonicalName(), finder, appJavaClassPath);
		Method method = finder.getMethod();
		CFGConstructor cfgConstructor = new CFGConstructor();
		CFG cfg = cfgConstructor.constructCFG(method.getCode());
		cfg.setMethod(method);
		List<CFGNode> nodeList = new ArrayList<CFGNode>(cfg.getNodeList());
		Collections.sort(nodeList, new Comparator<CFGNode>() {

			@Override
			public int compare(CFGNode o1, CFGNode o2) {
				return Integer.compare(o1.getIdx(), o2.getIdx());
			}
			
		});
		methodId = InstrumentationUtils.getMethodId(methodLocation.getClassCanonicalName(), method);
		cfgInstance = new CFGInstance(cfg, methodId, nodeList);
		cachedCFGs.put(methodId, cfgInstance);		
		return cfgInstance;
	}
	
	public void clearCache() {
		cachedCFGs.clear();
		Repository.clearCache();
	}
	
	private static class MethodFinderByMethodSignature extends ByteCodeMethodFinder {
		private ClassLocation loc;

		public MethodFinderByMethodSignature(ClassLocation loc) {
			this.loc = loc;
		}

		public void visitMethod(Method method) {
			if (loc.getMethodSign().equals(InstrumentationUtils.getMethodWithSignature(method.getName(), method.getSignature()))) {
				setMethod(method);
			}
		}
	}
}
