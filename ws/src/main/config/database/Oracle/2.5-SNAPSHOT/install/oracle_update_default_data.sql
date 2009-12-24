/*--------------------------------------------------------------------------
*
* File       : oracle_update_default_data.sql
*
* Subject    : Update the seeded/current data
*
* Parameters : None
*
* Conditions : Tables/Data should exists before running this script.
*
* Author/Dt  : Shriram Mani 05/13/2008
*
* Comments   : None
*
--------------------------------------------------------------------------*/

--
prompt generate OID's for crf
--
update crf set oc_oid  =
'F_' ||
substr(upper(regexp_replace(crf.name, '\\s+|\\W+', '', 1, 0)),1,5) || '_' ||
crf_id ;

-- generate OID's for study event definitions
update study_event_definition set oc_oid =
'SE_' ||
substr(upper(regexp_replace(name, '\\s+|\\W+', '', 1, 0)),1,9) || study_event_definition_id;

-- generate OID's for crf versions
update crf_version
   set oc_oid  =
                ( select crf.oc_oid || '_' ||
                        substr(upper(regexp_replace(crf_version.name, '\\s+|\\W+', '', 1, 0)),1,5) ||
                        crf_version.crf_version_id
                  from crf
                 where crf_version.crf_id=crf.crf_id
                  );

-- Should work because crf_version doesn't exist without crf
--
prompt generate OID's for item groups
--
update item_group ig
   set ig.oc_oid  =
                 (select 'IG_' ||
                         substr(upper(regexp_replace(c2.name, '\\s+|\\W+', '', 1, 0)),1,5) || '_' ||
                         substr(upper(regexp_replace(ig.name, '\\s+|\\W+', '', 1, 0)),1,28) ||
                         ig.item_group_id
                    from crf c2
                   where ig.crf_id = c2.crf_id
                 )
  where exists (select 'x'
                  from crf c1
                 where ig.crf_id = c1.crf_id
               );

--
prompt generate OIDs for items
--
update item it
   set oc_oid  =
                (select
                        'I_' ||
                        substr(upper(regexp_replace(crf1.name, '\\s+|\\W+', '', 1, 0)),1,5) || '_' ||
                        substr(upper(regexp_replace(it.name, '\\s+|\\W+', '', 1, 0)),1,29) ||
                        it.item_id
                   from item_group_metadata igm1,
                        item_group ig1,
                        crf crf1
                  where igm1.item_id = it.item_id
                    AND igm1.item_group_id = ig1.item_group_id
                    AND ig1.crf_id = crf1.crf_id
                )
  where exists  (select 'x'
                   from item_group_metadata igm2,
                        item_group ig2,
                        crf crf2
                  where igm2.item_id = it.item_id
                    AND igm2.item_group_id = ig2.item_group_id
                    AND ig2.crf_id = crf2.crf_id
                );

--
prompt generate OIDs for study
--
update study
   set oc_oid = substr(upper(regexp_replace(unique_identifier, '\\s+|\\W+', '', 1, 0)),1,9) || study_id;

--
prompt generate OIDs for study_subject
--
update study_subject
   set oc_oid = substr(upper(regexp_replace(label, '\\s+|\\W+', '', 1, 0)),1,9) || study_subject_id;

--
update audit_log_event_type set name = 'Event CRF complete with password' where name = 'Event CRF signed complete';
update audit_log_event_type set name = 'Event CRF Initial Data Entry complete with password' where name = 'Event CRF IDE signed (DDE)';
update audit_log_event_type set name = 'Event CRF Double Data Entry complete with password' where name = 'Event CRF validated and signed (DDE)';
update audit_log_event_type set name = 'Event CRF Initial Data Entry complete' where name = 'Event CRF IDE completed (DDE)';
update audit_log_event_type set name = 'Event CRF Double Data Entry complete' where name = 'Event CRF validated(DDE)';

--
commit;
--
