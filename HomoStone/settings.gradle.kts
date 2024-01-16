pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        val token = "f3c6485ad8d7ed9372b099af41fe9e93dfe7be17576c7c34"
        maven("https://packagecloud.io/priv/${token}/stone/pos-android/maven2")
    }
}

rootProject.name = "HomoStone"
include(":app")
 