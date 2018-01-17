package microbat.mutation.trace;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import tregression.empiricalstudy.Regression;
import tregression.io.RegressionRetriever;

public class MuRegressionRetriever extends RegressionRetriever {
	
	public Regression retriveRegression(String projectName, String bugID) throws SQLException {
		Connection conn = null;
		List<AutoCloseable> closables = new ArrayList<>();
		try {
			conn = getConnection();
			Object[] rs = loadRegression(projectName, bugID, conn, closables);
			int idx = 0;
			int regressionId = (int) rs[idx++];
			int buggyTraceId = (int) rs[idx++];
			Regression regression = retrieveRegressionInfo(conn, closables, rs, idx, regressionId, buggyTraceId);
			System.out.println("Retrieve done!");
			return regression;
		} finally {
			closeDb(conn, closables);
		}
	}

}
