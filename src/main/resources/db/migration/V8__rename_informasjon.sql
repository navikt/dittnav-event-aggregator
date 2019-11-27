DROP VIEW brukernotifikasjon_view;

ALTER TABLE INFORMASJON RENAME TO BESKJED;

CREATE VIEW brukernotifikasjon_view AS
SELECT eventId, produsent, 'beskjed' as type FROM BESKJED
UNION
SELECT eventId, produsent, 'oppgave' as type FROM OPPGAVE
UNION
SELECT eventId, produsent, 'innboks' as type FROM INNBOKS;
