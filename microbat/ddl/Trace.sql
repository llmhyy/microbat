USE trace
;
DROP TABLE IF EXISTS Trace
;
CREATE TABLE Trace
(
	trace_id VARCHAR(255) NOT NULL,
	project_name VARCHAR(255),
	project_version VARCHAR(255),
	bug_id VARCHAR(255),
	generated_time TIMESTAMP,
	PRIMARY KEY (trace_id)
) 
;


