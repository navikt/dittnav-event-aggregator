package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.setBeskjedAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.getBrukernotifikasjonFromViewByAktiv
import no.nav.personbruker.dittnav.eventaggregator.innboks.setInnboksAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setOppgaveAktivFlag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoneRepository(private val database: Database) {

    val log: Logger = LoggerFactory.getLogger(BeskjedRepository::class.java)

    suspend fun writeDoneEventsForBeskjedToCache(entities: List<Done>) {
        if (entities.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                setBeskjedAktivFlag(entity.eventId, entity.produsent, entity.fodselsnummer, false)
            }
        }
        log.info("Har satt ${entities.size} beskjed-eventer til inaktiv.")
    }

    suspend fun writeDoneEventsForOppgaveToCache(entities: List<Done>) {
        if (entities.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                setOppgaveAktivFlag(entity.eventId, entity.produsent, entity.fodselsnummer, false)
            }
        }
        log.info("Har satt ${entities.size} oppgave-eventer til inaktiv.")
    }

    suspend fun writeDoneEventsForInnboksToCache(entities: List<Done>) {
        if (entities.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                setInnboksAktivFlag(entity.eventId, entity.produsent, entity.fodselsnummer, false)
            }
        }
        log.info("Har satt ${entities.size} innboks-eventer til inaktiv.")
    }

    suspend fun writeDoneEventToCache(entities: List<Done>) {
        if (entities.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                createDoneEvent(entity)
            }
        }
        log.info("Har skrevet ${entities.size} done-eventer til vente-tabellen.")
    }

    suspend fun fetchActiveBrukernotifikasjonerFromView(): List<Brukernotifikasjon> {
        var resultat = emptyList<Brukernotifikasjon>()
        database.queryWithExceptionTranslation {
            resultat = getBrukernotifikasjonFromViewByAktiv(true)
        }
        if (resultat.isEmpty()) {
            log.warn("Fant ingen aktive brukernotifikasjoner i databasen")
        }
        return resultat
    }

    suspend fun fetchInaktiveBrukernotifikasjonerFromView(): List<Brukernotifikasjon> {
        var resultat = emptyList<Brukernotifikasjon>()
        database.queryWithExceptionTranslation {
            resultat = getBrukernotifikasjonFromViewByAktiv(false)
        }
        if (resultat.isEmpty()) {
            log.warn("Fant ingen inaktive brukernotifikasjoner i databasen")
        }
        return resultat
    }

    suspend fun fetchAllDoneEvents(): List<Done> {
        var resultat = emptyList<Done>()
        database.queryWithExceptionTranslation {
            resultat = getAllDoneEvent()
        }
        return resultat
    }

    suspend fun deleteDoneEventFromCache(doneEventsToDelete: List<Done>) {
        database.queryWithExceptionTranslation {
            doneEventsToDelete.forEach { doneEvent ->
                deleteDoneEvent(doneEvent)
            }
        }
        log.info("Har fjernet ${doneEventsToDelete.size} done-eventer fra ventetabellen.")
    }

}
