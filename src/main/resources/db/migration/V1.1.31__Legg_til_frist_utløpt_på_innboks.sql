ALTER TABLE innboks
    ADD COLUMN frist_utløpt BOOLEAN DEFAULT NULL;
ALTER TABLE innboks_arkiv
    ADD COLUMN frist_utløpt BOOLEAN DEFAULT NULL;

