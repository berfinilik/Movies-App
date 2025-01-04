buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // Google Services Plugin
        classpath("com.google.gms:google-services:4.4.1")
        // Navigation Safe Args Plugin
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.2")
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}