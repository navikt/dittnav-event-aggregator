package no.nav.personbruker.dittnav.eventaggregator.beskjed.archive

import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.done.PeriodicDoneEventWaitingTableProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

class PeriodicBeskjedArchiver(
    private val beskjedArchiveRepository: BeskjedArchivingRepository,
    private val ageThresholdDays: Int
): PeriodicJob(interval = Duration.ofSeconds(5)) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicDoneEventWaitingTableProcessor::class.java)

    override val job = initializeJob {
        archiveOldBeskjed()
    }

    private suspend fun archiveOldBeskjed() {
        val thresholdDate = LocalDateTime.now().minusDays(ageThresholdDays.toLong())

        try {
            val toArchive = beskjedArchiveRepository.getBeskjedOlderThan(thresholdDate)

            beskjedArchiveRepository.moveToArchive(toArchive)
        } catch (rt: RetriableDatabaseException) {
            log.warn("Fikk en periodisk feil mot databasen ved arkivering av Beskjed. Forsøker igjen senere.")
        } catch (e: Exception) {
            log.error("Fikk feil mot databasen ved arkivering av beskjed. Stopper prosessering.")
            stop()
        }
    }
}
