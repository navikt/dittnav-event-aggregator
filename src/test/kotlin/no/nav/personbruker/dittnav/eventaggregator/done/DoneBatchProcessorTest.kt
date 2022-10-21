package no.nav.personbruker.dittnav.eventaggregator.done

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.personbruker.dittnav.eventaggregator.common.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.common.brukernotifikasjon.BrukernotifikasjonObjectMother
import org.junit.jupiter.api.Test

internal class DoneBatchProcessorTest {

    @Test
    fun `skal gruppere done-eventer etter hvilken event-type de tilhorer ut i fra en liste av brukernotifikasjoner`() {
        val existingEntitiesInDatabase = BrukernotifikasjonObjectMother.giveMeOneOfEachEventType()
        val doneEventsToProcess = existingEntitiesInDatabase.createDoneEventsForEach()
        val doneEventWithoutMatchingBeatification = DoneTestData.done(eventId = "4")
        doneEventsToProcess.add(doneEventWithoutMatchingBeatification)

        val processor = DoneBatchProcessor(existingEntitiesInDatabase)
        processor.process(doneEventsToProcess)

        processor.foundBeskjed.size shouldBe 1
        processor.foundInnboks.size shouldBe 1
        processor.foundOppgave.size shouldBe 1
        processor.notFoundEvents.size shouldBe 1

        val totalNumberOfGroupedEvents = processor.allFoundEvents.size + processor.notFoundEvents.size
        totalNumberOfGroupedEvents shouldBe doneEventsToProcess.size
    }

    @Test
    fun `skal haandtere at det ikke finnes eventer i databasen og at det ikke ble mottatt eventer`() {
        val processor = DoneBatchProcessor(emptyList())

        processor.process(emptyList())

        processor.foundBeskjed.size shouldBe 0
        processor.foundInnboks.size shouldBe 0
        processor.foundOppgave.size shouldBe 0
        processor.notFoundEvents.size shouldBe 0
    }

    @Test
    fun `skal haandtere at det ikke ble mottatt eventer`() {
        val existingEntitiesInDatabase = BrukernotifikasjonObjectMother.giveMeOneOfEachEventType()
        val processor = DoneBatchProcessor(existingEntitiesInDatabase)

        processor.process(emptyList())

        processor.foundBeskjed.size shouldBe 0
        processor.foundInnboks.size shouldBe 0
        processor.foundOppgave.size shouldBe 0
        processor.notFoundEvents.size shouldBe 0
    }

    @Test
    fun `skal haandtere at ingen av eventene ble funnet i databasen`() {
        val processor = DoneBatchProcessor(emptyList())
        val batchOfEntities = listOf(DoneTestData.done("1"), DoneTestData.done("2"))

        processor.process(batchOfEntities)

        processor.foundBeskjed.size shouldBe 0
        processor.foundInnboks.size shouldBe 0
        processor.foundOppgave.size shouldBe 0
        processor.notFoundEvents.size shouldBe 2
    }

    @Test
    fun `skal returnere alle eventer som ble funnet samlet i en liste`() {
        val existingEntitiesInDatabase = BrukernotifikasjonObjectMother.giveMeOneOfEachEventType()
        val doneEventsToProcess = existingEntitiesInDatabase.createDoneEventsForEach()

        val processor = DoneBatchProcessor(existingEntitiesInDatabase)
        processor.process(doneEventsToProcess)

        processor.allFoundEvents shouldContainAll doneEventsToProcess
    }

}

private fun  List<Brukernotifikasjon>.createDoneEventsForEach(): MutableList<Done> {
    val doneEvents = mutableListOf<Done>()
    forEach { brukernotifikasjon ->
        val associatedDoneEvent = DoneTestData.done(
            brukernotifikasjon.eventId,
            brukernotifikasjon.systembruker,
            brukernotifikasjon.fodselsnummer
        )
        doneEvents.add(associatedDoneEvent)
    }
    return doneEvents

}
