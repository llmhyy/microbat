USE trace
;
DROP TABLE IF EXISTS ControlScope
;
CREATE TABLE ControlScope
(
	location_id INTEGER,
	trace_id INTEGER,
	class_name VARCHAR(255),
	line_number INTEGER,
	is_loop INTEGER
) 
;


