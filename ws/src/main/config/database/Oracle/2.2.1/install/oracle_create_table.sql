/*--------------------------------------------------------------------------
*
* File       : oracle_create_table.sql
*
* Subject    : Creates tables
*
* Parameters : None
*
* Conditions : Execute script as Clinica
*
* Author/Dt  : Shriram Mani 12/13/2007
*
* Comments   : None
*
--------------------------------------------------------------------------*/
--
-- Name: archived_dataset_file; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE archived_dataset_file (
    archived_dataset_file_id integer not null,
    name varchar2(255),
    dataset_id integer,
    export_format_id integer,
    file_reference varchar2(1000),
    run_time integer,
    file_size integer,
    date_created date,
    owner_id integer
);



--
-- Name: archived_dataset_file_archived_dataset_file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('archived_dataset_file', 'archived_dataset_file_id'), 1, false);


--
-- Name: audit_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE audit_event (
    audit_id integer not null,
    audit_date timestamp(3) NOT NULL,
    audit_table varchar2(500) NOT NULL,
    user_id integer,
    entity_id integer,
    reason_for_change varchar2(1000),
    action_message varchar2(4000)
);



--
-- Name: audit_event_audit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('audit_event', 'audit_id'), 10, true);


--
-- Name: audit_event_context; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE audit_event_context (
    audit_id integer,
    study_id integer,
    subject_id integer,
    study_subject_id integer,
    role_name varchar2(200),
    event_crf_id integer,
    study_event_id integer,
    study_event_definition_id integer,
    crf_id integer,
    crf_version_id integer,
    study_crf_id integer,
    item_id integer
);

--
-- Name: audit_event_values; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--
CREATE TABLE audit_event_values
(
  audit_id integer,
  column_name  varchar2(255),
  old_value varchar2(2000),
  new_value varchar2(2000)
);


CREATE TABLE audit_log_event (
    audit_id integer NOT NULL,
    audit_date timestamp(3) NOT NULL,
    audit_table varchar2(500) NOT NULL,
    user_id integer,
    entity_id integer,
    entity_name varchar2(500),
    reason_for_change varchar2(1000),
    audit_log_event_type_id integer,
    old_value varchar2(2000),
    new_value varchar2(2000),
    event_crf_id integer
);

CREATE TABLE audit_log_event_type (
    audit_log_event_type_id integer NOT NULL,
    name varchar2(255)
);

/*
A    date_end date,
    date_created date,
    date_updated date,
    date_last_run date,
    owner_id integer,
    approver_id integer,
    update_id integer,
    show_event_location varchar2(1) default 'N',
    show_event_start varchar2(1) default 'N',
    show_event_end varchar2(1) default 'N',
    show_subject_dob varchar2(1) default 'N',
    show_subject_gender varchar2(1) default 'N',
    show_event_status varchar2(1) default 'N',
    show_subject_status varchar2(1) default 'N',
    show_subject_unique_id varchar2(1) default 'N',
    show_subject_age_at_event varchar2(1) default 'N',
    show_crf_status varchar2(1) default 'N',
    show_crf_version varchar2(1) default 'N',
    show_crf_int_name varchar2(1) default 'N',
    show_crf_int_date varchar2(1) default 'N',
    show_group_info varchar2(1) default 'N',
    show_disc_info varchar2(1) default 'N'
);
*/


CREATE TABLE completion_status (
    completion_status_id integer NOT NULL,
    status_id integer,
    name varchar2(255),
    description varchar2(1000)
);


--
-- Name: crf; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE crf(
  crf_id integer,
  status_id integer,
  name varchar2(255),
  description varchar2(2048),
  owner_id integer,
  date_created date,
  date_updated date,
  update_id integer
);

--
-- Name: crf_version; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE crf_version(
  crf_version_id integer,
  crf_id integer,
  name varchar2(255),
  description varchar2(4000),
  revision_notes varchar2(255),
  status_id integer,
  date_created date,
  date_updated date,
  owner_id integer,
  update_id integer
);


--
-- Name: dataset; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--
CREATE TABLE dataset
    (
      dataset_id integer,
      study_id integer,
      status_id integer,
      name varchar2(255),
      description varchar2(2000),
      sql_statement varchar2(4000),
      num_runs integer,
      date_start date,
      date_end date,
      date_created date,
      date_updated date,
      date_last_run date,
      owner_id integer,
      approver_id integer,
      update_id integer,
      show_event_location integer default 0,
      show_event_start integer default 0,
      show_event_end integer default 0,
      show_subject_dob integer default 0,
      show_subject_gender integer default 0,
      show_event_status integer default 0,
      show_subject_status integer default 0,
      show_subject_unique_id integer default 0,
      show_subject_age_at_event integer default 0,
      show_crf_status integer default 0,
      show_crf_version integer default 0,
      show_crf_int_name integer default 0,
      show_crf_int_date integer default 0,
      show_group_info integer default 0,
      show_disc_info integer default 0
);



--
-- Name: dataset_crf_version_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dataset_crf_version_map (
    dataset_id integer,
    event_definition_crf_id integer
);



--
-- Name: dataset_dataset_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dataset', 'dataset_id'), 1, false);


--
-- Name: dataset_filter_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dataset_filter_map (
    dataset_id integer,
    filter_id integer,
    ordinal integer
);



--
-- Name: dataset_study_group_class_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dataset_study_group_class_map (
    dataset_id integer NOT NULL,
    study_group_class_id integer NOT NULL
);



--
-- Name: dc_computed_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dc_computed_event (
    dc_summary_event_id integer not null,
    dc_event_id integer NOT NULL,
    item_target_id integer,
    summary_type varchar2(255)
);



--
-- Name: dc_computed_event_dc_summary_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_computed_event', 'dc_summary_event_id'), 1, false);


--
-- Name: dc_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dc_event (
    dc_event_id integer not null,
    decision_condition_id integer,
    ordinal integer NOT NULL,
    "type" varchar2(256) NOT NULL
);



--
-- Name: dc_event_dc_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_event', 'dc_event_id'), 1, false);


--
-- Name: dc_primitive; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dc_primitive (
    dc_primitive_id integer not null,
    decision_condition_id integer,
    item_id integer,
    dynamic_value_item_id integer,
    comparison varchar2(3) NOT NULL,
    constant_value varchar2(4000)
);



--
-- Name: dc_primitive_dc_primitive_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_primitive', 'dc_primitive_id'), 1, false);


--
-- Name: dc_section_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dc_section_event (
    dc_event_id integer not null,
    section_id integer NOT NULL
);



--
-- Name: dc_section_event_dc_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_section_event', 'dc_event_id'), 1, false);


--
-- Name: dc_send_email_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dc_send_email_event (
    dc_event_id integer not null,
    to_address varchar2(1000) NOT NULL,
    subject varchar2(1000),
    body varchar2(4000)
);



--
-- Name: dc_send_email_event_dc_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_send_email_event', 'dc_event_id'), 1, false);


--
-- Name: dc_substitution_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dc_substitution_event (
    dc_event_id integer not null,
    item_id integer,
    value varchar2(1000) NOT NULL
);



--
-- Name: dc_substitution_event_dc_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('dc_substitution_event', 'dc_event_id'), 1, false);


--
-- Name: dc_summary_item_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dc_summary_item_map (
    dc_summary_event_id integer,
    item_id integer,
    ordinal integer
);



--
-- Name: decision_condition; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE decision_condition (
    decision_condition_id integer not null,
    crf_version_id integer,
    status_id integer,
    label varchar2(1000) NOT NULL,
    comments varchar2(3000) NOT NULL,
    quantity integer NOT NULL,
    "type" varchar2(3) NOT NULL,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer
);



--
-- Name: decision_condition_decision_condition_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('decision_condition', 'decision_condition_id'), 1, false);


--
-- Name: discrepancy_note; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE discrepancy_note (
    discrepancy_note_id integer not null,
    description varchar2(255),
    discrepancy_note_type_id integer,
    resolution_status_id integer,
    detailed_notes varchar2(1000),
    date_created date,
    owner_id integer,
    parent_dn_id integer,
    entity_type varchar2(30),
    study_id integer
);



--
-- Name: discrepancy_note_discrepancy_note_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('discrepancy_note', 'discrepancy_note_id'), 1, false);


--
-- Name: discrepancy_note_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE discrepancy_note_type (
    discrepancy_note_type_id integer not null,
    name varchar2(50),
    description varchar2(255)
);



--
-- Name: discrepancy_note_type_discrepancy_note_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('discrepancy_note_type', 'discrepancy_note_type_id'), 1, false);


Rem SET default_with_oids = false;

--
-- Name: dn_event_crf_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dn_event_crf_map (
    event_crf_id integer,
    discrepancy_note_id integer,
    column_name varchar2(255)
);



--
-- Name: dn_item_data_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dn_item_data_map (
    item_data_id integer,
    discrepancy_note_id integer,
    column_name varchar2(255)
);



--
-- Name: dn_study_event_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dn_study_event_map (
    study_event_id integer,
    discrepancy_note_id integer,
    column_name varchar2(255)
);



--
-- Name: dn_study_subject_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dn_study_subject_map (
    study_subject_id integer,
    discrepancy_note_id integer,
    column_name varchar2(255)
);



Rem SET default_with_oids = true;

--
-- Name: dn_subject_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE dn_subject_map (
    subject_id integer,
    discrepancy_note_id integer,
    column_name varchar2(255)
);



--
-- Name: event_crf; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE event_crf (
    event_crf_id integer not null,
    study_event_id integer,
    crf_version_id integer,
    date_interviewed date,
    interviewer_name varchar2(255),
    completion_status_id integer,
    status_id integer,
    annotations varchar2(4000),
    date_completed timestamp(3),
    validator_id integer,
    date_validate date,
    date_validate_completed timestamp(3),
    validator_annotations varchar2(4000),
    validate_string varchar2(256),
    owner_id integer,
    date_created date,
    study_subject_id integer,
    date_updated date,
    update_id integer
);



--
-- Name: event_crf_event_crf_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('event_crf', 'event_crf_id'), 1, false);


--
-- Name: event_definition_crf; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE event_definition_crf (
    event_definition_crf_id integer not null,
    study_event_definition_id integer,
    study_id integer,
    crf_id integer,
    required_crf integer,
    double_entry integer,
    require_all_text_filled integer,
    decision_conditions integer,
    null_values varchar2(255),
    default_version_id integer,
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    ordinal integer
);



--
-- Name: event_definition_crf_event_definition_crf_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('event_definition_crf', 'event_definition_crf_id'), 1, false);


--
-- Name: export_format; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE export_format (
    export_format_id integer not null,
    name varchar2(255),
    description varchar2(1000),
    mime_type varchar2(255)
);



--
-- Name: export_format_export_format_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('export_format', 'export_format_id'), 1, false);


--
-- Name: filter; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE filter (
    filter_id integer not null,
    name varchar2(255),
    description varchar2(2000),
    sql_statement varchar2(4000),
    status_id integer,
    date_created date,
    date_updated date,
    owner_id integer NOT NULL,
    update_id integer
);



--
-- Name: filter_crf_version_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE filter_crf_version_map (
    filter_id integer,
    crf_version_id integer
);



--
-- Name: filter_filter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('filter', 'filter_id'), 1, false);


--
-- Name: group_class_types; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE group_class_types (
    group_class_type_id integer not null,
    name varchar2(255),
    description varchar2(1000)
);



--
-- Name: group_class_types_group_class_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('group_class_types', 'group_class_type_id'), 2, true);


--
-- Name: item; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE item (
    item_id integer not null,
    name varchar2(255),
    description varchar2(4000),
    units varchar2(64),
    phi_status integer,
    item_data_type_id integer,
    item_reference_type_id integer,
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer
);



--
-- Name: item_data; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE item_data (
    item_data_id integer not null,
    item_id integer NOT NULL,
    event_crf_id integer,
    status_id integer,
    value varchar2(4000),
    date_created date,
    date_updated date,
    owner_id integer,
    update_id integer,
    ordinal integer
);



--
-- Name: item_data_item_data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_data', 'item_data_id'), 1, false);


--
-- Name: item_data_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE item_data_type (
    item_data_type_id integer not null,
    code varchar2(20),
    name varchar2(255),
    definition varchar2(1000),
    reference varchar2(1000)
);



--
-- Name: item_data_type_item_data_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_data_type', 'item_data_type_id'), 9, true);


--
-- Name: item_form_metadata; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE item_form_metadata (
    item_form_metadata_id integer not null,
    item_id integer NOT NULL,
    crf_version_id integer,
    "header" varchar2(2000),
    subheader varchar2(240),
    parent_id integer,
    parent_label varchar2(120),
    column_number integer,
    page_number_label varchar2(5),
    question_number_label varchar2(20),
    left_item_text varchar2(4000),
    right_item_text varchar2(2000),
    section_id integer NOT NULL,
    decision_condition_id integer,
    response_set_id integer NOT NULL,
    regexp varchar2(1000),
    regexp_error_msg varchar2(255),
    ordinal integer NOT NULL,
    required integer,
    default_value varchar2(4000),
    response_layout varchar2(255)
);



--
-- Name: item_form_metadata_item_form_metadata_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_form_metadata', 'item_form_metadata_id'), 1, false);


--
-- Name: item_group; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE item_group (
    item_group_id integer not null,
    name varchar2(255),
    crf_id integer NOT NULL,
    status_id integer,
    date_created date,
    date_updated date,
    owner_id integer,
    update_id integer
);



--
-- Name: item_group_item_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_group', 'item_group_id'), 1, false);


--
-- Name: item_group_metadata; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE item_group_metadata (
    item_group_metadata_id integer not null,
    item_group_id integer NOT NULL,
    "header" varchar2(255),
    subheader varchar2(255),
    layout varchar2(100),
    repeat_number integer,
    repeat_max integer,
    repeat_array varchar2(255),
    row_start_number integer,
    crf_version_id integer NOT NULL,
    item_id integer NOT NULL,
    ordinal integer NOT NULL,
    borders integer
);



--
-- Name: item_group_metadata_item_group_metadata_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_group_metadata', 'item_group_metadata_id'), 1, false);


--
-- Name: item_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item', 'item_id'), 1, false);


--
-- Name: item_reference_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE item_reference_type (
    item_reference_type_id integer not null,
    name varchar2(255),
    description varchar2(1000)
);



--
-- Name: item_reference_type_item_reference_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('item_reference_type', 'item_reference_type_id'), 1, true);


--
-- Name: null_value_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE null_value_type (
    null_value_type_id integer not null,
    code varchar2(20),
    name varchar2(255),
    definition varchar2(1000),
    reference varchar2(1000)
);



--
-- Name: null_value_type_null_value_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('null_value_type', 'null_value_type_id'), 11, true);


--
-- Name: openclinica_version; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE openclinica_version (
    name varchar2(255),
    test_path varchar2(1000)
);



--
-- Name: privilege; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE privilege (
    priv_id integer not null,
    priv_name varchar2(50),
    priv_desc varchar2(2000)
);



--
-- Name: privilege_priv_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('privilege', 'priv_id'), 1, false);


--
-- Name: resolution_status; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE resolution_status (
    resolution_status_id integer not null,
    name varchar2(50),
    description varchar2(255)
);



--
-- Name: resolution_status_resolution_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('resolution_status', 'resolution_status_id'), 1, false);


--
-- Name: response_set; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE response_set (
    response_set_id integer not null,
    response_type_id integer,
    label varchar2(80),
    options_text varchar2(4000),
    options_values varchar2(4000),
    version_id integer
);



--
-- Name: response_set_response_set_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('response_set', 'response_set_id'), 1, false);


--
-- Name: response_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE response_type (
    response_type_id integer not null,
    name varchar2(255),
    description varchar2(1000)
);



--
-- Name: response_type_response_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('response_type', 'response_type_id'), 7, true);


--
-- Name: role_privilege_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE role_privilege_map (
    role_id integer NOT NULL,
    priv_id integer NOT NULL,
    priv_value varchar2(50)
);



--
-- Name: section; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE section (
    section_id integer not null,
    crf_version_id integer NOT NULL,
    status_id integer,
    label varchar2(2000),
    title varchar2(2000),
    subtitle varchar2(2000),
    instructions varchar2(2000),
    page_number_label varchar2(5),
    ordinal integer,
    parent_id integer,
    date_created date,
    date_updated date,
    owner_id integer NOT NULL,
    update_id integer
);



--
-- Name: section_section_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('section', 'section_id'), 1, false);


--
-- Name: status; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE status (
    status_id integer not null,
    name varchar2(255),
    description varchar2(1000)
);



--
-- Name: status_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('status', 'status_id'), 1, false);


--
-- Name: study; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study (
    study_id integer not null,
    parent_study_id integer,
    unique_identifier varchar2(30),
    secondary_identifier varchar2(255),
    name varchar2(255),
    summary varchar2(255),
    date_planned_start date,
    date_planned_end date,
    date_created date,
    date_updated date,
    owner_id integer,
    update_id integer,
    type_id integer,
    status_id integer,
    principal_investigator varchar2(255),
    facility_name varchar2(255),
    facility_city varchar2(255),
    facility_state varchar2(20),
    facility_zip varchar2(64),
    facility_country varchar2(64),
    facility_recruitment_status varchar2(60),
    facility_contact_name varchar2(255),
    facility_contact_degree varchar2(255),
    facility_contact_phone varchar2(255),
    facility_contact_email varchar2(255),
    protocol_type varchar2(30),
    protocol_description varchar2(1000),
    protocol_date_verification date,
    phase varchar2(30),
    expected_total_enrollment integer,
    sponsor varchar2(255),
    collaborators varchar2(1000),
    medline_identifier varchar2(255),
    url varchar2(255),
    url_description varchar2(255),
    conditions varchar2(500),
    keywords varchar2(255),
    eligibility varchar2(500),
    gender varchar2(30),
    age_max varchar2(3),
    age_min varchar2(3),
    healthy_volunteer_accepted integer,
    purpose varchar2(64),
    allocation varchar2(64),
    masking varchar2(30),
    control varchar2(30),
    "assignment" varchar2(30),
    endpoint varchar2(64),
    interventions varchar2(1000),
    duration varchar2(30),
    selection varchar2(30),
    timing varchar2(30),
    official_title varchar2(255),
    results_reference integer
);



--
-- Name: study_event; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study_event (
    study_event_id integer not null,
    study_event_definition_id integer,
    study_subject_id integer,
    "location" varchar2(2000),
    sample_ordinal integer,
    date_start timestamp(3),
    date_end timestamp(3),
    owner_id integer,
    status_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    subject_event_status_id integer,
    start_time_flag integer,
    end_time_flag integer
);



--
-- Name: study_event_definition; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study_event_definition (
    study_event_definition_id integer not null,
    study_id integer,
    name varchar2(2000),
    description varchar2(2000),
    repeating integer,
    "type" varchar2(20),
    category varchar2(2000),
    owner_id integer,
    status_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    ordinal integer
);



--
-- Name: study_event_definition_study_event_definition_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_event_definition', 'study_event_definition_id'), 1, false);


--
-- Name: study_event_study_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_event', 'study_event_id'), 1, false);


--
-- Name: study_group; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study_group (
    study_group_id integer not null,
    name varchar2(255),
    description varchar2(1000),
    study_group_class_id integer
);



--
-- Name: study_group_class; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study_group_class (
    study_group_class_id integer not null,
    name varchar2(30),
    study_id integer,
    owner_id integer,
    date_created date,
    group_class_type_id integer,
    status_id integer,
    date_updated date,
    update_id integer,
    subject_assignment varchar2(30)
);



--
-- Name: study_group_class_study_group_class_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_group_class', 'study_group_class_id'), 1, false);


--
-- Name: study_group_study_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_group', 'study_group_id'), 1, false);


--
-- Name: study_parameter; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study_parameter (
    study_parameter_id integer not null,
    handle varchar2(50),
    name varchar2(50),
    description varchar2(255),
    default_value varchar2(50),
    inheritable integer default 1,
    overridable integer
);



--
-- Name: study_parameter_study_parameter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_parameter', 'study_parameter_id'), 1, false);


--
-- Name: study_parameter_value; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study_parameter_value (
    study_parameter_value_id integer not null,
    study_id integer,
    value varchar2(50),
    parameter varchar2(50)
);



--
-- Name: study_parameter_value_study_parameter_value_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_parameter_value', 'study_parameter_value_id'), 1, false);


--
-- Name: study_study_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study', 'study_id'), 1, true);


--
-- Name: study_subject; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study_subject (
    study_subject_id integer not null,
    label varchar2(30),
    secondary_label varchar2(30),
    subject_id integer,
    study_id integer,
    status_id integer,
    enrollment_date date,
    date_created date,
    date_updated date,
    owner_id integer,
    update_id integer
);



--
-- Name: study_subject_study_subject_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_subject', 'study_subject_id'), 1, false);


--
-- Name: study_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study_type (
    study_type_id integer not null,
    name varchar2(255),
    description varchar2(1000)
);



--
-- Name: study_type_study_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('study_type', 'study_type_id'), 1, false);


--
-- Name: study_user_role; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE study_user_role (
    role_name varchar2(40),
    study_id integer,
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    user_name varchar2(40)
);



--
-- Name: subject; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE subject (
    subject_id integer not null,
    father_id integer,
    mother_id integer,
    status_id integer,
    date_of_birth date,
    gender character(1),
    unique_identifier varchar2(255),
    date_created date,
    owner_id integer,
    date_updated date,
    update_id integer,
    dob_collected integer
);



--
-- Name: subject_event_status; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE subject_event_status (
    subject_event_status_id integer not null,
    name varchar2(255),
    description varchar2(1000)
);



--
-- Name: subject_event_status_subject_event_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('subject_event_status', 'subject_event_status_id'), 1, false);


--
-- Name: subject_group_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE subject_group_map (
    subject_group_map_id integer not null,
    study_group_class_id integer,
    study_subject_id integer,
    study_group_id integer,
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    update_id integer,
    notes varchar2(255)
);



--
-- Name: subject_group_map_subject_group_map_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('subject_group_map', 'subject_group_map_id'), 1, false);


--
-- Name: subject_subject_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('subject', 'subject_id'), 1, false);


--
-- Name: user_account; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE user_account (
    user_id integer not null,
    user_name varchar2(64),
    passwd varchar2(255),
    first_name varchar2(50),
    last_name varchar2(50),
    email varchar2(120),
    active_study integer,
    institutional_affiliation varchar2(255),
    status_id integer,
    owner_id integer,
    date_created date,
    date_updated date,
    date_lastvisit timestamp(3),
    passwd_timestamp date,
    passwd_challenge_question varchar2(64),
    passwd_challenge_answer varchar2(255),
    phone varchar2(64),
    user_type_id integer,
    update_id integer
);



--
-- Name: user_account_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('user_account', 'user_id'), 3, true);


--
-- Name: user_role; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE user_role (
    role_id integer not null,
    role_name varchar2(50) NOT NULL,
    parent_id integer,
    role_desc varchar2(2000)
);



--
-- Name: user_role_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('user_role', 'role_id'), 6, true);


--
-- Name: user_type; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE user_type (
    user_type_id integer not null,
    user_type varchar2(50)
);



--
-- Name: user_type_user_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: clinica
--

Rem SELECT pg_catalog.setval(pg_catalog.pg_get_serial_sequence('user_type', 'user_type_id'), 1, false);


--
-- Name: versioning_map; Type: TABLE; Schema: public; Owner: clinica; Tablespace:
--

CREATE TABLE versioning_map (
    crf_version_id integer,
    item_id integer
);


