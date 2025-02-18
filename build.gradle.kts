plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.8.7" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}