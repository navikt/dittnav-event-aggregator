ALTER TABLE beskjed RENAME COLUMN produsent TO systembruker;
ALTER TABLE done RENAME COLUMN produsent TO systembruker;
ALTER TABLE innboks RENAME COLUMN produsent TO systembruker;
ALTER TABLE oppgave RENAME COLUMN produsent TO systembruker;

DROP VIEW brukernotifikasjon_view;

CREATE VIEW brukernotifikasjon_view AS
SELECT eventId, systembruker, 'beskjed' as type, fodselsnummer, aktiv FROM BESKJED
UNION
SELECT eventId, systembruker, 'oppgave' as type, fodselsnummer, aktiv FROM OPPGAVE
UNION
SELECT eventId, systembruker, 'innboks' as type, fodselsnummer, aktiv FROM INNBOKS;
