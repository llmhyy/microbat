package experiment.utils.report.rules;

import java.util.Arrays;
import java.util.List;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;

public class SimulatorComparisonRule implements IComparisonRule {
	private int foundCauseCol = -1;
	private int generalCauseCol = -1;
	private int exceptionCol = -1;

	@Override
	public RecordDiff getRecordDiff(Record oldRecord, Record newRecord) {
		setCol(oldRecord);
		ChangeType changeType = ChangeType.NONE;
		/* compare found cause */
		double oldFoundCause = Double.valueOf(oldRecord.getStringValue(foundCauseCol));
		double newFoundCause = Double.valueOf(newRecord.getStringValue(foundCauseCol));
		if (newFoundCause == -1) {
			if (oldFoundCause != -1) {
				changeType = ChangeType.DECLINED;
			}
		} else if (oldFoundCause == -1) {
			if (newFoundCause != -1) {
				changeType = ChangeType.IMPROVED;
			}
		}
		/* compare general cause */
		if (changeType == ChangeType.NONE) {
			double oldGeneralCause = Double.valueOf(oldRecord.getStringValue(generalCauseCol));
			double newGeneralCause = Double.valueOf(newRecord.getStringValue(generalCauseCol));
			if (newGeneralCause == -1) {
				if (oldGeneralCause != -1) {
					changeType = ChangeType.DECLINED;
				}
			} else if (oldGeneralCause == -1) {
				if (newGeneralCause != -1) {
					changeType = ChangeType.IMPROVED;
				}
			}
		}
		if (changeType == ChangeType.NONE) {
			String oldException = oldRecord.getStringValue(exceptionCol);
			String newException = newRecord.getStringValue(exceptionCol);
			if (oldException.isEmpty() && !newException.isEmpty()) {
				changeType = ChangeType.DECLINED;
			} else if (!oldException.isEmpty() && newException.isEmpty()) {
				changeType = ChangeType.IMPROVED;
			} else if (!oldException.equals(newException)) {
				changeType = ChangeType.UNKNOWN;
			}
		}
		if (changeType != ChangeType.NONE) {
			RecordDiff diff = new RecordDiff(oldRecord, newRecord);
			diff.setChangeType(changeType);
			diff.setDiffCols(Arrays.asList(foundCauseCol, generalCauseCol, exceptionCol));
			return diff;
		}
		return null;
	}

	private void setCol(Record oldRecord) {
		if (foundCauseCol < 0) {
			foundCauseCol = oldRecord.getColIdx("found cause");
			generalCauseCol = oldRecord.getColIdx("general cause");
			exceptionCol = oldRecord.getColIdx("exception");
		}
	}

	@Override
	public String getName() {
		return "simulator";
	}

	@Override
	public List<String> getComparisonColumns() {
		return null;
	}

}
