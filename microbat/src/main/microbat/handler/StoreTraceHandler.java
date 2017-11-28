package microbat.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import microbat.model.trace.Trace;
import microbat.views.MicroBatViews;

public class StoreTraceHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Trace trace = MicroBatViews.getTraceView().getTrace();
		
		MysqlDataSource dataSource = new MysqlDataSource();
		DBSettings settings = new DBSettings();
		dataSource.setServerName(settings.dbAddress);
		dataSource.setPort(settings.dbPort);
		dataSource.setUser(settings.username);
		dataSource.setPassword(settings.password);
		dataSource.setDatabaseName(settings.dbName);
		
		Connection conn;
		try {
			conn = dataSource.getConnection();
			String sql = "insert into trace (trace_id, project_name, project_version, bug_id, generated_time) "
					+ "values (?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			
			stmt.setString(1, trace.getAppJavaClassPath().getLaunchClass());
			stmt.setString(2, "a");
			stmt.setString(3, "b");
			stmt.setString(4, "0");
			stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			
			stmt.execute();
			
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		System.out.println("test");
		return null;
	}

}
