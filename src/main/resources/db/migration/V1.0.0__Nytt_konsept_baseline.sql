
CREATE TABLE IF NOT EXISTS ytest_beskjed (
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

CREATE TABLE IF NOT EXISTS ytest_oppgave (
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

CREATE TABLE IF NOT EXISTS ytest_innboks (
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

CREATE TABLE IF NOT EXISTS ytest_done (
    id serial primary key,
    produsent character varying(100),
    eventtidspunkt timestamp without time zone,
    fodselsnummer character varying(50),
    eventid character varying(50),
    grupperingsid character varying(100)
);

ALTER TABLE YTEST_BESKJED DROP CONSTRAINT IF EXISTS ytest_beskjedEventIdProdusent;
ALTER TABLE YTEST_BESKJED ADD CONSTRAINT ytest_beskjedEventIdProdusent UNIQUE (eventid, produsent);
ALTER TABLE YTEST_OPPGAVE DROP CONSTRAINT IF EXISTS oppgaveEventIdProdusent;
ALTER TABLE YTEST_OPPGAVE ADD CONSTRAINT ytest_oppgaveEventIdProdusent UNIQUE (eventid, produsent);
ALTER TABLE YTEST_INNBOKS DROP CONSTRAINT IF EXISTS ytest_innboksEventIdProdusent;
ALTER TABLE YTEST_INNBOKS ADD CONSTRAINT ytest_innboksEventIdProdusent UNIQUE (eventid, produsent);

CREATE OR REPLACE VIEW ytest_brukernotifikasjon_view AS
SELECT eventId, produsent, 'beskjed' as type, fodselsnummer FROM YTEST_BESKJED
UNION
SELECT eventId, produsent, 'oppgave' as type, fodselsnummer FROM YTEST_OPPGAVE
UNION
SELECT eventId, produsent, 'innboks' as type, fodselsnummer FROM YTEST_INNBOKS;
