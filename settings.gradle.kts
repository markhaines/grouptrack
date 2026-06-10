import java.util.Properties

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
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    // karoo-ext lives on GitHub Packages, which requires auth even for public
    // packages. Credentials are resolved in this order:
    //   1. gpr.user / gpr.key from local.properties (local dev, also picked up by Android Studio)
    //   2. GITHUB_ACTOR / GITHUB_TOKEN  (GitHub Actions)
    //   3. USERNAME / TOKEN env vars     (manual CLI builds)
    val localProps = Properties().apply {
        val f = File(settingsDir, "local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    val gprUser = localProps.getProperty("gpr.user")
        ?: System.getenv("GITHUB_ACTOR") ?: System.getenv("USERNAME")
    val gprKey = localProps.getProperty("gpr.key")
        ?: System.getenv("GITHUB_TOKEN") ?: System.getenv("TOKEN")

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/hammerheadnav/karoo-ext")
            credentials {
                username = gprUser
                password = gprKey
            }
        }
    }
}

rootProject.name = "GroupTrack"
include(":app")
