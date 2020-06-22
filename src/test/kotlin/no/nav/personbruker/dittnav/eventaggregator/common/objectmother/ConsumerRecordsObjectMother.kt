package no.nav.personbruker.dittnav.eventaggregator.common.objectmother

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.AvroInnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import no.nav.personbruker.dittnav.eventaggregator.oppgave.AvroOppgaveObjectMother
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition

object ConsumerRecordsObjectMother {

    fun createMatchingRecords(entitiesInDbToMatch: List<Brukernotifikasjon>): ConsumerRecords<Nokkel, Done> {
        val listOfConsumerRecord = mutableListOf<ConsumerRecord<Nokkel, Done>>()
        entitiesInDbToMatch.forEach { entity ->
            val done = AvroDoneObjectMother.createDoneRecord(entity.eventId, entity.fodselsnummer)
            listOfConsumerRecord.add(done)
        }
        return giveMeConsumerRecordsWithThisConsumerRecord(listOfConsumerRecord)
    }

    fun createMatchingRecords(entityInDbToMatch: Brukernotifikasjon): ConsumerRecords<Nokkel, Done> {
        val matchingDoneEvent = AvroDoneObjectMother.createDoneRecord(entityInDbToMatch.eventId, entityInDbToMatch.fodselsnummer)
        return giveMeConsumerRecordsWithThisConsumerRecord(matchingDoneEvent)
    }

    fun <T> giveMeConsumerRecordsWithThisConsumerRecord(listOfConcreteRecords: List<ConsumerRecord<Nokkel, T>>): ConsumerRecords<Nokkel, T> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<Nokkel, T>>>()
        records[TopicPartition(listOfConcreteRecords[0].topic(), 1)] = listOfConcreteRecords
        return ConsumerRecords(records)
    }

    fun <T> giveMeConsumerRecordsWithThisConsumerRecord(concreteRecord: ConsumerRecord<Nokkel, T>): ConsumerRecords<Nokkel, T> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<Nokkel, T>>>()
        records[TopicPartition(concreteRecord.topic(), 1)] = listOf(concreteRecord)
        return ConsumerRecords(records)
    }

    fun giveMeANumberOfBeskjedRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<Nokkel, Beskjed> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<Nokkel, Beskjed>>>()
        val recordsForSingleTopic = createBeskjedRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    private fun createBeskjedRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<Nokkel, Beskjed>> {
        val allRecords = mutableListOf<ConsumerRecord<Nokkel, Beskjed>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroBeskjedObjectMother.createBeskjed(i)
            val nokkel = createNokkel(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
    }

    fun <T> createConsumerRecord(topicName: String, actualEvent: T): ConsumerRecord<Nokkel, T> {
        val nokkel = createNokkel(1)
        return ConsumerRecord(topicName, 1, 0, nokkel, actualEvent)
    }

    fun <T> createConsumerRecord(nokkel: Nokkel, actualEvent: T): ConsumerRecord<Nokkel, T> {
        return ConsumerRecord("dummyTopic", 1, 0, nokkel, actualEvent)
    }

    fun giveMeANumberOfInnboksRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<Nokkel, Innboks> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<Nokkel, Innboks>>>()
        val recordsForSingleTopic = createInnboksRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    fun giveMeANumberOfOppgaveRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<Nokkel, Oppgave> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<Nokkel, Oppgave>>>()
        val recordsForSingleTopic = createOppgaveRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }


    private fun createInnboksRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<Nokkel, Innboks>> {
        val allRecords = mutableListOf<ConsumerRecord<Nokkel, Innboks>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroInnboksObjectMother.createInnboks(i)
            val nokkel = createNokkel(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
    }


    private fun createOppgaveRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<Nokkel, Oppgave>> {
        val allRecords = mutableListOf<ConsumerRecord<Nokkel, Oppgave>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroOppgaveObjectMother.createOppgave(i)
            val nokkel = createNokkel(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
    }

    fun wrapInConsumerRecords(recordsForSingleTopic: List<ConsumerRecord<Nokkel, Done>>, topicName: String = "dummyTopic"): ConsumerRecords<Nokkel, Done> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<Nokkel, Done>>>()
        records[TopicPartition(topicName, 1)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    fun wrapInConsumerRecords(singleRecord: ConsumerRecord<Nokkel, Done>): ConsumerRecords<Nokkel, Done> {
        return wrapInConsumerRecords(listOf(singleRecord))
    }

}
