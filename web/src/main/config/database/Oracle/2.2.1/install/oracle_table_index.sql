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
* Author/Dt  : Shriram Mani 12/11/2007
*
* Comments   : We can have separate tablespace for large data
*
--------------------------------------------------------------------------*/
--
-- Name: i_audit_event_audit_table; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_audit_event_audit_table ON audit_event (audit_table);


--
-- Name: i_audit_event_context_audit_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_audit_event_context_audit_id ON audit_event_context (audit_id);


--
-- Name: i_audit_event_context_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_audit_event_context_study_id ON audit_event_context (study_id);


--
-- Name: i_audit_event_entity_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_audit_event_entity_id ON audit_event (entity_id);


--
-- Name: i_audit_event_user_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_audit_event_user_id ON audit_event (user_id);


--
-- Name: i_audit_event_values_audit_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_audit_event_values_audit_id ON audit_event_values (audit_id);


--
-- Name: i_crf_crf_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_crf_crf_id ON crf (crf_id);


--
-- Name: i_crf_version_crf_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_crf_version_crf_id ON crf_version (crf_id);


--
-- Name: i_crf_version_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_crf_version_crf_version_id ON crf_version (crf_version_id);


--
-- Name: i_dataset_dataset_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_dataset_dataset_id ON dataset (dataset_id);


--
-- Name: i_dataset_owner_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_dataset_owner_id ON dataset (owner_id);


--
-- Name: i_dataset_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_dataset_study_id ON dataset (study_id);


--
-- Name: i_event_crf_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_event_crf_crf_version_id ON event_crf (crf_version_id);


/*
-- Same column is indexed because of the primary key. Shriram mani
--
-- Name: i_event_crf_event_crf_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_event_crf_event_crf_id ON event_crf USING btree (event_crf_id);
*/

--
-- Name: i_event_crf_study_event_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_event_crf_study_event_id ON event_crf (study_event_id);


/*
-- Same column is indexed because of the primary key. Shriram mani
--
-- Name: i_event_definition_crf_event_definition_crf_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_event_definition_crf_event_definition_crf_id ON event_definition_crf (event_definition_crf_id);
*/

--
-- Name: i_event_definition_crf_study_event_definition_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_evntdefcrf_study_evnt_def_id ON event_definition_crf (study_event_definition_id);


--
-- Name: i_item_form_metadata_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_itm_form_metadata_crf_ver_id ON item_form_metadata (crf_version_id);

/*
-- Same column is indexed because of the primary key. Shriram mani

--
-- Name: i_item_form_metadata_item_form_metadata_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_item_form_metadata_item_form_metadata_id ON item_form_metadata (item_form_metadata_id);

*/
--
-- Name: i_item_form_metadata_item_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_item_form_metadata_item_id ON item_form_metadata (item_id);


--
-- Name: i_item_form_metadata_response_set_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_item_form_meta_resp_set_id ON item_form_metadata (response_set_id);

/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_item_item_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_item_item_id ON item (item_id);

*/
--
-- Name: i_item_name; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_item_name ON item (name);


/*
-- Already an index exist for the primary key -- Shriram Mani
-- Name: i_response_set_response_set_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--
--

CREATE INDEX i_response_set_response_set_id ON response_set (response_set_id);
*/

--
-- Name: i_response_set_response_type_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_response_set_resp_type_id ON response_set (response_type_id);


/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_response_type_response_type_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_response_type_response_type_id ON response_type (response_type_id);
*/

--
-- Name: i_section_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_section_crf_version_id ON section (crf_version_id);


--
-- Name: i_section_ordinal; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_section_ordinal ON section (ordinal);


--
-- Name: i_section_parent_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_section_parent_id ON section (parent_id);


/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_section_section_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_section_section_id ON section (section_id);
*/

/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_study_event_definition_study_event_definition_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_study_event_definition_study_event_definition_id ON study_event_definition (study_event_definition_id);
*/

--
-- Name: i_study_event_definition_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_study_event_def_study_id ON study_event_definition (study_id);


--
-- Name: i_study_event_study_event_definition_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_study_evnt_study_evt_def_id ON study_event (study_event_definition_id);


/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_study_event_study_event_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_event_study_event_id ON study_event (study_event_id);
*/

--
-- Name: i_study_event_study_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_event_study_subject_id ON study_event (study_subject_id);


/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_study_group_class_study_group_class_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_group_class_study_group_class_id ON study_group_class (study_group_class_id);
*/

--
-- Name: i_study_group_study_group_class_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_grp_study_grp_class_id ON study_group (study_group_class_id);


/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_study_group_study_group_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_group_study_group_id ON study_group (study_group_id);
*/

/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_study_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_study_id ON study (study_id);
*/

--
-- Name: i_study_subject_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_subject_study_id ON study_subject (study_id);


/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_study_subject_study_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_subject_study_subject_id ON study_subject (study_subject_id);
*/

--
-- Name: i_study_subject_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_subject_subject_id ON study_subject (subject_id);


--
-- Name: i_study_user_role_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_study_user_role_study_id ON study_user_role (study_id);


--
-- Name: i_study_user_role_user_name; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_user_role_user_name ON study_user_role (user_name);


--
-- Name: i_subject_father_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_father_id ON subject (father_id);


--
-- Name: i_subject_group_map_study_group_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subj_grp_map_study_group_id ON subject_group_map (study_group_id);


--
-- Name: i_subject_group_map_study_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_subj_grp_map_study_subj_id ON subject_group_map (study_subject_id);


/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_subject_group_map_subject_group_map_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_group_map_subject_group_map_id ON subject_group_map (subject_group_map_id);
*/

--
-- Name: i_subject_mother_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_mother_id ON subject (mother_id);


/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_subject_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_subject_id ON subject (subject_id);
*/

/*
-- Already an index exist for the primary key -- Shriram Mani
--
-- Name: i_user_account_user_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_user_account_user_id ON user_account (user_id);
*/

--
-- Name: i_user_account_user_name; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_user_account_user_name ON user_account (user_name);


--
-- Name: i_versioning_map_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_versioning_map_crf_ver_id ON versioning_map (crf_version_id);


--
-- Name: i_versioning_map_item_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace:
--

CREATE INDEX i_versioning_map_item_id ON versioning_map (item_id);


