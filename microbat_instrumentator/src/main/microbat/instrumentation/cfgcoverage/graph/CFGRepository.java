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
import microbat.instrumentation.cfgcoverage.CoverageAgentUtils;
import microbat.model.ClassLocation;
import sav.strategies.dto.AppJavaClassPath;

public class CFGRepository {
	private Map<String, List<CFGNode>> nodeListMap = new HashMap<String, List<CFGNode>>();
	private Map<String, CFG> cachedCFGs = new HashMap<>();
	
	public CFGInstance createCfgInstance(ClassLocation methodLocation, AppJavaClassPath appJavaClassPath) {
		CFG cfg = findCfg(methodLocation, appJavaClassPath);
		CFG cloneCfg = new CFG();
		cloneCfg.setStartNode(cfg.getStartNode());
		cloneCfg.setMethod(cfg.getMethod());
		for (CFGNode exitNode : cfg.getExitList()) {
			cloneCfg.addExitNode(exitNode);
		}
		List<CFGNode> nodeList = nodeListMap.get(methodLocation.getId());
		for (CFGNode node : nodeList) {
			cloneCfg.addNode(node);
		}
		return new CFGInstance(cloneCfg, methodLocation.getId(), nodeList);
	}

	public CFG findCfg(ClassLocation methodLocation, AppJavaClassPath appJavaClassPath) {
		String methodId = CoverageAgentUtils.getMethodId(methodLocation.getClassCanonicalName(), methodLocation.getMethodSign(), methodLocation.getLineNumber());
		CFG cfg = cachedCFGs.get(methodId);
		if (cfg != null) {
			return cfg;
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
		cfg = cfgConstructor.constructCFG(method.getCode());
		cfg.setMethod(method);
		List<CFGNode> nodeList = new ArrayList<CFGNode>(cfg.getNodeList());
		Collections.sort(nodeList, new Comparator<CFGNode>() {

			@Override
			public int compare(CFGNode o1, CFGNode o2) {
				return Integer.compare(o1.getIdx(), o2.getIdx());
			}
			
		});
		nodeListMap.put(methodId, nodeList);
		
		return cfg;
	}
	
	public void clearCache() {
		cachedCFGs.clear();
		nodeListMap.clear();
		Repository.clearCache();
	}
	
	private static class MethodFinderByMethodSignature extends ByteCodeMethodFinder {
		private ClassLocation loc;

		public MethodFinderByMethodSignature(ClassLocation loc) {
			
		}

		public void visitMethod(Method method) {
			if (loc.getMethodSign().equals(CoverageAgentUtils.getMethodWithSignature(method.getName(), method.getSignature()))) {
				setMethod(method);
			}
		}
	}
}
