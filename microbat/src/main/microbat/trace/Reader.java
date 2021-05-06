/**
 * 
 */
package microbat.trace;

/**
 * @author knightsong
 *
 */
public enum Reader {
	FILE {
		@Override
		public TraceReader create() {
			return new FileTraceReader();
		}
	},
	SQLITE3 {
		@Override
		public TraceReader create() {
			return new SqliteTraceReader();
		}
	},
	MYSQL {
		@Override
		public TraceReader create() {
			return new MysqlTraceReader();
		}
	};

	@Override
	public String toString() {
		String id = name();
		String lower = id.substring(1).toLowerCase();
		return id.charAt(0) + lower;
	}

	public abstract TraceReader create();
}
