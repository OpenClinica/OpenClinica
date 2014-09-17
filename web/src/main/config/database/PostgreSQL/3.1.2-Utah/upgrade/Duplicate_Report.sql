create or replace function test_item_data() returns integer as $$
declare
	item_data_record record;
	min_dn_count integer;
	max_dn_count integer;
	min_item_value text;
	max_item_value text;
	max_overall integer;
	min_item_date_updated date;
	max_item_date_updated date;
	min_item_date_created date;
	max_item_date_created date;
	min_item_timestamp timestamp;
	max_item_timestamp timestamp;
	min_last_touched timestamp;
    max_last_touched timestamp;
	min_item_updater_id integer;
	max_item_updater_id integer;
	event_ssid text;
	crf_name text;
	ret_count integer;
	crf_version_name text;
	sed_name text;
begin
	select into ret_count 0;
	select into max_overall max(overall.cnt) from (select count(item_id) as cnt from item_data 
		where ordinal = 1 
		group by item_id, event_crf_id 
		having count(item_id) > 1 and count(event_crf_id) > 1) AS overall;
	-- usually, the above will be 2.  however, it may be higher
	for i in 2..max_overall loop
		for item_data_record in
			(select max(item_data_id) as max_item_data_id,
			min(item_data_id) as min_item_data_id, item_id, event_crf_id 
			from item_data where 
			ordinal = 1 
			group by item_id, event_crf_id 
			having count(item_id) > 1 and count(event_crf_id) > 1)
		loop
			-- should not need the below two lines except for logging purposes:
			select into min_item_value id.value from item_data id where id.item_data_id = item_data_record.min_item_data_id;
			select into max_item_value id.value from item_data id where id.item_data_id = item_data_record.max_item_data_id;
			-- add more logging here: report the max and min, values and then event crf id and metadata with that
			select into event_ssid ss.label from study_subject ss, event_crf ec where ec.study_subject_id = ss.study_subject_id and ec.event_crf_id = item_data_record.event_crf_id;
			select into crf_version_name crfv.name from crf_version crfv, event_crf ec where ec.crf_version_id = crfv.crf_version_id and ec.event_crf_id = item_data_record.event_crf_id;
			select into crf_name crf.name from crf, crf_version crfv, event_crf ec where crf.crf_id = crfv.crf_id and ec.crf_version_id = crfv.crf_version_id and ec.event_crf_id = item_data_record.event_crf_id;
			select into sed_name sed.name from study_event_definition sed, study_event se, event_crf ec where sed.study_event_definition_id = se.study_event_definition_id and se.study_event_id = ec.study_event_id and ec.event_crf_id = item_data_record.event_crf_id;
			raise notice 'looking at records for SSID % CRF % Version % Study Event %', event_ssid, crf_name, crf_version_name, sed_name;
			raise notice 'comparing item_data_id % with a value of % and item_data_id % with a value of %', item_data_record.min_item_data_id, min_item_value, item_data_record.max_item_data_id, max_item_value;
			-- end of extra here, continue with logic of removals
			-- if our values are identical, we can remove the initial row created and move DNs over to the most recent row.
			if min_item_value = max_item_value then
			-- could we remove the additional if check?
			-- additional check to make sure we remove least recent date_updated per: audit logs
			-- CHANGE to check most recently 'touched', ie, date_update OR date_create
			-- IF date_update does not exist and date_create are the same, compare on PK
				raise notice 'test remove: values are equal, remove the older %', item_data_record.min_item_data_id;
				--ret_count = ret_count + delete(item_data_record.min_item_data_id, item_data_record.max_item_data_id);
				
			elsif min_item_value <> max_item_value then
				select into min_item_date_updated id.date_updated from item_data id where id.item_data_id = item_data_record.min_item_data_id;
				select into max_item_date_updated id.date_updated from item_data id where id.item_data_id = item_data_record.max_item_data_id;
				select into min_item_date_created id.date_created from item_data id where id.item_data_id = item_data_record.min_item_data_id;
				select into max_item_date_created id.date_created from item_data id where id.item_data_id = item_data_record.max_item_data_id;
				-- select into min_item_updater_id id.update_id from item_data id where id.item_data_id = item_data_record.min_item_data_id;
				-- select into max_item_updater_id id.update_id from item_data id where id.item_data_id = item_data_record.max_item_data_id;
				select into min_item_timestamp max(ad.audit_date) from audit_log_event ad 
					where ad.entity_id = item_data_record.min_item_data_id and ad.audit_table = 'item_data' group by ad.entity_id;
				select into max_item_timestamp max(ad.audit_date) from audit_log_event ad 
					where ad.entity_id = item_data_record.max_item_data_id and ad.audit_table = 'item_data' group by ad.entity_id;
				raise notice 'reviewed TIMESTAMPS: min % VS max %', min_item_timestamp, max_item_timestamp;
				-- determine which date is last touched for our min item
				if min_item_date_updated is not null then
					min_last_touched = min_item_timestamp;
				else 
					min_last_touched = min_item_date_created;
				end if;
				-- determine which date is last touched for our max item
				if max_item_date_updated is not null then
					max_last_touched = max_item_timestamp;
				else 
					max_last_touched = max_item_date_created;
				end if;

				-- the main logic; determine which one wins it
				
				if min_last_touched > max_last_touched then
					--raise notice 'reviewed TIMESTAMPS: min % VS max %', min_item_timestamp, max_item_timestamp; 
					--ret_count = ret_count + delete(item_data_record.min_item_data_id, item_data_record.max_item_data_id);
					raise notice 'removing older % based on last touched %', item_data_record.min_item_data_id, min_last_touched; 
				elsif max_last_touched > min_last_touched then
					--raise notice 'reviewed TIMESTAMPS: max % VS  min %', max_item_timestamp, min_item_timestamp;
					--ret_count = ret_count + delete(item_data_record.max_item_data_id, item_data_record.min_item_data_id);
					raise notice 'removing newer % based on last touched %', item_data_record.max_item_data_id, max_last_touched;
				-- final rows that dont make the cut - compare on PK
				else 
					--ret_count = ret_count + delete(item_data_record.max_item_data_id, item_data_record.min_item_data_id);
					raise notice 'removed on PK %', item_data_record.max_item_data_id;
				end if;
				
			end if;
			
		end loop;
	raise notice 'i is %', i;
	raise notice 'max_overall is %', max_overall;
	end loop;

	return ret_count;
end;
$$
LANGUAGE plpgsql;

select test_item_data();