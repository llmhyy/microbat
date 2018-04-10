package microbat.mutation.trace.dto;

public class BackupClassFiles {
	private String classFilePath;
	private String orgClassFilePath;
	private String mutatedClassFilePath;

	public BackupClassFiles(String classFilePath, String bkOrgClassFilePath, String bkMutatedClassFilePath) {
		this.classFilePath = classFilePath;
		this.orgClassFilePath = bkOrgClassFilePath;
		this.mutatedClassFilePath = bkMutatedClassFilePath;
	}

	public String getOrgClassFilePath() {
		return orgClassFilePath;
	}

	public void setOrgClassFilePath(String orgClassFilePath) {
		this.orgClassFilePath = orgClassFilePath;
	}

	public String getMutatedClassFilePath() {
		return mutatedClassFilePath;
	}

	public void setMutatedClassFilePath(String mutatedClassFilePath) {
		this.mutatedClassFilePath = mutatedClassFilePath;
	}

	public String getClassFilePath() {
		return classFilePath;
	}
}
