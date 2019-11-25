ALTER TABLE INFORMASJON ADD CONSTRAINT informasjonEventIdProdusent UNIQUE (eventid, produsent);
ALTER TABLE OPPGAVE ADD CONSTRAINT oppgaveEventIdProdusent UNIQUE (eventid, produsent);
ALTER TABLE INNBOKS ADD CONSTRAINT innboksEventIdProdusent UNIQUE (eventid, produsent);