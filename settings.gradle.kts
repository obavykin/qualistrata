pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
    }
}

rootProject.name = "qualistrata"

include(
    "qualistrata-core",
    "qualistrata-ui",
    "qualistrata-api",
    "qualistrata-db",
    "qualistrata-unix",
    "qualistrata-perf"
)
