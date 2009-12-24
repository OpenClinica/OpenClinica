/*--------------------------------------------------------------------------
*
* File       : oracle_insert_default_data.sql
*
* Subject    : Prepopulate the tables with seeded data
*
* Parameters : None
*
* Conditions : Tables should exists before running this script.
*
* Author/Dt  : Shriram Mani 11/14/2007
*
* Comments   : None
*
--------------------------------------------------------------------------*/

--
-- Data for Name: archived_dataset_file; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_event_context; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_event_values; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_log_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_log_event_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (1, 'Item data value updated');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (2, 'Study subject created');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (3, 'Study subject status changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (4, 'Study subject value changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (5, 'Subject created');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (6, 'Subject status changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (7, 'Subject global value changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (8, 'Event CRF marked complete');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (9, 'Event CRF properties changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (10, 'Event CRF IDE completed (DDE)');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (11, 'Event CRF validated(DDE)');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (12, 'Item data status changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (13, 'Item data deleted');


--
-- Data for Name: completion_status; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO completion_status (completion_status_id, status_id, name, description) VALUES (1, 1, 'completion status', 'place filler for completion status');


--
-- Data for Name: crf; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: crf_version; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dataset; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dataset_crf_version_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dataset_filter_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dataset_study_group_class_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_computed_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_primitive; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_section_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_send_email_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_substitution_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_summary_item_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: decision_condition; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: discrepancy_note; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: discrepancy_note_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (1, 'Failed Validation Check', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (2, 'Incomplete', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (3, 'Unclear/Unreadable', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (4, 'Annotation', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (5, 'Other', '');


--
-- Data for Name: dn_event_crf_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dn_item_data_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dn_study_event_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dn_study_subject_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dn_subject_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: event_crf; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: event_definition_crf; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: export_format; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (1, 'text/plain', 'Default export format for tab-delimited text', 'text/plain');
INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (2, 'text/plain', 'Default export format for comma-delimited text', 'text/plain');
INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (3, 'application/vnd.ms-excel', 'Default export format for Excel files', 'application/vnd.ms-excel');
INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (4, 'text/plain', 'Default export format for CDISC ODM XML files', 'text/plain');
INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (5, 'text/plain', 'Default export format for SAS files', 'text/plain');



--
-- Data for Name: extract_data_table; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: filter; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: filter_crf_version_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: group_class_types; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO group_class_types (group_class_type_id, name, description) VALUES (1, 'Arm', NULL);
INSERT INTO group_class_types (group_class_type_id, name, description) VALUES (2, 'Family/Pedigree', NULL);
INSERT INTO group_class_types (group_class_type_id, name, description) VALUES (3, 'Demographic', NULL);
INSERT INTO group_class_types (group_class_type_id, name, description) VALUES (4, 'Other', NULL);



--
-- Data for Name: item; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_data; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_data_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (1, 'BL', 'Boolean', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (2, 'BN', 'BooleanNonNull', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (3, 'ED', 'Encapsulated Data', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (4, 'TEL', 'A telecommunication address', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (5, 'ST', 'Character String', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (6, 'INT', 'Integer', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (7, 'REAL', 'Floating', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (8, 'SET', NULL, 'a value that contains other distinct values', NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (9, 'DATE', 'date', 'date', NULL);



--
-- Data for Name: item_form_metadata; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_group; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_group_metadata; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_reference_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO item_reference_type (item_reference_type_id, name, description) VALUES (1, 'literal', NULL);


--
-- Data for Name: null_value_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (1, 'NI', 'NoInformation', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (2, 'NA', 'not applicable', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (3, 'UNK', 'unknown', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (4, 'NASK', 'not asked', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (5, 'ASKU', 'asked but unknown', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (6, 'NAV', 'temporarily unavailable', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (7, 'OTH', 'other', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (8, 'PINF', 'positive infinity', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (9, 'NINF', 'negative infinity', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (10, 'MSK', 'masked', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (11, 'NP', 'not present', NULL, NULL);


--
-- Data for Name: openclinica_version; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: privilege; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: resolution_status; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO resolution_status (resolution_status_id, name, description) VALUES (1, 'New/Open', '');
INSERT INTO resolution_status (resolution_status_id, name, description) VALUES (2, 'Updated', '');
INSERT INTO resolution_status (resolution_status_id, name, description) VALUES (3, 'Resolved/Closed', '');


--
-- Data for Name: response_set; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: response_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO response_type (response_type_id, name, description) VALUES (1, 'text', 'free form text entry limited to one line');
INSERT INTO response_type (response_type_id, name, description) VALUES (2, 'textarea', 'free form text area display');
INSERT INTO response_type (response_type_id, name, description) VALUES (3, 'checkbox', 'selecting one from many options');
INSERT INTO response_type (response_type_id, name, description) VALUES (4, 'file', 'for upload of files');
INSERT INTO response_type (response_type_id, name, description) VALUES (5, 'radio', 'selecting one from many options');
INSERT INTO response_type (response_type_id, name, description) VALUES (6, 'single-select', 'pick one from a list');
INSERT INTO response_type (response_type_id, name, description) VALUES (7, 'multi-select', 'pick many from a list');


--
-- Data for Name: role_privilege_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: section; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: status; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO status (status_id, name, description) VALUES (1, 'available', 'this is the active status');
INSERT INTO status (status_id, name, description) VALUES (2, 'unavailable', 'this is the inactive status');
INSERT INTO status (status_id, name, description) VALUES (3, 'private', NULL);
INSERT INTO status (status_id, name, description) VALUES (4, 'pending', NULL);
INSERT INTO status (status_id, name, description) VALUES (5, 'removed', 'this indicates that a record is specifically removed by user');
INSERT INTO status (status_id, name, description) VALUES (6, 'locked', NULL);
INSERT INTO status (status_id, name, description) VALUES (7, 'auto-removed','this indicates that a record is removed due to the removal of its parent record');



--
-- Data for Name: study; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study (study_id, parent_study_id, unique_identifier, secondary_identifier, name, summary, date_planned_start, date_planned_end, date_created, date_updated, owner_id, update_id, type_id, status_id, principal_investigator, facility_name, facility_city, facility_state, facility_zip, facility_country, facility_recruitment_status, facility_contact_name, facility_contact_degree, facility_contact_phone, facility_contact_email, protocol_type, protocol_description, protocol_date_verification, phase, expected_total_enrollment, sponsor, collaborators, medline_identifier, url, url_description, conditions, keywords, eligibility, gender, age_max, age_min, healthy_volunteer_accepted, purpose, allocation, masking, control, "assignment", endpoint, interventions, duration, selection, timing, official_title, results_reference)
VALUES (1, NULL, 'default-study', 'default-study', 'Default Study', '', to_date('2006-10-23', 'YYYY-MM-DD'), to_date('2006-10-23', 'YYYY-MM-DD'), to_date('2006-10-23', 'YYYY-MM-DD'), to_date('2006-10-23', 'YYYY-MM-DD'), 1, NULL, 1, 1, 'default', '', '', '', '', '', '', '', '', '', '', 'observational', '', to_date('2006-10-23', 'YYYY-MM-DD'), 'default', 0, 'default', '', '', '', '', '', '', '', 'both', '', '', 0, 'Natural History', '', '', '', '', '', '', 'longitudinal', 'Convenience Sample', 'Retrospective', '', 0);


--
-- Data for Name: study_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: study_event_definition; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: study_group; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: study_group_class; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: study_parameter; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (1, 'collectDob', 'collect subject''s date of birth', 'In study creation, Subject Birthdate can be set to require collect full birthdate, year of birth, or not used', 'required', 1, 0);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (2, 'discrepancyManagement', '', '', 'true', 1, 0);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (3, 'subjectPersonIdRequired', '', 'In study creation, Person ID can be set to required, optional, or not used', 'required', 1, 0);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (4, 'genderRequired', '', 'In study creation, Subject Gender can be set to required or not used', 'required', 1, 0);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (5, 'subjectIdGeneration', '', 'In study creation, Study Subject ID can be set to Manual Entry, Auto-generate (editable), Auto-generate (non-editable)', 'manual', 1, 0);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (6, 'subjectIdPrefixSuffix', '', 'In study and/or site creation, if Study Subject ID is set to Auto-generate, user can optionally specify a prefix and suffix for the format of the ID, using the format [PRETEXT][AUTO#][POSTTEXT]', 'false', 1, 0);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (7, 'interviewerNameRequired', '', 'In study or site creation, CRF Interviewer Name can be set as optional or required fields', 'required', 1, 1);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (8, 'interviewerNameDefault', '', 'In study or site creation, CRF Interviewer Name can be set to default to blank or to be pre-populated with user''s name and the date of the study event', 'blank', 1, 1);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (9, 'interviewerNameEditable', '', 'In study creation, CRF Interviewer Name can be set to editable or not editable', 'editable', 1, 0);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (10, 'interviewDateRequired', '', 'In study or site creation, CRF Interviewer Date can be set as optional or required fields', 'required', 1, 1);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (11, 'interviewDateDefault', '', 'In study or site creation, CRF Interviewer Date can be set to default to blank or to be pre-populated with user''s name and the date of the study event', 'eventDate', 1, 1);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (12, 'interviewDateEditable', '', 'In study creation, CRF Interview Name and Date can be set to editable or not editable', 'editable', 1, 0);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (13, 'personIdShownOnCRF', '', '', 'false', 1, 0);


--
-- Data for Name: study_parameter_value; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (1, 1, '1', 'collectDob');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (2, 1, 'true', 'discrepancyManagement');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (3, 1, 'true', 'genderRequired');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (4, 1, 'required', 'subjectPersonIdRequired');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (5, 1, 'true', 'interviewerNameRequired');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (6, 1, 'blank', 'interviewerNameDefault');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (7, 1, 'true', 'interviewerNameEditable');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (8, 1, 'true', 'interviewDateRequired');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (9, 1, 'blank', 'interviewDateDefault');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (10, 1, 'true', 'interviewDateEditable');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (11, 1, 'manual', 'subjectIdGeneration');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (12, 1, '', 'subjectIdPrefixSuffix');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (13, 1, 'true', 'personIdShownOnCRF');


--
-- Data for Name: study_subject; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: study_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study_type (study_type_id, name, description) VALUES (1, 'genetic', NULL);
INSERT INTO study_type (study_type_id, name, description) VALUES (2, 'non-genetic', NULL);


--
-- Data for Name: study_user_role; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study_user_role (role_name, study_id, status_id, owner_id, date_created, date_updated, update_id, user_name) VALUES ('admin', 1, 1, 1, to_date('2006-10-23', 'YYYY-MM-DD'), NULL, NULL, 'root');
INSERT INTO study_user_role (role_name, study_id, status_id, owner_id, date_created, date_updated, update_id, user_name) VALUES ('director', 1, 1, 1, to_date('2006-10-23', 'YYYY-MM-DD'), NULL, NULL, 'root');


--
-- Data for Name: subject; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: subject_event_status; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (1, 'scheduled', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (2, 'not scheduled', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (3, 'data entry started', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (4, 'completed', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (5, 'stopped', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (6, 'skipped', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (7, 'locked', '');


--
-- Data for Name: subject_group_map; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO user_account (user_id, user_name, passwd, first_name, last_name, email, active_study, institutional_affiliation, status_id, owner_id, date_created, date_updated, date_lastvisit, passwd_timestamp, passwd_challenge_question, passwd_challenge_answer, phone, user_type_id, update_id) VALUES (1, 'root', '25d55ad283aa400af464c76d713c07ad', 'Root', 'User', 'openclinica_admin@example.com', 1, 'Akaza Research', 1, 1, NULL, to_date('2006-10-23', 'YYYY-MM-DD'), timestamp'2006-10-23 16:46:44.942', to_date('2006-10-23', 'YYYY-MM-DD'), NULL, NULL, '617 621 8585', 3, 1);


--
-- Data for Name: user_account; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: user_role; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (1, 'admin', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (2, 'coordinator', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (3, 'director', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (4, 'investigator', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (5, 'ra', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (6, 'guest', 1, NULL);


--
-- Data for Name: user_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO user_type (user_type_id, user_type) VALUES (1, 'admin');
INSERT INTO user_type (user_type_id, user_type) VALUES (2, 'user');
INSERT INTO user_type (user_type_id, user_type) VALUES (3, 'tech-admin');


--
-- Data for Name: versioning_map; Type: TABLE DATA; Schema: public; Owner: clinica
--

commit;

