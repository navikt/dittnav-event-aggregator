package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.*
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.informasjon.getAllInformasjonByAktiv
import no.nav.personbruker.dittnav.eventaggregator.informasjon.setInformasjonAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.melding.getAllMeldingByAktiv
import no.nav.personbruker.dittnav.eventaggregator.melding.setMeldingAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgaveByAktiv
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setOppgaveAktivFlag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.coroutines.CoroutineContext

class CachedDoneEventConsumer(
        val database: Database,
        val job: Job = Job()
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
            val allDone = database.dbQuery { getAllDoneEvent() }
            val allAktivInformasjon = database.dbQuery { getAllInformasjonByAktiv(true) }
            val allAktivOppgave = database.dbQuery { getAllOppgaveByAktiv(true) }
            val allAktivMelding = database.dbQuery { getAllMeldingByAktiv(true) }
            allDone.forEach { done ->
                if(allAktivInformasjon.any { it.eventId == done.eventId}) {
                    database.dbQuery { setInformasjonAktivFlag(done.eventId, false) }
                    log.info("Fant nytt Informasjon-event etter tidligere mottatt Done-event, setter event med eventId ${done.eventId} inaktivt")
                } else if(allAktivOppgave.any {it.eventId == done.eventId}) {
                    database.dbQuery { setOppgaveAktivFlag(done.eventId, false) }
                    log.info("Fant nytt Oppgave-event etter tidligere mottatt Done-event, setter event med eventId ${done.eventId} inaktivt")
                } else if(allAktivMelding.any {it.eventId == done.eventId}) {
                    database.dbQuery { setMeldingAktivFlag(done.eventId, false) }
                    log.info("Fant nytt Melding-event etter tidligere mottatt Done-event, setter event med eventId ${done.eventId} inaktivt")
                }
            }
        }
    }
}
