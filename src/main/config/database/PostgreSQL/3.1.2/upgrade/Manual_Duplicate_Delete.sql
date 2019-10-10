-- Tom Hickerson
-- OpenClinica LLC
CREATE OR REPLACE FUNCTION delete(remove integer, promote integer) returns integer AS $$
        BEGIN
		
		-- Changed ordinal = 1 to oridinal > 1 in delete statement to account for additional duplicates at higher ordinals - Tope Oluwole -- OpenClinica, LLC -- 15dec11
        	update dn_item_data_map set item_data_id = promote 
				where item_data_id = remove;
		update audit_log_event set entity_id = promote 
				where entity_id = remove and audit_table = 'item_data';
		delete from item_data where item_data_id = remove and ordinal > 1;

		
		--raise notice 'deleted %', remove;
		--update item_data set status_id = 7, date_updated = now(), update_id = 1 
		--	where item_data_id = item_data_record.min_item_data_id;
		return 1;
        END;
$$ LANGUAGE plpgsql;

create or replace function repair_item_data_2() returns integer as $$
declare
	ret_count integer;
begin
	ALTER TABLE dn_item_data_map DISABLE TRIGGER ALL;
	ALTER TABLE audit_log_event DISABLE TRIGGER ALL;
	ALTER TABLE item_data DISABLE TRIGGER ALL;
	select into ret_count 0;
	-- remove all the rows here, replace them with other rows
	-- based on the spread sheet attached to the ticket
	ret_count = ret_count + delete(XXXXXX, YYYYYY);
	
	
	ALTER TABLE dn_item_data_map ENABLE TRIGGER ALL;
	ALTER TABLE audit_log_event ENABLE TRIGGER ALL;
	ALTER TABLE item_data ENABLE TRIGGER ALL;
	return ret_count;

end;
$$
LANGUAGE plpgsql;

select repair_item_data_2();
