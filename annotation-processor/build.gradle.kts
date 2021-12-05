val kspVersion: String by project

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "dev.meloche"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:1.10.2")
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}



publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.meloche"
            artifactId = "scrubber"
            version = "1.0"

            from(components["kotlin"])
        }
    }
}


