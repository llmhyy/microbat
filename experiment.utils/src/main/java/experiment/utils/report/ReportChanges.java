/**
 * 
 */
package experiment.utils.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;
import experiment.utils.report.rules.IComparisonRule;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 *
 */
public class ReportChanges {
	private List<Record> missingRecords;
	private List<Record> addedRecords;
	private Map<IComparisonRule, List<RecordDiff>> changes;
	private Map<IComparisonRule, List<RecordDiff>> improves;
	private Map<IComparisonRule, List<RecordDiff>> declines;
	
	public ReportChanges(Records oldRecords, Records newRecords) {
		changes = new HashMap<IComparisonRule, List<RecordDiff>>();
		improves = new HashMap<IComparisonRule, List<RecordDiff>>();
		declines = new HashMap<IComparisonRule, List<RecordDiff>>();
	}

	public void addChange(Record oldRecord, Record newRecord, IComparisonRule rule, RecordDiff recordDiff) {
		if (recordDiff == null) {
			return;
		}
		switch (recordDiff.getChangeType()) {
		case DECLINED:
			CollectionUtils.getListInitIfEmpty(declines, rule).add(recordDiff);
			break;
		case IMPROVED:
			CollectionUtils.getListInitIfEmpty(improves, rule).add(recordDiff);
			break;
		case UNKNOWN:
			CollectionUtils.getListInitIfEmpty(changes, rule).add(recordDiff);
			break;
		default:
			break;
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

	public Map<IComparisonRule, List<RecordDiff>> getChanges() {
		return changes;
	}

	public Map<IComparisonRule, List<RecordDiff>> getImproves() {
		return improves;
	}

	public Map<IComparisonRule, List<RecordDiff>> getDeclines() {
		return declines;
	}
	
	public List<IComparisonRule> getAllRules() {
		Set<IComparisonRule> allRules = new HashSet<>();
		allRules.addAll(changes.keySet());
		allRules.addAll(improves.keySet());
		allRules.addAll(declines.keySet());
		List<IComparisonRule> result = new ArrayList<>(allRules);
		Collections.sort(result, new Comparator<IComparisonRule>() {

			@Override
			public int compare(IComparisonRule o1, IComparisonRule o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return result;
	}
}
