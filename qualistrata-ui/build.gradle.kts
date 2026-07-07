dependencies {
    api(project(":qualistrata-core"))

    implementation("com.microsoft.playwright:playwright:1.60.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.22.0")

    compileOnly("org.testng:testng:7.11.0")
    testImplementation("org.testng:testng:7.11.0")
}
