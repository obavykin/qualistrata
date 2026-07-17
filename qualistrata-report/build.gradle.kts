import org.gradle.api.tasks.testing.Test

description = "Framework-neutral test-run reporting for QualiStrata consumers."

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.3")

    compileOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    compileOnly("org.junit.platform:junit-platform-engine:1.11.4")

    testImplementation("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation("org.junit.platform:junit-platform-engine:1.11.4")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    systemProperty(
        "junit.platform.listeners.autodetection.enabled",
        "true"
    )
    systemProperty(
        "qualistrata.report.enabled",
        System.getProperty("qualistrata.report.enabled", "true")
    )
    systemProperty(
        "qualistrata.report.source",
        System.getProperty("qualistrata.report.source", name)
    )

    val configuredRunId = System.getProperty("qualistrata.report.runId")
    if (!configuredRunId.isNullOrBlank()) {
        systemProperty("qualistrata.report.runId", configuredRunId)
    }

    val defaultOutput = layout.buildDirectory
        .file("reports/qualistrata/$name/test-run.json")
        .get()
        .asFile
        .absolutePath

    systemProperty(
        "qualistrata.report.output",
        System.getProperty("qualistrata.report.output", defaultOutput)
    )
}

/*
 * JUnit integration is isolated behind compile-only JUnit Platform dependencies.
 * TestNG remains absent until its adapter is introduced.
 */
