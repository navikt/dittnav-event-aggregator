create VIEW brukernotifikasjon_view AS SELECT eventId, produsent, 'informasjon' as type FROM INFORMASJON UNION SELECT eventId, produsent, 'oppgave' as type FROM OPPGAVE;
