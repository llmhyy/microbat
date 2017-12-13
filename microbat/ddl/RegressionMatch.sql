USE trace
;
DROP TABLE IF EXISTS RegressionMatch
;
CREATE TABLE RegressionMatch
(
	regression_id INTEGER,
	buggy_step INTEGER,
	correct_step INTEGER
) 
;


