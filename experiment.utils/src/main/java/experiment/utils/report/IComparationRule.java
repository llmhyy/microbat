/**
 * 
 */
package experiment.utils.report;

import experiment.utils.report.Records.Record;
import experiment.utils.report.excel.RecordDiff;

/**
 * @author LLT
 *
 */
public interface IComparationRule {

	public RecordDiff getRecordDiff(Record oldRecord, Record newRecord);
	
	public String getName();

}
