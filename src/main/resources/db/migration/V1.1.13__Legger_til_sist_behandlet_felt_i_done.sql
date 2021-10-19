ALTER TABLE done ADD COLUMN sistBehandlet TIMESTAMP WITHOUT TIME ZONE;

UPDATE done SET sistBehandlet = eventtidspunkt;
