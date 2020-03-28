CREATE INDEX IF NOT EXISTS ytest_beskjed_index_for_fields_used_in_view
    ON ytest_beskjed (eventid, produsent, fodselsnummer, aktiv);

CREATE INDEX IF NOT EXISTS ytest_innboks_index_for_fields_used_in_view
    ON ytest_innboks (eventid, produsent, fodselsnummer, aktiv);

CREATE INDEX IF NOT EXISTS ytest_oppgave_index_for_fields_used_in_view
    ON ytest_oppgave (eventid, produsent, fodselsnummer, aktiv);
