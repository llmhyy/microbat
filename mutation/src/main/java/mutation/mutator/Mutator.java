package mutation.mutator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import japa.parser.ast.CompilationUnit;
import mutation.Activator;
import mutation.io.DebugLineFileWriter;
import mutation.io.MutationFileWriter;
import mutation.mutator.MutationVisitor.MutationNode;
import mutation.mutator.insertdebugline.DebugLineInsertion;
import mutation.mutator.mapping.MuMapParser;
import mutation.mutator.mapping.MutationMap;
import mutation.parser.ClassAnalyzer;
import mutation.parser.ClassDescriptor;
import mutation.parser.JParser;
import mutation.utils.IResourceUtils;
import sav.common.core.SavRtException;
import sav.common.core.utils.BreakpointUtils;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.Randomness;
import sav.strategies.dto.ClassLocation;
import sav.strategies.mutanbug.DebugLineInsertionResult;
import sav.strategies.mutanbug.IMutator;
import sav.strategies.mutanbug.MutationResult;

/**
 * Created by hoangtung on 4/9/15.
 */
public class Mutator implements IMutator {
	public static final String MUTATION_BASE_DIR = "mutation.basedir";
	//TODO LLT: correct the configuration file path, temporary fix for running in eclipse
	private static final String OPERATOR_MAP_FILE = "\\src\\main\\resources\\MuMap.txt";
	private static final int MU_TOTAL_NO_LIMIT = -1;
	private Map<String, List<String>> opMapConfig;
	private String srcFolder;
	private String mutationOutputFolder;
	private int muTotal = MU_TOTAL_NO_LIMIT;
	
	public Mutator(String srcFolder, String mutationOutputFolder, int muTotal) {
		this.srcFolder = srcFolder;
		this.mutationOutputFolder = mutationOutputFolder;
		this.muTotal = muTotal;
	}
	
	public Mutator(String srcFolder, String tmpMutationFolder) {
		this(srcFolder, tmpMutationFolder, MU_TOTAL_NO_LIMIT);
	}
	
	public Mutator(String srcFolder) {
		this(srcFolder, null, MU_TOTAL_NO_LIMIT);
	}
	
	@Override
	public <T extends ClassLocation> Map<String, MutationResult> mutate(List<T> locs) {
		return mutate(locs, new DefaultMutationVisitor());
	}
	
	public <T extends ClassLocation> Map<String, MutationResult> mutate(List<T> locs, MutationVisitor mutationVisitor) {
		Map<String, List<Integer>> classLocationMap = BreakpointUtils.initLineNoMap(locs);
		JParser cuParser = new JParser(srcFolder, classLocationMap.keySet());
		ClassAnalyzer classAnalyzer = new ClassAnalyzer(srcFolder, cuParser);
		Map<String, List<String>> opMapConfig = getOpMapConfig();
		mutationVisitor.init(new MutationMap(opMapConfig), classAnalyzer);
		
		MutationFileWriter fileWriter = new MutationFileWriter(srcFolder, mutationOutputFolder);
		List<MutationObject> muResult = new ArrayList<>();
		for (Entry<String, List<Integer>> entry : classLocationMap.entrySet()) {
			String className = entry.getKey();
			
			CompilationUnit cu = cuParser.parse(className);
			List<ClassDescriptor> descList = classAnalyzer.analyzeCompilationUnit(cu);
			if(!descList.isEmpty()){
				mutationVisitor.reset(descList.get(0), entry.getValue());
				
				cu.accept(mutationVisitor, true);
				Map<Integer, List<MutationNode>> muRes = mutationVisitor.getResult();
				for (Entry<Integer, List<MutationNode>> lineData : muRes.entrySet()) {
					Integer line = lineData.getKey();
					for (MutationNode muNode : lineData.getValue()) {
						muResult.add(new MutationObject(className, line, muNode));
					}
				}
			}
		}
		/* collect data */
		if (muTotal != MU_TOTAL_NO_LIMIT && muTotal < muResult.size()) {
			muResult = Randomness.randomSubList(muResult, muTotal);
		}
		Map<String, MutationResult> result = new HashMap<String, MutationResult>();
		Map<ClassLocation, List<MutationNode>> muMap = groupByClassLocation(muResult);
		for (ClassLocation loc : muMap.keySet()) {
			MutationResult lineRes = result.get(loc.getClassCanonicalName());
			if (lineRes == null) {
				lineRes = new MutationResult(srcFolder, loc.getClassCanonicalName());
				result.put(loc.getClassCanonicalName(), lineRes);
			}
			Integer line = loc.getLineNo();
			Map<File, String> muFiles = fileWriter.write(muMap.get(loc), loc.getClassCanonicalName(), line);
			lineRes.put(line, muFiles);
		}
		return result;
	}
	
	private Map<ClassLocation, List<MutationNode>> groupByClassLocation(List<MutationObject> muObjs) {
		Map<ClassLocation, List<MutationNode>> map = new HashMap<>();
		for (MutationObject muObj : muObjs) {
			CollectionUtils.getListInitIfEmpty(map, muObj.classLocation).add(muObj.muNode);
		}
		return map;
	}
	
	private static class MutationObject {
		ClassLocation classLocation;
		MutationNode muNode;

		public MutationObject(String className, int line, MutationNode muNode) {
			super();
			this.classLocation = new ClassLocation(className, null, line);
			this.muNode = muNode;
		}
	}

	@Override
	public <T extends ClassLocation> Map<String, DebugLineInsertionResult> insertDebugLine(
			Map<String, List<T>> classLocationMap) {
		JParser cuParser = new JParser(srcFolder, classLocationMap.keySet());
		ClassAnalyzer classAnalyzer = new ClassAnalyzer(srcFolder, cuParser);
		DebugLineInsertion insertion = new DebugLineInsertion();
		insertion.setFileWriter(new DebugLineFileWriter(srcFolder));
		Map<String, DebugLineInsertionResult> result = new HashMap<String, DebugLineInsertionResult>();
		for (Entry<String, List<T>> entry : classLocationMap.entrySet()) {
			CompilationUnit cu = cuParser.parse(entry.getKey());
			/*
			 * TODO: change behavior or analyzeCompilationUnit or
			 * DebugLineInsertion?
			 */
			insertion.init(entry.getKey(), classAnalyzer
					.analyzeCompilationUnit(cu).get(0), BreakpointUtils
					.extractLineNo(entry.getValue()));
			DebugLineInsertionResult insertResult = insertion.insert(cu);
			result.put(entry.getKey(), insertResult);
		}
		return result;
	}
	
	public Map<String, List<String>> getOpMapConfig() {
		if (opMapConfig == null) {
			String muMapFile = IResourceUtils.getResourceAbsolutePath(Activator.PLUGIN_ID, "MuMap.txt");
			opMapConfig = MuMapParser.parse(muMapFile);
		}
		return opMapConfig;
	}
	
	public void setOpMapConfig(Map<String, List<String>> opMapConfig) {
		this.opMapConfig = opMapConfig;
	}
}
