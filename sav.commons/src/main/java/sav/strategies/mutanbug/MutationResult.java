package sav.strategies.mutanbug;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by hoangtung on 4/9/15.
 */

public class MutationResult {
	private String sourceFolder;
	private String className;
	private Map<Integer, List<File>> mutatedFiles;
	
	public MutationResult(String sourceFolder, String className) {
		this.sourceFolder = sourceFolder;
		this.className = className;
		mutatedFiles = new HashMap<Integer, List<File>>();
	}
	
	public void put(Integer line, List<File> muFiles) {
		mutatedFiles.put(line, muFiles);
	}
	
	public List<File> getMutatedFiles(int lineNo) {
		return mutatedFiles.get(lineNo);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map<Integer, List<File>> getMutatedFiles() {
		return mutatedFiles;
	}

	public void setMutatedFiles(Map<Integer, List<File>> mutatedFiles) {
		this.mutatedFiles = mutatedFiles;
	}
	
	public String getSourceFolder() {
		return sourceFolder;
	}

	@Override
	public String toString() {
		return "MutationResult [className=" + className + ", mutatedFiles="
				+ mutatedFiles + "]";
	}
}
