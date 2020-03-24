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
        log.info("Skal prosessere ${batchOfEntities.size} done-eventer")
        batchOfEntities.forEach { entityToLookForInTheCache ->
            groupEventsByType(entityToLookForInTheCache)
        }
        logSummaryForBatch()
    }

    private fun groupEventsByType(doneEventToLookForAMatch: Done) {
        beskjederInDatabase.forEach { beskjedInDatabase ->
            if (beskjedInDatabase.isRepresentsSameEvent(doneEventToLookForAMatch)) {
                foundBeskjed.add(doneEventToLookForAMatch)
                return
            }
        }

        innboksInDatabase.forEach { innboksInDatabase ->
            if (innboksInDatabase.isRepresentsSameEvent(doneEventToLookForAMatch)) {
                foundInnboks.add(doneEventToLookForAMatch)
                return
            }
        }

        oppgaveInDatabase.forEach { oppgaveInDatabase ->
            if (oppgaveInDatabase.isRepresentsSameEvent(doneEventToLookForAMatch)) {
                foundOppgave.add(doneEventToLookForAMatch)
                return
            }
        }

        notFoundEvents.add(doneEventToLookForAMatch)
        log.warn("Fant ikke matchende event for done-event med eventId $doneEventToLookForAMatch")
    }

    private fun logSummaryForBatch() {
        log.info("Fant ${foundBeskjed.size} done-eventer med tilhørende eventer i beskjed-tabellen.")
        log.info("Fant ${foundInnboks.size} done-eventer med tilhørende eventer i innboks-tabellen.")
        log.info("Fant ${foundOppgave.size} done-eventer med tilhørende eventer i oppgave-tabellen.")
        log.info("Det er ${foundOppgave.size} done-eventer det ikke ble funnet et tilhørende event for nå.")
    }

}
