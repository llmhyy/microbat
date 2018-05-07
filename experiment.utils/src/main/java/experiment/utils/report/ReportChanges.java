/**
 * 
 */
package experiment.utils.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import experiment.utils.report.Records.Record;
import experiment.utils.report.TextComparationRule.ChangeType;
import experiment.utils.report.excel.RecordDiff;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 *
 */
public class ReportChanges {
	private List<Record> missingRecords;
	private List<Record> addedRecords;
	private Map<IComparationRule, List<RecordDiff>> changes;
	private Map<IComparationRule, List<RecordDiff>> improves;
	private Map<IComparationRule, List<RecordDiff>> declines;
	
	public ReportChanges(Records oldRecords, Records newRecords) {
		changes = new HashMap<IComparationRule, List<RecordDiff>>();
		improves = new HashMap<IComparationRule, List<RecordDiff>>();
		declines = new HashMap<IComparationRule, List<RecordDiff>>();
	}

	public void addChange(Record oldRecord, Record newRecord, IComparationRule rule, RecordDiff recordDiff) {
		if (recordDiff == null) {
			return;
		}
		CollectionUtils.getListInitIfEmpty(changes, rule).add(recordDiff);
		if (recordDiff.getChangeType() == ChangeType.DECLINED) {
			CollectionUtils.getListInitIfEmpty(declines, rule).add(recordDiff);
		} else if (recordDiff.getChangeType() == ChangeType.IMPROVED) {
			CollectionUtils.getListInitIfEmpty(improves, rule).add(recordDiff);
		}
	}
	
	public List<Record> getMissingRecords() {
		return missingRecords;
	}

	public void setMissingRecords(List<Record> missingRecords) {
		this.missingRecords = missingRecords;
	}

	public List<Record> getAddedRecords() {
		return addedRecords;
	}

	public void setAddedRecords(List<Record> newRecords) {
		this.addedRecords = newRecords;
	}

	public Map<IComparationRule, List<RecordDiff>> getChanges() {
		return changes;
	}
}
