--changes added by jun,06-02-2005
alter table dataset add column show_event_location boolean default false;
alter table dataset add column show_event_start boolean default false;
alter table dataset add column show_event_end boolean default false;
alter table dataset add column show_subject_dob boolean default false;
alter table dataset add column show_subject_gender boolean default false;

-- Table: test_table_three



CREATE TABLE test_table_three
(
  subject_id numeric,
  subject_identifier varchar(30),
  study_id int4,
  study_identifier varchar(30),
  event_definition_crf_id int4,
  crf_id int4,
  crf_description varchar(2048),
  crf_name varchar(255),
  crf_version_id int4,
  crf_version_revision_notes varchar(255),
  crf_version_name varchar(255),
  study_event_id int4,
  event_crf_id int4,
  item_data_id int4,
  value varchar(255),
  study_event_definition_name varchar(2000),
  study_event_definition_repeating bool,
  sample_ordinal numeric,
  item_id int4,
  item_name varchar(255),
  item_description varchar(4000),
  item_units varchar(64),
  date_created date,
  study_event_definition_id int4,
  options_text varchar(4000),
  options_values varchar(4000),
  response_type_id numeric,
  gender char(1),
  date_of_birth date,
  "location" varchar(2000),
  date_start date,
  date_end date
) 
WITH OIDS;
ALTER TABLE test_table_three OWNER TO postgres;


-- Table: openclinica_version

DROP TABLE openclinica_version;

CREATE TABLE openclinica_version
(
  name varchar(255),
  test_path varchar(1000)
) 
WITH OIDS;
ALTER TABLE openclinica_version OWNER TO postgres;

