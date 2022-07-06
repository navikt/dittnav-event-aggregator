ALTER TABLE innboks ADD COLUMN eksternVarsling boolean;
ALTER TABLE innboks ADD COLUMN prefererteKanaler character varying(100);

CREATE TABLE doknotifikasjon_status_innboks (
    eventId VARCHAR(50) UNIQUE,
    status TEXT,
    melding TEXT,
    distribusjonsId BIGINT,
    tidspunkt TIMESTAMP WITHOUT TIME ZONE,
    antall_oppdateringer SMALLINT,
    constraint fk_dokstatus_innboks_eventid FOREIGN KEY(eventId) REFERENCES innboks(eventId)
);
