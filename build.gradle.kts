import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-library`
}

allprojects {
    group = "com.bavykin.qualistrata"
    version = "0.1.0-SNAPSHOT"
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

    plugins.withId("java-library") {
        apply(plugin = "maven-publish")

        extensions.configure<JavaPluginExtension>("java") {
            withSourcesJar()
            withJavadocJar()
        }

        extensions.configure<PublishingExtension>("publishing") {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    pom {
                        name.set(project.name)
                        description.set("QualiStrata test automation accelerator module: ${project.name}")
                        url.set("https://github.com/obavykin/qualistrata")

                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0")
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
    }
}