/* Updates the schema for the OpenClinica application
   Can be applied to an existing OpenClinica 2.0.1 database to make it equivilant to OpenClinica-2.2 schema
   Authors: thicherson, bperry, jxu, ywang
*/
SET client_encoding = 'SQL_ASCII';
SET check_function_bodies = false;
--Don't show warning messages during installation
SET client_min_messages = error;
SET search_path = public, pg_catalog;
SET default_tablespace = '';
SET default_with_oids = true;


-- Bug fix with study character length
ALTER TABLE study ALTER name TYPE varchar(255);



-- Form builder 

-- the following two tables are inside the old data model, but never used
DROP TABLE item_group_map;
DROP TABLE item_group;

CREATE TABLE item_group
(
  item_group_id serial NOT NULL,
  name varchar(255),
  crf_id numeric NOT NULL,
  status_id numeric,
  date_created date,
  date_updated date,
  owner_id numeric,
  update_id numeric,
  CONSTRAINT pk_item_group PRIMARY KEY (item_group_id),
  CONSTRAINT fk_item_gro_fk_item_g_status FOREIGN KEY (status_id) REFERENCES status (status_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_item_gro_fk_item_g_user_acc FOREIGN KEY (owner_id) REFERENCES user_account (user_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_item_group_crf FOREIGN KEY (crf_id) REFERENCES crf (crf_id) ON UPDATE RESTRICT ON DELETE RESTRICT
) 
WITH OIDS;
ALTER TABLE item_group OWNER TO clinica;


CREATE TABLE item_group_metadata
(
  item_group_metadata_id serial NOT NULL,
  item_group_id numeric NOT NULL,
  header varchar(255),
  subheader varchar(255),
  layout varchar(100),
  repeat_number numeric,
  repeat_max numeric,
  repeat_array varchar(255),
  row_start_number numeric,
  crf_version_id numeric NOT NULL,
  item_id numeric NOT NULL,
  ordinal numeric NOT NULL,
  borders numeric,
  CONSTRAINT pk_item_group_metadata PRIMARY KEY (item_group_metadata_id),
  CONSTRAINT fk_crf_metadata FOREIGN KEY (crf_version_id) REFERENCES crf_version (crf_version_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item (item_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_item_group FOREIGN KEY (item_group_id) REFERENCES item_group (item_group_id) ON UPDATE RESTRICT ON DELETE RESTRICT
) 
WITH OIDS;
ALTER TABLE item_group_metadata OWNER TO clinica;


alter table item_data add ordinal numeric null;


-- for supporting the addition of new characteristics in data sets
ALTER TABLE dataset ADD COLUMN show_event_status bool DEFAULT false;
ALTER TABLE dataset ADD COLUMN show_subject_status bool DEFAULT false;
ALTER TABLE dataset ADD COLUMN show_subject_unique_id bool DEFAULT false;
ALTER TABLE dataset ADD COLUMN show_subject_age_at_event bool DEFAULT false;
ALTER TABLE dataset ADD COLUMN show_crf_status bool DEFAULT false;
ALTER TABLE dataset ADD COLUMN show_crf_version bool DEFAULT false;
ALTER TABLE dataset ADD COLUMN show_crf_int_name bool DEFAULT false;
ALTER TABLE dataset ADD COLUMN show_crf_int_date bool DEFAULT false;
ALTER TABLE dataset ADD COLUMN show_group_info bool DEFAULT false;
ALTER TABLE dataset ADD COLUMN show_disc_info bool DEFAULT false;

-- also for supporting the addition of subject groups to be assoc with datasets
CREATE TABLE dataset_study_group_class_map
(
  dataset_id integer NOT NULL,
  study_group_class_id integer NOT NULL,
  CONSTRAINT fk_dataset__ref_study_group_class FOREIGN KEY (study_group_class_id) REFERENCES study_group_class (study_group_class_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_dataset__reference_dataset FOREIGN KEY (dataset_id) REFERENCES dataset (dataset_id) ON UPDATE RESTRICT ON DELETE RESTRICT
) 
WITH OIDS;
ALTER TABLE dataset_study_group_class_map OWNER TO clinica;

-- increase sizes of fields for tables
ALTER TABLE item ALTER description TYPE varchar(4000);
ALTER TABLE item_data ALTER value TYPE varchar(4000);
ALTER TABLE item_form_metadata ALTER left_item_text TYPE varchar(4000);

-- add default_value column for item_form_metadata table
ALTER TABLE item_form_metadata ADD COLUMN default_value varchar(4000);

-- add response_layout column for item_form_metadata table
ALTER TABLE item_form_metadata ADD COLUMN response_layout varchar(255);

-- for the table study_event: 
-- change types of both columns date_start and date_end from date to timestamp
-- and add two columns start_time_flag and end_time_flag. empty column or null will be treated as 'false'
ALTER TABLE study_event ALTER date_start TYPE timestamp;
ALTER TABLE study_event ALTER date_end TYPE timestamp;
ALTER TABLE study_event ADD COLUMN start_time_flag bool;
ALTER TABLE study_event ADD COLUMN end_time_flag bool;


-- to fix general issue of remove and restore

insert into status (status_id,name, description)
 values (7,'auto-removed','this indicates that a record is removed due to the removal of its parent record');

UPDATE status SET description = 'this indicates that a record is specifically removed by user'

 WHERE name = 'removed';
 

-- update new values for the group_class_types table
UPDATE group_class_types SET name = 'Arm' WHERE group_class_type_id = 1;
UPDATE group_class_types SET name = 'Family/Pedigree' WHERE group_class_type_id = 2;
INSERT INTO group_class_types (group_class_type_id, name) VALUES (3, 'Demographic');
INSERT INTO group_class_types (group_class_type_id, name) VALUES (4, 'Other');
 


