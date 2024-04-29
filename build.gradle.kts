buildscript {

    repositories {
        google()
        mavenCentral()
        maven(url = "http://maven.google.com/").isAllowInsecureProtocol=true
    }
    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")

        // Hilt
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.51")
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    // Hilt
    id("com.google.dagger.hilt.android") version "2.44" apply false

}