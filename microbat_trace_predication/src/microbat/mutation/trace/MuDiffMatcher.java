package microbat.mutation.trace;

import java.util.ArrayList;
import java.util.List;

import tregression.separatesnapshots.DiffMatcher;
import tregression.separatesnapshots.diff.FilePairWithDiff;

public class MuDiffMatcher extends DiffMatcher {
	private String buggySourcePath;
	private String fixSourcePath;
	
	public MuDiffMatcher(String sourceFolder, String orgFilePath, String mutationFilePath) {
		super(sourceFolder, null, null, null);
		this.buggySourcePath = mutationFilePath;
		this.fixSourcePath = orgFilePath;
	}
	
	@Override
	public void matchCode() {
		super.matchCode();
		List<FilePairWithDiff> orgFileDiffList = this.fileDiffList;
		List<FilePairWithDiff> wrapFileDiffList = new ArrayList<>();
		for (FilePairWithDiff diff : orgFileDiffList) {
			FilePairWithDiff wrapDiff = new FilePairWithDiff() {
				@Override
				public String getSourceDeclaringCompilationUnit() {
					return getTargetDeclaringCompilationUnit();
				}
			};
			wrapDiff.setSourceToTargetMap(diff.getSourceToTargetMap());
			wrapDiff.setTargetToSourceMap(diff.getTargetToSourceMap());
			wrapDiff.setSourceFile(diff.getSourceFile());
			wrapDiff.setTargetFile(diff.getTargetFile());
			wrapDiff.setSourceFolderName(diff.getSourceFolderName());
			wrapDiff.setChunks(diff.getChunks());
			wrapFileDiffList.add(wrapDiff);
		}
		this.fileDiffList = wrapFileDiffList;
	}
	
	protected List<String> getRawDiffContent(){
		return super.getRawDiffContent(buggySourcePath, fixSourcePath, true);
	}
}
