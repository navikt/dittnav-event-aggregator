plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

val dittNavDependenciesVersions = "2020.08.25-16.08-b375af598ee6"

dependencies {
    implementation("com.github.navikt:dittnav-dependencies:$dittNavDependenciesVersions")
}
