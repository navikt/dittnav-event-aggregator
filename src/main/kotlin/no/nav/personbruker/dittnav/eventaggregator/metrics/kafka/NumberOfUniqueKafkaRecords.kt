package no.nav.personbruker.dittnav.eventaggregator.metrics.kafka

data class NumberOfUniqueKafkaRecords(
        val beskjed: Long = 0,
        val beskjedDuplicates: Long = 0,
        val innboks: Long = 0,
        val innboksDuplicates: Long = 0,
        val oppgaver: Long = 0,
        val oppgaverDuplicates: Long = 0,
        val done: Long = 0,
        val doneDuplicates: Long = 0
) {

    val totalt = beskjed + innboks + oppgaver + done
    val totaltDuplicates = beskjedDuplicates + innboksDuplicates + oppgaverDuplicates + doneDuplicates
    val beskjedTotal = beskjed + beskjedDuplicates
    val innboksTotal = innboks + innboksDuplicates
    val oppgaveTotal = oppgaver + oppgaverDuplicates
    val doneTotal = done + doneDuplicates
    val eventsInTotal = beskjedTotal + innboksTotal + oppgaveTotal + doneTotal

    override fun toString(): String {
        return """
            Kafka       Unique      Duplicates              "På topic"
            Beskjed     $beskjed    $beskjedDuplicates      $beskjedTotal
            Innboks     $innboks    $innboksDuplicates      $innboksTotal
            Oppgave     $oppgaver   $oppgaverDuplicates     $oppgaveTotal
            Done        $done       $doneDuplicates         $doneTotal
            Totalt      $totalt     $totaltDuplicates       $eventsInTotal
        """.trimIndent()
    }

}
