plugins {
    alias(libs.plugins.kotlearn.domainModule)
}

kotlin {

    sourceSets {

        commonMain.dependencies {
            implementation(projects.data.settings)

            implementation(libs.bundles.kotlin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

    }
}
