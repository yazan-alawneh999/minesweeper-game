plugins {
    alias(libs.plugins.kotlearn.domainModule)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {

    sourceSets {

        commonMain.dependencies {
            implementation(projects.data.game)
            implementation(projects.data.settings)

            implementation(libs.coroutines.core)
            implementation(libs.kotlinx.serialization)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

    }

}

