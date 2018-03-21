package microbat.mutation.trace;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.model.trace.Trace;
import microbat.mutation.trace.dto.MuTrial;
import tregression.empiricalstudy.EmpiricalTrial;
import tregression.empiricalstudy.DeadEndRecord;
import tregression.io.RegressionRecorder;
import tregression.model.PairList;

public class MuRegressionRecorder extends RegressionRecorder {

	public void record(MuTrial muTrial, Trace buggyTrace, Trace correctTrace, PairList pairList,
			String projectName, String bugId) throws SQLException {
		EmpiricalTrial trial = muTrial.getTrial();
		List<DeadEndRecord> mendingRecords = trial.getDeadEndRecordList();
		Connection conn = null;
		List<AutoCloseable> closables = new ArrayList<AutoCloseable>();
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			String[] tc = trial.getTestcase().split("#");
			int buggyTraceId = insertTrace(buggyTrace, projectName, null, tc[0], tc[1], conn, closables);
			int correctTraceId = insertTrace(correctTrace, projectName, null, tc[0], tc[1], conn, closables);
			int regressionId = insertRegression(projectName, bugId, trial, buggyTraceId, correctTraceId, conn, closables);
			insertMendingRecord(regressionId, mendingRecords, conn, closables);
			insertRegressionMatch(regressionId, pairList, conn, closables);
			Map<Integer, String> mutationFiles = new HashMap<>();
			mutationFiles.put(buggyTraceId, muTrial.getMutationFilePath());
			mutationFiles.put(correctTraceId, muTrial.getOrgFilePath());
			insertMutationFile(muTrial.getMutationClassName(), mutationFiles, conn, closables);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			rollback(conn);
			throw e;
		} finally {
			closeDb(conn, closables);
		}
		System.out.println("RecordDB finished!");
	}

	private void insertMutationFile(String mutationClassName, Map<Integer, String> mutationFiles, Connection conn,
			List<AutoCloseable> closables) throws SQLException {
		String sql = "INSERT INTO MutationFile (trace_id, mutation_file, mutation_class_name)" + " VALUES (?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		closables.add(ps);
		for (Integer traceId : mutationFiles.keySet()) {
			int idx = 1;
			ps.setInt(idx++, traceId);
			try {
				ps.setBlob(idx++, new FileInputStream(mutationFiles.get(traceId)));
			} catch (FileNotFoundException e) {
				throw new SQLException(e);
			}
			ps.setString(idx++, mutationClassName);
			ps.addBatch();
		}
		ps.executeBatch();
	}
}
