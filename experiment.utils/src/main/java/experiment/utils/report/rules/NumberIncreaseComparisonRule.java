package experiment.utils.report.rules;

import java.util.ArrayList;
import java.util.List;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;

public class NumberIncreaseComparisonRule implements IComparisonRule {
	private List<String> comparedCols = new ArrayList<String>();
	private List<Integer> oldReportCols = new ArrayList<Integer>();
	private List<Integer> newReportCols = new ArrayList<Integer>();
	
	public NumberIncreaseComparisonRule(List<String> comparedCols) {
		this.comparedCols = comparedCols;
	}

	@Override
	public RecordDiff getRecordDiff(Record oldRecord, Record newRecord) {
		setCol(oldRecord, newRecord);
		ChangeType changeType = ChangeType.NONE;
		for (int i = 0; i < comparedCols.size(); i++) {
			double oldValue = Double.valueOf(oldRecord.getStringValue(oldReportCols.get(i)));
			double newValue = Double.valueOf(newRecord.getStringValue(newReportCols.get(i)));
			if (changeType != ChangeType.NONE) {
				break;
			}
			if (newValue > oldValue) {
				changeType = ChangeType.IMPROVED;
			} else if (newValue < oldValue) {
				changeType = ChangeType.DECLINED;
			}
		}
		if (changeType != ChangeType.NONE) {
			RecordDiff diff = new RecordDiff(oldRecord, newRecord);
			diff.setChangeType(changeType);
			diff.setDiffCols(newReportCols);
			return diff;
		}
		return null;
	}
	
	private void setCol(Record oldRecord, Record newRecord) {
		if (!oldReportCols.isEmpty()) {
			return;
		}
		for (String comparedCol : comparedCols) {
			oldReportCols.add(oldRecord.getColIdx(comparedCol));
			newReportCols.add(newRecord.getColIdx(comparedCol));
		}
	}

	@Override
	public String getName() {
		return "numberIncrease";
	}

	@Override
	public List<String> getComparisonColumns() {
		return comparedCols;
	}

}
