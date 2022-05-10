CREATE INDEX IF NOT EXISTS beskjed_index_eventtidspunkt
    ON beskjed (eventtidspunkt);

CREATE INDEX IF NOT EXISTS innboks_index_eventtidspunkt
    ON innboks (eventtidspunkt);

CREATE INDEX IF NOT EXISTS oppgave_index_eventtidspunkt
    ON oppgave (eventtidspunkt);

CREATE INDEX IF NOT EXISTS statusoppdatering_index_eventtidspunkt
    ON statusoppdatering (eventtidspunkt);

CREATE INDEX IF NOT EXISTS done_index_eventtidspunkt
    ON done (eventtidspunkt);
