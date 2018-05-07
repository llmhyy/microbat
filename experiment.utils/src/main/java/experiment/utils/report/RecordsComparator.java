/**
 * 
 */
package experiment.utils.report;

import java.util.List;
import java.util.Set;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 *
 */
public class RecordsComparator {
	
	public static <T extends IComparationRule> ReportChanges compare(Records oldRecords, Records newRecords,
			List<T> comparationRules) {
		ReportChanges changes = new ReportChanges(oldRecords, newRecords);
		changes.setMissingRecords(getRecordInAButNotInB(oldRecords, newRecords));
		changes.setAddedRecords(getRecordInAButNotInB(newRecords, oldRecords));
		for (String key : newRecords.getKeys()) {
			Record newRecord = newRecords.getRecord(key);
			Record oldRecord = oldRecords.getRecord(key);
			if (oldRecord != null) {
				for (IComparationRule rule : comparationRules) {
					RecordDiff recordDiff = rule.getRecordDiff(oldRecord, newRecord);
					changes.addChange(oldRecord, newRecord, rule, recordDiff);
				}
			}
		}
		return changes;
	}
	
	private static List<Record> getRecordInAButNotInB(Records a, Records b) {
		Set<String> aKeys = a.getKeys();
		Set<String> bKeys = b.getKeys();
		List<String> missingKeys = CollectionUtils.subtract(aKeys, bKeys);
		return a.getRecords(missingKeys);
	}
}
