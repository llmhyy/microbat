/**
 * 
 */
package experiment.utils.report.excel;

import java.util.ArrayList;
import java.util.List;

import experiment.utils.report.Records.Record;
import experiment.utils.report.rules.ChangeType;

/**
 * @author LLT
 *
 */
public class RecordDiff {
	private Record oldRecord;
	private Record newRecord;
	private ChangeType changeType;
	private List<Integer> diffCols; // idx in new report

	public RecordDiff(Record oldRecord, Record newRecord) {
		this.oldRecord = oldRecord;
		this.newRecord = newRecord;
		diffCols = new ArrayList<Integer>();
	}
	
	public void setDiffCols(List<Integer> diffCols) {
		this.diffCols = diffCols;
	}
	
	public void addDiffColIdx(int colIdx) {
		diffCols.add(colIdx);
	}

	public Record getOldRecord() {
		return oldRecord;
	}

	public Record getNewRecord() {
		return newRecord;
	}

	public ChangeType getChangeType() {
		return changeType;
	}

	public List<Integer> getDiffCols() {
		return diffCols;
	}

	public void setChangeType(ChangeType changeType) {
		this.changeType = changeType;
	}
}
