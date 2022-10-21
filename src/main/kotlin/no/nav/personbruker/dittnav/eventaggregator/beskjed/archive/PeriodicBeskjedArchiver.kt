package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import no.nav.personbruker.dittnav.eventaggregator.archive.ArchiveMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.PeriodicDoneEventWaitingTableProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

class PeriodicBeskjedArchiver(
    private val beskjedArchiveRepository: BeskjedArchivingRepository,
    private val archiveMetricsProbe: ArchiveMetricsProbe,
    private val ageThresholdDays: Int,
    interval: Duration = Duration.ofSeconds(10)
): PeriodicJob(interval) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicDoneEventWaitingTableProcessor::class.java)

    override val job = initializeJob {
        archiveOldBeskjed()
    }

    private suspend fun archiveOldBeskjed() {
        val thresholdDate = nowAtUtc().minusDays(ageThresholdDays.toLong())

        try {
            val toArchive = beskjedArchiveRepository.getOldBeskjedAsArchiveDto(thresholdDate)

            if (toArchive.isNotEmpty()) {
                beskjedArchiveRepository.moveToBeskjedArchive(toArchive)

                archiveMetricsProbe.countEntitiesArchived(EventType.BESKJED_INTERN, toArchive)
            }

        } catch (rt: RetriableDatabaseException) {
            log.warn("Fikk en periodisk feil mot databasen ved arkivering av Beskjed. Forsøker igjen senere.", rt)
        } catch (e: Exception) {
            log.error("Fikk feil mot databasen ved arkivering av beskjed. Stopper prosessering.", e)
        }
    }
}
