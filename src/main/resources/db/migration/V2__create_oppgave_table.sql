create TABLE OPPGAVE(
    id              serial primary key,
    produsent       varchar(100),
    eventTidspunkt  timestamp,
    aktorId         varchar(50),
    eventId         varchar(50),
    dokumentId      varchar(100),
    tekst           varchar(500),
    link            varchar(200),
    sikkerhetsnivaa integer,
    sistOppdatert   timestamp,
    aktiv           boolean
)