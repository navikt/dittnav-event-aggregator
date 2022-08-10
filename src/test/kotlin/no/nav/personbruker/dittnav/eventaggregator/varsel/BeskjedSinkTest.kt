package no.nav.personbruker.dittnav.eventaggregator.varsel

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.beskjed.toBeskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.LocalPostgresDatabase
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.list
import org.junit.jupiter.api.Test

class BeskjedSinkTest {
    private val testRapid = TestRapid()

    private val database = LocalPostgresDatabase.migratedDb()
    private val beskjedRepository = BeskjedRepository(database)


    @Test
    fun `test `() {
        val beskjedSink = BeskjedSink(testRapid, beskjedRepository)


        //testRapid.sendTestMessage()
        runBlocking {
            val beskjeder = getBeskjeder()

            beskjeder.size shouldBe 0
        }
    }


    private suspend fun getBeskjeder(): List<Beskjed> {
        return database.dbQuery { this.prepareStatement("select * from beskjed").executeQuery().list { toBeskjed() } }
    }
}