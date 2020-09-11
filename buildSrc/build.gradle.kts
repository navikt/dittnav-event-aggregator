plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

val dittNavDependenciesVersion = "2020.09.02-13.10-5d51a853214e"

dependencies {
    implementation("com.github.navikt:dittnav-dependencies:$dittNavDependenciesVersion")
}
