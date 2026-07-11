plugins {
    alias(libs.plugins.kotlearn.dataModule)
}

kotlin {

    sourceSets {

        commonMain.dependencies {
            implementation(projects.data.core)

            implementation(libs.bundles.kotlin)
        }

    }
}
