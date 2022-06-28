CREATE TABLE doknotifikasjon_status_beskjed (
    eventId VARCHAR(50) UNIQUE,
    status VARCHAR(25),
    melding VARCHAR(120),
    distribusjonsId BIGINT,
    tidspunkt TIMESTAMP WITHOUT TIME ZONE,
    antall_oppdateringer SMALLINT,
    constraint fk_dokstatus_beskjed_eventid FOREIGN KEY(eventId) REFERENCES beskjed(eventId)
);

CREATE TABLE doknotifikasjon_status_oppgave (
    eventId VARCHAR(50) UNIQUE,
    status VARCHAR(25),
    melding VARCHAR(120),
    distribusjonsId BIGINT,
    tidspunkt TIMESTAMP WITHOUT TIME ZONE,
    antall_oppdateringer SMALLINT,
    constraint fk_dokstatus_oppgave_eventid FOREIGN KEY(eventId) REFERENCES oppgave(eventId)
);
