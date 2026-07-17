description = "Framework-neutral test-run reporting for QualiStrata consumers."

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.3")
}

/*
 * JUnit and TestNG integrations remain isolated from the framework-neutral
 * reporting core. Their dependencies will be introduced as compile-only
 * dependencies together with the corresponding adapters.
 */
