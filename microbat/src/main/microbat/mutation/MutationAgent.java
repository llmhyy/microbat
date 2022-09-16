package microbat.mutation;

import java.util.ArrayList;
import java.util.List;

import jmutation.MutationFramework;
import jmutation.model.MutationResult;
import jmutation.model.TestCase;
import jmutation.model.project.Project;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class MutationAgent {

	protected final int maxMutationLimit = 10;
	protected final int maxMutation = 1;
	
	protected int mutationCount = 0;
	
	protected final String srcFolderPath = "src\\main\\java";
	protected final String testFolderPath = "src\\test\\java";
	protected final String projectPath;
	protected final String dropInDir;
	protected final String microbatConfigPath;
	
	protected Trace buggyTrace = null;
	protected String mutatedProjPath = null;
	protected String originalProjPath = null;
	protected TestCase testCase = null;
	
	protected List<TraceNode> rootCauses = new ArrayList<>();
	protected List<VarValue> inputs = new ArrayList<>();
	protected List<VarValue> outputs = new ArrayList<>();
	
	protected int testCaseID = -1;
	protected int seed = 1;
	
	public MutationAgent(String projectPath, String dropInDir, String microbatConfigPath) {
		this.projectPath = projectPath;
		this.dropInDir = dropInDir;
		this.microbatConfigPath = microbatConfigPath;
	}
	
	public void startMutation() {
		
		if (!isReady()) {
			throw new RuntimeException("Mutation Agent is not ready");
		}
		
		System.out.println("Mutating Test Case " + this.testCaseID);
		this.reset();
		
		MutationFramework mutationFramework = new MutationFramework();
		mutationFramework.setProjectPath(projectPath);
		mutationFramework.setDropInsDir(dropInDir);
		mutationFramework.setMicrobatConfigPath(microbatConfigPath);
		mutationFramework.setMaxNumberOfMutations(maxMutation);
		mutationFramework.toggleStrongMutations(true);
		
		this.testCase = mutationFramework.getTestCases().get(this.testCaseID);
		mutationFramework.setTestCase(testCase);
		
		// Mutate project until it fail the test case
		MutationResult result = null;
		boolean testCaseFailed = false;
		for (int i=0; i<100; i++) {
			this.mutationCount++;
			mutationFramework.setSeed(i);
			result = mutationFramework.startMutationFramework();
			if (!result.isTestCasePassed()) {
				testCaseFailed = true;
				break;
			}
		}
		
		if (!testCaseFailed) {
			throw new RuntimeException(this.genErrorMsg("Cannot fail the test case"));
		}

		Project mutatedProject = result.getMutatedProject();
		Project originalProject = result.getOriginalProject();
		
		this.mutatedProjPath = mutatedProject.getRoot().getAbsolutePath();
		this.originalProjPath = originalProject.getRoot().getAbsolutePath();
		
		this.buggyTrace = result.getMutatedTrace();
		this.buggyTrace.setSourceVersion(true);

		this.rootCauses = result.getRootCauses();
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
	
	public List<VarValue> getInputs() {
		return this.inputs;
	}
	
	public List<VarValue> getOutputs() {
		return this.outputs;
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
		return this.testCaseID>=0;
	}
	
	public void reset() {
		this.buggyTrace = null;
		this.mutatedProjPath = "";
		this.originalProjPath = "";
		
		this.rootCauses.clear();
		this.inputs.clear();
		this.outputs.clear();
		
		this.mutationCount = 0;
	}
	
	public void setTestCaseID(int testCaseID) {
		this.testCaseID = testCaseID;
	}
	
	public String genErrorMsg(final String msg) {
		return "MutationAgent: " + msg;
	}
	
	public int getMutationCount() {
		return this.mutationCount;
	}
}

