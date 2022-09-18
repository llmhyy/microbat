package microbat.mutation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import jmutation.MutationFramework;
import jmutation.model.MicrobatConfig;
import jmutation.model.MutationResult;
import jmutation.model.TestCase;
import jmutation.model.project.Project;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

/**
 * Mutation Agent make use the java mutation framework to
 * mutate the originally correct trace to become a buggy
 * trace that fail the target test case. 
 * @author David
 *
 */
public class MutationAgent {

	protected final int maxMutationLimit = 10;
	protected final int maxMutation = 1;
	
	protected int mutationCount = 0;
	
	protected final String srcFolderPath = "src\\main\\java";
	protected final String testFolderPath = "src\\test\\java";
//	protected final String dropInDir = ResourcesPlugin.getWorkspace().getRoot().getFullPath().toString();
	
	protected final String projectPath;
	protected final String java_path;
	protected final int stepLimit;
	
	protected Trace buggyTrace = null;
	protected String mutatedProjPath = null;
	protected String originalProjPath = null;
	protected TestCase testCase = null;
	
	protected List<TraceNode> rootCauses = new ArrayList<>();
	
	protected int testCaseID = -1;
	protected String testCaseClass = null;
	protected String testCaseMethodName = null;
	protected int seed = 1;
	
	protected MutationResult result = null;
	
	public MutationAgent(String projectPath, String java_path, int stepLimit) {
		this.projectPath = projectPath;
		this.java_path = java_path;
		this.stepLimit = stepLimit;
	}
	
	public void startMutation() {
		
		if (!isReady()) {
			throw new RuntimeException("Mutation Agent is not ready");
		}
		
		System.out.println("Mutating Test Case " + this.testCaseID);
		this.reset();
		
		// Set up the mutation framework
		MutationFramework mutationFramework = new MutationFramework();
		
//		mutationFramework.setDropInsDir(this.dropInDir);
		mutationFramework.setMaxNumberOfMutations(this.maxMutation);
		mutationFramework.toggleStrongMutations(true);
		
		MicrobatConfig microbatConfig = MicrobatConfig.defaultConfig();
		microbatConfig = microbatConfig.setJavaHome(this.java_path);
		microbatConfig = microbatConfig.setStepLimit(this.stepLimit);
		mutationFramework.setMicrobatConfig(microbatConfig);
		mutationFramework.setProjectPath(this.projectPath);
		if (this.testCaseID>=0) {
			this.testCase = mutationFramework.getTestCases().get(this.testCaseID);
		} else {
			final String qualifiedName = String.format("%s#%s", this.testCaseClass, this.testCaseMethodName);
			for (TestCase testCase : mutationFramework.getTestCases()) {
				if (testCase.qualifiedName().equals(qualifiedName)) {
					this.testCase = testCase;
					break;
				}
			}
		}
		mutationFramework.setTestCase(testCase);
		
		// Mutate project until it fail the test case
		boolean testCaseFailed = false;
		for (int i=0; i<100; i++) {
			this.mutationCount++;
			mutationFramework.setSeed(i);
			this.result = mutationFramework.startMutationFramework();
			if (!this.result.isTestCasePassed()) {
				testCaseFailed = true;
				break;
			}
		}
		
		if (!testCaseFailed) {
			throw new RuntimeException(this.genErrorMsg("Cannot fail the test case"));
		}
		
		// Get the mutation information
		Project mutatedProject = this.result.getMutatedProject();
		Project originalProject = this.result.getOriginalProject();
		
		this.mutatedProjPath = mutatedProject.getRoot().getAbsolutePath();
		this.originalProjPath = originalProject.getRoot().getAbsolutePath();
		
		this.buggyTrace = this.result.getMutatedTrace();
		this.buggyTrace.setSourceVersion(true);

		this.rootCauses = this.result.getRootCauses();
	}
	
	public void setSeed(int seed) {
		this.seed = seed;
	}
	
	public Trace getBuggyTrace() {
		return this.buggyTrace;
	}
	
	public List<TraceNode> getRootCause() {
		return this.rootCauses;
	}
	
	
	public String getMutatedProjPath() {
		return this.mutatedProjPath;
	}
	
	public String getOriginalProjPath() {
		return this.originalProjPath;
	}
	
	public TestCase getTestCase() {
		return this.testCase;
	}
	
	public boolean isReady() {
		return this.testCaseID>=0 || (this.testCaseClass != null && this.testCaseMethodName != null);
	}
	
	public void reset() {
		this.buggyTrace = null;
		this.mutatedProjPath = "";
		this.originalProjPath = "";
		this.rootCauses.clear();
		this.mutationCount = 0;
	}
	
	public void setTestCaseID(int testCaseID) {
		this.testCaseID = testCaseID;
	}
	
	public void setTestCaseInfo(final String testCaseClass, final String testCaseMethodName) {
		this.testCaseClass = testCaseClass;
		this.testCaseMethodName = testCaseMethodName;
	}
	
	public String genErrorMsg(final String msg) {
		return "MutationAgent: " + msg;
	}
	
	public int getMutationCount() {
		return this.mutationCount;
	}
}

