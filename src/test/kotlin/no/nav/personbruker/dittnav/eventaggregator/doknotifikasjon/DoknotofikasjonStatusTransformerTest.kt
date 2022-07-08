package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


internal class DoknotofikasjonStatusTransformerTest {
    @Test
    fun `should transform to internal dto`() {
        val external = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(
            bestillingsId = "testId",
            bestiller = "bestiller",
            status = "status",
            melding = "some melding",
            distribusjonsId = 123
        )

        val internal = DoknotofikasjonStatusTransformer.toInternal(external)

        external.getBestillerId() shouldBe internal.bestillerAppnavn
        external.getBestillingsId() shouldBe internal.eventId
        external.getStatus() shouldBe internal.status
        external.getMelding() shouldBe internal.melding
        external.getDistribusjonId() shouldBe internal.distribusjonsId
        internal.kanaler.isEmpty() shouldBe true
    }

    @Test
    fun `should parse kanal where applicable`() {
        val external = DoknotifikasjonStatusObjectMother.createDoknotifikasjonStatus(
            bestillingsId = "test",
            melding = "notifikasjon sendt via mail"
        )

        val internal = DoknotofikasjonStatusTransformer.toInternal(external)

        internal.kanaler shouldContain "MAIL"
    }
}
