CREATE OR REPLACE VIEW ytest_brukernotifikasjon_view AS
SELECT eventId, produsent, 'beskjed' as type, fodselsnummer, aktiv FROM YTEST_BESKJED
UNION
SELECT eventId, produsent, 'oppgave' as type, fodselsnummer, aktiv FROM YTEST_OPPGAVE
UNION
SELECT eventId, produsent, 'innboks' as type, fodselsnummer, aktiv FROM YTEST_INNBOKS;
