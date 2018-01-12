package mutation.mutator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import japa.parser.ast.CompilationUnit;
import mutation.io.DebugLineFileWriter;
import mutation.io.MutationFileWriter;
import mutation.mutator.MutationVisitor.MutationNode;
import mutation.mutator.insertdebugline.DebugLineInsertion;
import mutation.mutator.mapping.MuMapParser;
import mutation.mutator.mapping.MutationMap;
import mutation.parser.ClassAnalyzer;
import mutation.parser.ClassDescriptor;
import mutation.parser.JParser;
import sav.common.core.utils.BreakpointUtils;
import sav.strategies.dto.ClassLocation;
import sav.strategies.mutanbug.DebugLineInsertionResult;
import sav.strategies.mutanbug.IMutator;
import sav.strategies.mutanbug.MutationResult;

/**
 * Created by hoangtung on 4/9/15.
 */
public class Mutator implements IMutator {
	//TODO LLT: correct the configuration file path, temporary fix for running in eclipse
	private static final String OPERATOR_MAP_FILE = "\\src\\main\\resources\\MuMap.txt";
	
	private Map<String, List<String>> opMapConfig;
	private String srcFolder;
	private String tmpMutationFolder;
	
	public Mutator(String srcFolder, String tmpMutationFolder) {
		this.srcFolder = srcFolder;
		this.tmpMutationFolder = tmpMutationFolder;
	}
	
	public Mutator(String srcFolder) {
		this.srcFolder = srcFolder;
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
		
		Map<String, MutationResult> result = new HashMap<String, MutationResult>();
		MutationFileWriter fileWriter = new MutationFileWriter(srcFolder, tmpMutationFolder);
		for (Entry<String, List<Integer>> entry : classLocationMap.entrySet()) {
			String className = entry.getKey();
			
			CompilationUnit cu = cuParser.parse(className);
			List<ClassDescriptor> descList = classAnalyzer.analyzeCompilationUnit(cu);
			if(!descList.isEmpty()){
				mutationVisitor.reset(descList.get(0), entry.getValue());
				
				cu.accept(mutationVisitor, true);
				Map<Integer, List<MutationNode>> muRes = mutationVisitor.getResult();
				MutationResult lineRes = new MutationResult(className);
				for (Entry<Integer, List<MutationNode>> lineData : muRes.entrySet()) {
					Integer line = lineData.getKey();
					List<File> muFiles = fileWriter.write(lineData.getValue(), className, line);
					lineRes.put(line, muFiles);
				}
				result.put(className, lineRes);
			}
			
		}
		
		return result;
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
			// load default
			Bundle bundle = Platform.getBundle("mutation");
			if(bundle != null){
				URL url = bundle.getEntry(OPERATOR_MAP_FILE);
				try {
					URL fileURL = org.eclipse.core.runtime.FileLocator.toFileURL(url);
					String file = fileURL.getFile();
					
					opMapConfig = MuMapParser.parse(file);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NullPointerException e){
					e.printStackTrace();
					
					String userDir = System.getProperty("user.dir");
					String mutationDir = userDir + File.separator + "dropins" + File.separator + "MuMap.txt";
					opMapConfig = MuMapParser.parse(mutationDir);
				}
			}
			else{
				opMapConfig = MuMapParser.parse("./src/main/resources/MuMap.txt");
			}
		}
		return opMapConfig;
	}
	
	public void setOpMapConfig(Map<String, List<String>> opMapConfig) {
		this.opMapConfig = opMapConfig;
	}
}
