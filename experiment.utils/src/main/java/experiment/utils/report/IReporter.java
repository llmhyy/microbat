package experiment.utils.report;

import java.io.IOException;

public interface IReporter {

	void writeChanges(ReportChanges reportChanges, String sheetName, String[] mergedHeaders) throws IOException;

}
