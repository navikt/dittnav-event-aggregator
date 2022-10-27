package no.nav.personbruker.dittnav.eventaggregator.archive

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.PeriodicDoneEventWaitingTableProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class PeriodicVarselArchiver(
    private val varselArchivingRepository: VarselArchivingRepository,
    private val archiveMetricsProbe: ArchiveMetricsProbe,
    private val ageThresholdDays: Int,
    interval: Duration = Duration.ofSeconds(10)
): PeriodicJob(interval) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicDoneEventWaitingTableProcessor::class.java)

    override val job = initializeJob {
        archiveOldVarsler()
    }

    private suspend fun archiveOldVarsler() {
        val thresholdDate = nowAtUtc().minusDays(ageThresholdDays.toLong())

        try {

            val archivedBeskjeder = varselArchivingRepository.archiveOldBeskjeder(thresholdDate)
            archiveMetricsProbe.countEntitiesArchived(EventType.BESKJED_INTERN, archivedBeskjeder)

            val archivedOppgaver = varselArchivingRepository.archiveOldOppgaver(thresholdDate)
            archiveMetricsProbe.countEntitiesArchived(EventType.OPPGAVE_INTERN, archivedOppgaver)

            val archivedInnbokser = varselArchivingRepository.archiveOldInnbokser(thresholdDate)
            archiveMetricsProbe.countEntitiesArchived(EventType.INNBOKS_INTERN, archivedInnbokser)

        } catch (rt: RetriableDatabaseException) {
            log.warn("Fikk en periodisk feil mot databasen ved arkivering av Beskjed. Forsøker igjen senere.", rt)
        } catch (e: Exception) {
            log.error("Fikk feil mot databasen ved arkivering av beskjed. Stopper prosessering.", e)
        }
    }
}
