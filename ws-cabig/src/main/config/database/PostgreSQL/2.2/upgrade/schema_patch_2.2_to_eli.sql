
-- new response_types have been added. ywang, 1/8/2007
INSERT INTO response_type VALUES (8, 'calculation', 'value calculated automatically');
INSERT INTO response_type VALUES (9, 'group-calculation', 'value calculated automatically from an entire group of items');

-- add more columns in the table <dataset>.
ALTER TABLE dataset ADD odm_metadataversion_name varchar(255);
ALTER TABLE dataset ADD odm_metadataversion_oid varchar(255);
ALTER TABLE dataset ADD odm_prior_study_oid varchar(255);
ALTER TABLE dataset ADD odm_prior_metadataversion_oid varchar(255);