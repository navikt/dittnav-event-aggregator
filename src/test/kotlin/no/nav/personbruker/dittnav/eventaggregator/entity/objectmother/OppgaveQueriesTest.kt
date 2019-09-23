package no.nav.personbruker.dittnav.eventaggregator.database.entity

import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.eventaggregator.database.H2Database
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.sql.SQLException

object OppgaveQueriesTest : Spek({
    val database = H2Database()

    describe("Returnerer cachede Oppgave-eventer") {
        val aktorId1 = "12345"
        val aktorId2 = "54321"

        val oppgave1 = OppgaveObjectMother.createOppgave(1, aktorId1)
        val oppgave2 = OppgaveObjectMother.createOppgave(2, aktorId2)
        val oppgave3 = OppgaveObjectMother.createOppgave(3, aktorId1)

        before {
            runBlocking {
                database.dbQuery {
                    createOppgave(oppgave1)
                    createOppgave(oppgave2)
                    createOppgave(oppgave3)
                }
            }
        }
        it("Finner alle cachede Oppgave-eventer") {
            runBlocking {
                assertThat(database.dbQuery { getAllOppgave() })
                        .hasSize(3)
                        .containsAll(listOf(oppgave1, oppgave2, oppgave3))
            }
        }
        it("Finner cachede Oppgave-eventer for aktorId") {
            runBlocking {
                assertThat(database.dbQuery { getOppgaveByAktorId(aktorId1) })
                        .hasSize(2)
                        .containsAll(listOf(oppgave1, oppgave3))
                        .doesNotContain(oppgave2)
            }
        }
        it("Gir tom liste dersom Oppgave-event med gitt aktorId ikke finnes") {
            runBlocking {
                assertThat(database.dbQuery { getOppgaveByAktorId("-1") })
                        .hasSize(0)
            }
        }
        it("Finner cachet Oppgave-event for id") {
            runBlocking {
                assertThat(database.dbQuery { getOppgaveById(2) })
                        .isNotNull()
                        .isEqualTo(oppgave2)
            }
        }
        it("Kaster exception dersom Oppgave-event med id ikke finnes") {
            assertThatThrownBy {
                runBlocking {
                    assertThat(database.dbQuery { getOppgaveById(-1) })
                }
            }
                    .isInstanceOf(SQLException::class.java)
                    .hasMessage("Found no rows")
        }


    }
})