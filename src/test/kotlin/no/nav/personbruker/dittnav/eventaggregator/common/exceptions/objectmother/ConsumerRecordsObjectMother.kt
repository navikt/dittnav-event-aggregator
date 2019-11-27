package no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Innboks
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.AvroInnboksObjectMother
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition

object ConsumerRecordsObjectMother {

    fun giveMeANumberOfInformationRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<String, Beskjed> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<String, Beskjed>>>()
        val recordsForSingleTopic = createBeskjedRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    private fun createBeskjedRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<String, Beskjed>> {
        val allRecords = mutableListOf<ConsumerRecord<String, Beskjed>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroBeskjedObjectMother.createBeskjed(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), "key-$i", schemaRecord))
        }
        return allRecords
    }

    fun giveMeANumberOfDoneRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<String, Done> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<String, Done>>>()
        val recordsForSingleTopic = createDoneRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    private fun createDoneRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<String, Done>> {
        val allRecords = mutableListOf<ConsumerRecord<String, Done>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroDoneObjectMother.createDone("$i")
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), "key-$i", schemaRecord))
        }
        return allRecords
    }

    fun giveMeANumberOfInnboksRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<String, Innboks> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<String, Innboks>>>()
        val recordsForSingleTopic = createInnboksRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    private fun createInnboksRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<String, Innboks>> {
        val allRecords = mutableListOf<ConsumerRecord<String, Innboks>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroInnboksObjectMother.createInnboks(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), "key-$i", schemaRecord))
        }
        return allRecords
    }

    fun wrapInConsumerRecords(singleRecord: ConsumerRecord<String, Done>, topicName: String = "dummyTopic"): ConsumerRecords<String, Done> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<String, Done>>>()
        val recordsForSingleTopic = listOf(singleRecord)
        records[TopicPartition(topicName, 1)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

}
