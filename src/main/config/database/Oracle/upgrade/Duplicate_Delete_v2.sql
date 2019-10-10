-- Tom Hickerson -- OpenClinica, LLC - 2012
-- package to govern output to the screen, Tom Hickerson 2012
-- next package and functions taken from
-- http://asktom.oracle.com/pls/asktom/f?p=100:11:0::::P11_QUESTION_ID:146412348066
-- to get around limitations in logging messages.

create or replace package my_dbms_output
    as
        procedure put( s in varchar2 );
        procedure put_line( s in varchar2 );
        procedure new_line;
    
        function get_line( n in number ) return varchar2;
        pragma restrict_references( get_line, wnds, rnds );
    
        function get_line_count return number;
        pragma restrict_references( get_line_count, wnds, rnds, wnps );
   
       pragma restrict_references( my_dbms_output, wnds, rnds, wnps, rnps );
   end;
   /

create or replace package body my_dbms_output
    as
    
    type Array is table of varchar2(4000) index by binary_integer;
    g_data        array;
    g_cnt        number default 1;
    
        procedure put( s in varchar2 )
        is
        begin
            if ( g_data.last is not null ) then
               g_data(g_data.last) := g_data(g_data.last) || s;
           else
               g_data(1) := s;
           end if;
       end;
   
       procedure put_line( s in varchar2 )
       is
       begin
           put( s );
           g_data(g_data.last+1) := null;
       end;
   
       procedure new_line
       is
       begin
           put( null );
           g_data(g_data.last+1) := null;
       end;
   
       function get_line( n in number ) return varchar2
       is
           l_str varchar2(4000) default g_data(n);
       begin
           g_data.delete(n);
           return l_str;
       end;
   
       function get_line_count return number
       is
       begin
           return g_data.count+1;
       end;
   
   end;
   /
   
create or replace view my_dbms_output_view
    as
    select rownum lineno, my_dbms_output.get_line( rownum ) text
      from all_objects
     where rownum < ( select my_dbms_output.get_line_count from dual );

-- meant to remove duplicates, possibly triplcates or quadruplicates, and reassign their discrepancy notes.

CREATE OR REPLACE FUNCTION delte(remove IN integer, promote IN integer) 
  return integer IS
      BEGIN
		  update dn_item_data_map set item_data_id = promote 
			where item_data_id = remove;
		  update audit_log_event set entity_id = promote 
          where entity_id = remove and audit_table = 'item_data';
		      
      return 1;
      END delte;
/


CREATE OR REPLACE FUNCTION repair_item_data_1 return integer is 

  CURSOR item_data_record_max
  IS
    SELECT MAX(item_data_id)
    FROM item_data
    GROUP BY item_id,
      event_crf_id, ordinal
    HAVING COUNT(item_id)   > 1
    AND COUNT(event_crf_id) > 1
    and count(ordinal) > 1;
    
  CURSOR item_data_record_min
  IS
    SELECT MIN(item_data_id)
    FROM item_data
    GROUP BY item_id,
      event_crf_id, ordinal
    HAVING COUNT(item_id)   > 1
    AND COUNT(event_crf_id) > 1
    and count(ordinal) > 1;
	
	min_dn_count integer;
	max_dn_count integer;
	min_item_value varchar2(4000);
	max_item_value varchar2(4000);
	max_overall integer;
	min_item_date_updated date;
	max_item_date_updated date;
	min_item_date_created date;
	max_item_date_created date;
	min_item_timestamp timestamp;
	max_item_timestamp timestamp;
	min_last_touched date;--timestamp;
    max_last_touched date;--timestamp;
	min_item_updater_id integer;
	max_item_updater_id integer;
	event_ssid varchar2(4000);
	crf_name varchar2(4000);
	ret_count integer;
	crf_version_name varchar2(4000);
	sed_name varchar2(4000);
  max_item_data_id    INTEGER;
  min_item_data_id    INTEGER;
  max_audit_date_present integer;
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
	group by item_id, event_crf_id, ordinal 
    HAVING COUNT(item_id)   > 1
    AND COUNT(event_crf_id) > 1
    and count(ordinal) > 1
    );
  DBMS_OUTPUT.ENABLE(1000000);
	-- usually, the above will be 2.  however, it may be higher
	if max_overall is not null then
	OPEN item_data_record_max;
    OPEN item_data_record_min;
	for i in 2..max_overall
  LOOP
    
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
		
      my_dbms_output.put_line('looking at records for SSID '|| event_ssid || ' CRF '|| crf_name ||' Version '|| crf_version_name ||' Study Event ' || sed_name);
	  
      my_dbms_output.put_line('comparing item_data_id '|| min_item_data_id ||' with a value of '|| min_item_value ||' and item_data_id '|| max_item_data_id ||' with a value of ' || max_item_value);
      -- end of extra here, continue with logic of removals
      -- if our values are identical, we can remove the initial row created and move DNs over to the most recent row.
      IF min_item_value = max_item_value or (min_item_value is null and max_item_value is null) THEN
	  
        ret_count          := ret_count + DELTE(min_item_data_id, max_item_data_id);
        DELETE FROM item_data WHERE item_data_id = min_item_data_id returning rowid into rr;
        my_dbms_output.put_line('deleted '|| min_item_data_id || ' rowid ' || rr);
      
	  ELSE 
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
        
        -- determine which date is last touched for our min item
        IF min_item_date_updated IS NOT NULL THEN
			min_last_touched := min_item_date_updated;      --:= min_item_timestamp;
        ELSE
			min_last_touched := min_item_date_created;
        END IF;
        -- determine which date is last touched for our max item
        IF max_item_date_updated IS NOT NULL THEN
			max_last_touched := max_item_date_updated;--      := max_item_timestamp;
        ELSE
			max_last_touched := max_item_date_created;
        END IF;
        -- the main logic; determine which one wins it
		my_dbms_output.put_line('reviewed timestamps: min ' || min_last_touched || ' vs. ' || max_last_touched);
			
		my_dbms_output.put_line('min: ' || min_item_data_id || ' max: ' || max_item_data_id);
		my_dbms_output.put_line('min value: ' || min_item_value || ' max value: ' || max_item_value);
			
        IF max_last_touched > min_last_touched THEN
          
			ret_count := ret_count + DELTE(max_item_data_id, min_item_data_id);
			DELETE FROM item_data WHERE item_data_id = max_item_data_id returning rowid into rr;
			my_dbms_output.put_line('max deleted '|| max_item_data_id || ' rowid ' || rr);
			
        elsif min_last_touched > max_last_touched THEN
          
			ret_count           := ret_count + DELTE(min_item_data_id, max_item_data_id);
			DELETE FROM item_data WHERE item_data_id = min_item_data_id returning rowid into rr;
			my_dbms_output.put_line('min deleted '|| min_item_data_id || ' rowid ' || rr);          -- final rows that dont make the cut - compare on PK
        ELSE
          
			IF min_item_value = '' AND max_item_value <> '' THEN
				ret_count      := ret_count + DELTE(min_item_data_id, max_item_data_id);
				DELETE FROM item_data WHERE item_data_id = min_item_data_id returning rowid into rr;
				my_dbms_output.put_line('min deleted '|| min_item_data_id || ' rowid ' || rr);
            
			elsif max_item_value = '' AND min_item_value <> '' THEN
				ret_count         := ret_count + DELTE(max_item_data_id, min_item_data_id);
				DELETE FROM item_data WHERE item_data_id = max_item_data_id returning rowid into rr;
				my_dbms_output.put_line('max deleted '|| max_item_data_id || ' rowid ' || rr);
            
			ELSE
            -- both items are nonblank
				ret_count := ret_count + DELTE(max_item_data_id, min_item_data_id);
				DELETE FROM item_data WHERE item_data_id = max_item_data_id returning rowid into rr;
				my_dbms_output.put_line('max deleted '|| max_item_data_id || ' on nonblanks, rowid ' || rr);
            
			END IF;
        END IF;
      END IF;
    END LOOP;
  END LOOP;
  CLOSE item_data_record_max;
  CLOSE item_data_record_min;
	-- if..then..else added for a cleaner response to when there are no duplicate rows found in the first place -- Tope Oluwole - 22-Dec-2011
  else 
		max_overall := 0;
		my_dbms_output.put_line( 'Initial delete script claims no duplicate records are in the item_data table.');
  end if;
	
  RETURN ret_count;
END repair_item_data_1;
/



-- Adds contraint to item_data table
create or replace
FUNCTION apply_item_data_contraint return integer as 
BEGIN
	execute immediate 'ALTER TABLE item_data ADD CONSTRAINT duplicate_item_uniqueness_key UNIQUE (item_id, event_crf_id, ordinal)';
	return 1;
END apply_item_data_contraint;
/

-- Tope Oluwole -- OpenClinica, LLC -- 23dec2011
-- Finds rogue item_data_id to be promoted and removed
create or replace
FUNCTION check_4_dups_that_may_fail return INTEGER IS 
  item_id_tab INTEGER;
  event_crf_id_tab INTEGER;
  ordinal_tab INTEGER;
  rr urowid;
  min_value_tab VARCHAR2(4000);
  max_value_tab VARCHAR2(4000);
  vbuffer varchar2(4000);
  status integer;
  CURSOR dup_data_record_type IS 
      select item_id, event_crf_id, ordinal, min(value) as min_value, max(value) as max_value
			from item_data
			group by item_id, event_crf_id, ordinal
			having count(item_id) > 1 or count(event_crf_id) > 1 or count(ordinal) > 1;
  --TYPE item_id_tab IS TABLE OF item_data.item_id%TYPE;
  CURSOR dup_data_record_report IS
			select i.item_id, i.event_crf_id, i.ordinal, min(i.value) as min_value, max(i.value) as max_value, it.name, min(i.item_data_id) as min_item_data_id, max(i.item_data_id) as max_item_data_id
				from item_data i, item it
        where i.item_id = it.item_id
				group by i.item_id, i.event_crf_id, i.ordinal, it.name
				having count(i.item_id) > 1 or count(i.event_crf_id) > 1 or count(i.ordinal) > 1;
	dup_data_record_count integer;
	contraint_ran_flag integer;
BEGIN
	my_dbms_output.put_line('Double checking there are no more duplicate rows in the item_data table.');
	
	contraint_ran_flag := 0;
	dup_data_record_count := 0;

	open dup_data_record_type;
	--open dup_data_record_report;
	fetch dup_data_record_type into item_id_tab, event_crf_id_tab, ordinal_tab, min_value_tab, max_value_tab;

	if dup_data_record_type%notfound then 
		BEGIN
			contraint_ran_flag := apply_item_data_contraint();
			if contraint_ran_flag = 1 then
				my_dbms_output.put_line('There are no duplicate rows in the item_data table after the double check. The constraint has been applied.');
			end if;
			return 1;
		EXCEPTION 
			-- In the event a unique contraint on item_data data has already been applied. 
			-- duplicate_table condition name is based on PostgreSQL Error Code 42P07 at http://www.postgresql.org/docs/8.0/static/errcodes-appendix.html
			when DUP_VAL_ON_INDEX then
      -- ORACLE equivalent: DUP_VAL_ON_INDEX
      
				my_dbms_output.put_line('There are no duplicate rows in the item_data table. The constraint has already been applied.');
				return 1;
			when others then
				my_dbms_output.put_line('There are no duplicate rows in the item_data table. The constraint has already been applied.');
				return 1;
		END;
	else
	-- If duplicate rows are found after the double check, produce a report of duplicates.
		my_dbms_output.put_line('Duplicate records have been found after the double check. View data output for a report.');
		my_dbms_output.put_line('\t Item ID \t Event CRF ID \t Ordinal \t Min Value \t Max Value \t Item Name \t Min Item Data ID \t Max Item Data ID');
		DBMS_OUTPUT.GET_LINE(vbuffer, status); 
	for dup_data_record in dup_data_record_report 
    loop
			my_dbms_output.put_line('\t'|| dup_data_record.item_id ||'\t'|| dup_data_record.event_crf_id ||'\t'|| dup_data_record.ordinal ||'\t'|| dup_data_record.min_value ||'\t'|| dup_data_record.max_value ||'\t'|| dup_data_record.name ||'\t' || dup_data_record.min_item_data_id || '\t' || dup_data_record.max_item_data_id);
			dup_data_record_count := dup_data_record_count + 1;
			
	end loop;
    
	my_dbms_output.put_line('Duplicate rows found: ' || dup_data_record_count);
	DBMS_OUTPUT.GET_LINE(vbuffer, status); 
    return dup_data_record_count;
	end if;
	close dup_data_record_report;
	close dup_data_record_type;
END check_4_dups_that_may_fail;
/
-- Initial delete script
set serveroutput on;

-- Tope Oluwole -- OpenClinica, LLC -- 23dec2011

ALTER TABLE item_data
DISABLE ALL TRIGGERS;
ALTER TABLE dn_item_data_map
DISABLE ALL TRIGGERS;
ALTER TABLE audit_log_event
DISABLE ALL TRIGGERS;
    
declare
  nnn number;
  xxx number;
  vbuffer varchar2(4000);
  status integer;
begin
  nnn := -1;
  xxx := -1;
  -- while nnn does not equal zero, run the following
  LOOP
	nnn := repair_item_data_1();
	-- the above strips out multi-repeats
	my_dbms_output.put_line ('Removed this many rows ' || nnn || ' ' || xxx);
	commit;
	
	EXIT WHEN nnn = 0;
  END LOOP;
  
  -- while xxx does not equal zero, run the following
  -- Checks if any duplicate records are remaining, and if so, provides report
  LOOP
	xxx := check_4_dups_that_may_fail();
	commit;
	EXIT WHEN xxx = 1;
  END LOOP;
  my_dbms_output.put_line ('Finished looping for checking for duplicates: ' || nnn || ' ' || xxx);
end;
/
ALTER TABLE item_data
ENABLE ALL TRIGGERS;
ALTER TABLE dn_item_data_map
ENABLE ALL TRIGGERS;
ALTER TABLE audit_log_event
ENABLE ALL TRIGGERS;
