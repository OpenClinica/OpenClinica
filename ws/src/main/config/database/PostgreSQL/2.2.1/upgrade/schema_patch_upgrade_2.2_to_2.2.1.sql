--
--Updates the schema for datasets in OpenClinica by 
--changing the table name the data is stored in, and
--updates the sql_statement column in the dataset 
--table to correspond to the new table name.
--Author: pgalvin
--

update dataset set sql_statement=replace(sql_statement,'test_table_three','extract_data_table');
alter table test_table_three rename to extract_data_table;



-- adding the relationship here, may need to get moved, tbh 1-20-2008
-- what needs to happen:

-- Create a new row in item_group, item_group_metadata
-- associate item_group with item_group_metadata
-- associate item_group with all crfs
-- associate item_group_metadata with all items
-- associate item_group_metadata with all crf versions

-- remove constraints and re-add them at the end

--insert 

--insert


CREATE FUNCTION crf_item_data_groups_refresh() RETURNS integer AS $$
DECLARE
    mviews RECORD;
    crfvs RECORD;
    itemgroups RECORD;
    items RECORD;
    ordinal INTEGER;
BEGIN
    --PERFORM cs_log('Updating item groups with CRFs...');

    FOR mviews IN SELECT distinct c.* FROM crf c where c.crf_id not in (select crf_id from item_group) ORDER BY c.crf_id LOOP

        -- Now "mviews" has one record
	insert into item_group (name, crf_id, status_id, date_created, owner_id)
		values
		('Ungrouped', mviews.crf_id, 1, '2008-01-01', 1);
    END LOOP;

    --PERFORM cs_log('Done updating item groups with CRFs.');

    --PERFORM cs_log('Updating item group metadatas with CRF versions...');

    FOR itemgroups IN SELECT g.* FROM item_group g where g.item_group_id NOT IN (select distinct item_group_id from item_group_metadata) ORDER BY g.item_group_id LOOP

		FOR crfvs IN SELECT * FROM crf_version where crf_version.crf_id IN (select crf_id from item_group where item_group_id=itemgroups.item_group_id) ORDER BY crf_version_id LOOP

        
	
	--, item_group_metadata m where g.item_group_id = m.item_group_id and m.crf_version_id = crfvs.crf_version_id ORDER BY g.item_group_id LOOP

		ordinal := 1;
		FOR items in SELECT i.* from item i, item_form_metadata f where f.item_id = i.item_id and f.crf_version_id = crfvs.crf_version_id order by i.item_id LOOP

			--insert into item_group_metadata (header, subheader, repeat_number, repeat_max, repeat_array, row_start_number, 
			--item_group_id, crf_version_id, item_id, ordinal, layout)
			--values
			--('','',1,40,'',1,itemgroups.item_group_id,crfvs.crf_version_id,items.item_id,1,'Matrix');
			--trying a new version of this, tbh
			EXECUTE 'INSERT INTO ITEM_GROUP_METADATA (item_group_id,header,subheader,
				layout, repeat_number, repeat_max,repeat_array,row_start_number, crf_version_id,
				item_id , ordinal) VALUES (
				'|| itemgroups.item_group_id ||','''', '''', ''Matrix'', 1, 1, '''', 1,' || crfvs.crf_version_id ||
				', '|| items.item_id ||','|| ordinal ||');';
			ordinal := ordinal +1;
		END LOOP;

	END LOOP;

    END LOOP;

    --PERFORM cs_log('Done updating item group metadatas.');

    RETURN 1;
END;
$$ LANGUAGE plpgsql;

select crf_item_data_groups_refresh();

-- drop function?
drop function crf_item_data_groups_refresh();
-- end of changes, tbh

