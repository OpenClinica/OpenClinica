/*--------------------------------------------------------------------------
*
* File       : oracle_package.sql
*
* Subject    : Package procedures that are called from triggers
*
* Parameters : none
*
* Conditions : tables and sequences should exist before creating the
*              package
*
* Author/Dt  : Shriram Mani  12/16/2007
*              Shriram Mani  01/28/2007 - Added nvl function to all
* 					  comparisons
*
* Comments   : Some of the queries are simplified. It can be further
*              simplified, but not necessary.
*
--------------------------------------------------------------------------*/
CREATE OR REPLACE package clinica_table_api is

procedure event_crf_trigger(
      tg_op varchar2
     ,newrec  in out event_crf%rowtype
     ,oldrec event_crf%rowtype default null
   );

procedure global_subject_trigger(
      tg_op varchar2
     ,newrec  in out subject%rowtype
     ,oldrec subject%rowtype default null
   );

procedure item_data_trigger(
      tg_op varchar2
     ,newrec  in out item_data%rowtype
     ,oldrec item_data%rowtype default null
   );

procedure study_subject_trigger(
      tg_op varchar2
     ,newrec  in out study_subject%rowtype
     ,oldrec study_subject%rowtype default null
   );


end;
/

CREATE OR REPLACE package body clinica_table_api is

procedure event_crf_trigger(
      tg_op varchar2
     ,newrec in out event_crf%rowtype
     ,oldrec event_crf%rowtype default null
   ) is
event_type_id    varchar2(10) := null;
entity_name_value varchar2(200);
BEGIN
    IF (TG_OP = 'UPDATE') THEN
        IF nvl(OLDREC.status_id, '-1') <> nvl(NEWREC.status_id, '-1') THEN
        ---------------
        --Event CRF status changed
        entity_name_value := 'Status';
        --
        IF (nvl(OLDREC.status_id, '-1') = '1' AND nvl(NEWREC.status_id, '-1') = '2') THEN
          event_type_id := '8';
        ELSIF (nvl(OLDREC.status_id, '-1') = '1' AND nvl(NEWREC.status_id, '-1') = '4') THEN
          event_type_id := '10';
        ELSIF (nvl(OLDREC.status_id, '-1') = '4' AND nvl(NEWREC.status_id, '-1') = '2') THEN
          event_type_id := '11';
        END IF;
        --
        if event_type_id is not null then
            INSERT INTO audit_log_event(
                audit_id,
                audit_log_event_type_id,
                audit_date,
                user_id,
                audit_table,
                entity_id,
                entity_name,
                old_value,
                new_value,
                event_crf_id
              ) VALUES (
                audit_log_event_audit_id_seq.nextval,
                event_type_id,
                sysdate,
                NEWREC.update_id,
                'event_crf',
                NEWREC.event_crf_id,
                entity_name_value,
                OLDREC.status_id,
                NEWREC.status_id,
                NEWREC.event_crf_id
              );
        END IF;
       END IF;

        IF (nvl(OLDREC.date_interviewed, '01-JAN-1000') <> nvl(NEWREC.date_interviewed, '01-JAN-1000')) THEN
          --Event CRF date interviewed
          entity_name_value := 'Date interviewed';
          event_type_id := '9';
          --
            INSERT INTO audit_log_event(
                audit_id,
                audit_log_event_type_id,
                audit_date,
                user_id,
                audit_table,
                entity_id,
                entity_name,
                old_value,
                new_value,
                event_crf_id
              ) VALUES (
                audit_log_event_audit_id_seq.nextval,
                event_type_id,
                sysdate,
                NEWREC.update_id,
                'event_crf',
                NEWREC.event_crf_id,
                entity_name_value,
                OLDREC.date_interviewed,
                NEWREC.date_interviewed,
                NEWREC.event_crf_id
              );
        END IF;

        IF((nvl(OLDREC.interviewer_name, ' ') <> nvl(NEWREC.interviewer_name, ' ')) AND (OLDREC.interviewer_name is not null)) THEN
          --Event CRF interviewer name
          entity_name_value := 'Interviewer Name';
          event_type_id := '9';
          --
            INSERT INTO audit_log_event(
                audit_id,
                audit_log_event_type_id,
                audit_date,
                user_id,
                audit_table,
                entity_id,
                entity_name,
                old_value,
                new_value,
                event_crf_id
              ) VALUES (
                audit_log_event_audit_id_seq.nextval,
                event_type_id,
                sysdate,
                NEWREC.update_id,
                'event_crf',
                NEWREC.event_crf_id,
                entity_name_value,
                OLDREC.interviewer_name,
                NEWREC.interviewer_name,
                NEWREC.event_crf_id
              );
        END IF;
    END IF;
END;
--------------------------------------------------------------------------------

procedure global_subject_trigger(
      tg_op varchar2
     ,newrec in out subject%rowtype
     ,oldrec subject%rowtype default null
   ) is
event_type_id    varchar2(10) := null;
entity_name_value varchar2(200);
BEGIN
    --
    IF (TG_OP = 'INSERT') THEN
        ---------------
        --Subject created
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id)
            VALUES (audit_log_event_audit_id_seq.nextval, '5', sysdate, NEWREC.owner_id, 'subject', NEWREC.subject_id);
        ---------------
    ELSIF (TG_OP = 'UPDATE') THEN
        IF nvl(OLDREC.status_id, '-1') <> nvl(NEWREC.status_id, '-1') THEN
        ---------------
        --Subject status changed
        entity_name_value  := 'Status';
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
            VALUES (audit_log_event_audit_id_seq.nextval, '6', sysdate, NEWREC.update_id, 'subject', NEWREC.subject_id, entity_name_value, OLDREC.status_id, NEWREC.status_id);
        ---------------
        END IF;

        IF(nvl(OLDREC.unique_identifier, '-1') <> nvl(NEWREC.unique_identifier, '-1')) THEN
        ---------------
        --Subject value changed
        entity_name_value  := 'Person ID';
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
            VALUES (audit_log_event_audit_id_seq.nextval, '7', sysdate, NEWREC.update_id, 'subject', NEWREC.subject_id, entity_name_value, OLDREC.unique_identifier, NEWREC.unique_identifier);
        ---------------
        END IF;

        IF(nvl(OLDREC.date_of_birth, '01-JAN-1000') <> nvl(NEWREC.date_of_birth, '01-JAN-1000')) THEN
        ---------------
        --Subject value changed
        entity_name_value  := 'Date of Birth';
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
            VALUES (audit_log_event_audit_id_seq.nextval, '7', sysdate, NEWREC.update_id, 'subject', NEWREC.subject_id, entity_name_value, OLDREC.date_of_birth, NEWREC.date_of_birth);
        ---------------
        END IF;
    END IF;
END;
--------------------------------------------------------------------------------

procedure item_data_trigger(
      tg_op varchar2
     ,newrec in out item_data%rowtype
     ,oldrec item_data%rowtype default null
   ) is
event_type_id    varchar2(10) := null;
entity_name_value varchar2(200);
BEGIN
    --
    IF (TG_OP = 'DELETE') THEN
        ---------------
        --Item data deleted (by deleting an event crf)
        begin
          SELECT item.name
            INTO entity_name_value
            FROM item
           WHERE item.item_id = OLDREC.item_id;
        exception
          when others then
            entity_name_value := null;
        end;
        --
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, event_crf_id)
            VALUES (audit_log_event_audit_id_seq.nextval , '13', sysdate, OLDREC.update_id, 'item_data', OLDREC.item_data_id, entity_name_value, OLDREC.value, OLDREC.event_crf_id);
    ELSIF (TG_OP = 'UPDATE') THEN
        IF nvl(OLDREC.status_id, '-1') <> nvl(NEWREC.status_id, '-1') THEN
        ---------------
        --Item data status changed (by removing an event crf)
        begin
          SELECT item.name
            INTO entity_name_value
            FROM item
           WHERE item.item_id = NEWREC.item_id;
        exception
          when others then
            entity_name_value := null;
        end;
        --
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value, event_crf_id)
            VALUES (audit_log_event_audit_id_seq.nextval , '12', sysdate, NEWREC.update_id, 'item_data', NEWREC.item_data_id, entity_name_value, OLDREC.status_id, NEWREC.status_id, NEWREC.event_crf_id);
        ---------------
        END IF;

        IF(nvl(OLDREC.value, ' ') <> nvl(NEWREC.value, ' ')) THEN
        ---------------
        --Item data updated
        begin
          SELECT item.name
            INTO entity_name_value
            FROM item
           WHERE item.item_id = NEWREC.item_id;
        exception
          when others then
            entity_name_value := null;
        end;
        --
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value, event_crf_id)
            VALUES (audit_log_event_audit_id_seq.nextval , '1', sysdate, NEWREC.update_id, 'item_data', NEWREC.item_data_id, entity_name_value, OLDREC.value, NEWREC.value, NEWREC.event_crf_id);
        ---------------
        END IF;
    END IF;
END;
--------------------------------------------------------------------------------

procedure study_subject_trigger(
      tg_op varchar2
     ,newrec in out study_subject%rowtype
     ,oldrec study_subject%rowtype default null
   ) is
event_type_id    varchar2(10) := null;
entity_name_value varchar2(200);
BEGIN
    --
    IF (TG_OP = 'INSERT') THEN
        ---------------
        --Study subject created
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id)
            VALUES (audit_log_event_audit_id_seq.nextval,'2', sysdate, NEWREC.owner_id, 'study_subject', NEWREC.study_subject_id);
        ---------------
    ELSIF (TG_OP = 'UPDATE') THEN
        IF nvl(OLDREC.status_id, '-1') <> nvl(NEWREC.status_id, '-1') THEN
        ---------------
        --Study subject status changed
        entity_name_value := 'Status';
        --
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
            VALUES (audit_log_event_audit_id_seq.nextval,'3', sysdate, NEWREC.update_id, 'study_subject', NEWREC.study_subject_id, entity_name_value, OLDREC.status_id, NEWREC.status_id);
        ---------------
        END IF;

        IF(nvl(OLDREC.label, ' ') <> nvl(NEWREC.label, ' ')) THEN
        ---------------
        --Study subject value changed
        entity_name_value := 'Study Subject ID';
        --
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
            VALUES (audit_log_event_audit_id_seq.nextval,'4', sysdate, NEWREC.update_id, 'study_subject', NEWREC.study_subject_id, entity_name_value, OLDREC.label, NEWREC.label);
        ---------------
        END IF;

        IF(nvl(OLDREC.secondary_label, ' ') <> nvl(NEWREC.secondary_label, ' ')) THEN
        ---------------
        --Study subject value changed
        entity_name_value := 'Secondary Subject ID';
        --
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
            VALUES (audit_log_event_audit_id_seq.nextval,'4', sysdate, NEWREC.update_id, 'study_subject', NEWREC.study_subject_id, entity_name_value, OLDREC.secondary_label, NEWREC.secondary_label);
        ---------------
        END IF;

        IF(nvl(OLDREC.enrollment_date, '01-JAN-1000') <> nvl(NEWREC.enrollment_date, '01-JAN-1000')) THEN
        ---------------
        --Study subject value changed
        entity_name_value := 'Enrollment Date';
        --
        INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
            VALUES (audit_log_event_audit_id_seq.nextval,'4', sysdate, NEWREC.update_id, 'study_subject', NEWREC.study_subject_id, entity_name_value, OLDREC.enrollment_date, NEWREC.enrollment_date);
        ---------------
        END IF;
    END IF;
END;
--------------------------------------------------------------------------------

end;
/