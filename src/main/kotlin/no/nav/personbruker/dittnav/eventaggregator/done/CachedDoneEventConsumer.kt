package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class CachedDoneEventConsumer(
        private val doneRepository: DoneRepository,
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

    private suspend fun updateTheDatabase(groupedDoneEvents: CachedDoneProcessingResult) {
        doneRepository.writeDoneEventsForBeskjedToCache(groupedDoneEvents.eventsMatchingBeskjed)
        doneRepository.writeDoneEventsForOppgaveToCache(groupedDoneEvents.eventsMatchingOppgave)
        doneRepository.writeDoneEventsForInnboksToCache(groupedDoneEvents.eventsMatchingInnboks)
        doneRepository.deleteDoneEventFromCache(groupedDoneEvents.eventsMatchingAny)
    }

    private suspend fun processEvents(allDone: List<Done>) {
        val groupedDoneEvents = fetchMatchingEvents(allDone)
        updateTheDatabase(groupedDoneEvents)
        log.info("Status for prosessering av done-eventer, opp mot aktive eventer:\n$groupedDoneEvents")
    }

    private suspend fun fetchMatchingEvents(allDone: List<Done>): CachedDoneProcessingResult {
        val eventIds = allDone.map { it.eventId }.distinct()
        val matchingBrukernotifikasjoner = doneRepository.fetchBrukernotifikasjonerFromViewForEventIds(eventIds)
        return DoneBatchProcessor.process(allDone, matchingBrukernotifikasjoner)
    }

}
