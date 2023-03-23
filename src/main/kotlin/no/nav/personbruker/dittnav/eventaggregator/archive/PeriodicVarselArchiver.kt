package no.nav.personbruker.dittnav.eventaggregator.archive

import mu.KotlinLogging
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.common.database.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import java.time.Duration

class PeriodicVarselArchiver(
    private val varselArchivingRepository: VarselArchivingRepository,
    private val archiveMetricsProbe: ArchiveMetricsProbe,
    private val ageThresholdDays: Int,
    interval: Duration = Duration.ofSeconds(10)
) : PeriodicJob(interval) {

    private val log = KotlinLogging.logger { }

    override val job = initializeJob {
        archiveOldVarsler()
    }

    private suspend fun archiveOldVarsler() {
        val thresholdDate = nowAtUtc().minusDays(ageThresholdDays.toLong())

        try {
            val archivedBeskjeder = varselArchivingRepository.archiveOldVarsler(VarselType.BESKJED, thresholdDate)
            archiveMetricsProbe.countEntitiesArchived(EventType.BESKJED_INTERN, archivedBeskjeder)

            val archivedOppgaver = varselArchivingRepository.archiveOldVarsler(VarselType.OPPGAVE, thresholdDate)
            archiveMetricsProbe.countEntitiesArchived(EventType.OPPGAVE_INTERN, archivedOppgaver)

            val archivedInnbokser = varselArchivingRepository.archiveOldVarsler(VarselType.INNBOKS, thresholdDate)
            archiveMetricsProbe.countEntitiesArchived(EventType.INNBOKS_INTERN, archivedInnbokser)

        } catch (rt: RetriableDatabaseException) {
            log.warn("Fikk en periodisk feil mot databasen ved arkivering av Beskjed. Forsøker igjen senere.", rt)
        } catch (e: Exception) {
            log.error("Fikk feil mot databasen ved arkivering av beskjed. Forsøker igjen senere.", e)
        }
    }
}
