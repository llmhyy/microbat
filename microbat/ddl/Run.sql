CREATE TABLE Run
(
	run_id VARCHAR(255) NOT NULL,
	project_name VARCHAR(50),
	project_version VARCHAR(50),
	launch_class TEXT,
	launch_method TEXT,
	PRIMARY KEY (run_id)
) 
;


