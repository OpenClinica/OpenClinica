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
prompt Create table ARCHIVED_DATASET_FILE
--

create table ARCHIVED_DATASET_FILE (
    archived_dataset_file_id           integer not null,
    name                               varchar2(255),
    dataset_id                         integer,
    export_format_id                   integer,
    file_reference                     varchar2(1000),
    run_time                           integer,
    file_size                          integer,
    date_created                       date,
    owner_id                           integer
);                                     
                                       
--
prompt Create table AUDIT_EVENT
--

create table AUDIT_EVENT (
    audit_id                           integer not null,
    audit_date                         timestamp(3) not null,
    audit_table                        varchar2(500) not null,
    user_id                            integer,
    entity_id                          integer,
    reason_for_change                  varchar2(1000),
    action_message                     varchar2(4000)
);                                     
                                       
--
prompt Create table AUDIT_EVENT_CONTEXT
--

create table AUDIT_EVENT_CONTEXT (
    audit_id                           integer,
    study_id                           integer,
    subject_id                         integer,
    study_subject_id                   integer,
    role_name                          varchar2(200),
    event_crf_id                       integer,
    study_event_id                     integer,
    study_event_definition_id          integer,
    crf_id                             integer,
    crf_version_id                     integer,
    study_crf_id                       integer,
    item_id                            integer
);                                     
                                       
--
prompt Create table AUDIT_EVENT_VALUES
--

create table AUDIT_EVENT_VALUES (
    audit_id                           integer,
    column_name                        varchar2(255),
    old_value                          varchar2(2000),
    new_value                          varchar2(2000)
);                                     
                                       
--
prompt Create table AUDIT_LOG_EVENT
--

create table AUDIT_LOG_EVENT (
    audit_id                           integer not null,
    audit_date                         timestamp(3) not null,
    audit_table                        varchar2(500) not null,
    user_id                            integer,
    entity_id                          integer,
    entity_name                        varchar2(500),
    reason_for_change                  varchar2(1000),
    audit_log_event_type_id            integer,
    old_value                          varchar2(2000),
    new_value                          varchar2(2000),
    event_crf_id                       integer,
    study_event_id                     integer,
    event_crf_version_id               integer
);
                                       
--
prompt Create table AUDIT_LOG_EVENT_TYPE
--

create table AUDIT_LOG_EVENT_TYPE (
    audit_log_event_type_id            integer not null,
    name                               varchar2(255)
);                                     
                                       
--
prompt Create table COMPLETION_STATUS
--

create table COMPLETION_STATUS (
    completion_status_id               integer not null,
    status_id                          integer,
    name                               varchar2(255),
    description                        varchar2(1000)
);                                     
                                       
--
prompt Create table CRF
--

create table CRF (
    crf_id                             integer not null,
    status_id                          integer,
    name                               varchar2(255),
    description                        varchar2(2048),
    owner_id                           integer,
    date_created                       date,
    date_updated                       date,
    update_id                          integer,
    oc_oid                             varchar2(40) not null
);

--
prompt Create table CRF_VERSION
--

create table CRF_VERSION (
    crf_version_id                     integer not null,
    crf_id                             integer not null,
    name                               varchar2(255),
    description                        varchar2(4000),
    revision_notes                     varchar2(255),
    status_id                          integer,
    date_created                       date,
    date_updated                       date,
    owner_id                           integer,
    update_id                          integer,
    oc_oid                             varchar2(40) not null
);

--
prompt Create table DATASET
--

create table DATASET (
    dataset_id                         integer not null,
    study_id                           integer,
    status_id                          integer,
    name                               varchar2(255),
    description                        varchar2(2000),
    sql_statement                      varchar2(4000),
    num_runs                           integer,
    date_start                         date,
    date_end                           date,
    date_created                       date,
    date_updated                       date,
    date_last_run                      date,
    owner_id                           integer,
    approver_id                        integer,
    update_id                          integer,
    show_event_location                varchar2(1) DEFAULT '0',
    show_event_start                   varchar2(1) DEFAULT '0',
    show_event_end                     varchar2(1) DEFAULT '0',
    show_subject_dob                   varchar2(1) DEFAULT '0',
    show_subject_gender                varchar2(1) DEFAULT '0',
    show_event_status                  varchar2(1) DEFAULT '0',
    show_subject_status                varchar2(1) DEFAULT '0',
    show_subject_unique_id             varchar2(1) DEFAULT '0',
    show_subject_age_at_event          varchar2(1) DEFAULT '0',
    show_crf_status                    varchar2(1) DEFAULT '0',
    show_crf_version                   varchar2(1) DEFAULT '0',
    show_crf_int_name                  varchar2(1) DEFAULT '0',
    show_crf_int_date                  varchar2(1) DEFAULT '0',
    show_group_info                    varchar2(1) DEFAULT '0',
    show_disc_info                     varchar2(1) DEFAULT '0',
    odm_metadataversion_name           varchar2(255),
    odm_metadataversion_oid            varchar2(255),
    odm_prior_study_oid                varchar2(255),
    odm_prior_metadataversion_oid      varchar2(255),
    show_secondary_id                  varchar2(1) DEFAULT '0'
);

--
prompt Create table DATASET_CRF_VERSION_MAP
--

create table DATASET_CRF_VERSION_MAP (
    dataset_id                         integer,
    event_definition_crf_id            integer,
    electronic_signature               varchar2(1) default '0'
);

--
prompt Create table DATASET_FILTER_MAP
--

create table DATASET_FILTER_MAP (
    dataset_id                         integer,
    filter_id                          integer,
    ordinal                            integer
);

--
prompt Create table DATASET_STUDY_GROUP_CLASS_MAP
--

create table DATASET_STUDY_GROUP_CLASS_MAP (
    dataset_id                         integer not null,
    study_group_class_id               integer not null
);

--
prompt Create table DC_COMPUTED_EVENT
--

create table DC_COMPUTED_EVENT (
    dc_summary_event_id                integer not null,
    dc_event_id                        integer not null,
    item_target_id                     integer,
    summary_type                       varchar2(255)
);

--
prompt Create table DC_EVENT
--

create table DC_EVENT (
    dc_event_id                        integer not null,
    decision_condition_id              integer,
    ordinal                            integer not null,
    "type"                             varchar2(256) not null
);

--
prompt Create table DC_PRIMITIVE
--

create table DC_PRIMITIVE (
    dc_primitive_id                    integer not null,
    decision_condition_id              integer,
    item_id                            integer,
    dynamic_value_item_id              integer,
    comparison                         varchar2(3) not null,
    constant_value                     varchar2(4000)
);

--
prompt Create table DC_SECTION_EVENT
--

create table DC_SECTION_EVENT (
    dc_event_id                        integer not null,
    section_id                         integer not null
);

--
prompt Create table DC_SEND_EMAIL_EVENT
--

create table DC_SEND_EMAIL_EVENT (
    dc_event_id                        integer not null,
    to_address                         varchar2(1000) not null,
    subject                            varchar2(1000),
    body                               varchar2(4000)
);

--
prompt Create table DC_SUBSTITUTION_EVENT
--

create table DC_SUBSTITUTION_EVENT (
    dc_event_id                        integer not null,
    item_id                            integer,
    value                              varchar2(1000) not null
);

--
prompt Create table DC_SUMMARY_ITEM_MAP
--

create table DC_SUMMARY_ITEM_MAP (
    dc_summary_event_id                integer,
    item_id                            integer,
    ordinal                            integer
);

--
prompt Create table DECISION_CONDITION
--

create table DECISION_CONDITION (
    decision_condition_id              integer not null,
    crf_version_id                     integer,
    status_id                          integer,
    label                              varchar2(1000) not null,
    comments                           varchar2(3000) not null,
    quantity                           integer not null,
    "type"                             varchar2(3) not null,
    owner_id                           integer,
    date_created                       date,
    date_updated                       date,
    update_id                          integer
);

--
prompt Create table DISCREPANCY_NOTE
--

create table DISCREPANCY_NOTE (
    discrepancy_note_id                integer not null,
    description                        varchar2(255),
    discrepancy_note_type_id           integer,
    resolution_status_id               integer,
    detailed_notes                     varchar2(1000),
    date_created                       date,
    owner_id                           integer,
    parent_dn_id                       integer,
    entity_type                        varchar2(30),
    study_id                           integer
);

--
prompt Create table DISCREPANCY_NOTE_TYPE
--

create table DISCREPANCY_NOTE_TYPE (
    discrepancy_note_type_id           integer not null,
    name                               varchar2(50),
    description                        varchar2(255)
);

Rem SET default_with_oids = false;

--
prompt Create table DN_EVENT_CRF_MAP
--

create table DN_EVENT_CRF_MAP (
    event_crf_id                       integer,
    discrepancy_note_id                integer,
    column_name                        varchar2(255)
);

--
prompt Create table DN_ITEM_DATA_MAP
--

create table DN_ITEM_DATA_MAP (
    item_data_id                       integer,
    discrepancy_note_id                integer,
    column_name                        varchar2(255)
);

--
prompt Create table DN_STUDY_EVENT_MAP
--

create table DN_STUDY_EVENT_MAP (
    study_event_id                     integer,
    discrepancy_note_id                integer,
    column_name                        varchar2(255)
);

--
prompt Create table DN_STUDY_SUBJECT_MAP
--

create table DN_STUDY_SUBJECT_MAP (
    study_subject_id                   integer,
    discrepancy_note_id                integer,
    column_name                        varchar2(255)
);

Rem SET default_with_oids = true;

--
prompt Create table DN_SUBJECT_MAP
--

create table DN_SUBJECT_MAP (
    subject_id                         integer,
    discrepancy_note_id                integer,
    column_name                        varchar2(255)
);

--
prompt Create table EVENT_CRF
--

create table EVENT_CRF (
    event_crf_id                       integer not null,
    study_event_id                     integer,
    crf_version_id                     integer,
    date_interviewed                   date,
    interviewer_name                   varchar2(255),
    completion_status_id               integer,
    status_id                          integer,
    annotations                        varchar2(4000),
    date_completed                     timestamp(3),
    validator_id                       integer,
    date_validate                      date,
    date_validate_completed            timestamp(3),
    validator_annotations              varchar2(4000),
    validate_string                    varchar2(256),
    owner_id                           integer,
    date_created                       date,
    study_subject_id                   integer,
    date_updated                       date,
    update_id                          integer,
    electronic_signature_status        varchar2(1) default '0'
);

--
prompt Create table EVENT_DEFINITION_CRF
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
    ordinal integer,
    electronic_signature varchar2(1)
);

                                       
--
prompt Create table EXPORT_FORMAT
--

create table EXPORT_FORMAT (
    export_format_id                   integer not null,
    name                               varchar2(255),
    description                        varchar2(1000),
    mime_type                          varchar2(255)
);                                     

/* This is a materialized view and the table is not going to be created
--
prompt Create table EXTRACT_DATA_TABLE
--

create table EXTRACT_DATA_TABLE (
    subject_id                         integer,
    subject_identifier                 varchar2(30),
    study_id                           integer,
    study_identifier                   varchar2(30),
    event_definition_crf_id            integer,
    crf_id                             integer,
    crf_description                    varchar2(2048),
    crf_name                           varchar2(255),
    crf_version_id                     integer,
    crf_version_revision_notes         varchar2(255),
    crf_version_name                   varchar2(255),
    study_event_id                     integer,
    event_crf_id                       integer,
    item_data_id                       integer,
    value                              varchar2(255),
    study_event_definition_name        varchar2(2000),
    study_event_def_repeating          varchar2(1),
    sample_ordinal                     integer,
    item_id                            integer,
    item_name                          varchar2(255),
    item_description                   varchar2(4000),
    item_units                         varchar2(64),
    date_created                       date,
    study_event_definition_id          integer,
    options_text                       varchar2(4000),
    options_values                     varchar2(4000),
    response_type_id                   integer,
    gender                             varchar2(1),
    date_of_birth                      date,
    "location"                         varchar2(2000),
    date_start                         date,
    date_end                           date
    secondary_label                    varchar2(2000)
);
*/

--
prompt Create table FILTER
--

create table FILTER (
    filter_id                          integer not null,
    name                               varchar2(255),
    description                        varchar2(2000),
    sql_statement                      varchar2(4000),
    status_id                          integer,
    date_created                       date,
    date_updated                       date,
    owner_id                           integer not null,
    update_id                          integer
);                                     
                                       
--
prompt Create table FILTER_CRF_VERSION_MAP
--

create table FILTER_CRF_VERSION_MAP (
    filter_id                          integer,
    crf_version_id                     integer
);                                     
                                       
--
prompt Create table GROUP_CLASS_TYPES
--

create table GROUP_CLASS_TYPES (
    group_class_type_id                integer not null,
    name                               varchar2(255),
    description                        varchar2(1000)
);                                     
                                       
--
prompt Create table ITEM
--

create table ITEM (
    item_id                            integer not null,
    name                               varchar2(255),
    description                        varchar2(4000),
    units                              varchar2(64),
    phi_status                         varchar2(1),
    item_data_type_id                  integer,
    item_reference_type_id             integer,
    status_id                          integer,
    owner_id                           integer,
    date_created                       date,
    date_updated                       date,
    update_id                          integer,
    oc_oid                             varchar2(40) not null
);
                                       
--
prompt Create table ITEM_DATA
--

create table ITEM_DATA (
    item_data_id                       integer not null,
    item_id                            integer not null,
    event_crf_id                       integer,
    status_id                          integer,
    value                              varchar2(4000),
    date_created                       date,
    date_updated                       date,
    owner_id                           integer,
    update_id                          integer,
    ordinal                            integer
);                                     
                                       
--
prompt Create table ITEM_DATA_TYPE
--

create table ITEM_DATA_TYPE (
    item_data_type_id                  integer not null,
    code                               varchar2(20),
    name                               varchar2(255),
    definition                         varchar2(1000),
    reference                          varchar2(1000)
);                                     
                                       
--
prompt Create table ITEM_FORM_METADATA
--

create table ITEM_FORM_METADATA (
    item_form_metadata_id              integer not null,
    item_id                            integer not null,
    crf_version_id                     integer,
    "header"                           varchar2(2000),
    subheader                          varchar2(240),
    parent_id                          integer,
    parent_label                       varchar2(120),
    column_number                      integer,
    page_number_label                  varchar2(5),
    question_number_label              varchar2(20),
    left_item_text                     varchar2(4000),
    right_item_text                    varchar2(2000),
    section_id                         integer not null,
    decision_condition_id              integer,
    response_set_id                    integer not null,
    regexp                             varchar2(1000),
    regexp_error_msg                   varchar2(255),
    ordinal                            integer not null,
    required                           varchar2(1),
    default_value                      varchar2(4000),
    response_layout                    varchar2(255)
);
                                       
--
prompt Create table ITEM_GROUP
--

create table ITEM_GROUP (
    item_group_id                      integer not null,
    name                               varchar2(255),
    crf_id                             integer not null,
    status_id                          integer,
    date_created                       date,
    date_updated                       date,
    owner_id                           integer,
    update_id                          integer,
    oc_oid                             varchar2(40) not null
);                                     
                                       
--
prompt Create table ITEM_GROUP_METADATA
--

create table ITEM_GROUP_METADATA (
    item_group_metadata_id             integer not null,
    item_group_id                      integer not null,
    "header"                           varchar2(255),
    subheader                          varchar2(255),
    layout                             varchar2(100),
    repeat_number                      integer,
    repeat_max                         integer,
    repeat_array                       varchar2(255),
    row_start_number                   integer,
    crf_version_id                     integer not null,
    item_id                            integer not null,
    ordinal                            integer not null,
    borders                            integer
);                                     
                                       
--
prompt Create table ITEM_REFERENCE_TYPE
--

create table ITEM_REFERENCE_TYPE (
    item_reference_type_id             integer not null,
    name                               varchar2(255),
    description                        varchar2(1000)
);                                     
                                       
--
prompt Create table NULL_VALUE_TYPE
--

create table NULL_VALUE_TYPE (
    null_value_type_id                 integer not null,
    code                               varchar2(20),
    name                               varchar2(255),
    definition                         varchar2(1000),
    reference                          varchar2(1000)
);                                     
                                       
--
prompt Create table OPENCLINICA_VERSION
--

create table OPENCLINICA_VERSION (
    name                               varchar2(255),
    test_path                          varchar2(1000)
);                                     
                                       
--
prompt Create table PRIVILEGE
--

create table PRIVILEGE (
    priv_id                            integer not null,
    priv_name                          varchar2(50),
    priv_desc                          varchar2(2000)
);                                     
                                       
--
prompt Create table RESOLUTION_STATUS
--

create table RESOLUTION_STATUS (
    resolution_status_id               integer not null,
    name                               varchar2(50),
    description                        varchar2(255)
);                                     
                                       
--
prompt Create table RESPONSE_SET
--

create table RESPONSE_SET (
    response_set_id                    integer not null,
    response_type_id                   integer,
    label                              varchar2(80),
    options_text                       varchar2(4000),
    options_values                     varchar2(4000),
    version_id                         integer
);                                     
                                       
--
prompt Create table RESPONSE_TYPE
--

create table RESPONSE_TYPE (
    response_type_id                   integer not null,
    name                               varchar2(255),
    description                        varchar2(1000)
);                                     
                                       
--
prompt Create table ROLE_PRIVILEGE_MAP
--

create table ROLE_PRIVILEGE_MAP (
    role_id                            integer not null,
    priv_id                            integer not null,
    priv_value                         varchar2(50)
);                                     
                                       
--
prompt Create table SECTION
--

create table SECTION (
    section_id                         integer not null,
    crf_version_id                     integer not null,
    status_id                          integer,
    label                              varchar2(2000),
    title                              varchar2(2000),
    subtitle                           varchar2(2000),
    instructions                       varchar2(2000),
    page_number_label                  varchar2(5),
    ordinal                            integer,
    parent_id                          integer,
    date_created                       date,
    date_updated                       date,
    owner_id                           integer not null,
    update_id                          integer,
    borders                            integer
);                                     
                                       
--
prompt Create table STATUS
--

create table STATUS (
    status_id                          integer not null,
    name                               varchar2(255),
    description                        varchar2(1000)
);                                     
                                       
--
prompt Create table STUDY
--

create table STUDY (
    study_id                           integer not null,
    parent_study_id                    integer,
    unique_identifier                  varchar2(30),
    secondary_identifier               varchar2(255),
    name                               varchar2(255),
    summary                            varchar2(255),
    date_planned_start                 date,
    date_planned_end                   date,
    date_created                       date,
    date_updated                       date,
    owner_id                           integer,
    update_id                          integer,
    type_id                            integer,
    status_id                          integer,
    principal_investigator             varchar2(255),
    facility_name                      varchar2(255),
    facility_city                      varchar2(255),
    facility_state                     varchar2(20),
    facility_zip                       varchar2(64),
    facility_country                   varchar2(64),
    facility_recruitment_status        varchar2(60),
    facility_contact_name              varchar2(255),
    facility_contact_degree            varchar2(255),
    facility_contact_phone             varchar2(255),
    facility_contact_email             varchar2(255),
    protocol_type                      varchar2(30),
    protocol_description               varchar2(1000),
    protocol_date_verification         date,
    phase                              varchar2(30),
    expected_total_enrollment          integer,
    sponsor                            varchar2(255),
    collaborators                      varchar2(1000),
    medline_identifier                 varchar2(255),
    url                                varchar2(255),
    url_description                    varchar2(255),
    conditions                         varchar2(500),
    keywords                           varchar2(255),
    eligibility                        varchar2(500),
    gender                             varchar2(30),
    age_max                            varchar2(3),
    age_min                            varchar2(3),
    healthy_volunteer_accepted         varchar2(1),
    purpose                            varchar2(64),
    allocation                         varchar2(64),
    masking                            varchar2(30),
    control                            varchar2(30),
    "assignment"                       varchar2(30),
    endpoint                           varchar2(64),
    interventions                      varchar2(1000),
    duration                           varchar2(30),
    selection                          varchar2(30),
    timing                             varchar2(30),
    official_title                     varchar2(255),
    results_reference                  varchar2(1),
    oc_oid                             varchar2(40)
);
                                       
--
prompt Create table STUDY_EVENT
--

create table STUDY_EVENT (
    study_event_id                     integer not null,
    study_event_definition_id          integer,
    study_subject_id                   integer,
    "location"                         varchar2(2000),
    sample_ordinal                     integer,
    date_start                         timestamp(3),
    date_end                           timestamp(3),
    owner_id                           integer,
    status_id                          integer,
    date_created                       date,
    date_updated                       date,
    update_id                          integer,
    subject_event_status_id            integer,
    start_time_flag                    varchar2(1),
    end_time_flag                      varchar2(1)
);
                                       
--
prompt Create table STUDY_EVENT_DEFINITION
--

create table STUDY_EVENT_DEFINITION (
    study_event_definition_id          integer not null,
    study_id                           integer,
    name                               varchar2(2000),
    description                        varchar2(2000),
    repeating                          varchar2(1),
    "type"                             varchar2(20),
    category                           varchar2(2000),
    owner_id                           integer,
    status_id                          integer,
    date_created                       date,
    date_updated                       date,
    update_id                          integer,
    ordinal                            integer,
    oc_oid                             varchar2(40) not null
);                                     
                                       
--
prompt Create table STUDY_GROUP
--

create table STUDY_GROUP (
    study_group_id                     integer not null,
    name                               varchar2(255),
    description                        varchar2(1000),
    study_group_class_id               integer
);                                     
                                       
--
prompt Create table STUDY_GROUP_CLASS
--

create table STUDY_GROUP_CLASS (
    study_group_class_id               integer not null,
    name                               varchar2(30),
    study_id                           integer,
    owner_id                           integer,
    date_created                       date,
    group_class_type_id                integer,
    status_id                          integer,
    date_updated                       date,
    update_id                          integer,
    subject_assignment                 varchar2(30)
);
                                       
--
prompt Create table STUDY_PARAMETER
--

create table STUDY_PARAMETER (
    study_parameter_id                 integer not null,
    handle                             varchar2(50),
    name                               varchar2(50),
    description                        varchar2(255),
    default_value                      varchar2(50),
    inheritable                        varchar2(1) default '1',
    overridable                        varchar2(1)
);                                     
                                       
--
prompt Create table STUDY_PARAMETER_VALUE
--

create table STUDY_PARAMETER_VALUE (
    study_parameter_value_id           integer not null,
    study_id                           integer,
    value                              varchar2(50),
    parameter                          varchar2(50)
);                                     
                                       
--
prompt Create table STUDY_SUBJECT
--

create table STUDY_SUBJECT (
    study_subject_id                   integer not null,
    label                              varchar2(30),
    secondary_label                    varchar2(30),
    subject_id                         integer,
    study_id                           integer,
    status_id                          integer,
    enrollment_date                    date,
    date_created                       date,
    date_updated                       date,
    owner_id                           integer,
    update_id                          integer,
    oc_oid                             varchar2(40) not null
);                                     

--
prompt Create table STUDY_TYPE
--

create table STUDY_TYPE (
    study_type_id                      integer not null,
    name                               varchar2(255),
    description                        varchar2(1000)
);                                     
                                       
--
prompt Create table STUDY_USER_ROLE
--

create table STUDY_USER_ROLE (
    role_name                          varchar2(40),
    study_id                           integer,
    status_id                          integer,
    owner_id                           integer,
    date_created                       date,
    date_updated                       date,
    update_id                          integer,
    user_name                          varchar2(40)
);                                     
                                       
--
prompt Create table SUBJECT
--

create table SUBJECT (
    subject_id                         integer not null,
    father_id                          integer,
    mother_id                          integer,
    status_id                          integer,
    date_of_birth                      date,
    gender                             varchar2(1),
    unique_identifier                  varchar2(255),
    date_created                       date,
    owner_id                           integer,
    date_updated                       date,
    update_id                          integer,
    dob_collected                      varchar2(1)
);                                     
                                       
--
prompt Create table SUBJECT_EVENT_STATUS
--

create table SUBJECT_EVENT_STATUS (
    subject_event_status_id            integer not null,
    name                               varchar2(255),
    description                        varchar2(1000)
);                                     
                                       
--
prompt Create table SUBJECT_GROUP_MAP
--

create table SUBJECT_GROUP_MAP (
    subject_group_map_id               integer not null,
    study_group_class_id               integer,
    study_subject_id                   integer,
    study_group_id                     integer,
    status_id                          integer,
    owner_id                           integer,
    date_created                       date,
    date_updated                       date,
    update_id                          integer,
    notes                              varchar2(255)
);                                     
                                       
--
prompt Create table USER_ACCOUNT
--

create table USER_ACCOUNT (
    user_id                            integer not null,
    user_name                          varchar2(64),
    passwd                             varchar2(255),
    first_name                         varchar2(50),
    last_name                          varchar2(50),
    email                              varchar2(120),
    active_study                       integer,
    institutional_affiliation          varchar2(255),
    status_id                          integer,
    owner_id                           integer,
    date_created                       date,
    date_updated                       date,
    date_lastvisit                     timestamp(3),
    passwd_timestamp                   date,
    passwd_challenge_question          varchar2(64),
    passwd_challenge_answer            varchar2(255),
    phone                              varchar2(64),
    user_type_id                       integer,
    update_id                          integer
);                                     
                                       
--
prompt Create table USER_ROLE
--

create table USER_ROLE (
    role_id                            integer not null,
    role_name                          varchar2(50) not null,
    parent_id                          integer,
    role_desc                          varchar2(2000)
);                                     
                                       
--
prompt Create table USER_TYPE
--

create table USER_TYPE (
    user_type_id                       integer not null,
    user_type                          varchar2(50)
);                                     
                                       
--
prompt Create table VERSIONING_MAP
--

create table VERSIONING_MAP (
    crf_version_id                     integer,
    item_id                            integer
);                                     
                                       
--                                     
-- Rules Implementation                
--                                     
                                       
--
prompt Create table RULE
--

create table RULE
(
   rule_id                             integer not null,
   name                                varchar2(255),
   description                         varchar2(255),
   oc_oid                              varchar2(40),
   enabled                             varchar2(1),
   rule_expression_id                  integer not null,
   owner_id                            integer,
   date_created                        date,
   date_updated                        date,
   update_id                           integer,
   status_id                           integer
);

--
prompt Create table RULE_SET
--

create table RULE_SET
(
   rule_set_id                         integer not null,
   rule_expression_id                  integer not null,
   study_event_definition_id           integer not null,
   crf_id                              integer,
   crf_version_id                      integer,
   study_id                            integer not null,
   owner_id                            integer,
   date_created                        date,
   date_updated                        date,
   update_id                           integer,
   status_id                           integer
);

--
prompt Create table RULE_SET_AUDIT
--

create table RULE_SET_AUDIT
(
   rule_set_audit_id                   integer not null,
   rule_set_id                         integer not null,
   date_updated                        date,
   updater_id                          integer,
   status_id                           integer
);

--
prompt Create table RULE_SET_RULE
--

create table RULE_SET_RULE
(
   rule_set_rule_id                    integer not null,
   rule_set_id                         integer not null,
   rule_id                             integer not null,
   owner_id                            integer,
   date_created                        date,
   date_updated                        date,
   update_id                           integer,
   status_id                           integer
);

--
prompt Create table RULE_SET_RULE_AUDIT
--

create table RULE_SET_RULE_AUDIT
(
   rule_set_rule_audit_id              integer not null,
   rule_set_rule_id                    integer not null,
   date_updated                        date,
   updater_id                          integer,
   status_id                           integer
);

--
prompt Create table RULE_ACTION
--

create table RULE_ACTION
(
   rule_action_id                      integer not null,
   rule_set_rule_id                    integer not null,
   action_type                         integer not null,
   expression_evaluates_to             varchar2(1) not null,
   message                             varchar2(255),
   email_to                            varchar2(255),
   owner_id                            integer,
   date_created                        date,
   date_updated                        date,
   update_id                           integer,
   status_id                           integer
);

--
prompt Create table RULE_EXPRESSION
--

create table RULE_EXPRESSION
(
   rule_expression_id                  integer not null,
   value                               varchar2(1025) not null,
   context                             integer not null,
   owner_id                            integer,
   date_created                        date,
   date_updated                        date,
   update_id                           integer,
   status_id                           integer
);

