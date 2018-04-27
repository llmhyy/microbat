package microbat.mutation.trace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.bcel.Repository;
import org.eclipse.jdt.core.ICompilationUnit;

import microbat.agent.ExecTraceFileReader;
import microbat.codeanalysis.runtime.InstrumentationExecutor;
import microbat.codeanalysis.runtime.PreCheckInformation;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.mutation.trace.dto.AnalysisTestcaseParams;
import microbat.mutation.trace.dto.BackupClassFiles;
import microbat.mutation.trace.dto.MutationCase;
import microbat.mutation.trace.dto.MutationExecutionResult;
import microbat.mutation.trace.dto.SingleMutation;
import microbat.mutation.trace.dto.TraceExecutionInfo;
import microbat.mutation.trace.report.IMutationExperimentMonitor;
import microbat.util.IResourceUtils;
import microbat.util.JavaUtil;
import microbat.util.Settings;
import sav.common.core.utils.FileUtils;
import sav.strategies.dto.AppJavaClassPath;
import tregression.SimulationFailException;
import tregression.empiricalstudy.DeadEndCSVWriter;
import tregression.empiricalstudy.DeadEndRecord;
import tregression.empiricalstudy.EmpiricalTrial;
import tregression.empiricalstudy.RootCauseFinder;
import tregression.empiricalstudy.Simulator;
import tregression.empiricalstudy.solutionpattern.PatternIdentifier;
import tregression.empiricalstudy.training.DED;
import tregression.empiricalstudy.training.DeadEndData;
import tregression.model.PairList;
import tregression.separatesnapshots.DiffMatcher;
import tregression.tracematch.ControlPathBasedTraceMatcher;

public class MutationEvaluator {
	protected ExecTraceFileReader execTraceReader = new ExecTraceFileReader();
	
	public MutationExecutionResult runSingleMutationTrial(MutationCase mutationCase, IMutationExperimentMonitor monitor) {
		System.out.println("Start Mutation case: " + mutationCase.getMutation().getMutationBugId());
		if (!mutationCase.isValid()) {
			return null;
		}
			
		SingleMutation mutation = mutationCase.getMutation();
		AnalysisTestcaseParams params = mutationCase.getTestcaseParams();
		MutationExecutionResult result = new MutationExecutionResult();

		TraceExecutionInfo correctTrace = restoreTrace(mutationCase.getCorrectTraceExec(),
				mutationCase.getCorrectPrecheckPath(), mutationCase.getTestcaseParams().getProjectName(),
				MuRegressionUtils.createProjectClassPath(params), false);
		result.setCorrectTrace(correctTrace.getTrace());
		
		TraceExecutionInfo mutationTrace = restoreTrace(mutationCase.getBugTraceExec(),
				mutationCase.getBugPrecheckPath(), mutationCase.getTestcaseParams().getProjectName(),
				MuRegressionUtils.createProjectClassPath(params), true);
		MuRegressionUtils.fillMuBkpJavaFilePath(mutationTrace.getTrace(), mutation.getMutationJavaFile(),
				mutation.getMutatedClass());
		result.setBugTrace(mutationTrace.getTrace());

		AppJavaClassPathWrapper.wrapAppClassPath(mutationTrace.getTrace(), correctTrace.getTrace(), params.getBkClassFiles());
		List<EmpiricalTrial> trials = Collections.emptyList();
		try {
			ICompilationUnit iunit = JavaUtil.findNonCacheICompilationUnitInProject(mutation.getMutatedClass(),
					params.getProjectName());
			String orgFilePath = IResourceUtils.getAbsolutePathOsStr(iunit.getPath());
			String mutationFilePath = mutation.getFile().getAbsolutePath();
			
			CheckResult checkResult = checkRootCause(mutation, orgFilePath, mutationFilePath, mutationTrace, correctTrace,
					params, monitor);
			if(checkResult==null){
				return null;
			}
			
			trials = checkResult.trials;
			boolean foundRootCause = checkResult.foundRootCause;
			System.out.println("foundRootCause:" + foundRootCause);
			if(!trials.isEmpty()){
				EmpiricalTrial trial = trials.get(0);
				result.setTrial(trial);	
				
				if (foundRootCause) {
					String backupJFile = orgFilePath.replace(".java", "_bk.java");
					FileUtils.copyFile(orgFilePath, backupJFile, true);
					try {
						FileUtils.copyFile(mutationFilePath, orgFilePath, true);
						Settings.iCompilationUnitMap.remove(mutation.getMutatedClass());
						Settings.compilationUnitMap.remove(mutation.getMutatedClass());
						if(!trial.getDeadEndRecordList().isEmpty()){
							Repository.clearCache();
							DeadEndRecord record = trial.getDeadEndRecordList().get(0);
							String muBugId = mutation.getMutationBugId();
							DED datas = record.getTransformedData(trial.getBuggyTrace());
//							DED datas = new TrainingDataTransfer().transfer(record, trial.getBuggyTrace());
							setTestCase(datas, trial.getTestcase());						
//							new DeadEndReporter().export(datas.getAllData(), params.getProjectName(), muBugId);
							new DeadEndCSVWriter(params.getProjectOutputFolder()).export(datas.getAllData(), params.getProjectName(), muBugId);
						}
					} catch (Throwable e) {
						e.printStackTrace();
					} finally {
						FileUtils.copyFile(backupJFile, orgFilePath, true);
						Settings.iCompilationUnitMap.remove(mutation.getMutatedClass());
						Settings.compilationUnitMap.remove(mutation.getMutatedClass());
						new File(backupJFile).delete();
						recoverOrgClassFile(params);
					}
				} else {
					System.out.println("bug Not found!");
				}
				
				result.setValid(foundRootCause);
			}
			
			
			
			
		} catch (Throwable e) {
			System.err.println("test case has exception when generating trace:");
			e.printStackTrace();
		} finally {
			try {
				recoverOrgClassFile(params);
			} catch (Throwable e1) {
			}
		}
		return result;
	}
	
	private CheckResult checkRootCause(SingleMutation mutation, String orgFilePath, String mutationFilePath, 
			TraceExecutionInfo mutationTraceInfo, TraceExecutionInfo correctTraceInfo, AnalysisTestcaseParams params, 
			 IMutationExperimentMonitor monitor) throws SimulationFailException {
		Trace killingMutatantTrace = mutationTraceInfo.getTrace();
		Trace correctTrace = correctTraceInfo.getTrace();
			
		DiffMatcher diffMatcher = new MuDiffMatcher(mutation.getSourceFolder(), orgFilePath, mutationFilePath);
		diffMatcher.matchCode();
		
		AppJavaClassPathWrapper.wrapAppClassPath(killingMutatantTrace, correctTrace, params.getBkClassFiles());
		
		long start = System.currentTimeMillis();
		ControlPathBasedTraceMatcher traceMatcher = new ControlPathBasedTraceMatcher();
		PairList pairList = traceMatcher.matchTraceNodePair(killingMutatantTrace, correctTrace, diffMatcher); 
		int matchTime = (int) (System.currentTimeMillis() - start);
		
		Simulator simulator = new Simulator(params.getAnalysisParams().isUseSliceBreaker(), false, 
				params.getAnalysisParams().getBreakerLimit());
		simulator.prepare(killingMutatantTrace, correctTrace, pairList, diffMatcher);
		RootCauseFinder rootcauseFinder = new RootCauseFinder();
		
		if(simulator.getObservedFault()==null){
			return null;
		}
		
		rootcauseFinder.checkRootCause(simulator.getObservedFault(), killingMutatantTrace, correctTrace, pairList, diffMatcher);
		TraceNode rootCause = rootcauseFinder.retrieveRootCause(pairList, diffMatcher, killingMutatantTrace, correctTrace);
			
		boolean foundRootCause = rootCause != null;

		boolean[] enableRandoms = new boolean[]{false, true};
		int[] breakLimits = new int[]{1, 3, 5};
		
		simulator.setUseSliceBreaker(true);
		simulator.setAllowCacheCriticalConditionalStep(true);
		List<EmpiricalTrial> returnTrials = new ArrayList<>();
		
		if(foundRootCause){
			for(int i=0; i<enableRandoms.length; i++){
				for(int j=0; j<breakLimits.length; j++){
					simulator.setEnableRandom(enableRandoms[i]);
					simulator.setBreakerTrialLimit(breakLimits[j]);
					
					String fileName = "random-" + enableRandoms[i] + "-limit-" + breakLimits[j];
					
					List<EmpiricalTrial> trials = simulator.detectMutatedBug(killingMutatantTrace, correctTrace, diffMatcher, 0);
					
					if(i==0 && j==0){
						returnTrials = trials;
					}
					
					for (EmpiricalTrial t : trials) {
						t.setTraceMatchTime(matchTime);
						t.setBuggyTrace(killingMutatantTrace);
						t.setFixedTrace(correctTrace);
						t.setPairList(pairList);
						t.setDiffMatcher(diffMatcher);
						
						PatternIdentifier identifier = new PatternIdentifier();
						identifier.identifyPattern(t);
					}
					try {
						monitor.reportEmpiralTrial(fileName, trials, params, mutation);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
			
		return new CheckResult(returnTrials, foundRootCause);
	}
	
	private void setTestCase(DED datas, String tc) {
		if(datas.getTrueData()!=null){
			datas.getTrueData().testcase = tc;					
		}
		for(DeadEndData data: datas.getFalseDatas()){
			data.testcase = tc;
		}
	}
	
	private void recoverOrgClassFile(AnalysisTestcaseParams params) {
		BackupClassFiles bkClassFiles = params.getBkClassFiles();
		if (bkClassFiles != null) {
			FileUtils.copyFile(bkClassFiles.getOrgClassFilePath(), bkClassFiles.getClassFilePath(), true);
		}
	}

	protected TraceExecutionInfo restoreTrace(String execPath, String precheckPath, String projectName,
			AppJavaClassPath appJavaClassPath, boolean isMutationTrace) {
		Trace trace = execTraceReader.read(execPath);
		PreCheckInformation precheckInfo = execTraceReader.readPrecheck(precheckPath);
		trace.setAppJavaClassPath(appJavaClassPath);
		InstrumentationExecutor.appendMissingInfo(trace, appJavaClassPath);
		trace.setSourceVersion(isMutationTrace);
		return new TraceExecutionInfo(precheckInfo, trace, execPath, precheckPath);
	}

	public class CheckResult{
		List<EmpiricalTrial> trials;
		boolean foundRootCause;
		public CheckResult(List<EmpiricalTrial> trials, boolean foundRootCause) {
			super();
			this.trials = trials;
			this.foundRootCause = foundRootCause;
		}
	}
}
