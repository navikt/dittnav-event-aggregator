package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother.BrukernotifikasjonObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test

internal class DoneBatchProcessorTest {

    @Test
    fun `skal gruppere done-eventer etter hvilken event-type de tilhorer ut i fra en liste av brukernotifikasjoner`() {
        val existingEntitiesInDatabase = BrukernotifikasjonObjectMother.giveMeOneOfEachEventType()
        val doneEventsToProcess = DoneObjectMother.giveMeOneDoneEventForEach(existingEntitiesInDatabase)
        val doneEventWithoutMatchingBeatification = DoneObjectMother.createDone("4")
        doneEventsToProcess.add(doneEventWithoutMatchingBeatification)

        val processor = DoneBatchProcessor(existingEntitiesInDatabase)
        processor.process(doneEventsToProcess)

        processor.foundBeskjed.size `should be` 1
        processor.foundInnboks.size `should be` 1
        processor.foundOppgave.size `should be` 1
        processor.notFoundEvents.size `should be` 1

        val totalNumberOfGroupedEvents = processor.foundBeskjed.size +
                processor.foundInnboks.size +
                processor.foundOppgave.size +
                processor.notFoundEvents.size
        totalNumberOfGroupedEvents `should be equal to` doneEventsToProcess.size
    }

}
