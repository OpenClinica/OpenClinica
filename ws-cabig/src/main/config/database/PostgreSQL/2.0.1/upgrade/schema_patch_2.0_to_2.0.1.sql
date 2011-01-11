/* Updates the schema for the OpenClinica application
   Can be applied to an existing OpenClinica 2.0 database to make it equivilant to the 2.0.1 schema
   -- Inserts new study parameter to control display of Person ID in CRF header
   -- Creates study paramaters for default study
   -- Drops the unneccessary cryptography functions
   -- Drops the old auditing triggers and trigger functions
   -- Sets up the new auditing tables and supporting entities
   Authors: jsampson, jxu
*/
SET client_encoding = 'SQL_ASCII';
SET check_function_bodies = false;
--Don't show warning messages during installation
SET client_min_messages = error;
SET search_path = public, pg_catalog;
SET default_tablespace = '';
SET default_with_oids = true;

--Insert new study parameter to control display of Person ID in CRF header
-----------------------------------------------
INSERT INTO study_parameter (study_parameter_id,handle,name,description,default_value,inheritable,overridable)
	VALUES (13,'personIdShownOnCRF','','','false',true,false);
-----------------------------------------------

--Add study parameters for the default study
-----------------------------------------------
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (1, 1, '1','collectDob');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (2, 1, 'true','discrepancyManagement');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (3, 1, 'true','genderRequired');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (4, 1, 'required','subjectPersonIdRequired');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (5, 1, 'true','interviewerNameRequired');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (6, 1, 'blank','interviewerNameDefault');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (7, 1, 'true','interviewerNameEditable');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (8, 1, 'true','interviewDateRequired');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (9, 1, 'blank','interviewDateDefault');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (10, 1, 'true','interviewDateEditable');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (11, 1, 'manual','subjectIdGeneration');
-- not implemented for now, so the value is an empty string
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (12, 1, '','subjectIdPrefixSuffix');
INSERT INTO study_parameter_value(study_parameter_value_id,study_id,value,parameter)
	VALUES (13, 1, 'true','personIdShownOnCRF');
-----------------------------------------------

-- Drop the unneccessary cryptography functions
-----------------------------------------------
DROP FUNCTION armor(bytea);
DROP FUNCTION cipher_exists(text);
DROP FUNCTION crypt(text, text);
DROP FUNCTION dearmor(text);
DROP FUNCTION decrypt(bytea, bytea, text);
DROP FUNCTION decrypt_iv(bytea, bytea, bytea, text);
DROP FUNCTION digest(bytea, text);
DROP FUNCTION digest(text, text);
DROP FUNCTION digest_exists(text);
DROP FUNCTION encrypt(bytea, bytea, text);
DROP FUNCTION encrypt_iv(bytea, bytea, bytea, text);
DROP FUNCTION gen_salt(text);
DROP FUNCTION gen_salt(text, int4);
DROP FUNCTION hmac(bytea, bytea, text);
DROP FUNCTION hmac(text, text, text);
DROP FUNCTION hmac_exists(text);
DROP FUNCTION pgp_key_id(bytea);
DROP FUNCTION pgp_pub_decrypt(bytea, bytea);
DROP FUNCTION pgp_pub_decrypt(bytea, bytea, text);
DROP FUNCTION pgp_pub_decrypt(bytea, bytea, text, text);
DROP FUNCTION pgp_pub_decrypt_bytea(bytea, bytea);
DROP FUNCTION pgp_pub_decrypt_bytea(bytea, bytea, text);
DROP FUNCTION pgp_pub_decrypt_bytea(bytea, bytea, text, text);
DROP FUNCTION pgp_pub_encrypt(text, bytea);
DROP FUNCTION pgp_pub_encrypt(text, bytea, text);
DROP FUNCTION pgp_pub_encrypt_bytea(bytea, bytea);
DROP FUNCTION pgp_pub_encrypt_bytea(bytea, bytea, text);
DROP FUNCTION pgp_sym_decrypt(bytea, text);
DROP FUNCTION pgp_sym_decrypt(bytea, text, text);
DROP FUNCTION pgp_sym_decrypt_bytea(bytea, text);
DROP FUNCTION pgp_sym_decrypt_bytea(bytea, text, text);
DROP FUNCTION pgp_sym_encrypt(text, text);
DROP FUNCTION pgp_sym_encrypt(text, text, text);
DROP FUNCTION pgp_sym_encrypt_bytea(bytea, text);
DROP FUNCTION pgp_sym_encrypt_bytea(bytea, text, text);
-----------------------------------------------

-- Drop the old auditing triggers and functions
-----------------------------------------------
DROP TRIGGER discrepancy_note_trigger ON discrepancy_note;
DROP FUNCTION discrepancy_note_trigger();
DROP TRIGGER event_crf_trigger ON event_crf;
DROP FUNCTION event_crf_trigger();
DROP TRIGGER item_data_trigger ON item_data;
DROP FUNCTION item_data_trigger();
DROP TRIGGER study_event_trigger ON study_event;
DROP FUNCTION study_event_trigger();
DROP TRIGGER study_subject_trigger ON study_subject;
DROP FUNCTION study_subject_trigger();
DROP TRIGGER study_user_role_trigger ON study_user_role;
DROP FUNCTION study_user_role_trigger();
DROP TRIGGER subject_group_map_trigger ON subject_group_map;
DROP FUNCTION subject_group_map_trigger();
DROP TRIGGER subject_trigger ON subject;
DROP FUNCTION subject_trigger();
DROP TRIGGER user_account_trigger ON user_account;
DROP FUNCTION user_account_trigger();
-----------------------------------------------

-- Setup the new auditing tables and supporting entities
-- NOTE: The old auditing tables are left in place for posterity, but will no longer accumulate auditing data
-----------------------------------------------
CREATE TABLE audit_log_event (
    audit_id serial NOT NULL,
    audit_date timestamp without time zone NOT NULL,
    audit_table character varying(500) NOT NULL,
    user_id numeric,
    entity_id numeric,
    entity_name character varying(500),
    reason_for_change character varying(1000),
    audit_log_event_type_id numeric,
    old_value character varying(2000),
    new_value character varying(2000),
    event_crf_id numeric
);
ALTER TABLE ONLY audit_log_event
    ADD CONSTRAINT audit_log_event_pkey PRIMARY KEY (audit_id);


ALTER TABLE audit_log_event OWNER TO clinica;

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('audit_log_event', 'audit_id'), 1, true);

CREATE TABLE audit_log_event_type (
    audit_log_event_type_id serial NOT NULL,
    name character varying(255)
);
ALTER TABLE ONLY audit_log_event_type
  ADD CONSTRAINT audit_log_event_type_pkey PRIMARY KEY (audit_log_event_type_id);


ALTER TABLE audit_log_event_type OWNER TO clinica;

-------------------
--Audit Event types
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (1, 'Item data value updated');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (2, 'Study subject created');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (3, 'Study subject status changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (4, 'Study subject value changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (5, 'Subject created');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (6, 'Subject status changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (7, 'Subject global value changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (8, 'Event CRF marked complete');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (9, 'Event CRF properties changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (10, 'Event CRF IDE completed (DDE)');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (11, 'Event CRF validated(DDE)');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (12, 'Item data status changed');
INSERT INTO audit_log_event_type (audit_log_event_type_id, name) VALUES (13, 'Item data deleted');

CREATE OR REPLACE FUNCTION item_data_trigger()
  RETURNS "trigger" AS
$BODY$DECLARE
	pk INTEGER;
	entity_name_value TEXT;
BEGIN
	IF (TG_OP = 'DELETE') THEN
		---------------
		--Item data deleted (by deleting an event crf)
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value item.name FROM item WHERE item.item_id = OLD.item_id;
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, event_crf_id)
			VALUES (pk, '13', now(), OLD.update_id, 'item_data', OLD.item_data_id, entity_name_value, OLD.value, OLD.event_crf_id);
		RETURN NULL; --return values ignored for 'after' triggers
	ELSIF (TG_OP = 'UPDATE') THEN
		IF(OLD.status_id <> NEW.status_id) THEN
		---------------
		--Item data status changed (by removing an event crf)
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value item.name FROM item WHERE item.item_id = NEW.item_id;
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value, event_crf_id)
			VALUES (pk, '12', now(), NEW.update_id, 'item_data', NEW.item_data_id, entity_name_value, OLD.status_id, NEW.status_id, NEW.event_crf_id);
		---------------
		END IF;

		IF(OLD.value <> NEW.value) THEN
		---------------
		--Item data updated
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value item.name FROM item WHERE item.item_id = NEW.item_id;
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value, event_crf_id)
			VALUES (pk, '1', now(), NEW.update_id, 'item_data', NEW.item_data_id, entity_name_value, OLD.value, NEW.value, NEW.event_crf_id);
		---------------
		END IF;
		RETURN NULL; --return values ignored for 'after' triggers
	END IF;
RETURN NULL; --return values ignored for 'after' triggers
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION item_data_trigger() OWNER TO clinica;

CREATE TRIGGER item_data_update
  AFTER UPDATE OR DELETE
  ON item_data
  FOR EACH ROW
  EXECUTE PROCEDURE item_data_trigger();

CREATE OR REPLACE FUNCTION study_subject_trigger()
  RETURNS "trigger" AS
$BODY$DECLARE
	pk INTEGER;
	entity_name_value TEXT;
BEGIN
	IF (TG_OP = 'INSERT') THEN
		---------------
		--Study subject created
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id)
			VALUES (pk, '2', now(), NEW.owner_id, 'study_subject', NEW.study_subject_id);
		RETURN NULL; --return values ignored for 'after' triggers
		---------------
	ELSIF (TG_OP = 'UPDATE') THEN
		IF(OLD.status_id <> NEW.status_id) THEN
		---------------
		--Study subject status changed
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Status';
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
			VALUES (pk, '3', now(), NEW.update_id, 'study_subject', NEW.study_subject_id, entity_name_value, OLD.status_id, NEW.status_id);
		---------------
		END IF;

		IF(OLD.label <> NEW.label) THEN
		---------------
		--Study subject value changed
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Study Subject ID';
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
			VALUES (pk, '4', now(), NEW.update_id, 'study_subject', NEW.study_subject_id, entity_name_value, OLD.label, NEW.label);
		---------------
		END IF;

		IF(OLD.secondary_label <> NEW.secondary_label) THEN
		---------------
		--Study subject value changed
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Secondary Subject ID';
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
			VALUES (pk, '4', now(), NEW.update_id, 'study_subject', NEW.study_subject_id, entity_name_value, OLD.secondary_label, NEW.secondary_label);
		---------------
		END IF;

		IF(OLD.enrollment_date <> NEW.enrollment_date) THEN
		---------------
		--Study subject value changed
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Enrollment Date';
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
			VALUES (pk, '4', now(), NEW.update_id, 'study_subject', NEW.study_subject_id, entity_name_value, OLD.enrollment_date, NEW.enrollment_date);
		---------------
		END IF;
	RETURN NULL; --return values ignored for 'after' triggers
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION study_subject_trigger() OWNER TO clinica;

CREATE TRIGGER study_subject_insert_updare
  AFTER INSERT OR UPDATE
  ON study_subject
  FOR EACH ROW
  EXECUTE PROCEDURE study_subject_trigger();
  
CREATE OR REPLACE FUNCTION global_subject_trigger()
  RETURNS "trigger" AS
$BODY$DECLARE
	pk INTEGER;
	entity_name_value TEXT;
BEGIN
	IF (TG_OP = 'INSERT') THEN
		---------------
		--Subject created
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id)
			VALUES (pk, '5', now(), NEW.owner_id, 'subject', NEW.subject_id);
		RETURN NULL; --return values ignored for 'after' triggers
		---------------
	ELSIF (TG_OP = 'UPDATE') THEN
		IF(OLD.status_id <> NEW.status_id) THEN
		---------------
		--Subject status changed
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Status';
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
			VALUES (pk, '6', now(), NEW.update_id, 'subject', NEW.subject_id, entity_name_value, OLD.status_id, NEW.status_id);
		---------------
		END IF;

		IF(OLD.unique_identifier <> NEW.unique_identifier) THEN
		---------------
		--Subject value changed
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Person ID';
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
			VALUES (pk, '7', now(), NEW.update_id, 'subject', NEW.subject_id, entity_name_value, OLD.unique_identifier, NEW.unique_identifier);
		---------------
		END IF;

		IF(OLD.date_of_birth <> NEW.date_of_birth) THEN
		---------------
		--Subject value changed
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Date of Birth';
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value)
			VALUES (pk, '7', now(), NEW.update_id, 'subject', NEW.subject_id, entity_name_value, OLD.date_of_birth, NEW.date_of_birth);
		---------------
		END IF;
	RETURN NULL; --return values ignored for 'after' triggers
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION global_subject_trigger() OWNER TO clinica;

CREATE TRIGGER global_subject_insert_update
  AFTER INSERT OR UPDATE
  ON subject
  FOR EACH ROW
  EXECUTE PROCEDURE global_subject_trigger();
  
CREATE OR REPLACE FUNCTION event_crf_trigger()
  RETURNS "trigger" AS
$BODY$DECLARE
	pk INTEGER;
	entity_name_value TEXT;
BEGIN
	IF (TG_OP = 'UPDATE') THEN
		IF(OLD.status_id <> NEW.status_id) THEN
		---------------
		--Event CRF status changed
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Status';
		IF(OLD.status_id = '1' AND NEW.status_id = '2') THEN
			INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value, event_crf_id)
				VALUES (pk, '8', now(), NEW.update_id, 'event_crf', NEW.event_crf_id, entity_name_value, OLD.status_id, NEW.status_id, NEW.event_crf_id);
		ELSIF (OLD.status_id = '1' AND NEW.status_id = '4') THEN
			INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value, event_crf_id)
				VALUES (pk, '10', now(), NEW.update_id, 'event_crf', NEW.event_crf_id, entity_name_value, OLD.status_id, NEW.status_id, NEW.event_crf_id);
		ELSIF (OLD.status_id = '4' AND NEW.status_id = '2') THEN
			INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value, event_crf_id)
				VALUES (pk, '11', now(), NEW.update_id, 'event_crf', NEW.event_crf_id, entity_name_value, OLD.status_id, NEW.status_id, NEW.event_crf_id);
		END IF;
		---------------
		END IF;

		IF(OLD.date_interviewed <> NEW.date_interviewed) THEN
		---------------
		--Event CRF date interviewed
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Date interviewed';
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value, event_crf_id)
			VALUES (pk, '9', now(), NEW.update_id, 'event_crf', NEW.event_crf_id, entity_name_value, OLD.date_interviewed, NEW.date_interviewed, NEW.event_crf_id);
		---------------
		END IF;

		IF((OLD.interviewer_name <> NEW.interviewer_name) AND (OLD.interviewer_name <> '')) THEN		---------------
		--Event CRF interviewer name
		SELECT INTO pk NEXTVAL('audit_log_event_audit_id_seq');
		SELECT INTO entity_name_value 'Interviewer Name';
		INSERT INTO audit_log_event(audit_id, audit_log_event_type_id, audit_date, user_id, audit_table, entity_id, entity_name, old_value, new_value, event_crf_id)
			VALUES (pk, '9', now(), NEW.update_id, 'event_crf', NEW.event_crf_id, entity_name_value, OLD.interviewer_name, NEW.interviewer_name, NEW.event_crf_id);
		---------------
		END IF;
	RETURN NULL; --return values ignored for 'after' triggers
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION event_crf_trigger() OWNER TO clinica;


CREATE TRIGGER event_crf_update
  AFTER UPDATE
  ON event_crf
  FOR EACH ROW
  EXECUTE PROCEDURE event_crf_trigger();
-----------------------------------------------
