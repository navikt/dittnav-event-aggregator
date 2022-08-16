CREATE TABLE beskjed_arkiv (
    eventid text,
    fodselsnummer text,
    tekst text,
    link text,
    sikkerhetsnivaa smallint,
    aktiv boolean,
    produsentApp text,
    eksternVarslingSendt boolean,
    eksternVarslingKanaler text,
    forstbehandlet timestamp without time zone,
    arkivert timestamp without time zone
);

CREATE TABLE oppgave_arkiv (
    eventid text,
    fodselsnummer text,
    tekst text,
    link text,
    sikkerhetsnivaa smallint,
    aktiv boolean,
    produsentApp text,
    eksternVarslingSendt boolean,
    eksternVarslingKanaler text,
    forstbehandlet timestamp without time zone,
    arkivert timestamp without time zone
);

CREATE TABLE innboks_arkiv (
    eventid text,
    fodselsnummer text,
    tekst text,
    link text,
    sikkerhetsnivaa smallint,
    aktiv boolean,
    produsentApp text,
    eksternVarslingSendt boolean,
    eksternVarslingKanaler text,
    forstbehandlet timestamp without time zone,
    arkivert timestamp without time zone
);

CREATE INDEX IF NOT EXISTS beskjed_arkiv_fnr ON beskjed_arkiv(fodselsnummer);
CREATE INDEX IF NOT EXISTS oppgave_arkiv_fnr ON oppgave_arkiv(fodselsnummer);
CREATE INDEX IF NOT EXISTS innboks_arkiv_fnr ON innboks_arkiv(fodselsnummer);

CREATE INDEX IF NOT EXISTS beskjed_arkiv_eventid ON beskjed_arkiv(eventid);
CREATE INDEX IF NOT EXISTS oppgave_arkiv_eventid ON oppgave_arkiv(eventid);
CREATE INDEX IF NOT EXISTS innboks_arkiv_eventid ON innboks_arkiv(eventid);
