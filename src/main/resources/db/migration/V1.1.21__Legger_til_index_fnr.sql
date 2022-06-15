CREATE INDEX IF NOT EXISTS beskjed_fnr ON beskjed(fodselsnummer);
CREATE INDEX IF NOT EXISTS oppgave_fnr ON oppgave(fodselsnummer);
CREATE INDEX IF NOT EXISTS innboks_fnr ON innboks(fodselsnummer);
CREATE INDEX IF NOT EXISTS statusoppdatering_fnr ON statusoppdatering(fodselsnummer);
