ALTER TABLE space_blocks ADD COLUMN series_id UUID;

CREATE INDEX ix_space_blocks_series ON space_blocks (series_id) WHERE series_id IS NOT NULL;
