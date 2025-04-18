plugins {
    id("com.gradle.develocity") version "4.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        uploadInBackground = !System.getenv("CI").toBoolean()
    }
}

rootProject.name = "learning-scafi-alchemist"
