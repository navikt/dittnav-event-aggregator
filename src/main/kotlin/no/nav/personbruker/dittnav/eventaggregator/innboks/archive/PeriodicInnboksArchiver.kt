package no.nav.personbruker.dittnav.eventaggregator.innboks.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.ArchiveMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.PeriodicDoneEventWaitingTableProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

class PeriodicInnboksArchiver(
    private val innboksArchiveRepository: InnboksArchivingRepository,
    private val archiveMetricsProbe: ArchiveMetricsProbe,
    private val ageThresholdDays: Int,
    interval: Duration = Duration.ofSeconds(10)
): PeriodicJob(interval) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicDoneEventWaitingTableProcessor::class.java)

    override val job = initializeJob {
        archiveOldInnboks()
    }

    private suspend fun archiveOldInnboks() {
        val thresholdDate = nowAtUtc().minusDays(ageThresholdDays.toLong())

        try {
            val toArchive = innboksArchiveRepository.getOldInnboksAsArchiveDto(thresholdDate)

            if (toArchive.isNotEmpty()) {
                innboksArchiveRepository.moveToInnboksArchive(toArchive)

                archiveMetricsProbe.countEntitiesArchived(EventType.INNBOKS_INTERN, toArchive)
            }

        } catch (rt: RetriableDatabaseException) {
            log.warn("Fikk en periodisk feil mot databasen ved arkivering av Innboks. Forsøker igjen senere.", rt)
        } catch (e: Exception) {
            log.error("Fikk feil mot databasen ved arkivering av innboks. Stopper prosessering.", e)
            stop()
        }
    }
}
