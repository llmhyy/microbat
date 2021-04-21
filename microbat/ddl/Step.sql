CREATE TABLE Step
(
	trace_id TEXT NOT NULL,
	step_order INTEGER NOT NULL,
	control_dominator INTEGER,
	step_in INTEGER,
	step_over INTEGER,
	invocation_parent INTEGER,
	loop_parent INTEGER,
	location_id INTEGER,
	read_vars MEDIUMTEXT,
	written_vars MEDIUMTEXT,
	PRIMARY KEY (trace_id, step_order),
	FOREIGN KEY (trace_id) REFERENCES Trace(trace_id)
) 
;


