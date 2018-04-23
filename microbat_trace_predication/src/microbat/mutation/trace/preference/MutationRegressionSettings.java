package microbat.mutation.trace.preference;

import java.util.List;

import microbat.mutation.mutation.MutationType;

public class MutationRegressionSettings {
	private boolean runAllProjectsInWorkspace;
	private String mutationOutputSpace;
	private String targetProject;
	private String bugId;
	private List<MutationType> mutationTypes;
	private boolean rerun;

	public String getTargetProject() {
		return targetProject;
	}

	public void setTargetProject(String targetProject) {
		this.targetProject = targetProject;
	}

	public boolean isRunAllProjectsInWorkspace() {
		return runAllProjectsInWorkspace;
	}

	public void setRunAllProjectsInWorkspace(boolean runAllProjectsInWorkspace) {
		this.runAllProjectsInWorkspace = runAllProjectsInWorkspace;
	}

	public String getBugId() {
		return bugId;
	}

	public void setBugId(String bugId) {
		this.bugId = bugId;
	}

	public List<MutationType> getMutationTypes() {
		return mutationTypes;
	}

	public void setMutationTypes(List<MutationType> mutationTypes) {
		this.mutationTypes = mutationTypes;
	}

	public boolean isRerun() {
		return rerun;
	}

	public void setRerun(boolean rerun) {
		this.rerun = rerun;
	}

	public String getMutationOutputSpace() {
		return mutationOutputSpace;
	}

	public void setMutationOutputSpace(String mutationOutputSpace) {
		this.mutationOutputSpace = mutationOutputSpace;
	}
	
	

}
