package no.nav.personbruker.dittnav.eventaggregator.database.consumer

import kotlinx.coroutines.*
import no.nav.personbruker.dittnav.eventaggregator.database.Database
import no.nav.personbruker.dittnav.eventaggregator.database.entity.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.coroutines.CoroutineContext

class CachedDoneEventConsumer(
        val job: Job = Job(),
        val database: Database
): CoroutineScope {

    private val log: Logger = LoggerFactory.getLogger(CachedDoneEventConsumer::class.java)
    private var lastRun: LocalDateTime = LocalDateTime.now()
    private val minutesToWait = 5

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun cancel() {
        log.info("Stopper db-consumer")
        job.cancel()
    }

    fun poll() {
        launch {
            while (job.isActive) {
                if(ChronoUnit.MINUTES.between(lastRun, LocalDateTime.now()) > minutesToWait) {
                    log.info("Mer enn $minutesToWait minutter siden sist vi prosesserte tidligere mottatte Done-eventer fra databasen, kjører igjen.")
                    processDoneEvents()
                    lastRun = LocalDateTime.now()
                }
            }
        }
    }

    fun processDoneEvents() {
        runBlocking {
            val allDone = database.dbQuery { getAllDone() }
            val allAktivInformasjon = database.dbQuery { getAllInformasjonByAktiv(true) }
            val allOppgave = database.dbQuery { getAllOppgave() }
            allDone.forEach { done ->
                if(allAktivInformasjon.any { it.eventId == done.eventId}) {
                    database.dbQuery { setInformasjonAktiv(done.eventId, false) }
                    log.info("Fant nytt Informasjon-event etter tidligere mottatt Done-event, setter event med eventId ${done.eventId} inaktivt")
                } else if(allOppgave.any {it.eventId == done.eventId}) {
                    database.dbQuery { setOppgaveAktiv(done.eventId, false) }
                    log.info("Fant nytt Oppgave-event etter tidligere mottatt Done-event, setter event med eventId ${done.eventId} inaktivt")
                }
            }
        }
    }
}
