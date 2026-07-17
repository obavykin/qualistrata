import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

plugins {
    `java-library`
}

val publishableProjectNames = setOf(
    "qualistrata-core",
    "qualistrata-db",
    "qualistrata-unix",
    "qualistrata-ui",
    "qualistrata-perf",
    "qualistrata-report"
)

val moduleDescriptions = mapOf(
    "qualistrata-core" to "Core configuration, target resolution, resource model, and common primitives for QualiStrata automation projects.",
    "qualistrata-db" to "Database automation support for QualiStrata, including JDBC client abstractions, diagnostics, and report dataset primitives.",
    "qualistrata-unix" to "Unix and SSH automation support for QualiStrata, including command execution, session lifecycle, and environment checks.",
    "qualistrata-ui" to "UI automation support for QualiStrata, including driver-independent page abstractions, locators, Playwright integration, and test framework adapters.",
    "qualistrata-perf" to "Performance and telemetry support for QualiStrata, including lightweight timing, local telemetry artifacts, and safe telemetry publishing abstractions.",
    "qualistrata-report" to "Framework-neutral test execution reporting for QualiStrata consumers, including canonical JSON artifacts and JUnit Platform integration."
)

val qualistrataVersion = providers.gradleProperty("qualistrata.version")
    .orElse("0.1.0-SNAPSHOT")
    .get()

allprojects {
    group = "com.bavykin.qualistrata"
    version = qualistrataVersion
}

subprojects {
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }

        withSourcesJar()
        withJavadocJar()
    }

    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:5.11.4"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")

        // Required by Gradle 9+ to launch tests through the JUnit Platform.
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    /*
     * Registers a dedicated integration-test source set for tests that may
     * require external infrastructure such as SSH hosts, databases, browsers,
     * test environments, or other runtime services.
     *
     * Integration tests are intentionally not attached to the regular 'check'
     * or 'build' lifecycle. This keeps the default build fast, deterministic,
     * and suitable for CI environments that do not provide external test
     * infrastructure.
     */
    val integrationTestSourceSet = sourceSets.create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")

        /*
         * Integration tests should be able to use production classes and the
         * same test dependencies as unit tests.
         */
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }

    configurations[integrationTestSourceSet.implementationConfigurationName]
        .extendsFrom(configurations["testImplementation"])

    configurations[integrationTestSourceSet.runtimeOnlyConfigurationName]
        .extendsFrom(configurations["testRuntimeOnly"])

    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath = integrationTestSourceSet.runtimeClasspath

        shouldRunAfter(tasks.named("test"))

        useJUnitPlatform()

        /*
         * Forwards only QualiStrata integration-test system properties from the
         * Gradle process to the forked test JVM.
         *
         * This keeps infrastructure-dependent integration tests configurable from
         * the command line, for example:
         *
         * -Dqualistrata.integration.ssh.host=...
         * -Dqualistrata.integration.ssh.username=...
         */
        systemProperties(
            System.getProperties()
                .stringPropertyNames()
                .filter { it.startsWith("qualistrata.integration.") }
                .associateWith { System.getProperty(it) }
        )
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    /*
     * Only real, consumer-validated framework modules are published.
     *
     * qualistrata-api is intentionally excluded from 0.1.0-alpha.1 because it is
     * currently only a placeholder module. Publishing placeholders creates noisy
     * Maven Central coordinates and makes the public API surface look larger than
     * it actually is.
     */
    if (name in publishableProjectNames) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")

        extensions.configure<JavaPluginExtension>("java") {
            withSourcesJar()
            withJavadocJar()
        }

        extensions.configure<PublishingExtension>("publishing") {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    pom {
                        name.set("QualiStrata ${project.name.removePrefix("qualistrata-").replaceFirstChar { it.uppercase() }}")
                        description.set(moduleDescriptions.getValue(project.name))
                        url.set("https://github.com/obavykin/qualistrata")

                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }

                        developers {
                            developer {
                                id.set("obavykin")
                                name.set("Oleksandr Bavykin")
                            }
                        }

                        scm {
                            connection.set("scm:git:https://github.com/obavykin/qualistrata.git")
                            developerConnection.set("scm:git:ssh://git@github.com:obavykin/qualistrata.git")
                            url.set("https://github.com/obavykin/qualistrata")
                        }
                    }
                }
            }

            repositories {
                maven {
                    name = "CentralStaging"
                    url = rootProject.layout.buildDirectory.dir("central-staging").get().asFile.toURI()
                }
                maven {
                    name = "QualiStrataLocal"

                    url = uri(
                        providers.gradleProperty("qualistrata.localRepo")
                            .orElse(
                                rootProject.layout.buildDirectory
                                    .dir("local-maven-repo")
                                    .map { it.asFile.absolutePath }
                            )
                            .get()
                    )
                }
            }
        }

        extensions.configure<SigningExtension>("signing") {
            val releasePublishRequested = gradle.startParameter.taskNames.any { taskName ->
                taskName.contains("publish", ignoreCase = true) &&
                        !taskName.contains("MavenLocal", ignoreCase = true) &&
                        !taskName.contains("QualiStrataLocal", ignoreCase = true)
            }
            val releaseSigningRequired = releasePublishRequested && !version.toString().endsWith("SNAPSHOT")

            /*
             * Local Maven publication must stay simple for consumer validation.
             * Remote release publication must be signed.
             *
             * Supported signing modes:
             * - signingMode=gpg      -> use local GPG installation/keyring
             * - default/in-memory    -> use signingKey or signingKeyFile
             */
            isRequired = releaseSigningRequired

            if (releaseSigningRequired) {
                val signingMode = providers.gradleProperty("signingMode")
                    .orElse(providers.environmentVariable("SIGNING_MODE"))
                    .orElse("inMemory")
                    .get()

                if (signingMode.equals("gpg", ignoreCase = true)) {
                    useGpgCmd()
                } else {
                    val inlineSigningKey = providers.gradleProperty("signingKey")
                        .orElse(providers.environmentVariable("SIGNING_KEY"))
                        .orNull
                    val signingKeyFile = providers.gradleProperty("signingKeyFile")
                        .orElse(providers.environmentVariable("SIGNING_KEY_FILE"))
                        .orNull
                    val signingPassword = providers.gradleProperty("signingPassword")
                        .orElse(providers.environmentVariable("SIGNING_PASSWORD"))
                        .orNull

                    val signingKeyFromFile = signingKeyFile
                        ?.takeIf { it.isNotBlank() }
                        ?.let { file(it) }
                        ?.takeIf { it.isFile }
                        ?.readText()

                    val signingKey = inlineSigningKey
                        ?.takeIf { it.isNotBlank() }
                        ?: signingKeyFromFile

                    val normalizedSigningKey = signingKey?.replace("\\n", "\n")

                    check(!normalizedSigningKey.isNullOrBlank()) {
                        "Release publication requires signingKey or signingKeyFile."
                    }
                    check(!signingPassword.isNullOrBlank()) {
                        "Release publication requires signingPassword."
                    }

                    useInMemoryPgpKeys(normalizedSigningKey, signingPassword)
                }

                sign(extensions.getByType(PublishingExtension::class).publications)
            }
        }
    }
}
