---
--- Indexing for performance inhacing in datawarehouse script
---

-- create indexes on item_group_metadata
CREATE INDEX item_id_item_group_metadata_table ON item_group_metadata USING btree (item_id);
CREATE INDEX crf_version_id_item_group_metadata_table ON item_group_metadata USING btree (crf_version_id);

-- create indexes on item_data
CREATE INDEX status_id_item_data_table ON item_data USING btree (status_id);
CREATE INDEX item_id_item_data_table ON item_data USING btree (item_id);
CREATE INDEX event_crf_id_item_data_table ON item_data USING btree (event_crf_id);

-- create indexes on item_form_metadata, event_definition_crf, event_crf, item_group
CREATE INDEX response_set_id_item_form_metadata_table ON item_form_metadata USING btree (response_set_id);
CREATE INDEX crf_id_event_definition_crf_table ON event_definition_crf USING btree (crf_id);
CREATE INDEX status_id_event_crf_table ON event_crf USING btree (status_id);
CREATE INDEX item_group_id_item_group_table ON item_group USING btree (item_group_id);
