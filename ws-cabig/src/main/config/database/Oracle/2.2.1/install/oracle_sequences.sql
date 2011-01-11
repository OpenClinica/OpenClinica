/*--------------------------------------------------------------------------
*
* File       : oracle_sequences.sql
*
* Subject    : Sequences for primary keys
*
* Parameters : None
*
* Conditions : None
*
* Author/Dt  : Shriram Mani   11/16/2007
*
* Comments   : Start #s are based on the original file.
*
--------------------------------------------------------------------------*/
create sequence archived_dataset_file_id_seq start with 1;
create sequence audit_id_seq start with 10;
create sequence audit_log_event_audit_id_seq start with 10;
create sequence dataset_id_seq start with 1;
create sequence dc_summary_event_id_seq start with 1;
create sequence dc_event_id_seq start with 1;
create sequence dc_primitive_id_seq start with 1;
create sequence dc_section_event_id_seq start with 1;
create sequence dc_send_email_event_id_seq start with 1;
create sequence dc_substitution_event_id_seq start with 1;
create sequence decision_condition_id_seq start with 1;
create sequence discrepancy_note_id_seq start with 1;
create sequence discrepancy_note_type_id_seq start with 1;
create sequence event_crf_id_seq start with 1;
create sequence event_definition_crf_id_seq start with 1;
create sequence export_format_id_seq start with 1;
create sequence filter_id_seq start with 1;
create sequence group_class_type_id_seq start with 2;
create sequence item_data_id_seq start with 1;
create sequence item_data_type_id_seq start with 9;
create sequence item_form_metadata_id_seq start with 1;
create sequence item_group_id_seq start with 1;
create sequence item_group_metadata_id_seq start with 1;
create sequence item_id_seq start with 1;
create sequence item_reference_type_id_seq start with 1;
create sequence null_value_type_id_seq start with 11;
create sequence priv_id_seq start with 1;
create sequence resolution_status_id_seq start with 1;
create sequence response_set_id_seq start with 1;
create sequence response_type_id_seq start with 7;
create sequence section_id_seq start with 1;
create sequence status_id_seq start with 1;
create sequence study_event_definition_id_seq start with 1;
create sequence study_event_id_seq start with 1;
create sequence study_group_class_id_seq start with 1;
create sequence study_group_id_seq start with 1;
create sequence study_parameter_id_seq start with 1;
create sequence study_parameter_value_id_seq start with 1;
create sequence study_id_seq start with 1;
create sequence study_subject_id_seq start with 1;
create sequence study_type_id_seq start with 1;
create sequence subject_event_status_id_seq start with 1;
create sequence subject_group_map_id_seq start with 1;
create sequence subject_id_seq start with 1;
create sequence user_id_seq start with 3;
create sequence role_id_seq start with 6;
create sequence user_type_id_seq start with 1;
create sequence crf_id_seq start with 1;
create sequence crf_version_id_seq start with 1;
