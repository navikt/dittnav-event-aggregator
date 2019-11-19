DROP VIEW brukernotifikasjon_view;

ALTER TABLE MELDING RENAME TO INNBOKS;

CREATE VIEW brukernotifikasjon_view AS
SELECT eventId, produsent, 'informasjon' as type FROM INFORMASJON
UNION
SELECT eventId, produsent, 'oppgave' as type FROM OPPGAVE
UNION
SELECT eventId, produsent, 'innboks' as type FROM INNBOKS;
