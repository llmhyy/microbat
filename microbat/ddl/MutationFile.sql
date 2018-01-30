DROP TABLE IF EXISTS MutationFile
;
CREATE TABLE MutationFile
(
	trace_id INTEGER,
	mutation_file MEDIUMBLOB,
	mutation_class_name VARCHAR(255)
) 
;


