CREATE VIEW varsel_header_view AS
SELECT 'beskjed' as type, eventId, namespace, appnavn, fodselsnummer, aktiv FROM BESKJED
UNION
SELECT 'oppgave' as type, eventId, namespace, appnavn, fodselsnummer, aktiv FROM OPPGAVE
UNION
SELECT 'innboks' as type, eventId, namespace, appnavn, fodselsnummer, aktiv FROM INNBOKS;
