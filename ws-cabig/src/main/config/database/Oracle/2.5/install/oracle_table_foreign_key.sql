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
* Author/Dt  : Shriram  05/01/2008
*
* Comments   : None
*
--------------------------------------------------------------------------*/
--
prompt Add foreign key on table STUDY_EVENT_DEFINITION.study_id to study.study_id
--

alter table STUDY_EVENT_DEFINITION
    add constraint "FK_STUDY_EV_FK-STUDY__STUDY"
    FOREIGN KEY (study_id)
    references study (study_id);

--
prompt Add foreign key on table SUBJECT.father_id to subject.subject_id
--

alter table SUBJECT
    add constraint "HAS FATHER"
    FOREIGN KEY (father_id)
    references subject (subject_id) ON DELETE SET NULL;

--
prompt Add foreign key on table SUBJECT.mother_id to subject.subject_id
--

alter table SUBJECT
    add constraint "HAS MOTHER"
    FOREIGN KEY (mother_id)
    references subject (subject_id) ON DELETE SET NULL;

--
prompt Add foreign key on table STUDY.parent_study_id to study.study_id
--

alter table STUDY
    add constraint "PROJECT IS CONTAINED WITHIN PA"
    FOREIGN KEY (parent_study_id)
    references study (study_id) ON DELETE SET NULL;

--
prompt Add foreign key on table DISCREPANCY_NOTE.discrepancy_note_type_id to discrepancy_note_type.discrepancy_note_type_id
--

alter table DISCREPANCY_NOTE
    add constraint dn_discrepancy_note_type_id_fk
    FOREIGN KEY (discrepancy_note_type_id)
    references discrepancy_note_type (discrepancy_note_type_id);

--
prompt Add foreign key on table DISCREPANCY_NOTE.owner_id to user_account.user_id
--

alter table DISCREPANCY_NOTE
    add constraint discrepancy_note_owner_id_fkey
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table DISCREPANCY_NOTE.resolution_status_id to resolution_status.resolution_status_id
--

alter table DISCREPANCY_NOTE
    add constraint dn_resolution_status_id_fkey
    FOREIGN KEY (resolution_status_id)
    references resolution_status (resolution_status_id);

--
prompt Add foreign key on table DISCREPANCY_NOTE.study_id to study.study_id
--

alter table DISCREPANCY_NOTE
    add constraint discrepancy_note_study_id_fkey
    FOREIGN KEY (study_id)
    references study (study_id);

--
prompt Add foreign key on table DN_EVENT_CRF_MAP.discrepancy_note_id to discrepancy_note.discrepancy_note_id
--

alter table DN_EVENT_CRF_MAP
    add constraint dn_event_crf_map_dn_id_fkey
    FOREIGN KEY (discrepancy_note_id)
    references discrepancy_note (discrepancy_note_id);

--
prompt Add foreign key on table DN_EVENT_CRF_MAP.event_crf_id to event_crf.event_crf_id
--

alter table DN_EVENT_CRF_MAP
    add constraint dn_evnt_crf_map_evnt_crf_id_fk
    FOREIGN KEY (event_crf_id)
    references event_crf (event_crf_id);

--
prompt Add foreign key on table DN_ITEM_DATA_MAP.discrepancy_note_id to discrepancy_note.discrepancy_note_id
--

alter table DN_ITEM_DATA_MAP
    add constraint dn_item_data_map_dn_id_fkey
    FOREIGN KEY (discrepancy_note_id)
    references discrepancy_note (discrepancy_note_id);

--
prompt Add foreign key on table DN_ITEM_DATA_MAP.item_data_id to item_data.item_data_id
--

alter table DN_ITEM_DATA_MAP
    add constraint dn_itm_data_map_itm_data_id_fk
    FOREIGN KEY (item_data_id)
    references item_data (item_data_id);

--
prompt Add foreign key on table DN_STUDY_EVENT_MAP.discrepancy_note_id to discrepancy_note.discrepancy_note_id
--

alter table DN_STUDY_EVENT_MAP
    add constraint dn_study_event_map_dn_id_fkey
    FOREIGN KEY (discrepancy_note_id)
    references discrepancy_note (discrepancy_note_id);

--
prompt Add foreign key on table DN_STUDY_EVENT_MAP.study_event_id to study_event.study_event_id
--

alter table DN_STUDY_EVENT_MAP
    add constraint dn_sem_study_event_id_fkey
    FOREIGN KEY (study_event_id)
    references study_event (study_event_id);

--
prompt Add foreign key on table DN_STUDY_SUBJECT_MAP.discrepancy_note_id to discrepancy_note.discrepancy_note_id
--

alter table DN_STUDY_SUBJECT_MAP
    add constraint dn_study_subject_map_dn_id_fk
    FOREIGN KEY (discrepancy_note_id)
    references discrepancy_note (discrepancy_note_id);

--
prompt Add foreign key on table DN_STUDY_SUBJECT_MAP.study_subject_id to study_subject.study_subject_id
--

alter table DN_STUDY_SUBJECT_MAP
    add constraint dn_ssm_study_subject_id_fkey
    FOREIGN KEY (study_subject_id)
    references study_subject (study_subject_id);

--
prompt Add foreign key on table DN_SUBJECT_MAP.discrepancy_note_id to discrepancy_note.discrepancy_note_id
--

alter table DN_SUBJECT_MAP
    add constraint dn_subject_map_dn_id_fkey
    FOREIGN KEY (discrepancy_note_id)
    references discrepancy_note (discrepancy_note_id);

--
prompt Add foreign key on table DN_SUBJECT_MAP.subject_id to subject.subject_id
--

alter table DN_SUBJECT_MAP
    add constraint dn_subject_map_subject_id_fkey
    FOREIGN KEY (subject_id)
    references subject (subject_id);

--
prompt Add foreign key on table ITEM_DATA.item_id to item.item_id
--

alter table ITEM_DATA
    add constraint fk_answer_reference_item
    FOREIGN KEY (item_id)
    references item (item_id);

--
prompt Add foreign key on table ARCHIVED_DATASET_FILE.dataset_id to dataset.dataset_id
--

alter table ARCHIVED_DATASET_FILE
    add constraint fk_archived_reference_dataset
    FOREIGN KEY (dataset_id)
    references dataset (dataset_id);

--
prompt Add foreign key on table ARCHIVED_DATASET_FILE.export_format_id to export_format.export_format_id
--

alter table ARCHIVED_DATASET_FILE
    add constraint fk_archived_reference_export_f
    FOREIGN KEY (export_format_id)
    references export_format (export_format_id);

--
prompt Add foreign key on table AUDIT_EVENT_CONTEXT.audit_id to audit_event.audit_id
--

alter table AUDIT_EVENT_CONTEXT
    add constraint fk_audit_ev_reference_audit_ev
    FOREIGN KEY (audit_id)
    references audit_event (audit_id);

--
prompt Add foreign key on table AUDIT_EVENT_VALUES.audit_id to audit_event.audit_id
--

alter table AUDIT_EVENT_VALUES
    add constraint fk_audit_lo_ref_audit_lo
    FOREIGN KEY (audit_id)
    references audit_event (audit_id);

--
prompt Add foreign key on table COMPLETION_STATUS.status_id to status.status_id
--

alter table COMPLETION_STATUS
    add constraint fk_completi_fk_comple_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table CRF.owner_id to user_account.user_id
--

alter table CRF
    add constraint fk_crf_crf_user_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table CRF.status_id to status.status_id
--

alter table CRF
    add constraint fk_crf_fk_crf_fk_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table ITEM_GROUP_METADATA.crf_version_id to crf_version.crf_version_id
--

alter table ITEM_GROUP_METADATA
    add constraint fk_crf_metadata
    FOREIGN KEY (crf_version_id)
    references crf_version (crf_version_id);

--
prompt Add foreign key on table CRF_VERSION.owner_id to user_account.user_id
--

alter table CRF_VERSION
    add constraint fk_crf_vers_crf_versi_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table CRF_VERSION.status_id to status.status_id
--

alter table CRF_VERSION
    add constraint fk_crf_vers_fk_crf_ve_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table DATASET_CRF_VERSION_MAP.event_definition_crf_id to event_definition_crf.event_definition_crf_id
--

alter table DATASET_CRF_VERSION_MAP
    add constraint fk_dataset_ver_ref_event_def
    FOREIGN KEY (event_definition_crf_id)
    references event_definition_crf (event_definition_crf_id);

--
prompt Add foreign key on table DATASET_STUDY_GROUP_CLASS_MAP.study_group_class_id to study_group_class.study_group_class_id
--

alter table DATASET_STUDY_GROUP_CLASS_MAP
    add constraint fk_dataset_ref_study_grp_class
    FOREIGN KEY (study_group_class_id)
    references study_group_class (study_group_class_id);

--
prompt Add foreign key on table DATASET_CRF_VERSION_MAP.dataset_id to dataset.dataset_id
--

alter table DATASET_CRF_VERSION_MAP
    add constraint fk_dataset_crf_ref_dataset
    FOREIGN KEY (dataset_id)
    references dataset (dataset_id);

--
prompt Add foreign key on table DATASET_STUDY_GROUP_CLASS_MAP.dataset_id to dataset.dataset_id
--

alter table DATASET_STUDY_GROUP_CLASS_MAP
    add constraint fk_dataset_study_ref_dataset
    FOREIGN KEY (dataset_id)
    references dataset (dataset_id);

--
prompt Add foreign key on table DATASET.status_id to status.status_id
--

alter table DATASET
    add constraint fk_dataset_fk_datase_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table DATASET.study_id to study.study_id
--

alter table DATASET
    add constraint fk_dataset_fk_datase_study
    FOREIGN KEY (study_id)
    references study (study_id);

--
prompt Add foreign key on table DATASET.owner_id to user_account.user_id
--

alter table DATASET
    add constraint fk_dataset_fk_datase_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table DATASET_FILTER_MAP.dataset_id to dataset.dataset_id
--

alter table DATASET_FILTER_MAP
    add constraint fk_dataset_reference_dataset
    FOREIGN KEY (dataset_id)
    references dataset (dataset_id);

--
prompt Add foreign key on table DATASET_FILTER_MAP.filter_id to filter.filter_id
--

alter table DATASET_FILTER_MAP
    add constraint fk_dataset_reference_filter
    FOREIGN KEY (filter_id)
    references filter (filter_id);

--
prompt Add foreign key on table DC_COMPUTED_EVENT.dc_event_id to dc_event.dc_event_id
--

alter table DC_COMPUTED_EVENT
    add constraint fk_dc_compu_fk_dc_com_dc_event
    FOREIGN KEY (dc_event_id)
    references dc_event (dc_event_id);

--
prompt Add foreign key on table DC_EVENT.decision_condition_id to decision_condition.decision_condition_id
--

alter table DC_EVENT
    add constraint fk_dc_event_fk_dc_eve_decision
    FOREIGN KEY (decision_condition_id)
    references decision_condition (decision_condition_id);

--
prompt Add foreign key on table DC_PRIMITIVE.decision_condition_id to decision_condition.decision_condition_id
--

alter table DC_PRIMITIVE
    add constraint fk_dc_primi_fk_dc_pri_decision
    FOREIGN KEY (decision_condition_id)
    references decision_condition (decision_condition_id);

--
prompt Add foreign key on table DC_PRIMITIVE.dynamic_value_item_id to item.item_id
--

alter table DC_PRIMITIVE
    add constraint fk_dc_primi_fk_dc_pri_item
    FOREIGN KEY (dynamic_value_item_id)
    references item (item_id);

--
prompt Add foreign key on table DC_PRIMITIVE.item_id to item.item_id
--

alter table DC_PRIMITIVE
    add constraint fk_dc_primi_fk_item_i_item
    FOREIGN KEY (item_id)
    references item (item_id);

--
prompt Add foreign key on table DC_SECTION_EVENT.dc_event_id to dc_event.dc_event_id
--

alter table DC_SECTION_EVENT
    add constraint fk_dc_secti_fk_dc_sec_dc_event
    FOREIGN KEY (dc_event_id)
    references dc_event (dc_event_id);

--
prompt Add foreign key on table DC_SEND_EMAIL_EVENT.dc_event_id to dc_event.dc_event_id
--

alter table DC_SEND_EMAIL_EVENT
    add constraint fk_dc_send__dc_send_e_dc_event
    FOREIGN KEY (dc_event_id)
    references dc_event (dc_event_id);

--
prompt Add foreign key on table DC_SUBSTITUTION_EVENT.dc_event_id to dc_event.dc_event_id
--

alter table DC_SUBSTITUTION_EVENT
    add constraint fk_dc_subst_fk_dc_sub_dc_event
    FOREIGN KEY (dc_event_id)
    references dc_event (dc_event_id);

--
prompt Add foreign key on table DC_SUBSTITUTION_EVENT.item_id to item.item_id
--

alter table DC_SUBSTITUTION_EVENT
    add constraint fk_dc_subst_fk_dc_sub_item
    FOREIGN KEY (item_id)
    references item (item_id);

--
prompt Add foreign key on table DC_SUMMARY_ITEM_MAP.dc_summary_event_id to dc_computed_event.dc_summary_event_id
--

alter table DC_SUMMARY_ITEM_MAP
    add constraint fk_dc_summa_fk_dc_sum_dc_compu
    FOREIGN KEY (dc_summary_event_id)
    references dc_computed_event (dc_summary_event_id);

--
prompt Add foreign key on table DC_SUMMARY_ITEM_MAP.item_id to item.item_id
--

alter table DC_SUMMARY_ITEM_MAP
    add constraint fk_dc_summa_fk_dc_sum_item
    FOREIGN KEY (item_id)
    references item (item_id);

--
prompt Add foreign key on table DECISION_CONDITION.crf_version_id to crf_version.crf_version_id
--

alter table DECISION_CONDITION
    add constraint fk_decision_fk_decisi_crf_vers
    FOREIGN KEY (crf_version_id)
    references crf_version (crf_version_id);

--
prompt Add foreign key on table DECISION_CONDITION.status_id to status.status_id
--

alter table DECISION_CONDITION
    add constraint fk_decision_fk_decisi_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table DECISION_CONDITION.owner_id to user_account.user_id
--

alter table DECISION_CONDITION
    add constraint fk_decision_fk_decisi_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table EVENT_CRF.completion_status_id to completion_status.completion_status_id
--

alter table EVENT_CRF
    add constraint fk_event_cr_fk_event__completi
    FOREIGN KEY (completion_status_id)
    references completion_status (completion_status_id);

--
prompt Add foreign key on table EVENT_CRF.status_id to status.status_id
--

alter table EVENT_CRF
    add constraint fk_event_cr_fk_event__status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table EVENT_CRF.study_event_id to study_event.study_event_id
--

alter table EVENT_CRF
    add constraint fk_event_cr_fk_event__study_ev
    FOREIGN KEY (study_event_id)
    references study_event (study_event_id);

--
prompt Add foreign key on table EVENT_CRF.owner_id to user_account.user_id
--

alter table EVENT_CRF
    add constraint fk_event_cr_fk_event__user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table EVENT_CRF.study_subject_id to study_subject.study_subject_id
--

alter table EVENT_CRF
    add constraint fk_event_cr_reference_study_su
    FOREIGN KEY (study_subject_id)
    references study_subject (study_subject_id);

--
prompt Add foreign key on table EVENT_DEFINITION_CRF.status_id to status.status_id
--

alter table EVENT_DEFINITION_CRF
    add constraint fk_event_de_fk_study__status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table EVENT_DEFINITION_CRF.study_event_definition_id to study_event_definition.study_event_definition_id
--

alter table EVENT_DEFINITION_CRF
    add constraint fk_event_de_reference_study_ev
    FOREIGN KEY (study_event_definition_id)
    references study_event_definition (study_event_definition_id);

--
prompt Add foreign key on table EVENT_DEFINITION_CRF.owner_id to user_account.user_id
--

alter table EVENT_DEFINITION_CRF
    add constraint fk_event_de_study_crf_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table FILTER_CRF_VERSION_MAP.crf_version_id to crf_version.crf_version_id
--

alter table FILTER_CRF_VERSION_MAP
    add constraint fk_filter_c_reference_crf_vers
    FOREIGN KEY (crf_version_id)
    references crf_version (crf_version_id);

--
prompt Add foreign key on table FILTER_CRF_VERSION_MAP.filter_id to filter.filter_id
--

alter table FILTER_CRF_VERSION_MAP
    add constraint fk_filter_c_reference_filter
    FOREIGN KEY (filter_id)
    references filter (filter_id);

--
prompt Add foreign key on table FILTER.status_id to status.status_id
--

alter table FILTER
    add constraint fk_filter_fk_query__status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table FILTER.owner_id to user_account.user_id
--

alter table FILTER
    add constraint fk_filter_fk_query__user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table STUDY_GROUP.study_group_class_id to study_group_class.study_group_class_id
--

alter table STUDY_GROUP
    add constraint fk_group_class_study_group
    FOREIGN KEY (study_group_class_id)
    references study_group_class (study_group_class_id) ON DELETE SET NULL;

--
prompt Add foreign key on table ITEM_GROUP_METADATA.item_id to item.item_id
--

alter table ITEM_GROUP_METADATA
    add constraint fk_item
    FOREIGN KEY (item_id)
    references item (item_id);

--
prompt Add foreign key on table ITEM_DATA.status_id to status.status_id
--

alter table ITEM_DATA
    add constraint fk_item_dat_fk_item_d_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table ITEM_DATA.owner_id to user_account.user_id
--

alter table ITEM_DATA
    add constraint fk_item_dat_fk_item_d_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table ITEM.item_reference_type_id to item_reference_type.item_reference_type_id
--

alter table ITEM
    add constraint fk_item_fk_item_f_item_ref
    FOREIGN KEY (item_reference_type_id)
    references item_reference_type (item_reference_type_id);

--
prompt Add foreign key on table ITEM.item_data_type_id to item_data_type.item_data_type_id
--

alter table ITEM
    add constraint fk_item_fk_item_i_item_dat
    FOREIGN KEY (item_data_type_id)
    references item_data_type (item_data_type_id);

--
prompt Add foreign key on table ITEM.status_id to status.status_id
--

alter table ITEM
    add constraint fk_item_fk_item_s_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table ITEM.owner_id to user_account.user_id
--

alter table ITEM
    add constraint fk_item_fk_item_u_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table ITEM_GROUP.status_id to status.status_id
--

alter table ITEM_GROUP
    add constraint fk_item_gro_fk_item_g_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table ITEM_GROUP.owner_id to user_account.user_id
--

alter table ITEM_GROUP
    add constraint fk_item_gro_fk_item_g_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table ITEM_GROUP_METADATA.item_group_id to item_group.item_group_id
--

alter table ITEM_GROUP_METADATA
    add constraint fk_item_group
    FOREIGN KEY (item_group_id)
    references item_group (item_group_id);

--
prompt Add foreign key on table ITEM_GROUP.crf_id to crf.crf_id
--

alter table ITEM_GROUP
    add constraint fk_item_group_crf
    FOREIGN KEY (crf_id)
    references crf (crf_id);

--
prompt Add foreign key on table ITEM_FORM_METADATA.item_id to item.item_id
--

alter table ITEM_FORM_METADATA
    add constraint fk_item_id
    FOREIGN KEY (item_id)
    references item (item_id);

--
prompt Add foreign key on table ITEM_DATA.event_crf_id to event_crf.event_crf_id
--

alter table ITEM_DATA
    add constraint fk_item_reference_subject
    FOREIGN KEY (event_crf_id)
    references event_crf (event_crf_id);

--
prompt Add foreign key on table STUDY_USER_ROLE.study_id to study.study_id
--

alter table STUDY_USER_ROLE
    add constraint fk_person_role_study_id
    FOREIGN KEY (study_id)
    references study (study_id);

--
prompt Add foreign key on table ROLE_PRIVILEGE_MAP.priv_id to privilege.priv_id
--

alter table ROLE_PRIVILEGE_MAP
    add constraint fk_priv_id
    FOREIGN KEY (priv_id)
    references privilege (priv_id);

--
prompt Add foreign key on table STUDY_SUBJECT.study_id to study.study_id
--

alter table STUDY_SUBJECT
    add constraint fk_project__reference_study2
    FOREIGN KEY (study_id)
    references study (study_id);

--
prompt Add foreign key on table RESPONSE_SET.response_type_id to response_type.response_type_id
--

alter table RESPONSE_SET
    add constraint fk_response_fk_respon_response
    FOREIGN KEY (response_type_id)
    references response_type (response_type_id);

--
prompt Add foreign key on table ROLE_PRIVILEGE_MAP.role_id to user_role.role_id
--

alter table ROLE_PRIVILEGE_MAP
    add constraint fk_role_id
    FOREIGN KEY (role_id)
    references user_role (role_id);

--
prompt Add foreign key on table ITEM_FORM_METADATA.response_set_id to response_set.response_set_id
--

alter table ITEM_FORM_METADATA
    add constraint fk_rs_id
    FOREIGN KEY (response_set_id)
    references response_set (response_set_id);

--
prompt Add foreign key on table ITEM_FORM_METADATA.section_id to section.section_id
--

alter table ITEM_FORM_METADATA
    add constraint fk_sec_id
    FOREIGN KEY (section_id)
    references section (section_id);

--
prompt Add foreign key on table SECTION.status_id to status.status_id
--

alter table SECTION
    add constraint fk_section_fk_sectio_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table SECTION.owner_id to user_account.user_id
--

alter table SECTION
    add constraint fk_section_fk_sectio_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table SECTION.crf_version_id to crf_version.crf_version_id
--

alter table SECTION
    add constraint fk_section_version
    FOREIGN KEY (crf_version_id)
    references crf_version (crf_version_id);

--
prompt Add foreign key on table STUDY_EVENT.status_id to status.status_id
--

alter table STUDY_EVENT
    add constraint fk_study_ev_fk_study__status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table STUDY_EVENT.study_event_definition_id to study_event_definition.study_event_definition_id
--

alter table STUDY_EVENT
    add constraint fk_study_ev_fk_study__study_ev
    FOREIGN KEY (study_event_definition_id)
    references study_event_definition (study_event_definition_id);

--
prompt Add foreign key on table STUDY_EVENT.owner_id to user_account.user_id
--

alter table STUDY_EVENT
    add constraint fk_study_ev_fk_study__user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table STUDY_EVENT_DEFINITION.status_id to status.status_id
--

alter table STUDY_EVENT_DEFINITION
    add constraint fk_study_ev_fk_studye_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table STUDY_EVENT_DEFINITION.owner_id to user_account.user_id
--

alter table STUDY_EVENT_DEFINITION
    add constraint fk_study_ev_fk_studye_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table STUDY_EVENT.study_subject_id to study_subject.study_subject_id
--

alter table STUDY_EVENT
    add constraint fk_study_ev_reference_study_su
    FOREIGN KEY (study_subject_id)
    references study_subject (study_subject_id);

--
prompt Add foreign key on table STUDY.status_id to status.status_id
--

alter table STUDY
    add constraint fk_study_fk_study__status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table STUDY.owner_id to user_account.user_id
--

alter table STUDY
    add constraint fk_study_fk_study__user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table STUDY_GROUP_CLASS.group_class_type_id to group_class_types.group_class_type_id
--

alter table STUDY_GROUP_CLASS
    add constraint fk_study_gr_fk_study__group_ty
    FOREIGN KEY (group_class_type_id)
    references group_class_types (group_class_type_id);

--
prompt Add foreign key on table STUDY_GROUP_CLASS.status_id to status.status_id
--

alter table STUDY_GROUP_CLASS
    add constraint fk_study_gr_fk_study__status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table STUDY_GROUP_CLASS.owner_id to user_account.user_id
--

alter table STUDY_GROUP_CLASS
    add constraint fk_study_gr_fk_study__user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table EVENT_DEFINITION_CRF.crf_id to crf.crf_id
--

alter table EVENT_DEFINITION_CRF
    add constraint fk_study_inst_reference
    FOREIGN KEY (crf_id)
    references crf (crf_id);

--
prompt Add foreign key on table EVENT_DEFINITION_CRF.study_id to study.study_id
--

alter table EVENT_DEFINITION_CRF
    add constraint fk_study_reference_instrument
    FOREIGN KEY (study_id)
    references study (study_id);

--
prompt Add foreign key on table STUDY_SUBJECT.subject_id to subject.subject_id
--

alter table STUDY_SUBJECT
    add constraint fk_study_reference_subject
    FOREIGN KEY (subject_id)
    references subject (subject_id) ON DELETE SET NULL;

--
prompt Add foreign key on table STUDY_SUBJECT.status_id to status.status_id
--

alter table STUDY_SUBJECT
    add constraint fk_study_su_fk_study__status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table STUDY_SUBJECT.owner_id to user_account.user_id
--

alter table STUDY_SUBJECT
    add constraint fk_study_su_fk_study__user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table STUDY.type_id to study_type.study_type_id
--

alter table STUDY
    add constraint fk_study_type
    FOREIGN KEY (type_id)
    references study_type (study_type_id);

--
prompt Add foreign key on table STUDY_USER_ROLE.status_id to status.status_id
--

alter table STUDY_USER_ROLE
    add constraint fk_study_us_fk_study__status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table STUDY_USER_ROLE.owner_id to user_account.user_id
--

alter table STUDY_USER_ROLE
    add constraint fk_study_us_study_use_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table SUBJECT_GROUP_MAP.owner_id to user_account.user_id
--

alter table SUBJECT_GROUP_MAP
    add constraint fk_subject__fk_sub_gr_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table SUBJECT_GROUP_MAP.study_group_id to study_group.study_group_id
--

alter table SUBJECT_GROUP_MAP
    add constraint fk_subject__fk_subjec_group_ro
    FOREIGN KEY (study_group_id)
    references study_group (study_group_id);

--
prompt Add foreign key on table SUBJECT_GROUP_MAP.status_id to status.status_id
--

alter table SUBJECT_GROUP_MAP
    add constraint fk_subject__fk_subjec_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table SUBJECT_GROUP_MAP.study_group_class_id to study_group_class.study_group_class_id
--

alter table SUBJECT_GROUP_MAP
    add constraint fk_subject__fk_subjec_study_gr
    FOREIGN KEY (study_group_class_id)
    references study_group_class (study_group_class_id);

--
prompt Add foreign key on table SUBJECT_GROUP_MAP.study_subject_id to study_subject.study_subject_id
--

alter table SUBJECT_GROUP_MAP
    add constraint fk_subject__subject_g_study_su
    FOREIGN KEY (study_subject_id)
    references study_subject (study_subject_id);

--
prompt Add foreign key on table SUBJECT.status_id to status.status_id
--

alter table SUBJECT
    add constraint fk_subject_fk_subjec_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table SUBJECT.owner_id to user_account.user_id
--

alter table SUBJECT
    add constraint fk_subject_fk_subjec_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table EVENT_CRF.crf_version_id to crf_version.crf_version_id
--

alter table EVENT_CRF
    add constraint fk_subject_referenc_instrument
    FOREIGN KEY (crf_version_id)
    references crf_version (crf_version_id);

--
prompt Add foreign key on table USER_ACCOUNT.owner_id to user_account.user_id
--

alter table USER_ACCOUNT
    add constraint fk_user_acc_fk_user_f_user_acc
    FOREIGN KEY (owner_id)
    references user_account (user_id);

--
prompt Add foreign key on table USER_ACCOUNT.user_type_id to user_type.user_type_id
--

alter table USER_ACCOUNT
    add constraint fk_user_acc_ref_user__user_typ
    FOREIGN KEY (user_type_id)
    references user_type (user_type_id);

--
prompt Add foreign key on table USER_ACCOUNT.status_id to status.status_id
--

alter table USER_ACCOUNT
    add constraint fk_user_acc_status_re_status
    FOREIGN KEY (status_id)
    references status (status_id);

--
prompt Add foreign key on table VERSIONING_MAP.crf_version_id to crf_version.crf_version_id
--

alter table VERSIONING_MAP
    add constraint fk_versioni_fk_versio_crf_vers
    FOREIGN KEY (crf_version_id)
    references crf_version (crf_version_id);

--
prompt Add foreign key on table VERSIONING_MAP.item_id to item.item_id
--

alter table VERSIONING_MAP
    add constraint fk_versioni_fk_versio_item
    FOREIGN KEY (item_id)
    references item (item_id);

--
prompt Add foreign key on table CRF_VERSION.crf_id to crf.crf_id
--

alter table CRF_VERSION
    add constraint fk_versioni_reference_instrume
    FOREIGN KEY (crf_id)
    references crf (crf_id);

--
prompt Add foreign key on table EVENT_DEFINITION_CRF.default_version_id to crf_version.crf_version_id
--

alter table EVENT_DEFINITION_CRF
    add constraint fk_versioning_study_inst
    FOREIGN KEY (default_version_id)
    references crf_version (crf_version_id) ON DELETE SET NULL;

--
prompt Add foreign key on table STUDY_PARAMETER_VALUE.parameter to study_parameter.handle
--

alter table STUDY_PARAMETER_VALUE
    add constraint study_param_value_param_fkey
    FOREIGN KEY (parameter)
    references study_parameter (handle);

--
prompt Add foreign key on table STUDY_PARAMETER_VALUE.study_id to study.study_id
--

alter table STUDY_PARAMETER_VALUE
    add constraint study_param_value_study_id_fk
    FOREIGN KEY (study_id)
    references study (study_id);


