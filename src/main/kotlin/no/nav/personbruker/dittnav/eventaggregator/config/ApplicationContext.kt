package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.eventaggregator.archive.PeriodicVarselArchiver
import no.nav.personbruker.dittnav.eventaggregator.archive.VarselArchivingRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.done.DonePersistingService
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.PeriodicDoneEventWaitingTableProcessor
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildArchivingMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.metrics.buildDBMetricsProbe

class ApplicationContext {

    val environment = Environment()
    val database: Database = PostgresDatabase(environment)

    private val archivingMetricsProbe = buildArchivingMetricsProbe(environment)
    private val varselArchivingRepository = VarselArchivingRepository(database)

    private val varselArchiver = PeriodicVarselArchiver(varselArchivingRepository, archivingMetricsProbe, environment.archivingThresholdDays)

    fun startAllArchivers() {
        if (environment.archivingEnabled) {
            varselArchiver.start()
        }
    }

    suspend fun stopAllArchivers() {
        if (environment.archivingEnabled) {
            varselArchiver.stop()
        }
    }
}
