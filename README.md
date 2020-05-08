# DittNAV event aggregator

Microservice som brukes for å lese inn eventer fra DittNAV sine kafka-topics, og lagrer disse i DittNAV sin 
event-cache (database). DittNAV-api henter ut eventer per bruker fra denne event-cache-en for å serve 
DittNAV (frontend).

# Kom i gang
1. Bygge dittnav-event-aggregator:
    * bygge og kjøre enhetstester: `gradle clean test`
    * bygge og kjøre integrasjonstester: `gradle clean build`
2. Start lokal instans av Kafka og Postgres ved å kjøre `docker-compose up -d`
3. Start konsumenten ved å kjøre kommandoen `gradle runServer`

# Feilsøking
For å være sikker på at man får en ny tom database og tomme kafka-topics kan man kjøre kommandoen: `docker-compose down -v`

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot https://github.com/orgs/navikt/teams/personbruker

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-personbruker.
