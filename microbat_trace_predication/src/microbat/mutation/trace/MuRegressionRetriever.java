package microbat.mutation.trace;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import sav.common.core.utils.SingleTimer;
import sav.common.core.utils.StringUtils;
import tregression.empiricalstudy.Regression;
import tregression.io.RegressionRetriever;

public class MuRegressionRetriever extends RegressionRetriever {
	
	public MuRegression retrieveRegression(String projectName, String bugID) throws SQLException {
		Connection conn = null;
		List<AutoCloseable> closables = new ArrayList<>();
		try {
			SingleTimer timer = SingleTimer.start("Retrieve MuRegression");
			conn = getConnection();
			Object[] rs = loadRegression(projectName, bugID, conn, closables);
			int idx = 0;
			int regressionId = (int) rs[idx++];
			int buggyTraceId = (int) rs[idx++];
			int correctTraceId = (int) rs[idx++];
			Regression regression = retrieveRegressionInfo(regressionId, buggyTraceId, correctTraceId, conn, closables);
			rs = loadMutationFile(correctTraceId, buggyTraceId, conn, closables);
			// result
			MuRegression result = new MuRegression();
			result.setMutationFiles((String)rs[0], (String)rs[1], (String)rs[2]);
			result.setRegression(regression);
			System.out.println(timer.getResult());
			return result;
		} finally {
			closeDb(conn, closables);
		}
	}
	
	/**
	 * object[3]
	 * [0]: original code
	 * [1]: mutation code
	 * [2]: mutation class name
	 */
	private Object[] loadMutationFile(int orgTraceId, int muTraceId, Connection conn,
			List<AutoCloseable> closables) throws SQLException {
		String matchList = StringUtils.join(",", orgTraceId, muTraceId);
		PreparedStatement ps = conn
				.prepareStatement(String.format("SELECT * FROM MutationFile WHERE trace_id IN (%s)", matchList));
		ResultSet rs = ps.executeQuery();
		closables.add(ps);
		closables.add(rs);
		Object[] result = new Object[3];
		while (rs.next()) {
			int traceId = rs.getInt("trace_id");
			Blob blob = rs.getBlob("mutation_file");
			String className = rs.getString("mutation_class_name");
			StringWriter writer = new StringWriter();
			try {
				IOUtils.copy(blob.getBinaryStream(), writer, "utf-8");
			} catch (IOException e) {
				throw new SQLException(e);
			}
			if (traceId == orgTraceId) {
				result[0] = writer.getBuffer().toString();
			} else {
				result[1] = writer.getBuffer().toString();	
			}
			result[2] = className;
		}
		ps.close();
		rs.close();
		return result;
	}
	
	public List<String> getMuBugIds(String targetProject) throws SQLException {
		Connection conn = null;
		List<AutoCloseable> closables = new ArrayList<>();
		try {
			conn = getConnection();
			return loadMuBugIds(targetProject, conn, closables);
		} finally {
			closeDb(conn, closables);
		}
	}

	public List<String> loadMuBugIds(String projectName, Connection conn, List<AutoCloseable> closables)
			throws SQLException {
		PreparedStatement ps = conn
				.prepareStatement("SELECT bug_id FROM Regression WHERE project_name=? AND LOWER(bug_id) LIKE 'mu-%'");
		int idx = 1;
		ps.setString(idx++, projectName);
		ResultSet rs = ps.executeQuery();
		closables.add(ps);
		closables.add(rs);
		int total = countNumberOfRows(rs);
		List<String> bugIds = new ArrayList<>(total);
		while (rs.next()) {
			bugIds.add(rs.getString("bug_id"));
		}
		return bugIds;
	}
}
