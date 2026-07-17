description = "Framework-neutral test-run reporting for QualiStrata consumers."

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.3")

    compileOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    compileOnly("org.junit.platform:junit-platform-engine:1.11.4")

    testImplementation("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation("org.junit.platform:junit-platform-engine:1.11.4")
}

/*
 * JUnit integration is isolated behind compile-only JUnit Platform dependencies.
 * TestNG remains absent until its adapter is introduced.
 */
