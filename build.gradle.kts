import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version(Kotlin.version)
    kotlin("plugin.allopen").version(Kotlin.version)

    id("org.flywaydb.flyway") version (Flyway.version)

    // Apply the application plugin to add support for building a CLI application.
    application
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    jcenter()
    mavenCentral()
    maven("http://packages.confluent.io/maven")
    mavenLocal()
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
configurations["intTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    implementation(NAV.vaultJdbc)
    implementation(Hikari.cp)
    implementation(Postgresql.postgresql)
    implementation(Flyway.core)
    implementation(Logback.classic)
    implementation(Logstash.logbackEncoder)
    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.logback)
    implementation(Ktor.serverNetty)
    implementation(Ktor.htmlBuilder)
    implementation(Kafka.Apache.clients)
    implementation(Kafka.Confluent.avroSerializer)
    implementation(Brukernotifikasjon.schemas)
    implementation(Influxdb.java)

    testImplementation(Junit.api)
    testImplementation(Junit.engine)
    testImplementation(NAV.kafkaEmbedded)
    testImplementation(Kafka.Apache.kafka_2_12)
    testImplementation(Kafka.Apache.streams)
    testImplementation(Kafka.Confluent.schemaRegistry)
    testImplementation(H2Database.h2)
    testImplementation(Kluent.kluent)
    testImplementation(Mockk.mockk)

    intTestImplementation(Junit.engine)
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks {
    withType<Jar> {
        manifest {
            attributes["Main-Class"] = application.mainClassName
        }
        from(configurations.runtime.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }

    register("runServer", JavaExec::class) {
        environment("KAFKA_BOOTSTRAP_SERVERS", "localhost:29092")
        environment("KAFKA_SCHEMAREGISTRY_SERVERS", "http://localhost:8081")
        environment("SERVICEUSER_USERNAME", "username")
        environment("SERVICEUSER_PASSWORD", "password")
        environment("GROUP_ID", "dittnav_events")
        environment("DB_HOST", "localhost:5432")
        environment("DB_NAME", "dittnav-event-cache-preprod")
        environment("DB_PASSWORD", "testpassword")
        environment("DB_MOUNT_PATH", "notUsedOnLocalhost")
        environment("NAIS_CLUSTER_NAME", "dev-sbs")
        environment("NAIS_NAMESPACE", "q1")
        environment("SENSU_HOST", "stub")
        environment("SENSU_PORT", "")
        environment("PRODUCER_ALIASES", "")

        main = application.mainClassName
        classpath = sourceSets["main"].runtimeClasspath
    }
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")
}

tasks.check { dependsOn(integrationTest) }
