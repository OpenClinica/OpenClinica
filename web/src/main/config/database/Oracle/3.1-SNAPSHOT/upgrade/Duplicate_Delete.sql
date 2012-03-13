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
  select max(cnt) into max_overall from (select count(*) as cnt from item_data where ordinal = 1 group by item_id, event_crf_id, ordinal); 
  
  DBMS_OUTPUT.ENABLE(1000000);
  dbms_output.put_line('found max overall: ' || max_overall);
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

      SELECT id.value
      INTO max_item_value
      FROM item_data id
      WHERE id.item_data_id = max_item_data_id;
      -- add more logging here: report the max and min, values and then event crf id and metadata with that

      SELECT ss.label
      INTO event_ssid
      FROM study_subject ss,
        event_crf ec
      WHERE ec.study_subject_id = ss.study_subject_id
      AND ec.event_crf_id       =
        (SELECT event_crf_id FROM item_data WHERE item_data_id = max_item_data_id
        );

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
      dbms_output.put_line('comparing item_data_id '|| min_item_data_id ||' with a value of '|| min_item_value ||' and item_data_id '|| max_item_data_id ||' with a value of ' || max_item_value);
      -- end of extra here, continue with logic of removals
      -- if our values are identical, we can remove the initial row created and move DNs over to the most recent row.
      -- but first, we need to check the null values here, becuase the following = and <> statements don't apply to nulls
      if min_item_value is null then
        ret_count          := ret_count + DELTE(min_item_data_id, max_item_data_id);
        dbms_output.put_line('removing null value created: ' || min_item_data_id);
        DELETE FROM item_data WHERE item_data_id = min_item_data_id AND ordinal = 1 returning rowid into rr;
        dbms_output.put_line('deleted '|| min_item_data_id || ' rowid ' || rr);
      end if;
      if max_item_value is null then
        ret_count          := ret_count + DELTE(max_item_data_id, min_item_data_id);
        dbms_output.put_line('removing null value created: ' || max_item_data_id);
        DELETE FROM item_data WHERE item_data_id = max_item_data_id AND ordinal = 1 returning rowid into rr;
        dbms_output.put_line('deleted '|| max_item_data_id || ' rowid ' || rr);
      end if;
      IF (min_item_value     = max_item_value) 
      THEN
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
          -- line put in to see if we get this far
          -- ret_count := ret_count + 10;
          -- here we look at blanks vs nonblanks, and then finally, make a decision based on PK
          IF min_item_value = '' AND max_item_value <> '' --OR (min_item_value is null AND max_item_value is not null) 
          THEN
            ret_count      := ret_count + DELTE(min_item_data_id, max_item_data_id);
            DELETE FROM item_data WHERE item_data_id = min_item_data_id AND ordinal = 1 returning rowid into rr;
            dbms_output.put_line('deleted '|| min_item_data_id || ' rowid ' || rr);
          
          elsif max_item_value = '' AND min_item_value <> '' -- OR (max_item_value is null AND min_item_value is not null) 
          THEN
            ret_count         := ret_count + DELTE(max_item_data_id, min_item_data_id);
            DELETE FROM item_data WHERE item_data_id = max_item_data_id AND ordinal = 1 returning rowid into rr;
            dbms_output.put_line('deleted '|| max_item_data_id || ' rowid ' || rr);
          
          ELSE
            -- both items are nonblank
            ret_count := ret_count + DELTE(max_item_data_id, min_item_data_id);
            DELETE FROM item_data WHERE item_data_id = max_item_data_id AND ordinal = 1 returning rowid into rr;
            dbms_output.put_line('deleted '|| max_item_data_id || ' rowid ' || rr);
            --raise notice 'removed on PK %', item_data_record.max_item_data_id;
          END IF;
        END IF;    
      END IF;
	  --ret_count := ret_count + 1;
    
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

ALTER TABLE item_data
DISABLE ALL TRIGGERS;
ALTER TABLE dn_item_data_map
DISABLE ALL TRIGGERS;
ALTER TABLE audit_log_event
DISABLE ALL TRIGGERS;
    
declare
  nnn number;
begin
  
  nnn := repair_item_data_1();
  dbms_output.put_line (nnn);
  
end;
/
ALTER TABLE item_data
ENABLE ALL TRIGGERS;
ALTER TABLE dn_item_data_map
ENABLE ALL TRIGGERS;
ALTER TABLE audit_log_event
ENABLE ALL TRIGGERS;

commit;