CREATE TABLE Step
(
	trace_id INTEGER NOT NULL,
	step_order INTEGER NOT NULL,
	control_dominator INTEGER,
	step_in INTEGER,
	step_over INTEGER,
	invocation_parent INTEGER,
	loop_parent INTEGER,
	location_id INTEGER,
	read_vars TEXT,
	written_vars TEXT,
	time TIMESTAMP,
	PRIMARY KEY (trace_id, step_order)
) 
;


