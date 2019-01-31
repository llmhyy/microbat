package experiment.utils.report;

import java.io.IOException;
import java.util.Arrays;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;
import experiment.utils.report.rules.IComparisonRule;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.FileUtils;
import sav.common.core.utils.StringUtils;

public class StatisticReporter implements IReporter {
	private String resultFile;

	public StatisticReporter(String resultFile) {
		this.resultFile = resultFile;
	}

	@Override
	public void writeChanges(ReportChanges reportChanges, String sheetName, String[] mergedHeaders) throws IOException {
		StringBuilder sb = new StringBuilder();
		/* missing cases */
		sb.append("---------------------------------------------------------------------\n")
			.append("Missing Rows: ").append(CollectionUtils.getSize(reportChanges.getMissingRecords())).append("\n")
			.append("---------------------------------------------------------------------\n");
		for (Record record : CollectionUtils.nullToEmpty(reportChanges.getMissingRecords())) {
			sb.append(Arrays.toString(record.getKeyStringArr())).append("\n");
		}
		FileUtils.writeFile(resultFile, sb.toString(), true);
		
		/* added cases */
		sb = new StringBuilder();
		sb.append("---------------------------------------------------------------------\n")
			.append("Added Rows: ").append(CollectionUtils.getSize(reportChanges.getAddedRecords())).append("\n")
			.append("---------------------------------------------------------------------\n");
		for (Record record : CollectionUtils.nullToEmpty(reportChanges.getAddedRecords())) {
			sb.append(Arrays.toString(record.getKeyStringArr())).append("\n");
		}
		FileUtils.writeFile(resultFile, sb.toString(), true);
		
		/* change cases */
		sb = new StringBuilder();
		for (IComparisonRule rule : reportChanges.getAllRules()) {
			sb.append("---------------------------------------------------------------------\n")
				.append("ComparisonRule: ").append(rule.getName()).append(" [").append(StringUtils.join(rule.getComparisonColumns(), ",")).append("]").append("\n")
				.append("Improved: ").append(CollectionUtils.getSize(reportChanges.getImproves().get(rule))).append("\n")
				.append("Declined: ").append(CollectionUtils.getSize(reportChanges.getDeclines().get(rule))).append("\n")
				.append("Changed: ").append(CollectionUtils.getSize(reportChanges.getChanges().get(rule))).append("\n")
			.append("---------------------------------------------------------------------\n");
			sb.append("IMPROVED: \n");
			for (RecordDiff diff : CollectionUtils.nullToEmpty(reportChanges.getImproves().get(rule))) {
				sb.append(Arrays.toString(diff.getNewRecord().getKeyStringArr())).append("\n");
			}
			sb.append("DECLINED: \n");
			for (RecordDiff diff : CollectionUtils.nullToEmpty(reportChanges.getDeclines().get(rule))) {
				sb.append(Arrays.toString(diff.getNewRecord().getKeyStringArr())).append("\n");
			}
			sb.append("CHANGED: \n");
			for (RecordDiff diff : CollectionUtils.nullToEmpty(reportChanges.getChanges().get(rule))) {
				sb.append(Arrays.toString(diff.getNewRecord().getKeyStringArr())).append("\n");
			}
		}
		FileUtils.writeFile(resultFile, sb.toString(), true);
		
	}

	
}
