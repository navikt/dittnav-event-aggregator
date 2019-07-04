# DittNAV event aggregator

Microservice som brukes for å lese inn eventer fra DittNAV sine kafka-topics, og lagrer disse i DittNAV sin 
event-cache (database). DittNAV-api henter ut eventer per bruker fra denne event-cache-en for å serve 
DittNAV (frontend).

# Kom i gang
1. Bygg dittnav-event-aggregator ved å kjøre `gradle clean build`
2. Start lokal instans av Kafka og Postgres ved å kjøre `docker-compose up -d`
3. Start konsumenten ved å kjøre filen App.
4. Produser et informasjonsevent ved å gjøre en `post` mot endepunktet `http://localhost:8080/produce/informasjon`, 
f.eks. ´curl -X POST -v http://localhost:8080/produce/informasjon´

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot https://github.com/orgs/navikt/teams/personbruker

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-personbruker.
