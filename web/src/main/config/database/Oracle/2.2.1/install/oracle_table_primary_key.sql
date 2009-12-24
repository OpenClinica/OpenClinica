/*--------------------------------------------------------------------------
*
* File       : oracle_table_primary_key.sql
*
* Subject    : Creates primary key constraints for all the tables
*
* Parameters : None
*
* Conditions : Create the table before creating the constraints
*
* Author/Dt  : Shriram  12/11/2007
*
* Comments   : None
*
--------------------------------------------------------------------------*/
--
-- Name: audit_log_event_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE audit_log_event
    ADD CONSTRAINT audit_log_event_pkey PRIMARY KEY (audit_id);


--
-- Name: discrepancy_note_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE discrepancy_note
    ADD CONSTRAINT discrepancy_note_pkey PRIMARY KEY (discrepancy_note_id);


--
-- Name: discrepancy_note_type_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE discrepancy_note_type
    ADD CONSTRAINT discrepancy_note_type_pkey PRIMARY KEY (discrepancy_note_type_id);


--
-- Name: pk_answer; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE item_data
    ADD CONSTRAINT pk_answer PRIMARY KEY (item_data_id);


--
-- Name: pk_archived_dataset_file; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE archived_dataset_file
    ADD CONSTRAINT pk_archived_dataset_file PRIMARY KEY (archived_dataset_file_id);


--
-- Name: pk_audit_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE audit_event
    ADD CONSTRAINT pk_audit_event PRIMARY KEY (audit_id);


--
-- Name: pk_completion_status; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE completion_status
    ADD CONSTRAINT pk_completion_status PRIMARY KEY (completion_status_id);


--
-- Name: pk_dc_computed_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE dc_computed_event
    ADD CONSTRAINT pk_dc_computed_event PRIMARY KEY (dc_summary_event_id);


--
-- Name: pk_dc_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE dc_event
    ADD CONSTRAINT pk_dc_event PRIMARY KEY (dc_event_id);


--
-- Name: pk_dc_primitive; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE dc_primitive
    ADD CONSTRAINT pk_dc_primitive PRIMARY KEY (dc_primitive_id);


--
-- Name: pk_dc_section_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE dc_section_event
    ADD CONSTRAINT pk_dc_section_event PRIMARY KEY (dc_event_id);


--
-- Name: pk_dc_send_email_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE dc_send_email_event
    ADD CONSTRAINT pk_dc_send_email_event PRIMARY KEY (dc_event_id);


--
-- Name: pk_dc_substitution_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE dc_substitution_event
    ADD CONSTRAINT pk_dc_substitution_event PRIMARY KEY (dc_event_id);


--
-- Name: pk_decision_condition; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE decision_condition
    ADD CONSTRAINT pk_decision_condition PRIMARY KEY (decision_condition_id);


--
-- Name: pk_event_crf; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE event_crf
    ADD CONSTRAINT pk_event_crf PRIMARY KEY (event_crf_id);


--
-- Name: pk_export_format; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE export_format
    ADD CONSTRAINT pk_export_format PRIMARY KEY (export_format_id);


--
-- Name: pk_group_role; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE study_group
    ADD CONSTRAINT pk_group_role PRIMARY KEY (study_group_id);


--
-- Name: pk_group_types; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE group_class_types
    ADD CONSTRAINT pk_group_types PRIMARY KEY (group_class_type_id);


--
-- Name: pk_individual; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE subject
    ADD CONSTRAINT pk_individual PRIMARY KEY (subject_id);


--
-- Name: pk_instrument; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE crf
    ADD CONSTRAINT pk_instrument PRIMARY KEY (crf_id);


--
-- Name: pk_item; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE item
    ADD CONSTRAINT pk_item PRIMARY KEY (item_id);


--
-- Name: pk_item_data_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE item_data_type
    ADD CONSTRAINT pk_item_data_type PRIMARY KEY (item_data_type_id);


--
-- Name: pk_item_group; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE item_group
    ADD CONSTRAINT pk_item_group PRIMARY KEY (item_group_id);


--
-- Name: pk_item_group_metadata; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE item_group_metadata
    ADD CONSTRAINT pk_item_group_metadata PRIMARY KEY (item_group_metadata_id);


--
-- Name: pk_item_reference_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE item_reference_type
    ADD CONSTRAINT pk_item_reference_type PRIMARY KEY (item_reference_type_id);


--
-- Name: pk_null_value_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE null_value_type
    ADD CONSTRAINT pk_null_value_type PRIMARY KEY (null_value_type_id);


--
-- Name: pk_person_user; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE user_account
    ADD CONSTRAINT pk_person_user PRIMARY KEY (user_id);


--
-- Name: pk_pl_metadata_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE item_form_metadata
    ADD CONSTRAINT pk_pl_metadata_id PRIMARY KEY (item_form_metadata_id);


--
-- Name: pk_priv_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE privilege
    ADD CONSTRAINT pk_priv_id PRIMARY KEY (priv_id);


--
-- Name: pk_project; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE study
    ADD CONSTRAINT pk_project PRIMARY KEY (study_id);


--
-- Name: pk_project_family; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE study_group_class
    ADD CONSTRAINT pk_project_family PRIMARY KEY (study_group_class_id);


--
-- Name: pk_project_individual; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE study_subject
    ADD CONSTRAINT pk_project_individual PRIMARY KEY (study_subject_id);


--
-- Name: pk_project_instrument; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE event_definition_crf
    ADD CONSTRAINT pk_project_instrument PRIMARY KEY (event_definition_crf_id);


--
-- Name: pk_query_library; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE filter
    ADD CONSTRAINT pk_query_library PRIMARY KEY (filter_id);


--
-- Name: pk_report_library; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE dataset
    ADD CONSTRAINT pk_report_library PRIMARY KEY (dataset_id);


--
-- Name: pk_response_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE response_type
    ADD CONSTRAINT pk_response_type PRIMARY KEY (response_type_id);


--
-- Name: pk_role_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE user_role
    ADD CONSTRAINT pk_role_id PRIMARY KEY (role_id);


--
-- Name: pk_rs_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE response_set
    ADD CONSTRAINT pk_rs_id PRIMARY KEY (response_set_id);


--
-- Name: pk_section_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE section
    ADD CONSTRAINT pk_section_id PRIMARY KEY (section_id);


--
-- Name: pk_status; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE status
    ADD CONSTRAINT pk_status PRIMARY KEY (status_id);


--
-- Name: pk_study_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE study_event
    ADD CONSTRAINT pk_study_event PRIMARY KEY (study_event_id);


--
-- Name: pk_study_event_definition; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE study_event_definition
    ADD CONSTRAINT pk_study_event_definition PRIMARY KEY (study_event_definition_id);


--
-- Name: pk_study_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE study_type
    ADD CONSTRAINT pk_study_type PRIMARY KEY (study_type_id);


--
-- Name: pk_subject_event_status; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE subject_event_status
    ADD CONSTRAINT pk_subject_event_status PRIMARY KEY (subject_event_status_id);


--
-- Name: pk_subject_group_map; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE subject_group_map
    ADD CONSTRAINT pk_subject_group_map PRIMARY KEY (subject_group_map_id);


--
-- Name: pk_user_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE user_type
    ADD CONSTRAINT pk_user_type PRIMARY KEY (user_type_id);


--
-- Name: pk_versioning; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE crf_version
    ADD CONSTRAINT pk_versioning PRIMARY KEY (crf_version_id);


--
-- Name: pkey_audit_log_event_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE audit_log_event_type
    ADD CONSTRAINT pkey_audit_log_event_type PRIMARY KEY (audit_log_event_type_id);


--
-- Name: resolution_status_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE resolution_status
    ADD CONSTRAINT resolution_status_pkey PRIMARY KEY (resolution_status_id);


--
-- Name: study_parameter_handle_key; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE study_parameter
    ADD CONSTRAINT study_parameter_handle_key UNIQUE (handle);


--
-- Name: study_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace:
--

ALTER TABLE study_parameter
    ADD CONSTRAINT study_parameter_pkey PRIMARY KEY (study_parameter_id);


