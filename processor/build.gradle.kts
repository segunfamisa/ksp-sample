plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
}
repositories {
    google()
    mavenCentral()
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.21-1.0.0-beta05")
    testImplementation("junit:junit:4.+")
}
ksp {
    arg("option1", "value1")
    arg("option2", "value2")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}
