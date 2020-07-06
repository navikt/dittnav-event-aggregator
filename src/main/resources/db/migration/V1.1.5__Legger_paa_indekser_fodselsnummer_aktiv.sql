CREATE INDEX IF NOT EXISTS beskjed_index_fnr_aktiv
    ON beskjed (fodselsnummer, aktiv);

CREATE INDEX IF NOT EXISTS innboks_index_fnr_aktiv
    ON innboks (fodselsnummer, aktiv);

CREATE INDEX IF NOT EXISTS oppgave_index_fnr_aktiv
    ON oppgave (fodselsnummer, aktiv);
