-- first indexing table, Tom Hickerson 10-22-2006

-- drop indexes in Postgres, working on each table to determine foreign key relationships
-- and plot out the indexes that are required.  Testing will then have to be done to figure 
-- out if noticable performance or provable performance is acheived.

-- this table will serve as the base for creating indexes,
-- other files will have to be created to drop and redrop indexes.

-- run a vacuum script

-- vacuum verbose analyse;

-- nomenclature: i_tablename_columnname

-- table: audit_event
drop index i_audit_event_audit_table;

drop index i_audit_event_user_id;

drop index i_audit_event_entity_id;

-- table: audit_event_context

drop index i_audit_event_context_audit_id;

drop index i_audit_event_context_study_id;

-- table: audit_event_values

drop index i_audit_event_values_audit_id;

-- table: crf

drop index i_crf_crf_id;

-- table: crf_version

drop index i_crf_version_crf_id;

drop index i_crf_version_crf_version_id;

-- table: dataset

drop index i_dataset_dataset_id;

drop index i_dataset_study_id;

drop index i_dataset_owner_id;

-- table: event_definition_crf

drop index i_event_definition_crf_event_definition_crf_id;

drop index i_event_definition_crf_study_event_definition_id;

-- table: event_crf

drop index i_event_crf_event_crf_id;

drop index i_event_crf_study_event_id;

drop index i_event_crf_crf_version_id;

-- table: item

drop index i_item_item_id;

drop index i_item_name;

-- table: item_form_metadata

drop index i_item_form_metadata_item_form_metadata_id;

drop index i_item_form_metadata_item_id;

drop index i_item_form_metadata_crf_version_id;

drop index i_item_form_metadata_response_set_id;

-- table: response_set

drop index i_response_set_response_set_id;

drop index i_response_set_response_type_id;

-- table: response_type

drop index i_response_type_response_type_id;

-- table: section

drop index i_section_section_id;

drop index i_section_crf_version_id;

drop index i_section_ordinal;

drop index i_section_parent_id;

-- table: study

drop index i_study_study_id;

-- table: study_event

drop index i_study_event_study_event_id;

drop index i_study_event_study_event_definition_id;

drop index i_study_event_study_subject_id;

-- to add later: sudy event status id???

-- table: study_event_definition

drop index i_study_event_definition_study_event_definition_id;

drop index i_study_event_definition_study_id;

-- table: study_group

drop index i_study_group_study_group_id;

drop index i_study_group_study_group_class_id;

-- table: study_group_class

drop index i_study_group_class_study_group_class_id;

-- table: study_subject

drop index i_study_subject_study_subject_id;

drop index i_study_subject_study_id;

drop index i_study_subject_subject_id;

-- table: study_user_role

drop index i_study_user_role_study_id;

drop index i_study_user_role_user_name;

-- table: subject

drop index i_subject_subject_id;

drop index i_subject_father_id;

drop index i_subject_mother_id;

-- table: subject_group_map

drop index i_subject_group_map_subject_group_map_id;

drop index i_subject_group_map_study_subject_id;

drop index i_subject_group_map_study_group_id;

-- table: user_account

drop index i_user_account_user_id;

drop index i_user_account_user_name;

-- table: versioning map

drop index i_versioning_map_crf_version_id;

drop index i_versioning_map_item_id;

-- in order to drop and recreate, make sure to use the drop script and then run this one again



