USE trace
;
DROP TABLE IF EXISTS Regression
;
CREATE TABLE Regression
(
	regression_id INTEGER NOT NULL AUTO_INCREMENT,
	buggy_trace INTEGER,
	correct_trace INTEGER,
	root_cause_step INTEGER,
	is_overskip INTEGER,
	over_skip_number INTEGER,
	control_mending_start INTEGER,
	data_mending_start INTEGER,
	mending_corresponding_step INTEGER,
	mending_returning_point INTEGER,
	PRIMARY KEY (regression_id)
) 
;


