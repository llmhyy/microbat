/**
 * 
 */
package experiment.utils.report;

import java.util.List;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;

/**
 * @author LLT
 *
 */
public class TextComparationRule implements IComparationRule {
	private List<String> toCompareColumns;
	
	public TextComparationRule(List<String> toCompareColumns) {
		this.toCompareColumns = toCompareColumns;
	}
	
	public RecordDiff getRecordDiff(Record oldRecord, Record newRecord) {
		RecordDiff diff = null;
		List<String> headers = toCompareColumns;
		if (headers == null) {
			headers = oldRecord.getHeaders();
		}
		int[] cols = oldRecord.getRecords().toColumnIdx(headers);
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
	
	
	
	public static enum ChangeType {
		NONE,
		IMPROVED, 
		DECLINED,
		UNKNOWN
	}

	public String getName() {
		return "text-diff";
	}
}
