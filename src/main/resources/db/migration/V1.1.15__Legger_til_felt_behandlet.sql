ALTER TABLE beskjed ADD COLUMN forstBehandlet timestamp without time zone;
ALTER TABLE oppgave ADD COLUMN forstBehandlet timestamp without time zone;
ALTER TABLE innboks ADD COLUMN forstBehandlet timestamp without time zone;
ALTER TABLE statusoppdatering ADD COLUMN forstBehandlet timestamp without time zone;
ALTER TABLE done ADD COLUMN forstBehandlet timestamp without time zone;

update beskjed set forstBehandlet = case when eventtidspunkt < '1975-01-01' then to_timestamp(extract(epoch from eventtidspunkt) * 1000) at time zone 'UTC' else eventtidspunkt end;
update oppgave set forstBehandlet = case when eventtidspunkt < '1975-01-01' then to_timestamp(extract(epoch from eventtidspunkt) * 1000) at time zone 'UTC' else eventtidspunkt end;
update innboks set forstBehandlet = case when eventtidspunkt < '1975-01-01' then to_timestamp(extract(epoch from eventtidspunkt) * 1000) at time zone 'UTC' else eventtidspunkt end;
update statusoppdatering set forstBehandlet = case when eventtidspunkt < '1975-01-01' then to_timestamp(extract(epoch from eventtidspunkt) * 1000) at time zone 'UTC' else eventtidspunkt end;
update done set forstBehandlet = case when eventtidspunkt < '1975-01-01' then to_timestamp(extract(epoch from eventtidspunkt) * 1000) at time zone 'UTC' else eventtidspunkt end;
