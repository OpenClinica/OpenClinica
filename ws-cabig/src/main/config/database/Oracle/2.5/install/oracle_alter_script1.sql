/*--------------------------------------------------------------------------
*
* File       : oracle_alter_script1.sql
*
* Subject    : Doubtful now
*
* Parameters : None
*
* Conditions : Create the table before creating the constraints
*
* Author/Dt  : Shriram  05/14/2008
*
* Comments   : None
*
--------------------------------------------------------------------------*/

---
prompt OID Implementation
---
--
prompt Altering table crf
--
ALTER TABLE crf ADD ( oc_oid varchar2(40) );

--
prompt Altering table study_event_definition
--
ALTER TABLE study_event_definition ADD ( oc_oid varchar2(40) );

--
prompt Altering table crf_version
--
ALTER TABLE crf_version ADD ( oc_oid varchar2(40) );

--
prompt Altering table item_group
--
ALTER TABLE item_group ADD ( oc_oid varchar2(40) );

--
prompt Altering table item
--
ALTER TABLE item ADD ( oc_oid varchar2(40) );

--
prompt Altering table study
--
ALTER TABLE study ADD ( oc_oid varchar2(40) );

--
prompt Altering table study_subject
--
ALTER TABLE study_subject ADD ( oc_oid varchar2(40) );




@@oracle_update_default_data.sql

prompt make new columns not null
--
prompt Altering table crf
--
ALTER TABLE crf MODIFY ( oc_oid NOT NULL );

--
prompt Altering table study_event_definition
--
ALTER TABLE study_event_definition MODIFY ( oc_oid NOT NULL );

--
prompt Altering table crf_version
--
ALTER TABLE crf_version MODIFY ( oc_oid NOT NULL );

--
prompt Altering table item_group
--
ALTER TABLE item_group MODIFY ( oc_oid NOT NULL );

--
prompt Altering table item
--
ALTER TABLE item MODIFY ( oc_oid NOT NULL );

--
prompt Altering table study
--
ALTER TABLE study MODIFY ( oc_oid NOT NULL );

--
prompt Altering table study_subject
--
ALTER TABLE study_subject MODIFY ( oc_oid NOT NULL );

--
prompt Altering table audit_log_event
--
ALTER TABLE audit_log_event ADD study_event_id INTEGER;

--
prompt Altering table audit_log_event
--
ALTER TABLE audit_log_event ADD event_crf_version_id INTEGER;

--
prompt Altering table dataset
--
ALTER TABLE dataset ADD show_secondary_id varchar2(1) default '0';

--
prompt Altering table extract_data_table (Materialized View and there is no need)
--
-- ALTER TABLE extract_data_table ADD secondary_label varchar2(2000);



--
prompt Create primary key on RULE_ID - Table RULE
--

alter table RULE
   add constraint rule_id_pkey PRIMARY KEY (rule_id);


--
prompt Create primary key on RULE_SET_ID - Table RULE_SET
--

alter table RULE_SET
   add constraint rule_set_id_pkey PRIMARY KEY (rule_set_id);


--
prompt Create primary key on RULE_SET_AUDIT_ID - Table RULE_SET_AUDIT
--

alter table RULE_SET_AUDIT
   add constraint rule_set_audit_id_pkey PRIMARY KEY (rule_set_audit_id);


--
prompt Create primary key on RULE_SET_RULE_ID - Table RULE_SET_RULE
--

alter table RULE_SET_RULE
   add constraint rule_set_rule_id_pkey PRIMARY KEY (rule_set_rule_id);


--
prompt Create primary key on RULE_SET_RULE_ID - Table RULE_SET_RULE_AUDIT
--

alter table RULE_SET_RULE_AUDIT
   add constraint rule_set_rule_audit_id_pkey PRIMARY KEY (rule_set_rule_audit_id);


--
prompt Create primary key on RULE_ACTION_ID - Table RULE_ACTION
--

alter table RULE_ACTION
   add constraint rule_action_id_pkey PRIMARY KEY (rule_action_id);


--
prompt Create primary key on RULE_EXPRESSION_ID - Table RULE_EXPRESSION
--

alter table RULE_EXPRESSION
    add constraint expression_id_pkey PRIMARY KEY (rule_expression_id);


