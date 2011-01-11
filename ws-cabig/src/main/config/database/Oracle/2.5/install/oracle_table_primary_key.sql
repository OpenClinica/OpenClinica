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
* Author/Dt  : Shriram  05/14/2008
*
* Comments   : None
*
--------------------------------------------------------------------------*/

--
prompt Create primary key on AUDIT_ID - Table AUDIT_LOG_EVENT
--

alter table AUDIT_LOG_EVENT
    add constraint audit_log_event_pkey PRIMARY KEY (AUDIT_ID);


--
prompt Create primary key on DISCREPANCY_NOTE_ID - Table DISCREPANCY_NOTE
--

alter table DISCREPANCY_NOTE
    add constraint discrepancy_note_pkey PRIMARY KEY (DISCREPANCY_NOTE_ID);


--
prompt Create primary key on DISCREPANCY_NOTE_TYPE_ID - Table DISCREPANCY_NOTE_TYPE
--

alter table DISCREPANCY_NOTE_TYPE
    add constraint discrepancy_note_type_pkey PRIMARY KEY (DISCREPANCY_NOTE_TYPE_ID);


--
prompt Create primary key on ITEM_DATA_ID - Table ITEM_DATA
--

alter table ITEM_DATA
    add constraint pk_answer PRIMARY KEY (ITEM_DATA_ID);


--
prompt Create primary key on ARCHIVED_DATASET_FILE_ID - Table ARCHIVED_DATASET_FILE
--

alter table ARCHIVED_DATASET_FILE
    add constraint pk_archived_dataset_file PRIMARY KEY (ARCHIVED_DATASET_FILE_ID);


--
prompt Create primary key on AUDIT_ID - Table AUDIT_EVENT
--

alter table AUDIT_EVENT
    add constraint pk_audit_event PRIMARY KEY (AUDIT_ID);


--
prompt Create primary key on COMPLETION_STATUS_ID - Table COMPLETION_STATUS
--

alter table COMPLETION_STATUS
    add constraint pk_completion_status PRIMARY KEY (COMPLETION_STATUS_ID);


--
prompt Create primary key on DC_SUMMARY_EVENT_ID - Table DC_COMPUTED_EVENT
--

alter table DC_COMPUTED_EVENT
    add constraint pk_dc_computed_event PRIMARY KEY (DC_SUMMARY_EVENT_ID);


--
prompt Create primary key on DC_EVENT_ID - Table DC_EVENT
--

alter table DC_EVENT
    add constraint pk_dc_event PRIMARY KEY (DC_EVENT_ID);


--
prompt Create primary key on DC_PRIMITIVE_ID - Table DC_PRIMITIVE
--

alter table DC_PRIMITIVE
    add constraint pk_dc_primitive PRIMARY KEY (DC_PRIMITIVE_ID);


--
prompt Create primary key on DC_EVENT_ID - Table DC_SECTION_EVENT
--

alter table DC_SECTION_EVENT
    add constraint pk_dc_section_event PRIMARY KEY (DC_EVENT_ID);


--
prompt Create primary key on DC_EVENT_ID - Table DC_SEND_EMAIL_EVENT
--

alter table DC_SEND_EMAIL_EVENT
    add constraint pk_dc_send_email_event PRIMARY KEY (DC_EVENT_ID);


--
prompt Create primary key on DC_EVENT_ID - Table DC_SUBSTITUTION_EVENT
--

alter table DC_SUBSTITUTION_EVENT
    add constraint pk_dc_substitution_event PRIMARY KEY (DC_EVENT_ID);


--
prompt Create primary key on DECISION_CONDITION_ID - Table DECISION_CONDITION
--

alter table DECISION_CONDITION
    add constraint pk_decision_condition PRIMARY KEY (DECISION_CONDITION_ID);


--
prompt Create primary key on EVENT_CRF_ID - Table EVENT_CRF
--

alter table EVENT_CRF
    add constraint pk_event_crf PRIMARY KEY (EVENT_CRF_ID);


--
prompt Create primary key on EXPORT_FORMAT_ID - Table EXPORT_FORMAT
--

alter table EXPORT_FORMAT
    add constraint pk_export_format PRIMARY KEY (EXPORT_FORMAT_ID);


--
prompt Create primary key on STUDY_GROUP_ID - Table STUDY_GROUP
--

alter table STUDY_GROUP
    add constraint pk_group_role PRIMARY KEY (STUDY_GROUP_ID);


--
prompt Create primary key on GROUP_CLASS_TYPE_ID - Table GROUP_CLASS_TYPES
--

alter table GROUP_CLASS_TYPES
    add constraint pk_group_types PRIMARY KEY (GROUP_CLASS_TYPE_ID);


--
prompt Create primary key on SUBJECT_ID - Table SUBJECT
--

alter table SUBJECT
    add constraint pk_individual PRIMARY KEY (SUBJECT_ID);


--
prompt Create primary key on CRF_ID - Table CRF
--

alter table CRF
    add constraint pk_instrument PRIMARY KEY (CRF_ID);


--
prompt Create primary key on ITEM_ID - Table ITEM
--

alter table ITEM
    add constraint pk_item PRIMARY KEY (ITEM_ID);


--
prompt Create primary key on ITEM_DATA_TYPE_ID - Table ITEM_DATA_TYPE
--

alter table ITEM_DATA_TYPE
    add constraint pk_item_data_type PRIMARY KEY (ITEM_DATA_TYPE_ID);


--
prompt Create primary key on ITEM_GROUP_ID - Table ITEM_GROUP
--

alter table ITEM_GROUP
    add constraint pk_item_group PRIMARY KEY (ITEM_GROUP_ID);


--
prompt Create primary key on ITEM_GROUP_METADATA_ID - Table ITEM_GROUP_METADATA
--

alter table ITEM_GROUP_METADATA
    add constraint pk_item_group_metadata PRIMARY KEY (ITEM_GROUP_METADATA_ID);


--
prompt Create primary key on ITEM_REFERENCE_TYPE_ID - Table ITEM_REFERENCE_TYPE
--

alter table ITEM_REFERENCE_TYPE
    add constraint pk_item_reference_type PRIMARY KEY (ITEM_REFERENCE_TYPE_ID);


--
prompt Create primary key on NULL_VALUE_TYPE_ID - Table NULL_VALUE_TYPE
--

alter table NULL_VALUE_TYPE
    add constraint pk_null_value_type PRIMARY KEY (NULL_VALUE_TYPE_ID);


--
prompt Create primary key on USER_ID - Table USER_ACCOUNT
--

alter table USER_ACCOUNT
    add constraint pk_person_user PRIMARY KEY (USER_ID);


--
prompt Create primary key on ITEM_FORM_METADATA_ID - Table ITEM_FORM_METADATA
--

alter table ITEM_FORM_METADATA
    add constraint pk_pl_metadata_id PRIMARY KEY (ITEM_FORM_METADATA_ID);


--
prompt Create primary key on PRIV_ID - Table PRIVILEGE
--

alter table PRIVILEGE
    add constraint pk_priv_id PRIMARY KEY (PRIV_ID);


--
prompt Create primary key on STUDY_ID - Table STUDY
--

alter table STUDY
    add constraint pk_study PRIMARY KEY (STUDY_ID);


--
prompt Create primary key on STUDY_GROUP_CLASS_ID - Table STUDY_GROUP_CLASS
--

alter table STUDY_GROUP_CLASS
    add constraint pk_project_family PRIMARY KEY (STUDY_GROUP_CLASS_ID);


--
prompt Create primary key on STUDY_SUBJECT_ID - Table STUDY_SUBJECT
--

alter table STUDY_SUBJECT
    add constraint pk_project_individual PRIMARY KEY (STUDY_SUBJECT_ID);


--
prompt Create primary key on EVENT_DEFINITION_CRF_ID - Table EVENT_DEFINITION_CRF
--

alter table EVENT_DEFINITION_CRF
    add constraint pk_project_instrument PRIMARY KEY (EVENT_DEFINITION_CRF_ID);


--
prompt Create primary key on FILTER_ID - Table FILTER
--

alter table FILTER
    add constraint pk_query_library PRIMARY KEY (FILTER_ID);


--
prompt Create primary key on DATASET_ID - Table DATASET
--

alter table DATASET
    add constraint pk_report_library PRIMARY KEY (DATASET_ID);


--
prompt Create primary key on RESPONSE_TYPE_ID - Table RESPONSE_TYPE
--

alter table RESPONSE_TYPE
    add constraint pk_response_type PRIMARY KEY (RESPONSE_TYPE_ID);


--
prompt Create primary key on ROLE_ID - Table USER_ROLE
--

alter table USER_ROLE
    add constraint pk_role_id PRIMARY KEY (ROLE_ID);


--
prompt Create primary key on RESPONSE_SET_ID - Table RESPONSE_SET
--

alter table RESPONSE_SET
    add constraint pk_rs_id PRIMARY KEY (RESPONSE_SET_ID);


--
prompt Create primary key on SECTION_ID - Table SECTION
--

alter table SECTION
    add constraint pk_section_id PRIMARY KEY (SECTION_ID);


--
prompt Create primary key on STATUS_ID - Table STATUS
--

alter table STATUS
    add constraint pk_status PRIMARY KEY (STATUS_ID);


--
prompt Create primary key on STUDY_EVENT_ID - Table STUDY_EVENT
--

alter table STUDY_EVENT
    add constraint pk_study_event PRIMARY KEY (STUDY_EVENT_ID);


--
prompt Create primary key on STUDY_EVENT_DEFINITION_ID - Table STUDY_EVENT_DEFINITION
--

alter table STUDY_EVENT_DEFINITION
    add constraint pk_study_event_definition PRIMARY KEY (STUDY_EVENT_DEFINITION_ID);


--
prompt Create primary key on STUDY_TYPE_ID - Table STUDY_TYPE
--

alter table STUDY_TYPE
    add constraint pk_study_type PRIMARY KEY (STUDY_TYPE_ID);


--
prompt Create primary key on SUBJECT_EVENT_STATUS_ID - Table SUBJECT_EVENT_STATUS
--

alter table SUBJECT_EVENT_STATUS
    add constraint pk_subject_event_status PRIMARY KEY (SUBJECT_EVENT_STATUS_ID);


--
prompt Create primary key on SUBJECT_GROUP_MAP_ID - Table SUBJECT_GROUP_MAP
--

alter table SUBJECT_GROUP_MAP
    add constraint pk_subject_group_map PRIMARY KEY (SUBJECT_GROUP_MAP_ID);


--
prompt Create primary key on USER_TYPE_ID - Table USER_TYPE
--

alter table USER_TYPE
    add constraint pk_user_type PRIMARY KEY (USER_TYPE_ID);


--
prompt Create primary key on CRF_VERSION_ID - Table CRF_VERSION
--

alter table CRF_VERSION
    add constraint pk_versioning PRIMARY KEY (CRF_VERSION_ID);


--
prompt Create primary key on AUDIT_LOG_EVENT_TYPE_ID - Table AUDIT_LOG_EVENT_TYPE
--

alter table AUDIT_LOG_EVENT_TYPE
    add constraint pkey_audit_log_event_type PRIMARY KEY (AUDIT_LOG_EVENT_TYPE_ID);


--
prompt Create primary key on RESOLUTION_STATUS_ID - Table RESOLUTION_STATUS
--

alter table RESOLUTION_STATUS
    add constraint resolution_status_pkey PRIMARY KEY (RESOLUTION_STATUS_ID);


--
prompt Create primary key on STUDY_PARAMETER_ID - Table STUDY_PARAMETER
--

alter table study_parameter
    add constraint study_parameter_pkey PRIMARY KEY (study_parameter_id);


--
prompt Create primary key on RULE_ID - Table RULE
--

alter table RULE
   add constraint rule_id_pkey PRIMARY KEY (rule_id);


--
prompt Create primary key on RULE_SET_ID - Table RULE_SET
--

alter table RULE_SET
   add constraint rule_set_id_pkey PRIMARY KEY (rule_set_id);


--
prompt Create primary key on RULE_SET_AUDIT_ID - Table RULE_SET_AUDIT
--

alter table RULE_SET_AUDIT
   add constraint rule_set_audit_id_pkey PRIMARY KEY (rule_set_audit_id);


--
prompt Create primary key on RULE_SET_RULE_ID - Table RULE_SET_RULE
--

alter table RULE_SET_RULE
   add constraint rule_set_rule_id_pkey PRIMARY KEY (rule_set_rule_id);


--
prompt Create primary key on RULE_SET_RULE_ID - Table RULE_SET_RULE_AUDIT
--

alter table RULE_SET_RULE_AUDIT
   add constraint rule_set_rule_audit_id_pkey PRIMARY KEY (rule_set_rule_audit_id);


--
prompt Create primary key on RULE_ACTION_ID - Table RULE_ACTION
--

alter table RULE_ACTION
   add constraint rule_action_id_pkey PRIMARY KEY (rule_action_id);


--
prompt Create primary key on RULE_EXPRESSION_ID - Table RULE_EXPRESSION
--

alter table RULE_EXPRESSION
    add constraint expression_id_pkey PRIMARY KEY (rule_expression_id);


