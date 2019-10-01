create VIEW brukernotifikasjon_view AS SELECT id, produsent, 'informasjon' as type FROM INFORMASJON UNION SELECT id, produsent, 'oppgave' as type FROM OPPGAVE;
