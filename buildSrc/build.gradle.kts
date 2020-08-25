plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.navikt:dittnav-dependencies:2020.08.25-13.36-700bcd6a0812")
}
