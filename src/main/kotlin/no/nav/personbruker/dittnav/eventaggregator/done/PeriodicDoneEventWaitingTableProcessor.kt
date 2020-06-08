package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.health.HealthStatus
import no.nav.personbruker.dittnav.eventaggregator.health.Status
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class PeriodicDoneEventWaitingTableProcessor(
        private val doneRepository: DoneRepository,
        private val dbMetricsProbe: DBMetricsProbe,
        private val job: Job = Job()
) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(PeriodicDoneEventWaitingTableProcessor::class.java)
    private val minutesToWait = Duration.ofMinutes(2)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    fun status(): HealthStatus {
        return when (job.isActive) {
            true -> HealthStatus("PeriodicDoneEventWaitingTableProcessor", Status.OK, "Processor is running", false)
            false -> HealthStatus("PeriodicDoneEventWaitingTableProcessor", Status.ERROR, "Processor is not running", false)
        }
    }

    suspend fun stop() {
        log.info("Stopper periodisk prosessering av ventetabellen for done-eventer")
        job.cancelAndJoin()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    fun start() {
        log.info("Periodisk prosessering av ventetabellen har blitt aktivert, første prosessering skjer om $minutesToWait minutter.")
        launch {
            while (job.isActive) {
                delay(minutesToWait)
                processDoneEvents()
            }
        }
    }

    suspend fun processDoneEvents() {
        try {
            val allDoneEventsWithinLimit = doneRepository.fetchAllDoneEventsWithLimit()
            processEvents(allDoneEventsWithinLimit)

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
