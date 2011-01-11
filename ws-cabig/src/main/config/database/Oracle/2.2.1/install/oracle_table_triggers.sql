/*--------------------------------------------------------------------------
*
* File       : oracle_table_triggers.sql
*
* Subject    : Triggers to capture audit data and for assigning PK value
*
* Parameters : None
*
* Conditions : Tables should exists before creating triggers
*
* Author/Dt  : Shriram Mani 12/17/2007
* Modified   : Shriram Mani 01/28/2008 - Added trigger operation for
                the first 4 triggers.
*
* Comments   : None
*
--------------------------------------------------------------------------*/

CREATE OR REPLACE TRIGGER event_crf_update
    AFTER UPDATE ON event_crf
    FOR EACH ROW
declare
trg_operation varchar2(10);
newrec  event_crf%rowtype;
oldrec  event_crf%rowtype;
begin
--
newrec.EVENT_CRF_ID              := :new.EVENT_CRF_ID;
newrec.STUDY_EVENT_ID            := :new.STUDY_EVENT_ID;
newrec.CRF_VERSION_ID            := :new.CRF_VERSION_ID;
newrec.DATE_INTERVIEWED          := :new.DATE_INTERVIEWED;
newrec.INTERVIEWER_NAME          := :new.INTERVIEWER_NAME;
newrec.COMPLETION_STATUS_ID      := :new.COMPLETION_STATUS_ID;
newrec.STATUS_ID                 := :new.STATUS_ID;
newrec.ANNOTATIONS               := :new.ANNOTATIONS;
newrec.DATE_COMPLETED            := :new.DATE_COMPLETED;
newrec.VALIDATOR_ID              := :new.VALIDATOR_ID;
newrec.DATE_VALIDATE             := :new.DATE_VALIDATE;
newrec.DATE_VALIDATE_COMPLETED   := :new.DATE_VALIDATE_COMPLETED;
newrec.VALIDATOR_ANNOTATIONS     := :new.VALIDATOR_ANNOTATIONS;
newrec.VALIDATE_STRING           := :new.VALIDATE_STRING;
newrec.OWNER_ID                  := :new.OWNER_ID;
newrec.DATE_CREATED              := :new.DATE_CREATED;
newrec.STUDY_SUBJECT_ID          := :new.STUDY_SUBJECT_ID;
newrec.DATE_UPDATED              := :new.DATE_UPDATED;
newrec.UPDATE_ID                 := :new.UPDATE_ID;
--
oldrec.EVENT_CRF_ID              := :old.EVENT_CRF_ID;
oldrec.STUDY_EVENT_ID            := :old.STUDY_EVENT_ID;
oldrec.CRF_VERSION_ID            := :old.CRF_VERSION_ID;
oldrec.DATE_INTERVIEWED          := :old.DATE_INTERVIEWED;
oldrec.INTERVIEWER_NAME          := :old.INTERVIEWER_NAME;
oldrec.COMPLETION_STATUS_ID      := :old.COMPLETION_STATUS_ID;
oldrec.STATUS_ID                 := :old.STATUS_ID;
oldrec.ANNOTATIONS               := :old.ANNOTATIONS;
oldrec.DATE_COMPLETED            := :old.DATE_COMPLETED;
oldrec.VALIDATOR_ID              := :old.VALIDATOR_ID;
oldrec.DATE_VALIDATE             := :old.DATE_VALIDATE;
oldrec.DATE_VALIDATE_COMPLETED   := :old.DATE_VALIDATE_COMPLETED;
oldrec.VALIDATOR_ANNOTATIONS     := :old.VALIDATOR_ANNOTATIONS;
oldrec.VALIDATE_STRING           := :old.VALIDATE_STRING;
oldrec.OWNER_ID                  := :old.OWNER_ID;
oldrec.DATE_CREATED              := :old.DATE_CREATED;
oldrec.STUDY_SUBJECT_ID          := :old.STUDY_SUBJECT_ID;
oldrec.DATE_UPDATED              := :old.DATE_UPDATED;
oldrec.UPDATE_ID                 := :old.UPDATE_ID;
--
  if inserting then
    trg_operation := 'INSERT';
  elsif updating then
    trg_operation := 'UPDATE';
  elsif deleting then
    trg_operation := 'DELETE';
  end if;
   clinica_table_api.event_crf_trigger(trg_operation, newrec, oldrec);
end;
/


--
-- Name: global_subject_insert_update; Type: TRIGGER; Schema: public; Owner: clinica
--

CREATE OR REPLACE TRIGGER global_subject_insert_update
    AFTER INSERT OR UPDATE ON subject
    FOR EACH ROW
declare
trg_operation varchar2(10);
newrec  subject%rowtype;
oldrec  subject%rowtype;
begin
  --
  newrec.SUBJECT_ID         := :new.SUBJECT_ID;
  newrec.FATHER_ID          := :new.FATHER_ID;
  newrec.MOTHER_ID          := :new.MOTHER_ID;
  newrec.STATUS_ID          := :new.STATUS_ID;
  newrec.DATE_OF_BIRTH      := :new.DATE_OF_BIRTH;
  newrec.GENDER             := :new.GENDER;
  newrec.UNIQUE_IDENTIFIER  := :new.UNIQUE_IDENTIFIER;
  newrec.DATE_CREATED       := :new.DATE_CREATED;
  newrec.OWNER_ID           := :new.OWNER_ID;
  newrec.DATE_UPDATED       := :new.DATE_UPDATED;
  newrec.UPDATE_ID          := :new.UPDATE_ID;
  newrec.DOB_COLLECTED      := :new.DOB_COLLECTED;
  --
  oldrec.SUBJECT_ID         := :old.SUBJECT_ID;
  oldrec.FATHER_ID          := :old.FATHER_ID;
  oldrec.MOTHER_ID          := :old.MOTHER_ID;
  oldrec.STATUS_ID          := :old.STATUS_ID;
  oldrec.DATE_OF_BIRTH      := :old.DATE_OF_BIRTH;
  oldrec.GENDER             := :old.GENDER;
  oldrec.UNIQUE_IDENTIFIER  := :old.UNIQUE_IDENTIFIER;
  oldrec.DATE_CREATED       := :old.DATE_CREATED;
  oldrec.OWNER_ID           := :old.OWNER_ID;
  oldrec.DATE_UPDATED       := :old.DATE_UPDATED;
  oldrec.UPDATE_ID          := :old.UPDATE_ID;
  oldrec.DOB_COLLECTED      := :old.DOB_COLLECTED;

  if inserting then
    trg_operation := 'INSERT';
  elsif updating then
    trg_operation := 'UPDATE';
  elsif deleting then
    trg_operation := 'DELETE';
  end if;
  --
  clinica_table_api.global_subject_trigger(trg_operation, newrec, oldrec);
end;
/
--
-- Name: item_data_update; Type: TRIGGER; Schema: public; Owner: clinica
--

CREATE OR REPLACE TRIGGER item_data_update
    AFTER DELETE OR UPDATE ON item_data
    FOR EACH ROW
declare
  trg_operation  varchar2(10);
  newrec         item_data%rowtype;
  oldrec         item_data%rowtype;
begin
  --
  newrec.ITEM_DATA_ID := :new.ITEM_DATA_ID;
  newrec.ITEM_ID      := :new.ITEM_ID;
  newrec.EVENT_CRF_ID := :new.EVENT_CRF_ID;
  newrec.STATUS_ID    := :new.STATUS_ID;
  newrec.VALUE        := :new.VALUE;
  newrec.DATE_CREATED := :new.DATE_CREATED;
  newrec.DATE_UPDATED := :new.DATE_UPDATED;
  newrec.OWNER_ID     := :new.OWNER_ID;
  newrec.UPDATE_ID    := :new.UPDATE_ID;
  newrec.ORDINAL      := :new.ORDINAL;
  --
  oldrec.ITEM_DATA_ID := :old.ITEM_DATA_ID;
  oldrec.ITEM_ID      := :old.ITEM_ID;
  oldrec.EVENT_CRF_ID := :old.EVENT_CRF_ID;
  oldrec.STATUS_ID    := :old.STATUS_ID;
  oldrec.VALUE        := :old.VALUE;
  oldrec.DATE_CREATED := :old.DATE_CREATED;
  oldrec.DATE_UPDATED := :old.DATE_UPDATED;
  oldrec.OWNER_ID     := :old.OWNER_ID;
  oldrec.UPDATE_ID    := :old.UPDATE_ID;
  oldrec.ORDINAL      := :old.ORDINAL;
  --
  if inserting then
    trg_operation := 'INSERT';
  elsif updating then
    trg_operation := 'UPDATE';
  elsif deleting then
    trg_operation := 'DELETE';
  end if;
  --
  clinica_table_api.item_data_trigger(trg_operation, newrec, oldrec);
  --
end;
/

--
-- Name: study_subject_insert_updare; Type: TRIGGER; Schema: public; Owner: clinica
--

CREATE OR REPLACE TRIGGER study_subject_insert_update
    AFTER INSERT OR UPDATE ON study_subject
    FOR EACH ROW
declare
  trg_operation  varchar2(10);
  newrec         study_subject%rowtype;
  oldrec         study_subject%rowtype;
begin
  --
  newrec.STUDY_SUBJECT_ID  := :new.STUDY_SUBJECT_ID;
  newrec.LABEL             := :new.LABEL;
  newrec.SECONDARY_LABEL   := :new.SECONDARY_LABEL;
  newrec.SUBJECT_ID        := :new.SUBJECT_ID;
  newrec.STUDY_ID          := :new.STUDY_ID;
  newrec.STATUS_ID         := :new.STATUS_ID;
  newrec.ENROLLMENT_DATE   := :new.ENROLLMENT_DATE;
  newrec.DATE_CREATED      := :new.DATE_CREATED;
  newrec.DATE_UPDATED	   := :new.DATE_UPDATED;
  newrec.OWNER_ID	       := :new.OWNER_ID;
  newrec.UPDATE_ID	       := :new.UPDATE_ID;
  --
  oldrec.STUDY_SUBJECT_ID  := :old.STUDY_SUBJECT_ID;
  oldrec.LABEL             := :old.LABEL;
  oldrec.SECONDARY_LABEL   := :old.SECONDARY_LABEL;
  oldrec.SUBJECT_ID        := :old.SUBJECT_ID;
  oldrec.STUDY_ID          := :old.STUDY_ID;
  oldrec.STATUS_ID         := :old.STATUS_ID;
  oldrec.ENROLLMENT_DATE   := :old.ENROLLMENT_DATE;
  oldrec.DATE_CREATED      := :old.DATE_CREATED;
  oldrec.DATE_UPDATED	   := :old.DATE_UPDATED;
  oldrec.OWNER_ID	       := :old.OWNER_ID;
  oldrec.UPDATE_ID	       := :old.UPDATE_ID;
  --
  if inserting then
    trg_operation := 'INSERT';
  elsif updating then
    trg_operation := 'UPDATE';
  elsif deleting then
    trg_operation := 'DELETE';
  end if;
  --
  clinica_table_api.study_subject_trigger(trg_operation, newrec, oldrec);
end;
/


Rem New triggers to support default value assignment for PKs




create or replace trigger crf_bef_trg 
  before insert on crf
  for each row
begin
  if :new.crf_id is null then
    select crf_id_seq.nextval
      into :new.crf_id
      from dual;
  end if;
end;
/

create or replace trigger crf_version_bef_trg 
  before insert on crf_version
  for each row
begin
  if :new.crf_version_id is null then
    select crf_version_id_seq.nextval
      into :new.crf_version_id
      from dual;
  end if;
end;
/

create or replace trigger archived_dataset_file_bef_trg
  before insert on archived_dataset_file
  for each row
begin
  if :new.archived_dataset_file_id is null then
    select archived_dataset_file_id_seq.nextval
      into :new.archived_dataset_file_id
      from dual;
  end if;
end;
/

create or replace trigger audit_event_bef_trg
  before insert on audit_event
  for each row
begin
  if :new.audit_id is null then
    select audit_id_seq.nextval
      into :new.audit_id
      from dual;
  end if;
end;
/

create or replace trigger audit_log_event_bef_trg
  before insert on audit_log_event
  for each row
begin
  if :new.audit_id is null then
    select audit_log_event_audit_id_seq.nextval
      into :new.audit_id
      from dual;
  end if;
end;
/

create or replace trigger dataset_bef_trg
  before insert on dataset
  for each row
begin
  if :new.dataset_id is null then
    select dataset_id_seq.nextval
      into :new.dataset_id
      from dual;
  end if;
end;
/

create or replace trigger dc_computed_event_bef_trg
  before insert on dc_computed_event
  for each row
begin
  if :new.dc_summary_event_id is null then
    select dc_summary_event_id_seq.nextval
      into :new.dc_summary_event_id
      from dual;
  end if;
end;
/

create or replace trigger dc_event_bef_trg
  before insert on dc_event
  for each row
begin
  if :new.dc_event_id is null then
    select dc_event_id_seq.nextval
      into :new.dc_event_id
      from dual;
  end if;
end;
/

create or replace trigger dc_primitive_bef_trg
  before insert on dc_primitive
  for each row
begin
  if :new.dc_primitive_id is null then
    select dc_primitive_id_seq.nextval
      into :new.dc_primitive_id
      from dual;
  end if;
end;
/

create or replace trigger dc_section_event_bef_trg
  before insert on dc_section_event
  for each row
begin
  if :new.dc_event_id is null then
    select dc_section_event_id_seq.nextval
      into :new.dc_event_id
      from dual;
  end if;
end;
/

create or replace trigger dc_send_email_event_bef_trg
  before insert on dc_send_email_event
  for each row
begin
  if :new.dc_event_id is null then
    select dc_send_email_event_id_seq.nextval
      into :new.dc_event_id
      from dual;
  end if;
end;
/

create or replace trigger dc_substitution_event_bef_trg
  before insert on dc_substitution_event
  for each row
begin
  if :new.dc_event_id is null then
    select dc_substitution_event_id_seq.nextval
      into :new.dc_event_id
      from dual;
  end if;
end;
/

create or replace trigger decision_condition_bef_trg
  before insert on decision_condition
  for each row
begin
  if :new.decision_condition_id is null then
    select decision_condition_id_seq.nextval
      into :new.decision_condition_id
      from dual;
  end if;
end;
/

create or replace trigger discrepancy_note_bef_trg
  before insert on discrepancy_note
  for each row
begin
  if :new.discrepancy_note_id is null then
    select discrepancy_note_id_seq.nextval
      into :new.discrepancy_note_id
      from dual;
  end if;
end;
/

create or replace trigger discrepancy_note_type_bef_trg
  before insert on discrepancy_note_type
  for each row
begin
  if :new.discrepancy_note_type_id is null then
    select discrepancy_note_type_id_seq.nextval
      into :new.discrepancy_note_type_id
      from dual;
  end if;
end;
/

create or replace trigger event_crf_bef_trg
  before insert on event_crf
  for each row
begin
  if :new.event_crf_id is null then
    select event_crf_id_seq.nextval
      into :new.event_crf_id
      from dual;
  end if;
end;
/

create or replace trigger event_definition_crf_bef_trg
  before insert on event_definition_crf
  for each row
begin
  if :new.event_definition_crf_id is null then
    select event_definition_crf_id_seq.nextval
      into :new.event_definition_crf_id
      from dual;
  end if;
end;
/

create or replace trigger export_format_bef_trg
  before insert on export_format
  for each row
begin
  if :new.export_format_id is null then
    select export_format_id_seq.nextval
      into :new.export_format_id
      from dual;
  end if;
end;
/

create or replace trigger filter_bef_trg
  before insert on filter
  for each row
begin
  if :new.filter_id is null then
    select filter_id_seq.nextval
      into :new.filter_id
      from dual;
  end if;
end;
/

create or replace trigger group_class_types_bef_trg
  before insert on group_class_types
  for each row
begin
  if :new.group_class_type_id is null then
    select group_class_type_id_seq.nextval
      into :new.group_class_type_id
      from dual;
  end if;
end;
/

create or replace trigger item_data_bef_trg
  before insert on item_data
  for each row
begin
  if :new.item_data_id is null then
    select item_data_id_seq.nextval
      into :new.item_data_id
      from dual;
  end if;
end;
/

create or replace trigger item_data_type_bef_trg
  before insert on item_data_type
  for each row
begin
  if :new.item_data_type_id is null then
    select item_data_type_id_seq.nextval
      into :new.item_data_type_id
      from dual;
  end if;
end;
/

create or replace trigger item_form_metadata_bef_trg
  before insert on item_form_metadata
  for each row
begin
  if :new.item_form_metadata_id is null then
    select item_form_metadata_id_seq.nextval
      into :new.item_form_metadata_id
      from dual;
  end if;
end;
/

create or replace trigger item_group_bef_trg
  before insert on item_group
  for each row
begin
  if :new.item_group_id is null then
    select item_group_id_seq.nextval
      into :new.item_group_id
      from dual;
  end if;
end;
/

create or replace trigger item_group_metadata_bef_trg
  before insert on item_group_metadata
  for each row
begin
  if :new.item_group_metadata_id is null then
    select item_group_metadata_id_seq.nextval
      into :new.item_group_metadata_id
      from dual;
  end if;
end;
/

create or replace trigger item_bef_trg
  before insert on item
  for each row
begin
  if :new.item_id is null then
    select item_id_seq.nextval
      into :new.item_id
      from dual;
  end if;
end;
/

create or replace trigger item_reference_type_bef_trg
  before insert on item_reference_type
  for each row
begin
  if :new.item_reference_type_id is null then
    select item_reference_type_id_seq.nextval
      into :new.item_reference_type_id
      from dual;
  end if;
end;
/

create or replace trigger null_value_type_bef_trg
  before insert on null_value_type
  for each row
begin
  if :new.null_value_type_id is null then
    select null_value_type_id_seq.nextval
      into :new.null_value_type_id
      from dual;
  end if;
end;
/

create or replace trigger privilege_bef_trg
  before insert on privilege
  for each row
begin
  if :new.priv_id is null then
    select priv_id_seq.nextval
      into :new.priv_id
      from dual;
  end if;
end;
/

create or replace trigger resolution_status_bef_trg
  before insert on resolution_status
  for each row
begin
  if :new.resolution_status_id is null then
    select resolution_status_id_seq.nextval
      into :new.resolution_status_id
      from dual;
  end if;
end;
/

create or replace trigger response_set_bef_trg
  before insert on response_set
  for each row
begin
  if :new.response_set_id is null then
    select response_set_id_seq.nextval
      into :new.response_set_id
      from dual;
  end if;
end;
/

create or replace trigger response_type_bef_trg
  before insert on response_type
  for each row
begin
  if :new.response_type_id is null then
    select response_type_id_seq.nextval
      into :new.response_type_id
      from dual;
  end if;
end;
/

create or replace trigger section_bef_trg
  before insert on section
  for each row
begin
  if :new.section_id is null then
    select section_id_seq.nextval
      into :new.section_id
      from dual;
  end if;
end;
/

create or replace trigger status_bef_trg
  before insert on status
  for each row
begin
  if :new.status_id is null then
    select status_id_seq.nextval
      into :new.status_id
      from dual;
  end if;
end;
/

create or replace trigger study_event_definition_bef_trg
  before insert on study_event_definition
  for each row
begin
  if :new.study_event_definition_id is null then
    select study_event_definition_id_seq.nextval
      into :new.study_event_definition_id
      from dual;
  end if;
end;
/

create or replace trigger study_event_bef_trg
  before insert on study_event
  for each row
begin
  if :new.study_event_id is null then
    select study_event_id_seq.nextval
      into :new.study_event_id
      from dual;
  end if;
end;
/

create or replace trigger study_group_class_bef_trg
  before insert on study_group_class
  for each row
begin
  if :new.study_group_class_id is null then
    select study_group_class_id_seq.nextval
      into :new.study_group_class_id
      from dual;
  end if;
end;
/

create or replace trigger study_group_bef_trg
  before insert on study_group
  for each row
begin
  if :new.study_group_id is null then
    select study_group_id_seq.nextval
      into :new.study_group_id
      from dual;
  end if;
end;
/

create or replace trigger study_parameter_bef_trg
  before insert on study_parameter
  for each row
begin
  if :new.study_parameter_id is null then
    select study_parameter_id_seq.nextval
      into :new.study_parameter_id
      from dual;
  end if;
end;
/

create or replace trigger study_parameter_value_bef_trg
  before insert on study_parameter_value
  for each row
begin
  if :new.study_parameter_value_id is null then
    select study_parameter_value_id_seq.nextval
      into :new.study_parameter_value_id
      from dual;
  end if;
end;
/

create or replace trigger study_bef_trg
  before insert on study
  for each row
begin
  if :new.study_id is null then
    select study_id_seq.nextval
      into :new.study_id
      from dual;
  end if;
end;
/

create or replace trigger study_subject_bef_trg
  before insert on study_subject
  for each row
begin
  if :new.study_subject_id is null then
    select study_subject_id_seq.nextval
      into :new.study_subject_id
      from dual;
  end if;
end;
/

create or replace trigger study_type_bef_trg
  before insert on study_type
  for each row
begin
  if :new.study_type_id is null then
    select study_type_id_seq.nextval
      into :new.study_type_id
      from dual;
  end if;
end;
/

create or replace trigger subject_event_status_bef_trg
  before insert on subject_event_status
  for each row
begin
  if :new.subject_event_status_id is null then
    select subject_event_status_id_seq.nextval
      into :new.subject_event_status_id
      from dual;
  end if;
end;
/

create or replace trigger subject_group_map_bef_trg
  before insert on subject_group_map
  for each row
begin
  if :new.subject_group_map_id is null then
    select subject_group_map_id_seq.nextval
      into :new.subject_group_map_id
      from dual;
  end if;
end;
/

create or replace trigger subject_bef_trg
  before insert on subject
  for each row
begin
  if :new.subject_id is null then
    select subject_id_seq.nextval
      into :new.subject_id
      from dual;
  end if;
end;
/

create or replace trigger user_account_bef_trg
  before insert on user_account
  for each row
begin
  if :new.user_id is null then
    select user_id_seq.nextval
      into :new.user_id
      from dual;
  end if;
end;
/

create or replace trigger user_role_bef_trg
  before insert on user_role
  for each row
begin
  if :new.role_id is null then
    select role_id_seq.nextval
      into :new.role_id
      from dual;
  end if;
end;
/

create or replace trigger user_type_bef_trg
  before insert on user_type
  for each row
begin
  if :new.user_type_id is null then
    select user_type_id_seq.nextval
      into :new.user_type_id
      from dual;
  end if;
end;
/
