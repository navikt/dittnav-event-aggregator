ALTER TABLE beskjed ADD CONSTRAINT beskjed_unique_eventid UNIQUE (eventid);
ALTER TABLE oppgave ADD CONSTRAINT oppgave_unique_eventid UNIQUE (eventid);
ALTER TABLE innboks ADD CONSTRAINT innboks_unique_eventid UNIQUE (eventid);
ALTER TABLE statusoppdatering ADD CONSTRAINT statusoppdatering_unique_eventid UNIQUE (eventid);
ALTER TABLE done ADD CONSTRAINT done_unique_eventid UNIQUE (eventid);
