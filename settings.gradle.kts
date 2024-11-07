@file:Suppress("UnstableApiUsage")

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
        maven("https://api.xposed.info/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://api.xposed.info/")
    }
}

rootProject.name = "HapticFeedBack"
include(":app")
include(":UniversalLoader:XposedLoader")
include(":UniversalLoader:XposedApi:modern:api")
include(":UniversalLoader:XposedApi:modern:service")
include(":UniversalLoader:XposedCompat")
include(":UniversalLoader:hiddenapi-stub")