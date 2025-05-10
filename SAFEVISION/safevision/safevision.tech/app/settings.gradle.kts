pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/XRPLF/xrpl4j")
            credentials {
                username = providers.gradleProperty("GITHUB_USERNAME").orNull
                password = providers.gradleProperty("GITHUB_TOKEN").orNull
            }
        }
        maven { url = uri("https://maven.webrtc.org") }
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/XRPLF/xrpl4j")
            credentials {
                username = providers.gradleProperty("GITHUB_USERNAME").orNull
                password = providers.gradleProperty("GITHUB_TOKEN").orNull
            }
        }
        maven { url = uri("https://maven.webrtc.org") }
        maven { url = uri("https://jitpack.io") }
    }
}

// Désactivation de "Configuration on Demand"
gradle.settingsEvaluated {
    gradle.startParameter.isConfigureOnDemand = false
}

// Nom du projet
rootProject.name = "SafeVision.tech"

// Inclusion des modules
include(":app")
include(":app_secondary")
