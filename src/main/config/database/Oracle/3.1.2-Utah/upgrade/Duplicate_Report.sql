
CREATE OR REPLACE
FUNCTION test_item_data
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
      IF min_item_value     = max_item_value THEN
        --ret_count          := ret_count + DELTE(min_item_data_id, max_item_data_id);
		dbms_output.put_line('removing initial row created: ' || min_item_data_id);
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
          dbms_output.put_line('reviewed TIMESTAMPS: min '|| min_item_timestamp ||' VS max ' || max_item_timestamp);
          --ret_count           := ret_count + DELTE(min_item_data_id, max_item_data_id);
        elsif max_last_touched > min_last_touched THEN
          dbms_output.put_line('reviewed TIMESTAMPS: max '|| max_item_timestamp ||' VS  min ' || min_item_timestamp);
          --ret_count := ret_count + DELTE(max_item_data_id, min_item_data_id);
          -- final rows that dont make the cut - compare on PK
        ELSE
          -- here we look at blanks vs nonblanks, and then finally, make a decision based on PK
          IF min_item_value = '' AND max_item_value <> '' THEN
            --ret_count      := ret_count + DELTE(min_item_data_id, max_item_data_id);
            dbms_output.put_line('removed on Blank Value ' || min_item_data_id);
          elsif max_item_value = '' AND min_item_value <> '' THEN
            --ret_count         := ret_count + DELTE(max_item_data_id, min_item_data_id);
            dbms_output.put_line('removed on Blank Value ' || max_item_data_id);
          ELSE
            -- both items are nonblank
            --ret_count := ret_count + DELTE(max_item_data_id, min_item_data_id);
            dbms_output.put_line('removed on PK ' || max_item_data_id);
          END IF;
        END IF;
      END IF;
    END LOOP;
    CLOSE item_data_record_max;
    CLOSE item_data_record_min;
    dbms_output.put_line('i is '|| i);
    dbms_output.put_line('max_overall is '|| max_overall);
  END LOOP;
  RETURN ret_count;
END test_item_data;
/
SELECT test_item_data() FROM dual;