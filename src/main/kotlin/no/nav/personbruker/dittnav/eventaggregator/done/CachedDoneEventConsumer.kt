package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class CachedDoneEventConsumer(
        private val doneRepository: DoneRepository,
        private val dbMetricsProbe: DBMetricsProbe,
        private val job: Job = Job()
) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(CachedDoneEventConsumer::class.java)
    private val minutesToWait = Duration.ofMinutes(2)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    suspend fun stopPolling() {
        log.info("Stopper db-consumer")
        job.cancelAndJoin()
    }

    fun poll() {
        launch {
            while (job.isActive) {
                delay(minutesToWait)
                log.info("Det er $minutesToWait minutter siden sist vi prosesserte tidligere mottatte Done-eventer fra databasen, kjører igjen.")
                processDoneEvents()
            }
        }
    }

    suspend fun processDoneEvents() {
        try {
            val allDone = doneRepository.fetchAllDoneEvents()
            processEvents(allDone)
        } catch (rde: RetriableDatabaseException) {
            log.warn("Behandling av done-eventer fra ventetabellen feilet. Klarte ikke å skrive til databasen, prøver igjen senere. Context: ${rde.context}", rde)

        } catch (ure: UnretriableDatabaseException) {
            log.warn("Behandling av done-eventer fra ventetabellen feilet. Klarte ikke å skrive til databasen, prøver igjen senere. Context: ${ure.context}", ure)

        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av done-eventer fra ventetabellen, forsøker igjen senere", e)
        }
    }


    private suspend fun processEvents(allDone: List<Done>) {
        val groupedDoneEvents = fetchRelatedEvents(allDone)
        groupedDoneEvents.process(allDone)
        dbMetricsProbe.runWithMetrics(eventType = EventType.DONE) {
            groupedDoneEvents.notFoundEvents.forEach { event ->
                countCachedEventForProducer(event.systembruker)
            }
        }
        updateTheDatabase(groupedDoneEvents)
        // Fjern logging etter metrikken er verifisert i Grafana
        log.info("Status for prosessering av done-eventer, opp mot aktive eventer:\n$groupedDoneEvents")
    }

    private suspend fun fetchRelatedEvents(allDone: List<Done>): DoneBatchProcessor {
        val eventIds = allDone.map { it.eventId }.distinct()
        val activeBrukernotifikasjoner = doneRepository.fetchBrukernotifikasjonerFromViewForEventIds(eventIds)
        return DoneBatchProcessor(activeBrukernotifikasjoner)
    }

    private suspend fun updateTheDatabase(groupedDoneEvents: DoneBatchProcessor) {
        doneRepository.writeDoneEventsForBeskjedToCache(groupedDoneEvents.foundBeskjed)
        doneRepository.writeDoneEventsForOppgaveToCache(groupedDoneEvents.foundOppgave)
        doneRepository.writeDoneEventsForInnboksToCache(groupedDoneEvents.foundInnboks)
        doneRepository.deleteDoneEventsFromCache(groupedDoneEvents.allFoundEvents)
    }
}
