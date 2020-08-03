package microbat.model.trace;

import java.util.Date;
import java.util.List;

import microbat.filedb.annotation.Attribute;
import microbat.filedb.annotation.Key;
import microbat.filedb.annotation.RecordType;

/**
 * @author LLT
 *
 */
@RecordType
public class ExecutionMetadata {
	@Key
	private String key;
	@Attribute
	private String projectName;
	@Attribute
	private String projectVersion;
	@Attribute
	private String launchClass;
	@Attribute
	private String launchMethod;
	@Attribute
	private boolean isMultiThread;
	@Attribute
	private List<String> includedLibraryClasses;
	@Attribute
	private List<String> excludedLibraryClasses;
	@Attribute
	private Date executionTime;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectVersion() {
		return projectVersion;
	}

	public void setProjectVersion(String projectVersion) {
		this.projectVersion = projectVersion;
	}

	public String getLaunchClass() {
		return launchClass;
	}

	public void setLaunchClass(String launchClass) {
		this.launchClass = launchClass;
	}

	public String getLaunchMethod() {
		return launchMethod;
	}

	public void setLaunchMethod(String launchMethod) {
		this.launchMethod = launchMethod;
	}

	public Date getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Date executionTime) {
		this.executionTime = executionTime;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isMultiThread() {
		return isMultiThread;
	}

	public void setMultiThread(boolean isMultiThread) {
		this.isMultiThread = isMultiThread;
	}

	public List<String> getIncludedLibraryClasses() {
		return includedLibraryClasses;
	}

	public void setIncludedLibraryClasses(List<String> includedLibraryClasses) {
		this.includedLibraryClasses = includedLibraryClasses;
	}

	public List<String> getExcludedLibraryClasses() {
		return excludedLibraryClasses;
	}

	public void setExcludedLibraryClasses(List<String> excludedLibraryClasses) {
		this.excludedLibraryClasses = excludedLibraryClasses;
	}

}
