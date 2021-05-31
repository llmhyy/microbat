package microbat.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {
	Connection initializeConnection() throws SQLException;
}
