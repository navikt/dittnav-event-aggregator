# DittNAV event aggregator

Microservice som brukes for å lese inn eventer fra DittNAV sine kafka-topics, og lagrer disse i DittNAV sin 
event-cache (database). DittNAV-api henter ut eventer per bruker fra denne event-cache-en for å serve 
DittNAV (frontend).

# Kom i gang
1. Start en lokal instans av Kafka f.eks. 
`docker run -e ADV_HOST=127.0.0.1 --rm -p 3030:3030 -p 9092:9092 -p 8081:8081 -p 8083:8083 -p 8082:8082 -p 2181:2181 landoop/fast-data-dev`
2. Start konsumenten ved å kjøre filen App.
3. Produser noen eventer ved å kjøre filen AppProducer.

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot https://github.com/orgs/navikt/teams/personbruker

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-personbruker.
