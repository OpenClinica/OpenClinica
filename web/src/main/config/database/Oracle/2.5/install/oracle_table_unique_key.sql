/*--------------------------------------------------------------------------
*
* File       : oracle_table_unique_key.sql
*
* Subject    : Creates unique key constraints for certain tables
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
prompt Adding Unique Constraint UNIQ_CRF_OC_OID to table CRF
--

alter table CRF
    add constraint uniq_crf_oc_oid UNIQUE (OC_OID);

--
prompt Adding Unique Constraint UNIQ_STUDY_EVENT_DEF_OID to table STUDY_EVENT_DEFINITION
--

alter table STUDY_EVENT_DEFINITION
    add constraint uniq_study_event_def_oid UNIQUE (OC_OID);

--
prompt Adding Unique Constraint UNIQ_CRF_VERSION_OC_OID to table CRF_VERSION
--

alter table CRF_VERSION
    add constraint uniq_crf_version_oc_oid UNIQUE (OC_OID);

--
prompt Adding Unique Constraint UNIQ_ITEM_GROUP_OC_OID to table ITEM_GROUP
--

alter table ITEM_GROUP
    add constraint uniq_item_group_oc_oid UNIQUE (OC_OID);

--
prompt Adding Unique Constraint UNIQ_ITEM_OC_OID to table ITEM
--

alter table ITEM
    add constraint uniq_item_oc_oid UNIQUE (OC_OID);

--
prompt Adding Unique Constraint UNIQ_STUDY_OID to table STUDY
--

alter table STUDY
    add constraint uniq_study_oid UNIQUE (OC_OID);

--
prompt Adding Unique Constraint HANDLE to table STUDY_PARAMETER
--

alter table STUDY_PARAMETER
    add constraint study_parameter_handle_key UNIQUE (HANDLE);

--
prompt Adding Unique Constraint UNIQ_STUDY_SUBJECT_OID to table STUDY_SUBJECT
--

alter table STUDY_SUBJECT
    add constraint uniq_study_subject_oid UNIQUE (OC_OID);


