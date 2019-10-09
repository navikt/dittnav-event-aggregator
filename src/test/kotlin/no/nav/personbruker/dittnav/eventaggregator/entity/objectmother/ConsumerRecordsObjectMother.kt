package no.nav.personbruker.dittnav.eventaggregator.entity.objectmother

import no.nav.brukernotifikasjon.schemas.Informasjon
import no.nav.personbruker.dittnav.eventaggregator.schema.objectmother.InformasjonObjectMother
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition

object ConsumerRecordsObjectMother {

    fun giveMeANumberOfInformationRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<String, Informasjon> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<String, Informasjon>>>()
        val recordsForSingleTopic = createRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords<String, Informasjon>(records)
    }

    private fun createRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<String, Informasjon>> {
        val allRecords = mutableListOf<ConsumerRecord<String, Informasjon>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = InformasjonObjectMother.createInformasjon(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), "key-$i", schemaRecord))
        }
        return allRecords
    }

}
