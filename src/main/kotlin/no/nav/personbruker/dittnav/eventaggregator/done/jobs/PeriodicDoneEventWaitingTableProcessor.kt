package no.nav.personbruker.dittnav.eventaggregator.done.jobs

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.common.PeriodicJob
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.RetriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.UnretriableDatabaseException
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.Done
import no.nav.personbruker.dittnav.eventaggregator.done.DoneRepository
import no.nav.personbruker.dittnav.eventaggregator.done.VarselInaktivertProducer
import no.nav.personbruker.dittnav.eventaggregator.metrics.db.DBMetricsProbe
import no.nav.personbruker.dittnav.eventaggregator.varsel.HendelseType.Inaktivert
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHendelse
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

class PeriodicDoneEventWaitingTableProcessor(
    private val doneRepository: DoneRepository,
    private val varselInaktivertProducer: VarselInaktivertProducer,
    private val dbMetricsProbe: DBMetricsProbe,
) : PeriodicJob(interval = Duration.ofSeconds(30)) {

    private val log: Logger = LoggerFactory.getLogger(PeriodicDoneEventWaitingTableProcessor::class.java)

    override val job = initializeJob {
        processDoneEvents()
    }

    suspend fun processDoneEvents() {
        try {

            val allDoneEventsWithinLimit = doneRepository.fetchAllDoneEventsWithLimit()
            val groupedDoneEvents = fetchRelatedEvents(allDoneEventsWithinLimit)
            groupedDoneEvents.process(allDoneEventsWithinLimit)
            dbMetricsProbe.runWithMetrics(eventType = EventType.DONE_INTERN) {
                groupedDoneEvents.notFoundEvents.forEach { event ->
                    countCachedEventForProducer(event.appnavn)
                }
            }
            updateTheDatabase(groupedDoneEvents)
            sendVarselInaktivert(groupedDoneEvents)

        } catch (rde: RetriableDatabaseException) {
            log.warn(
                "Behandling av done-eventer fra ventetabellen feilet. Klarte ikke å skrive til databasen, prøver igjen senere. Context: ${rde.context}",
                rde
            )

        } catch (ure: UnretriableDatabaseException) {
            log.warn(
                "Behandling av done-eventer fra ventetabellen feilet. Klarte ikke å skrive til databasen, prøver igjen senere. Context: ${ure.context}",
                ure
            )

        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av done-eventer fra ventetabellen, forsøker igjen senere", e)
        }
    }

    private suspend fun fetchRelatedEvents(allDone: List<Done>): DoneBatchProcessor {
        val eventIds = allDone.map { it.eventId }.distinct()
        val activeVarsler = doneRepository.fetchVarslerFromViewForEventIds(eventIds)
        return DoneBatchProcessor(activeVarsler)
    }

    private suspend fun updateTheDatabase(groupedDoneEvents: DoneBatchProcessor) {
        doneRepository.updateVarselTables(groupedDoneEvents.foundBeskjed, VarselType.BESKJED)
        doneRepository.updateVarselTables(groupedDoneEvents.foundOppgave, VarselType.OPPGAVE)
        doneRepository.updateInnboksTable(groupedDoneEvents.foundInnboks)
        doneRepository.deleteDoneEventsFromCache(groupedDoneEvents.allFoundEvents)
        doneRepository.updateDoneEventsSistBehandlet(
            groupedDoneEvents.notFoundEvents,
            LocalDateTimeHelper.nowAtUtc()
        )
    }

    private fun sendVarselInaktivert(groupedDoneEvents: DoneBatchProcessor) {
        groupedDoneEvents.allFoundEventsByType.forEach { (type, done) ->
            varselInaktivertProducer.varselInaktivert(
                VarselHendelse(Inaktivert, type.toVarselType(), eventId = done.eventId, namespace = done.namespace, appnavn = done.appnavn)
            )
        }
    }
}
