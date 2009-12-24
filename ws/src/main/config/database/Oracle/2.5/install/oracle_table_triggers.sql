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
* Author/Dt  : Shriram Mani 05/15/2008
*
* Comments   : None
*
--------------------------------------------------------------------------*/

--
prompt Create trigger EVENT_CRF_UPDATE on table EVENT_CRF
--

CREATE OR REPLACE TRIGGER event_crf_update
    AFTER UPDATE ON event_crf
    FOR EACH ROW
declare
trg_operation varchar2(10);
newrec  event_crf%rowtype;
oldrec  event_crf%rowtype;
begin
--
newrec.EVENT_CRF_ID                := :new.EVENT_CRF_ID;
newrec.STUDY_EVENT_ID              := :new.STUDY_EVENT_ID;
newrec.CRF_VERSION_ID              := :new.CRF_VERSION_ID;
newrec.DATE_INTERVIEWED            := :new.DATE_INTERVIEWED;
newrec.INTERVIEWER_NAME            := :new.INTERVIEWER_NAME;
newrec.COMPLETION_STATUS_ID        := :new.COMPLETION_STATUS_ID;
newrec.STATUS_ID                   := :new.STATUS_ID;
newrec.ANNOTATIONS                 := :new.ANNOTATIONS;
newrec.DATE_COMPLETED              := :new.DATE_COMPLETED;
newrec.VALIDATOR_ID                := :new.VALIDATOR_ID;
newrec.DATE_VALIDATE               := :new.DATE_VALIDATE;
newrec.DATE_VALIDATE_COMPLETED     := :new.DATE_VALIDATE_COMPLETED;
newrec.VALIDATOR_ANNOTATIONS       := :new.VALIDATOR_ANNOTATIONS;
newrec.VALIDATE_STRING             := :new.VALIDATE_STRING;
newrec.OWNER_ID                    := :new.OWNER_ID;
newrec.DATE_CREATED                := :new.DATE_CREATED;
newrec.STUDY_SUBJECT_ID            := :new.STUDY_SUBJECT_ID;
newrec.DATE_UPDATED                := :new.DATE_UPDATED;
newrec.UPDATE_ID                   := :new.UPDATE_ID;
newrec.ELECTRONIC_SIGNATURE_STATUS := :new.ELECTRONIC_SIGNATURE_STATUS;
--
oldrec.EVENT_CRF_ID                := :old.EVENT_CRF_ID;
oldrec.STUDY_EVENT_ID              := :old.STUDY_EVENT_ID;
oldrec.CRF_VERSION_ID              := :old.CRF_VERSION_ID;
oldrec.DATE_INTERVIEWED            := :old.DATE_INTERVIEWED;
oldrec.INTERVIEWER_NAME            := :old.INTERVIEWER_NAME;
oldrec.COMPLETION_STATUS_ID        := :old.COMPLETION_STATUS_ID;
oldrec.STATUS_ID                   := :old.STATUS_ID;
oldrec.ANNOTATIONS                 := :old.ANNOTATIONS;
oldrec.DATE_COMPLETED              := :old.DATE_COMPLETED;
oldrec.VALIDATOR_ID                := :old.VALIDATOR_ID;
oldrec.DATE_VALIDATE               := :old.DATE_VALIDATE;
oldrec.DATE_VALIDATE_COMPLETED     := :old.DATE_VALIDATE_COMPLETED;
oldrec.VALIDATOR_ANNOTATIONS       := :old.VALIDATOR_ANNOTATIONS;
oldrec.VALIDATE_STRING             := :old.VALIDATE_STRING;
oldrec.OWNER_ID                    := :old.OWNER_ID;
oldrec.DATE_CREATED                := :old.DATE_CREATED;
oldrec.STUDY_SUBJECT_ID            := :old.STUDY_SUBJECT_ID;
oldrec.DATE_UPDATED                := :old.DATE_UPDATED;
oldrec.UPDATE_ID                   := :old.UPDATE_ID;
oldrec.ELECTRONIC_SIGNATURE_STATUS := :old.ELECTRONIC_SIGNATURE_STATUS;
--
  --
  if inserting then
    trg_operation := 'INSERT';
  elsif updating then
    trg_operation := 'UPDATE';
  elsif deleting then
    trg_operation := 'DELETE';
  end if;
  --
   clinica_table_api.event_crf_trigger(trg_operation, newrec, oldrec);
end;
/

--
prompt Create trigger GLOBAL_SUBJECT_INSERT_UPDATE on table SUBJECT
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

  --
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
prompt Create trigger ITEM_DATA_UPDATE on table ITEM_DATA
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
prompt Create trigger STUDY_EVENT_INSERT_UPDATE on table STUDY_EVENT
--

CREATE OR REPLACE TRIGGER study_event_insert_update
    AFTER INSERT OR UPDATE ON study_event
    FOR EACH ROW
declare
  trg_operation  varchar2(10);
  newrec         study_event%rowtype;
  oldrec         study_event%rowtype;
begin
  --
  
  newrec.study_event_id             := :new.study_event_id;
  newrec.study_event_definition_id  := :new.study_event_definition_id;
  newrec.study_subject_id           := :new.study_subject_id;
  newrec.sample_ordinal             := :new.sample_ordinal;
  newrec.date_start                 := :new.date_start;
  newrec.date_end                   := :new.date_end;
  newrec.owner_id                   := :new.owner_id;
  newrec.status_id                  := :new.status_id;
  newrec.date_created               := :new.date_created;
  newrec.date_updated               := :new.date_updated;
  newrec.update_id                  := :new.update_id;
  newrec.subject_event_status_id    := :new.subject_event_status_id;
  newrec.start_time_flag            := :new.start_time_flag;
  newrec.end_time_flag              := :new.end_time_flag;
  newrec."location"                 := :new."location";
  --
  oldrec.study_event_id             := :old.study_event_id;
  oldrec.study_event_definition_id  := :old.study_event_definition_id;
  oldrec.study_subject_id           := :old.study_subject_id;
  oldrec.sample_ordinal             := :old.sample_ordinal;
  oldrec.date_start                 := :old.date_start;
  oldrec.date_end                   := :old.date_end;
  oldrec.owner_id                   := :old.owner_id;
  oldrec.status_id                  := :old.status_id;
  oldrec.date_created               := :old.date_created;
  oldrec.date_updated               := :old.date_updated;
  oldrec.update_id                  := :old.update_id;
  oldrec.subject_event_status_id    := :old.subject_event_status_id;
  oldrec.start_time_flag            := :old.start_time_flag;
  oldrec.end_time_flag              := :old.end_time_flag;
  oldrec."location"                 := :old."location";
  --
  if inserting then
    trg_operation := 'INSERT';
  elsif updating then
    trg_operation := 'UPDATE';
  elsif deleting then
    trg_operation := 'DELETE';
  end if;
  --
  clinica_table_api.study_event_trigger(trg_operation, newrec, oldrec);
end;
/


--
prompt Create trigger STUDY_SUBJECT_INSERT_UPDATE on table STUDY_SUBJECT
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
  NEWREC.UPDATE_ID         := :new.UPDATE_ID;
  NEWREC.OWNER_ID          := :new.OWNER_ID;
  --
  oldrec.STUDY_SUBJECT_ID  := :old.STUDY_SUBJECT_ID;
  oldrec.LABEL             := :old.LABEL;
  oldrec.SECONDARY_LABEL   := :old.SECONDARY_LABEL;
  oldrec.SUBJECT_ID        := :old.SUBJECT_ID;
  oldrec.STUDY_ID          := :old.STUDY_ID;
  oldrec.STATUS_ID         := :old.STATUS_ID;
  oldrec.ENROLLMENT_DATE   := :old.ENROLLMENT_DATE;
  oldrec.DATE_CREATED      := :old.DATE_CREATED;
  --
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

--
prompt Create trigger SUBJECT_GRP_MAP_INSERT_UPDATE on table SUBJECT_GROUP_MAP
--

CREATE OR REPLACE TRIGGER subject_grp_map_insert_update
    AFTER INSERT OR UPDATE ON subject_group_map
    FOR EACH ROW
declare
  trg_operation  varchar2(10);
  newrec         subject_group_map%rowtype;
  oldrec         subject_group_map%rowtype;
begin
  --
  newrec.SUBJECT_GROUP_MAP_ID := :new.SUBJECT_GROUP_MAP_ID;
  newrec.STUDY_GROUP_CLASS_ID := :new.STUDY_GROUP_CLASS_ID;
  newrec.STUDY_SUBJECT_ID     := :new.STUDY_SUBJECT_ID;
  newrec.STUDY_GROUP_ID       := :new.STUDY_GROUP_ID;
  newrec.STATUS_ID            := :new.STATUS_ID;
  newrec.OWNER_ID             := :new.OWNER_ID;
  newrec.DATE_CREATED         := :new.DATE_CREATED;
  newrec.DATE_UPDATED         := :new.DATE_UPDATED;
  newrec.UPDATE_ID            := :new.UPDATE_ID;
  newrec.NOTES                := :new.NOTES;
  --
  oldrec.SUBJECT_GROUP_MAP_ID := :old.SUBJECT_GROUP_MAP_ID;
  oldrec.STUDY_GROUP_CLASS_ID := :old.STUDY_GROUP_CLASS_ID;
  oldrec.STUDY_SUBJECT_ID     := :old.STUDY_SUBJECT_ID;
  oldrec.STUDY_GROUP_ID       := :old.STUDY_GROUP_ID;
  oldrec.STATUS_ID            := :old.STATUS_ID;
  oldrec.OWNER_ID             := :old.OWNER_ID;
  oldrec.DATE_CREATED         := :old.DATE_CREATED;
  oldrec.DATE_UPDATED         := :old.DATE_UPDATED;
  oldrec.UPDATE_ID            := :old.UPDATE_ID;
  oldrec.NOTES                := :old.NOTES;
  --
  if inserting then
    trg_operation := 'INSERT';
  elsif updating then
    trg_operation := 'UPDATE';
  elsif deleting then
    trg_operation := 'DELETE';
  end if;
  --
  clinica_table_api.subject_grp_assign_trigger(trg_operation, newrec, oldrec);
end;
/

--
prompt Create trigger REPEATING_DATA_INSERT on table ITEM_DATA
--

CREATE OR REPLACE TRIGGER repeating_data_insert
    AFTER INSERT ON item_data
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
  --
  if inserting then
    trg_operation := 'INSERT';
  elsif updating then
    trg_operation := 'UPDATE';
  elsif deleting then
    trg_operation := 'DELETE';
  end if;
  --
  clinica_table_api.repeating_item_data_trigger(trg_operation, newrec, oldrec);
  --
end;
/

--
prompt Creating triggers to support default value assignment for PKs
prompt
--

--
prompt Create trigger ARCHIVED_DATASET_FILE_BEF_TRG on table ARCHIVED_DATASET_FILE
--

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

--
prompt Create trigger AUDIT_EVENT_BEF_TRG on table AUDIT_EVENT
--

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

--
prompt Create trigger AUDIT_LOG_EVENT_BEF_TRG on table AUDIT_LOG_EVENT
--

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

--
prompt Create trigger CRF_BEF_TRG on table DATASET
--

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
--
prompt Create trigger CRF_VERSION_BEF_TRG on table DATASET
--

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

--
prompt Create trigger DATASET_BEF_TRG on table DATASET
--

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

--
prompt Create trigger DC_COMPUTED_EVENT_BEF_TRG on table DC_COMPUTED_EVENT
--

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

--
prompt Create trigger DC_EVENT_BEF_TRG on table DC_EVENT
--

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

--
prompt Create trigger DC_PRIMITIVE_BEF_TRG on table DC_PRIMITIVE
--

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

--
prompt Create trigger DC_SECTION_EVENT_BEF_TRG on table DC_SECTION_EVENT
--

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

--
prompt Create trigger DC_SEND_EMAIL_EVENT_BEF_TRG on table DC_SEND_EMAIL_EVENT
--

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

--
prompt Create trigger DC_SUBSTITUTION_EVENT_BEF_TRG on table DC_SUBSTITUTION_EVENT
--

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

--
prompt Create trigger DECISION_CONDITION_BEF_TRG on table DECISION_CONDITION
--

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

--
prompt Create trigger DISCREPANCY_NOTE_BEF_TRG on table DISCREPANCY_NOTE
--

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

--
prompt Create trigger DISCREPANCY_NOTE_TYPE_BEF_TRG on table DISCREPANCY_NOTE_TYPE
--

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

--
prompt Create trigger EVENT_CRF_BEF_TRG on table EVENT_CRF
--

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

--
prompt Create trigger EVENT_DEFINITION_CRF_BEF_TRG on table EVENT_DEFINITION_CRF
--

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

--
prompt Create trigger EXPORT_FORMAT_BEF_TRG on table EXPORT_FORMAT
--

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

--
prompt Create trigger FILTER_BEF_TRG on table FILTER
--

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

--
prompt Create trigger GROUP_CLASS_TYPES_BEF_TRG on table GROUP_CLASS_TYPES
--

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

--
prompt Create trigger ITEM_DATA_BEF_TRG on table ITEM_DATA
--

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

--
prompt Create trigger ITEM_DATA_TYPE_BEF_TRG on table ITEM_DATA_TYPE
--

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

--
prompt Create trigger ITEM_FORM_METADATA_BEF_TRG on table ITEM_FORM_METADATA
--

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

--
prompt Create trigger ITEM_GROUP_BEF_TRG on table ITEM_GROUP
--

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

--
prompt Create trigger ITEM_GROUP_METADATA_BEF_TRG on table ITEM_GROUP_METADATA
--

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

--
prompt Create trigger ITEM_BEF_TRG on table ITEM
--

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

--
prompt Create trigger ITEM_REFERENCE_TYPE_BEF_TRG on table ITEM_REFERENCE_TYPE
--

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

--
prompt Create trigger NULL_VALUE_TYPE_BEF_TRG on table NULL_VALUE_TYPE
--

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

--
prompt Create trigger PRIVILEGE_BEF_TRG on table PRIVILEGE
--

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

--
prompt Create trigger RESOLUTION_STATUS_BEF_TRG on table RESOLUTION_STATUS
--

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

--
prompt Create trigger RESPONSE_SET_BEF_TRG on table RESPONSE_SET
--

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

--
prompt Create trigger RESPONSE_TYPE_BEF_TRG on table RESPONSE_TYPE
--

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

--
prompt Create trigger SECTION_BEF_TRG on table SECTION
--

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

--
prompt Create trigger STATUS_BEF_TRG on table STATUS
--

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

--
prompt Create trigger STUDY_EVENT_DEFINITION_BEF_TRG on table STUDY_EVENT_DEFINITION
--

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

--
prompt Create trigger STUDY_EVENT_BEF_TRG on table STUDY_EVENT
--

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

--
prompt Create trigger STUDY_GROUP_CLASS_BEF_TRG on table STUDY_GROUP_CLASS
--

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

--
prompt Create trigger STUDY_GROUP_BEF_TRG on table STUDY_GROUP
--

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

--
prompt Create trigger STUDY_PARAMETER_BEF_TRG on table STUDY_PARAMETER
--

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

--
prompt Create trigger STUDY_PARAMETER_VALUE_BEF_TRG on table STUDY_PARAMETER_VALUE
--

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

--
prompt Create trigger STUDY_BEF_TRG on table STUDY
--

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

--
prompt Create trigger STUDY_SUBJECT_BEF_TRG on table STUDY_SUBJECT
--

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

--
prompt Create trigger STUDY_TYPE_BEF_TRG on table STUDY_TYPE
--

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

--
prompt Create trigger SUBJECT_EVENT_STATUS_BEF_TRG on table SUBJECT_EVENT_STATUS
--

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

--
prompt Create trigger SUBJECT_GROUP_MAP_BEF_TRG on table SUBJECT_GROUP_MAP
--

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

--
prompt Create trigger SUBJECT_BEF_TRG on table SUBJECT
--

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

--
prompt Create trigger USER_ACCOUNT_BEF_TRG on table USER_ACCOUNT
--

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

--
prompt Create trigger USER_ROLE_BEF_TRG on table USER_ROLE
--

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

--
prompt Create trigger USER_TYPE_BEF_TRG on table USER_TYPE
--

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




--
prompt Create trigger RULE_BEF_TRG on table RULE
--

create or replace trigger rule_bef_trg
  before insert on RULE
  for each row
begin
  if :new.rule_id is null then
    select rule_id_seq.nextval
      into :new.rule_id
      from dual;
  end if;
end;
/

--
prompt Create trigger RULE_SET_BEF_TRG on table RULE_SET
--

create or replace trigger rule_set_bef_trg
  before insert on RULE_SET
  for each row
begin
  if :new.rule_set_id is null then
    select rule_set_id_seq.nextval
      into :new.rule_set_id
      from dual;
  end if;
end;
/

--
prompt Create trigger RULE_SET_AUDIT_BEF_TRG on table RULE_SET_AUDIT
--

create or replace trigger rule_set_audit_bef_trg
  before insert on RULE_SET_AUDIT
  for each row
begin
  if :new.rule_set_audit_id is null then
    select rule_set_audit_id_seq.nextval
      into :new.rule_set_audit_id
      from dual;
  end if;
end;
/

--
prompt Create trigger RULE_SET_RULE_BEF_TRG on table RULE_SET_RULE
--

create or replace trigger rule_set_rule_bef_trg
  before insert on RULE_SET_RULE
  for each row
begin
  if :new.rule_set_rule_id is null then
    select rule_set_rule_id_seq.nextval
      into :new.rule_set_rule_id
      from dual;
  end if;
end;
/

--
prompt Create trigger RULE_SET_RULE_AUDIT_BEF_TRG on table RULE_SET_RULE_AUDIT
--

create or replace trigger rule_set_rule_audit_bef_trg
  before insert on RULE_SET_RULE_AUDIT
  for each row
begin
  if :new.rule_set_rule_audit_id is null then
    select rule_set_rule_audit_id_seq.nextval
      into :new.rule_set_rule_audit_id
      from dual;
  end if;
end;
/

--
prompt Create trigger RULE_ACTION_BEF_TRG on table RULE_ACTION
--

create or replace trigger rule_action_bef_trg
  before insert on RULE_ACTION
  for each row
begin
  if :new.rule_action_id is null then
    select rule_action_id_seq.nextval
      into :new.rule_action_id
      from dual;
  end if;
end;
/

--
prompt Create trigger RULE_EXPRESSION_BEF_TRG on table RULE_EXPRESSION
--

create or replace trigger rule_expression_bef_trg
  before insert on RULE_EXPRESSION
  for each row
begin
  if :new.rule_expression_id is null then
    select rule_expression_id_seq.nextval
      into :new.rule_expression_id
      from dual;
  end if;
end;
/
