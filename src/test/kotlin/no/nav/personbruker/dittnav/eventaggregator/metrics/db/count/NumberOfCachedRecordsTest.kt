package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class NumberOfCachedRecordsTest {

    @Test
    fun `Skal summere totalene riktig`() {
        val cachedRecords = NumberOfCachedRecords(1, 2, 3, 4, 5, 6, 7)

        cachedRecords.beskjedTotal `should be equal to` 1 + 2
        cachedRecords.innboksTotal `should be equal to` 3 + 4
        cachedRecords.oppgaveTotal `should be equal to` 5 + 6
        cachedRecords.doneTotal `should be equal to` 2 + 4 + 6 + 7
    }

}
