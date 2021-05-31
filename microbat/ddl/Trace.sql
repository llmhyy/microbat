CREATE TABLE Trace
(
	trace_id TEXT NOT NULL,
	run_id VARCHAR(100) NOT NULL,
	thread_id VARCHAR(255),
	thread_name VARCHAR(255),
	generated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	isMain BOOL,
	PRIMARY KEY (trace_id),
	FOREIGN KEY(run_id) REFERENCES Run(run_id)
);

CREATE UNIQUE INDEX tid ON Trace;

