package sav.strategies.mutanbug;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sav.common.core.utils.CollectionUtils;


/**
 * Created by hoangtung on 4/9/15.
 */

public class MutationResult {
	private String sourceFolder;
	private String className;
	private Map<Integer, List<File>> mutatedFiles;
	private Map<File, String> mutationTypes;
	
	public MutationResult(String sourceFolder, String className) {
		this.sourceFolder = sourceFolder;
		this.className = className;
		mutatedFiles = new HashMap<Integer, List<File>>();
		mutationTypes = new HashMap<>();
	}
	
	public void put(Integer line, List<File> muFiles) {
		mutatedFiles.put(line, muFiles);
	}
	
	public void put(Integer line, Map<File, String> muFiles) {
		mutationTypes.putAll(muFiles);
		put(line, new ArrayList<>(muFiles.keySet()));
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
	
	public String getMutationType(File file) {
		return mutationTypes.get(file);
	}
	
	public void merge(MutationResult other) {
		if (!this.sourceFolder.equals(other.sourceFolder)
				|| !this.className.equals(other.className)) {
			return;
		}
		for (Integer line : this.mutatedFiles.keySet()) {
			List<File> files = this.mutatedFiles.get(line);
			List<File> otherFiles = other.mutatedFiles.get(line);
			if (CollectionUtils.isNotEmpty(otherFiles)) {
				files.addAll(otherFiles);
			}
		}
		for (Integer line : other.mutatedFiles.keySet()) {
			if (!this.mutatedFiles.containsKey(line)) {
				this.mutatedFiles.put(line, other.mutatedFiles.get(line));
			}
		}
		this.mutationTypes.putAll(other.mutationTypes);
	}

	@Override
	public String toString() {
		return "MutationResult [className=" + className + ", mutatedFiles="
				+ mutatedFiles + "]";
	}

	public static void merge(Map<String, MutationResult> to, Map<String, MutationResult> from) {
		for (String className : to.keySet()) {
			MutationResult otherMuResult = from.get(className);
			if (otherMuResult != null) {
				to.get(className).merge(otherMuResult);
			}
		}
		for (String className : from.keySet()) {
			MutationResult thisMutationResult = to.get(className);
			if (thisMutationResult == null) {
				to.put(className, from.get(className));
			}
		}
	}
	
}
