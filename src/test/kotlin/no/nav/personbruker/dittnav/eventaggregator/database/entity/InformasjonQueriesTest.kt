package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import no.nav.personbruker.dittnav.eventaggregator.util.InformasjonObjectMother
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.sql.SQLException

object InformasjonQueriesTest : Spek({

    val database = H2Database()

    describe("Returnerer cachede Informasjons-eventer") {
        val informasjon1 = InformasjonObjectMother.createInformasjon(1)
        val informasjon2 = InformasjonObjectMother.createInformasjon(2)
        val informasjon3 = InformasjonObjectMother.createInformasjon(3)

        before {
            runBlocking {
                database.dbQuery {
                    createInformasjon(informasjon1)
                    createInformasjon(informasjon2)
                    createInformasjon(informasjon3)
                }
            }
        }
        it("Finner alle cachede Informasjons-eventer") {
            runBlocking {
                assertThat(database.dbQuery { getAllInformasjon() })
                        .hasSize(3)
                        .containsAll(listOf(informasjon1, informasjon2, informasjon3))
            }
        }
        it("Finner cachet Informasjon-event med ID") {
            runBlocking {
                assertThat(database.dbQuery { getInformasjonById(2) })
                        .isEqualTo(informasjon2)
            }
        }
        it("Kaster Exception hvis Informasjon-event med ID ikke finnes") {
            assertThatThrownBy {
                runBlocking {
                    database.dbQuery { getInformasjonById(999) }
                }
            }
                    .isInstanceOf(SQLException::class.java)
                    .hasMessage("Found no rows")
        }
        it("Finner cachede Informasjons-eventer for aktørID") {
            runBlocking {
                assertThat(database.dbQuery { getInformasjonByAktorid("12345")})
                        .hasSize(3)
                        .containsAll(listOf(informasjon1, informasjon2, informasjon3))
            }
        }
        it("Returnerer tom liste hvis Informasjons-eventer for aktørID ikke finnes") {
            runBlocking {
                assertThat(database.dbQuery { getInformasjonByAktorid("-1")})
                        .isEmpty()
            }
        }
    }
})