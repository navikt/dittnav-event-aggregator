package no.nav.personbruker.dittnav.eventaggregator.done

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import no.nav.personbruker.dittnav.eventaggregator.beskjed.getAllBeskjedByAktiv
import no.nav.personbruker.dittnav.eventaggregator.beskjed.setBeskjedAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.innboks.getAllInnboksByAktiv
import no.nav.personbruker.dittnav.eventaggregator.innboks.setInnboksAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.oppgave.getAllOppgaveByAktiv
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setOppgaveAktivFlag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class CachedDoneEventConsumer(
        val database: Database,
        val job: Job = Job()
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
        val allDone = database.dbQuery { getAllDoneEvent() }
        val allAktivBeskjed = database.dbQuery { getAllBeskjedByAktiv(true) }
        val allAktivOppgave = database.dbQuery { getAllOppgaveByAktiv(true) }
        val allAktivInnboks = database.dbQuery { getAllInnboksByAktiv(true) }
        allDone.forEach { done ->
            if (allAktivBeskjed.any { beskjed -> beskjed.eventId == done.eventId && beskjed.produsent == done.produsent && beskjed.fodselsnummer == done.fodselsnummer }) {
                database.dbQuery { setBeskjedAktivFlag(done.eventId, done.produsent, done.fodselsnummer, false) }
                log.info("Fant nytt Beskjed-event etter tidligere mottatt Done-event, setter event med eventId ${done.eventId} inaktivt")
            } else if (allAktivOppgave.any { oppgave -> oppgave.eventId == done.eventId && oppgave.produsent == done.produsent && oppgave.fodselsnummer == done.fodselsnummer }) {
                database.dbQuery { setOppgaveAktivFlag(done.eventId, done.produsent, done.fodselsnummer, false) }
                log.info("Fant nytt Oppgave-event etter tidligere mottatt Done-event, setter event med eventId ${done.eventId} inaktivt")
            } else if (allAktivInnboks.any { innboks -> innboks.eventId == done.eventId && innboks.produsent == done.produsent && innboks.fodselsnummer == done.fodselsnummer }) {
                database.dbQuery { setInnboksAktivFlag(done.eventId, done.produsent, done.fodselsnummer, false) }
                log.info("Fant nytt Innboks-event etter tidligere mottatt Done-event, setter event med eventId ${done.eventId} inaktivt")
            }
        }
    }
}
