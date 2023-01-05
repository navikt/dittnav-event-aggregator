ALTER TABLE beskjed
    ADD COLUMN frist_utløpt BOOLEAN;
ALTER TABLE beskjed_arkiv
    ADD COLUMN frist_utløpt BOOLEAN;

ALTER TABLE innboks
    ADD COLUMN frist_utløpt BOOLEAN;
ALTER TABLE innboks_arkiv
    ADD COLUMN frist_utløpt BOOLEAN;

ALTER TABLE oppgave
    ADD COLUMN frist_utløpt BOOLEAN;
ALTER TABLE oppgave_arkiv
    ADD COLUMN frist_utløpt BOOLEAN;

