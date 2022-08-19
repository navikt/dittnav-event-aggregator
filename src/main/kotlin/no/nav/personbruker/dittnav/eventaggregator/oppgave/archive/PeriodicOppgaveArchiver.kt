package no.nav.personbruker.dittnav.eventaggregator.oppgave.archive

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

class PeriodicOppgaveArchiver(
    private val oppgaveArchiveRepository: OppgaveArchivingRepository,
    private val archiveMetricsProbe: ArchiveMetricsProbe,
    private val ageThresholdDays: Int,
    interval: Duration = Duration.ofSeconds(5)
): PeriodicJob(interval) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicDoneEventWaitingTableProcessor::class.java)

    override val job = initializeJob {
        archiveOldOppgave()
    }

    private suspend fun archiveOldOppgave() {
        val thresholdDate = nowAtUtc().minusDays(ageThresholdDays.toLong())

        try {
            val toArchive = oppgaveArchiveRepository.getOldOppgaveAsArchiveDto(thresholdDate)

            if (toArchive.isNotEmpty()) {
                oppgaveArchiveRepository.moveToOppgaveArchive(toArchive)

                archiveMetricsProbe.countEntitiesArchived(EventType.OPPGAVE_INTERN, toArchive)
            }

        } catch (rt: RetriableDatabaseException) {
            log.warn("Fikk en periodisk feil mot databasen ved arkivering av Oppgave. Forsøker igjen senere.", rt)
        } catch (e: Exception) {
            log.error("Fikk feil mot databasen ved arkivering av oppgave. Stopper prosessering.", e)
            stop()
        }
    }
}
