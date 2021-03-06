
CREATE TABLE IF NOT EXISTS beskjed (
    id serial primary key,
    produsent character varying(100),
    eventtidspunkt timestamp without time zone,
    fodselsnummer character varying(50),
    eventid character varying(50),
    grupperingsid character varying(100),
    tekst character varying(500),
    link character varying(200),
    sikkerhetsnivaa integer,
    sistoppdatert timestamp without time zone,
    aktiv boolean,
    synligfremtil timestamp without time zone,
    uid character varying(100)
);

CREATE TABLE IF NOT EXISTS oppgave (
    id serial primary key,
    produsent character varying(100),
    eventtidspunkt timestamp without time zone,
    fodselsnummer character varying(50),
    eventid character varying(50),
    grupperingsid character varying(100),
    tekst character varying(500),
    link character varying(200),
    sikkerhetsnivaa integer,
    sistoppdatert timestamp without time zone,
    aktiv boolean
);

CREATE TABLE IF NOT EXISTS innboks (
    id serial primary key,
    produsent character varying(100),
    eventtidspunkt timestamp without time zone,
    fodselsnummer character varying(50),
    eventid character varying(50),
    grupperingsid character varying(100),
    tekst character varying(500),
    link character varying(200),
    sikkerhetsnivaa integer,
    sistoppdatert timestamp without time zone,
    aktiv boolean
);

CREATE TABLE IF NOT EXISTS done (
    id serial primary key,
    produsent character varying(100),
    eventtidspunkt timestamp without time zone,
    fodselsnummer character varying(50),
    eventid character varying(50),
    grupperingsid character varying(100)
);

ALTER TABLE BESKJED DROP CONSTRAINT IF EXISTS beskjedEventIdProdusent;
ALTER TABLE BESKJED ADD CONSTRAINT beskjedEventIdProdusent UNIQUE (eventid, produsent);
ALTER TABLE OPPGAVE DROP CONSTRAINT IF EXISTS oppgaveEventIdProdusent;
ALTER TABLE OPPGAVE ADD CONSTRAINT oppgaveEventIdProdusent UNIQUE (eventid, produsent);
ALTER TABLE INNBOKS DROP CONSTRAINT IF EXISTS innboksEventIdProdusent;
ALTER TABLE INNBOKS ADD CONSTRAINT innboksEventIdProdusent UNIQUE (eventid, produsent);

CREATE OR REPLACE VIEW brukernotifikasjon_view AS
SELECT eventId, produsent, 'beskjed' as type, fodselsnummer FROM BESKJED
UNION
SELECT eventId, produsent, 'oppgave' as type, fodselsnummer FROM OPPGAVE
UNION
SELECT eventId, produsent, 'innboks' as type, fodselsnummer FROM INNBOKS;
