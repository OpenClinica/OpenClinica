/*--------------------------------------------------------------------------
*
* File       : oracle_table_foreign_key.sql
*
* Subject    : Creates Foreign Keys for referential integrity
*
* Parameters : None
*
* Conditions : Create the table before creating the FKs
*
* Author/Dt  : Shriram  12/15/2007
*
* Comments   : Some of the foreign keys won't work unless the table exist
*
--------------------------------------------------------------------------*/
--
-- Name: FK_STUDY_EV_FK-STUDY__STUDY; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_event_definition
    ADD CONSTRAINT "FK_STUDY_EV_FK-STUDY__STUDY" FOREIGN KEY (study_id) REFERENCES study(study_id) ;


--
-- Name: HAS FATHER; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE subject
    ADD CONSTRAINT "HAS FATHER" FOREIGN KEY (father_id) REFERENCES subject(subject_id) ON DELETE SET NULL;


--
-- Name: HAS MOTHER; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE subject
    ADD CONSTRAINT "HAS MOTHER" FOREIGN KEY (mother_id) REFERENCES subject(subject_id) ON DELETE SET NULL;


--
-- Name: PROJECT IS CONTAINED WITHIN PA; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study
    ADD CONSTRAINT "PROJECT IS CONTAINED WITHIN PA" FOREIGN KEY (parent_study_id) REFERENCES study(study_id) ON DELETE SET NULL;


--
-- Name: discrepancy_note_discrepancy_note_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE discrepancy_note
    ADD CONSTRAINT dn_discrepancy_note_type_id_fk FOREIGN KEY (discrepancy_note_type_id) REFERENCES discrepancy_note_type(discrepancy_note_type_id) ;


--
-- Name: discrepancy_note_owner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE discrepancy_note
    ADD CONSTRAINT discrepancy_note_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: discrepancy_note_resolution_status_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE discrepancy_note
    ADD CONSTRAINT dn_resolution_status_id_fkey FOREIGN KEY (resolution_status_id) REFERENCES resolution_status(resolution_status_id) ;


--
-- Name: discrepancy_note_study_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE discrepancy_note
    ADD CONSTRAINT discrepancy_note_study_id_fkey FOREIGN KEY (study_id) REFERENCES study(study_id) ;


--
-- Name: dn_event_crf_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_event_crf_map
    ADD CONSTRAINT dn_event_crf_map_dn_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ;


--
-- Name: dn_event_crf_map_event_crf_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_event_crf_map
    ADD CONSTRAINT dn_evnt_crf_map_evnt_crf_id_fk FOREIGN KEY (event_crf_id) REFERENCES event_crf(event_crf_id) ;


--
-- Name: dn_item_data_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_item_data_map
    ADD CONSTRAINT dn_item_data_map_dn_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ;


--
-- Name: dn_item_data_map_item_data_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_item_data_map
    ADD CONSTRAINT dn_itm_data_map_itm_data_id_fk FOREIGN KEY (item_data_id) REFERENCES item_data(item_data_id) ;


--
-- Name: dn_study_event_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_study_event_map
    ADD CONSTRAINT dn_study_event_map_dn_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ;


--
-- Name: dn_study_event_map_study_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_study_event_map
    ADD CONSTRAINT dn_sem_study_event_id_fkey FOREIGN KEY (study_event_id) REFERENCES study_event(study_event_id) ;


--
-- Name: dn_study_subject_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_study_subject_map
    ADD CONSTRAINT dn_study_subject_map_dn_id_fk FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ;


--
-- Name: dn_study_subject_map_study_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_study_subject_map
    ADD CONSTRAINT dn_ssm_study_subject_id_fkey FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ;


--
-- Name: dn_subject_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_subject_map
    ADD CONSTRAINT dn_subject_map_dn_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ;


--
-- Name: dn_subject_map_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dn_subject_map
    ADD CONSTRAINT dn_subject_map_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(subject_id) ;


--
-- Name: fk_answer_reference_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_data
    ADD CONSTRAINT fk_answer_reference_item FOREIGN KEY (item_id) REFERENCES item(item_id);


--
-- Name: fk_archived_reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE archived_dataset_file
    ADD CONSTRAINT fk_archived_reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ;


--
-- Name: fk_archived_reference_export_f; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE archived_dataset_file
    ADD CONSTRAINT fk_archived_reference_export_f FOREIGN KEY (export_format_id) REFERENCES export_format(export_format_id) ;


--
-- Name: fk_audit_ev_reference_audit_ev; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE audit_event_context
    ADD CONSTRAINT fk_audit_ev_reference_audit_ev FOREIGN KEY (audit_id) REFERENCES audit_event(audit_id) ;


--
-- Name: fk_audit_lo_ref_audit_lo; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE audit_event_values
    ADD CONSTRAINT fk_audit_lo_ref_audit_lo FOREIGN KEY (audit_id) REFERENCES audit_event(audit_id) ;


--
-- Name: fk_completi_fk_comple_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE completion_status
    ADD CONSTRAINT fk_completi_fk_comple_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_crf_crf_user_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE crf
    ADD CONSTRAINT fk_crf_crf_user_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_crf_fk_crf_fk_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE crf
    ADD CONSTRAINT fk_crf_fk_crf_fk_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_crf_metadata; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_group_metadata
    ADD CONSTRAINT fk_crf_metadata FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ;


--
-- Name: fk_crf_vers_crf_versi_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE crf_version
    ADD CONSTRAINT fk_crf_vers_crf_versi_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_crf_vers_fk_crf_ve_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE crf_version
    ADD CONSTRAINT fk_crf_vers_fk_crf_ve_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_dataset__ref_event_event_de; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dataset_crf_version_map
    ADD CONSTRAINT fk_dataset__ref_event_event_de FOREIGN KEY (event_definition_crf_id) REFERENCES event_definition_crf(event_definition_crf_id) ;


--
-- Name: fk_dataset__ref_study_group_class; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dataset_study_group_class_map
    ADD CONSTRAINT fk_dataset_ref_study_grp_class FOREIGN KEY (study_group_class_id) REFERENCES study_group_class(study_group_class_id) ;


--
-- Name: fk_dataset__reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dataset_crf_version_map
    ADD CONSTRAINT fk_dataset__reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ;


--
-- Name: fk_dataset__reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dataset_study_group_class_map
    ADD CONSTRAINT fk_dataset__reference_dataset1 FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ;


--
-- Name: fk_dataset_fk_datase_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dataset
    ADD CONSTRAINT fk_dataset_fk_datase_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_dataset_fk_datase_study; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dataset
    ADD CONSTRAINT fk_dataset_fk_datase_study FOREIGN KEY (study_id) REFERENCES study(study_id) ;


--
-- Name: fk_dataset_fk_datase_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dataset
    ADD CONSTRAINT fk_dataset_fk_datase_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_dataset_reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dataset_filter_map
    ADD CONSTRAINT fk_dataset_reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ;


--
-- Name: fk_dataset_reference_filter; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dataset_filter_map
    ADD CONSTRAINT fk_dataset_reference_filter FOREIGN KEY (filter_id) REFERENCES filter(filter_id) ;


--
-- Name: fk_dc_compu_fk_dc_com_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_computed_event
    ADD CONSTRAINT fk_dc_compu_fk_dc_com_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ;


--
-- Name: fk_dc_event_fk_dc_eve_decision; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_event
    ADD CONSTRAINT fk_dc_event_fk_dc_eve_decision FOREIGN KEY (decision_condition_id) REFERENCES decision_condition(decision_condition_id) ;


--
-- Name: fk_dc_primi_fk_dc_pri_decision; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_primitive
    ADD CONSTRAINT fk_dc_primi_fk_dc_pri_decision FOREIGN KEY (decision_condition_id) REFERENCES decision_condition(decision_condition_id) ;


--
-- Name: fk_dc_primi_fk_dc_pri_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_primitive
    ADD CONSTRAINT fk_dc_primi_fk_dc_pri_item FOREIGN KEY (dynamic_value_item_id) REFERENCES item(item_id) ;


--
-- Name: fk_dc_primi_fk_item_i_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_primitive
    ADD CONSTRAINT fk_dc_primi_fk_item_i_item FOREIGN KEY (item_id) REFERENCES item(item_id) ;


--
-- Name: fk_dc_secti_fk_dc_sec_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_section_event
    ADD CONSTRAINT fk_dc_secti_fk_dc_sec_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ;


--
-- Name: fk_dc_send__dc_send_e_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_send_email_event
    ADD CONSTRAINT fk_dc_send__dc_send_e_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ;


--
-- Name: fk_dc_subst_fk_dc_sub_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_substitution_event
    ADD CONSTRAINT fk_dc_subst_fk_dc_sub_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ;


--
-- Name: fk_dc_subst_fk_dc_sub_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_substitution_event
    ADD CONSTRAINT fk_dc_subst_fk_dc_sub_item FOREIGN KEY (item_id) REFERENCES item(item_id) ;


--
-- Name: fk_dc_summa_fk_dc_sum_dc_compu; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_summary_item_map
    ADD CONSTRAINT fk_dc_summa_fk_dc_sum_dc_compu FOREIGN KEY (dc_summary_event_id) REFERENCES dc_computed_event(dc_summary_event_id) ;


--
-- Name: fk_dc_summa_fk_dc_sum_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE dc_summary_item_map
    ADD CONSTRAINT fk_dc_summa_fk_dc_sum_item FOREIGN KEY (item_id) REFERENCES item(item_id) ;


--
-- Name: fk_decision_fk_decisi_crf_vers; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE decision_condition
    ADD CONSTRAINT fk_decision_fk_decisi_crf_vers FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ;


--
-- Name: fk_decision_fk_decisi_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE decision_condition
    ADD CONSTRAINT fk_decision_fk_decisi_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_decision_fk_decisi_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE decision_condition
    ADD CONSTRAINT fk_decision_fk_decisi_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_event_cr_fk_event__completi; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__completi FOREIGN KEY (completion_status_id) REFERENCES completion_status(completion_status_id) ;


--
-- Name: fk_event_cr_fk_event__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_event_cr_fk_event__study_ev; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__study_ev FOREIGN KEY (study_event_id) REFERENCES study_event(study_event_id) ;


--
-- Name: fk_event_cr_fk_event__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_event_cr_reference_study_su; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_crf
    ADD CONSTRAINT fk_event_cr_reference_study_su FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ;


--
-- Name: fk_event_de_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_definition_crf
    ADD CONSTRAINT fk_event_de_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_event_de_reference_study_ev; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_definition_crf
    ADD CONSTRAINT fk_event_de_reference_study_ev FOREIGN KEY (study_event_definition_id) REFERENCES study_event_definition(study_event_definition_id) ;


--
-- Name: fk_event_de_study_crf_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_definition_crf
    ADD CONSTRAINT fk_event_de_study_crf_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_filter_c_reference_crf_vers; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE filter_crf_version_map
    ADD CONSTRAINT fk_filter_c_reference_crf_vers FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ;


--
-- Name: fk_filter_c_reference_filter; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE filter_crf_version_map
    ADD CONSTRAINT fk_filter_c_reference_filter FOREIGN KEY (filter_id) REFERENCES filter(filter_id) ;


--
-- Name: fk_filter_fk_query__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE filter
    ADD CONSTRAINT fk_filter_fk_query__status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_filter_fk_query__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE filter
    ADD CONSTRAINT fk_filter_fk_query__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_group_class_study_group; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_group
    ADD CONSTRAINT fk_group_class_study_group FOREIGN KEY (study_group_class_id) REFERENCES study_group_class(study_group_class_id) ON DELETE SET NULL;


--
-- Name: fk_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_group_metadata
    ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(item_id) ;


--
-- Name: fk_item_dat_fk_item_d_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_data
    ADD CONSTRAINT fk_item_dat_fk_item_d_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_item_dat_fk_item_d_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_data
    ADD CONSTRAINT fk_item_dat_fk_item_d_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_item_fk_item_f_item_ref; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item
    ADD CONSTRAINT fk_item_fk_item_f_item_ref FOREIGN KEY (item_reference_type_id) REFERENCES item_reference_type(item_reference_type_id) ;


--
-- Name: fk_item_fk_item_i_item_dat; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item
    ADD CONSTRAINT fk_item_fk_item_i_item_dat FOREIGN KEY (item_data_type_id) REFERENCES item_data_type(item_data_type_id) ;


--
-- Name: fk_item_fk_item_s_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item
    ADD CONSTRAINT fk_item_fk_item_s_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_item_fk_item_u_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item
    ADD CONSTRAINT fk_item_fk_item_u_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_item_gro_fk_item_g_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_group
    ADD CONSTRAINT fk_item_gro_fk_item_g_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_item_gro_fk_item_g_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_group
    ADD CONSTRAINT fk_item_gro_fk_item_g_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_item_group; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_group_metadata
    ADD CONSTRAINT fk_item_group FOREIGN KEY (item_group_id) REFERENCES item_group(item_group_id) ;


--
-- Name: fk_item_group_crf; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_group
    ADD CONSTRAINT fk_item_group_crf FOREIGN KEY (crf_id) REFERENCES crf(crf_id) ;


--
-- Name: fk_item_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_form_metadata
    ADD CONSTRAINT fk_item_id FOREIGN KEY (item_id) REFERENCES item(item_id);


--
-- Name: fk_item_reference_subject; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_data
    ADD CONSTRAINT fk_item_reference_subject FOREIGN KEY (event_crf_id) REFERENCES event_crf(event_crf_id);


--
-- Name: fk_person_role_study_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_user_role
    ADD CONSTRAINT fk_person_role_study_id FOREIGN KEY (study_id) REFERENCES study(study_id);


--
-- Name: fk_priv_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE role_privilege_map
    ADD CONSTRAINT fk_priv_id FOREIGN KEY (priv_id) REFERENCES privilege(priv_id);


--
-- Name: fk_project__reference_study2; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_subject
    ADD CONSTRAINT fk_project__reference_study2 FOREIGN KEY (study_id) REFERENCES study(study_id);


--
-- Name: fk_response_fk_respon_response; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE response_set
    ADD CONSTRAINT fk_response_fk_respon_response FOREIGN KEY (response_type_id) REFERENCES response_type(response_type_id) ;


--
-- Name: fk_role_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE role_privilege_map
    ADD CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES user_role(role_id);


--
-- Name: fk_rs_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_form_metadata
    ADD CONSTRAINT fk_rs_id FOREIGN KEY (response_set_id) REFERENCES response_set(response_set_id);


--
-- Name: fk_sec_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE item_form_metadata
    ADD CONSTRAINT fk_sec_id FOREIGN KEY (section_id) REFERENCES section(section_id);


--
-- Name: fk_section_fk_sectio_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE section
    ADD CONSTRAINT fk_section_fk_sectio_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_section_fk_sectio_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE section
    ADD CONSTRAINT fk_section_fk_sectio_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_section_version; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE section
    ADD CONSTRAINT fk_section_version FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id);


--
-- Name: fk_study_ev_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_event
    ADD CONSTRAINT fk_study_ev_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_study_ev_fk_study__study_ev; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_event
    ADD CONSTRAINT fk_study_ev_fk_study__study_ev FOREIGN KEY (study_event_definition_id) REFERENCES study_event_definition(study_event_definition_id) ;


--
-- Name: fk_study_ev_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_event
    ADD CONSTRAINT fk_study_ev_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_study_ev_fk_studye_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_event_definition
    ADD CONSTRAINT fk_study_ev_fk_studye_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_study_ev_fk_studye_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_event_definition
    ADD CONSTRAINT fk_study_ev_fk_studye_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_study_ev_reference_study_su; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_event
    ADD CONSTRAINT fk_study_ev_reference_study_su FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ;


--
-- Name: fk_study_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study
    ADD CONSTRAINT fk_study_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_study_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study
    ADD CONSTRAINT fk_study_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_study_gr_fk_study__group_ty; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_group_class
    ADD CONSTRAINT fk_study_gr_fk_study__group_ty FOREIGN KEY (group_class_type_id) REFERENCES group_class_types(group_class_type_id) ;


--
-- Name: fk_study_gr_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_group_class
    ADD CONSTRAINT fk_study_gr_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_study_gr_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_group_class
    ADD CONSTRAINT fk_study_gr_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_study_inst_reference; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_definition_crf
    ADD CONSTRAINT fk_study_inst_reference FOREIGN KEY (crf_id) REFERENCES crf(crf_id);


--
-- Name: fk_study_reference_instrument; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_definition_crf
    ADD CONSTRAINT fk_study_reference_instrument FOREIGN KEY (study_id) REFERENCES study(study_id);


--
-- Name: fk_study_reference_subject; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_subject
    ADD CONSTRAINT fk_study_reference_subject FOREIGN KEY (subject_id) REFERENCES subject(subject_id) ON DELETE SET NULL;


--
-- Name: fk_study_su_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_subject
    ADD CONSTRAINT fk_study_su_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_study_su_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_subject
    ADD CONSTRAINT fk_study_su_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_study_type; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study
    ADD CONSTRAINT fk_study_type FOREIGN KEY (type_id) REFERENCES study_type(study_type_id) ;


--
-- Name: fk_study_us_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_user_role
    ADD CONSTRAINT fk_study_us_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_study_us_study_use_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_user_role
    ADD CONSTRAINT fk_study_us_study_use_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_subject__fk_sub_gr_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE subject_group_map
    ADD CONSTRAINT fk_subject__fk_sub_gr_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_subject__fk_subjec_group_ro; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE subject_group_map
    ADD CONSTRAINT fk_subject__fk_subjec_group_ro FOREIGN KEY (study_group_id) REFERENCES study_group(study_group_id) ;


--
-- Name: fk_subject__fk_subjec_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE subject_group_map
    ADD CONSTRAINT fk_subject__fk_subjec_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_subject__fk_subjec_study_gr; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE subject_group_map
    ADD CONSTRAINT fk_subject__fk_subjec_study_gr FOREIGN KEY (study_group_class_id) REFERENCES study_group_class(study_group_class_id) ;


--
-- Name: fk_subject__subject_g_study_su; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE subject_group_map
    ADD CONSTRAINT fk_subject__subject_g_study_su FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ;


--
-- Name: fk_subject_fk_subjec_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE subject
    ADD CONSTRAINT fk_subject_fk_subjec_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_subject_fk_subjec_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE subject
    ADD CONSTRAINT fk_subject_fk_subjec_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_subject_reference_instrument; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_crf
    ADD CONSTRAINT fk_subject_referenc_instrument FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ;


--
-- Name: fk_user_acc_fk_user_f_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE user_account
    ADD CONSTRAINT fk_user_acc_fk_user_f_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ;


--
-- Name: fk_user_acc_ref_user__user_typ; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE user_account
    ADD CONSTRAINT fk_user_acc_ref_user__user_typ FOREIGN KEY (user_type_id) REFERENCES user_type(user_type_id) ;


--
-- Name: fk_user_acc_status_re_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE user_account
    ADD CONSTRAINT fk_user_acc_status_re_status FOREIGN KEY (status_id) REFERENCES status(status_id) ;


--
-- Name: fk_versioni_fk_versio_crf_vers; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE versioning_map
    ADD CONSTRAINT fk_versioni_fk_versio_crf_vers FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ;


--
-- Name: fk_versioni_fk_versio_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE versioning_map
    ADD CONSTRAINT fk_versioni_fk_versio_item FOREIGN KEY (item_id) REFERENCES item(item_id) ;


--
-- Name: fk_versioni_reference_instrume; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE crf_version
    ADD CONSTRAINT fk_versioni_reference_instrume FOREIGN KEY (crf_id) REFERENCES crf(crf_id);


--
-- Name: fk_versioning_study_inst; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE event_definition_crf
    ADD CONSTRAINT fk_versioning_study_inst FOREIGN KEY (default_version_id) REFERENCES crf_version(crf_version_id) ON DELETE SET NULL;


--
-- Name: study_parameter_value_parameter_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_parameter_value
    ADD CONSTRAINT study_param_value_param_fkey FOREIGN KEY (parameter) REFERENCES study_parameter(handle) ;


--
-- Name: study_parameter_value_study_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE study_parameter_value
    ADD CONSTRAINT study_param_value_study_id_fk FOREIGN KEY (study_id) REFERENCES study(study_id) ;


