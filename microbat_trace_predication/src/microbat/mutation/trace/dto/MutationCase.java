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

import microbat.mutation.trace.MuRegressionUtils;
import sav.common.core.SavRtException;
import sav.common.core.utils.ResourceUtils;

/**
 * @author LLT
 *
 */
public class MutationCase {
	private AnalysisTestcaseParams testcaseParams;
	private SingleMutation mutation;
	private String correctTraceExec;
	private String bugTraceExec;
	
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

	public void setCorrectTraceExec(String correctTraceExec) {
		this.correctTraceExec = correctTraceExec;
	}

	public void setBugTraceExec(String bugTraceExec) {
		this.bugTraceExec = bugTraceExec;
	}

	public void store() {
		CSVPrinter csvPrinter = null;
		try {
			File csvFile = new File(MuRegressionUtils.getMutationCaseFilePath(testcaseParams.getProjectName()));
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
									mutation.getSourceFolder(),
									getRelativePath(testcaseParams.getAnalysisOutputFolder(), correctTraceExec),
									getRelativePath(testcaseParams.getAnalysisOutputFolder(), bugTraceExec)
									);

			csvPrinter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ResourceUtils.closeQuitely(csvPrinter);
		}
	}

	private String getRelativePath(String parent, String absolutePath) {
		if (!absolutePath.startsWith(parent)) {
			throw new SavRtException(String.format("Invalid path: %s [not exist in %s]", absolutePath, parent));
		}
		return absolutePath.substring(parent.length());
	}
	
	private static String getAbsolutePath(String parent, String relativePath) {
		return new StringBuilder(parent).append(relativePath).toString();
	}

	public static MutationCase load(String targetProject, String muBugId) throws IOException {
		List<CSVRecord> records = getRecords(targetProject);
		for (CSVRecord record : records) {
			if (muBugId.equals(record.get(Column.MUTATION_BUG_ID))) {
				AnalysisTestcaseParams testcaseParams = new AnalysisTestcaseParams(targetProject, 
						record.get(Column.JUNIT_CLASS_NAME), record.get(Column.TEST_METHOD), null);
				SingleMutation mutation = new SingleMutation();
				mutation.setMutatedClass(record.get(Column.MUTATED_CLASS));
				mutation.setLine(getInteger(record, Column.LINE));
				String analysisOutputFolder = testcaseParams.getAnalysisOutputFolder();
				mutation.setMutatedJFile(new File(getAbsolutePath(analysisOutputFolder, record.get(Column.MUTATED_JFILE_RELATIVE_PATH))));
				mutation.setMutationType(record.get(Column.MUTATION_TYPE));
				mutation.setSourceFolder(record.get(Column.SOURCE_FOLDER));
				mutation.setMutationBugId(muBugId);
				MutationCase mutationCase = new MutationCase(testcaseParams, mutation);
				mutationCase.correctTraceExec = getAbsolutePath(analysisOutputFolder, record.get(Column.CORRECT_EXEC_RELATIVE_PATH));
				mutationCase.bugTraceExec = getAbsolutePath(analysisOutputFolder, record.get(Column.BUG_EXEC_RELATIVE_PATH));
				return mutationCase;
			}
		}
		return null;
	}

	private static List<CSVRecord> getRecords(String targetProject) throws IOException {
		CSVFormat format = CSVFormat.EXCEL.withHeader(Column.allColumns());
		String csvFile = MuRegressionUtils.getMutationCaseFilePath(targetProject);
		CSVParser parser = CSVParser.parse(new File(csvFile), Charset.forName("UTF-8"), format);
		List<CSVRecord> records = parser.getRecords();
		if (!records.isEmpty()) {
			records.remove(0); // remove header
		}
		return records;
	}
	
	public static List<String> loadAllMutationBugIds(String targetProject) {
		try {
			List<CSVRecord> records = getRecords(targetProject);
			List<String> bugIds = new ArrayList<>();
			for (CSVRecord record : records) {
				bugIds.add(record.get(Column.MUTATION_BUG_ID));
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
	
	private static enum Column {
		MUTATION_BUG_ID,
		JUNIT_CLASS_NAME,
		TEST_METHOD,
		MUTATION_TYPE,
		MUTATED_CLASS,
		LINE,
		MUTATED_JFILE_RELATIVE_PATH,
		SOURCE_FOLDER,
		CORRECT_EXEC_RELATIVE_PATH,
		BUG_EXEC_RELATIVE_PATH;
		
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
