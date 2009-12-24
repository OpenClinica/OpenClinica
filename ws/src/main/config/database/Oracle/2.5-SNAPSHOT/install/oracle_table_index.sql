/*--------------------------------------------------------------------------
*
* File       : oracle_table_index.sql
*
* Subject    : Creates indexes for the tables
*
* Parameters : None
*
* Conditions : Create tables before creating the indexes
*
* Author/Dt  : Shriram Mani 05/12/2008
*
* Comments   : Except for primary key indexes
*
--------------------------------------------------------------------------*/

--
prompt create index on EVENT_DEFINITION_CRF (CRF_ID)
--

CREATE INDEX crf_id_event_definition_crf ON event_definition_crf (crf_id);

--
prompt create index on ITEM_GROUP_METADATA (CRF_VERSION_ID)
--

CREATE INDEX crf_ver_id_item_group_metadata ON item_group_metadata (crf_version_id);

--
prompt create index on ITEM_DATA (EVENT_CRF_ID)
--

CREATE INDEX event_crf_id_item_data_table ON item_data (event_crf_id);

--
prompt create index on AUDIT_EVENT (AUDIT_TABLE)
--

CREATE INDEX i_audit_event_audit_table ON audit_event (audit_table);

--
prompt create index on AUDIT_EVENT_CONTEXT (AUDIT_ID)
--

CREATE INDEX i_audit_event_context_audit_id ON audit_event_context (audit_id);

--
prompt create index on AUDIT_EVENT_CONTEXT (STUDY_ID)
--

CREATE INDEX i_audit_event_context_study_id ON audit_event_context (study_id);

--
prompt create index on AUDIT_EVENT (ENTITY_ID)
--

CREATE INDEX i_audit_event_entity_id ON audit_event (entity_id);

--
prompt create index on AUDIT_EVENT (USER_ID)
--

CREATE INDEX i_audit_event_user_id ON audit_event (user_id);

--
prompt create index on AUDIT_EVENT_VALUES (AUDIT_ID)
--

CREATE INDEX i_audit_event_values_audit_id ON audit_event_values (audit_id);

--
prompt create index on CRF (CRF_ID)
--

CREATE INDEX i_crf_crf_id ON crf (crf_id);

--
prompt create index on CRF_VERSION (CRF_ID)
--

CREATE INDEX i_crf_version_crf_id ON crf_version (crf_id);

--
prompt create index on CRF_VERSION (CRF_VERSION_ID)
--

CREATE INDEX i_crf_version_crf_version_id ON crf_version (crf_version_id);

--
prompt create index on DATASET (DATASET_ID)
--

CREATE INDEX i_dataset_dataset_id ON dataset (dataset_id);

--
prompt create index on DATASET (OWNER_ID)
--

CREATE INDEX i_dataset_owner_id ON dataset (owner_id);

--
prompt create index on DATASET (STUDY_ID)
--

CREATE INDEX i_dataset_study_id ON dataset (study_id);

--
prompt create index on EVENT_CRF (CRF_VERSION_ID)
--

CREATE INDEX i_event_crf_crf_version_id ON event_crf (crf_version_id);

--
prompt create index on EVENT_CRF (STUDY_EVENT_ID)
--

CREATE INDEX i_event_crf_study_event_id ON event_crf (study_event_id);

--
prompt create index on EVENT_DEFINITION_CRF (STUDY_EVENT_DEFINITION_ID)
--

CREATE INDEX i_evntdefcrf_study_evnt_def_id ON event_definition_crf (study_event_definition_id);

--
prompt create index on ITEM_FORM_METADATA (RESPONSE_SET_ID)
--

CREATE INDEX i_item_form_meta_resp_set_id ON item_form_metadata (response_set_id);

--
prompt create index on ITEM_FORM_METADATA (ITEM_ID)
--

CREATE INDEX i_item_form_metadata_item_id ON item_form_metadata (item_id);

--
prompt create index on ITEM (NAME)
--

CREATE INDEX i_item_name ON item (name);

--
prompt create index on ITEM_FORM_METADATA (CRF_VERSION_ID)
--

CREATE INDEX i_itm_form_metadata_crf_ver_id ON item_form_metadata (crf_version_id);

--
prompt create index on RESPONSE_SET (RESPONSE_TYPE_ID)
--

CREATE INDEX i_response_set_resp_type_id ON response_set (response_type_id);

--
prompt create index on SECTION (CRF_VERSION_ID)
--

CREATE INDEX i_section_crf_version_id ON section (crf_version_id);

--
prompt create index on SECTION (ORDINAL)
--

CREATE INDEX i_section_ordinal ON section (ordinal);

--
prompt create index on SECTION (PARENT_ID)
--

CREATE INDEX i_section_parent_id ON section (parent_id);

--
prompt create index on STUDY_EVENT_DEFINITION (STUDY_ID)
--

CREATE INDEX i_study_event_def_study_id ON study_event_definition (study_id);

--
prompt create index on STUDY_EVENT (STUDY_SUBJECT_ID)
--

CREATE INDEX i_study_event_study_subject_id ON study_event (study_subject_id);

--
prompt create index on STUDY_EVENT (STUDY_EVENT_DEFINITION_ID)
--

CREATE INDEX i_study_evnt_study_evt_def_id ON study_event (study_event_definition_id);

--
prompt create index on STUDY_GROUP (STUDY_GROUP_CLASS_ID)
--

CREATE INDEX i_study_grp_study_grp_class_id ON study_group (study_group_class_id);

--
prompt create index on STUDY_SUBJECT (STUDY_ID)
--

CREATE INDEX i_study_subject_study_id ON study_subject (study_id);

--
prompt create index on STUDY_SUBJECT (SUBJECT_ID)
--

CREATE INDEX i_study_subject_subject_id ON study_subject (subject_id);

--
prompt create index on STUDY_USER_ROLE (STUDY_ID)
--

CREATE INDEX i_study_user_role_study_id ON study_user_role (study_id);

--
prompt create index on STUDY_USER_ROLE (USER_NAME)
--

CREATE INDEX i_study_user_role_user_name ON study_user_role (user_name);

--
prompt create index on SUBJECT_GROUP_MAP (STUDY_GROUP_ID)
--

CREATE INDEX i_subj_grp_map_study_group_id ON subject_group_map (study_group_id);

--
prompt create index on SUBJECT_GROUP_MAP (STUDY_SUBJECT_ID)
--

CREATE INDEX i_subj_grp_map_study_subj_id ON subject_group_map (study_subject_id);

--
prompt create index on SUBJECT (FATHER_ID)
--

CREATE INDEX i_subject_father_id ON subject (father_id);

--
prompt create index on SUBJECT (MOTHER_ID)
--

CREATE INDEX i_subject_mother_id ON subject (mother_id);

--
prompt create index on USER_ACCOUNT (USER_NAME)
--

CREATE INDEX i_user_account_user_name ON user_account (user_name);

--
prompt create index on VERSIONING_MAP (CRF_VERSION_ID)
--

CREATE INDEX i_versioning_map_crf_ver_id ON versioning_map (crf_version_id);

--
prompt create index on VERSIONING_MAP (ITEM_ID)
--

CREATE INDEX i_versioning_map_item_id ON versioning_map (item_id);

--
prompt create index on ITEM_GROUP (ITEM_GROUP_ID)
--

CREATE INDEX item_group_id_item_group_table ON item_group (item_group_id);

--
prompt create index on ITEM_DATA (ITEM_ID)
--

CREATE INDEX item_id_item_data_table ON item_data (item_id);

--
prompt create index on ITEM_GROUP_METADATA (ITEM_ID)
--

CREATE INDEX item_id_item_group_metadata ON item_group_metadata (item_id);

--
prompt create index on CRF (OC_OID)
--

CREATE INDEX oc_oid_crf_table ON crf (oc_oid);

--
prompt create index on CRF_VERSION (OC_OID)
--

CREATE INDEX oc_oid_crf_version_table ON crf_version (oc_oid);

--
prompt create index on ITEM_GROUP (OC_OID)
--

CREATE INDEX oc_oid_item_group_table ON item_group (oc_oid);

--
prompt create index on ITEM (OC_OID)
--

CREATE INDEX oc_oid_item_table ON item (oc_oid);

--
prompt create index on STUDY_EVENT_DEFINITION (OC_OID)
--

CREATE INDEX oc_oid_study_event_definition ON study_event_definition (oc_oid);

--
prompt create index on STUDY_SUBJECT (OC_OID)
--

CREATE INDEX oc_oid_study_subject_table ON study_subject (oc_oid);

--
prompt create index on STUDY (OC_OID)
--

CREATE INDEX oc_oid_study_table ON study (oc_oid);

--
prompt create index on EVENT_CRF (STATUS_ID)
--

CREATE INDEX status_id_event_crf_table ON event_crf (status_id);

--
prompt create index on ITEM_DATA (STATUS_ID)
--

CREATE INDEX status_id_item_data_table ON item_data (status_id);


/*

-- Same column is indexed because of the primary key. Shriram mani


CREATE INDEX i_event_crf_event_crf_id ON event_crf USING btree (event_crf_id);
CREATE INDEX i_event_definition_crf_event_definition_crf_id ON event_definition_crf (event_definition_crf_id);
CREATE INDEX i_item_form_metadata_item_form_metadata_id ON item_form_metadata (item_form_metadata_id);
CREATE INDEX i_item_item_id ON item (item_id);
CREATE INDEX i_response_set_response_set_id ON response_set (response_set_id);
CREATE INDEX i_response_type_response_type_id ON response_type (response_type_id);
CREATE INDEX i_section_section_id ON section (section_id);
CREATE INDEX i_study_event_definition_study_event_definition_id ON study_event_definition (study_event_definition_id);
CREATE INDEX i_study_event_study_event_id ON study_event (study_event_id);
CREATE INDEX i_study_group_class_study_group_class_id ON study_group_class (study_group_class_id);
CREATE INDEX i_study_group_study_group_id ON study_group (study_group_id);
CREATE INDEX i_study_study_id ON study (study_id);
CREATE INDEX i_study_subject_study_subject_id ON study_subject (study_subject_id);
CREATE INDEX i_subject_group_map_subject_group_map_id ON subject_group_map (subject_group_map_id);
CREATE INDEX i_subject_subject_id ON subject (subject_id);
CREATE INDEX i_user_account_user_id ON user_account (user_id);

*/




