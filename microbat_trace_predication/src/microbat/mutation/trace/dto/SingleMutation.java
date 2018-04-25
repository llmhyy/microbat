package microbat.mutation.trace.dto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.StringUtils;
import sav.strategies.mutanbug.MutationResult;

public class SingleMutation {
	private String mutatedClass;
	private int line;
	private File mutatedJFile;
	private String mutationType;
	private String sourceFolder;
	private String mutationBugId;

	public static List<SingleMutation> from(Map<String, MutationResult> mutations, String junitTestClass,
			String testMethod) {
		List<SingleMutation> singleMus = new ArrayList<>();
		String bugIdPrefix = StringUtils.join("#", ClassUtils.getSimpleName(junitTestClass), testMethod);
		for (String tobeMutatedClass : mutations.keySet()) {
			MutationResult result = mutations.get(tobeMutatedClass);
			for (Integer line : result.getMutatedFiles().keySet()) {
				List<File> mutatedFileList = result.getMutatedFiles(line);
				for (File mutationFile : mutatedFileList) {
					SingleMutation muInfo = new SingleMutation();
					muInfo.mutatedClass = tobeMutatedClass;
					muInfo.line = line;
					muInfo.mutatedJFile = mutationFile;
					if (mutationFile.getAbsolutePath().length() >= 260) {
						File muFolder = mutationFile.getParentFile();
						String newName = muFolder.getName().substring(muFolder.getName().lastIndexOf(".") + 1);
						File newMuFolder = new File(FileUtils.getFilePath(muFolder.getParent(), newName));
						muFolder.renameTo(newMuFolder);
						muInfo.mutatedJFile = new File(newMuFolder, mutationFile.getName());
					}
					muInfo.mutationType = result.getMutationType(mutationFile);
					muInfo.sourceFolder = result.getSourceFolder();
					String muId = mutationFile.getParentFile().getName();
					muId = muId.substring(muId.lastIndexOf(".") + 1, muId.length());
					muInfo.mutationBugId = StringUtils.join("#", bugIdPrefix, muId);
					singleMus.add(muInfo);
				}
			}
		}
		return singleMus;
	}
	
	public String getMutationOutputFolder() {
		return mutatedJFile.getParent();
	}

	public String getMutatedClass() {
		return mutatedClass;
	}

	public void setMutatedClass(String mutatedClass) {
		this.mutatedClass = mutatedClass;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public File getFile() {
		return mutatedJFile;
	}
	
	public String getMutationJavaFile() {
		return mutatedJFile.getAbsolutePath();
	}

	public void setMutatedJFile(File mutatedJFile) {
		this.mutatedJFile = mutatedJFile;
	}

	public String getMutationType() {
		return mutationType;
	}

	public void setMutationType(String mutationType) {
		this.mutationType = mutationType;
	}

	public String getSourceFolder() {
		return sourceFolder;
	}

	public void setSourceFolder(String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	public String getMutationBugId() {
		return mutationBugId;
	}
	
	public void setMutationBugId(String mutationBugId) {
		this.mutationBugId = mutationBugId;
	}

	public void remove() {
		FileUtils.deleteFolder(mutatedJFile.getParentFile());
	}

}
