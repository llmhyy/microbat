package experiment.utils.report.rules;

import java.util.ArrayList;
import java.util.List;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;

public class CombinationComparisonRules implements IComparisonRule {
	
	private List<IComparisonRule> rules;
	
	public CombinationComparisonRules(List<IComparisonRule> rules) {
		this.rules = rules;
	}

	@Override
	public RecordDiff getRecordDiff(Record oldRecord, Record newRecord) {
		for (IComparisonRule rule : rules) {
			RecordDiff recordDiff = rule.getRecordDiff(oldRecord, newRecord);
			if (recordDiff != null) {
				return recordDiff;
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return "combination";
	}

	@Override
	public List<String> getComparisonColumns() {
		List<String> allColumns = new ArrayList<String>();
		for (IComparisonRule rule : rules) {
			allColumns.addAll(rule.getComparisonColumns());
		}
		return allColumns;
	}

}
