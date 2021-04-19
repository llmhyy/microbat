CREATE TABLE Location ( 
	location_id integer NOT NULL,
	trace_id TEXT NOT NULL,
	class_name varchar(255),
	line_number integer,
	is_conditional integer,
	is_return integer
)
;

