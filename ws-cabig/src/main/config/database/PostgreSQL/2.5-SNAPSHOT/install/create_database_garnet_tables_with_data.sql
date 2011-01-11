/* Creates the schema for OpenClinica version GARNET
   Authors: thicherson, bperry, jsampson, jxu, ywang
*/

--
-- PostgreSQL database dump
--

--SET client_encoding = 'SQL_ASCII';
CREATE PROCEDURAL LANGUAGE plpgsql;
SET check_function_bodies = false;
SET client_min_messages = error; --Don't show warning messages during installation
SET default_tablespace = '';
SET default_with_oids = true;
SET search_path = public, pg_catalog;
COMMENT ON SCHEMA public IS 'Standard public schema';


--
-- Name: archived_dataset_file; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE archived_dataset_file (
    archived_dataset_file_id serial NOT NULL,
    name character varying(255),
    dataset_id integer,
    export_format_id integer,
    file_reference character varying(1000),
    run_time integer,
    file_size integer,
    date_created date,
    owner_id integer
);


ALTER TABLE public.archived_dataset_file OWNER TO clinica;

--
-- Name: archived_dataset_file_archived_dataset_file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('archived_dataset_file', 'archived_dataset_file_id'), 1, false);


--
-- Name: audit_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE audit_event (
    audit_id serial NOT NULL,
    audit_date timestamp without time zone NOT NULL,
    audit_table character varying(500) NOT NULL,
    user_id integer,
    entity_id integer,
    reason_for_change character varying(1000),
    action_message character varying(4000)
);


ALTER TABLE public.audit_event OWNER TO clinica;

--
-- Name: audit_event_audit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('audit_event', 'audit_id'), 10, true);


--
-- Name: audit_event_context; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE audit_event_context (
    audit_id integer,
    study_id integer,
    subject_id integer,
    study_subject_id integer,
    role_name character varying(200),
    event_crf_id integer,
    study_event_id integer,
    study_event_definition_id integer,
    crf_id integer,
    crf_version_id integer,
    study_crf_id integer,
    item_id integer
);


ALTER TABLE public.audit_event_context OWNER TO clinica;

--
-- Name: audit_event_values; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE audit_event_values (
    audit_id integer,
    column_name character varying(255),
    old_value character varying(2000),
    new_value character varying(2000)
);


ALTER TABLE public.audit_event_values OWNER TO clinica;

--
-- Name: audit_log_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE audit_log_event (
    audit_id serial NOT NULL,
    audit_date timestamp without time zone NOT NULL,
    audit_table character varying(500) NOT NULL,
    user_id integer,
    entity_id integer,
    entity_name character varying(500),
    reason_for_change character varying(1000),
    audit_log_event_type_id integer,
    old_value character varying(2000),
    new_value character varying(2000),
    event_crf_id integer
);


ALTER TABLE public.audit_log_event OWNER TO clinica;

--
-- Name: audit_log_event_audit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('audit_log_event', 'audit_id'), 1, true);


--
-- Name: audit_log_event_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE audit_log_event_type (
    audit_log_event_type_id serial NOT NULL,
    name character varying(255)
);


ALTER TABLE public.audit_log_event_type OWNER TO clinica;

--
-- Name: audit_log_event_type_audit_log_event_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('audit_log_event_type', 'audit_log_event_type_id'), 1, false);


--
-- Name: completion_status; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE completion_status (
    completion_status_id serial NOT NULL,
    status_id integer,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.completion_status OWNER TO clinica;

--
-- Name: completion_status_completion_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('completion_status', 'completion_status_id'), 1, true);


--
-- Name: crf; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE crf (
    crf_id serial NOT NULL,
    status_id integer,
    name character varying(255),
    description character varying(2048),
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer
);


ALTER TABLE public.crf OWNER TO clinica;

--
-- Name: crf_crf_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('crf', 'crf_id'), 1, false);


--
-- Name: crf_version; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE crf_version (
    crf_version_id serial NOT NULL,
    crf_id integer NOT NULL,
    name character varying(255),
    description character varying(4000),
    revision_notes character varying(255),
    status_id integer,
    date_created date,
    date_updated date,
    owner_id integer,
    update_id integer
);


ALTER TABLE public.crf_version OWNER TO clinica;

--
-- Name: crf_version_crf_version_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('crf_version', 'crf_version_id'), 1, false);


--
-- Name: dataset; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dataset (
    dataset_id serial NOT NULL,
    study_id integer,
    status_id integer,
    name character varying(255),
    description character varying(2000),
    sql_statement text,
    num_runs integer,
    date_start date,
    date_end date,
    date_created date,
    date_updated date,
    date_last_run date,
    owner_id integer,
    approver_id integer,
    update_id integer,
    show_event_location boolean DEFAULT false,
    show_event_start boolean DEFAULT false,
    show_event_end boolean DEFAULT false,
    show_subject_dob boolean DEFAULT false,
    show_subject_gender boolean DEFAULT false,
    show_event_status boolean DEFAULT false,
    show_subject_status boolean DEFAULT false,
    show_subject_unique_id boolean DEFAULT false,
    show_subject_age_at_event boolean DEFAULT false,
    show_crf_status boolean DEFAULT false,
    show_crf_version boolean DEFAULT false,
    show_crf_int_name boolean DEFAULT false,
    show_crf_int_date boolean DEFAULT false,
    show_group_info boolean DEFAULT false,
    show_disc_info boolean DEFAULT false
);


ALTER TABLE public.dataset OWNER TO clinica;

--
-- Name: dataset_crf_version_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dataset_crf_version_map (
    dataset_id integer,
    event_definition_crf_id integer
);


ALTER TABLE public.dataset_crf_version_map OWNER TO clinica;

--
-- Name: dataset_dataset_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dataset', 'dataset_id'), 1, false);


--
-- Name: dataset_filter_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dataset_filter_map (
    dataset_id integer,
    filter_id integer,
    ordinal integer
);


ALTER TABLE public.dataset_filter_map OWNER TO clinica;

--
-- Name: dataset_study_group_class_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dataset_study_group_class_map (
    dataset_id integer NOT NULL,
    study_group_class_id integer NOT NULL
);


ALTER TABLE public.dataset_study_group_class_map OWNER TO clinica;

--
-- Name: dc_computed_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dc_computed_event (
    dc_summary_event_id serial NOT NULL,
    dc_event_id integer NOT NULL,
    item_target_id integer,
    summary_type character varying(255)
);


ALTER TABLE public.dc_computed_event OWNER TO clinica;

--
-- Name: dc_computed_event_dc_summary_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_computed_event', 'dc_summary_event_id'), 1, false);


--
-- Name: dc_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dc_event (
    dc_event_id serial NOT NULL,
    decision_condition_id integer,
    ordinal integer NOT NULL,
    "type" character varying(256) NOT NULL
);


ALTER TABLE public.dc_event OWNER TO clinica;

--
-- Name: dc_event_dc_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_event', 'dc_event_id'), 1, false);


--
-- Name: dc_primitive; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dc_primitive (
    dc_primitive_id serial NOT NULL,
    decision_condition_id integer,
    item_id integer,
    dynamic_value_item_id integer,
    comparison character varying(3) NOT NULL,
    constant_value character varying(4000)
);


ALTER TABLE public.dc_primitive OWNER TO clinica;

--
-- Name: dc_primitive_dc_primitive_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_primitive', 'dc_primitive_id'), 1, false);


--
-- Name: dc_section_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dc_section_event (
    dc_event_id serial NOT NULL,
    section_id integer NOT NULL
);


ALTER TABLE public.dc_section_event OWNER TO clinica;

--
-- Name: dc_section_event_dc_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_section_event', 'dc_event_id'), 1, false);


--
-- Name: dc_send_email_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dc_send_email_event (
    dc_event_id serial NOT NULL,
    to_address character varying(1000) NOT NULL,
    subject character varying(1000),
    body character varying(4000)
);


ALTER TABLE public.dc_send_email_event OWNER TO clinica;

--
-- Name: dc_send_email_event_dc_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_send_email_event', 'dc_event_id'), 1, false);


--
-- Name: dc_substitution_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dc_substitution_event (
    dc_event_id serial NOT NULL,
    item_id integer,
    value character varying(1000) NOT NULL
);


ALTER TABLE public.dc_substitution_event OWNER TO clinica;

--
-- Name: dc_substitution_event_dc_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_substitution_event', 'dc_event_id'), 1, false);


--
-- Name: dc_summary_item_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dc_summary_item_map (
    dc_summary_event_id integer,
    item_id integer,
    ordinal integer
);


ALTER TABLE public.dc_summary_item_map OWNER TO clinica;

--
-- Name: decision_condition; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE decision_condition (
    decision_condition_id serial NOT NULL,
    crf_version_id integer,
    status_id integer,
    label character varying(1000) NOT NULL,
    comments character varying(3000) NOT NULL,
    quantity integer NOT NULL,
    "type" character varying(3) NOT NULL,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer
);


ALTER TABLE public.decision_condition OWNER TO clinica;

--
-- Name: decision_condition_decision_condition_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('decision_condition', 'decision_condition_id'), 1, false);


--
-- Name: discrepancy_note; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE discrepancy_note (
    discrepancy_note_id serial NOT NULL,
    description character varying(255),
    discrepancy_note_type_id integer,
    resolution_status_id integer,
    detailed_notes character varying(1000),
    date_created date,
    owner_id integer,
    parent_dn_id integer,
    entity_type character varying(30),
    study_id integer
);


ALTER TABLE public.discrepancy_note OWNER TO clinica;

--
-- Name: discrepancy_note_discrepancy_note_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('discrepancy_note', 'discrepancy_note_id'), 1, false);


--
-- Name: discrepancy_note_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE discrepancy_note_type (
    discrepancy_note_type_id serial NOT NULL,
    name character varying(50),
    description character varying(255)
);


ALTER TABLE public.discrepancy_note_type OWNER TO clinica;

--
-- Name: discrepancy_note_type_discrepancy_note_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('discrepancy_note_type', 'discrepancy_note_type_id'), 1, false);


SET default_with_oids = false;

--
-- Name: dn_event_crf_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_event_crf_map (
    event_crf_id integer,
    discrepancy_note_id integer,
    column_name character varying(255)
);


ALTER TABLE public.dn_event_crf_map OWNER TO clinica;

--
-- Name: dn_item_data_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_item_data_map (
    item_data_id integer,
    discrepancy_note_id integer,
    column_name character varying(255)
);


ALTER TABLE public.dn_item_data_map OWNER TO clinica;

--
-- Name: dn_study_event_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_study_event_map (
    study_event_id integer,
    discrepancy_note_id integer,
    column_name character varying(255)
);


ALTER TABLE public.dn_study_event_map OWNER TO clinica;

--
-- Name: dn_study_subject_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_study_subject_map (
    study_subject_id integer,
    discrepancy_note_id integer,
    column_name character varying(255)
);


ALTER TABLE public.dn_study_subject_map OWNER TO clinica;

SET default_with_oids = true;

--
-- Name: dn_subject_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE dn_subject_map (
    subject_id integer,
    discrepancy_note_id integer,
    column_name character varying(255)
);


ALTER TABLE public.dn_subject_map OWNER TO clinica;

--
-- Name: event_crf; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE event_crf (
    event_crf_id serial NOT NULL,
    study_event_id integer,
    crf_version_id integer,
    date_interviewed date,
    interviewer_name character varying(255),
    completion_status_id integer,
    status_id integer,
    annotations character varying(4000),
    date_completed timestamp without time zone,
    validator_id integer,
    date_validate date,
    date_validate_completed timestamp without time zone,
    validator_annotations character varying(4000),
    validate_string character varying(256),
    owner_id integer,
    date_created date,
    study_subject_id integer,
    date_updated date,
    update_id integer
);


ALTER TABLE public.event_crf OWNER TO clinica;

--
-- Name: event_crf_event_crf_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('event_crf', 'event_crf_id'), 1, false);


--
-- Name: event_definition_crf; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE event_definition_crf (
    event_definition_crf_id serial NOT NULL,
    study_event_definition_id integer,
    study_id integer,
    crf_id integer,
    required_crf boolean,
    double_entry boolean,
    require_all_text_filled boolean,
    decision_conditions boolean,
    null_values character varying(255),
    default_version_id integer,
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    ordinal integer
);


ALTER TABLE public.event_definition_crf OWNER TO clinica;

--
-- Name: event_definition_crf_event_definition_crf_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('event_definition_crf', 'event_definition_crf_id'), 1, false);


--
-- Name: export_format; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE export_format (
    export_format_id serial NOT NULL,
    name character varying(255),
    description character varying(1000),
    mime_type character varying(255)
);


ALTER TABLE public.export_format OWNER TO clinica;

--
-- Name: export_format_export_format_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('export_format', 'export_format_id'), 1, false);


--
-- Name: filter; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE filter (
    filter_id serial NOT NULL,
    name character varying(255),
    description character varying(2000),
    sql_statement text,
    status_id integer,
    date_created date,
    date_updated date,
    owner_id integer NOT NULL,
    update_id integer
);


ALTER TABLE public.filter OWNER TO clinica;

--
-- Name: filter_crf_version_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE filter_crf_version_map (
    filter_id integer,
    crf_version_id integer
);


ALTER TABLE public.filter_crf_version_map OWNER TO clinica;

--
-- Name: filter_filter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('filter', 'filter_id'), 1, false);


--
-- Name: group_class_types; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE group_class_types (
    group_class_type_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.group_class_types OWNER TO clinica;

--
-- Name: group_class_types_group_class_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('group_class_types', 'group_class_type_id'), 2, true);


--
-- Name: item; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE item (
    item_id serial NOT NULL,
    name character varying(255),
    description character varying(4000),
    units character varying(64),
    phi_status boolean,
    item_data_type_id integer,
    item_reference_type_id integer,
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer
);


ALTER TABLE public.item OWNER TO clinica;

--
-- Name: item_data; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE item_data (
    item_data_id serial NOT NULL,
    item_id integer NOT NULL,
    event_crf_id integer,
    status_id integer,
    value character varying(4000),
    date_created date,
    date_updated date,
    owner_id integer,
    update_id integer,
    ordinal integer
);


ALTER TABLE public.item_data OWNER TO clinica;

--
-- Name: item_data_item_data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_data', 'item_data_id'), 1, false);


--
-- Name: item_data_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
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
-- Name: item_data_type_item_data_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_data_type', 'item_data_type_id'), 9, true);


--
-- Name: item_form_metadata; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE item_form_metadata (
    item_form_metadata_id serial NOT NULL,
    item_id integer NOT NULL,
    crf_version_id integer,
    "header" character varying(2000),
    subheader character varying(240),
    parent_id integer,
    parent_label character varying(120),
    column_number integer,
    page_number_label character varying(5),
    question_number_label character varying(20),
    left_item_text character varying(4000),
    right_item_text character varying(2000),
    section_id integer NOT NULL,
    decision_condition_id integer,
    response_set_id integer NOT NULL,
    regexp character varying(1000),
    regexp_error_msg character varying(255),
    ordinal integer NOT NULL,
    required boolean,
    default_value character varying(4000),
    response_layout character varying(255)
);


ALTER TABLE public.item_form_metadata OWNER TO clinica;

--
-- Name: item_form_metadata_item_form_metadata_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_form_metadata', 'item_form_metadata_id'), 1, false);


--
-- Name: item_group; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE item_group (
    item_group_id serial NOT NULL,
    name character varying(255),
    crf_id integer NOT NULL,
    status_id integer,
    date_created date,
    date_updated date,
    owner_id integer,
    update_id integer
);


ALTER TABLE public.item_group OWNER TO clinica;

--
-- Name: item_group_item_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_group', 'item_group_id'), 1, false);


--
-- Name: item_group_metadata; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE item_group_metadata (
    item_group_metadata_id serial NOT NULL,
    item_group_id integer NOT NULL,
    "header" character varying(255),
    subheader character varying(255),
    layout character varying(100),
    repeat_number integer,
    repeat_max integer,
    repeat_array character varying(255),
    row_start_number integer,
    crf_version_id integer NOT NULL,
    item_id integer NOT NULL,
    ordinal integer NOT NULL,
    borders integer
);


ALTER TABLE public.item_group_metadata OWNER TO clinica;

--
-- Name: item_group_metadata_item_group_metadata_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_group_metadata', 'item_group_metadata_id'), 1, false);


--
-- Name: item_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item', 'item_id'), 1, false);


--
-- Name: item_reference_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE item_reference_type (
    item_reference_type_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.item_reference_type OWNER TO clinica;

--
-- Name: item_reference_type_item_reference_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_reference_type', 'item_reference_type_id'), 1, true);


--
-- Name: null_value_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
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
-- Name: null_value_type_null_value_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('null_value_type', 'null_value_type_id'), 11, true);


--
-- Name: openclinica_version; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE openclinica_version (
    name character varying(255),
    test_path character varying(1000)
);


ALTER TABLE public.openclinica_version OWNER TO clinica;

--
-- Name: privilege; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE privilege (
    priv_id serial NOT NULL,
    priv_name character varying(50),
    priv_desc character varying(2000)
);


ALTER TABLE public.privilege OWNER TO clinica;

--
-- Name: privilege_priv_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('privilege', 'priv_id'), 1, false);


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
-- Name: resolution_status_resolution_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('resolution_status', 'resolution_status_id'), 1, false);


--
-- Name: response_set; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE response_set (
    response_set_id serial NOT NULL,
    response_type_id integer,
    label character varying(80),
    options_text character varying(4000),
    options_values character varying(4000),
    version_id integer
);


ALTER TABLE public.response_set OWNER TO clinica;

--
-- Name: response_set_response_set_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('response_set', 'response_set_id'), 1, false);


--
-- Name: response_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE response_type (
    response_type_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.response_type OWNER TO clinica;

--
-- Name: response_type_response_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('response_type', 'response_type_id'), 7, true);


--
-- Name: role_privilege_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE role_privilege_map (
    role_id integer NOT NULL,
    priv_id integer NOT NULL,
    priv_value character varying(50)
);


ALTER TABLE public.role_privilege_map OWNER TO clinica;

--
-- Name: section; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE section (
    section_id serial NOT NULL,
    crf_version_id integer NOT NULL,
    status_id integer,
    label character varying(2000),
    title character varying(2000),
    subtitle character varying(2000),
    instructions character varying(2000),
    page_number_label character varying(5),
    ordinal integer,
    parent_id integer,
    date_created date,
    date_updated date,
    owner_id integer NOT NULL,
    update_id integer,
    borders integer
);


ALTER TABLE public.section OWNER TO clinica;

--
-- Name: section_section_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('section', 'section_id'), 1, false);


--
-- Name: status; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE status (
    status_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.status OWNER TO clinica;

--
-- Name: status_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('status', 'status_id'), 1, false);


--
-- Name: study; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study (
    study_id serial NOT NULL,
    parent_study_id integer,
    unique_identifier character varying(30),
    secondary_identifier character varying(255),
    name character varying(255),
    summary character varying(255),
    date_planned_start date,
    date_planned_end date,
    date_created date,
    date_updated date,
    owner_id integer,
    update_id integer,
    type_id integer,
    status_id integer,
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
    expected_total_enrollment integer,
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
    results_reference boolean
);


ALTER TABLE public.study OWNER TO clinica;

--
-- Name: study_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study_event (
    study_event_id serial NOT NULL,
    study_event_definition_id integer,
    study_subject_id integer,
    "location" character varying(2000),
    sample_ordinal integer,
    date_start timestamp without time zone,
    date_end timestamp without time zone,
    owner_id integer,
    status_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    subject_event_status_id integer,
    start_time_flag boolean,
    end_time_flag boolean
);


ALTER TABLE public.study_event OWNER TO clinica;

--
-- Name: study_event_definition; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study_event_definition (
    study_event_definition_id serial NOT NULL,
    study_id integer,
    name character varying(2000),
    description character varying(2000),
    repeating boolean,
    "type" character varying(20),
    category character varying(2000),
    owner_id integer,
    status_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    ordinal integer
);


ALTER TABLE public.study_event_definition OWNER TO clinica;

--
-- Name: study_event_definition_study_event_definition_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_event_definition', 'study_event_definition_id'), 1, false);


--
-- Name: study_event_study_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_event', 'study_event_id'), 1, false);


--
-- Name: study_group; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study_group (
    study_group_id serial NOT NULL,
    name character varying(255),
    description character varying(1000),
    study_group_class_id integer
);


ALTER TABLE public.study_group OWNER TO clinica;

--
-- Name: study_group_class; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study_group_class (
    study_group_class_id serial NOT NULL,
    name character varying(30),
    study_id integer,
    owner_id integer,
    date_created date,
    group_class_type_id integer,
    status_id integer,
    date_updated date,
    update_id integer,
    subject_assignment character varying(30)
);


ALTER TABLE public.study_group_class OWNER TO clinica;

--
-- Name: study_group_class_study_group_class_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_group_class', 'study_group_class_id'), 1, false);


--
-- Name: study_group_study_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_group', 'study_group_id'), 1, false);


--
-- Name: study_parameter; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study_parameter (
    study_parameter_id serial NOT NULL,
    handle character varying(50),
    name character varying(50),
    description character varying(255),
    default_value character varying(50),
    inheritable boolean DEFAULT true,
    overridable boolean
);


ALTER TABLE public.study_parameter OWNER TO clinica;

--
-- Name: study_parameter_study_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_parameter', 'study_parameter_id'), 1, false);


--
-- Name: study_parameter_value; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study_parameter_value (
    study_parameter_value_id serial NOT NULL,
    study_id integer,
    value character varying(50),
    parameter character varying(50)
);


ALTER TABLE public.study_parameter_value OWNER TO clinica;

--
-- Name: study_parameter_value_study_parameter_value_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_parameter_value', 'study_parameter_value_id'), 1, false);


--
-- Name: study_study_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study', 'study_id'), 1, true);


--
-- Name: study_subject; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study_subject (
    study_subject_id serial NOT NULL,
    label character varying(30),
    secondary_label character varying(30),
    subject_id integer,
    study_id integer,
    status_id integer,
    enrollment_date date,
    date_created date,
    date_updated date,
    owner_id integer,
    update_id integer
);


ALTER TABLE public.study_subject OWNER TO clinica;

--
-- Name: study_subject_study_subject_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_subject', 'study_subject_id'), 1, false);


--
-- Name: study_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study_type (
    study_type_id serial NOT NULL,
    name character varying(255),
    description character varying(1000)
);


ALTER TABLE public.study_type OWNER TO clinica;

--
-- Name: study_type_study_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_type', 'study_type_id'), 1, false);


--
-- Name: study_user_role; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE study_user_role (
    role_name character varying(40),
    study_id integer,
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    user_name character varying(40)
);


ALTER TABLE public.study_user_role OWNER TO clinica;

--
-- Name: subject; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE subject (
    subject_id serial NOT NULL,
    father_id integer,
    mother_id integer,
    status_id integer,
    date_of_birth date,
    gender character(1),
    unique_identifier character varying(255),
    date_created date,
    owner_id integer,
    date_updated date,
    update_id integer,
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
-- Name: subject_event_status_subject_event_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('subject_event_status', 'subject_event_status_id'), 1, false);


--
-- Name: subject_group_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE subject_group_map (
    subject_group_map_id serial NOT NULL,
    study_group_class_id integer,
    study_subject_id integer,
    study_group_id integer,
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    notes character varying(255)
);


ALTER TABLE public.subject_group_map OWNER TO clinica;

--
-- Name: subject_group_map_subject_group_map_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('subject_group_map', 'subject_group_map_id'), 1, false);


--
-- Name: subject_subject_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('subject', 'subject_id'), 1, false);


--
-- Name: user_account; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE user_account (
    user_id serial NOT NULL,
    user_name character varying(64),
    passwd character varying(255),
    first_name character varying(50),
    last_name character varying(50),
    email character varying(120),
    active_study integer,
    institutional_affiliation character varying(255),
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    date_lastvisit timestamp without time zone,
    passwd_timestamp date,
    passwd_challenge_question character varying(64),
    passwd_challenge_answer character varying(255),
    phone character varying(64),
    user_type_id integer,
    update_id integer
);


ALTER TABLE public.user_account OWNER TO clinica;

--
-- Name: user_account_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('user_account', 'user_id'), 3, true);


--
-- Name: user_role; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE user_role (
    role_id serial NOT NULL,
    role_name character varying(50) NOT NULL,
    parent_id integer,
    role_desc character varying(2000)
);


ALTER TABLE public.user_role OWNER TO clinica;

--
-- Name: user_role_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('user_role', 'role_id'), 6, true);


--
-- Name: user_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE user_type (
    user_type_id serial NOT NULL,
    user_type character varying(50)
);


ALTER TABLE public.user_type OWNER TO clinica;

--
-- Name: user_type_user_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('user_type', 'user_type_id'), 1, false);


--
-- Name: versioning_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace: 
--

CREATE TABLE versioning_map (
    crf_version_id integer,
    item_id integer
);


ALTER TABLE public.versioning_map OWNER TO clinica;

--
-- Data for Name: archived_dataset_file; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_event_context; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_event_values; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_log_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: audit_log_event_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

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


--
-- Data for Name: completion_status; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO completion_status (completion_status_id, status_id, name, description) VALUES (1, 1, 'completion status', 'place filler for completion status');


--
-- Data for Name: crf; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: crf_version; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dataset; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dataset_crf_version_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dataset_filter_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dataset_study_group_class_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_computed_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_primitive; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_section_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_send_email_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_substitution_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dc_summary_item_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: decision_condition; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: discrepancy_note; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: discrepancy_note_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (1, 'Failed Validation Check', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (2, 'Incomplete', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (3, 'Unclear/Unreadable', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (4, 'Annotation', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (5, 'Other', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (6, 'Query', '');
INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES (7, 'Reason for Change', '');


--
-- Data for Name: dn_event_crf_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dn_item_data_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dn_study_event_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dn_study_subject_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: dn_subject_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: event_crf; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: event_definition_crf; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: export_format; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (1, 'text/plain', 'Default export format for tab-delimited text', 'text/plain');
INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (2, 'text/plain', 'Default export format for comma-delimited text', 'text/plain');
INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (3, 'application/vnd.ms-excel', 'Default export format for Excel files', 'application/vnd.ms-excel');
INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (4, 'text/plain', 'Default export format for CDISC ODM XML files', 'text/plain');
INSERT INTO export_format (export_format_id, name, description, mime_type) VALUES (5, 'text/plain', 'Default export format for SAS files', 'text/plain');



--
-- Data for Name: filter; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: filter_crf_version_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: group_class_types; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO group_class_types (group_class_type_id, name, description) VALUES (1, 'Arm', NULL);
INSERT INTO group_class_types (group_class_type_id, name, description) VALUES (2, 'Family/Pedigree', NULL);
INSERT INTO group_class_types (group_class_type_id, name, description) VALUES (3, 'Demographic', NULL);
INSERT INTO group_class_types (group_class_type_id, name, description) VALUES (4, 'Other', NULL);



--
-- Data for Name: item; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_data; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_data_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (1, 'BL', 'Boolean', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (2, 'BN', 'BooleanNonNull', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (3, 'ED', 'Encapsulated Data', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (4, 'TEL', 'A telecommunication address', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (5, 'ST', 'Character String', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (6, 'INT', 'Integer', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (7, 'REAL', 'Floating', NULL, NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (8, 'SET', NULL, 'a value that contains other distinct values', NULL);
INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES (9, 'DATE', 'date', 'date', NULL);



--
-- Data for Name: item_form_metadata; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_group; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_group_metadata; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: item_reference_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO item_reference_type (item_reference_type_id, name, description) VALUES (1, 'literal', NULL);


--
-- Data for Name: null_value_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (1, 'NI', 'NoInformation', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (2, 'NA', 'not applicable', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (3, 'UNK', 'unknown', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (4, 'NASK', 'not asked', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (5, 'ASKU', 'asked but unknown', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (6, 'NAV', 'temporarily unavailable', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (7, 'OTH', 'other', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (8, 'PINF', 'positive infinity', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (9, 'NINF', 'negative infinity', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (10, 'MSK', 'masked', NULL, NULL);
INSERT INTO null_value_type (null_value_type_id, code, name, definition, reference) VALUES (11, 'NP', 'not present', NULL, NULL);


--
-- Data for Name: openclinica_version; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: privilege; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: resolution_status; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO resolution_status (resolution_status_id, name, description) VALUES (1, 'New', '');
INSERT INTO resolution_status (resolution_status_id, name, description) VALUES (2, 'Updated', '');
INSERT INTO resolution_status (resolution_status_id, name, description) VALUES (3, 'Resolution Proposed', '');
INSERT INTO resolution_status (resolution_status_id, name, description) VALUES (4, 'Closed', '');
INSERT INTO resolution_status (resolution_status_id, name, description) VALUES (5, 'Not Applicable', '');


--
-- Data for Name: response_set; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: response_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO response_type (response_type_id, name, description) VALUES (1, 'text', 'free form text entry limited to one line');
INSERT INTO response_type (response_type_id, name, description) VALUES (2, 'textarea', 'free form text area display');
INSERT INTO response_type (response_type_id, name, description) VALUES (3, 'checkbox', 'selecting one from many options');
INSERT INTO response_type (response_type_id, name, description) VALUES (4, 'file', 'for upload of files');
INSERT INTO response_type (response_type_id, name, description) VALUES (5, 'radio', 'selecting one from many options');
INSERT INTO response_type (response_type_id, name, description) VALUES (6, 'single-select', 'pick one from a list');
INSERT INTO response_type (response_type_id, name, description) VALUES (7, 'multi-select', 'pick many from a list');


--
-- Data for Name: role_privilege_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: section; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: status; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO status (status_id, name, description) VALUES (1, 'available', 'this is the active status');
INSERT INTO status (status_id, name, description) VALUES (2, 'unavailable', 'this is the inactive status');
INSERT INTO status (status_id, name, description) VALUES (3, 'private', NULL);
INSERT INTO status (status_id, name, description) VALUES (4, 'pending', NULL);
INSERT INTO status (status_id, name, description) VALUES (5, 'removed', 'this indicates that a record is specifically removed by user');
INSERT INTO status (status_id, name, description) VALUES (6, 'locked', NULL);
INSERT INTO status (status_id, name, description) VALUES (7, 'auto-removed','this indicates that a record is removed due to the removal of its parent record');
INSERT INTO status (status_id, name, description) VALUES (8, 'signed','this indicates all StudyEvents has been signed');



--
-- Data for Name: study; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study (study_id, parent_study_id, unique_identifier, secondary_identifier, name, summary, date_planned_start, date_planned_end, date_created, date_updated, owner_id, update_id, type_id, status_id, principal_investigator, facility_name, facility_city, facility_state, facility_zip, facility_country, facility_recruitment_status, facility_contact_name, facility_contact_degree, facility_contact_phone, facility_contact_email, protocol_type, protocol_description, protocol_date_verification, phase, expected_total_enrollment, sponsor, collaborators, medline_identifier, url, url_description, conditions, keywords, eligibility, gender, age_max, age_min, healthy_volunteer_accepted, purpose, allocation, masking, control, "assignment", endpoint, interventions, duration, selection, timing, official_title, results_reference) VALUES (1, NULL, 'default-study', 'default-study', 'Default Study', '', '2006-10-23', '2006-10-23', '2006-10-23', '2006-10-23', 1, NULL, 1, 1, 'default', '', '', '', '', '', '', '', '', '', '', 'observational', '', '2006-10-23', 'default', 0, 'default', '', '', '', '', '', '', '', 'both', '', '', false, 'Natural History', '', '', '', '', '', '', 'longitudinal', 'Convenience Sample', 'Retrospective', '', false);


--
-- Data for Name: study_event; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: study_event_definition; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: study_group; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: study_group_class; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: study_parameter; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (1, 'collectDob', 'collect subject''s date of birth', 'In study creation, Subject Birthdate can be set to require collect full birthdate, year of birth, or not used', 'required', true, false);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (2, 'discrepancyManagement', '', '', 'true', true, false);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (3, 'subjectPersonIdRequired', '', 'In study creation, Person ID can be set to required, optional, or not used', 'required', true, false);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (4, 'genderRequired', '', 'In study creation, Subject Gender can be set to required or not used', 'required', true, false);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (5, 'subjectIdGeneration', '', 'In study creation, Study Subject ID can be set to Manual Entry, Auto-generate (editable), Auto-generate (non-editable)', 'manual', true, false);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (6, 'subjectIdPrefixSuffix', '', 'In study and/or site creation, if Study Subject ID is set to Auto-generate, user can optionally specify a prefix and suffix for the format of the ID, using the format [PRETEXT][AUTO#][POSTTEXT]', 'false', true, false);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (7, 'interviewerNameRequired', '', 'In study or site creation, CRF Interviewer Name can be set as optional or required fields', 'required', true, true);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (8, 'interviewerNameDefault', '', 'In study or site creation, CRF Interviewer Name can be set to default to blank or to be pre-populated with user''s name and the date of the study event', 'blank', true, true);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (9, 'interviewerNameEditable', '', 'In study creation, CRF Interviewer Name can be set to editable or not editable', 'editable', true, false);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (10, 'interviewDateRequired', '', 'In study or site creation, CRF Interviewer Date can be set as optional or required fields', 'required', true, true);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (11, 'interviewDateDefault', '', 'In study or site creation, CRF Interviewer Date can be set to default to blank or to be pre-populated with user''s name and the date of the study event', 'eventDate', true, true);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (12, 'interviewDateEditable', '', 'In study creation, CRF Interview Name and Date can be set to editable or not editable', 'editable', true, false);
INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES (13, 'personIdShownOnCRF', '', '', 'false', true, false);


--
-- Data for Name: study_parameter_value; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (1, 1, '1', 'collectDob');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (2, 1, 'true', 'discrepancyManagement');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (3, 1, 'true', 'genderRequired');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (4, 1, 'required', 'subjectPersonIdRequired');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (5, 1, 'true', 'interviewerNameRequired');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (6, 1, 'blank', 'interviewerNameDefault');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (7, 1, 'true', 'interviewerNameEditable');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (8, 1, 'true', 'interviewDateRequired');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (9, 1, 'blank', 'interviewDateDefault');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (10, 1, 'true', 'interviewDateEditable');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (11, 1, 'manual', 'subjectIdGeneration');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (12, 1, '', 'subjectIdPrefixSuffix');
INSERT INTO study_parameter_value (study_parameter_value_id, study_id, value, parameter) VALUES (13, 1, 'true', 'personIdShownOnCRF');


--
-- Data for Name: study_subject; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: study_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study_type (study_type_id, name, description) VALUES (1, 'genetic', NULL);
INSERT INTO study_type (study_type_id, name, description) VALUES (2, 'non-genetic', NULL);


--
-- Data for Name: study_user_role; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO study_user_role (role_name, study_id, status_id, owner_id, date_created, date_updated, update_id, user_name) VALUES ('admin', 1, 1, 1, '2006-10-23', NULL, NULL, 'root');
INSERT INTO study_user_role (role_name, study_id, status_id, owner_id, date_created, date_updated, update_id, user_name) VALUES ('director', 1, 1, 1, '2006-10-23', NULL, NULL, 'root');


--
-- Data for Name: subject; Type: TABLE DATA; Schema: public; Owner: clinica
--



--
-- Data for Name: subject_event_status; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (1, 'scheduled', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (2, 'not scheduled', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (3, 'data entry started', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (4, 'completed', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (5, 'stopped', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (6, 'skipped', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (7, 'locked', '');
INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES (8, 'signed', '');


--
-- Data for Name: subject_group_map; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO user_account (user_id, user_name, passwd, first_name, last_name, email, active_study, institutional_affiliation, status_id, owner_id, date_created, date_updated, date_lastvisit, passwd_timestamp, passwd_challenge_question, passwd_challenge_answer, phone, user_type_id, update_id) VALUES (1, 'root', '25d55ad283aa400af464c76d713c07ad', 'Root', 'User', 'openclinica_admin@example.com', 1, 'Akaza Research', 1, 1, NULL, '2006-10-23', '2006-10-23 16:46:44.942', '2006-10-23', NULL, NULL, '617 621 8585', 3, 1);


--
-- Data for Name: user_account; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Data for Name: user_role; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (1, 'admin', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (2, 'coordinator', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (3, 'director', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (4, 'investigator', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (5, 'ra', 1, NULL);
INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES (6, 'monitor', 1, NULL);


--
-- Data for Name: user_type; Type: TABLE DATA; Schema: public; Owner: clinica
--

INSERT INTO user_type (user_type_id, user_type) VALUES (1, 'admin');
INSERT INTO user_type (user_type_id, user_type) VALUES (2, 'user');
INSERT INTO user_type (user_type_id, user_type) VALUES (3, 'tech-admin');


--
-- Data for Name: versioning_map; Type: TABLE DATA; Schema: public; Owner: clinica
--




--
-- Name: audit_log_event_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY audit_log_event
    ADD CONSTRAINT audit_log_event_pkey PRIMARY KEY (audit_id);


--
-- Name: discrepancy_note_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY discrepancy_note
    ADD CONSTRAINT discrepancy_note_pkey PRIMARY KEY (discrepancy_note_id);


--
-- Name: discrepancy_note_type_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY discrepancy_note_type
    ADD CONSTRAINT discrepancy_note_type_pkey PRIMARY KEY (discrepancy_note_type_id);


--
-- Name: pk_answer; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT pk_answer PRIMARY KEY (item_data_id);


--
-- Name: pk_archived_dataset_file; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY archived_dataset_file
    ADD CONSTRAINT pk_archived_dataset_file PRIMARY KEY (archived_dataset_file_id);


--
-- Name: pk_audit_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY audit_event
    ADD CONSTRAINT pk_audit_event PRIMARY KEY (audit_id);


--
-- Name: pk_completion_status; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY completion_status
    ADD CONSTRAINT pk_completion_status PRIMARY KEY (completion_status_id);


--
-- Name: pk_dc_computed_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY dc_computed_event
    ADD CONSTRAINT pk_dc_computed_event PRIMARY KEY (dc_summary_event_id);


--
-- Name: pk_dc_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY dc_event
    ADD CONSTRAINT pk_dc_event PRIMARY KEY (dc_event_id);


--
-- Name: pk_dc_primitive; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY dc_primitive
    ADD CONSTRAINT pk_dc_primitive PRIMARY KEY (dc_primitive_id);


--
-- Name: pk_dc_section_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY dc_section_event
    ADD CONSTRAINT pk_dc_section_event PRIMARY KEY (dc_event_id);


--
-- Name: pk_dc_send_email_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY dc_send_email_event
    ADD CONSTRAINT pk_dc_send_email_event PRIMARY KEY (dc_event_id);


--
-- Name: pk_dc_substitution_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY dc_substitution_event
    ADD CONSTRAINT pk_dc_substitution_event PRIMARY KEY (dc_event_id);


--
-- Name: pk_decision_condition; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY decision_condition
    ADD CONSTRAINT pk_decision_condition PRIMARY KEY (decision_condition_id);


--
-- Name: pk_event_crf; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT pk_event_crf PRIMARY KEY (event_crf_id);


--
-- Name: pk_export_format; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY export_format
    ADD CONSTRAINT pk_export_format PRIMARY KEY (export_format_id);


--
-- Name: pk_group_role; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY study_group
    ADD CONSTRAINT pk_group_role PRIMARY KEY (study_group_id);


--
-- Name: pk_group_types; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY group_class_types
    ADD CONSTRAINT pk_group_types PRIMARY KEY (group_class_type_id);


--
-- Name: pk_individual; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT pk_individual PRIMARY KEY (subject_id);


--
-- Name: pk_instrument; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY crf
    ADD CONSTRAINT pk_instrument PRIMARY KEY (crf_id);


--
-- Name: pk_item; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY item
    ADD CONSTRAINT pk_item PRIMARY KEY (item_id);


--
-- Name: pk_item_data_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY item_data_type
    ADD CONSTRAINT pk_item_data_type PRIMARY KEY (item_data_type_id);


--
-- Name: pk_item_group; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY item_group
    ADD CONSTRAINT pk_item_group PRIMARY KEY (item_group_id);


--
-- Name: pk_item_group_metadata; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY item_group_metadata
    ADD CONSTRAINT pk_item_group_metadata PRIMARY KEY (item_group_metadata_id);


--
-- Name: pk_item_reference_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY item_reference_type
    ADD CONSTRAINT pk_item_reference_type PRIMARY KEY (item_reference_type_id);


--
-- Name: pk_null_value_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY null_value_type
    ADD CONSTRAINT pk_null_value_type PRIMARY KEY (null_value_type_id);


--
-- Name: pk_person_user; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY user_account
    ADD CONSTRAINT pk_person_user PRIMARY KEY (user_id);


--
-- Name: pk_pl_metadata_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY item_form_metadata
    ADD CONSTRAINT pk_pl_metadata_id PRIMARY KEY (item_form_metadata_id);


--
-- Name: pk_priv_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY privilege
    ADD CONSTRAINT pk_priv_id PRIMARY KEY (priv_id);


--
-- Name: pk_project; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY study
    ADD CONSTRAINT pk_project PRIMARY KEY (study_id);


--
-- Name: pk_project_family; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY study_group_class
    ADD CONSTRAINT pk_project_family PRIMARY KEY (study_group_class_id);


--
-- Name: pk_project_individual; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT pk_project_individual PRIMARY KEY (study_subject_id);


--
-- Name: pk_project_instrument; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT pk_project_instrument PRIMARY KEY (event_definition_crf_id);


--
-- Name: pk_query_library; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY filter
    ADD CONSTRAINT pk_query_library PRIMARY KEY (filter_id);


--
-- Name: pk_report_library; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT pk_report_library PRIMARY KEY (dataset_id);


--
-- Name: pk_response_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY response_type
    ADD CONSTRAINT pk_response_type PRIMARY KEY (response_type_id);


--
-- Name: pk_role_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT pk_role_id PRIMARY KEY (role_id);


--
-- Name: pk_rs_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY response_set
    ADD CONSTRAINT pk_rs_id PRIMARY KEY (response_set_id);


--
-- Name: pk_section_id; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY section
    ADD CONSTRAINT pk_section_id PRIMARY KEY (section_id);


--
-- Name: pk_status; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY status
    ADD CONSTRAINT pk_status PRIMARY KEY (status_id);


--
-- Name: pk_study_event; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT pk_study_event PRIMARY KEY (study_event_id);


--
-- Name: pk_study_event_definition; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY study_event_definition
    ADD CONSTRAINT pk_study_event_definition PRIMARY KEY (study_event_definition_id);


--
-- Name: pk_study_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY study_type
    ADD CONSTRAINT pk_study_type PRIMARY KEY (study_type_id);


--
-- Name: pk_subject_event_status; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY subject_event_status
    ADD CONSTRAINT pk_subject_event_status PRIMARY KEY (subject_event_status_id);


--
-- Name: pk_subject_group_map; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT pk_subject_group_map PRIMARY KEY (subject_group_map_id);


--
-- Name: pk_user_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY user_type
    ADD CONSTRAINT pk_user_type PRIMARY KEY (user_type_id);


--
-- Name: pk_versioning; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY crf_version
    ADD CONSTRAINT pk_versioning PRIMARY KEY (crf_version_id);


--
-- Name: pkey_audit_log_event_type; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY audit_log_event_type
    ADD CONSTRAINT pkey_audit_log_event_type PRIMARY KEY (audit_log_event_type_id);


--
-- Name: resolution_status_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY resolution_status
    ADD CONSTRAINT resolution_status_pkey PRIMARY KEY (resolution_status_id);


--
-- Name: study_parameter_handle_key; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY study_parameter
    ADD CONSTRAINT study_parameter_handle_key UNIQUE (handle);


--
-- Name: study_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: clinica; Tablespace: 
--

ALTER TABLE ONLY study_parameter
    ADD CONSTRAINT study_parameter_pkey PRIMARY KEY (study_parameter_id);


--
-- Name: i_audit_event_audit_table; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_audit_event_audit_table ON audit_event USING btree (audit_table);


--
-- Name: i_audit_event_context_audit_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_audit_event_context_audit_id ON audit_event_context USING btree (audit_id);


--
-- Name: i_audit_event_context_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_audit_event_context_study_id ON audit_event_context USING btree (study_id);


--
-- Name: i_audit_event_entity_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_audit_event_entity_id ON audit_event USING btree (entity_id);


--
-- Name: i_audit_event_user_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_audit_event_user_id ON audit_event USING btree (user_id);


--
-- Name: i_audit_event_values_audit_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_audit_event_values_audit_id ON audit_event_values USING btree (audit_id);


--
-- Name: i_crf_crf_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_crf_crf_id ON crf USING btree (crf_id);


--
-- Name: i_crf_version_crf_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_crf_version_crf_id ON crf_version USING btree (crf_id);


--
-- Name: i_crf_version_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_crf_version_crf_version_id ON crf_version USING btree (crf_version_id);


--
-- Name: i_dataset_dataset_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_dataset_dataset_id ON dataset USING btree (dataset_id);


--
-- Name: i_dataset_owner_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_dataset_owner_id ON dataset USING btree (owner_id);


--
-- Name: i_dataset_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_dataset_study_id ON dataset USING btree (study_id);


--
-- Name: i_event_crf_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_event_crf_crf_version_id ON event_crf USING btree (crf_version_id);


--
-- Name: i_event_crf_event_crf_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_event_crf_event_crf_id ON event_crf USING btree (event_crf_id);


--
-- Name: i_event_crf_study_event_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_event_crf_study_event_id ON event_crf USING btree (study_event_id);


--
-- Name: i_event_definition_crf_event_definition_crf_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_event_definition_crf_event_definition_crf_id ON event_definition_crf USING btree (event_definition_crf_id);


--
-- Name: i_event_definition_crf_study_event_definition_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_event_definition_crf_study_event_definition_id ON event_definition_crf USING btree (study_event_definition_id);


--
-- Name: i_item_form_metadata_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_item_form_metadata_crf_version_id ON item_form_metadata USING btree (crf_version_id);


--
-- Name: i_item_form_metadata_item_form_metadata_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_item_form_metadata_item_form_metadata_id ON item_form_metadata USING btree (item_form_metadata_id);


--
-- Name: i_item_form_metadata_item_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_item_form_metadata_item_id ON item_form_metadata USING btree (item_id);


--
-- Name: i_item_form_metadata_response_set_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_item_form_metadata_response_set_id ON item_form_metadata USING btree (response_set_id);


--
-- Name: i_item_item_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_item_item_id ON item USING btree (item_id);


--
-- Name: i_item_name; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_item_name ON item USING btree (name);


--
-- Name: i_response_set_response_set_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_response_set_response_set_id ON response_set USING btree (response_set_id);


--
-- Name: i_response_set_response_type_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_response_set_response_type_id ON response_set USING btree (response_type_id);


--
-- Name: i_response_type_response_type_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_response_type_response_type_id ON response_type USING btree (response_type_id);


--
-- Name: i_section_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_section_crf_version_id ON section USING btree (crf_version_id);


--
-- Name: i_section_ordinal; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_section_ordinal ON section USING btree (ordinal);


--
-- Name: i_section_parent_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_section_parent_id ON section USING btree (parent_id);


--
-- Name: i_section_section_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_section_section_id ON section USING btree (section_id);


--
-- Name: i_study_event_definition_study_event_definition_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_event_definition_study_event_definition_id ON study_event_definition USING btree (study_event_definition_id);


--
-- Name: i_study_event_definition_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_event_definition_study_id ON study_event_definition USING btree (study_id);


--
-- Name: i_study_event_study_event_definition_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_event_study_event_definition_id ON study_event USING btree (study_event_definition_id);


--
-- Name: i_study_event_study_event_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_event_study_event_id ON study_event USING btree (study_event_id);


--
-- Name: i_study_event_study_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_event_study_subject_id ON study_event USING btree (study_subject_id);


--
-- Name: i_study_group_class_study_group_class_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_group_class_study_group_class_id ON study_group_class USING btree (study_group_class_id);


--
-- Name: i_study_group_study_group_class_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_group_study_group_class_id ON study_group USING btree (study_group_class_id);


--
-- Name: i_study_group_study_group_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_group_study_group_id ON study_group USING btree (study_group_id);


--
-- Name: i_study_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_study_id ON study USING btree (study_id);


--
-- Name: i_study_subject_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_subject_study_id ON study_subject USING btree (study_id);


--
-- Name: i_study_subject_study_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_subject_study_subject_id ON study_subject USING btree (study_subject_id);


--
-- Name: i_study_subject_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_subject_subject_id ON study_subject USING btree (subject_id);


--
-- Name: i_study_user_role_study_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_user_role_study_id ON study_user_role USING btree (study_id);


--
-- Name: i_study_user_role_user_name; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_study_user_role_user_name ON study_user_role USING btree (user_name);


--
-- Name: i_subject_father_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_father_id ON subject USING btree (father_id);


--
-- Name: i_subject_group_map_study_group_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_group_map_study_group_id ON subject_group_map USING btree (study_group_id);


--
-- Name: i_subject_group_map_study_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_group_map_study_subject_id ON subject_group_map USING btree (study_subject_id);


--
-- Name: i_subject_group_map_subject_group_map_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_group_map_subject_group_map_id ON subject_group_map USING btree (subject_group_map_id);


--
-- Name: i_subject_mother_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_mother_id ON subject USING btree (mother_id);


--
-- Name: i_subject_subject_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_subject_subject_id ON subject USING btree (subject_id);


--
-- Name: i_user_account_user_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_user_account_user_id ON user_account USING btree (user_id);


--
-- Name: i_user_account_user_name; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_user_account_user_name ON user_account USING btree (user_name);


--
-- Name: i_versioning_map_crf_version_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_versioning_map_crf_version_id ON versioning_map USING btree (crf_version_id);


--
-- Name: i_versioning_map_item_id; Type: INDEX; Schema: public; Owner: clinica; Tablespace: 
--

CREATE INDEX i_versioning_map_item_id ON versioning_map USING btree (item_id);

--
-- Name: FK_STUDY_EV_FK-STUDY__STUDY; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_event_definition
    ADD CONSTRAINT "FK_STUDY_EV_FK-STUDY__STUDY" FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: HAS FATHER; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT "HAS FATHER" FOREIGN KEY (father_id) REFERENCES subject(subject_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: HAS MOTHER; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT "HAS MOTHER" FOREIGN KEY (mother_id) REFERENCES subject(subject_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: PROJECT IS CONTAINED WITHIN PA; Type: FK CONSTRAINT; Schema: public; Owner: clinica
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
-- Name: fk_answer_reference_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT fk_answer_reference_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT;


--
-- Name: fk_archived_reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY archived_dataset_file
    ADD CONSTRAINT fk_archived_reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_archived_reference_export_f; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY archived_dataset_file
    ADD CONSTRAINT fk_archived_reference_export_f FOREIGN KEY (export_format_id) REFERENCES export_format(export_format_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_audit_ev_reference_audit_ev; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY audit_event_context
    ADD CONSTRAINT fk_audit_ev_reference_audit_ev FOREIGN KEY (audit_id) REFERENCES audit_event(audit_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_audit_lo_ref_audit_lo; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY audit_event_values
    ADD CONSTRAINT fk_audit_lo_ref_audit_lo FOREIGN KEY (audit_id) REFERENCES audit_event(audit_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_completi_fk_comple_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY completion_status
    ADD CONSTRAINT fk_completi_fk_comple_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_crf_crf_user_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY crf
    ADD CONSTRAINT fk_crf_crf_user_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_crf_fk_crf_fk_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY crf
    ADD CONSTRAINT fk_crf_fk_crf_fk_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_crf_metadata; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_group_metadata
    ADD CONSTRAINT fk_crf_metadata FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_crf_vers_crf_versi_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY crf_version
    ADD CONSTRAINT fk_crf_vers_crf_versi_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_crf_vers_fk_crf_ve_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY crf_version
    ADD CONSTRAINT fk_crf_vers_fk_crf_ve_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset__ref_event_event_de; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dataset_crf_version_map
    ADD CONSTRAINT fk_dataset__ref_event_event_de FOREIGN KEY (event_definition_crf_id) REFERENCES event_definition_crf(event_definition_crf_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset__ref_study_group_class; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dataset_study_group_class_map
    ADD CONSTRAINT fk_dataset__ref_study_group_class FOREIGN KEY (study_group_class_id) REFERENCES study_group_class(study_group_class_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset__reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dataset_crf_version_map
    ADD CONSTRAINT fk_dataset__reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset__reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dataset_study_group_class_map
    ADD CONSTRAINT fk_dataset__reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_fk_datase_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT fk_dataset_fk_datase_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_fk_datase_study; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT fk_dataset_fk_datase_study FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_fk_datase_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT fk_dataset_fk_datase_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_reference_dataset; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dataset_filter_map
    ADD CONSTRAINT fk_dataset_reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dataset_reference_filter; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dataset_filter_map
    ADD CONSTRAINT fk_dataset_reference_filter FOREIGN KEY (filter_id) REFERENCES filter(filter_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_compu_fk_dc_com_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_computed_event
    ADD CONSTRAINT fk_dc_compu_fk_dc_com_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_event_fk_dc_eve_decision; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_event
    ADD CONSTRAINT fk_dc_event_fk_dc_eve_decision FOREIGN KEY (decision_condition_id) REFERENCES decision_condition(decision_condition_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_primi_fk_dc_pri_decision; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_primitive
    ADD CONSTRAINT fk_dc_primi_fk_dc_pri_decision FOREIGN KEY (decision_condition_id) REFERENCES decision_condition(decision_condition_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_primi_fk_dc_pri_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_primitive
    ADD CONSTRAINT fk_dc_primi_fk_dc_pri_item FOREIGN KEY (dynamic_value_item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_primi_fk_item_i_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_primitive
    ADD CONSTRAINT fk_dc_primi_fk_item_i_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_secti_fk_dc_sec_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_section_event
    ADD CONSTRAINT fk_dc_secti_fk_dc_sec_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_send__dc_send_e_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_send_email_event
    ADD CONSTRAINT fk_dc_send__dc_send_e_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_subst_fk_dc_sub_dc_event; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_substitution_event
    ADD CONSTRAINT fk_dc_subst_fk_dc_sub_dc_event FOREIGN KEY (dc_event_id) REFERENCES dc_event(dc_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_subst_fk_dc_sub_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_substitution_event
    ADD CONSTRAINT fk_dc_subst_fk_dc_sub_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_summa_fk_dc_sum_dc_compu; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_summary_item_map
    ADD CONSTRAINT fk_dc_summa_fk_dc_sum_dc_compu FOREIGN KEY (dc_summary_event_id) REFERENCES dc_computed_event(dc_summary_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_dc_summa_fk_dc_sum_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY dc_summary_item_map
    ADD CONSTRAINT fk_dc_summa_fk_dc_sum_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_decision_fk_decisi_crf_vers; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY decision_condition
    ADD CONSTRAINT fk_decision_fk_decisi_crf_vers FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_decision_fk_decisi_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY decision_condition
    ADD CONSTRAINT fk_decision_fk_decisi_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_decision_fk_decisi_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY decision_condition
    ADD CONSTRAINT fk_decision_fk_decisi_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_fk_event__completi; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__completi FOREIGN KEY (completion_status_id) REFERENCES completion_status(completion_status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_fk_event__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_fk_event__study_ev; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__study_ev FOREIGN KEY (study_event_id) REFERENCES study_event(study_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_fk_event__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_fk_event__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_cr_reference_study_su; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_event_cr_reference_study_su FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_de_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_event_de_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_de_reference_study_ev; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_event_de_reference_study_ev FOREIGN KEY (study_event_definition_id) REFERENCES study_event_definition(study_event_definition_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_event_de_study_crf_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_event_de_study_crf_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_filter_c_reference_crf_vers; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY filter_crf_version_map
    ADD CONSTRAINT fk_filter_c_reference_crf_vers FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_filter_c_reference_filter; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY filter_crf_version_map
    ADD CONSTRAINT fk_filter_c_reference_filter FOREIGN KEY (filter_id) REFERENCES filter(filter_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_filter_fk_query__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY filter
    ADD CONSTRAINT fk_filter_fk_query__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_filter_fk_query__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY filter
    ADD CONSTRAINT fk_filter_fk_query__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_group_class_study_group; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_group
    ADD CONSTRAINT fk_group_class_study_group FOREIGN KEY (study_group_class_id) REFERENCES study_group_class(study_group_class_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: fk_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_group_metadata
    ADD CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_dat_fk_item_d_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT fk_item_dat_fk_item_d_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_dat_fk_item_d_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT fk_item_dat_fk_item_d_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_fk_item_f_item_ref; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item
    ADD CONSTRAINT fk_item_fk_item_f_item_ref FOREIGN KEY (item_reference_type_id) REFERENCES item_reference_type(item_reference_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_fk_item_i_item_dat; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item
    ADD CONSTRAINT fk_item_fk_item_i_item_dat FOREIGN KEY (item_data_type_id) REFERENCES item_data_type(item_data_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_fk_item_s_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item
    ADD CONSTRAINT fk_item_fk_item_s_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_fk_item_u_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item
    ADD CONSTRAINT fk_item_fk_item_u_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_gro_fk_item_g_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_group
    ADD CONSTRAINT fk_item_gro_fk_item_g_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_gro_fk_item_g_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_group
    ADD CONSTRAINT fk_item_gro_fk_item_g_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_group; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_group_metadata
    ADD CONSTRAINT fk_item_group FOREIGN KEY (item_group_id) REFERENCES item_group(item_group_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_group_crf; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_group
    ADD CONSTRAINT fk_item_group_crf FOREIGN KEY (crf_id) REFERENCES crf(crf_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_item_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_form_metadata
    ADD CONSTRAINT fk_item_id FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT;


--
-- Name: fk_item_reference_subject; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_data
    ADD CONSTRAINT fk_item_reference_subject FOREIGN KEY (event_crf_id) REFERENCES event_crf(event_crf_id) ON UPDATE RESTRICT;


--
-- Name: fk_person_role_study_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_user_role
    ADD CONSTRAINT fk_person_role_study_id FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT;


--
-- Name: fk_priv_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY role_privilege_map
    ADD CONSTRAINT fk_priv_id FOREIGN KEY (priv_id) REFERENCES privilege(priv_id) ON UPDATE RESTRICT;


--
-- Name: fk_project__reference_study2; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk_project__reference_study2 FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT;


--
-- Name: fk_response_fk_respon_response; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY response_set
    ADD CONSTRAINT fk_response_fk_respon_response FOREIGN KEY (response_type_id) REFERENCES response_type(response_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_role_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY role_privilege_map
    ADD CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES user_role(role_id) ON UPDATE RESTRICT;


--
-- Name: fk_rs_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_form_metadata
    ADD CONSTRAINT fk_rs_id FOREIGN KEY (response_set_id) REFERENCES response_set(response_set_id) ON UPDATE RESTRICT;


--
-- Name: fk_sec_id; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY item_form_metadata
    ADD CONSTRAINT fk_sec_id FOREIGN KEY (section_id) REFERENCES section(section_id) ON UPDATE RESTRICT;


--
-- Name: fk_section_fk_sectio_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY section
    ADD CONSTRAINT fk_section_fk_sectio_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_section_fk_sectio_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY section
    ADD CONSTRAINT fk_section_fk_sectio_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_section_version; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY section
    ADD CONSTRAINT fk_section_version FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT;


--
-- Name: fk_study_ev_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk_study_ev_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_fk_study__study_ev; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk_study_ev_fk_study__study_ev FOREIGN KEY (study_event_definition_id) REFERENCES study_event_definition(study_event_definition_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk_study_ev_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_fk_studye_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_event_definition
    ADD CONSTRAINT fk_study_ev_fk_studye_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_fk_studye_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_event_definition
    ADD CONSTRAINT fk_study_ev_fk_studye_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_ev_reference_study_su; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_event
    ADD CONSTRAINT fk_study_ev_reference_study_su FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study
    ADD CONSTRAINT fk_study_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study
    ADD CONSTRAINT fk_study_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_gr_fk_study__group_ty; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_group_class
    ADD CONSTRAINT fk_study_gr_fk_study__group_ty FOREIGN KEY (group_class_type_id) REFERENCES group_class_types(group_class_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_gr_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_group_class
    ADD CONSTRAINT fk_study_gr_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_gr_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_group_class
    ADD CONSTRAINT fk_study_gr_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_inst_reference; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_study_inst_reference FOREIGN KEY (crf_id) REFERENCES crf(crf_id) ON UPDATE RESTRICT;


--
-- Name: fk_study_reference_instrument; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_study_reference_instrument FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT;


--
-- Name: fk_study_reference_subject; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk_study_reference_subject FOREIGN KEY (subject_id) REFERENCES subject(subject_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: fk_study_su_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk_study_su_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_su_fk_study__user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_subject
    ADD CONSTRAINT fk_study_su_fk_study__user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_type; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study
    ADD CONSTRAINT fk_study_type FOREIGN KEY (type_id) REFERENCES study_type(study_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_us_fk_study__status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_user_role
    ADD CONSTRAINT fk_study_us_fk_study__status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_study_us_study_use_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_user_role
    ADD CONSTRAINT fk_study_us_study_use_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__fk_sub_gr_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__fk_sub_gr_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__fk_subjec_group_ro; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__fk_subjec_group_ro FOREIGN KEY (study_group_id) REFERENCES study_group(study_group_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__fk_subjec_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__fk_subjec_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__fk_subjec_study_gr; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__fk_subjec_study_gr FOREIGN KEY (study_group_class_id) REFERENCES study_group_class(study_group_class_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject__subject_g_study_su; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY subject_group_map
    ADD CONSTRAINT fk_subject__subject_g_study_su FOREIGN KEY (study_subject_id) REFERENCES study_subject(study_subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject_fk_subjec_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT fk_subject_fk_subjec_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject_fk_subjec_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT fk_subject_fk_subjec_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_subject_reference_instrument; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_crf
    ADD CONSTRAINT fk_subject_reference_instrument FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_user_acc_fk_user_f_user_acc; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY user_account
    ADD CONSTRAINT fk_user_acc_fk_user_f_user_acc FOREIGN KEY (owner_id) REFERENCES user_account(user_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_user_acc_ref_user__user_typ; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY user_account
    ADD CONSTRAINT fk_user_acc_ref_user__user_typ FOREIGN KEY (user_type_id) REFERENCES user_type(user_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_user_acc_status_re_status; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY user_account
    ADD CONSTRAINT fk_user_acc_status_re_status FOREIGN KEY (status_id) REFERENCES status(status_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_versioni_fk_versio_crf_vers; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY versioning_map
    ADD CONSTRAINT fk_versioni_fk_versio_crf_vers FOREIGN KEY (crf_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_versioni_fk_versio_item; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY versioning_map
    ADD CONSTRAINT fk_versioni_fk_versio_item FOREIGN KEY (item_id) REFERENCES item(item_id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: fk_versioni_reference_instrume; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY crf_version
    ADD CONSTRAINT fk_versioni_reference_instrume FOREIGN KEY (crf_id) REFERENCES crf(crf_id) ON UPDATE RESTRICT;


--
-- Name: fk_versioning_study_inst; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY event_definition_crf
    ADD CONSTRAINT fk_versioning_study_inst FOREIGN KEY (default_version_id) REFERENCES crf_version(crf_version_id) ON UPDATE RESTRICT ON DELETE SET NULL;


--
-- Name: study_parameter_value_parameter_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_parameter_value
    ADD CONSTRAINT study_parameter_value_parameter_fkey FOREIGN KEY (parameter) REFERENCES study_parameter(handle) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: study_parameter_value_study_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: clinica
--

ALTER TABLE ONLY study_parameter_value
    ADD CONSTRAINT study_parameter_value_study_id_fkey FOREIGN KEY (study_id) REFERENCES study(study_id) ON UPDATE RESTRICT ON DELETE RESTRICT;

--
--	Esigs implementation
--

alter table event_definition_crf add column electronic_signature boolean DEFAULT false;
alter table event_crf add column electronic_signature_status boolean DEFAULT false;

INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (14, 'Event CRF signed complete');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (15, 'Event CRF IDE signed (DDE)');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (16, 'Event CRF validated and signed (DDE)');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (17, 'Study Event scheduled');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (18, 'Study Event data entry started');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (19, 'Study Event completed');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (20, 'Study Event stopped');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (21, 'Study Event skipped');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (22, 'Study Event locked');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (23, 'Study Event removed');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (24, 'Study Event start date changed');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (25, 'Study Event end date changed');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (26, 'Study Event location changed');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (27, 'Subject Site Assignment');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (28, 'Subject Group Assignment');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (29, 'Subject Group changed');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (30, 'Item data inserted for repeating row');
INSERT INTO audit_log_event_type(audit_log_event_type_id, name) values (31, 'Study Event signed');

update audit_log_event_type set name = 'Event CRF complete with password' where name = 'Event CRF signed complete';
update audit_log_event_type set name = 'Event CRF Initial Data Entry complete with password' where name = 'Event CRF IDE signed (DDE)';
update audit_log_event_type set name = 'Event CRF Double Data Entry complete with password' where name = 'Event CRF validated and signed (DDE)';
update audit_log_event_type set name = 'Event CRF Initial Data Entry complete' where name = 'Event CRF IDE completed (DDE)';
update audit_log_event_type set name = 'Event CRF Double Data Entry complete' where name = 'Event CRF validated(DDE)';


---
--- OID Implementation
---

ALTER TABLE crf ADD COLUMN oc_oid character varying(40);
ALTER TABLE study_event_definition ADD COLUMN oc_oid character varying(40);
ALTER TABLE crf_version ADD COLUMN oc_oid character varying(40);
ALTER TABLE item_group ADD COLUMN oc_oid character varying(40);
ALTER TABLE item ADD COLUMN oc_oid character varying(40);
ALTER TABLE study ADD COLUMN oc_oid character varying(40);
ALTER TABLE study_subject ADD COLUMN oc_oid character varying(40);

-- generate OID's for crfs  
update crf set oc_oid  = 
'F_' ||
substring(upper(regexp_replace(crf.name, E'\\s+|\\W+', '','g')),0,5) || '_' ||
crf_id ;

-- generate OID's for study event definitions  
update study_event_definition set oc_oid = 
'SE_' ||
substring(upper(regexp_replace(name, E'\\s+|\\W+', '','g')),0,9) || study_event_definition_id;

-- generate OID's for crf versions  
update crf_version set oc_oid  = 
crf.oc_oid || '_' ||
substring(upper(regexp_replace(crf_version.name, E'\\s+|\\W+', '','g')),0,5) ||
crf_version.crf_version_id 
from crf where crf_version.crf_id=crf.crf_id;

-- generate OID's for item groups 
update item_group set oc_oid  = 
'IG_' ||
substring(upper(regexp_replace(crf.name, E'\\s+|\\W+', '','g')),0,5) || '_' ||
substring(upper(regexp_replace(item_group.name, E'\\s+|\\W+', '','g')),0,28) ||
item_group.item_group_id 
from crf where item_group.crf_id=crf.crf_id;

-- generate OIDs for items
update item set oc_oid  = 
'I_' ||
substring(upper(regexp_replace(crf.name, E'\\s+|\\W+', '','g')),0,5) || '_' ||
substring(upper(regexp_replace(item.name, E'\\s+|\\W+', '','g')),0,29) ||
item.item_id 
from item_group_metadata, item_group,crf where 
item_group_metadata.item_id = item.item_id AND
item_group_metadata.item_group_id = item_group.item_group_id AND
item_group.crf_id=crf.crf_id;

-- generate OIDs for study
update study set oc_oid = 'S_' || substring(upper(regexp_replace(unique_identifier, E'\\s+|\\W+|-', '','g')),0,9) || study_id;

-- generate OIDs for study_subject
update study_subject set oc_oid = 'SS_' || substring(upper(regexp_replace(label, E'\\s+|\\W+', '','g')),0,9) || study_subject_id;

-- make columns not null 
ALTER TABLE crf ALTER COLUMN oc_oid SET NOT NULL;
ALTER TABLE study_event_definition ALTER COLUMN oc_oid SET NOT NULL;
ALTER TABLE crf_version ALTER COLUMN oc_oid SET NOT NULL;
ALTER TABLE item_group ALTER COLUMN oc_oid SET NOT NULL;
ALTER TABLE item ALTER COLUMN oc_oid SET NOT NULL;
ALTER TABLE study ALTER COLUMN oc_oid SET NOT NULL;
ALTER TABLE study_subject ALTER COLUMN oc_oid SET NOT NULL;

-- add uniqueness constraints 
ALTER TABLE crf ADD CONSTRAINT uniq_crf_oc_oid UNIQUE (oc_oid);
ALTER TABLE study_event_definition ADD CONSTRAINT uniq_study_event_definition_oid UNIQUE (oc_oid);
ALTER TABLE crf_version ADD CONSTRAINT uniq_crf_version_oc_oid UNIQUE (oc_oid);
ALTER TABLE item_group ADD CONSTRAINT uniq_item_group_oc_oid UNIQUE (oc_oid);
ALTER TABLE item ADD CONSTRAINT uniq_item_oc_oid UNIQUE (oc_oid);
ALTER TABLE study ADD CONSTRAINT uniq_study_oid UNIQUE (oc_oid);
ALTER TABLE study_subject ADD CONSTRAINT uniq_study_subject_oid UNIQUE (oc_oid);

-- create indexes 
CREATE INDEX oc_oid_crf_table ON crf USING btree (oc_oid);
CREATE INDEX oc_oid_study_event_definition_table ON study_event_definition USING btree (oc_oid);
CREATE INDEX oc_oid_crf_version_table ON crf_version USING btree (oc_oid);
CREATE INDEX oc_oid_item_group_table ON item_group USING btree (oc_oid);
CREATE INDEX oc_oid_item_table ON item USING btree (oc_oid);
CREATE INDEX oc_oid_study_table ON study USING btree (oc_oid);
CREATE INDEX oc_oid_study_subject_table ON study_subject USING btree (oc_oid);

---
--- Indexing for performance enhancing in datawarehouse script
---

-- create indexes on item_group_metadata
CREATE INDEX item_id_item_group_metadata_table ON item_group_metadata USING btree (item_id);
CREATE INDEX crf_version_id_item_group_metadata_table ON item_group_metadata USING btree (crf_version_id);

-- create indexes on item_data
CREATE INDEX status_id_item_data_table ON item_data USING btree (status_id);
CREATE INDEX item_id_item_data_table ON item_data USING btree (item_id);
CREATE INDEX event_crf_id_item_data_table ON item_data USING btree (event_crf_id);

-- create indexes on item_form_metadata, event_definition_crf, event_crf, item_group
CREATE INDEX response_set_id_item_form_metadata_table ON item_form_metadata USING btree (response_set_id);
CREATE INDEX crf_id_event_definition_crf_table ON event_definition_crf USING btree (crf_id);
CREATE INDEX status_id_event_crf_table ON event_crf USING btree (status_id);
CREATE INDEX item_group_id_item_group_table ON item_group USING btree (item_group_id);


--
-- Scoring Implementation
--

-- new response_types have been added. ywang, 1/8/2007
INSERT INTO response_type VALUES (8, 'calculation', 'value calculated automatically');
INSERT INTO response_type VALUES (9, 'group-calculation', 'value calculated automatically from an entire group of items');

-- add more columns in the table <dataset>.
ALTER TABLE dataset ADD odm_metadataversion_name varchar(255);
ALTER TABLE dataset ADD odm_metadataversion_oid varchar(255);
ALTER TABLE dataset ADD odm_prior_study_oid varchar(255);
ALTER TABLE dataset ADD odm_prior_metadataversion_oid varchar(255);

--
-- Rules Implementation
--

CREATE TABLE "rule"
(
   rule_id serial NOT NULL, 
   name character varying(255),
   description character varying(255),
   oc_oid character varying(40), 
   enabled boolean,
   rule_expression_id integer NOT NULL,
   owner_id integer,
   date_created date,
   date_updated date,
   update_id integer,
   status_id integer,
   CONSTRAINT rule_id_pkey PRIMARY KEY (rule_id)
) WITHOUT OIDS;
ALTER TABLE "rule" OWNER TO clinica;

CREATE TABLE "rule_set"
(
   rule_set_id serial NOT NULL,
   rule_expression_id integer NOT NULL,
   study_event_definition_id integer NOT NULL,
   crf_id integer,
   crf_version_id integer,
   study_id integer NOT NULL,
   owner_id integer,
   date_created date,
   date_updated date,
   update_id integer,
   status_id integer,
   CONSTRAINT rule_set_id_pkey PRIMARY KEY (rule_set_id)
) WITHOUT OIDS;
ALTER TABLE "rule_set" OWNER TO clinica;

CREATE TABLE "rule_set_audit"
(
   rule_set_audit_id serial NOT NULL,
   rule_set_id integer NOT NULL,
   date_updated date,
   updater_id integer,
   status_id integer,
   CONSTRAINT rule_set_audit_id_pkey PRIMARY KEY (rule_set_audit_id)
) WITHOUT OIDS;
ALTER TABLE "rule_set_audit" OWNER TO clinica;


CREATE TABLE "rule_set_rule"
(
   rule_set_rule_id serial NOT NULL,
   rule_set_id integer NOT NULL, 
   rule_id integer NOT NULL, 
   owner_id integer,
   date_created date,
   date_updated date,
   update_id integer,
   status_id integer,
   CONSTRAINT rule_set_rule_id_pkey PRIMARY KEY (rule_set_rule_id)
) WITHOUT OIDS;
ALTER TABLE "rule_set_rule" OWNER TO clinica;

CREATE TABLE "rule_set_rule_audit"
(
   rule_set_rule_audit_id serial NOT NULL,
   rule_set_rule_id integer NOT NULL,
   date_updated date,
   updater_id integer,
   status_id integer,
   CONSTRAINT rule_set_rule_audit_id_pkey PRIMARY KEY (rule_set_rule_audit_id)
) WITHOUT OIDS;
ALTER TABLE "rule_set_rule_audit" OWNER TO clinica;

CREATE TABLE "rule_action"
(
   rule_action_id serial NOT NULL,
   rule_set_rule_id integer NOT NULL,
   action_type integer NOT NULL,
   expression_evaluates_to boolean NOT NULL,
   message character varying(255),
   email_to character varying(255),
   owner_id integer,
   date_created date,
   date_updated date,
   update_id integer,
   status_id integer,
   CONSTRAINT rule_action_id_pkey PRIMARY KEY (rule_action_id)
) WITHOUT OIDS;
ALTER TABLE "rule_action" OWNER TO clinica;

CREATE TABLE "rule_expression"
(
   rule_expression_id serial NOT NULL,
   value character varying(1025) NOT NULL, 
   context integer NOT NULL,
   owner_id integer,
   date_created date,
   date_updated date,
   update_id integer,
   status_id integer,
   CONSTRAINT expression_id_pkey PRIMARY KEY (rule_expression_id)
) WITHOUT OIDS;
ALTER TABLE "rule_expression" OWNER TO clinica;

ALTER TABLE audit_log_event ADD COLUMN study_event_id INTEGER;
ALTER TABLE audit_log_event ADD COLUMN event_crf_version_id INTEGER;
ALTER TABLE dataset ADD COLUMN show_secondary_id BOOLEAN DEFAULT false;

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

