CREATE INDEX IF NOT EXISTS beskjed_index_synligfremtil
    ON beskjed (synligFremTil);

CREATE INDEX IF NOT EXISTS oppgave_index_synligfremtil
    ON oppgave (synligFremTil);
