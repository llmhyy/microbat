package microbat.mutation.trace.handlers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import microbat.evaluation.io.IgnoredTestCaseFiles;
import microbat.mutation.mutation.TraceMutationVisitor;
import microbat.mutation.trace.TestCaseAnalyzer;
import microbat.util.IResourceUtils;
import microbat.util.JavaUtil;
import microbat.util.Settings;
import mutation.mutator.Mutator;
import sav.common.core.Constants;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.ClassLocation;
import sav.strategies.mutanbug.MutationResult;
import tregression.io.ExcelReporter;
import tregression.junit.ParsedTrials;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MutationGenerationHandler extends AbstractHandler {
	public static final String TMP_DIRECTORY;
	private IgnoredTestCaseFiles ignoredTestCaseFiles;
	private ParsedTrials parsedTrials;
	private int trialNumPerTestCase = 3;
	private double[] unclearRates = {0};
	private boolean isLimitTrialNum = false;
	private int optionSearchLimit = 100;

	static {
		File resultFolder = new File(
				IResourceUtils.getResourceAbsolutePath("microbat_trace_predication", "mutation_result"));
		TMP_DIRECTORY = resultFolder.getAbsolutePath();
		FileUtils.deleteAllFiles(TMP_DIRECTORY);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ignoredTestCaseFiles = new IgnoredTestCaseFiles();
		parsedTrials = new ParsedTrials();
		
		Job job = new Job("Do evaluation") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				TestCaseAnalyzer analyzer = new TestCaseAnalyzer();
				try {
					ExcelReporter reporter = new ExcelReporter(Settings.projectName, unclearRates);
					IPackageFragmentRoot testRoot = JavaUtil.findTestPackageRootInProject();
					
					for(IJavaElement element: testRoot.getChildren()){
						if(element instanceof IPackageFragment){
							analyzer.runEvaluation((IPackageFragment)element, reporter, isLimitTrialNum, 
									ignoredTestCaseFiles, parsedTrials, trialNumPerTestCase, unclearRates, 
									optionSearchLimit, monitor);				
						}
					}
				} catch (JavaModelException | IOException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

	private void test() {
		List<ClassLocation> locationList = new ArrayList<>();
		locationList.add(new ClassLocation("org.apache.commons.math.util.FastMath", "max", 3902));
		locationList.add(new ClassLocation("org.apache.commons.math.util.FastMath", "max", 3905));
		locationList.add(new ClassLocation("org.apache.commons.math.util.FastMath", "max", 3914));
		locationList.add(new ClassLocation("org.apache.commons.math.util.FastMath", "max", 3918));
		generateMutationFiles(locationList);
	}
	
	private Map<String, MutationResult> generateMutationFiles(List<ClassLocation> locationList){
		ClassLocation cl = locationList.get(0);
		String cName = cl.getClassCanonicalName();
		ICompilationUnit unit = JavaUtil.findICompilationUnitInProject(cName);
		URI uri = unit.getResource().getLocationURI();
		String sourceFolderPath = uri.toString();
		cName = ClassUtils.getJFilePath(cName);
		
		sourceFolderPath = sourceFolderPath.substring(0, sourceFolderPath.indexOf(cName));
		sourceFolderPath = sourceFolderPath.substring(5, sourceFolderPath.length());
		
		Mutator mutator = new Mutator(sourceFolderPath, TMP_DIRECTORY);
		Map<String, MutationResult> mutations = mutator.mutate(locationList, new TraceMutationVisitor());
		
		return mutations;
	}
	
}
