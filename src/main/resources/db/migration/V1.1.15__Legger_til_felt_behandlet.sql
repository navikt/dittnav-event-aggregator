ALTER TABLE beskjed ADD COLUMN forstBehandlet timestamp without time zone;
ALTER TABLE oppgave ADD COLUMN forstBehandlet timestamp without time zone;
ALTER TABLE innboks ADD COLUMN forstBehandlet timestamp without time zone;
ALTER TABLE statusoppdatering ADD COLUMN forstBehandlet timestamp without time zone;
ALTER TABLE done ADD COLUMN forstBehandlet timestamp without time zone;
