--
-- PostgreSQL database dump
--

SET client_encoding = 'UNICODE';
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

ALTER TABLE ONLY public.event_definition_crf DROP CONSTRAINT fk_versioning_study_inst;
ALTER TABLE ONLY public.crf_version DROP CONSTRAINT fk_versioni_reference_instrume;
ALTER TABLE ONLY public.versioning_map DROP CONSTRAINT fk_versioni_fk_versio_item;
ALTER TABLE ONLY public.versioning_map DROP CONSTRAINT fk_versioni_fk_versio_crf_vers;
ALTER TABLE ONLY public.user_account DROP CONSTRAINT fk_user_acc_status_re_status;
ALTER TABLE ONLY public.user_account DROP CONSTRAINT fk_user_acc_ref_user__user_typ;
ALTER TABLE ONLY public.user_account DROP CONSTRAINT fk_user_acc_fk_user_f_user_acc;
ALTER TABLE ONLY public.event_crf DROP CONSTRAINT fk_subject_reference_instrument;
ALTER TABLE ONLY public.subject DROP CONSTRAINT fk_subject_fk_subjec_user_acc;
ALTER TABLE ONLY public.subject DROP CONSTRAINT fk_subject_fk_subjec_status;
ALTER TABLE ONLY public.subject_group_map DROP CONSTRAINT fk_subject__subject_g_study_su;
ALTER TABLE ONLY public.subject_group_map DROP CONSTRAINT fk_subject__fk_subjec_study_gr;
ALTER TABLE ONLY public.subject_group_map DROP CONSTRAINT fk_subject__fk_subjec_status;
ALTER TABLE ONLY public.subject_group_map DROP CONSTRAINT fk_subject__fk_subjec_group_ro;
ALTER TABLE ONLY public.subject_group_map DROP CONSTRAINT fk_subject__fk_sub_gr_user_acc;
ALTER TABLE ONLY public.study_user_role DROP CONSTRAINT fk_study_us_study_use_user_acc;
ALTER TABLE ONLY public.study_user_role DROP CONSTRAINT fk_study_us_fk_study__status;
ALTER TABLE ONLY public.study DROP CONSTRAINT fk_study_type;
ALTER TABLE ONLY public.study_subject DROP CONSTRAINT fk_study_su_fk_study__user_acc;
ALTER TABLE ONLY public.study_subject DROP CONSTRAINT fk_study_su_fk_study__status;
ALTER TABLE ONLY public.study_subject DROP CONSTRAINT fk_study_reference_subject;
ALTER TABLE ONLY public.event_definition_crf DROP CONSTRAINT fk_study_reference_instrument;
ALTER TABLE ONLY public.event_definition_crf DROP CONSTRAINT fk_study_inst_reference;
ALTER TABLE ONLY public.study_group_class DROP CONSTRAINT fk_study_gr_fk_study__user_acc;
ALTER TABLE ONLY public.study_group_class DROP CONSTRAINT fk_study_gr_fk_study__status;
ALTER TABLE ONLY public.study_group_class DROP CONSTRAINT fk_study_gr_fk_study__group_ty;
ALTER TABLE ONLY public.study DROP CONSTRAINT fk_study_fk_study__user_acc;
ALTER TABLE ONLY public.study DROP CONSTRAINT fk_study_fk_study__status;
ALTER TABLE ONLY public.study_event DROP CONSTRAINT fk_study_ev_reference_study_su;
ALTER TABLE ONLY public.study_event_definition DROP CONSTRAINT fk_study_ev_fk_studye_user_acc;
ALTER TABLE ONLY public.study_event_definition DROP CONSTRAINT fk_study_ev_fk_studye_status;
ALTER TABLE ONLY public.study_event DROP CONSTRAINT fk_study_ev_fk_study__user_acc;
ALTER TABLE ONLY public.study_event DROP CONSTRAINT fk_study_ev_fk_study__study_ev;
ALTER TABLE ONLY public.study_event DROP CONSTRAINT fk_study_ev_fk_study__status;
ALTER TABLE ONLY public.section DROP CONSTRAINT fk_section_version;
ALTER TABLE ONLY public.section DROP CONSTRAINT fk_section_fk_sectio_user_acc;
ALTER TABLE ONLY public.section DROP CONSTRAINT fk_section_fk_sectio_status;
ALTER TABLE ONLY public.item_form_metadata DROP CONSTRAINT fk_sec_id;
ALTER TABLE ONLY public.item_form_metadata DROP CONSTRAINT fk_rs_id;
ALTER TABLE ONLY public.role_privilege_map DROP CONSTRAINT fk_role_id;
ALTER TABLE ONLY public.response_set DROP CONSTRAINT fk_response_fk_respon_response;
ALTER TABLE ONLY public.study_subject DROP CONSTRAINT fk_project__reference_study2;
ALTER TABLE ONLY public.role_privilege_map DROP CONSTRAINT fk_priv_id;
ALTER TABLE ONLY public.study_user_role DROP CONSTRAINT fk_person_role_study_id;
ALTER TABLE ONLY public.item_data DROP CONSTRAINT fk_item_reference_subject;
ALTER TABLE ONLY public.item_form_metadata DROP CONSTRAINT fk_item_id;
ALTER TABLE ONLY public.item_group DROP CONSTRAINT fk_item_gro_fk_item_g_user_acc;
ALTER TABLE ONLY public.item_group DROP CONSTRAINT fk_item_gro_fk_item_g_status;
ALTER TABLE ONLY public.item_group_map DROP CONSTRAINT fk_item_gro_fk_item_g_item_gro;
ALTER TABLE ONLY public.item_group_map DROP CONSTRAINT fk_item_gro_fk_item_g_item;
ALTER TABLE ONLY public.item DROP CONSTRAINT fk_item_fk_item_u_user_acc;
ALTER TABLE ONLY public.item DROP CONSTRAINT fk_item_fk_item_s_status;
ALTER TABLE ONLY public.item DROP CONSTRAINT fk_item_fk_item_i_item_dat;
ALTER TABLE ONLY public.item DROP CONSTRAINT fk_item_fk_item_f_item_ref;
ALTER TABLE ONLY public.item_data DROP CONSTRAINT fk_item_dat_fk_item_d_user_acc;
ALTER TABLE ONLY public.item_data DROP CONSTRAINT fk_item_dat_fk_item_d_status;
ALTER TABLE ONLY public.study_group DROP CONSTRAINT fk_group_class_study_group;
ALTER TABLE ONLY public.filter DROP CONSTRAINT fk_filter_fk_query__user_acc;
ALTER TABLE ONLY public.filter DROP CONSTRAINT fk_filter_fk_query__status;
ALTER TABLE ONLY public.filter_crf_version_map DROP CONSTRAINT fk_filter_c_reference_filter;
ALTER TABLE ONLY public.filter_crf_version_map DROP CONSTRAINT fk_filter_c_reference_crf_vers;
ALTER TABLE ONLY public.event_definition_crf DROP CONSTRAINT fk_event_de_study_crf_user_acc;
ALTER TABLE ONLY public.event_definition_crf DROP CONSTRAINT fk_event_de_reference_study_ev;
ALTER TABLE ONLY public.event_definition_crf DROP CONSTRAINT fk_event_de_fk_study__status;
ALTER TABLE ONLY public.event_crf DROP CONSTRAINT fk_event_cr_reference_study_su;
ALTER TABLE ONLY public.event_crf DROP CONSTRAINT fk_event_cr_fk_event__user_acc;
ALTER TABLE ONLY public.event_crf DROP CONSTRAINT fk_event_cr_fk_event__study_ev;
ALTER TABLE ONLY public.event_crf DROP CONSTRAINT fk_event_cr_fk_event__status;
ALTER TABLE ONLY public.event_crf DROP CONSTRAINT fk_event_cr_fk_event__completi;
ALTER TABLE ONLY public.decision_condition DROP CONSTRAINT fk_decision_fk_decisi_user_acc;
ALTER TABLE ONLY public.decision_condition DROP CONSTRAINT fk_decision_fk_decisi_status;
ALTER TABLE ONLY public.decision_condition DROP CONSTRAINT fk_decision_fk_decisi_crf_vers;
ALTER TABLE ONLY public.dc_summary_item_map DROP CONSTRAINT fk_dc_summa_fk_dc_sum_item;
ALTER TABLE ONLY public.dc_summary_item_map DROP CONSTRAINT fk_dc_summa_fk_dc_sum_dc_compu;
ALTER TABLE ONLY public.dc_substitution_event DROP CONSTRAINT fk_dc_subst_fk_dc_sub_item;
ALTER TABLE ONLY public.dc_substitution_event DROP CONSTRAINT fk_dc_subst_fk_dc_sub_dc_event;
ALTER TABLE ONLY public.dc_send_email_event DROP CONSTRAINT fk_dc_send__dc_send_e_dc_event;
ALTER TABLE ONLY public.dc_section_event DROP CONSTRAINT fk_dc_secti_fk_dc_sec_dc_event;
ALTER TABLE ONLY public.dc_primitive DROP CONSTRAINT fk_dc_primi_fk_item_i_item;
ALTER TABLE ONLY public.dc_primitive DROP CONSTRAINT fk_dc_primi_fk_dc_pri_item;
ALTER TABLE ONLY public.dc_primitive DROP CONSTRAINT fk_dc_primi_fk_dc_pri_decision;
ALTER TABLE ONLY public.dc_event DROP CONSTRAINT fk_dc_event_fk_dc_eve_decision;
ALTER TABLE ONLY public.dc_computed_event DROP CONSTRAINT fk_dc_compu_fk_dc_com_dc_event;
ALTER TABLE ONLY public.dataset_filter_map DROP CONSTRAINT fk_dataset_reference_filter;
ALTER TABLE ONLY public.dataset_filter_map DROP CONSTRAINT fk_dataset_reference_dataset;
ALTER TABLE ONLY public.dataset DROP CONSTRAINT fk_dataset_fk_datase_user_acc;
ALTER TABLE ONLY public.dataset DROP CONSTRAINT fk_dataset_fk_datase_study;
ALTER TABLE ONLY public.dataset DROP CONSTRAINT fk_dataset_fk_datase_status;
ALTER TABLE ONLY public.dataset_crf_version_map DROP CONSTRAINT fk_dataset__reference_dataset;
ALTER TABLE ONLY public.dataset_crf_version_map DROP CONSTRAINT fk_dataset__ref_event_event_de;
ALTER TABLE ONLY public.crf_version DROP CONSTRAINT fk_crf_vers_fk_crf_ve_status;
ALTER TABLE ONLY public.crf_version DROP CONSTRAINT fk_crf_vers_crf_versi_user_acc;
ALTER TABLE ONLY public.crf DROP CONSTRAINT fk_crf_fk_crf_fk_status;
ALTER TABLE ONLY public.crf DROP CONSTRAINT fk_crf_crf_user_user_acc;
ALTER TABLE ONLY public.completion_status DROP CONSTRAINT fk_completi_fk_comple_status;
ALTER TABLE ONLY public.audit_event_values DROP CONSTRAINT fk_audit_lo_ref_audit_lo;
ALTER TABLE ONLY public.audit_event_context DROP CONSTRAINT fk_audit_ev_reference_audit_ev;
ALTER TABLE ONLY public.archived_dataset_file DROP CONSTRAINT fk_archived_reference_export_f;
ALTER TABLE ONLY public.archived_dataset_file DROP CONSTRAINT fk_archived_reference_dataset;
ALTER TABLE ONLY public.item_data DROP CONSTRAINT fk_answer_reference_item;
ALTER TABLE ONLY public.dn_subject_map DROP CONSTRAINT dn_subject_map_subject_id_fkey;
ALTER TABLE ONLY public.dn_subject_map DROP CONSTRAINT dn_subject_map_discrepancy_note_id_fkey;
ALTER TABLE ONLY public.dn_study_subject_map DROP CONSTRAINT dn_study_subject_map_study_subject_id_fkey;
ALTER TABLE ONLY public.dn_study_subject_map DROP CONSTRAINT dn_study_subject_map_discrepancy_note_id_fkey;
ALTER TABLE ONLY public.dn_study_event_map DROP CONSTRAINT dn_study_event_map_study_event_id_fkey;
ALTER TABLE ONLY public.dn_study_event_map DROP CONSTRAINT dn_study_event_map_discrepancy_note_id_fkey;
ALTER TABLE ONLY public.dn_item_data_map DROP CONSTRAINT dn_item_data_map_item_data_id_fkey;
ALTER TABLE ONLY public.dn_item_data_map DROP CONSTRAINT dn_item_data_map_discrepancy_note_id_fkey;
ALTER TABLE ONLY public.dn_event_crf_map DROP CONSTRAINT dn_event_crf_map_event_crf_id_fkey;
ALTER TABLE ONLY public.dn_event_crf_map DROP CONSTRAINT dn_event_crf_map_discrepancy_note_id_fkey;
ALTER TABLE ONLY public.discrepancy_note DROP CONSTRAINT discrepancy_note_study_id_fkey;
ALTER TABLE ONLY public.discrepancy_note DROP CONSTRAINT discrepancy_note_resolution_status_id_fkey;
ALTER TABLE ONLY public.discrepancy_note DROP CONSTRAINT discrepancy_note_owner_id_fkey;
ALTER TABLE ONLY public.discrepancy_note DROP CONSTRAINT discrepancy_note_discrepancy_note_type_id_fkey;
ALTER TABLE ONLY public.study DROP CONSTRAINT "PROJECT IS CONTAINED WITHIN PA";
ALTER TABLE ONLY public.subject DROP CONSTRAINT "HAS MOTHER";
ALTER TABLE ONLY public.subject DROP CONSTRAINT "HAS FATHER";
ALTER TABLE ONLY public.study_event_definition DROP CONSTRAINT "FK_STUDY_EV_FK-STUDY__STUDY";
DROP TRIGGER user_account_trigger ON public.user_account;
DROP TRIGGER subject_trigger ON public.subject;
DROP TRIGGER subject_group_map_trigger ON public.subject_group_map;
DROP TRIGGER study_user_role_trigger ON public.study_user_role;
DROP TRIGGER study_subject_trigger ON public.study_subject;
DROP TRIGGER study_event_trigger ON public.study_event;
DROP TRIGGER item_data_trigger ON public.item_data;
DROP TRIGGER event_crf_trigger ON public.event_crf;
DROP TRIGGER discrepancy_note_trigger ON public.discrepancy_note;
ALTER TABLE ONLY public.resolution_status DROP CONSTRAINT resolution_status_pkey;
ALTER TABLE ONLY public.crf_version DROP CONSTRAINT pk_versioning;
ALTER TABLE ONLY public.user_type DROP CONSTRAINT pk_user_type;
ALTER TABLE ONLY public.subject_group_map DROP CONSTRAINT pk_subject_group_map;
ALTER TABLE ONLY public.subject_event_status DROP CONSTRAINT pk_subject_event_status;
ALTER TABLE ONLY public.study_type DROP CONSTRAINT pk_study_type;
ALTER TABLE ONLY public.study_event_definition DROP CONSTRAINT pk_study_event_definition;
ALTER TABLE ONLY public.study_event DROP CONSTRAINT pk_study_event;
ALTER TABLE ONLY public.status DROP CONSTRAINT pk_status;
ALTER TABLE ONLY public.section DROP CONSTRAINT pk_section_id;
ALTER TABLE ONLY public.response_set DROP CONSTRAINT pk_rs_id;
ALTER TABLE ONLY public.user_role DROP CONSTRAINT pk_role_id;
ALTER TABLE ONLY public.response_type DROP CONSTRAINT pk_response_type;
ALTER TABLE ONLY public.dataset DROP CONSTRAINT pk_report_library;
ALTER TABLE ONLY public.filter DROP CONSTRAINT pk_query_library;
ALTER TABLE ONLY public.event_definition_crf DROP CONSTRAINT pk_project_instrument;
ALTER TABLE ONLY public.study_subject DROP CONSTRAINT pk_project_individual;
ALTER TABLE ONLY public.study_group_class DROP CONSTRAINT pk_project_family;
ALTER TABLE ONLY public.study DROP CONSTRAINT pk_project;
ALTER TABLE ONLY public.privilege DROP CONSTRAINT pk_priv_id;
ALTER TABLE ONLY public.item_form_metadata DROP CONSTRAINT pk_pl_metadata_id;
ALTER TABLE ONLY public.item_group DROP CONSTRAINT pk_phenotype_list;
ALTER TABLE ONLY public.user_account DROP CONSTRAINT pk_person_user;
ALTER TABLE ONLY public.null_value_type DROP CONSTRAINT pk_null_value_type;
ALTER TABLE ONLY public.item_reference_type DROP CONSTRAINT pk_item_reference_type;
ALTER TABLE ONLY public.item_data_type DROP CONSTRAINT pk_item_data_type;
ALTER TABLE ONLY public.item DROP CONSTRAINT pk_item;
ALTER TABLE ONLY public.crf DROP CONSTRAINT pk_instrument;
ALTER TABLE ONLY public.subject DROP CONSTRAINT pk_individual;
ALTER TABLE ONLY public.group_class_types DROP CONSTRAINT pk_group_types;
ALTER TABLE ONLY public.study_group DROP CONSTRAINT pk_group_role;
ALTER TABLE ONLY public.export_format DROP CONSTRAINT pk_export_format;
ALTER TABLE ONLY public.event_crf DROP CONSTRAINT pk_event_crf;
ALTER TABLE ONLY public.decision_condition DROP CONSTRAINT pk_decision_condition;
ALTER TABLE ONLY public.dc_substitution_event DROP CONSTRAINT pk_dc_substitution_event;
ALTER TABLE ONLY public.dc_send_email_event DROP CONSTRAINT pk_dc_send_email_event;
ALTER TABLE ONLY public.dc_section_event DROP CONSTRAINT pk_dc_section_event;
ALTER TABLE ONLY public.dc_primitive DROP CONSTRAINT pk_dc_primitive;
ALTER TABLE ONLY public.dc_event DROP CONSTRAINT pk_dc_event;
ALTER TABLE ONLY public.dc_computed_event DROP CONSTRAINT pk_dc_computed_event;
ALTER TABLE ONLY public.completion_status DROP CONSTRAINT pk_completion_status;
ALTER TABLE ONLY public.audit_event DROP CONSTRAINT pk_audit_event;
ALTER TABLE ONLY public.archived_dataset_file DROP CONSTRAINT pk_archived_dataset_file;
ALTER TABLE ONLY public.item_data DROP CONSTRAINT pk_answer;
ALTER TABLE ONLY public.discrepancy_note_type DROP CONSTRAINT discrepancy_note_type_pkey;
ALTER TABLE ONLY public.discrepancy_note DROP CONSTRAINT discrepancy_note_pkey;
DROP TABLE public.versioning_map;
DROP TABLE public.user_type;
DROP TABLE public.user_role;
DROP TABLE public.user_account;
DROP TABLE public.test_table_three;
DROP TABLE public.subject_group_map;
DROP TABLE public.subject_event_status;
DROP TABLE public.subject;
DROP TABLE public.study_user_role;
DROP TABLE public.study_type;
DROP TABLE public.study_subject;
DROP TABLE public.study_group_class;
DROP TABLE public.study_event_definition;
DROP TABLE public.study_event;
DROP TABLE public.study;
DROP TABLE public.status;
DROP TABLE public.section;
DROP TABLE public.role_privilege_map;
DROP TABLE public.response_type;
DROP TABLE public.response_set;
DROP TABLE public.resolution_status;
DROP TABLE public.privilege;
DROP TABLE public.openclinica_version;
DROP TABLE public.null_value_type;
DROP TABLE public.item_reference_type;
DROP TABLE public.item_group_map;
DROP TABLE public.item_group;
DROP TABLE public.item_form_metadata;
DROP TABLE public.item_data_type;
DROP TABLE public.item_data;
DROP TABLE public.item;
DROP TABLE public.study_group;
DROP TABLE public.group_class_types;
DROP TABLE public.filter_crf_version_map;
DROP TABLE public.filter;
DROP TABLE public.export_format;
DROP TABLE public.event_definition_crf;
DROP TABLE public.event_crf;
DROP TABLE public.dn_subject_map;
DROP TABLE public.dn_study_subject_map;
DROP TABLE public.dn_study_event_map;
DROP TABLE public.dn_item_data_map;
DROP TABLE public.dn_event_crf_map;
DROP TABLE public.discrepancy_note_type;
DROP TABLE public.discrepancy_note;
DROP TABLE public.decision_condition;
DROP TABLE public.dc_summary_item_map;
DROP TABLE public.dc_substitution_event;
DROP TABLE public.dc_send_email_event;
DROP TABLE public.dc_section_event;
DROP TABLE public.dc_primitive;
DROP TABLE public.dc_event;
DROP TABLE public.dc_computed_event;
DROP TABLE public.dataset_filter_map;
DROP TABLE public.dataset_crf_version_map;
DROP TABLE public.dataset;
DROP TABLE public.crf_version;
DROP TABLE public.crf;
DROP TABLE public.completion_status;
DROP TABLE public.audit_event_values;
DROP TABLE public.audit_event_context;
DROP TABLE public.audit_event;
DROP TABLE public.archived_dataset_file;
DROP FUNCTION public.user_account_trigger();
DROP FUNCTION public.subject_trigger();
DROP FUNCTION public.subject_group_map_trigger();
DROP FUNCTION public.study_user_role_trigger();
DROP FUNCTION public.study_subject_trigger();
DROP FUNCTION public.study_event_trigger();
DROP FUNCTION public.item_data_trigger();
DROP FUNCTION public.event_crf_trigger();
DROP FUNCTION public.discrepancy_note_trigger();
DROP PROCEDURAL LANGUAGE plpgsql;
DROP FUNCTION public.plpgsql_validator(oid);
DROP FUNCTION public.plpgsql_call_handler();
DROP SCHEMA public;
--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO postgres;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';


--
-- Name: plpgsql_call_handler(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION plpgsql_call_handler() RETURNS language_handler
    AS '$libdir/plpgsql', 'plpgsql_call_handler'
    LANGUAGE c;


ALTER FUNCTION public.plpgsql_call_handler() OWNER TO postgres;

--
-- Name: plpgsql_validator(oid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION plpgsql_validator(oid) RETURNS void
    AS '$libdir/plpgsql', 'plpgsql_validator'
    LANGUAGE c;


ALTER FUNCTION public.plpgsql_validator(oid) OWNER TO postgres;

--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: public; Owner: 
--

CREATE TRUSTED PROCEDURAL LANGUAGE plpgsql HANDLER plpgsql_call_handler VALIDATOR plpgsql_validator;


--
-- Name: discrepancy_note_trigger(); Type: FUNCTION; Schema: public; Owner: clinica
--

CREATE FUNCTION discrepancy_note_trigger() RETURNS "trigger"
    AS $$
	DECLARE
		pk INTEGER;
		dn_event_crf_id integer;
		dn_item_id integer;
		dn_study_event_id integer;
		dn_study_subject_id integer;
		dn_subject_id integer;
		study_id integer;
		dn_type_name TEXT;
		resolution_status_name TEXT;
		item_name TEXT;
	BEGIN
		
		SELECT INTO pk NEXTVAL('audit_event_audit_id_seq');

		--do we only do inserts and deletes?

		IF (TG_OP = 'INSERT') THEN
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Discrepancy Note', 
			new.owner_ID,
			new.discrepancy_note_id, 'Added a Discrepancy Note','Added a Discrepancy Note');

			select into dn_event_crf_id event_crf_id from dn_event_crf_map where discrepancy_note_id = new.discrepancy_note_id;

			select into dn_item_id id.item_id from dn_item_data_map idm, item_data id where idm.item_data_id = id.item_data_id and idm.discrepancy_note_id = new.discrepancy_note_id;

			select into dn_study_event_id study_event_id from dn_study_event_map where discrepancy_note_id = new.discrepancy_note_id;

			select into dn_study_subject_id study_subject_id from dn_study_subject_map where discrepancy_note_id = new.discrepancy_note_id;

			select into dn_subject_id subject_id from dn_subject_map where discrepancy_note_id = new.discrepancy_note_id;

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, event_crf_id, item_id, study_event_id, study_subject_id, subject_id, ROLE_NAME)
			VALUES
			(pk, dn_event_crf_id, dn_item_id, dn_study_event_id, dn_study_subject_id, dn_subject_id,
			'Discrepancy note created in database');

			--short description, type, resolution status, note

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Short Description', null, NEW.description);

			select into dn_type_name name from discrepancy_note_type where discrepancy_note_type_id = new.discrepancy_note_type_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Type', null, dn_type_name);

			select into resolution_status_name name from resolution_status where resolution_status_id = new.resolution_status_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Resolution Status', null, resolution_status_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Note', null, new.detailed_notes);

			select into item_name name from item where item_id = dn_item_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Item', null, item_name);

		ELSIF (TG_OP = 'DELETE') THEN
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Discrepancy Note', 
			old.owner_ID,
			old.discrepancy_note_id, 'Deleted a Discrepancy Note','Deleted a Discrepancy Note');

			select into dn_subject_id subject_id from dn_subject_map where discrepancy_note_id = old.discrepancy_note_id;

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, subject_id, ROLE_NAME)
			VALUES
			(pk, dn_subject_id,
			'Discrepancy note deleted from database');

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Short Description', null, NEW.description);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Note', null, new.detailed_notes);
		END IF;

	RETURN NULL;	
	END;
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.discrepancy_note_trigger() OWNER TO clinica;

--
-- Name: event_crf_trigger(); Type: FUNCTION; Schema: public; Owner: clinica
--

CREATE FUNCTION event_crf_trigger() RETURNS "trigger"
    AS $$
	DECLARE
		pk INTEGER;
		crf_name TEXT;
		crf_version_name TEXT;
		study_event_def_name TEXT;
		study_event_start_date DATE;
	BEGIN
		--CREATES a row in the audit table; at the same time,
		--creates a row in the context table and multiple rows
		--in the values table

		SELECT INTO pk NEXTVAL('audit_event_audit_id_seq');
		IF (TG_OP = 'INSERT') THEN
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Event CRF', 
			new.owner_ID,
			new.event_CRF_ID, 'Added an Event CRF','Started CRF Data Entry');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, event_CRF_ID, study_event_id,
			study_subject_id, crf_version_id, ROLE_NAME)
			VALUES
			(pk, new.event_CRF_ID, new.study_event_id, new.study_subject_id, new.crf_version_id, 'created');
			
			select into crf_name crf.name from crf_version crfv, crf where crfv.crf_id = crf.crf_id and crfv.crf_version_id = new.crf_version_id;

			select into crf_version_name name from crf_version where crf_version_id = new.crf_version_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF', null, crf_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF Version', null, crf_version_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Interviewer', null, crf_version_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Interview Date', null, crf_version_name);

			select into study_event_def_name sed.name from study_event_definition sed, study_event se where se.study_event_definition_id = sed.study_event_definition_id and se.study_event_id = new.study_event_id;

			select into study_event_start_date date_start from study_event where study_event_id = new.study_event_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Definition', study_event_def_name, study_event_def_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Start Date', study_event_start_date, study_event_start_date);
		ELSIF (TG_OP = 'UPDATE') THEN
			--updated, removed, marked complete
			--inserted the basic data first, then other messages
			--errors if we try to insert values first, so we need to 
			--set up the audit_event row and then update it later on
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
					USER_ID, ENTITY_ID)
					VALUES
					(pk, now(), 
					'Event CRF', 
					new.update_ID,
					new.event_CRF_ID);

			select into crf_name crf.name from crf_version crfv, crf where crfv.crf_id = crf.crf_id and crfv.crf_version_id = new.crf_version_id;

			select into crf_version_name name from crf_version where crf_version_id = new.crf_version_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF', null, crf_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF Version', null, crf_version_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Interviewer', null, crf_version_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Interview Date', null, crf_version_name);

			select into study_event_def_name sed.name from study_event_definition sed, study_event se where se.study_event_definition_id = sed.study_event_definition_id and se.study_event_id = new.study_event_id;

			select into study_event_start_date date_start from study_event where study_event_id = new.study_event_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Definition', study_event_def_name, study_event_def_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Start Date', study_event_start_date, study_event_start_date);

			IF ( OLD.status_id != NEW.status_id) THEN
				
				IF ( OLD.status_id = 1 AND NEW.status_id = 5) THEN
					update audit_event set reason_for_change='Removed Event CRF and all related data' where audit_id = pk;

					update audit_event set action_message = 'Removed Event CRF' where audit_id = pk;

					INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, event_CRF_ID, study_event_id,
					study_subject_id, crf_version_id, ROLE_NAME)
					VALUES
					(pk, new.event_CRF_ID, new.study_event_id, 
					new.study_subject_id, new.crf_version_id, 'removed');
					
				ELSIF ( OLD.status_id = 5 AND NEW.status_id = 1) THEN

					update audit_event set reason_for_change='Restored Event CRF and all related data' where audit_id = pk;

					update audit_event set action_message = 'Restored Event CRF' where audit_id = pk;

					INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, event_CRF_ID, study_event_id,
					study_subject_id, crf_version_id, ROLE_NAME)
					VALUES
					(pk, new.event_CRF_ID, new.study_event_id, 
					new.study_subject_id, new.crf_version_id, 'restored');
				
				ELSIF ( NEW.status_id = 2) THEN

					update audit_event set reason_for_change='Marked CRF and all its items as complete' where audit_id = pk;

					update audit_event set action_message = 'Marked CRF Complete' where audit_id = pk;

					INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, event_CRF_ID, study_event_id,
					study_subject_id, crf_version_id, ROLE_NAME)
					VALUES
					(pk, new.event_CRF_ID, new.study_event_id, 
					new.study_subject_id, new.crf_version_id, 'completed');
				END IF;

			ELSE

				update audit_event set reason_for_change='Updated Event CRF Properties' where audit_id = pk;

				update audit_event set action_message = 'Updated Event CRF Properties' where audit_id = pk;

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, event_CRF_ID, study_event_id,
				study_subject_id, crf_version_id, ROLE_NAME)
				VALUES
				(pk, new.event_CRF_ID, new.study_event_id, 
				new.study_subject_id, new.crf_version_id, 'updated');
			END IF;

		ELSIF (TG_OP = 'DELETE') THEN
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Event CRF', 
			old.owner_ID,
			old.event_CRF_ID, 'Permenantly deleted Event CRF record and all associated data from the database','Deleted Event CRF');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, event_CRF_ID, study_event_id,
			study_subject_id, crf_version_id, ROLE_NAME)
			VALUES
			(pk, old.event_CRF_ID, old.study_event_id, old.study_subject_id, old.crf_version_id, 'deleted');
			
			select into crf_name crf.name from crf_version crfv, crf where crfv.crf_id = crf.crf_id and crfv.crf_version_id = old.crf_version_id;

			select into crf_version_name name from crf_version where crf_version_id = old.crf_version_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF', crf_name, crf_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF Version', crf_version_name, crf_version_name);

			select into study_event_def_name sed.name from study_event_definition sed, study_event se where se.study_event_definition_id = sed.study_event_definition_id and se.study_event_id = old.study_event_id;

			select into study_event_start_date date_start from study_event where study_event_id = old.study_event_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Definition', study_event_def_name, study_event_def_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Start Date', study_event_start_date, study_event_start_date);

		END IF;

		

		
	RETURN NULL;	
	END;
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.event_crf_trigger() OWNER TO clinica;

--
-- Name: item_data_trigger(); Type: FUNCTION; Schema: public; Owner: clinica
--

CREATE FUNCTION item_data_trigger() RETURNS "trigger"
    AS $$
	DECLARE
		pk INTEGER;
		item_name TEXT;
		study_event_def_name TEXT;
		start_date DATE;
		study_event_def_ord TEXT;
		crf_name TEXT;
		crf_version_name TEXT;
	BEGIN
		
		SELECT INTO pk NEXTVAL('audit_event_audit_id_seq');

		--will be very plain, one for insert, one for update, one for delete -- maybe not so plain, as we have to aggregate
		

		IF (TG_OP = 'INSERT') THEN

			--todo: start date, def ord, crf, crf ver

			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Item Data', 
			new.owner_ID,
			new.item_data_ID, 'Inserted Data into a CRF','Saved Data to a CRF');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, ITEM_ID, EVENT_CRF_ID, ROLE_NAME)
			VALUES
			(pk, NEW.ITEM_ID, NEW.EVENT_CRF_ID, 
			'ITEM_DATA_created');

			select into crf_name crf.name from crf, event_crf, crf_version 
				where crf_version.crf_id = crf.crf_id 
				and event_crf.crf_version_id = crf_version.crf_version_id and event_crf.event_crf_id = new.event_crf_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF', null, crf_name);

			select into crf_version_name crf_version.name from crf_version, event_crf 
				where event_crf.crf_version_id = crf_version.crf_version_id and
				event_crf.event_crf_id = new.event_crf_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF Version', null, crf_version_name);

			select into item_name item.name from item 
				where item.item_id = new.item_id;


			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,item_name, null, NEW.VALUE);

			select into study_event_def_name sed.name from study_event_definition sed, event_crf, study_event se 
				where sed.study_event_definition_id = se.study_event_definition_id 
				and event_crf.study_event_id = se.study_event_id and event_crf.event_crf_id = new.event_crf_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Definition', null, study_event_def_name);

			select into start_date se.date_start from event_crf, study_event se 
				where event_crf.study_event_id = se.study_event_id and event_crf.event_crf_id = new.event_crf_id;
			
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Start Date', null, start_date);

		ELSIF (TG_OP = 'UPDATE') THEN

			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Item Data', 
			new.update_ID,
			new.item_data_ID, 'Updated Data on a CRF','Updated CRF Data');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, ITEM_ID, EVENT_CRF_ID, ROLE_NAME)
			VALUES
			(pk, NEW.ITEM_ID, NEW.EVENT_CRF_ID, 
			'ITEM_DATA_updated');

			select into item_name item.name from item 
				where item.item_id = new.item_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,item_name, OLD.VALUE, NEW.VALUE);

			select into crf_name crf.name from crf, event_crf, crf_version 
				where crf_version.crf_id = crf.crf_id 
				and event_crf.crf_version_id = crf_version.crf_version_id and event_crf.event_crf_id = new.event_crf_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF', null, crf_name);

			select into crf_version_name crf_version.name from crf_version, event_crf 
				where event_crf.crf_version_id = crf_version.crf_version_id and
				event_crf.event_crf_id = new.event_crf_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'CRF Version', null, crf_version_name);

			select into start_date se.date_start from event_crf, study_event se 
				where event_crf.study_event_id = se.study_event_id and event_crf.event_crf_id = new.event_crf_id;
			
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Start Date', null, start_date);

		ELSIF (TG_OP = 'DELETE') THEN

			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Item Data', 
			old.update_ID,
			old.item_data_ID, 'Deleted Data from a CRF','Deleted Data from a CRF');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, ITEM_ID, EVENT_CRF_ID, ROLE_NAME)
			VALUES
			(pk, old.ITEM_ID, old.EVENT_CRF_ID, 
			'ITEM_DATA_deleted');

			select into item_name item.name from item 
				where item.item_id = old.item_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk, item_name, OLD.VALUE, old.value);
		END IF;
		
	RETURN NULL;	
	END;
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.item_data_trigger() OWNER TO clinica;

--
-- Name: study_event_trigger(); Type: FUNCTION; Schema: public; Owner: clinica
--

CREATE FUNCTION study_event_trigger() RETURNS "trigger"
    AS $$
	DECLARE
		pk INTEGER;
		new_study_event_def TEXT;
		old_study_event_def TEXT;
	BEGIN
		--CREATES a row in the audit table; at the same time,
		--creates a row in the context table and multiple rows

		SELECT INTO pk NEXTVAL('audit_event_audit_id_seq');

		IF (TG_OP = 'INSERT') THEN
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Study Event', 
			NEW.owner_ID,
			NEW.study_event_ID, 'Added a Study Event:','Added a Study Event');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, study_event_id,
			study_subject_id, study_event_definition_id, ROLE_NAME)
			VALUES
			(pk, new.study_event_id, new.study_subject_id, 
			new.study_event_definition_id, 'study event inserted');

			--do we want to select a study_id into the context table?

			SELECT INTO new_study_event_def NAME FROM STUDY_EVENT_DEFINITION WHERE STUDY_EVENT_DEFINITION_ID = NEW.STUDY_EVENT_DEFINITION_ID;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Location', null, NEW.location);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study Event Definition', null, new_study_event_def);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Event Ordinal', null, NEW.sample_ordinal);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Date Start', null, NEW.date_start);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Date End', null, NEW.date_end);

			--INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, --OLD_VALUE, NEW_VALUE)
			--VALUES
			--(pk,'study_event.owner_id', null, NEW.owner_id);

			--not sure if this will work
			--INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, --OLD_VALUE, NEW_VALUE)
			--VALUES
			--(pk,'study_event.date_created', null, --NEW.date_created);
		ELSIF (TG_OP = 'UPDATE') THEN
			--just updated and removed
			IF ((OLD.STATUS_ID = 1) AND (NEW.STATUS_ID = 5)) THEN
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Study Event', 
				new.update_id,
				old.STUDY_EVENT_ID, 'Removed Study Event and all associated events and study data', 'Removed a Study Event');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, study_event_id,
				study_subject_id, study_event_definition_id, ROLE_NAME)
				VALUES
				(pk, new.study_event_id, new.study_subject_id, 
				new.study_event_definition_id, 'study event removed');

				SELECT INTO new_study_event_def NAME FROM STUDY_EVENT_DEFINITION WHERE STUDY_EVENT_DEFINITION_ID = OLD.STUDY_EVENT_DEFINITION_ID;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Study Event Definition', new_study_event_def, new_study_event_def);
			ELSE
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Study Event', 
				new.update_ID,
				new.study_event_ID, 'Updated a Study Event:','Updated a Study Event');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, study_event_id,
				study_subject_id, study_event_definition_id, ROLE_NAME)
				VALUES
				(pk, new.study_event_id, new.study_subject_id, 
				new.study_event_definition_id, 'study event updated');

				SELECT INTO new_study_event_def NAME FROM STUDY_EVENT_DEFINITION WHERE STUDY_EVENT_DEFINITION_ID = NEW.STUDY_EVENT_DEFINITION_ID;

				SELECT INTO old_study_event_def NAME FROM STUDY_EVENT_DEFINITION WHERE STUDY_EVENT_DEFINITION_ID = OLD.STUDY_EVENT_DEFINITION_ID;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Study Event Definition', old_study_event_def, new_study_event_def);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Location', OLD.location, NEW.location);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Event Ordinal', old.sample_ordinal, NEW.sample_ordinal);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Date Start', old.date_start, NEW.date_start);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Date End', old.date_end, NEW.date_end);
			END IF;

			IF (OLD.SUBJECT_EVENT_STATUS_ID != NEW.SUBJECT_EVENT_STATUS_ID) THEN
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Subject Event Status ID', old.SUBJECT_EVENT_STATUS_ID, NEW.SUBJECT_EVENT_STATUS_ID);
			END IF;

		ELSIF (TG_OP = 'DELETE') THEN
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Study Event', 
			old.OWNER_ID,
			old.STUDY_EVENT_ID, 'Permenantly deleted Study Event record from database','Deleted a Study Event');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, study_event_id,
			study_subject_id, study_event_definition_id, ROLE_NAME)
			VALUES
			(pk, old.study_event_id, old.study_subject_id, 
			old.study_event_definition_id, 'deleted');

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Location', old.location, old.location);

		END IF;

	RETURN NULL;	
	END;
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.study_event_trigger() OWNER TO clinica;

--
-- Name: study_subject_trigger(); Type: FUNCTION; Schema: public; Owner: clinica
--

CREATE FUNCTION study_subject_trigger() RETURNS "trigger"
    AS $$
	DECLARE
		pk INTEGER;
		subj_global_id TEXT;
		old_study_name TEXT;
		new_study_name TEXT;
	BEGIN
		
		SELECT INTO pk NEXTVAL('audit_event_audit_id_seq');

		IF (TG_OP = 'INSERT') THEN
			SELECT INTO subj_global_id UNIQUE_IDENTIFIER FROM SUBJECT WHERE SUBJECT_ID = NEW.SUBJECT_ID;

			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
			USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Study Subject', 
			NEW.owner_ID,
			NEW.STUDY_SUBJECT_ID, 'Enrolled Subject in Study: ', 'Enrolled a Subject in a Study/Site');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, STUDY_ID,
			STUDY_SUBJECT_ID, ROLE_NAME)
			VALUES
			(pk, NEW.subject_ID, NEW.STUDY_ID, NEW.STUDY_SUBJECT_ID, 
			'Enrolled a Subject in a Study/Site');

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Date of Enrollment', null, NEW.ENROLLMENT_DATE);
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Unique ID', null, NEW.LABEL);
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Secondary ID', null, NEW.SECONDARY_LABEL);
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Subject Global ID', null, subj_global_id);

		ELSIF (TG_OP = 'UPDATE') THEN
			--we cover four cases here: removed, restored, reassignments,
			--and other updates
			IF ((OLD.STATUS_ID = 1) AND (NEW.STATUS_ID = 5)) THEN
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Study Subject', 
				new.update_id,
				old.STUDY_SUBJECT_ID, 'Removed study record and all associated study data: '|| old.label, 'Removed Subject from Study/Site');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, 
				STUDY_ID, STUDY_SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, old.subject_ID, OLD.STUDY_ID, OLD.STUDY_SUBJECT_ID,
				'removed');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Unique ID', OLD.LABEL, NEW.LABEL);

			ELSIF ((OLD.STATUS_ID = 5) AND (NEW.STATUS_ID = 1)) THEN
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Study Subject', 
				new.update_id,
				old.STUDY_SUBJECT_ID, 'Restored study subject record and all asscoiated study data: '|| old.label, 'Restored Subject to Study/Site');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, 
				STUDY_ID, STUDY_SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, old.subject_ID, OLD.STUDY_ID, OLD.STUDY_SUBJECT_ID,
				'restored');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Unique ID', OLD.LABEL, NEW.LABEL);

			ELSIF (OLD.STUDY_ID != NEW.STUDY_ID) THEN
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Study Subject', 
				new.update_id,
				old.STUDY_SUBJECT_ID, 'Reassigned study subject record and all associated study data to another site: '|| new.label, 'Reassigned Subject to New Site');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, 
				STUDY_ID, STUDY_SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, old.subject_ID, OLD.STUDY_ID, OLD.STUDY_SUBJECT_ID,
				'reassigned');

				SELECT INTO old_study_name NAME FROM STUDY WHERE STUDY_ID = OLD.STUDY_ID;

				SELECT INTO new_study_name NAME FROM STUDY WHERE STUDY_ID = NEW.STUDY_ID;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Study/Site', old_study_name, new_study_name);
			ELSE
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Study Subject', 
				new.update_id,
				old.STUDY_SUBJECT_ID, 'Updated subject information for a Study/Site', 'Updated Study Subject record');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, 
				STUDY_ID, STUDY_SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, old.subject_ID, OLD.STUDY_ID, OLD.STUDY_SUBJECT_ID,
				'updated');
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Unique ID', OLD.LABEL, NEW.LABEL);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Secondary Label', OLD.SECONDARY_LABEL, NEW.SECONDARY_LABEL);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Date of Enrollment', OLD.ENROLLMENT_DATE, NEW.ENROLLMENT_DATE);

			END IF;
		ELSIF (TG_OP = 'DELETE') THEN
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Study Subject', 
			new.update_id,
			old.STUDY_SUBJECT_ID, 'Permenantly deleted study subject record and all associated study data: '|| old.label, 'Deleted Study Subject from Study/Site');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, 
			STUDY_ID, STUDY_SUBJECT_ID, ROLE_NAME)
			VALUES
			(pk, old.subject_ID, OLD.STUDY_ID, OLD.STUDY_SUBJECT_ID,
			'deleted');

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Unique ID', OLD.LABEL, OLD.LABEL);
		END IF;

	RETURN NULL;	
	END;
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.study_subject_trigger() OWNER TO clinica;

--
-- Name: study_user_role_trigger(); Type: FUNCTION; Schema: public; Owner: clinica
--

CREATE FUNCTION study_user_role_trigger() RETURNS "trigger"
    AS $$
	DECLARE
		pk INTEGER;
		user_account_id	INTEGER;
		user_first_name TEXT;
		user_last_name TEXT;
		user_type_name TEXT;
		new_study_name TEXT;
		study_name TEXT;

	BEGIN
		
		SELECT INTO pk NEXTVAL('audit_event_audit_id_seq');

		IF (TG_OP = 'INSERT') THEN
			SELECT INTO user_account_id USER_ID FROM USER_ACCOUNT WHERE USER_NAME = NEW.USER_NAME;
			SELECT INTO user_first_name FIRST_NAME FROM USER_ACCOUNT WHERE USER_ID = user_account_id;
			SELECT INTO user_last_name LAST_NAME FROM USER_ACCOUNT WHERE USER_ID = user_account_id;
			
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'User Account', 
			new.owner_ID,
			user_account_id, 'Assigned User to a Study/Site:','Assigned User to Study');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
			VALUES
			(pk, NEW.study_id,
			'STUDY_USER_ROLE created in the database');

			--study site name and role name

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Username', null, NEW.user_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'User ID', null, user_account_id);

			select into study_name study.name from study where study_id = new.study_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study/Site', null, study_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Name', null, user_first_name || ' ' || user_last_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Role name', null, NEW.role_name);
			--add longer desc?


		ELSIF (TG_OP = 'UPDATE') THEN
			--again, modified, removed, restored
			SELECT INTO user_account_id USER_ID FROM USER_ACCOUNT WHERE USER_NAME = NEW.USER_NAME;
			SELECT INTO user_first_name FIRST_NAME FROM USER_ACCOUNT WHERE USER_ID = user_account_id;
			SELECT INTO user_last_name LAST_NAME FROM USER_ACCOUNT WHERE USER_ID = user_account_id;

			IF ((OLD.STATUS_ID = 1) AND (NEW.STATUS_ID = 5)) THEN
			
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'User Account', 
				new.owner_ID,
				user_account_id, 'Removed User from a Study/Site:','Removed User from Study');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
				VALUES
				(pk, NEW.study_id,
				'STUDY_USER_ROLE removed from study');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User ID', null, user_account_id);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Username', old.user_name, new.user_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Name', null, user_first_name || ' ' || user_last_name);

			ELSIF ((OLD.STATUS_ID = 5) AND (NEW.STATUS_ID = 1)) THEN
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'User Account', 
				new.owner_ID,
				user_account_id, 'Restored User to a Study/Site:','Restored User to Study');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
				VALUES
				(pk, NEW.study_id,
				'STUDY_USER_ROLE restored to study');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User ID', null, user_account_id);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Username', old.user_name, new.user_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Name', null, user_first_name || ' ' || user_last_name);
			ELSE
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'User Account', 
				new.owner_ID,
				user_account_id, 'Updated User Role in a Study/Site:','Modified User Role in Study');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
				VALUES
				(pk, NEW.study_id,
				'STUDY_USER_ROLE updated in the database');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Username', old.user_name, new.user_name);

				select into study_name study.name from study where study_id = old.study_id;

				select into new_study_name study.name from study where study_id = new.study_id;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User ID', null, user_account_id);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Study/Site', study_name, new_study_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Role name', old.role_name, new.role_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Name', null, user_first_name || ' ' || user_last_name);
			END IF;

		ELSIF (TG_OP = 'DELETE') THEN
			SELECT INTO user_account_id USER_ID FROM USER_ACCOUNT WHERE USER_NAME = OLD.USER_NAME;
			SELECT INTO user_first_name FIRST_NAME FROM USER_ACCOUNT WHERE USER_ID = user_account_id;
			SELECT INTO user_last_name LAST_NAME FROM USER_ACCOUNT WHERE USER_ID = user_account_id;

			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'User Account', 
			old.update_ID,
			user_account_id, 'Deleted User from a Study/Site:','Assigned User to Study');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
			VALUES
			(pk, old.study_id,
			'STUDY_USER_ROLE deleted from the database');

			--study site name and role name

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Username', old.user_name, old.user_name);

			select into study_name study.name from study where study_id = old.study_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Study/Site', study_name, study_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Name', null, user_first_name || ' ' || user_last_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Role name', old.role_name, old.role_name);
		END IF;

	RETURN NULL;	
	END;
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.study_user_role_trigger() OWNER TO clinica;

--
-- Name: subject_group_map_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION subject_group_map_trigger() RETURNS "trigger"
    AS $$
	DECLARE
		pk INTEGER;
		subj_global_id TEXT;
		study_id_new INTEGER;
		group_class_name TEXT;
		group_name TEXT;
		old_subj_global_id TEXT;
		old_group_class_name TEXT;
		old_group_name TEXT;
		
	BEGIN
		
		SELECT INTO pk NEXTVAL('audit_event_audit_id_seq');

		IF (TG_OP = 'INSERT') THEN

			select into study_id_new study_id from study_subject where study_subject_id = new.study_subject_id;

			select into group_class_name name from study_group_class where
			study_group_class_id = new.study_group_class_id;

			select into group_name name from study_group where
			study_group_id = new.study_group_id;

			--select into subj_global_id name from subject where
			--subject_id = new.subject_id;

			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
			USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Subject Group', 
			NEW.owner_ID,
			NEW.subject_group_map_ID, 
			'Assigned Subject to Group:', 
			'Assigned Subject to Group');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, 
			study_id, STUDY_SUBJECT_ID, ROLE_NAME)
			VALUES
			(pk, 
			study_id_new, 
			NEW.STUDY_SUBJECT_ID, 
			'Assigned Subject to Group');

			

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Date of Enrollment', null, NEW.DATE_CREATED);
			
			
			

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group Class', null, group_class_name);
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group', null, group_name);
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Notes', null, new.notes);

		ELSIF (TG_OP = 'UPDATE') THEN
			--we cover three cases here: removed, restored, updated 

			select into study_id_new study_id from study_subject where study_subject_id = new.study_subject_id;

			select into group_class_name name from study_group_class where
			study_group_class_id = new.study_group_class_id;

			select into old_group_class_name name from study_group_class where
			study_group_class_id = old.study_group_class_id;

			select into group_name name from study_group where
			study_group_id = new.study_group_id;

			select into old_group_name name from study_group where
			study_group_id = old.study_group_id;

			

			

			IF ((OLD.STATUS_ID = 1) AND (NEW.STATUS_ID = 5)) THEN
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Subject Group', 
				new.update_id,
				old.subject_group_map_ID, 
				'Removed Subject from Group', 
				'Removed Subject from a group');

			
				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, STUDY_SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, study_id_new, NEW.STUDY_SUBJECT_ID, 
				'Removed Subject from Group');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Date of Enrollment', old.date_created, NEW.DATE_CREATED);
			
			
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group Class', old_group_class_name, group_class_name);
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group', old_group_name, group_name);
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Notes', old.notes, new.notes);

			ELSIF ((OLD.STATUS_ID = 5) AND (NEW.STATUS_ID = 1)) THEN
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Subject Group', 
				new.update_id,
				old.subject_group_map_ID, 'Restored Subject to Group', 'Restored Subject to a group');

			
				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, STUDY_SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, study_id_new, NEW.STUDY_SUBJECT_ID, 
				'Removed Subject from Group');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Date of Enrollment', old.date_created, NEW.DATE_CREATED);
			
			
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group Class', old_group_class_name, group_class_name);
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group', old_group_name, group_name);
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Notes', old.notes, new.notes);

			
			ELSE
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Subject Group', 
				new.update_id,
				old.subject_group_map_ID, 'Updated Subject information for a Group', 'Updated Subject in a Group');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, STUDY_SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, study_id_new, NEW.STUDY_SUBJECT_ID, 
				'Removed Subject from Group');
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Date of Enrollment', old.date_created, NEW.DATE_CREATED);
			
			
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group Class', old_group_class_name, group_class_name);
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group', old_group_name, group_name);
				
				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Notes', old.notes, new.notes);

			END IF;
		ELSIF (TG_OP = 'DELETE') THEN

			select into study_id_new study_id from study_subject where study_subject_id = old.study_subject_id;

			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Subject Group', 
			old.update_id,
			old.subject_group_map_ID, 
			'Permenantly deleted Subject from Group', 
			'Deleted Subject from Group');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, STUDY_SUBJECT_ID, ROLE_NAME)
			VALUES
			(pk, study_id_new, old.STUDY_SUBJECT_ID, 
				'Deleted');

			select into old_group_class_name name from study_group_class where
			study_group_class_id = old.study_group_class_id;

			select into old_group_name name from study_group where
			study_group_id = old.study_group_id;

		

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Date of Enrollment', old.date_created, old.DATE_CREATED);
			
		
				
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group Class', old_group_class_name, old_group_class_name);
				
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Group', old_group_name, old_group_name);
				
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Notes', old.notes, old.notes);
		END IF;

	RETURN NULL;	
	END;
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.subject_group_map_trigger() OWNER TO clinica;

--
-- Name: subject_trigger(); Type: FUNCTION; Schema: public; Owner: clinica
--

CREATE FUNCTION subject_trigger() RETURNS "trigger"
    AS $$
	DECLARE
		pk INTEGER;
		mother TEXT;
		father TEXT;
	BEGIN
		
		SELECT INTO pk NEXTVAL('audit_event_audit_id_seq');
		
		--the following block will fill in the 'added subject record to database'
		IF (TG_OP = 'INSERT') THEN

			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
			USER_ID, ENTITY_ID, REASON_FOR_CHANGE,ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Subject', 
			NEW.owner_ID,
			NEW.SUBJECT_ID, 'Added Subject record to the database: '
			|| NEW.UNIQUE_IDENTIFIER,
			'Added Subject Record to database');

			--we want to add study_id to the context line as well,
			--but that may only occur when the trigger fires on an
			--insert to study_subject

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, ROLE_NAME)
			VALUES
			(pk, NEW.subject_ID, 
			'subject created in the database');

			--NEXT: COLLECT ALL INFORMATION, with human-readable --column names

			IF NEW.FATHER_ID IS NOT NULL then
				SELECT INTO father UNIQUE_IDENTIFIER FROM SUBJECT WHERE SUBJECT_ID = NEW.FATHER_ID;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Father ID', null, father);
			END IF;
			
			--INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, --OLD_VALUE, NEW_VALUE)
			--VALUES
			--(pk,'Subject ID', null, NEW.SUBJECT_ID);
			
			IF NEW.MOTHER_ID IS NOT NULL then
				SELECT INTO mother UNIQUE_IDENTIFIER FROM
				SUBJECT WHERE SUBJECT_ID = NEW.MOTHER_ID;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Mother ID', null, mother);
			END IF;
			
			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Date of Birth', null, NEW.DATE_OF_BIRTH);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Gender', null, NEW.GENDER);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Global ID', null, NEW.UNIQUE_IDENTIFIER);

		ELSIF (TG_OP = 'UPDATE') THEN
			--fufills the trigger for Updated global subject record,
			--but what about Removed global subject record
			--and restored global subject record?
			IF ((OLD.STATUS_ID = 1) and (NEW.STATUS_ID = 5)) THEN

				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Subject', 
				new.update_id,
				NEW.SUBJECT_ID, 'Removed Subject from system: ' || NEW.UNIQUE_IDENTIFIER,
				'Removed Subject from System');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, old.subject_ID, 
				'removed');

			ELSIF ( (OLD.STATUS_ID = 5) and (NEW.STATUS_ID = 1)) THEN

				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Subject', 
				new.update_id,
				NEW.SUBJECT_ID, 'Restored Subject to system: '
				|| NEW.UNIQUE_IDENTIFIER,
				'Restored Subject to system');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, old.subject_ID, 
				'restored');
			ELSE
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'Subject', 
				new.update_id,
				NEW.SUBJECT_ID, 
				'Updated Global Subject record: '
				|| NEW.UNIQUE_IDENTIFIER,
				'Updated Global Subject Record');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, ROLE_NAME)
				VALUES
				(pk, old.subject_ID, 
				'updated');

				IF ( OLD.MOTHER_ID != NEW.MOTHER_ID) THEN
					SELECT INTO mother UNIQUE_IDENTIFIER FROM
					SUBJECT WHERE SUBJECT_ID = NEW.MOTHER_ID;

					INSERT INTO AUDIT_EVENT_VALUES 	(AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
					VALUES
					(pk,'Mother ID', OLD.MOTHER_ID, mother);
				END IF;

				IF ( OLD.FATHER_ID != NEW.FATHER_ID) THEN
					SELECT INTO father UNIQUE_IDENTIFIER FROM
					SUBJECT WHERE SUBJECT_ID = NEW.FATHER_ID;

					INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
					VALUES
					(pk,'Father ID', OLD.FATHER_ID, father);
				END IF;

				IF ( OLD.STATUS_ID != NEW.STATUS_ID) THEN
					INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
					VALUES
					(pk,'Status ID', OLD.STATUS_ID, NEW.STATUS_ID);
				END IF;

				IF ( OLD.UNIQUE_IDENTIFIER != NEW.UNIQUE_IDENTIFIER) THEN
					INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
					VALUES
					(pk,'Global ID', OLD.UNIQUE_IDENTIFIER, NEW.UNIQUE_IDENTIFIER);
				END IF;


				IF ( OLD.OWNER_ID != NEW.OWNER_ID) THEN
					INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
					VALUES
					(pk,'Creator ID', OLD.OWNER_ID, NEW.OWNER_ID);
				END IF;

				IF ( OLD.UPDATE_ID != NEW.UPDATE_ID) THEN
					INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
					VALUES
					(pk,'Updater ID', OLD.UPDATE_ID, NEW.UPDATE_ID);
				END IF;

				IF ( OLD.DATE_UPDATED != NEW.DATE_UPDATED) THEN
					INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
					VALUES
					(pk,'Date Updated', OLD.DATE_UPDATED, NEW.DATE_UPDATED);
				END IF;
			END IF;

			--we are inserting gender and date of birth to show something no matter what

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Date of Birth', OLD.DATE_OF_BIRTH, NEW.DATE_OF_BIRTH);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Gender', OLD.GENDER, NEW.GENDER);

		ELSIF (TG_OP = 'DELETE') THEN

			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'Subject', 
			old.OWNER_ID,
			old.SUBJECT_ID, 'Permenantly deleted global subject record and all associated study data from database: '|| OLD.UNIQUE_IDENTIFIER , 'Deleted Subject from System');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, SUBJECT_ID, ROLE_NAME)
			VALUES
			(pk, old.subject_ID, 
			'deleted');

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Date of Birth', OLD.DATE_OF_BIRTH, OLD.DATE_OF_BIRTH);
		END IF;

	RETURN NULL;	
	END;
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.subject_trigger() OWNER TO clinica;

--
-- Name: user_account_trigger(); Type: FUNCTION; Schema: public; Owner: clinica
--

CREATE FUNCTION user_account_trigger() RETURNS "trigger"
    AS $$
	DECLARE
		pk INTEGER;
		active_study_name TEXT;
		user_type_name TEXT;

	BEGIN
		
		SELECT INTO pk NEXTVAL('audit_event_audit_id_seq');

		IF (TG_OP = 'INSERT') THEN
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
			VALUES
			(pk, now(), 
			'User Account', 
			new.owner_ID,
			new.user_ID, 'Added User Account','Added User Account');

			INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
			VALUES
			(pk, NEW.ACTIVE_STUDY,
			'USER_ACCOUNT created in the database');

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Username', null, NEW.user_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'First name', null, NEW.first_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Last name', null, NEW.last_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Email', null, NEW.email);

			select into active_study_name name from study where study_id = new.active_study;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Default Study', null, active_study_name);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Institutional Affiliation', null, NEW.institutional_affiliation);

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'Phone', null, NEW.phone);

			select into user_type_name user_type from user_type where user_type_id = new.user_type_id;

			INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
			VALUES
			(pk,'User Type', null, user_type_name);

		ELSIF (TG_OP = 'UPDATE') THEN
			--updated, removed, restored
			IF ((OLD.STATUS_ID = 5) AND (NEW.STATUS_ID = 1)) THEN
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'User Account', 
				new.update_ID,
				new.user_ID, 'Restored User Account to the system','Restored User Account');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
				VALUES
				(pk, NEW.ACTIVE_STUDY,
				'USER_ACCOUNT restored to the database');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User ID', OLD.USER_ID, NEW.user_id);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Username', OLD.USER_NAME, NEW.user_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Name', null, NEW.first_name || ' ' NEW.last_name);

				select into user_type_name user_type from user_type where user_type_id = new.user_type_id;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User Type', null, user_type_name);
				
			ELSIF (OLD.DATE_LASTVISIT != NEW.DATE_LASTVISIT) THEN

				IF (OLD.ACTIVE_STUDY != NEW.ACTIVE_STUDY) THEN
					INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
					USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
					VALUES
					(pk, now(), 
					'User Account', 
					new.update_ID,
					new.user_ID, 'User Changed Study','User Changed Study');
				ELSE
					INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
					USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
					VALUES
					(pk, now(), 
					'User Account', 
					new.update_ID,
					new.user_ID, 'User Login','User Login');
				END IF;
				
				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
				VALUES
				(pk, NEW.ACTIVE_STUDY,
				'USER_ACCOUNT updated in the database');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Username', OLD.USER_NAME, NEW.user_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'First name', OLD.FIRST_NAME, NEW.first_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Last name', OLD.LAST_NAME, NEW.last_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Email', OLD.EMAIL, NEW.email);

				select into active_study_name name from study where study_id = new.active_study;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Default Study', null, active_study_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Institutional Affiliation', 
				OLD.INSTITUTIONAL_AFFILIATION, NEW.institutional_affiliation);

				select into user_type_name user_type from user_type where user_type_id = new.user_type_id;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User Type', null, user_type_name);
				
			ELSIF (OLD.ACTIVE_STUDY != NEW.ACTIVE_STUDY) THEN

				
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
					USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
					VALUES
					(pk, now(), 
					'User Account', 
					new.update_ID,
					new.user_ID, 'User Changed Study','User Changed Study');
				
				
				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
				VALUES
				(pk, NEW.ACTIVE_STUDY,
				'USER_ACCOUNT updated in the database');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Username', OLD.USER_NAME, NEW.user_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'First name', OLD.FIRST_NAME, NEW.first_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Last name', OLD.LAST_NAME, NEW.last_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Email', OLD.EMAIL, NEW.email);

				select into active_study_name name from study where study_id = new.active_study;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Default Study', null, active_study_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Institutional Affiliation', 
				OLD.INSTITUTIONAL_AFFILIATION, NEW.institutional_affiliation);

				select into user_type_name user_type from user_type where user_type_id = new.user_type_id;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User Type', null, user_type_name);

			
			ELSE
				INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'User Account', 
				new.update_ID,
				new.user_ID, 'Updated User Account','Updated User Account');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
				VALUES
				(pk, NEW.ACTIVE_STUDY,
				'USER_ACCOUNT updated in the database');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Username', OLD.USER_NAME, NEW.user_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'First name', OLD.FIRST_NAME, NEW.first_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Last name', OLD.LAST_NAME, NEW.last_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Email', OLD.EMAIL, NEW.email);

				select into active_study_name name from study where study_id = new.active_study;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Default Study', null, active_study_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Institutional Affiliation', 
				OLD.INSTITUTIONAL_AFFILIATION, NEW.institutional_affiliation);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Phone', OLD.PHONE, NEW.phone);

				select into user_type_name user_type from user_type where user_type_id = new.user_type_id;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User Type', null, user_type_name);

				IF (OLD.PASSWD != NEW.PASSWD) THEN
					INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
					VALUES
					(pk,'Password', null, 'Changed');
				ELSE
					INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
					VALUES
					(pk,'Password', null, 'Not Changed');
				END IF;
			END IF;

		ELSIF (TG_OP = 'DELETE') THEN
			INSERT INTO AUDIT_EVENT (AUDIT_ID, AUDIT_DATE, AUDIT_TABLE, 
				USER_ID, ENTITY_ID, REASON_FOR_CHANGE, ACTION_MESSAGE)
				VALUES
				(pk, now(), 
				'User Account', 
				old.owner_ID,
				old.user_ID, 'Deleted User Account from the system','Deleted User Account');

				INSERT INTO AUDIT_EVENT_CONTEXT (AUDIT_ID, STUDY_ID, ROLE_NAME)
				VALUES
				(pk, old.ACTIVE_STUDY,
				'USER_ACCOUNT deleted');

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User ID', OLD.USER_ID, old.user_id);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Username', OLD.USER_NAME, old.user_name);

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'Name', null, OLD.first_name || ' ' OLD.last_name);

				select into user_type_name user_type from user_type where user_type_id = old.user_type_id;

				INSERT INTO AUDIT_EVENT_VALUES (AUDIT_ID, COLUMN_NAME, OLD_VALUE, NEW_VALUE)
				VALUES
				(pk,'User Type', user_type_name, user_type_name);
		END IF;

	RETURN NULL;	
	END;
	$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.user_account_trigger() OWNER TO clinica;

SET default_tablespace = '';

SET default_with_oids = true;

--
-- Name: archived_dataset_file; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE archived_dataset_file (
    archived_dataset_file_id serial NOT NULL,
    name character varying(255),
    dataset_id numeric,
    export_format_id numeric,
    file_reference character varying(1000),
    run_time numeric,
    file_size numeric,
    date_created date,
    owner_id numeric
);


ALTER TABLE public.archived_dataset_file OWNER TO clinica;

--
-- Name: audit_event; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE audit_event (
    audit_id serial NOT NULL,
    audit_date timestamp without time zone NOT NULL,
    audit_table character varying(500) NOT NULL,
    user_id numeric,
    entity_id numeric,
    reason_for_change character varying(1000),
    action_message character varying(4000)
);


ALTER TABLE public.audit_event OWNER TO clinica;

--
-- Name: audit_event_context; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE audit_event_context (
    audit_id numeric,
    study_id numeric,
    subject_id numeric,
    study_subject_id numeric,
    role_name character varying(200),
    event_crf_id numeric,
    study_event_id numeric,
    study_event_definition_id numeric,
    crf_id numeric,
    crf_version_id numeric,
    study_crf_id numeric,
    item_id numeric
);


ALTER TABLE public.audit_event_context OWNER TO clinica;

--
-- Name: audit_event_values; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE audit_event_values (
    audit_id numeric,
    column_name character varying(255),
    old_value character varying(2000),
    new_value character varying(2000)
);


ALTER TABLE public.audit_event_values OWNER TO clinica;

--
-- Name: completion_status; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE completion_status (
    completion_status_id serial NOT NULL,
    status_id numeric,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.completion_status OWNER TO clinica;

--
-- Name: crf; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE crf (
    crf_id serial NOT NULL,
    status_id numeric,
    name character varying(255),
    description character varying(2048),
    owner_id numeric,
    date_created date,
    date_updated date,
    update_id numeric
);


ALTER TABLE public.crf OWNER TO clinica;

--
-- Name: crf_version; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE crf_version (
    crf_version_id serial NOT NULL,
    crf_id numeric NOT NULL,
    name character varying(255),
    description character varying(4000),
    revision_notes character varying(255),
    status_id numeric,
    date_created date,
    date_updated date,
    owner_id numeric,
    update_id numeric
);


ALTER TABLE public.crf_version OWNER TO clinica;

--
-- Name: dataset; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dataset (
    dataset_id serial NOT NULL,
    study_id numeric,
    status_id numeric,
    name character varying(255),
    description character varying(2000),
    sql_statement text,
    num_runs numeric,
    date_start date,
    date_end date,
    date_created date,
    date_updated date,
    date_last_run date,
    owner_id numeric,
    approver_id numeric,
    update_id numeric,
    show_event_location boolean DEFAULT false,
    show_event_start boolean DEFAULT false,
    show_event_end boolean DEFAULT false,
    show_subject_dob boolean DEFAULT false,
    show_subject_gender boolean DEFAULT false
);


ALTER TABLE public.dataset OWNER TO clinica;

--
-- Name: dataset_crf_version_map; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dataset_crf_version_map (
    dataset_id numeric,
    event_definition_crf_id numeric
);


ALTER TABLE public.dataset_crf_version_map OWNER TO clinica;

--
-- Name: dataset_filter_map; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dataset_filter_map (
    dataset_id numeric,
    filter_id numeric,
    ordinal numeric
);


ALTER TABLE public.dataset_filter_map OWNER TO clinica;

--
-- Name: dc_computed_event; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dc_computed_event (
    dc_summary_event_id serial NOT NULL,
    dc_event_id numeric NOT NULL,
    item_target_id numeric,
    summary_type character varying(255)
);


ALTER TABLE public.dc_computed_event OWNER TO clinica;

--
-- Name: dc_event; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dc_event (
    dc_event_id serial NOT NULL,
    decision_condition_id numeric,
    ordinal numeric NOT NULL,
    "type" character varying(256) NOT NULL
);


ALTER TABLE public.dc_event OWNER TO clinica;

--
-- Name: dc_primitive; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dc_primitive (
    dc_primitive_id serial NOT NULL,
    decision_condition_id numeric,
    item_id numeric,
    dynamic_value_item_id numeric,
    comparison character varying(3) NOT NULL,
    constant_value character varying(4000)
);


ALTER TABLE public.dc_primitive OWNER TO clinica;

--
-- Name: dc_section_event; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dc_section_event (
    dc_event_id serial NOT NULL,
    section_id numeric NOT NULL
);


ALTER TABLE public.dc_section_event OWNER TO clinica;

--
-- Name: dc_send_email_event; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dc_send_email_event (
    dc_event_id serial NOT NULL,
    to_address character varying(1000) NOT NULL,
    subject character varying(1000),
    body character varying(4000)
);


ALTER TABLE public.dc_send_email_event OWNER TO clinica;

--
-- Name: dc_substitution_event; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dc_substitution_event (
    dc_event_id serial NOT NULL,
    item_id numeric,
    value character varying(1000) NOT NULL
);


ALTER TABLE public.dc_substitution_event OWNER TO clinica;

--
-- Name: dc_summary_item_map; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE dc_summary_item_map (
    dc_summary_event_id numeric,
    item_id numeric,
    ordinal numeric
);


ALTER TABLE public.dc_summary_item_map OWNER TO clinica;

--
-- Name: decision_condition; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE decision_condition (
    decision_condition_id serial NOT NULL,
    crf_version_id numeric,
    status_id numeric,
    label character varying(1000) NOT NULL,
    comments character varying(3000) NOT NULL,
    quantity numeric NOT NULL,
    "type" character varying(3) NOT NULL,
    owner_id numeric,
    date_created date,
    date_updated date,
    update_id numeric
);


ALTER TABLE public.decision_condition OWNER TO clinica;

--
-- Name: discrepancy_note; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE discrepancy_note (
    discrepancy_note_id serial NOT NULL,
    description character varying(255),
    discrepancy_note_type_id numeric,
    resolution_status_id numeric,
    detailed_notes character varying(1000),
    date_created date,
    owner_id numeric,
    parent_dn_id numeric,
    entity_type character varying(30),
    study_id numeric
);


ALTER TABLE public.discrepancy_note OWNER TO clinica;

--
-- Name: discrepancy_note_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE discrepancy_note_type (
    discrepancy_note_type_id serial NOT NULL,
    name character varying(50),
    description character varying(255)
);


ALTER TABLE public.discrepancy_note_type OWNER TO clinica;

SET default_with_oids = false;

--
-- Name: dn_event_crf_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_event_crf_map (
    event_crf_id numeric,
    discrepancy_note_id numeric,
    column_name character varying(255)
);


ALTER TABLE public.dn_event_crf_map OWNER TO clinica;

--
-- Name: dn_item_data_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_item_data_map (
    item_data_id numeric,
    discrepancy_note_id numeric,
    column_name character varying(255)
);


ALTER TABLE public.dn_item_data_map OWNER TO clinica;

--
-- Name: dn_study_event_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_study_event_map (
    study_event_id numeric,
    discrepancy_note_id numeric,
    column_name character varying(255)
);


ALTER TABLE public.dn_study_event_map OWNER TO clinica;

--
-- Name: dn_study_subject_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_study_subject_map (
    study_subject_id numeric,
    discrepancy_note_id numeric,
    column_name character varying(255)
);


ALTER TABLE public.dn_study_subject_map OWNER TO clinica;

SET default_with_oids = true;

--
-- Name: dn_subject_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_subject_map (
    subject_id numeric,
    discrepancy_note_id numeric,
    column_name character varying(255)
);


ALTER TABLE public.dn_subject_map OWNER TO clinica;

--
-- Name: event_crf; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE event_crf (
    event_crf_id serial NOT NULL,
    study_event_id numeric,
    crf_version_id numeric,
    date_interviewed date,
    interviewer_name character varying(255),
    completion_status_id numeric,
    status_id numeric,
    annotations character varying(4000),
    date_completed timestamp without time zone,
    validator_id numeric,
    date_validate date,
    date_validate_completed timestamp without time zone,
    validator_annotations character varying(4000),
    validate_string character varying(256),
    owner_id numeric,
    date_created date,
    study_subject_id integer,
    date_updated date,
    update_id numeric
);


ALTER TABLE public.event_crf OWNER TO clinica;

--
-- Name: event_definition_crf; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE event_definition_crf (
    event_definition_crf_id serial NOT NULL,
    study_event_definition_id numeric,
    study_id numeric,
    crf_id numeric,
    required_crf boolean,
    double_entry boolean,
    require_all_text_filled boolean,
    decision_conditions boolean,
    null_values character varying(255),
    default_version_id numeric,
    status_id numeric,
    owner_id numeric,
    date_created date,
    date_updated date,
    update_id numeric,
    ordinal numeric
);


ALTER TABLE public.event_definition_crf OWNER TO clinica;

--
-- Name: export_format; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE export_format (
    export_format_id serial NOT NULL,
    name character varying(255),
    description character varying(1000),
    mime_type character varying(255)
);


ALTER TABLE public.export_format OWNER TO clinica;

--
-- Name: filter; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE filter (
    filter_id serial NOT NULL,
    name character varying(255),
    description character varying(2000),
    sql_statement text,
    status_id numeric,
    date_created date,
    date_updated date,
    owner_id numeric NOT NULL,
    update_id numeric
);


ALTER TABLE public.filter OWNER TO clinica;

--
-- Name: filter_crf_version_map; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE filter_crf_version_map (
    filter_id numeric,
    crf_version_id numeric
);


ALTER TABLE public.filter_crf_version_map OWNER TO clinica;

--
-- Name: group_class_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE group_class_types (
    group_class_type_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.group_class_types OWNER TO clinica;

--
-- Name: study_group; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study_group (
    study_group_id serial NOT NULL,
    name character varying(255),
    description character varying(1000),
    study_group_class_id numeric
);


ALTER TABLE public.study_group OWNER TO clinica;

--
-- Name: item; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE item (
    item_id serial NOT NULL,
    name character varying(255),
    description character varying(4000),
    units character varying(64),
    phi_status boolean,
    item_data_type_id numeric,
    item_reference_type_id numeric,
    status_id numeric,
    owner_id numeric,
    date_created date,
    date_updated date,
    update_id numeric
);


ALTER TABLE public.item OWNER TO clinica;

--
-- Name: item_data; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE item_data (
    item_data_id serial NOT NULL,
    item_id numeric NOT NULL,
    event_crf_id numeric,
    status_id numeric,
    value character varying(255),
    date_created date,
    date_updated date,
    owner_id numeric,
    update_id numeric
);


ALTER TABLE public.item_data OWNER TO clinica;

--
-- Name: item_data_type; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE item_data_type (
    item_data_type_id serial NOT NULL,
    code character varying(20),
    name character varying(255),
    definition character varying(1000),
    reference character varying(1000)
);


ALTER TABLE public.item_data_type OWNER TO clinica;

--
-- Name: item_form_metadata; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE item_form_metadata (
    item_form_metadata_id serial NOT NULL,
    item_id numeric NOT NULL,
    crf_version_id numeric,
    header character varying(2000),
    subheader character varying(240),
    parent_id numeric,
    parent_label character varying(120),
    column_number numeric,
    page_number_label character varying(5),
    question_number_label character varying(20),
    left_item_text character varying(2000),
    right_item_text character varying(2000),
    section_id numeric NOT NULL,
    decision_condition_id numeric,
    response_set_id numeric NOT NULL,
    regexp character varying(1000),
    regexp_error_msg character varying(255),
    ordinal numeric NOT NULL,
    required boolean
);


ALTER TABLE public.item_form_metadata OWNER TO clinica;

--
-- Name: item_group; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE item_group (
    item_group_id serial NOT NULL,
    status_id numeric,
    name character varying(255),
    date_created date,
    date_updated date,
    owner_id numeric,
    update_id numeric
);


ALTER TABLE public.item_group OWNER TO clinica;

--
-- Name: item_group_map; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE item_group_map (
    item_group_id numeric,
    item_id numeric
);


ALTER TABLE public.item_group_map OWNER TO clinica;

--
-- Name: item_reference_type; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE item_reference_type (
    item_reference_type_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.item_reference_type OWNER TO clinica;

--
-- Name: null_value_type; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE null_value_type (
    null_value_type_id serial NOT NULL,
    code character varying(20),
    name character varying(255),
    definition character varying(1000),
    reference character varying(1000)
);


ALTER TABLE public.null_value_type OWNER TO clinica;

--
-- Name: openclinica_version; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE openclinica_version (
    name character varying(255),
    test_path character varying(1000)
);


ALTER TABLE public.openclinica_version OWNER TO clinica;

--
-- Name: privilege; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE privilege (
    priv_id serial NOT NULL,
    priv_name character varying(50),
    priv_desc character varying(2000)
);


ALTER TABLE public.privilege OWNER TO clinica;

--
-- Name: resolution_status; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE resolution_status (
    resolution_status_id serial NOT NULL,
    name character varying(50),
    description character varying(255)
);


ALTER TABLE public.resolution_status OWNER TO clinica;

--
-- Name: response_set; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE response_set (
    response_set_id serial NOT NULL,
    response_type_id numeric,
    label character varying(80),
    options_text character varying(4000),
    options_values character varying(4000),
    version_id numeric
);


ALTER TABLE public.response_set OWNER TO clinica;

--
-- Name: response_type; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE response_type (
    response_type_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.response_type OWNER TO clinica;

--
-- Name: role_privilege_map; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE role_privilege_map (
    role_id numeric NOT NULL,
    priv_id numeric NOT NULL,
    priv_value character varying(50)
);


ALTER TABLE public.role_privilege_map OWNER TO clinica;

--
-- Name: section; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE section (
    section_id serial NOT NULL,
    crf_version_id numeric NOT NULL,
    status_id numeric,
    label character varying(2000),
    title character varying(2000),
    subtitle character varying(2000),
    instructions character varying(2000),
    page_number_label character varying(5),
    ordinal numeric,
    parent_id numeric,
    date_created date,
    date_updated date,
    owner_id numeric NOT NULL,
    update_id numeric
);


ALTER TABLE public.section OWNER TO clinica;

--
-- Name: status; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE status (
    status_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.status OWNER TO clinica;

--
-- Name: study; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study (
    study_id serial NOT NULL,
    parent_study_id numeric,
    unique_identifier character varying(30),
    secondary_identifier character varying(255),
    name character varying(60),
    summary character varying(255),
    date_planned_start date,
    date_planned_end date,
    date_created date,
    date_updated date,
    owner_id numeric,
    update_id numeric,
    type_id numeric,
    status_id numeric,
    principal_investigator character varying(255),
    facility_name character varying(255),
    facility_city character varying(255),
    facility_state character varying(20),
    facility_zip character varying(64),
    facility_country character varying(64),
    facility_recruitment_status character varying(60),
    facility_contact_name character varying(255),
    facility_contact_degree character varying(255),
    facility_contact_phone character varying(255),
    facility_contact_email character varying(255),
    protocol_type character varying(30),
    protocol_description character varying(1000),
    protocol_date_verification date,
    phase character varying(30),
    expected_total_enrollment numeric,
    sponsor character varying(255),
    collaborators character varying(1000),
    medline_identifier character varying(255),
    url character varying(255),
    url_description character varying(255),
    conditions character varying(500),
    keywords character varying(255),
    eligibility character varying(500),
    gender character varying(30),
    age_max character varying(3),
    age_min character varying(3),
    healthy_volunteer_accepted boolean,
    purpose character varying(64),
    allocation character varying(64),
    masking character varying(30),
    control character varying(30),
    "assignment" character varying(30),
    endpoint character varying(64),
    interventions character varying(1000),
    duration character varying(30),
    selection character varying(30),
    timing character varying(30),
    official_title character varying(255),
    results_reference boolean,
    collect_dob boolean,
    discrepancy_management boolean
);


ALTER TABLE public.study OWNER TO clinica;

--
-- Name: study_event; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study_event (
    study_event_id serial NOT NULL,
    study_event_definition_id numeric,
    study_subject_id integer,
    "location" character varying(2000),
    sample_ordinal numeric,
    date_start date,
    date_end date,
    owner_id numeric,
    status_id numeric,
    date_created date,
    date_updated date,
    update_id numeric,
    subject_event_status_id numeric
);


ALTER TABLE public.study_event OWNER TO clinica;

--
-- Name: study_event_definition; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study_event_definition (
    study_event_definition_id serial NOT NULL,
    study_id numeric,
    name character varying(2000),
    description character varying(2000),
    repeating boolean,
    "type" character varying(20),
    category character varying(2000),
    owner_id numeric,
    status_id numeric,
    date_created date,
    date_updated date,
    update_id numeric,
    ordinal numeric
);


ALTER TABLE public.study_event_definition OWNER TO clinica;

--
-- Name: study_group_class; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study_group_class (
    study_group_class_id serial NOT NULL,
    name character varying(30),
    study_id numeric,
    owner_id numeric,
    date_created date,
    group_class_type_id numeric,
    status_id numeric,
    date_updated date,
    update_id numeric,
    subject_assignment character varying(30)
);


ALTER TABLE public.study_group_class OWNER TO clinica;

--
-- Name: study_subject; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study_subject (
    study_subject_id serial NOT NULL,
    label character varying(30),
    secondary_label character varying(30),
    subject_id numeric,
    study_id numeric,
    status_id numeric,
    enrollment_date date,
    date_created date,
    date_updated date,
    owner_id numeric,
    update_id numeric
);


ALTER TABLE public.study_subject OWNER TO clinica;

--
-- Name: study_type; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study_type (
    study_type_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.study_type OWNER TO clinica;

--
-- Name: study_user_role; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE study_user_role (
    role_name character varying(40),
    study_id numeric,
    status_id numeric,
    owner_id numeric,
    date_created date,
    date_updated date,
    update_id numeric,
    user_name character varying(40)
);


ALTER TABLE public.study_user_role OWNER TO clinica;

--
-- Name: subject; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject (
    subject_id serial NOT NULL,
    father_id numeric,
    mother_id numeric,
    status_id numeric,
    date_of_birth date,
    gender character(1),
    unique_identifier character varying(255),
    date_created date,
    owner_id numeric,
    date_updated date,
    update_id numeric,
    dob_collected boolean
);


ALTER TABLE public.subject OWNER TO clinica;

--
-- Name: subject_event_status; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE subject_event_status (
    subject_event_status_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.subject_event_status OWNER TO clinica;

--
-- Name: subject_group_map; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_group_map (
    subject_group_map_id serial NOT NULL,
    study_group_class_id numeric,
    study_subject_id numeric,
    study_group_id numeric,
    status_id numeric,
    owner_id numeric,
    date_created date,
    date_updated date,
    update_id numeric,
    notes character varying(255)
);


ALTER TABLE public.subject_group_map OWNER TO clinica;

--
-- Name: test_table_three; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE test_table_three (
    subject_id numeric,
    subject_identifier character varying(30),
    study_id integer,
    study_identifier character varying(30),
    event_definition_crf_id integer,
    crf_id integer,
    crf_description character varying(2048),
    crf_name character varying(255),
    crf_version_id integer,
    crf_version_revision_notes character varying(255),
    crf_version_name character varying(255),
    study_event_id integer,
    event_crf_id integer,
    item_data_id integer,
    value character varying(255),
    study_event_definition_name character varying(2000),
    study_event_definition_repeating boolean,
    sample_ordinal numeric,
    item_id integer,
    item_name character varying(255),
    item_description character varying(4000),
    item_units character varying(64),
    date_created date,
    study_event_definition_id integer,
    options_text character varying(4000),
    options_values character varying(4000),
    response_type_id numeric,
    gender character(1),
    date_of_birth date,
    "location" character varying(2000),
    date_start date,
    date_end date
);


ALTER TABLE public.test_table_three OWNER TO clinica;

--
-- Name: user_account; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE user_account (
    user_id serial NOT NULL,
    user_name character varying(64),
    passwd character varying(255),
    first_name character varying(50),
    last_name character varying(50),
    email character varying(120),
    active_study numeric,
    institutional_affiliation character varying(255),
    status_id numeric,
    owner_id numeric,
    date_created date,
    date_updated date,
    date_lastvisit timestamp without time zone,
    passwd_timestamp date,
    passwd_challenge_question character varying(64),
    passwd_challenge_answer character varying(255),
    phone character varying(64),
    user_type_id numeric,
    update_id numeric
);


ALTER TABLE public.user_account OWNER TO clinica;

--
-- Name: user_role; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE user_role (
    role_id serial NOT NULL,
    role_name character varying(50) NOT NULL,
    parent_id numeric,
    role_desc character varying(2000)
);


ALTER TABLE public.user_role OWNER TO clinica;

--
-- Name: user_type; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE user_type (
    user_type_id serial NOT NULL,
    user_type character varying(50)
);


ALTER TABLE public.user_type OWNER TO clinica;

--
-- Name: versioning_map; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE versioning_map (
    crf_version_id numeric,
    item_id numeric
);


ALTER TABLE public.versioning_map OWNER TO clinica;

--
-- Name: discrepancy_note_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY discrepancy_note
    ADD CONSTRAINT discrepancy_note_pkey PRIMARY KEY (discrepancy_note_id);


ALTER INDEX public.discrepancy_note_pkey OWNER TO clinica;

--
-- Name: discrepancy_note_type_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY discrepancy_note_type
    ADD CONSTRAINT discrepancy_note_type_pkey PRIMARY KEY (discrepancy_note_type_id);


ALTER INDEX public.discrepancy_note_type_pkey OWNER TO clinica;

--
-- Name: pk_answer; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT pk_answer PRIMARY KEY (item_data_id);


ALTER INDEX public.pk_answer OWNER TO clinica;

--
-- Name: pk_archived_dataset_file; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY archived_dataset_file
    ADD CONSTRAINT pk_archived_dataset_file PRIMARY KEY (archived_dataset_file_id);


ALTER INDEX public.pk_archived_dataset_file OWNER TO clinica;

--
-- Name: pk_audit_event; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY audit_event
    ADD CONSTRAINT pk_audit_event PRIMARY KEY (audit_id);


ALTER INDEX public.pk_audit_event OWNER TO clinica;

--
-- Name: pk_completion_status; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY completion_status
    ADD CONSTRAINT pk_completion_status PRIMARY KEY (completion_status_id);


ALTER INDEX public.pk_completion_status OWNER TO clinica;

--
-- Name: pk_dc_computed_event; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dc_computed_event
    ADD CONSTRAINT pk_dc_computed_event PRIMARY KEY (dc_summary_event_id);


ALTER INDEX public.pk_dc_computed_event OWNER TO clinica;

--
-- Name: pk_dc_event; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dc_event
    ADD CONSTRAINT pk_dc_event PRIMARY KEY (dc_event_id);


ALTER INDEX public.pk_dc_event OWNER TO clinica;

--
-- Name: pk_dc_primitive; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dc_primitive
    ADD CONSTRAINT pk_dc_primitive PRIMARY KEY (dc_primitive_id);


ALTER INDEX public.pk_dc_primitive OWNER TO clinica;

--
-- Name: pk_dc_section_event; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dc_section_event
    ADD CONSTRAINT pk_dc_section_event PRIMARY KEY (dc_event_id);


ALTER INDEX public.pk_dc_section_event OWNER TO clinica;

--
-- Name: pk_dc_send_email_event; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dc_send_email_event
    ADD CONSTRAINT pk_dc_send_email_event PRIMARY KEY (dc_event_id);


ALTER INDEX public.pk_dc_send_email_event OWNER TO clinica;

--
-- Name: pk_dc_substitution_event; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dc_substitution_event
    ADD CONSTRAINT pk_dc_substitution_event PRIMARY KEY (dc_event_id);


ALTER INDEX public.pk_dc_substitution_event OWNER TO clinica;

--
-- Name: pk_decision_condition; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY decision_condition
    ADD CONSTRAINT pk_decision_condition PRIMARY KEY (decision_condition_id);


ALTER INDEX public.pk_decision_condition OWNER TO clinica;

--
-- Name: pk_event_crf; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT pk_event_crf PRIMARY KEY (event_crf_id);


ALTER INDEX public.pk_event_crf OWNER TO clinica;

--
-- Name: pk_export_format; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY export_format
    ADD CONSTRAINT pk_export_format PRIMARY KEY (export_format_id);


ALTER INDEX public.pk_export_format OWNER TO clinica;

--
-- Name: pk_group_role; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study_group
    ADD CONSTRAINT pk_group_role PRIMARY KEY (study_group_id);


ALTER INDEX public.pk_group_role OWNER TO clinica;

--
-- Name: pk_group_types; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY group_class_types
    ADD CONSTRAINT pk_group_types PRIMARY KEY (group_class_type_id);


ALTER INDEX public.pk_group_types OWNER TO clinica;

--
-- Name: pk_individual; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT pk_individual PRIMARY KEY (subject_id);


ALTER INDEX public.pk_individual OWNER TO clinica;

--
-- Name: pk_instrument; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY crf
    ADD CONSTRAINT pk_instrument PRIMARY KEY (crf_id);


ALTER INDEX public.pk_instrument OWNER TO clinica;

--
-- Name: pk_item; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY item
    ADD CONSTRAINT pk_item PRIMARY KEY (item_id);


ALTER INDEX public.pk_item OWNER TO clinica;

--
-- Name: pk_item_data_type; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY item_data_type
    ADD CONSTRAINT pk_item_data_type PRIMARY KEY (item_data_type_id);


ALTER INDEX public.pk_item_data_type OWNER TO clinica;

--
-- Name: pk_item_reference_type; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY item_reference_type
    ADD CONSTRAINT pk_item_reference_type PRIMARY KEY (item_reference_type_id);


ALTER INDEX public.pk_item_reference_type OWNER TO clinica;

--
-- Name: pk_null_value_type; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY null_value_type
    ADD CONSTRAINT pk_null_value_type PRIMARY KEY (null_value_type_id);


ALTER INDEX public.pk_null_value_type OWNER TO clinica;

--
-- Name: pk_person_user; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY user_account
    ADD CONSTRAINT pk_person_user PRIMARY KEY (user_id);


ALTER INDEX public.pk_person_user OWNER TO clinica;

--
-- Name: pk_phenotype_list; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY item_group
    ADD CONSTRAINT pk_phenotype_list PRIMARY KEY (item_group_id);


ALTER INDEX public.pk_phenotype_list OWNER TO clinica;

--
-- Name: pk_pl_metadata_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY item_form_metadata
    ADD CONSTRAINT pk_pl_metadata_id PRIMARY KEY (item_form_metadata_id);


ALTER INDEX public.pk_pl_metadata_id OWNER TO clinica;

--
-- Name: pk_priv_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY privilege
    ADD CONSTRAINT pk_priv_id PRIMARY KEY (priv_id);


ALTER INDEX public.pk_priv_id OWNER TO clinica;

--
-- Name: pk_project; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study
    ADD CONSTRAINT pk_project PRIMARY KEY (study_id);


ALTER INDEX public.pk_project OWNER TO clinica;

--
-- Name: pk_project_family; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study_group_class
    ADD CONSTRAINT pk_project_family PRIMARY KEY (study_group_class_id);


ALTER INDEX public.pk_project_family OWNER TO clinica;

--
-- Name: pk_project_individual; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT pk_project_individual PRIMARY KEY (study_subject_id);


ALTER INDEX public.pk_project_individual OWNER TO clinica;

--
-- Name: pk_project_instrument; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT pk_project_instrument PRIMARY KEY (event_definition_crf_id);


ALTER INDEX public.pk_project_instrument OWNER TO clinica;

--
-- Name: pk_query_library; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY filter
    ADD CONSTRAINT pk_query_library PRIMARY KEY (filter_id);


ALTER INDEX public.pk_query_library OWNER TO clinica;

--
-- Name: pk_report_library; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT pk_report_library PRIMARY KEY (dataset_id);


ALTER INDEX public.pk_report_library OWNER TO clinica;

--
-- Name: pk_response_type; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY response_type
    ADD CONSTRAINT pk_response_type PRIMARY KEY (response_type_id);


ALTER INDEX public.pk_response_type OWNER TO clinica;

--
-- Name: pk_role_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT pk_role_id PRIMARY KEY (role_id);


ALTER INDEX public.pk_role_id OWNER TO clinica;

--
-- Name: pk_rs_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY response_set
    ADD CONSTRAINT pk_rs_id PRIMARY KEY (response_set_id);


ALTER INDEX public.pk_rs_id OWNER TO clinica;

--
-- Name: pk_section_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY section
    ADD CONSTRAINT pk_section_id PRIMARY KEY (section_id);


ALTER INDEX public.pk_section_id OWNER TO clinica;

--
-- Name: pk_status; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY status
    ADD CONSTRAINT pk_status PRIMARY KEY (status_id);


ALTER INDEX public.pk_status OWNER TO clinica;

--
-- Name: pk_study_event; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT pk_study_event PRIMARY KEY (study_event_id);


ALTER INDEX public.pk_study_event OWNER TO clinica;

--
-- Name: pk_study_event_definition; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study_event_definition
    ADD CONSTRAINT pk_study_event_definition PRIMARY KEY (study_event_definition_id);


ALTER INDEX public.pk_study_event_definition OWNER TO clinica;

--
-- Name: pk_study_type; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY study_type
    ADD CONSTRAINT pk_study_type PRIMARY KEY (study_type_id);


ALTER INDEX public.pk_study_type OWNER TO clinica;

--
-- Name: pk_subject_event_status; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY subject_event_status
    ADD CONSTRAINT pk_subject_event_status PRIMARY KEY (subject_event_status_id);


ALTER INDEX public.pk_subject_event_status OWNER TO clinica;

--
-- Name: pk_subject_group_map; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT pk_subject_group_map PRIMARY KEY (subject_group_map_id);


ALTER INDEX public.pk_subject_group_map OWNER TO clinica;

--
-- Name: pk_user_type; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY user_type
    ADD CONSTRAINT pk_user_type PRIMARY KEY (user_type_id);


ALTER INDEX public.pk_user_type OWNER TO clinica;

--
-- Name: pk_versioning; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY crf_version
    ADD CONSTRAINT pk_versioning PRIMARY KEY (crf_version_id);


ALTER INDEX public.pk_versioning OWNER TO clinica;

--
-- Name: resolution_status_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY resolution_status
    ADD CONSTRAINT resolution_status_pkey PRIMARY KEY (resolution_status_id);


ALTER INDEX public.resolution_status_pkey OWNER TO clinica;

--
-- Name: discrepancy_note_trigger; Type: TRIGGER; Schema: public; Owner: clinica
--

CREATE TRIGGER discrepancy_note_trigger
    AFTER INSERT OR DELETE ON discrepancy_note
    FOR EACH ROW
    EXECUTE PROCEDURE discrepancy_note_trigger();


--
-- Name: event_crf_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER event_crf_trigger
    AFTER INSERT OR DELETE OR UPDATE ON event_crf
    FOR EACH ROW
    EXECUTE PROCEDURE event_crf_trigger();


--
-- Name: item_data_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER item_data_trigger
    AFTER INSERT OR DELETE OR UPDATE ON item_data
    FOR EACH ROW
    EXECUTE PROCEDURE item_data_trigger();


--
-- Name: study_event_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER study_event_trigger
    AFTER INSERT OR DELETE OR UPDATE ON study_event
    FOR EACH ROW
    EXECUTE PROCEDURE study_event_trigger();


--
-- Name: study_subject_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER study_subject_trigger
    AFTER INSERT OR DELETE OR UPDATE ON study_subject
    FOR EACH ROW
    EXECUTE PROCEDURE study_subject_trigger();


--
-- Name: study_user_role_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER study_user_role_trigger
    AFTER INSERT OR DELETE OR UPDATE ON study_user_role
    FOR EACH ROW
    EXECUTE PROCEDURE study_user_role_trigger();


--
-- Name: subject_group_map_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER subject_group_map_trigger
    AFTER INSERT OR DELETE OR UPDATE ON subject_group_map
    FOR EACH ROW
    EXECUTE PROCEDURE subject_group_map_trigger();


--
-- Name: subject_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER subject_trigger
    AFTER INSERT OR DELETE OR UPDATE ON subject
    FOR EACH ROW
    EXECUTE PROCEDURE subject_trigger();


--
-- Name: user_account_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER user_account_trigger
    AFTER INSERT OR DELETE OR UPDATE ON user_account
    FOR EACH ROW
    EXECUTE PROCEDURE user_account_trigger();


--
-- Name: FK_STUDY_EV_FK-STUDY__STUDY; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_event_definition
    ADD CONSTRAINT "FK_STUDY_EV_FK-STUDY__STUDY" FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: HAS FATHER; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT "HAS FATHER" FOREIGN KEY (father_id) REFERENCES subject(subject_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: HAS MOTHER; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT "HAS MOTHER" FOREIGN KEY (mother_id) REFERENCES subject(subject_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: PROJECT IS CONTAINED WITHIN PA; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study
    ADD CONSTRAINT "PROJECT IS CONTAINED WITHIN PA" FOREIGN KEY (parent_study_id) REFERENCES study(study_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: discrepancy_note_discrepancy_note_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY discrepancy_note
    ADD CONSTRAINT discrepancy_note_discrepancy_note_type_id_fkey FOREIGN KEY (discrepancy_note_type_id) REFERENCES discrepancy_note_type(discrepancy_note_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: discrepancy_note_owner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY discrepancy_note
    ADD CONSTRAINT discrepancy_note_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: discrepancy_note_resolution_status_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY discrepancy_note
    ADD CONSTRAINT discrepancy_note_resolution_status_id_fkey FOREIGN KEY (resolution_status_id) REFERENCES resolution_status(resolution_status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: discrepancy_note_study_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY discrepancy_note
    ADD CONSTRAINT discrepancy_note_study_id_fkey FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_event_crf_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_event_crf_map
    ADD CONSTRAINT dn_event_crf_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_event_crf_map_event_crf_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_event_crf_map
    ADD CONSTRAINT dn_event_crf_map_event_crf_id_fkey FOREIGN KEY (event_crf_id) REFERENCES event_crf(event_crf_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_item_data_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_item_data_map
    ADD CONSTRAINT dn_item_data_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_item_data_map_item_data_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_item_data_map
    ADD CONSTRAINT dn_item_data_map_item_data_id_fkey FOREIGN KEY (item_data_id) REFERENCES item_data(item_data_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_study_event_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_study_event_map
    ADD CONSTRAINT dn_study_event_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_study_event_map_study_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_study_event_map
    ADD CONSTRAINT dn_study_event_map_study_event_id_fkey FOREIGN KEY (study_event_id) REFERENCES study_event(study_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_study_subject_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_study_subject_map
    ADD CONSTRAINT dn_study_subject_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_study_subject_map_study_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_study_subject_map
    ADD CONSTRAINT dn_study_subject_map_study_subject_id_fkey FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_subject_map_discrepancy_note_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_subject_map
    ADD CONSTRAINT dn_subject_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note(discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: dn_subject_map_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dn_subject_map
    ADD CONSTRAINT dn_subject_map_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_answer_reference_item; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT fk_answer_reference_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT;


--
-- Name: fk_archived_reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY archived_dataset_file
    ADD CONSTRAINT fk_archived_reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_archived_reference_export_f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY archived_dataset_file
    ADD CONSTRAINT fk_archived_reference_export_f FOREIGN KEY (export_format_id) REFERENCES export_format(export_format_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_audit_ev_reference_audit_ev; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY audit_event_context
    ADD CONSTRAINT fk_audit_ev_reference_audit_ev FOREIGN KEY (audit_id) REFERENCES audit_event(audit_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_audit_lo_ref_audit_lo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY audit_event_values
    ADD CONSTRAINT fk_audit_lo_ref_audit_lo FOREIGN KEY (audit_id) REFERENCES audit_event(audit_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_completi_fk_comple_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY completion_status
    ADD CONSTRAINT fk_completi_fk_comple_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_crf_crf_user_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY crf
    ADD CONSTRAINT fk_crf_crf_user_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_crf_fk_crf_fk_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY crf
    ADD CONSTRAINT fk_crf_fk_crf_fk_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_crf_vers_crf_versi_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY crf_version
    ADD CONSTRAINT fk_crf_vers_crf_versi_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_crf_vers_fk_crf_ve_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY crf_version
    ADD CONSTRAINT fk_crf_vers_fk_crf_ve_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset__ref_event_event_de; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dataset_crf_version_map
    ADD CONSTRAINT fk_dataset__ref_event_event_de FOREIGN KEY (event_definition_crf_id) REFERENCES event_definition_crf(event_definition_crf_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset__reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dataset_crf_version_map
    ADD CONSTRAINT fk_dataset__reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_fk_datase_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT fk_dataset_fk_datase_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_fk_datase_study; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT fk_dataset_fk_datase_study FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_fk_datase_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT fk_dataset_fk_datase_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dataset_filter_map
    ADD CONSTRAINT fk_dataset_reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_reference_filter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dataset_filter_map
    ADD CONSTRAINT fk_dataset_reference_filter FOREIGN KEY (filter_id) REFERENCES filter(filter_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_compu_fk_dc_com_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_computed_event
    ADD CONSTRAINT fk_dc_compu_fk_dc_com_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_event_fk_dc_eve_decision; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_event
    ADD CONSTRAINT fk_dc_event_fk_dc_eve_decision FOREIGN KEY (decision_condition_id) REFERENCES decision_condition(decision_condition_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_primi_fk_dc_pri_decision; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_primitive
    ADD CONSTRAINT fk_dc_primi_fk_dc_pri_decision FOREIGN KEY (decision_condition_id) REFERENCES decision_condition(decision_condition_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_primi_fk_dc_pri_item; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_primitive
    ADD CONSTRAINT fk_dc_primi_fk_dc_pri_item FOREIGN KEY (dynamic_value_item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_primi_fk_item_i_item; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_primitive
    ADD CONSTRAINT fk_dc_primi_fk_item_i_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_secti_fk_dc_sec_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_section_event
    ADD CONSTRAINT fk_dc_secti_fk_dc_sec_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_send__dc_send_e_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_send_email_event
    ADD CONSTRAINT fk_dc_send__dc_send_e_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_subst_fk_dc_sub_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_substitution_event
    ADD CONSTRAINT fk_dc_subst_fk_dc_sub_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_subst_fk_dc_sub_item; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_substitution_event
    ADD CONSTRAINT fk_dc_subst_fk_dc_sub_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_summa_fk_dc_sum_dc_compu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_summary_item_map
    ADD CONSTRAINT fk_dc_summa_fk_dc_sum_dc_compu FOREIGN KEY (dc_summary_event_id) REFERENCES dc_computed_event(dc_summary_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_summa_fk_dc_sum_item; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY dc_summary_item_map
    ADD CONSTRAINT fk_dc_summa_fk_dc_sum_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_decision_fk_decisi_crf_vers; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY decision_condition
    ADD CONSTRAINT fk_decision_fk_decisi_crf_vers FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_decision_fk_decisi_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY decision_condition
    ADD CONSTRAINT fk_decision_fk_decisi_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_decision_fk_decisi_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY decision_condition
    ADD CONSTRAINT fk_decision_fk_decisi_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_fk_event__completi; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__completi FOREIGN KEY (completion_status_id) REFERENCES completion_status(completion_status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_fk_event__status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_fk_event__study_ev; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__study_ev FOREIGN KEY (study_event_id) REFERENCES study_event(study_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_fk_event__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_reference_study_su; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_reference_study_su FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_de_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_event_de_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_de_reference_study_ev; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_event_de_reference_study_ev FOREIGN KEY (study_event_definition_id) REFERENCES study_event_definition(study_event_definition_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_de_study_crf_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_event_de_study_crf_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_filter_c_reference_crf_vers; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY filter_crf_version_map
    ADD CONSTRAINT fk_filter_c_reference_crf_vers FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_filter_c_reference_filter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY filter_crf_version_map
    ADD CONSTRAINT fk_filter_c_reference_filter FOREIGN KEY (filter_id) REFERENCES filter(filter_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_filter_fk_query__status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY filter
    ADD CONSTRAINT fk_filter_fk_query__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_filter_fk_query__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY filter
    ADD CONSTRAINT fk_filter_fk_query__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_group_class_study_group; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_group
    ADD CONSTRAINT fk_group_class_study_group FOREIGN KEY (study_group_class_id) REFERENCES study_group_class(study_group_class_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: fk_item_dat_fk_item_d_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT fk_item_dat_fk_item_d_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_dat_fk_item_d_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT fk_item_dat_fk_item_d_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_fk_item_f_item_ref; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item
    ADD CONSTRAINT fk_item_fk_item_f_item_ref FOREIGN KEY (item_reference_type_id) REFERENCES item_reference_type(item_reference_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_fk_item_i_item_dat; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item
    ADD CONSTRAINT fk_item_fk_item_i_item_dat FOREIGN KEY (item_data_type_id) REFERENCES item_data_type(item_data_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_fk_item_s_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item
    ADD CONSTRAINT fk_item_fk_item_s_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_fk_item_u_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item
    ADD CONSTRAINT fk_item_fk_item_u_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_gro_fk_item_g_item; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_group_map
    ADD CONSTRAINT fk_item_gro_fk_item_g_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_gro_fk_item_g_item_gro; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_group_map
    ADD CONSTRAINT fk_item_gro_fk_item_g_item_gro FOREIGN KEY (item_group_id) REFERENCES item_group(item_group_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_gro_fk_item_g_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_group
    ADD CONSTRAINT fk_item_gro_fk_item_g_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_gro_fk_item_g_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_group
    ADD CONSTRAINT fk_item_gro_fk_item_g_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_form_metadata
    ADD CONSTRAINT fk_item_id FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT;


--
-- Name: fk_item_reference_subject; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT fk_item_reference_subject FOREIGN KEY (event_crf_id) REFERENCES event_crf(event_crf_id) ON UPDATE RESTRICT;


--
-- Name: fk_person_role_study_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_user_role
    ADD CONSTRAINT fk_person_role_study_id FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT;


--
-- Name: fk_priv_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY role_privilege_map
    ADD CONSTRAINT fk_priv_id FOREIGN KEY (priv_id) REFERENCES privilege(priv_id) ON UPDATE RESTRICT;


--
-- Name: fk_project__reference_study2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk_project__reference_study2 FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT;


--
-- Name: fk_response_fk_respon_response; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY response_set
    ADD CONSTRAINT fk_response_fk_respon_response FOREIGN KEY (response_type_id) REFERENCES response_type(response_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_role_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY role_privilege_map
    ADD CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES user_role(role_id) ON UPDATE RESTRICT;


--
-- Name: fk_rs_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_form_metadata
    ADD CONSTRAINT fk_rs_id FOREIGN KEY (response_set_id) REFERENCES response_set(response_set_id) ON UPDATE RESTRICT;


--
-- Name: fk_sec_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY item_form_metadata
    ADD CONSTRAINT fk_sec_id FOREIGN KEY (section_id) REFERENCES section(section_id) ON UPDATE RESTRICT;


--
-- Name: fk_section_fk_sectio_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY section
    ADD CONSTRAINT fk_section_fk_sectio_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_section_fk_sectio_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY section
    ADD CONSTRAINT fk_section_fk_sectio_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_section_version; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY section
    ADD CONSTRAINT fk_section_version FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT;


--
-- Name: fk_study_ev_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk_study_ev_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_fk_study__study_ev; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk_study_ev_fk_study__study_ev FOREIGN KEY (study_event_definition_id) REFERENCES study_event_definition(study_event_definition_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk_study_ev_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_fk_studye_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_event_definition
    ADD CONSTRAINT fk_study_ev_fk_studye_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_fk_studye_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_event_definition
    ADD CONSTRAINT fk_study_ev_fk_studye_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_reference_study_su; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk_study_ev_reference_study_su FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study
    ADD CONSTRAINT fk_study_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study
    ADD CONSTRAINT fk_study_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_gr_fk_study__group_ty; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_group_class
    ADD CONSTRAINT fk_study_gr_fk_study__group_ty FOREIGN KEY (group_class_type_id) REFERENCES group_class_types(group_class_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_gr_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_group_class
    ADD CONSTRAINT fk_study_gr_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_gr_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_group_class
    ADD CONSTRAINT fk_study_gr_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_inst_reference; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_study_inst_reference FOREIGN KEY (crf_id) REFERENCES crf(crf_id) ON UPDATE RESTRICT;


--
-- Name: fk_study_reference_instrument; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_study_reference_instrument FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT;


--
-- Name: fk_study_reference_subject; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk_study_reference_subject FOREIGN KEY (subject_id) REFERENCES subject(subject_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: fk_study_su_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk_study_su_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_su_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk_study_su_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study
    ADD CONSTRAINT fk_study_type FOREIGN KEY (type_id) REFERENCES study_type(study_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_us_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_user_role
    ADD CONSTRAINT fk_study_us_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_us_study_use_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY study_user_role
    ADD CONSTRAINT fk_study_us_study_use_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__fk_sub_gr_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__fk_sub_gr_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__fk_subjec_group_ro; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__fk_subjec_group_ro FOREIGN KEY (study_group_id) REFERENCES study_group(study_group_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__fk_subjec_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__fk_subjec_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__fk_subjec_study_gr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__fk_subjec_study_gr FOREIGN KEY (study_group_class_id) REFERENCES study_group_class(study_group_class_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__subject_g_study_su; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__subject_g_study_su FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject_fk_subjec_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT fk_subject_fk_subjec_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject_fk_subjec_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT fk_subject_fk_subjec_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject_reference_instrument; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_subject_reference_instrument FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_user_acc_fk_user_f_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY user_account
    ADD CONSTRAINT fk_user_acc_fk_user_f_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_user_acc_ref_user__user_typ; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY user_account
    ADD CONSTRAINT fk_user_acc_ref_user__user_typ FOREIGN KEY (user_type_id) REFERENCES user_type(user_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_user_acc_status_re_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY user_account
    ADD CONSTRAINT fk_user_acc_status_re_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_versioni_fk_versio_crf_vers; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY versioning_map
    ADD CONSTRAINT fk_versioni_fk_versio_crf_vers FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_versioni_fk_versio_item; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY versioning_map
    ADD CONSTRAINT fk_versioni_fk_versio_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_versioni_reference_instrume; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY crf_version
    ADD CONSTRAINT fk_versioni_reference_instrume FOREIGN KEY (crf_id) REFERENCES crf(crf_id) ON UPDATE RESTRICT;


--
-- Name: fk_versioning_study_inst; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_versioning_study_inst FOREIGN KEY (default_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

