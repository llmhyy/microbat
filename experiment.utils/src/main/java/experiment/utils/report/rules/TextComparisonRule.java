/**
 * 
 */
package experiment.utils.report.rules;

import java.util.List;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;

/**
 * @author LLT
 *
 */
public class TextComparisonRule implements IComparisonRule {
	private List<String> toCompareColumns;
	private int[] cols;
	
	public TextComparisonRule(List<String> toCompareColumns) {
		this.toCompareColumns = toCompareColumns;
	}
	
	public RecordDiff getRecordDiff(Record oldRecord, Record newRecord) {
		RecordDiff diff = null;
		if (cols == null) {
			List<String> headers = toCompareColumns;
			if (headers == null) {
				headers = oldRecord.getHeaders();
			}
			cols = oldRecord.getRecords().toColumnIdx(headers);
		}
		for (int col : cols) {
			String oldValue = oldRecord.getStringValue(col).trim();
			String newValue = newRecord.getStringValue(col).trim();
			if (!oldValue.equals(newValue)) {
				if (diff == null) {
					diff = new RecordDiff(oldRecord, newRecord);
					diff.setChangeType(ChangeType.UNKNOWN);
				}
				diff.addDiffColIdx(col);
			}
		}
		return diff;
	}
	
	public String getName() {
		return "text";
	}

	@Override
	public List<String> getComparisonColumns() {
		return toCompareColumns;
	}
}
