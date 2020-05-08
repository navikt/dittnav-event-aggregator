CREATE INDEX IF NOT EXISTS beskjed_index_for_fields_used_in_view
    ON beskjed (eventid, produsent, fodselsnummer, aktiv);

CREATE INDEX IF NOT EXISTS innboks_index_for_fields_used_in_view
    ON innboks (eventid, produsent, fodselsnummer, aktiv);

CREATE INDEX IF NOT EXISTS oppgave_index_for_fields_used_in_view
    ON oppgave (eventid, produsent, fodselsnummer, aktiv);
