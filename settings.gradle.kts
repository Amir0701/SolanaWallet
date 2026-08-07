pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SolanaCryproWallet"
include(":app")
include(":core:crypto")
include(":core:network")
include(":core:wallet")
include(":feature:auth:api")
include(":feature:auth:impl")
include(":feature:send:api")
include(":feature:send:impl")
include(":feature:receive:api")
include(":feature:receive:impl")
