package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test

internal class DoneBatchProcessorTest {

    @Test
    fun `skal gruppere done-eventer etter hvilken event-type de tilhorer ut i fra en liste av brukernotifikasjoner`() {
        val allBeskjed = listOf(BeskjedObjectMother.giveMeBeskjed())
        val allInnboks = listOf(InnboksObjectMother.giveMeInnboks())
        val allOppgave = listOf(OppgaveObjectMother.giveMeOppgave())

        val doneBeskjed = DoneObjectMother.giveMeMatchingDoneEvent(allBeskjed[0])
        val doneInnboks = DoneObjectMother.giveMeMatchingDoneEvent(allInnboks[0])
        val doneOppgave = DoneObjectMother.giveMeMatchingDoneEvent(allOppgave[0])
        val doneEventWithoutMatchingBeatification = DoneObjectMother.createDone("4")
        val doneEventsToProcess = listOf(doneBeskjed, doneInnboks, doneOppgave, doneEventWithoutMatchingBeatification)

        val processor = DoneBatchProcessor(allBeskjed, allInnboks, allOppgave)
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

    @Test
    fun `skal haandtere at det ikke finnes eventer i databasen og at det ikke ble mottatt eventer`() {
        val processor = DoneBatchProcessor(emptyList(), emptyList(), emptyList())

        processor.process(emptyList())

        processor.foundBeskjed.size `should be` 0
        processor.foundInnboks.size `should be` 0
        processor.foundOppgave.size `should be` 0
        processor.notFoundEvents.size `should be` 0
    }

    @Test
    fun `skal haandtere at det ikke ble mottatt eventer`() {
        val allBeskjed = listOf(BeskjedObjectMother.giveMeBeskjed())
        val allInnboks = listOf(InnboksObjectMother.giveMeInnboks())
        val allOppgave = listOf(OppgaveObjectMother.giveMeOppgave())
        val processor = DoneBatchProcessor(allBeskjed, allInnboks, allOppgave)

        processor.process(emptyList())

        processor.foundBeskjed.size `should be` 0
        processor.foundInnboks.size `should be` 0
        processor.foundOppgave.size `should be` 0
        processor.notFoundEvents.size `should be` 0
    }

    @Test
    fun `skal haandtere at ingen av eventene ble funnet i databasen`() {
        val processor = DoneBatchProcessor(emptyList(), emptyList(), emptyList())
        val batchOfEntities = listOf(DoneObjectMother.createDone("1"), DoneObjectMother.createDone("2"))

        processor.process(batchOfEntities)

        processor.foundBeskjed.size `should be` 0
        processor.foundInnboks.size `should be` 0
        processor.foundOppgave.size `should be` 0
        processor.notFoundEvents.size `should be` 2
    }

}
