USE trace
;
DROP TABLE IF EXISTS Location
;
CREATE TABLE Location
(
	location_id INTEGER NOT NULL AUTO_INCREMENT,
	trace_id INTEGER NOT NULL,
	class_name VARCHAR(255),
	line_number INTEGER,
	is_conditional INTEGER,
	is_return INTEGER,
	PRIMARY KEY (location_id, trace_id)
) 
;


