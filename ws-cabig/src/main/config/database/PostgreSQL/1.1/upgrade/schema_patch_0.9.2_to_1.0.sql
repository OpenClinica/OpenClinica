ALTER TABLE study_group RENAME TO study_group_class;

ALTER TABLE group_role RENAME TO study_group;

ALTER TABLE group_types RENAME TO group_class_types;

ALTER TABLE study_group_class RENAME COLUMN study_group_id
TO study_group_class_id;

ALTER TABLE study_group_class ADD COLUMN subject_assignment VARCHAR(30);

ALTER TABLE group_class_types RENAME COLUMN group_type_id
TO group_class_type_id;

ALTER TABLE study_group RENAME COLUMN group_role_id
TO study_group_id;

ALTER TABLE subject_group_map RENAME COLUMN study_group_id
TO study_group_class_id;

ALTER TABLE subject_group_map RENAME COLUMN group_role_id
TO study_group_id;

ALTER TABLE subject_group_map ADD COLUMN notes VARCHAR(255);

ALTER TABLE study_group ADD COLUMN study_group_class_id numeric;

ALTER TABLE study_group
   add constraint fk_group_class_study_group foreign key (study_group_class_id)
      references study_group_class (study_group_class_id)
      on delete set null on update restrict;
      
ALTER TABLE study_group_class RENAME COLUMN group_type_id
TO group_class_type_id;      

insert into group_class_types values(3,'Demographic','');
update group_class_types set name='Arm' where group_class_type_id=1;
update group_class_types set name='Family/Pedigree' where group_class_type_id=2;

--table modified to accomodate changes to auditing, tbh, 9-14-2005

ALTER TABLE audit_event ADD COLUMN action_message VARCHAR(4000);


ALTER TABLE study ADD COLUMN discrepancy_management bool;

---added by jxu,09/15/2005

CREATE TABLE discrepancy_note_type
(
  discrepancy_note_type_id serial NOT NULL,
  name varchar(50),
  description varchar(255),
  CONSTRAINT discrepancy_note_type_pkey PRIMARY KEY (discrepancy_note_type_id)
) 
WITH OIDS;

ALTER TABLE discrepancy_note_type OWNER TO clinica;

insert into discrepancy_note_type values(1,'Failed Validation Check','');

insert into discrepancy_note_type values(2,'Incomplete','');

insert into discrepancy_note_type values(3,'Unclear/Unreadable','');

insert into discrepancy_note_type values(4,'Annotation','');

insert into discrepancy_note_type values(5,'Other','');


CREATE TABLE resolution_status
(
  resolution_status_id serial NOT NULL,
  name varchar(50),
  description varchar(255),
  CONSTRAINT resolution_status_pkey PRIMARY KEY (resolution_status_id)
) 
WITH OIDS;
ALTER TABLE resolution_status OWNER TO clinica;

insert into resolution_status values(1,'New/Open','');

insert into resolution_status values(2,'Updated','');

insert into resolution_status values(3,'Resolved/Closed','');


CREATE TABLE discrepancy_note
(
  discrepancy_note_id serial NOT NULL,
  description varchar(255),
  discrepancy_note_type_id numeric,
  resolution_status_id numeric,
  detailed_notes varchar(1000),
  date_created date,
  owner_id numeric,
  parent_dn_id numeric,
  CONSTRAINT discrepancy_note_pkey PRIMARY KEY (discrepancy_note_id),
  CONSTRAINT discrepancy_note_discrepancy_note_type_id_fkey FOREIGN KEY (discrepancy_note_type_id) REFERENCES discrepancy_note_type (discrepancy_note_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT discrepancy_note_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES user_account (user_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT discrepancy_note_resolution_status_id_fkey FOREIGN KEY (resolution_status_id) REFERENCES resolution_status (resolution_status_id) ON UPDATE RESTRICT ON DELETE RESTRICT
) 
WITH OIDS;
ALTER TABLE discrepancy_note OWNER TO clinica;

CREATE TABLE dn_event_crf_map
(
  event_crf_id numeric,
  discrepancy_note_id numeric,
  column_name varchar(255),
  CONSTRAINT dn_event_crf_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note (discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT dn_event_crf_map_event_crf_id_fkey FOREIGN KEY (event_crf_id) REFERENCES event_crf (event_crf_id) ON UPDATE RESTRICT ON DELETE RESTRICT
) 
WITHOUT OIDS;
ALTER TABLE dn_event_crf_map OWNER TO clinica;



CREATE TABLE dn_item_data_map
(
  item_data_id numeric,
  discrepancy_note_id numeric,
  column_name varchar(255),
  CONSTRAINT dn_item_data_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note (discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT dn_item_data_map_item_data_id_fkey FOREIGN KEY (item_data_id) REFERENCES item_data (item_data_id) ON UPDATE RESTRICT ON DELETE RESTRICT
) 
WITHOUT OIDS;
ALTER TABLE dn_item_data_map OWNER TO clinica;


CREATE TABLE dn_study_event_map
(
  study_event_id numeric,
  discrepancy_note_id numeric,
  column_name varchar(255),
  CONSTRAINT dn_study_event_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note (discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT dn_study_event_map_study_event_id_fkey FOREIGN KEY (study_event_id) REFERENCES study_event (study_event_id) ON UPDATE RESTRICT ON DELETE RESTRICT
) 
WITHOUT OIDS;
ALTER TABLE dn_study_event_map OWNER TO clinica;


CREATE TABLE dn_study_subject_map
(
  study_subject_id numeric,
  discrepancy_note_id numeric,
  column_name varchar(255),
  CONSTRAINT dn_study_subject_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note (discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT dn_study_subject_map_study_subject_id_fkey FOREIGN KEY (study_subject_id) REFERENCES study_subject (study_subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT
) 
WITHOUT OIDS;
ALTER TABLE dn_study_subject_map OWNER TO clinica;


CREATE TABLE dn_subject_map
(
  subject_id numeric,
  discrepancy_note_id numeric,
  column_name varchar(255),
  CONSTRAINT dn_subject_map_discrepancy_note_id_fkey FOREIGN KEY (discrepancy_note_id) REFERENCES discrepancy_note (discrepancy_note_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT dn_subject_map_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject (subject_id) ON UPDATE RESTRICT ON DELETE RESTRICT
) 
WITH OIDS;
ALTER TABLE dn_subject_map OWNER TO clinica;

ALTER TABLE discrepancy_note ADD COLUMN event_type VARCHAR(30);

INSERT INTO USER_TYPE (USER_TYPE) VALUES ('tech-admin');
--this should be in basecase already, tbh
ALTER TABLE discrepancy_note ADD COLUMN study_id numeric;
ALTER TABLE discrepancy_note ADD CONSTRAINT discrepancy_note_study_id_fkey FOREIGN KEY (study_id) REFERENCES study (study_id) ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE discrepancy_note RENAME event_type TO entity_type;


--added bu jxu, 09-28
insert into item_data_type values(9,'DATE','date','','');