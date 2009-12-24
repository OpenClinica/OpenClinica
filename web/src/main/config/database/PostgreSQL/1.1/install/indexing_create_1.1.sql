-- first indexing table, Tom Hickerson 10-22-2006

-- create indexes in Postgres, working on each table to determine foreign key relationships
-- and plot out the indexes that are required.  Testing will then have to be done to figure 
-- out if noticable performance or provable performance is acheived.

-- this table will serve as the base for creating indexes,
-- other files will have to be created to drop and recreate indexes.

-- run a vacuum script

vacuum verbose analyse;

-- nomenclature: i_tablename_columnname

-- table: audit_event
create index i_audit_event_audit_table on audit_event(audit_table);

create index i_audit_event_user_id on audit_event(user_id);

create index i_audit_event_entity_id on audit_event(entity_id);

-- table: audit_event_context

create index i_audit_event_context_audit_id on audit_event_context(audit_id);

create index i_audit_event_context_study_id on audit_event_context(study_id);

-- table: audit_event_values

create index i_audit_event_values_audit_id on audit_event_values(audit_id);

-- table: crf

create index i_crf_crf_id on crf(crf_id);

-- create index i_crf_crf_version_id on crf(crf_version_id);

-- table: crf_version

create index i_crf_version_crf_id on crf_version(crf_id);

create index i_crf_version_crf_version_id on crf_version(crf_version_id);

-- table: dataset

create index i_dataset_dataset_id on dataset(dataset_id);

create index i_dataset_study_id on dataset(study_id);

create index i_dataset_owner_id on dataset(owner_id);

-- table: event_definition_crf

create index i_event_definition_crf_event_definition_crf_id on event_definition_crf(event_definition_crf_id);

create index i_event_definition_crf_study_event_definition_id on event_definition_crf(study_event_definition_id);

-- table: event_crf

create index i_event_crf_event_crf_id on event_crf(event_crf_id);

create index i_event_crf_study_event_id on event_crf(study_event_id);

create index i_event_crf_crf_version_id on event_crf(crf_version_id);

-- table: item

create index i_item_item_id on item(item_id);

create index i_item_name on item(name);

-- table: item_form_metadata

create index i_item_form_metadata_item_form_metadata_id on item_form_metadata(item_form_metadata_id);

create index i_item_form_metadata_item_id on item_form_metadata(item_id);

create index i_item_form_metadata_crf_version_id on item_form_metadata(crf_version_id);

create index i_item_form_metadata_response_set_id on item_form_metadata(response_set_id);

-- table: response_set

create index i_response_set_response_set_id on response_set(response_set_id);

create index i_response_set_response_type_id on response_set(response_type_id);

-- table: response_type

create index i_response_type_response_type_id on response_type(response_type_id);

-- table: section

create index i_section_section_id on section(section_id);

create index i_section_crf_version_id on section(crf_version_id);

create index i_section_ordinal on section(ordinal);

create index i_section_parent_id on section(parent_id);

-- table: study

create index i_study_study_id on study(study_id);

-- table: study_event

create index i_study_event_study_event_id on study_event(study_event_id);

create index i_study_event_study_event_definition_id on study_event(study_event_definition_id);

create index i_study_event_study_subject_id on study_event(study_subject_id);

-- to add later: sudy event status id???

-- table: study_event_definition

create index i_study_event_definition_study_event_definition_id on study_event_definition(study_event_definition_id);

create index i_study_event_definition_study_id on study_event_definition(study_id);

-- table: study_group

create index i_study_group_study_group_id on study_group(study_group_id);

create index i_study_group_study_group_class_id on study_group(study_group_class_id);

-- table: study_group_class

create index i_study_group_class_study_group_class_id on study_group_class(study_group_class_id);

-- table: study_subject

create index i_study_subject_study_subject_id on study_subject(study_subject_id);

create index i_study_subject_study_id on study_subject(study_id);

create index i_study_subject_subject_id on study_subject(subject_id);

-- table: study_user_role

create index i_study_user_role_study_id on study_user_role(study_id);

create index i_study_user_role_user_name on study_user_role(user_name);

-- table: subject

create index i_subject_subject_id on subject(subject_id);

create index i_subject_father_id on subject(father_id);

create index i_subject_mother_id on subject(mother_id);

-- table: subject_group_map

create index i_subject_group_map_subject_group_map_id on subject_group_map(subject_group_map_id);

create index i_subject_group_map_study_subject_id on subject_group_map(study_subject_id);

create index i_subject_group_map_study_group_id on subject_group_map(study_group_id);

-- table: user_account

create index i_user_account_user_id on user_account(user_id);

create index i_user_account_user_name on user_account(user_name);

-- table: versioning map

create index i_versioning_map_crf_version_id on versioning_map(crf_version_id);

create index i_versioning_map_item_id on versioning_map(item_id);

-- in order to drop and recreate, make sure to use the drop script and then run this one again



