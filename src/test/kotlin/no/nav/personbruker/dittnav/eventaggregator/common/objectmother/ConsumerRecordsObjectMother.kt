package no.nav.personbruker.dittnav.eventaggregator.common.objectmother

import no.nav.brukernotifikasjon.schemas.internal.*
import no.nav.personbruker.dittnav.eventaggregator.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.AvroInnboksObjectMother
import no.nav.personbruker.dittnav.eventaggregator.nokkel.AvroNokkelInternObjectMother
import no.nav.personbruker.dittnav.eventaggregator.oppgave.AvroOppgaveObjectMother
import no.nav.personbruker.dittnav.eventaggregator.statusoppdatering.AvroStatusoppdateringObjectMother
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition

object ConsumerRecordsObjectMother {

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
            val nokkel = AvroNokkelInternObjectMother.createNokkelWithEventId(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
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
            val nokkel = AvroNokkelInternObjectMother.createNokkelWithEventId(i)
            allRecords.add(ConsumerRecord(topicName, i, i.toLong(), nokkel, schemaRecord))
        }
        return allRecords
    }


    private fun createOppgaveRecords(topicName: String, totalNumber: Int): List<ConsumerRecord<NokkelIntern, OppgaveIntern>> {
        val allRecords = mutableListOf<ConsumerRecord<NokkelIntern, OppgaveIntern>>()
        for (i in 0 until totalNumber) {
            val schemaRecord = AvroOppgaveObjectMother.createOppgave(i)
            val nokkel = AvroNokkelInternObjectMother.createNokkelWithEventId(i)
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
            val schemaRecord = AvroStatusoppdateringObjectMother.createStatusoppdatering()
            val nokkel = AvroNokkelInternObjectMother.createNokkelWithEventId(i)
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
