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
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}