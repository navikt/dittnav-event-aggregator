ALTER TABLE DONE DROP CONSTRAINT IF EXISTS doneEventIdProdusent;
ALTER TABLE DONE ADD CONSTRAINT doneEventIdProdusent UNIQUE (eventid, systembruker);
