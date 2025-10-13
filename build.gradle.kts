// build.gradle.kts

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    // Apply the JavaFX plugin to simplify JavaFX development.
    id("org.openjfx.javafxplugin") version "0.1.0"
}

// Define the group and version for your project.
group = "com.eureka"
version = "0.1.0"

// Specify the repository to download dependencies from. Maven Central is the standard.
repositories {
    mavenCentral()
}

// Define the project dependencies.
dependencies {
    // JavaFX modules needed for the application.
    implementation("org.openjfx:javafx-controls:21.0.3")
    implementation("org.openjfx:javafx-graphics:21.0.3")

    // Google's GSON library for JSON serialization and deserialization.
    implementation("com.google.code.gson:gson:2.10.1")


    // Apache Lucene libraries for text searching and indexing.
    implementation("org.apache.lucene:lucene-core:9.9.1")
    implementation("org.apache.lucene:lucene-analyzers-common:8.11.3")
    implementation("org.apache.lucene:lucene-queryparser:9.9.1")
}

// Configure the JavaFX plugin.
javafx {
    version = "21.0.3"
    // List the JavaFX modules that your application uses.
    modules = listOf("javafx.controls", "javafx.graphics")
}

// Configure the application plugin.
application {
    // Define the main class for the application.
    mainClass.set("com.eureka.EurekaApp")
}

// Configure the Java compiler options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
// This tells Gradle to process resource files (like .properties) as UTF-8
tasks.withType<ProcessResources> {
    filteringCharset = "UTF-8"
}