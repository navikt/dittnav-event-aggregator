package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    fun cancel() {
        log.info("Stopper db-consumer")
        job.cancel()
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
            log.info("Skal behandle ${allDone.size} done-eventer som er plassert i ventetabellen.")

            val doneEventsGroupedByActiveEvents = processActiveEventsOnly(allDone)
            if (doneEventsGroupedByActiveEvents.isMoreEventsToProcess()) {
                processDeactivatedEventsOnly(doneEventsGroupedByActiveEvents.notFoundEvents)
            }

        } catch (rde: RetriableDatabaseException) {
            log.warn("Behandling av done-eventer fra ventetabellen feilet. Klarte ikke å skrive til databasen, prøver igjen senere. Context: ${rde.context}", rde)

        } catch (ure: UnretriableDatabaseException) {
            log.warn("Behandling av done-eventer fra ventetabellen feilet. Klarte ikke å skrive til databasen, prøver igjen senere. Context: ${ure.context}", ure)

        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av done-eventer fra ventetabellen, forsøker igjen senere", e)
        }
    }

    private suspend fun processActiveEventsOnly(allDone: List<Done>): DoneBatchProcessor {
        val groupedDoneEvents = fetchActiveEvents()
        groupedDoneEvents.process(allDone)
        updateTheDatabase(groupedDoneEvents)

        val totalNumberOfEvents = groupedDoneEvents.allFoundEvents.size
        log.info("Fikk $totalNumberOfEvents treff tilsamme for done-eventer, fjerner nå disse fra ventetabellen.")
        return groupedDoneEvents
    }

    private suspend fun fetchActiveEvents(): DoneBatchProcessor {
        val activeBrukernotifikasjoner = doneRepository.fetchActiveBrukernotifikasjonerFromView()
        return DoneBatchProcessor(activeBrukernotifikasjoner)
    }

    private suspend fun updateTheDatabase(groupedDoneEvents: DoneBatchProcessor) {
        doneRepository.writeDoneEventsForBeskjedToCache(groupedDoneEvents.foundBeskjed)
        doneRepository.writeDoneEventsForOppgaveToCache(groupedDoneEvents.foundOppgave)
        doneRepository.writeDoneEventsForInnboksToCache(groupedDoneEvents.foundInnboks)
        doneRepository.deleteDoneEventFromCache(groupedDoneEvents.allFoundEvents)
    }

    private suspend fun processDeactivatedEventsOnly(remainingEventsToLookFor: List<Done>): DoneBatchProcessor {
        val groupedDoneEvents = fetchInactiveEvents()
        groupedDoneEvents.process(remainingEventsToLookFor)

        val totalNumberOfEvents = groupedDoneEvents.allFoundEvents.size
        log.info("Fikk $totalNumberOfEvents treff tilsamme for done-eventer, fjerner nå disse fra ventetabellen.")
        return groupedDoneEvents
    }

    private suspend fun fetchInactiveEvents(): DoneBatchProcessor {
        val inactiveBrukernotifikasjoner = doneRepository.fetchInaktiveBrukernotifikasjonerFromView()
        return DoneBatchProcessor(inactiveBrukernotifikasjoner)
    }

}
