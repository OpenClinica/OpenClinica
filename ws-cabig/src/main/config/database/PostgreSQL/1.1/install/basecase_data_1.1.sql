-- insert controlled vocabularies and root user into database

INSERT INTO status (status_id, name, description) VALUES (1,'available', 'this is the active status');
INSERT INTO status (status_id, name, description) VALUES (2,'unavailable', 'this is the inactive status');
INSERT INTO status (status_id, name, description) VALUES (3,'private', NULL);
INSERT INTO status (status_id, name, description) VALUES (4,'pending', NULL);
INSERT INTO status (status_id, name, description) VALUES (5,'deleted', NULL);
INSERT INTO status (status_id, name, description) VALUES (6,'locked', NULL);

INSERT INTO completion_status (status_id, name, description) VALUES (1, 'completion status', 'place filler for completion status');

--refined by jxu, 07/19/06
INSERT INTO group_class_types (name, description) VALUES ('Arm', NULL);
INSERT INTO group_class_types (name, description) VALUES ('Family/Pedigree', NULL);
INSERT INTO group_class_types (name, description) values ('Demographic',NULL);
INSERT INTO group_class_types (name, description) values ('Other',NULL);


INSERT INTO item_data_type (code, name, definition, reference) VALUES ('BL', 'Boolean', NULL, NULL);
INSERT INTO item_data_type (code, name, definition, reference) VALUES ('BN', 'BooleanNonNull', NULL, NULL);
INSERT INTO item_data_type (code, name, definition, reference) VALUES ('ED', 'Encapsulated Data', NULL, NULL);
INSERT INTO item_data_type (code, name, definition, reference) VALUES ('TEL', 'A telecommunication address', NULL, NULL);
INSERT INTO item_data_type (code, name, definition, reference) VALUES ('ST', 'Character String', NULL, NULL);
INSERT INTO item_data_type (code, name, definition, reference) VALUES ('INT', 'Integer', NULL, NULL);
INSERT INTO item_data_type (code, name, definition, reference) VALUES ('REAL', 'Floating', NULL, NULL);
INSERT INTO item_data_type (code, name, definition, reference) VALUES ('SET', NULL, 'a value that contains other distinct values', NULL);
INSERT INTO item_data_type (code, name, definition, reference) VALUES ('DATE', 'date', 'date', NULL);

INSERT INTO item_reference_type (name, description) VALUES ('literal', NULL);

INSERT INTO null_value_type (code, name, definition, reference) VALUES ('NI', 'NoInformation', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('NA', 'not applicable', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('UNK', 'unknown', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('NASK', 'not asked', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('ASKU', 'asked but unknown', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('NAV', 'temporarily unavailable', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('OTH', 'other', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('PINF', 'positive infinity', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('NINF', 'negative infinity', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('MSK', 'masked', NULL, NULL);
INSERT INTO null_value_type (code, name, definition, reference) VALUES ('NP', 'not present', NULL, NULL);

INSERT INTO response_type (name, description) VALUES ('text', 'free form text entry limited to one line');
INSERT INTO response_type (name, description) VALUES ('textarea', 'free form text area display');
INSERT INTO response_type (name, description) VALUES ('checkbox', 'selecting one from many options');
INSERT INTO response_type (name, description) VALUES ('file', 'for upload of files');
INSERT INTO response_type (name, description) VALUES ('radio', 'selecting one from many options');
INSERT INTO response_type (name, description) VALUES ('single-select', 'pick one from a list');
INSERT INTO response_type (name, description) VALUES ('multi-select', 'pick many from a list');

INSERT INTO study_type (study_type_id, name, description) VALUES (1, 'genetic', NULL);
INSERT INTO study_type (study_type_id, name, description) VALUES (2, 'non-genetic', NULL);

INSERT INTO user_type (user_type_id,user_type) VALUES (1, 'admin');
INSERT INTO user_type (user_type_id,user_type) VALUES (2, 'user');
INSERT INTO user_type (user_type_id,user_type) VALUES (3, 'tech-admin');

INSERT INTO user_role (role_name, parent_id) VALUES ('admin', 1);
INSERT INTO user_role (role_name, parent_id) VALUES ('coordinator', 1);
INSERT INTO user_role (role_name, parent_id) VALUES ('director', 1);
INSERT INTO user_role (role_name, parent_id) VALUES ('investigator', 1);
INSERT INTO user_role (role_name, parent_id) VALUES ('ra', 1);
INSERT INTO user_role (role_name, parent_id) VALUES ('guest', 1);


INSERT INTO user_account (user_id, user_name, passwd, first_name, last_name, email, active_study, institutional_affiliation, status_id, owner_id, date_created, date_updated, date_lastvisit, passwd_timestamp, passwd_challenge_question, passwd_challenge_answer, phone, user_type_id, update_id) VALUES (1, 'root', '25d55ad283aa400af464c76d713c07ad', 'Root', 'User', 'openclinica_admin@akazaresearch.com', 1, 'Akaza Research', 1, 1, NULL, NOW(), NOW(), NOW(), NULL, NULL, '515 444 2222', 3, 1);
INSERT INTO user_account (user_id, user_name, passwd, first_name, last_name, email, active_study, institutional_affiliation, status_id, owner_id, date_created, date_updated, date_lastvisit, passwd_timestamp, passwd_challenge_question, passwd_challenge_answer, phone, user_type_id, update_id) VALUES (2, 'demo_ra', '6e9bece1914809fb8493146417e722f6', 'demo', 'ra', 'demo_ra@akazaresearch.com', 1, 'Akaza Research', 1, 1, NULL, NOW(), NOW(), NOW(), NULL, NULL, '515 444 2222', 1, 1);
INSERT INTO user_account (user_id, user_name, passwd, first_name, last_name, email, active_study, institutional_affiliation, status_id, owner_id, date_created, date_updated, date_lastvisit, passwd_timestamp, passwd_challenge_question, passwd_challenge_answer, phone, user_type_id, update_id) VALUES (3, 'demo_director', '6e9bece1914809fb8493146417e722f6', 'demo', 'director', 'demo_director@akazaresearch.com', 1, 'Akaza Research', 1, 1, NULL, NOW(), NOW(), NOW(), NULL, NULL, '515 444 2222', 1, 1);

SELECT pg_catalog.setval ('user_account_user_id_seq', 3, true);

INSERT INTO study VALUES (1, NULL, 'default-study', 'default-study', 'Default Study', '', NOW(), NOW(), NOW(), NOW(), 1, NULL, 1, 1, 'default', '', '', '', '', '', '', '', '', '', '', 'observational', '', NOW(), 'default', 0, 'default', '', '', '', '', '', '', '', 'both', '', '', false, 'Natural History', '', '', '', '', '', '', 'longitudinal', 'Convenience Sample', 'Retrospective', '', false, false);

SELECT pg_catalog.setval ('study_study_id_seq', 1, true);

INSERT INTO study_user_role VALUES ('admin', 1, 1, 1, NOW(), NULL, NULL, 'root');
INSERT INTO study_user_role VALUES ('director', 1, 1, 1, NOW(), NULL, NULL, 'root');
INSERT INTO study_user_role VALUES ('ra', 1, 1, 1, NOW(), NULL, NULL, 'demo_ra');
INSERT INTO study_user_role VALUES ('director', 1, 1, 1, NOW(), NULL, NULL, 'demo_director');

UPDATE user_account SET active_study=1 WHERE user_id in (1, 2, 3);

INSERT INTO EXPORT_FORMAT VALUES (1, 'text/plain', 'Default export format for tab-delimited text', 'text/plain');
INSERT INTO EXPORT_FORMAT VALUES (2, 'text/plain', 'Default export format for comma-delimited text', 'text/plain');
INSERT INTO EXPORT_FORMAT VALUES (3, 'application/vnd.ms-excel', 'Default export format for Excel files', 'application/vnd.ms-excel');


update status set name='removed' where status_id=5;

insert into discrepancy_note_type values(1,'Failed Validation Check','');

insert into discrepancy_note_type values(2,'Incomplete','');

insert into discrepancy_note_type values(3,'Unclear/Unreadable','');

insert into discrepancy_note_type values(4,'Annotation','');

insert into discrepancy_note_type values(5,'Other','');

insert into resolution_status values(1,'New/Open','');

insert into resolution_status values(2,'Updated','');

insert into resolution_status values(3,'Resolved/Closed','');


insert into subject_event_status values(1, 'scheduled', '');
insert into subject_event_status values(2, 'not scheduled', '');
insert into subject_event_status values(3, 'data entry started', '');
insert into subject_event_status values(4, 'completed', '');
insert into subject_event_status values(5, 'stopped', '');
insert into subject_event_status values(6, 'skipped', '');
insert into subject_event_status values(7, 'locked', '');



--do the following only if you just added subject_event_status_id column to study_event table, no data in that new column 
update study_event set subject_event_status_id=1;