package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.eventaggregator.beskjed.archive.BeskjedArchivingRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.archive.PeriodicBeskjedArchiver
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.done.DonePersistingService
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.PeriodicDoneEventWaitingTableProcessor
import no.nav.personbruker.dittnav.eventaggregator.expired.ExpiredVarselRepository
import no.nav.personbruker.dittnav.eventaggregator.expired.PeriodicExpiredVarselProcessor
import no.nav.personbruker.dittnav.eventaggregator.innboks.archive.InnboksArchivingRepository
import no.nav.personbruker.dittnav.eventaggregator.innboks.archive.PeriodicInnboksArchiver
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildArchivingMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildDBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.oppgave.archive.OppgaveArchivingRepository
import no.nav.personbruker.dittnav.eventaggregator.oppgave.archive.PeriodicOppgaveArchiver
import org.slf4j.LoggerFactory

class ApplicationContext {

    private val log = LoggerFactory.getLogger(ApplicationContext::class.java)

    val environment = Environment()
    val database: Database = PostgresDatabase(environment)

    private val dbMetricsProbe = buildDBMetricsProbe(environment)
    private val archivingMetricsProbe = buildArchivingMetricsProbe(environment)

    private val beskjedArchivingRepository = BeskjedArchivingRepository(database)
    private var beskjedArchiver = PeriodicBeskjedArchiver(beskjedArchivingRepository, archivingMetricsProbe, environment.archivingThresholdDays)

    private val oppgaveArchivingRepository = OppgaveArchivingRepository(database)
    private var oppgaveArchiver = PeriodicOppgaveArchiver(oppgaveArchivingRepository, archivingMetricsProbe, environment.archivingThresholdDays)

    private val innboksArchivingRepository = InnboksArchivingRepository(database)
    private var innboksArchiver = PeriodicInnboksArchiver(innboksArchivingRepository, archivingMetricsProbe, environment.archivingThresholdDays)

    private val doneRepository = DoneRepository(database)
    private val donePersistingService = DonePersistingService(doneRepository)

    var periodicDoneEventWaitingTableProcessor = initializeDoneWaitingTableProcessor()

    private fun initializeDoneWaitingTableProcessor() = PeriodicDoneEventWaitingTableProcessor(donePersistingService, dbMetricsProbe)

    fun startAllArchivers() {
        if (environment.archivingEnabled) {
            beskjedArchiver.start()
            oppgaveArchiver.start()
            innboksArchiver.start()
        }
    }

    suspend fun stopAllArchivers() {
        if (environment.archivingEnabled) {
            beskjedArchiver.stop()
            oppgaveArchiver.stop()
            innboksArchiver.stop()
        }
    }
}
