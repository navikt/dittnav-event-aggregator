package no.nav.personbruker.dittnav.eventaggregator.common.objectmother

import no.nav.brukernotifikasjon.schemas.internal.*
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.AvroInnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import no.nav.personbruker.dittnav.eventaggregator.oppgave.AvroOppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.statusoppdatering.AvroStatusoppdateringObjectMother
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition

object ConsumerRecordsObjectMother {

    fun createMatchingRecords(entitiesInDbToMatch: List<Brukernotifikasjon>): ConsumerRecords<NokkelIntern, DoneIntern> {
        val listOfConsumerRecord = mutableListOf<ConsumerRecord<NokkelIntern, DoneIntern>>()
        entitiesInDbToMatch.forEach { entity ->
            val done = AvroDoneObjectMother.createDoneRecord(entity.eventId, entity.fodselsnummer)
            listOfConsumerRecord.add(done)
        }
        return giveMeConsumerRecordsWithThisConsumerRecord(listOfConsumerRecord)
    }

    fun createMatchingRecords(entityInDbToMatch: Brukernotifikasjon): ConsumerRecords<NokkelIntern, DoneIntern> {
        val matchingDoneEvent = AvroDoneObjectMother.createDoneRecord(entityInDbToMatch.eventId, entityInDbToMatch.fodselsnummer)
        return giveMeConsumerRecordsWithThisConsumerRecord(matchingDoneEvent)
    }

    fun <T> giveMeConsumerRecordsWithThisConsumerRecord(listOfConcreteRecords: List<ConsumerRecord<NokkelIntern, T>>): ConsumerRecords<NokkelIntern, T> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<NokkelIntern, T>>>()
        records[TopicPartition(listOfConcreteRecords[0].topic(), 1)] = listOfConcreteRecords
        return ConsumerRecords(records)
    }

    fun <T> giveMeConsumerRecordsWithThisConsumerRecord(concreteRecord: ConsumerRecord<NokkelIntern, T>): ConsumerRecords<NokkelIntern, T> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<NokkelIntern, T>>>()
        records[TopicPartition(concreteRecord.topic(), 1)] = listOf(concreteRecord)
        return ConsumerRecords(records)
    }

    fun giveMeANumberOfBeskjedRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<NokkelIntern, BeskjedIntern> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<NokkelIntern, BeskjedIntern>>>()
        val recordsForSingleTopic = createBeskjedRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    private fun createBeskjedRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<NokkelIntern, BeskjedIntern>> {
        val allRecords = mutableListOf<ConsumerRecord<NokkelIntern, BeskjedIntern>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroBeskjedObjectMother.createBeskjed(i)
            val nokkel = createNokkel(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
    }

    fun <T> createConsumerRecord(topicName: String, actualEvent: T): ConsumerRecord<NokkelIntern, T> {
        val nokkel = createNokkel(1)
        return ConsumerRecord(topicName, 1, 0, nokkel, actualEvent)
    }

    fun <T> createConsumerRecord(nokkel: NokkelIntern, actualEvent: T): ConsumerRecord<NokkelIntern, T> {
        return ConsumerRecord("dummyTopic", 1, 0, nokkel, actualEvent)
    }

    fun giveMeANumberOfInnboksRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<NokkelIntern, InnboksIntern> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<NokkelIntern, InnboksIntern>>>()
        val recordsForSingleTopic = createInnboksRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    fun giveMeANumberOfOppgaveRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<NokkelIntern, OppgaveIntern> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<NokkelIntern, OppgaveIntern>>>()
        val recordsForSingleTopic = createOppgaveRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }


    private fun createInnboksRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<NokkelIntern, InnboksIntern>> {
        val allRecords = mutableListOf<ConsumerRecord<NokkelIntern, InnboksIntern>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroInnboksObjectMother.createInnboks(i)
            val nokkel = createNokkel(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
    }


    private fun createOppgaveRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<NokkelIntern, OppgaveIntern>> {
        val allRecords = mutableListOf<ConsumerRecord<NokkelIntern, OppgaveIntern>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroOppgaveObjectMother.createOppgave(i)
            val nokkel = createNokkel(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
    }

    fun giveMeANumberOfStatusoppdateringRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<NokkelIntern, StatusoppdateringIntern> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<NokkelIntern, StatusoppdateringIntern>>>()
        val recordsForSingleTopic = createStatusoppdateringRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    private fun createStatusoppdateringRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<NokkelIntern, StatusoppdateringIntern>> {
        val allRecords = mutableListOf<ConsumerRecord<NokkelIntern, StatusoppdateringIntern>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroStatusoppdateringObjectMother.createStatusoppdatering(i)
            val nokkel = createNokkel(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
    }

    fun wrapInConsumerRecords(recordsForSingleTopic: List<ConsumerRecord<NokkelIntern, DoneIntern>>, topicName: String = "dummyTopic"): ConsumerRecords<NokkelIntern, DoneIntern> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<NokkelIntern, DoneIntern>>>()
        records[TopicPartition(topicName, 1)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    fun wrapInConsumerRecords(singleRecord: ConsumerRecord<NokkelIntern, DoneIntern>): ConsumerRecords<NokkelIntern, DoneIntern> {
        return wrapInConsumerRecords(listOf(singleRecord))
    }

}
