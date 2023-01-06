package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class PeriodicDoneEventWaitingTableProcessor(
        private val donePersistingService: DonePersistingService,
        private val varselInaktivertProducer: VarselInaktivertProducer,
        private val dbMetricsProbe: DBMetricsProbe,
) : PeriodicJob(interval = Duration.ofSeconds(30)) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicDoneEventWaitingTableProcessor::class.java)

    override val job = initializeJob {
        processDoneEvents()
    }

    suspend fun processDoneEvents() {
        try {

            val allDoneEventsWithinLimit = donePersistingService.fetchAllDoneEventsWithLimit()
            val groupedDoneEvents = fetchRelatedEvents(allDoneEventsWithinLimit)
            groupedDoneEvents.process(allDoneEventsWithinLimit)
            dbMetricsProbe.runWithMetrics(eventType = EventType.DONE_INTERN) {
                groupedDoneEvents.notFoundEvents.forEach { event ->
                    countCachedEventForProducer(event.appnavn)
                }
            }
            updateTheDatabase(groupedDoneEvents)
            sendVarselInaktivert(groupedDoneEvents)

        } catch (rde: RetriableDatabaseException) {
            log.warn("Behandling av done-eventer fra ventetabellen feilet. Klarte ikke å skrive til databasen, prøver igjen senere. Context: ${rde.context}", rde)

        } catch (ure: UnretriableDatabaseException) {
            log.warn("Behandling av done-eventer fra ventetabellen feilet. Klarte ikke å skrive til databasen, prøver igjen senere. Context: ${ure.context}", ure)

        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av done-eventer fra ventetabellen, forsøker igjen senere", e)
        }
    }

    private suspend fun fetchRelatedEvents(allDone: List<Done>): DoneBatchProcessor {
        val eventIds = allDone.map { it.eventId }.distinct()
        val activeBrukernotifikasjoner = donePersistingService.fetchBrukernotifikasjonerFromViewForEventIds(eventIds)
        return DoneBatchProcessor(activeBrukernotifikasjoner)
    }

    private suspend fun updateTheDatabase(groupedDoneEvents: DoneBatchProcessor) {
        donePersistingService.updateVarselTables(groupedDoneEvents.foundBeskjed, VarselType.BESKJED)
        donePersistingService.updateVarselTables(groupedDoneEvents.foundOppgave, VarselType.OPPGAVE)
        donePersistingService.writeDoneEventsForInnboksToCache(groupedDoneEvents.foundInnboks)
        donePersistingService.deleteDoneEventsFromCache(groupedDoneEvents.allFoundEvents)
        donePersistingService.updateDoneSistBehandetForUnmatchedEvents(groupedDoneEvents.notFoundEvents)
    }

    private fun sendVarselInaktivert(groupedDoneEvents: DoneBatchProcessor) {
        groupedDoneEvents.allFoundEvents.forEach { done ->
            varselInaktivertProducer.cancelEksternVarsling(done.eventId)
        }
    }
}
