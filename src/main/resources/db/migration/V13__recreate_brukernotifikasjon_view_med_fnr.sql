DROP VIEW brukernotifikasjon_view;

CREATE VIEW brukernotifikasjon_view AS
SELECT eventId, produsent, 'beskjed' as type, fodselsnummer FROM BESKJED
UNION
SELECT eventId, produsent, 'oppgave' as type, fodselsnummer FROM OPPGAVE
UNION
SELECT eventId, produsent, 'innboks' as type, fodselsnummer FROM INNBOKS;