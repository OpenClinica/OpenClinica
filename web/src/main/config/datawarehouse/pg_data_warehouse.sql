
-- View: "extract_data_table"
-- updated 7-8-07, added lots of new options and three tables
-- designed to do all the work as a cron job and not during run-time.
-- other updates per changing requirements on 7-19-07, tbh
-- enable fetching data when there is no match in sgmap, 11-12-2007, ywang
-- added new columns and modified joins, 06-2008, ywang

DROP TABLE extract_data_table;

CREATE TABLE extract_data_table AS

 SELECT ss.subject_id,
	ss.label AS subject_identifier,
	su.study_id,
	su.unique_identifier AS study_identifier,
	edc.event_definition_crf_id, crf.crf_id,
	crf.description AS crf_description,
	crf.name AS crf_name, crfv.crf_version_id,
	crfv.revision_notes AS crf_version_revision_notes,
	crfv.name AS crf_version_name,
	se.study_event_id,
	ec.event_crf_id, id.item_data_id, id.value,
	sed.name AS study_event_definition_name,
	sed.repeating AS study_event_definition_repeating,
	se.sample_ordinal,
	it.item_id,
	it.name AS item_name,
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
	ec.date_validate_completed AS event_crf_date_validate_completed,
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
	se.location,
	se.date_start,
	se.date_end,
	--another column added, tbh 08/2007
	id.ordinal AS item_data_ordinal,
	--added new columns, ywang 06/2008
	case when igm.item_id > 0 then ig.name else 'Ungrouped'
	end as item_group_name,
	ss.secondary_label,
	it.item_data_type_id,
	sed.oc_oid as study_event_definition_oid,
	crfv.oc_oid as crf_version_oid,
	case when igm.item_id > 0 then ig.oc_oid else
	(select distinct iig.oc_oid from item_group iig
	where iig.name = 'Ungrouped' and iig.crf_id = crfv.crf_id)
	end as item_group_oid,
	it.oc_oid as item_oid,
	ss.oc_oid as study_subject_oid

FROM
study_subject ss join study su on su.study_id::numeric = ss.study_id
join subject s on ss.subject_id = s.subject_id::numeric
join study_event_definition sed on (su.study_id::numeric = sed.study_id or su.parent_study_id = sed.study_id)
join study_event se on ss.study_subject_id = se.study_subject_id
and sed.study_event_definition_id::numeric = se.study_event_definition_id
left join subject_group_map sgmap on se.study_subject_id = sgmap.study_subject_id
join event_definition_crf edc on sed.study_event_definition_id::numeric = edc.study_event_definition_id
and (su.study_id::numeric = edc.study_id or su.parent_study_id = edc.study_id)
join crf on edc.crf_id = crf.crf_id::numeric
join event_crf ec on se.study_event_id = ec.study_event_id and se.study_subject_id = ec.study_subject_id
and (ec.status_id = 2::numeric or ec.status_id = 6::numeric)
join crf_version crfv on ec.crf_version_id = crfv.crf_version_id and crf.crf_id::numeric = crfv.crf_id
join item_form_metadata ifm on crfv.crf_version_id = ifm.crf_version_id
join item it on ifm.item_id = it.item_id::numeric
join item_data id on ifm.item_id = id.item_id and ec.event_crf_id = id.event_crf_id
and (id.status_id = 2::numeric or id.status_id = 6::numeric)
join response_set rs on ifm.response_set_id = rs.response_set_id::numeric
left join item_group_metadata igm on ifm.item_id = igm.item_id
and crfv.crf_version_id::numeric = igm.crf_version_id
left join item_group ig on igm.item_group_id = ig.item_group_id::numeric and crf.crf_id::numeric = ig.crf_id;

ALTER TABLE extract_data_table OWNER TO clinica;
