import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version(Kotlin.version)
    kotlin("plugin.allopen").version(Kotlin.version)

    id(Flyway.pluginId) version (Flyway.version)
    id(Shadow.pluginId) version (Shadow.version)

    // Apply the application plugin to add support for building a CLI application.
    application
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven")
    maven ( "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven" )
    mavenLocal()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.navikt:doknotifikasjon-schemas:1.2022.06.07-10.21-210529ac5c88")
    implementation(DittNAV.Common.utils)
    implementation(DittNAV.Common.influxdb)
    implementation(Flyway.core)
    implementation(Hikari.cp)
    implementation(Influxdb.java)
    implementation(Kafka.Apache.clients)
    implementation(Kafka.Confluent.avroSerializer)
    implementation(Logback.classic)
    implementation(Logstash.logbackEncoder)
    implementation(NAV.vaultJdbc)
    implementation(Postgresql.postgresql)
    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.logback)
    implementation(RapidsAndRivers)
    implementation(Ktor2.Server.core)
    implementation(Ktor2.Server.netty)
    implementation(Ktor2.Server.contentNegotiation)
    implementation(Ktor2.Server.auth)
    implementation(Ktor2.Server.authJwt)
    implementation(Ktor2.jackson)
    implementation(Ktor2.TmsTokenSupport.azureExchange)
    implementation(Ktor2.TmsTokenSupport.azureValidation)
    implementation(Ktor2.TmsTokenSupport.tokenXValidation)
    implementation(Ktor2.TmsTokenSupport.authenticationInstaller)

    testImplementation(Junit.api)
    testImplementation(Junit.engine)
    testImplementation(Kafka.Apache.kafka_2_12)
    testImplementation(Kafka.Apache.streams)
    testImplementation(Kafka.Confluent.schemaRegistry)
    testImplementation(Mockk.mockk)
    testImplementation(NAV.kafkaEmbedded)
    testImplementation(TestContainers.postgresql)
    testImplementation(Kotest.runnerJunit5)
    testImplementation(Kotest.assertionsCore)
    testImplementation(Ktor2.Test.serverTestHost)
    testImplementation(Ktor2.TmsTokenSupport.authenticationInstallerMock)
    testImplementation(Ktor2.TmsTokenSupport.tokenXValidationMock)
    testImplementation(Ktor2.TmsTokenSupport.azureValidationMock)
}

application {
    mainClass.set("no.nav.personbruker.dittnav.eventaggregator.config.ApplicationKt")

}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }

    register("runServer", JavaExec::class) {
        println("Setting default environment variables for running with DittNAV docker-compose")
        DockerComposeDefaults.environomentVariables.forEach { (name, value) ->
            println("Setting the environment variable $name")
            environment(name, value)
        }

        main = application.mainClass.get()
        classpath = sourceSets["main"].runtimeClasspath
    }
}

// TODO: Fjern følgende work around i ny versjon av Shadow-pluginet:
// Skal være løst i denne: https://github.com/johnrengelman/shadow/pull/612
project.setProperty("mainClassName", application.mainClass.get())
apply(plugin = Shadow.pluginId)
