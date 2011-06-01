-- meant to remove duplicates, possibly triplcates or quadruplicates, and reassign their discrepancy notes.
-- however, need to reconcile different values in database.
-- also pass in event_crf_id?
CREATE OR REPLACE
  FUNCTION DELTE(
      remove  IN INTEGER,
      promote IN INTEGER)
    RETURN INTEGER
  IS
  
  BEGIN
    
    UPDATE dn_item_data_map
    SET item_data_id   = promote
    WHERE item_data_id = remove;
    UPDATE audit_log_event
    SET entity_id   = promote
    WHERE entity_id = remove
    AND audit_table = 'item_data';
    --DELETE FROM item_data WHERE item_data_id = remove AND ordinal = 1 returning rowid into rr;
    --commit;
    --dbms_output.put_line('deleted '|| remove || ' rowid ' || rr);
    --update item_data set status_id = 7, date_updated = now(), update_id = 1
    -- where item_data_id = item_data_record.min_item_data_id;
    RETURN 1;
  END DELTE;
  /
CREATE OR REPLACE
FUNCTION repair_item_data_1
  RETURN INTEGER
IS
  --declare
  CURSOR item_data_record_max
  IS
    SELECT MAX(item_data_id)
    FROM item_data
    WHERE ordinal = 1
    GROUP BY item_id,
      event_crf_id
    HAVING COUNT(item_id)   > 1
    AND COUNT(event_crf_id) > 1;
  CURSOR item_data_record_min
  IS
    SELECT MIN(item_data_id)
    FROM item_data
    WHERE ordinal = 1
    GROUP BY item_id,
      event_crf_id
    HAVING COUNT(item_id)   > 1
    AND COUNT(event_crf_id) > 1;
  min_dn_count   INTEGER;
  max_dn_count   INTEGER;
  min_item_value VARCHAR2(4000);
  max_item_value VARCHAR2(4000);
  max_overall    INTEGER;
  min_item_date_updated DATE;
  max_item_date_updated DATE;
  min_item_date_created DATE;
  max_item_date_created DATE;
  min_item_timestamp TIMESTAMP;
  max_item_timestamp TIMESTAMP;
  min_last_touched TIMESTAMP;
  max_last_touched TIMESTAMP;
  min_item_updater_id INTEGER;
  max_item_updater_id INTEGER;
  event_ssid          VARCHAR2(4000);
  crf_name            VARCHAR2(4000);
  ret_count           INTEGER;
  crf_version_name    VARCHAR2(4000);
  sed_name            VARCHAR2(4000);
  max_item_data_id    INTEGER;
  min_item_data_id    INTEGER;
  vbuffer varchar2(4000);
  status integer;
  rr urowid;
BEGIN
  ret_count := 0;
  SELECT MAX(cnt)
  INTO max_overall
  FROM
    (SELECT COUNT(item_id) AS cnt
    FROM item_data
    WHERE ordinal           = 1
    HAVING COUNT(item_id)   > 1
    AND COUNT(event_crf_id) > 1
    );
    DBMS_OUTPUT.ENABLE(1000000);
  -- usually, the above will be 2.  however, it may be higher
  FOR i IN 2..max_overall
  LOOP
    OPEN item_data_record_max;
    OPEN item_data_record_min;
    LOOP
      FETCH item_data_record_max INTO max_item_data_id;
      FETCH item_data_record_min INTO min_item_data_id;
      EXIT
    WHEN item_data_record_max%notfound OR item_data_record_max%notfound IS NULL;
      SELECT id.value
      INTO min_item_value
      FROM item_data id
      WHERE id.item_data_id = min_item_data_id;

create or replace function repair_item_data_1() returns integer as $$
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
		where ordinal = 1 --and status_id != 5 and status_id != 7
		group by item_id, event_crf_id 
		having count(item_id) > 1 and count(event_crf_id) > 1) AS overall;
	-- usually, the above will be 2.  however, it may be higher
	for i in 2..max_overall loop
		for item_data_record in
			(select max(item_data_id) as max_item_data_id,
			min(item_data_id) as min_item_data_id, item_id, event_crf_id 
			from item_data where 
			ordinal = 1 
			--and
			--status_id != 5 and 
			--status_id != 7
			group by item_id, event_crf_id 
			having count(item_id) > 1 and count(event_crf_id) > 1)
		loop
			-- select into min_dn_count count(*) from Dn_Item_Data_Map as z1 where z1.item_data_id = item_data_record.min_item_data_id;
			-- select into max_dn_count count(*) from Dn_Item_Data_Map as z1 where z1.item_data_id = item_data_record.max_item_data_id;
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
				ret_count = ret_count + delete(item_data_record.min_item_data_id, item_data_record.max_item_data_id);
				
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
					ret_count = ret_count + delete(item_data_record.min_item_data_id, item_data_record.max_item_data_id);
				elsif max_last_touched > min_last_touched then
					--raise notice 'reviewed TIMESTAMPS: max % VS  min %', max_item_timestamp, min_item_timestamp;
					ret_count = ret_count + delete(item_data_record.max_item_data_id, item_data_record.min_item_data_id);
				-- final rows that dont make the cut - compare on PK
				else 
					ret_count = ret_count + delete(item_data_record.max_item_data_id, item_data_record.min_item_data_id);
					raise notice 'removed on PK %', item_data_record.max_item_data_id;
				end if;
				
			end if;
			
		end loop;
	raise notice 'i is %', i;
	raise notice 'max_overall is %', max_overall;
	end loop;

    	SELECT crfv.name
      INTO crf_version_name
      FROM crf_version crfv,
        event_crf ec
      WHERE ec.crf_version_id = crfv.crf_version_id
      AND ec.event_crf_id     =
        (SELECT event_crf_id FROM item_data WHERE item_data_id = max_item_data_id
        );
      SELECT crf.name
      INTO crf_name
      FROM crf,
        crf_version crfv,
        event_crf ec
      WHERE crf.crf_id      = crfv.crf_id
      AND ec.crf_version_id = crfv.crf_version_id
      AND ec.event_crf_id   =
        (SELECT event_crf_id FROM item_data WHERE item_data_id = max_item_data_id
        );
      SELECT sed.name
      INTO sed_name
      FROM study_event_definition sed,
        study_event se,
        event_crf ec
      WHERE sed.study_event_definition_id = se.study_event_definition_id
      AND se.study_event_id               = ec.study_event_id
      AND ec.event_crf_id                 =
        (SELECT event_crf_id FROM item_data WHERE item_data_id = max_item_data_id
        );
      dbms_output.put_line('looking at records for SSID '|| event_ssid || ' CRF '|| crf_name ||' Version '|| crf_version_name ||' Study Event ' || sed_name);
      --dbms_output.put_line('comparing item_data_id '|| min_item_data_id ||' with a value of '|| min_item_value ||' and item_data_id '|| max_item_data_id ||' with a value of ' || max_item_value);
      -- end of extra here, continue with logic of removals
      -- if our values are identical, we can remove the initial row created and move DNs over to the most recent row.
      IF (min_item_value     = max_item_value) or (min_item_value is null and max_item_value is null) THEN
        ret_count          := ret_count + DELTE(min_item_data_id, max_item_data_id);
        DELETE FROM item_data WHERE item_data_id = min_item_data_id AND ordinal = 1 returning rowid into rr;
        dbms_output.put_line('deleted '|| min_item_data_id || ' rowid ' || rr);
      elsif min_item_value <> max_item_value THEN
        SELECT id.date_updated
        INTO min_item_date_updated
        FROM item_data id
        WHERE id.item_data_id = min_item_data_id;
        SELECT id.date_updated
        INTO max_item_date_updated
        FROM item_data id
        WHERE id.item_data_id = max_item_data_id;
        SELECT id.date_created
        INTO min_item_date_created
        FROM item_data id
        WHERE id.item_data_id = min_item_data_id;
        SELECT id.date_created
        INTO max_item_date_created
        FROM item_data id
        WHERE id.item_data_id = max_item_data_id;
        -- select into min_item_updater_id id.update_id from item_data id where id.item_data_id = item_data_record.min_item_data_id;
        -- select into max_item_updater_id id.update_id from item_data id where id.item_data_id = item_data_record.max_item_data_id;
        SELECT MAX(ad.audit_date)
        INTO min_item_timestamp
        FROM audit_log_event ad
        WHERE ad.entity_id = min_item_data_id
        AND ad.audit_table = 'item_data'
        GROUP BY ad.entity_id;
        SELECT MAX(ad.audit_date)
        INTO max_item_timestamp
        FROM audit_log_event ad
        WHERE ad.entity_id = max_item_data_id
        AND ad.audit_table = 'item_data'
        GROUP BY ad.entity_id;
        --raise notice 'reviewed TIMESTAMPS: min % VS max %', min_item_timestamp, max_item_timestamp;
        -- determine which date is last touched for our min item
        IF min_item_date_updated IS NOT NULL THEN
          min_last_touched       := min_item_timestamp;
        ELSE
          min_last_touched := min_item_date_created;
        END IF;
        -- determine which date is last touched for our max item
        IF max_item_date_updated IS NOT NULL THEN
          max_last_touched       := max_item_timestamp;
        ELSE
          max_last_touched := max_item_date_created;
        END IF;
        -- the main logic; determine which one wins it
        IF min_last_touched > max_last_touched THEN
          --raise notice 'reviewed TIMESTAMPS: min % VS max %', min_item_timestamp, max_item_timestamp;
          ret_count           := ret_count + DELTE(min_item_data_id, max_item_data_id);
          DELETE FROM item_data WHERE item_data_id = min_item_data_id AND ordinal = 1 returning rowid into rr;
          dbms_output.put_line('deleted '|| min_item_data_id || ' rowid ' || rr);
        elsif max_last_touched > min_last_touched THEN
          --raise notice 'reviewed TIMESTAMPS: max % VS  min %', max_item_timestamp, min_item_timestamp;
          ret_count := ret_count + DELTE(max_item_data_id, min_item_data_id);
          DELETE FROM item_data WHERE item_data_id = max_item_data_id AND ordinal = 1 returning rowid into rr;
          dbms_output.put_line('deleted '|| max_item_data_id || ' rowid ' || rr);
          -- final rows that dont make the cut - compare on PK
        ELSE
          -- here we look at blanks vs nonblanks, and then finally, make a decision based on PK
          IF min_item_value = '' AND max_item_value <> '' THEN
            ret_count      := ret_count + DELTE(min_item_data_id, max_item_data_id);
            DELETE FROM item_data WHERE item_data_id = min_item_data_id AND ordinal = 1 returning rowid into rr;
            dbms_output.put_line('deleted '|| min_item_data_id || ' rowid ' || rr);
            --raise notice 'removed on Blank Value %', item_data_record.min_item_data_id;
          elsif max_item_value = '' AND min_item_value <> '' THEN
            ret_count         := ret_count + DELTE(max_item_data_id, min_item_data_id);
            DELETE FROM item_data WHERE item_data_id = max_item_data_id AND ordinal = 1 returning rowid into rr;
            dbms_output.put_line('deleted '|| max_item_data_id || ' rowid ' || rr);
            
            --raise notice 'removed on Blank Value %', item_data_record.max_item_data_id;
          ELSE
            -- both items are nonblank
            ret_count := ret_count + DELTE(max_item_data_id, min_item_data_id);
            DELETE FROM item_data WHERE item_data_id = max_item_data_id AND ordinal = 1 returning rowid into rr;
            dbms_output.put_line('deleted '|| max_item_data_id || ' rowid ' || rr);
            --raise notice 'removed on PK %', item_data_record.max_item_data_id;
          END IF;
        END IF;
        
      END IF;
    END LOOP;
    CLOSE item_data_record_max;
    CLOSE item_data_record_min;
    --'i is %', i;
    --raise notice 'max_overall is %', max_overall;
    --dbms_output.get_lines;
    --commit;
    DBMS_OUTPUT.GET_LINE(vbuffer, status); 
  END LOOP;
  RETURN ret_count;
END repair_item_data_1;
/
set serveroutput on;

declare
  nnn number;
begin
  nnn := repair_item_data_1();
  dbms_output.put_line (nnn);
end;
/

--grant all on item_data to thickerson; 
--DBMS_OUTPUT.ENABLE (buffer_size => 100000000);

--SELECT repair_item_data_1() FROM dual;

commit;