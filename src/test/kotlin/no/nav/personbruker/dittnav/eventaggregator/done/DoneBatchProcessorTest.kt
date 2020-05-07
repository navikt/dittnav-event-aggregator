package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.objectmother.BrukernotifikasjonObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should contain all`
import org.junit.jupiter.api.Test

internal class DoneBatchProcessorTest {

    @Test
    fun `skal gruppere done-eventer etter hvilken event-type de tilhorer ut i fra en liste av brukernotifikasjoner`() {
        val existingEntitiesInDatabase = BrukernotifikasjonObjectMother.giveMeOneOfEachEventType()
        val doneEventsToProcess = DoneObjectMother.giveMeOneDoneEventForEach(existingEntitiesInDatabase)
        val doneEventWithoutMatchingBeatification = DoneObjectMother.giveMeDone("4")
        doneEventsToProcess.add(doneEventWithoutMatchingBeatification)


        val result = DoneBatchProcessor.process(doneEventsToProcess, existingEntitiesInDatabase)

        result.eventsMatchingBeskjed.size `should be` 1
        result.eventsMatchingInnboks.size `should be` 1
        result.eventsMatchingOppgave.size `should be` 1
        result.eventsMatchingNone.size `should be` 1

        val totalNumberOfGroupedEvents = result.eventsMatchingAny.size + result.eventsMatchingNone.size
        totalNumberOfGroupedEvents `should be equal to` doneEventsToProcess.size
    }

    @Test
    fun `skal haandtere at det ikke finnes eventer i databasen og at det ikke ble mottatt eventer`() {
        val result = DoneBatchProcessor.process(emptyList(), emptyList())

        result.eventsMatchingBeskjed.size `should be` 0
        result.eventsMatchingInnboks.size `should be` 0
        result.eventsMatchingOppgave.size `should be` 0
        result.eventsMatchingNone.size `should be` 0
    }

    @Test
    fun `skal haandtere at det ikke ble mottatt eventer`() {
        val existingEntitiesInDatabase = BrukernotifikasjonObjectMother.giveMeOneOfEachEventType()
        val result = DoneBatchProcessor.process(emptyList(), existingEntitiesInDatabase)

        result.eventsMatchingBeskjed.size `should be` 0
        result.eventsMatchingInnboks.size `should be` 0
        result.eventsMatchingOppgave.size `should be` 0
        result.eventsMatchingNone.size `should be` 0
    }

    @Test
    fun `skal haandtere at ingen av eventene ble funnet i databasen`() {
        val batchOfEntities = listOf(DoneObjectMother.giveMeDone("1"), DoneObjectMother.giveMeDone("2"))

        val result = DoneBatchProcessor.process(batchOfEntities, emptyList())

        result.eventsMatchingBeskjed.size `should be` 0
        result.eventsMatchingInnboks.size `should be` 0
        result.eventsMatchingOppgave.size `should be` 0
        result.eventsMatchingNone.size `should be` 2
    }

    @Test
    fun `skal returnere alle eventer som ble funnet samlet i en liste`() {
        val existingEntitiesInDatabase = BrukernotifikasjonObjectMother.giveMeOneOfEachEventType()
        val doneEventsToProcess = DoneObjectMother.giveMeOneDoneEventForEach(existingEntitiesInDatabase)

        val result = DoneBatchProcessor.process(doneEventsToProcess, existingEntitiesInDatabase)

        result.allDoneEvents `should contain all` doneEventsToProcess
    }

}
