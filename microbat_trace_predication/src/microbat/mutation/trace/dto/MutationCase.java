package microbat.mutation.trace.dto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import microbat.mutation.trace.MuRegressionUtils;
import microbat.util.IProjectUtils;
import microbat.util.JavaUtil;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.ResourceUtils;

/**
 * @author LLT
 *
 */
public class MutationCase {
	private static final String ERROR_EMTPY_TRACE = "empty trace";
	private static final String ERROR_TIMEOUT = "time out";
	private static final String ERROR_TOO_LONG = "trace is too long";
	
	private AnalysisTestcaseParams testcaseParams;
	private SingleMutation mutation;
	private String correctTraceExec;
	private String bugTraceExec;
	private String correctPrecheckPath;
	private String bugPrecheckPath;
	private boolean isValid;
	private String error;
	
	public MutationCase(AnalysisTestcaseParams testcaseParams, SingleMutation mutation) {
		this.testcaseParams = testcaseParams;
		this.mutation = mutation;
	}

	public AnalysisTestcaseParams getTestcaseParams() {
		return testcaseParams;
	}

	public SingleMutation getMutation() {
		return mutation;
	}
	
	public String getCorrectTraceExec() {
		return correctTraceExec;
	}

	public String getBugTraceExec() {
		return bugTraceExec;
	}

	public void setCorrectTrace(TraceExecutionInfo correctTrace) {
		this.correctTraceExec = correctTrace.getExecPath();
		this.correctPrecheckPath = correctTrace.getPrecheckInfoPath();
	}

	public boolean isValid() {
		return isValid;
	}

	public String getError() {
		return error;
	}
	
	public String getCorrectPrecheckPath() {
		return correctPrecheckPath;
	}

	public String getBugPrecheckPath() {
		return bugPrecheckPath;
	}

	public void setMutationTrace(MutationTrace trace) {
		this.isValid = true;
		if (trace == null) {
			this.isValid = false;
			this.error = ERROR_EMTPY_TRACE;
			return;
		}
		if (trace.getTrace() != null) {
			this.bugTraceExec = trace.getTraceExecFile();
			this.bugPrecheckPath = trace.getTraceExecInfo().getPrecheckInfoPath();
		}
		if (trace.isTimeOut()) {
			this.isValid = false;
			this.error = ERROR_TIMEOUT;
		} else if (trace.isTooLong()) {
			this.isValid = false;
			this.error = ERROR_TOO_LONG;
		} else if (trace.getTrace() == null || trace.getTrace().size() < 1) {
			this.isValid = false;
			this.error = ERROR_EMTPY_TRACE;
		}
	}

	public void store(String mutationOutputSpace) {
		if (isValid) {
			String validPath = FileUtils.getFilePath(mutationOutputSpace, "mutation", testcaseParams.getProjectName(), "validMutationCases.csv");
			storeCsv(validPath);
		}
		storeCsv(MuRegressionUtils.getMutationCaseFilePath(testcaseParams.getProjectName(), mutationOutputSpace));
	}
	
	public void storeCsv(String csvFilePath) {
		CSVPrinter csvPrinter = null;
		try {
			File csvFile = new File(csvFilePath);
			CSVFormat format = CSVFormat.EXCEL;
			if (!csvFile.exists()) {
				format = format.withHeader(Column.allColumns());
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true));
			csvPrinter = new CSVPrinter(writer, format);
			csvPrinter.printRecord(mutation.getMutationBugId(), 
									testcaseParams.getJunitClassName(), 
									testcaseParams.getTestMethod(), 
									mutation.getMutationType(),
									mutation.getMutatedClass(),
									mutation.getLine(),
									getRelativePath(testcaseParams.getAnalysisOutputFolder(), mutation.getFile().getAbsolutePath()),
									getRelativePath(testcaseParams.getProjectFolder(), mutation.getSourceFolder()),
									getRelativePath(testcaseParams.getAnalysisOutputFolder(), correctTraceExec),
									getRelativePath(testcaseParams.getAnalysisOutputFolder(), correctPrecheckPath),
									getRelativePath(testcaseParams.getAnalysisOutputFolder(), bugTraceExec),
									getRelativePath(testcaseParams.getAnalysisOutputFolder(), bugPrecheckPath),
									isValid,
									error
									);

			csvPrinter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ResourceUtils.closeQuitely(csvPrinter);
		}
	}

	private String getRelativePath(String parent, String absolutePath) {
		if (absolutePath == null || !absolutePath.startsWith(parent)) {
			return absolutePath;
		}
		return absolutePath.substring(parent.length());
	}
	
	private static String getAbsolutePath(String parent, String relativePath) {
		return new StringBuilder(parent).append(relativePath).toString();
	}

	public static MutationCase load(String targetProject, String muBugId, String mutationOutputSpace,
			AnalysisParams analysisParams, String projectFolder) throws IOException {
		List<CSVRecord> records = getRecords(targetProject, mutationOutputSpace);
		for (CSVRecord record : records) {
			if (muBugId.equals(record.get(Column.MUTATION_BUG_ID))) {
				return toMutationCase(targetProject, analysisParams, projectFolder, record);
			}
		}
		return null;
	}
	
	public static List<MutationCase> loadAllMutationCases(String targetProject,
			String mutationOutputSpace, AnalysisParams analysisParams, String projectFolder) throws IOException {
		List<CSVRecord> records = getRecords(targetProject, mutationOutputSpace);
		List<MutationCase> result = new ArrayList<>(records.size());
		for (CSVRecord record : records) {
			result.add(toMutationCase(targetProject, analysisParams, projectFolder, record));
		}
		return result;
	}

	private static MutationCase toMutationCase(String targetProject, AnalysisParams analysisParams,
			String projectFolder, CSVRecord record) {
		AnalysisTestcaseParams testcaseParams = new AnalysisTestcaseParams(targetProject, 
				record.get(Column.JUNIT_CLASS_NAME), record.get(Column.TEST_METHOD), analysisParams, projectFolder);
		SingleMutation mutation = new SingleMutation();
		mutation.setMutatedClass(record.get(Column.MUTATED_CLASS));
		mutation.setLine(getInteger(record, Column.LINE));
		String analysisOutputFolder = testcaseParams.getAnalysisOutputFolder();
		mutation.setMutatedJFile(new File(getAbsolutePath(analysisOutputFolder, record.get(Column.MUTATED_JFILE_RELATIVE_PATH))));
		mutation.setMutationType(record.get(Column.MUTATION_TYPE));
		mutation.setSourceFolder(getAbsolutePath(testcaseParams.getProjectFolder(), record.get(Column.SOURCE_FOLDER)));
		mutation.setMutationBugId(record.get(Column.MUTATION_BUG_ID));
		MutationCase mutationCase = new MutationCase(testcaseParams, mutation);
		mutationCase.correctTraceExec = getAbsolutePath(analysisOutputFolder, record.get(Column.CORRECT_EXEC_RELATIVE_PATH));
		mutationCase.correctPrecheckPath = getAbsolutePath(analysisOutputFolder, record.get(Column.CORRECT_PRECHECK_RELATIVE_PATH));
		mutationCase.bugTraceExec = getAbsolutePath(analysisOutputFolder, record.get(Column.BUG_EXEC_RELATIVE_PATH));
		mutationCase.bugPrecheckPath = getAbsolutePath(analysisOutputFolder, record.get(Column.BUG_PRECHECK_RELATIVE_PATH));
		mutationCase.isValid = Boolean.valueOf(record.get(Column.IS_VALID));
		mutationCase.error = record.get(Column.ERROR);
		
		/* backupClassFile */
		String classFileName = ClassUtils.getSimpleName(mutation.getMutatedClass()) + ".class";
		IJavaProject project = JavaCore.create(JavaUtil.getSpecificJavaProjectInWorkspace(targetProject));
		BackupClassFiles backupClassFiles = new BackupClassFiles(
				ClassUtils.getClassFilePath(IProjectUtils.getTargetFolder(project), mutation.getMutatedClass()),
				FileUtils.getFilePath(testcaseParams.getAnalysisOutputFolder(), classFileName),
				FileUtils.getFilePath(mutation.getMutationOutputFolder(), classFileName));
		testcaseParams.setBkClassFiles(backupClassFiles);
		return mutationCase;
	}

	public static List<CSVRecord> getRecords(String targetProject, String mutationOutputSpace) throws IOException {
		String mutationCasePath = MuRegressionUtils.getValidMutationCaseFilePath(targetProject, mutationOutputSpace);
		if (!new File(mutationCasePath).exists()) {
			mutationCasePath = MuRegressionUtils.getMutationCaseFilePath(targetProject, mutationOutputSpace);
		}
		return getRecords(mutationCasePath);
	}
	
	public static List<CSVRecord> getRecords(String filePath) throws IOException {
		CSVFormat format = CSVFormat.EXCEL.withHeader(Column.allColumns());
		String csvFilePath = filePath;
		File csvFile = new File(csvFilePath);
		if (!csvFile.exists()) {
			return Collections.emptyList();
		}
		CSVParser parser = CSVParser.parse(csvFile, Charset.forName("UTF-8"), format);
		List<CSVRecord> records = parser.getRecords();
		if (!records.isEmpty()) {
			records.remove(0); // remove header
		}
		return records;
	}
	
	public static List<String> loadAllMutationBugIds(String targetProject, String mutationOutputSpace) {
		try {
			List<CSVRecord> records = getRecords(targetProject, mutationOutputSpace);
			List<String> bugIds = new ArrayList<>();
			for (CSVRecord record : records) {
				if (Boolean.valueOf(record.get(Column.IS_VALID))) {
					bugIds.add(record.get(Column.MUTATION_BUG_ID));
				}
			}
			return bugIds;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	private static int getInteger(CSVRecord record, Column col) {
		return Integer.valueOf(record.get(col));
	}
	
	public static enum Column {
		MUTATION_BUG_ID,
		JUNIT_CLASS_NAME,
		TEST_METHOD,
		MUTATION_TYPE,
		MUTATED_CLASS,
		LINE,
		MUTATED_JFILE_RELATIVE_PATH,
		SOURCE_FOLDER,
		CORRECT_EXEC_RELATIVE_PATH,
		CORRECT_PRECHECK_RELATIVE_PATH,
		BUG_EXEC_RELATIVE_PATH,
		BUG_PRECHECK_RELATIVE_PATH,
		IS_VALID,
		ERROR;
		
		public static String[] allColumns() {
			Column[] values = values();
			String[] cols = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				cols[i] = values[i].name();
			}
			return cols;
		}
	}

	
}
