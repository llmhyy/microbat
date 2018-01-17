package microbat.mutation.trace;

import java.util.List;

import tregression.separatesnapshots.DiffMatcher;

public class MuDiffMatcher extends DiffMatcher {
	private String buggySourcePath;
	private String fixSourcePath;

	public MuDiffMatcher(String sourceFolder, String orgFilePath, String mutationFilePath) {
		super(sourceFolder, null, null, null);
		this.buggySourcePath = mutationFilePath;
		this.fixSourcePath = orgFilePath;
	}
	
	protected List<String> getRawDiffContent(){
		return super.getRawDiffContent(buggySourcePath, fixSourcePath);
	}
}
