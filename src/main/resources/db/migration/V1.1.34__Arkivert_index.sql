CREATE INDEX IF NOT EXISTS beskjed_arkiv_arkivert_idx
    ON beskjed_arkiv(arkivert);

CREATE INDEX IF NOT EXISTS oppgave_arkiv_arkivert_idx
    ON oppgave_arkiv(arkivert);

CREATE INDEX IF NOT EXISTS innboks_arkiv_arkivert_idx
    ON innboks_arkiv(arkivert);
