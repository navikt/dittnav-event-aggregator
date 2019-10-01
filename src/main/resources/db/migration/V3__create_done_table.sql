create TABLE DONE(
    id              serial primary key,
    produsent       varchar(100),
    eventTidspunkt  timestamp,
    aktorId         varchar(50),
    eventId         varchar(50),
    dokumentId      varchar(100)
)
