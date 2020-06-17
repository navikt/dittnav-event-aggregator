package no.nav.personbruker.dittnav.eventaggregator.metrics.db.count

data class NumberOfCachedRecords(val beskjedAktive: Long = 0,
                                 val beskjedInaktive: Long = 0,
                                 val innboksAktive: Long = 0,
                                 val innboksInaktive: Long = 0,
                                 val oppgaveAktive: Long = 0,
                                 val oppgaveInaktive: Long = 0,
                                 val doneVentetabell: Long = 0) {

    val beskjedTotal = beskjedAktive + beskjedInaktive
    val innboksTotal = innboksAktive + innboksInaktive
    val oppgaveTotal = oppgaveAktive + oppgaveInaktive
    val doneTotal = doneVentetabell + beskjedInaktive + innboksInaktive + oppgaveInaktive

    override fun toString(): String {
        return """
            Cache       Totalt      Aktive      Inaktive
            Beskjed     $beskjedTotal       $beskjedAktive      $beskjedInaktive        
            Innboks     $innboksTotal       $innboksAktive      $innboksInaktive
            Oppgave     $oppgaveTotal       $oppgaveAktive      $oppgaveInaktive
            Done        $doneTotal      $doneVentetabell
        """.trimIndent()
    }

}
