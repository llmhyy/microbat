USE trace
;
DROP TABLE IF EXISTS DeadEndRecord
;
CREATE TABLE DeadEndRecord
(
	regression_id INTEGER,
	record_type INTEGER,
	occur_order INTEGER,
	correspondence_order INTEGER,
	break_step_order INTEGER,
	variable TEXT
) 
;


