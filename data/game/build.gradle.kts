plugins {
    alias(libs.plugins.kotlearn.dataModule)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {

    sourceSets {

        commonMain.dependencies {
            implementation(projects.data.core)

            implementation(libs.bundles.kotlin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

    }
}
