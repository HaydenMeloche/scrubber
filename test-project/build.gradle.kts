plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":annotation-processor"))
    ksp(project(":annotation-processor"))
}

ksp {
    arg("option1", "value1")
    arg("option2", "value2")
}
