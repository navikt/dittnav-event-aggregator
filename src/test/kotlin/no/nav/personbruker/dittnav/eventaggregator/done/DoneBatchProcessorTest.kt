package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother.BrukernotifikasjonObjectMother
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

        val processor = DoneBatchProcessor(existingEntitiesInDatabase)
        processor.process(doneEventsToProcess)

        processor.foundBeskjed.size `should be` 1
        processor.foundInnboks.size `should be` 1
        processor.foundOppgave.size `should be` 1
        processor.notFoundEvents.size `should be` 1

        val totalNumberOfGroupedEvents = processor.allFoundEvents().size + processor.notFoundEvents.size
        totalNumberOfGroupedEvents `should be equal to` doneEventsToProcess.size
    }

    @Test
    fun `skal haandtere at det ikke finnes eventer i databasen og at det ikke ble mottatt eventer`() {
        val processor = DoneBatchProcessor(emptyList())

        processor.process(emptyList())

        processor.foundBeskjed.size `should be` 0
        processor.foundInnboks.size `should be` 0
        processor.foundOppgave.size `should be` 0
        processor.notFoundEvents.size `should be` 0
    }

    @Test
    fun `skal haandtere at det ikke ble mottatt eventer`() {
        val existingEntitiesInDatabase = BrukernotifikasjonObjectMother.giveMeOneOfEachEventType()
        val processor = DoneBatchProcessor(existingEntitiesInDatabase)

        processor.process(emptyList())

        processor.foundBeskjed.size `should be` 0
        processor.foundInnboks.size `should be` 0
        processor.foundOppgave.size `should be` 0
        processor.notFoundEvents.size `should be` 0
        processor.isMoreEventsToProcess() `should be equal to` false
    }

    @Test
    fun `skal haandtere at ingen av eventene ble funnet i databasen`() {
        val processor = DoneBatchProcessor(emptyList())
        val batchOfEntities = listOf(DoneObjectMother.giveMeDone("1"), DoneObjectMother.giveMeDone("2"))

        processor.process(batchOfEntities)

        processor.foundBeskjed.size `should be` 0
        processor.foundInnboks.size `should be` 0
        processor.foundOppgave.size `should be` 0
        processor.notFoundEvents.size `should be` 2
        processor.isMoreEventsToProcess() `should be equal to` true
    }

    @Test
    fun `skal returnere alle eventer som ble funnet samlet i en liste`() {
        val existingEntitiesInDatabase = BrukernotifikasjonObjectMother.giveMeOneOfEachEventType()
        val doneEventsToProcess = DoneObjectMother.giveMeOneDoneEventForEach(existingEntitiesInDatabase)

        val processor = DoneBatchProcessor(existingEntitiesInDatabase)
        processor.process(doneEventsToProcess)

        processor.allFoundEvents() `should contain all` doneEventsToProcess
        processor.totalNumberOfFoundEvents() `should be` doneEventsToProcess.size
    }

}
