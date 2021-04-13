CREATE TABLE Trace
(
	trace_id INTEGER NOT NULL AUTO_INCREMENT,
	run_id VARCHAR(255),
	thread_id VARCHAR(255),
	thread_name VARCHAR(255),
	generated_time TIMESTAMP,
	isMain BOOL,
	PRIMARY KEY (trace_id)
) 
;


