package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class CachedDoneEventConsumer(
        private val doneRepository: DoneRepository,
        private val job: Job = Job()
) : CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(CachedDoneEventConsumer::class.java)
    private val minutesToWait = Duration.ofMinutes(5)

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
        val allDone = doneRepository.fetchAllDoneEvents()
        log.info("Skal behandle ${allDone.size} done-eventer som er plassert i ventetabellen.")

        val groupedDoneEvents = groupDoneEventsByAssociatedEventType(allDone)
        updateTheDatabase(groupedDoneEvents)
    }

    private suspend fun groupDoneEventsByAssociatedEventType(allDone: List<Done>): DoneBatchProcessor {
        val alleBrukernotifikasjoner = doneRepository.fetchBrukernotifikasjonerFromView()
        val groupedDoneEvents = DoneBatchProcessor(alleBrukernotifikasjoner)
        groupedDoneEvents.process(allDone)
        return groupedDoneEvents
    }

    private suspend fun updateTheDatabase(groupedDoneEvents: DoneBatchProcessor) {
        doneRepository.writeDoneEventsForBeskjedToCache(groupedDoneEvents.foundBeskjed)
        doneRepository.writeDoneEventsForOppgaveToCache(groupedDoneEvents.foundOppgave)
        doneRepository.writeDoneEventsForInnboksToCache(groupedDoneEvents.foundInnboks)

        val allFoundEvents = groupedDoneEvents.foundBeskjed + groupedDoneEvents.foundOppgave + groupedDoneEvents.foundInnboks
        log.info("Fikk ${allFoundEvents.size} treff tilsamme for done-eventer, fjerner nå disse fra ventetabellen.")
        doneRepository.deleteDoneEventFromCache(allFoundEvents)
    }

}
