INSERT INTO study_parameter (study_parameter_id,handle,name,description,default_value,inheritable,overridable)
 VALUES
(13,'personIdShownOnCRF','','','false',true,false);

-------------------------------------
--add study parameters for the default study created in basecase sql
--------------------------------------
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (1, 1, '1','collectDob');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (2, 1, 'true','discrepancyManagement');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (3, 1, 'true','genderRequired');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (4, 1, 'required','subjectPersonIdRequired');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (5, 1, 'true','interviewerNameRequired');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (6, 1, 'blank','interviewerNameDefault');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (7, 1, 'true','interviewerNameEditable');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (8, 1, 'true','interviewDateRequired');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (9, 1, 'blank','interviewDateDefault');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (10, 1, 'true','interviewDateEditable');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (11, 1, 'manual','subjectIdGeneration');

-- not implemented for now, so the value is an empty string
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (12, 1, '','subjectIdPrefixSuffix');

INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
VALUES (13, 1, 'true','personIdShownOnCRF');

