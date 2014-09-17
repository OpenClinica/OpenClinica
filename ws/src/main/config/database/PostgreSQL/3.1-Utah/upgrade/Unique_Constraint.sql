ALTER TABLE item_data ADD CONSTRAINT duplicate_item_uniqueness_key UNIQUE (item_id, event_crf_id, ordinal);
-- should also work for oracle syntax