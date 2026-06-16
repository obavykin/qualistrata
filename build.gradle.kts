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
}

/*
 * Builds a portable local library distribution for manual use in consuming
 * projects that cannot depend on mavenLocal() or a remote Maven repository.
 *
 * The task is registered inside the qualistrata-unix project because Gradle 9
 * does not allow unsafe cross-project resolution of another project's
 * runtimeClasspath configuration from the root project.
 *
 * The distribution contains:
 * - qualistrata-core jar;
 * - qualistrata-unix jar;
 * - runtime third-party dependencies required by qualistrata-unix, for example JSch.
 */
project(":qualistrata-unix") {
    tasks.register<Copy>("qualistrataUnixLocalDistribution") {
        description = "Builds a portable local distribution for qualistrata-unix."
        group = "distribution"

        dependsOn(
            ":qualistrata-core:jar",
            ":qualistrata-unix:jar"
        )

        into(rootProject.layout.buildDirectory.dir("distributions/qualistrata-unix-libs"))

        from(project(":qualistrata-core").tasks.named("jar"))
        from(tasks.named("jar"))

        /*
         * Copy third-party runtime dependencies owned by qualistrata-unix.
         * Project jars are copied explicitly above to keep the distribution
         * predictable and readable.
         */
        from(configurations.named("runtimeClasspath")) {
            exclude("qualistrata-core-*.jar")
            exclude("qualistrata-unix-*.jar")
        }
    }
}