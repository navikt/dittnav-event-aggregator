ALTER TABLE BESKJED ADD CONSTRAINT beskjedEventIdProdusent UNIQUE (eventid, produsent);
ALTER TABLE OPPGAVE ADD CONSTRAINT oppgaveEventIdProdusent UNIQUE (eventid, produsent);
ALTER TABLE INNBOKS ADD CONSTRAINT innboksEventIdProdusent UNIQUE (eventid, produsent);