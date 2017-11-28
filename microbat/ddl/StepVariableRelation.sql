USE trace
;
DROP TABLE IF EXISTS StepVariableRelation
;
CREATE TABLE StepVariableRelation
(
	var_id TEXT,
	trace_id VARCHAR(255),
	step_order INTEGER,
	RW INTEGER
) 
;


