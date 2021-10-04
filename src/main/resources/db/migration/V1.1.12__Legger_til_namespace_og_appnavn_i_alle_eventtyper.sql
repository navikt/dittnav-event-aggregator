ALTER TABLE beskjed ADD COLUMN namespace character varying(100);
ALTER TABLE oppgave ADD COLUMN namespace character varying(100);
ALTER TABLE innboks ADD COLUMN namespace character varying(100);
ALTER TABLE done ADD COLUMN namespace character varying(100);
ALTER TABLE statusoppdatering ADD COLUMN namespace character varying(100);

ALTER TABLE beskjed ADD COLUMN appnavn character varying(100);
ALTER TABLE oppgave ADD COLUMN appnavn character varying(100);
ALTER TABLE innboks ADD COLUMN appnavn character varying(100);
ALTER TABLE done ADD COLUMN appnavn character varying(100);
ALTER TABLE statusoppdatering ADD COLUMN appnavn character varying(100);