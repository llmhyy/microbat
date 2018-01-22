USE trace
;
DROP TABLE IF EXISTS MutationFile
;
CREATE TABLE MutationFile
(
	trace_id INTEGER,
	mutation_file BLOB,
	mutation_class_name VARCHAR(255)
) 
;


