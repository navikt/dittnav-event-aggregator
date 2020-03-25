package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneBatchProcessor(
        private val beskjederInDatabase: List<Beskjed>,
        private val innboksInDatabase: List<Innboks>,
        private val oppgaveInDatabase: List<Oppgave>
) {

    private val log: Logger = LoggerFactory.getLogger(DoneBatchProcessor::class.java)

    val foundBeskjed = mutableListOf<Done>()
    val foundOppgave = mutableListOf<Done>()
    val foundInnboks = mutableListOf<Done>()
    val notFoundEvents = mutableListOf<Done>()

    fun process(batchOfEntities: List<Done>) {
        batchOfEntities.forEach { entityToLookForInTheCache ->
            groupEventsByType(entityToLookForInTheCache)
        }
    }

    private fun groupEventsByType(doneEventToLookForAMatch: Done) {
        beskjederInDatabase.forEach { beskjedInDatabase ->
            if (beskjedInDatabase.isRepresentsSameEvent(doneEventToLookForAMatch)) {
                foundBeskjed.add(doneEventToLookForAMatch)
                log.info("Skal sette Beskjed-event med eventId ${doneEventToLookForAMatch.eventId} inaktivt")
                return
            }
        }

        innboksInDatabase.forEach { innboksInDatabase ->
            if (innboksInDatabase.isRepresentsSameEvent(doneEventToLookForAMatch)) {
                foundInnboks.add(doneEventToLookForAMatch)
                log.info("Skal sette Innboks-event med eventId ${doneEventToLookForAMatch.eventId} inaktivt")
                return
            }
        }

        oppgaveInDatabase.forEach { oppgaveInDatabase ->
            if (oppgaveInDatabase.isRepresentsSameEvent(doneEventToLookForAMatch)) {
                foundOppgave.add(doneEventToLookForAMatch)
                log.info("Skal sette Oppgave-event med eventId ${doneEventToLookForAMatch.eventId} inaktivt")
                return
            }
        }

        notFoundEvents.add(doneEventToLookForAMatch)
        log.info("Fant ikke matchende event for done-event med eventId $doneEventToLookForAMatch")
    }

}
