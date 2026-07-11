plugins {
    alias(libs.plugins.kotlearn.dataModule)
}

kotlin {

    sourceSets {

        commonMain.dependencies {
            implementation(libs.datastore.preferences.core)
        }

    }
}