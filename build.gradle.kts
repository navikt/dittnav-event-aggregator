import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version("1.3.31")
    kotlin("plugin.allopen").version("1.3.31")

    // Apply the application plugin to add support for building a CLI application.
    application
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    maven("http://packages.confluent.io/maven")
    mavenLocal()
}

val prometheusVersion = "0.6.0"
val ktorVersion = "1.1.3"
val junitVersion = "5.4.1"
val kafkaVersion = "2.2.0"
val confluentVersion = "5.2.0"
val dittNavSkjemaVersion = "1.0-SNAPSHOT"
val logstashVersion = 5.2
val logbackVersion = "1.2.3"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("ch.qos.logback:logback-classic:$logbackVersion")
    compile("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    compile("io.prometheus:simpleclient_common:$prometheusVersion")
    compile("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("org.apache.kafka:kafka-clients:$kafkaVersion")
    compile("io.confluent:kafka-avro-serializer:$confluentVersion")
    compile("no.nav.personbruker.dittnav:skjema:$dittNavSkjemaVersion")
    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile("org.assertj:assertj-core:3.12.1")
    testCompile(kotlin("test-junit5"))
    testImplementation("no.nav:kafka-embedded-env:2.1.1")
    testImplementation("org.apache.kafka:kafka_2.12:$kafkaVersion")
    testImplementation("org.apache.kafka:kafka-streams:$kafkaVersion")
    testImplementation("org.apache.kafka:kafka-streams:$kafkaVersion")
    testImplementation("io.confluent:kafka-schema-registry:$confluentVersion")
}

application {
    mainClassName = "no.nav.personbruker.AppKt"
}
