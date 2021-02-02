import de.fayard.refreshVersions.bootstrapRefreshVersions
import org.danilopianini.VersionAliases.justAdditionalAliases

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("de.fayard.refreshVersions:refreshVersions:0.9.7")
        classpath("org.danilopianini:refreshversions-aliases:+")
    }
}

//bootstrapRefreshVersions()
bootstrapRefreshVersions(justAdditionalAliases)

rootProject.name = "scafi-alchemist-skeleton"
