package no.nav.personbruker.dittnav.eventaggregator.database.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.joda.time.DateTime

object InformasjonTable : IntIdTable() {
    val produsent: Column<String> = varchar("produsent", 100)
    val eventTidspunkt: Column<DateTime> = datetime("eventtidspunkt")
    val aktorid: Column<String> = varchar("aktorid", 50)
    val eventId: Column<String> = varchar("eventid", 50)
    val dokumentId: Column<String> = varchar("dokumentid", 100)
    val tekst: Column<String> = varchar("tekst", 500)
    val link: Column<String> = varchar("link", 200)
    val sikkerhetsnivaa: Column<Int> = integer("sikkerhetsnivaa")
    val sistOppdatert: Column<DateTime> = datetime("sistoppdatert")
    val aktiv: Column<Boolean> = bool("aktiv")
}
