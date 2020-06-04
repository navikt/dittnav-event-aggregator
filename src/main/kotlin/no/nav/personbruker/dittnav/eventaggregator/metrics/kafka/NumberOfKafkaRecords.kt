package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

data class NumberOfKafkaRecords(val beskjed: Long = 0,
                                val innboks: Long = 0,
                                val oppgaver: Long = 0,
                                val done: Long = 0) {

    val totalt = beskjed + innboks + oppgaver + done

    override fun toString(): String {
        return """
            Kafka       "På topic"    
            Beskjed     $beskjed
            Innboks     $innboks
            Oppgave     $oppgaver
            Done        $done
            Totalt      $totalt
        """.trimIndent()
    }

}
