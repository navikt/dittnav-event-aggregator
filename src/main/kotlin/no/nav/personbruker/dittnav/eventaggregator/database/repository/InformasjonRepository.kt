package no.nav.personbruker.dittnav.eventaggregator.database.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.personbruker.dittnav.eventaggregator.config.DatabaseConnectionFactory.dbQuery
import no.nav.personbruker.dittnav.eventaggregator.database.entity.Informasjon
import no.nav.personbruker.dittnav.eventaggregator.database.tables.InformasjonTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class InformasjonRepository {

    suspend fun getAllInfo(): List<Informasjon> = dbQuery {
        InformasjonTable.selectAll().map { row ->
            toInformasjon(row)
        }
    }

    suspend fun getInformasjonByAktorid(aktorid: String): Informasjon? = dbQuery {
        InformasjonTable.select {
            (InformasjonTable.aktorid eq aktorid)
        }.mapNotNull { toInformasjon(it) }
                .singleOrNull()
    }

    private fun toInformasjon(row: ResultRow): Informasjon =
            Informasjon(
                    id = row[InformasjonTable.id],
                    produsent = row[InformasjonTable.produsent],
                    eventTidspunkt = row[InformasjonTable.eventTidspunkt],
                    aktorid = row[InformasjonTable.aktorid],
                    eventId = row[InformasjonTable.eventId],
                    dokumentId = row[InformasjonTable.dokumentId],
                    tekst = row[InformasjonTable.tekst],
                    link = row[InformasjonTable.link],
                    sikkerhetsnivaa = row[InformasjonTable.sikkerhetsnivaa],
                    sistOppdatert = row[InformasjonTable.sistOppdatert],
                    aktiv = row[InformasjonTable.aktiv]
            )

    suspend fun createInfo(info: Informasjon): EntityID<Int> {
        return withContext(Dispatchers.IO) {
            transaction {
                val generatedId = InformasjonTable.insertAndGetId {
                    it[produsent] = info.produsent
                    it[eventTidspunkt] = info.eventTidspunkt
                    it[aktorid] = info.aktorid
                    it[eventId] = info.eventId
                    it[dokumentId] = info.dokumentId
                    it[tekst] = info.tekst
                    it[link] = info.link
                    it[sikkerhetsnivaa] = info.sikkerhetsnivaa
                    it[sistOppdatert] = info.sistOppdatert
                    it[aktiv] = info.aktiv
                }
                return@transaction generatedId
            }
        }
    }

    suspend fun getInformasjonById(id: EntityID<Int>): Informasjon? = dbQuery {
        InformasjonTable.select {
            (InformasjonTable.id eq id)
        }.mapNotNull { toInformasjon(it) }
                .singleOrNull()
    }

}
