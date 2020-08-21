CREATE TABLE IF NOT EXISTS statusoppdatering (
    id serial primary key,
    systembruker character varying(100),
    eventtidspunkt timestamp without time zone,
    fodselsnummer character varying(50),
    eventid character varying(50),
    grupperingsid character varying(100),
    link character varying(200),
    sikkerhetsnivaa integer,
    sistoppdatert timestamp without time zone,
    statusglobal character varying(100),
    statusintern character varying(100),
    sakstema character varying(100)
);

ALTER TABLE statusoppdatering DROP CONSTRAINT IF EXISTS statusOppdateringsEventErUnikMedIdOgProdusent;
ALTER TABLE statusoppdatering ADD CONSTRAINT statusOppdateringsEventErUnikMedIdOgProdusent UNIQUE (eventid, systembruker);

CREATE INDEX IF NOT EXISTS statusoppdatering_index_eventid_srvbruker_fnr
    ON statusoppdatering (eventid, systembruker, fodselsnummer);