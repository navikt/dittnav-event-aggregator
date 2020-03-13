package no.nav.personbruker.dittnav.eventaggregator.common.exceptions.objectmother

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.done.schema.AvroDoneObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.AvroInnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.createNokkel
import no.nav.personbruker.dittnav.eventaggregator.oppgave.AvroOppgaveObjectMother
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition

object ConsumerRecordsObjectMother {

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

    fun giveMeANumberOfDoneRecords(numberOfRecords: Int, topicName: String): ConsumerRecords<Nokkel, Done> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<Nokkel, Done>>>()
        val recordsForSingleTopic = createDoneRecords(topicName, numberOfRecords)
        records[TopicPartition(topicName, numberOfRecords)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

    private fun createDoneRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<Nokkel, Done>> {
        val allRecords = mutableListOf<ConsumerRecord<Nokkel, Done>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroDoneObjectMother.createDone("$i")
            val nokkel = createNokkel(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
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

    fun wrapInConsumerRecords(singleRecord: ConsumerRecord<Nokkel, Done>, topicName: String = "dummyTopic"): ConsumerRecords<Nokkel, Done> {
        val records = mutableMapOf<TopicPartition, List<ConsumerRecord<Nokkel, Done>>>()
        val recordsForSingleTopic = listOf(singleRecord)
        records[TopicPartition(topicName, 1)] = recordsForSingleTopic
        return ConsumerRecords(records)
    }

}
