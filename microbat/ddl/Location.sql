USE trace
;
DROP TABLE IF EXISTS Location
;
CREATE TABLE Location
(
	location_id VARCHAR(255) NOT NULL,
	trace_id VARCHAR(255) NOT NULL,
	class_name VARCHAR(255),
	line_number INTEGER,
	is_conditional INTEGER,
	is_return INTEGER,
	PRIMARY KEY (location_id, trace_id)
) 
;


