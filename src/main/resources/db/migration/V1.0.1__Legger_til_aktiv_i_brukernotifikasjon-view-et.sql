CREATE OR REPLACE VIEW brukernotifikasjon_view AS
SELECT eventId, produsent, 'beskjed' as type, fodselsnummer, aktiv FROM BESKJED
UNION
SELECT eventId, produsent, 'oppgave' as type, fodselsnummer, aktiv FROM OPPGAVE
UNION
SELECT eventId, produsent, 'innboks' as type, fodselsnummer, aktiv FROM INNBOKS;
