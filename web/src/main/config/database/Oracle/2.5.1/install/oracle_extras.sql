/*--------------------------------------------------------------------------
*
* File       : oracle_extras.sql
*
* Subject    : Creates the sequence maintenance routines and the
*               data for that
*
* Parameters : None
*
* Conditions :
*
* Author/Dt  : Shriram Mani 05/15/2008
*
* Comments   : This script contains extra code to maintain the sequences
*               upto date based on the current max value in every table.
*              It is not necessary to have this table. Whenever a new
*               table or sequence is added, this table should be updated.
*              The plsql block at the bottom can be executed without the
*               scripts on the top.
*
--------------------------------------------------------------------------*/

prompt Creating table "Table_Sequences"
create table table_sequences
(
  sequence_name  varchar2(30) not null,
  table_name     varchar2(30) not null,
  column_name    varchar2(30) not null
);

prompt Populating table "Table_Sequences" with seeded data

insert into table_sequences ( sequence_name, table_name, column_name )
values ('AUDIT_EVENT_VAL_AUDIT_ID_SEQ', 'AUDIT_EVENT_VALUES', 'AUDIT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('ARCHIVED_DATASET_FILE_ID_SEQ', 'ARCHIVED_DATASET_FILE', 'ARCHIVED_DATASET_FILE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('AUDIT_ID_SEQ', 'AUDIT_EVENT', 'AUDIT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('AUDIT_LOG_EVENT_AUDIT_ID_SEQ', 'AUDIT_LOG_EVENT', 'AUDIT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('AUDIT_LOG_EVENT_TYPE_ID_SEQ', 'AUDIT_LOG_EVENT_TYPE', 'AUDIT_LOG_EVENT_TYPE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('CRF_VERSION_ID_SEQ', 'CRF_VERSION', 'CRF_VERSION_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('CRF_ID_SEQ', 'CRF', 'CRF_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('COMPLETION_STATUS_ID_SEQ', 'COMPLETION_STATUS', 'COMPLETION_STATUS_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DATASET_ID_SEQ', 'DATASET', 'DATASET_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DC_SUMMARY_EVENT_ID_SEQ', 'DC_COMPUTED_EVENT', 'DC_SUMMARY_EVENT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DC_EVENT_ID_SEQ', 'DC_EVENT', 'DC_EVENT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DC_PRIMITIVE_ID_SEQ', 'DC_PRIMITIVE', 'DC_PRIMITIVE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DC_SECTION_EVENT_ID_SEQ', 'DC_SECTION_EVENT', 'DC_EVENT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DC_SEND_EMAIL_EVENT_ID_SEQ', 'DC_SEND_EMAIL_EVENT', 'DC_EVENT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DC_SUBSTITUTION_EVENT_ID_SEQ', 'DC_SUBSTITUTION_EVENT', 'DC_EVENT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DECISION_CONDITION_ID_SEQ', 'DECISION_CONDITION', 'DECISION_CONDITION_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DISCREPANCY_NOTE_ID_SEQ', 'DISCREPANCY_NOTE', 'DISCREPANCY_NOTE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('DISCREPANCY_NOTE_TYPE_ID_SEQ', 'DISCREPANCY_NOTE_TYPE', 'DISCREPANCY_NOTE_TYPE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('EVENT_CRF_ID_SEQ', 'EVENT_CRF', 'EVENT_CRF_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('EVENT_DEFINITION_CRF_ID_SEQ', 'EVENT_DEFINITION_CRF', 'EVENT_DEFINITION_CRF_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('EXPORT_FORMAT_ID_SEQ', 'EXPORT_FORMAT', 'EXPORT_FORMAT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('FILTER_ID_SEQ', 'FILTER', 'FILTER_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('GROUP_CLASS_TYPE_ID_SEQ', 'GROUP_CLASS_TYPES', 'GROUP_CLASS_TYPE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('ITEM_ID_SEQ', 'ITEM', 'ITEM_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('ITEM_DATA_ID_SEQ', 'ITEM_DATA', 'ITEM_DATA_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('ITEM_DATA_TYPE_ID_SEQ', 'ITEM_DATA_TYPE', 'ITEM_DATA_TYPE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('ITEM_FORM_METADATA_ID_SEQ', 'ITEM_FORM_METADATA', 'ITEM_FORM_METADATA_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('ITEM_GROUP_ID_SEQ', 'ITEM_GROUP', 'ITEM_GROUP_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('ITEM_GROUP_METADATA_ID_SEQ', 'ITEM_GROUP_METADATA', 'ITEM_GROUP_METADATA_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('ITEM_REFERENCE_TYPE_ID_SEQ', 'ITEM_REFERENCE_TYPE', 'ITEM_REFERENCE_TYPE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('NULL_VALUE_TYPE_ID_SEQ', 'NULL_VALUE_TYPE', 'NULL_VALUE_TYPE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('PRIV_ID_SEQ', 'PRIVILEGE', 'PRIV_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RESOLUTION_STATUS_ID_SEQ', 'RESOLUTION_STATUS', 'RESOLUTION_STATUS_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RESPONSE_SET_ID_SEQ', 'RESPONSE_SET', 'RESPONSE_SET_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RESPONSE_TYPE_ID_SEQ', 'RESPONSE_TYPE', 'RESPONSE_TYPE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('ROLE_ID_SEQ', 'USER_ROLE', 'ROLE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('SECTION_ID_SEQ', 'SECTION', 'SECTION_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STATUS_ID_SEQ', 'STATUS', 'STATUS_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STUDY_ID_SEQ', 'STUDY', 'STUDY_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STUDY_EVENT_ID_SEQ', 'STUDY_EVENT', 'STUDY_EVENT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STUDY_EVENT_DEFINITION_ID_SEQ', 'STUDY_EVENT_DEFINITION', 'STUDY_EVENT_DEFINITION_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STUDY_GROUP_ID_SEQ', 'STUDY_GROUP', 'STUDY_GROUP_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STUDY_GROUP_CLASS_ID_SEQ', 'STUDY_GROUP_CLASS', 'STUDY_GROUP_CLASS_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STUDY_PARAMETER_ID_SEQ', 'STUDY_PARAMETER', 'STUDY_PARAMETER_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STUDY_PARAMETER_VALUE_ID_SEQ', 'STUDY_PARAMETER_VALUE', 'STUDY_PARAMETER_VALUE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STUDY_SUBJECT_ID_SEQ', 'STUDY_SUBJECT', 'STUDY_SUBJECT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('STUDY_TYPE_ID_SEQ', 'STUDY_TYPE', 'STUDY_TYPE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('SUBJECT_ID_SEQ', 'SUBJECT', 'SUBJECT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('SUBJECT_EVENT_STATUS_ID_SEQ', 'SUBJECT_EVENT_STATUS', 'SUBJECT_EVENT_STATUS_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('SUBJECT_GROUP_MAP_ID_SEQ', 'SUBJECT_GROUP_MAP', 'SUBJECT_GROUP_MAP_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('USER_ID_SEQ', 'USER_ACCOUNT', 'USER_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('USER_TYPE_ID_SEQ', 'USER_TYPE', 'USER_TYPE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RULE_ID_SEQ', 'RULE', 'RULE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RULE_SET_ID_SEQ', 'RULE_SET', 'RULE_SET_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RULE_SET_AUDIT_ID_SEQ', 'RULE_SET_AUDIT', 'RULE_SET_AUDIT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RULE_SET_RULE_ID_SEQ', 'RULE_SET_RULE', 'RULE_SET_RULE_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RULE_SET_RULE_AUDIT_ID_SEQ', 'RULE_SET_RULE_AUDIT', 'RULE_SET_RULE_AUDIT_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RULE_ACTION_ID_SEQ', 'RULE_ACTION', 'RULE_ACTION_ID');
insert into table_sequences ( sequence_name, table_name, column_name )
values ('RULE_EXPRESSION_ID_SEQ', 'RULE_EXPRESSION', 'RULE_EXPRESSION_ID');


COMMIT;

--
-- Function to reset the sequences to the most recent value
--
--
prompt Reseting all the sequences based on the maximum value in each table
--
declare
lsql varchar2(2000);
lidval integer;
lcurval integer;
lseqname varchar2(30);
--
  cursor lcur is
  select table_name,
         column_name,
         sequence_name
    from table_sequences ts
   where exists (select 'x'
                   from user_tables ut
                  where ut.table_name = ts.table_name
                );
  --
  cursor seqcur is
  select us.last_number
    from user_sequences us
   where sequence_name = lseqname;
begin
  for r in lcur
  loop
    --
    lseqname := r.sequence_name;
    --
    lsql := 'select max( ' || r.column_name || ' ) from ' || r.table_name;
    --
    execute immediate lsql into lidval;
    --
    open seqcur;
    fetch seqcur into lcurval;
    close seqcur;
    --
    if lcurval is null then
      dbms_output.put_line('Sequence ' || lseqname || ' does not exist');
    else
      --
      if lcurval < nvl(lidval, 0) then
        --
        for i in lcurval..lidval
        loop
          lsql := 'select ' || r.sequence_name || '.nextval from dual';
          execute immediate lsql into lcurval;
        end loop;
        --
      end if;
      --
    end if;
    --
    dbms_output.put_line(lsql);
    --
    dbms_output.put_line(lidval);
  end loop;
end;
/

