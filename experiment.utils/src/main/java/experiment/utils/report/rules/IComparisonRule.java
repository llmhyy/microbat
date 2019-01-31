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
public interface IComparisonRule {

	public RecordDiff getRecordDiff(Record oldRecord, Record newRecord);
	
	public String getName();

	public List<String> getComparisonColumns();
}
