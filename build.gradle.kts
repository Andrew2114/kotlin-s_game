plugins {
kotlin("jvm") version "1.9.22"
application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
mavenCantral()
}

dependencies {
implemention("org.jetbrains.kotlin:kotlin-stdlib")
testImplementation(kotlin("test"))
}

application {
mainClass.set("Main.kt")
}

tasks.test {
useJUnitPlatform()
}

tasks.jar {
manifest {
attributes["Main-Class"] = "MainKt"
}
from(configurations.runtimeClasspath.get().map { if (it.isDirictory) it else zipTree(it) })
duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
