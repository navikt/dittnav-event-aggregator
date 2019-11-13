package no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.brukernotifikasjon.schemas.Melding
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.informasjon.AvroInformasjonObjectMother
import no.nav.personbruker.dittnav.eventaggregator.melding.AvroMeldingObjectMother
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition

object ConsumerRecordsObjectMother {

    fun giveMeANumberOfInformationRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<String, Informasjon> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<String, Informasjon>>>()
        val recordsForSingleTopic = createInformasjonRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords<String, Informasjon>(records)
    }

    private fun createInformasjonRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<String, Informasjon>> {
        val allRecords = mutableListOf<ConsumerRecord<String, Informasjon>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroInformasjonObjectMother.createInformasjon(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), "key-$i", schemaRecord))
        }
        return allRecords
    }

    fun giveMeANumberOfDoneRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<String, Done> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<String, Done>>>()
        val recordsForSingleTopic = createDoneRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords<String, Done>(records)
    }

    private fun createDoneRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<String, Done>> {
        val allRecords = mutableListOf<ConsumerRecord<String, Done>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroDoneObjectMother.createDone("$i")
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), "key-$i", schemaRecord))
        }
        return allRecords
    }

    fun giveMeANumberOfMeldingRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<String, Melding> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<String, Melding>>>()
        val recordsForSingleTopic = createMeldingRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    private fun createMeldingRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<String, Melding>> {
        val allRecords = mutableListOf<ConsumerRecord<String, Melding>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroMeldingObjectMother.createMelding(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), "key-$i", schemaRecord))
        }
        return allRecords
    }

    fun wrapInConsumerRecords(singleRecord: ConsumerRecord<String, Done>, topicName: String = "dummyTopic"): ConsumerRecords<String, Done> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<String, Done>>>()
        val recordsForSingleTopic = listOf(singleRecord)
        records[TopicPartition(topicName, 1)] = recordsForSingleTopic
        return ConsumerRecords<String, Done>(records)
    }

}
