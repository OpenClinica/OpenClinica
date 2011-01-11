/*------------------------------------------------------------------------------
*
* File       : oracle_create_mv.sql
*
* Subject    : Creates the materialized view which will be refreshed
*               automatically.
*
* Parameters : None
*
* Conditions : Need "create materialized view" privilege. In 10g
*              "Create Materialized View" is not part of
*              "CResource" roles by default
*               Tables should exist before running this script
*
* Author/Dt  : Shriram Mani 05/09/2008
*
* Comments   : None
*
------------------------------------------------------------------------------*/
--
prompt creating materialized view extract_data_table
--
CREATE MATERIALIZED VIEW extract_data_table
BUILD IMMEDIATE
REFRESH COMPLETE START WITH TRUNC(SYSDATE)
-- Refresh every day at 12:01 AM
NEXT TRUNC(SYSDATE+1)+(1/24/60)
AS
--
SELECT 
       ss.subject_id,
       ss.label AS subject_identifier,
       su.study_id,
       su.unique_identifier AS study_identifier,
       edc.event_definition_crf_id, crf.crf_id,
       crf.description AS crf_description,
       crf.NAME AS crf_name, crfv.crf_version_id,
       crfv.revision_notes AS crf_version_revision_notes,
       crfv.NAME AS crf_version_name,
       se.study_event_id,
       ec.event_crf_id, ID.item_data_id, 
       ID.VALUE,
       sed.NAME AS study_event_definition_name,
       sed.repeating AS study_event_def_repeating,
       se.sample_ordinal,
       it.item_id,
       it.NAME AS item_name,
       it.description AS item_description,
       it.units AS item_units,
       ss.enrollment_date AS date_created,
       sed.study_event_definition_id,
       rs.options_text,
       rs.options_values,
       rs.response_type_id,
       s.gender,
       s.date_of_birth,
       -- add new columns below, tbh
       --
       s.status_id AS subject_status_id,
       s.unique_identifier,
       s.dob_collected,
       ec.completion_status_id,
       ec.date_created AS event_crf_start_time,
       crfv.status_id AS crf_version_status_id,
       ec.interviewer_name,
       ec.date_interviewed,
       ec.date_completed AS event_crf_date_completed,
       ec.date_validate_completed AS event_crf_date_valid_completed,
       sgmap.study_group_id,
       sgmap.study_group_class_id,
       --removing five columns below, adding two above.
       --
       --dn.description AS discrepancy_note_description,
       --dn.resolution_status_id AS discrepancy_resolution_status_id,
       --dn.detailed_notes,
       --dn.discrepancy_note_type_id,
       --
       -- added new columns above, tbh
       se."location",
       se.date_start,
       se.date_end,
       --another column added, tbh 08/2007
       ID.ordinal AS item_data_ordinal,
       ig.NAME AS item_group_name
       --
  FROM study su, 
       subject s, 
       event_definition_crf edc, 
       crf, 
       crf_version crfv,
       study_event se, 
       subject_group_map sgmap,
       event_crf ec, 
       item_data ID, 
       item it, 
       study_subject ss,
       study_event_definition sed, 
       item_form_metadata ifm, 
       response_set rs,
       item_group ig,
       item_group_metadata igm
       --, dn_item_data_map didmap, discrepancy_note dn
       --
 WHERE it.item_id = ID.item_id
   AND se.study_subject_id = sgmap.study_subject_id (+)
   AND ifm.item_id = ID.item_id
   AND ifm.response_set_id = rs.response_set_id
   AND ID.event_crf_id = ec.event_crf_id
   AND ec.crf_version_id = crfv.crf_version_id
   AND ec.study_event_id = se.study_event_id
   AND se.study_subject_id = ss.study_subject_id
   AND ss.subject_id = s.subject_id
   AND se.study_event_definition_id = sed.study_event_definition_id
   AND sed.study_id = su.study_id
   AND sed.study_event_definition_id = edc.study_event_definition_id
   AND edc.crf_id = crf.crf_id
   AND crf.crf_id = crfv.crf_id
   --AND ig.item_group_id IN (SELECT ig.item_group_id from item_group where crf_id=crf.crf_id and )
   AND crf.crf_id = ig.crf_id
   --AND crfv.crf_id = ig.crf_id
   AND it.item_id = igm.item_id
   AND ig.item_group_id = igm.item_group_id
   AND crfv.crf_version_id = igm.crf_version_id
   --AND didmap.item_data_id = id.item_data_id
   --AND dn.discrepancy_note_id = didmap.discrepancy_note_id
   AND (ID.status_id = 2 OR ID.status_id = 6)
   AND (ec.status_id = 2 OR ec.status_id = 6)
/
