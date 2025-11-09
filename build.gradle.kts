// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

val libs = the<LibrariesForLibs>()

subprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.squareup" && requested.name == "javapoet") {
                useVersion(libs.versions.javapoet.get())
                because("Ensure canonicalName() is available by forcing JavaPoet 1.13.0")
            }
        }
    }
}