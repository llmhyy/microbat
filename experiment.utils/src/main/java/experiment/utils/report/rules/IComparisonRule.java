/**
 * 
 */
package experiment.utils.report.rules;

import experiment.utils.report.Records;
import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;

/**
 * @author LLT
 *
 */
public interface IComparisonRule {

	public RecordDiff getRecordDiff(Record oldRecord, Record newRecord);
	
	public String getName();

}
