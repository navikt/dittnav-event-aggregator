plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val dittNavDependenciesVersion = "2022.01.13-08.31-581d3f963a95"

dependencies {
    implementation("com.github.navikt:dittnav-dependencies:$dittNavDependenciesVersion")
}
