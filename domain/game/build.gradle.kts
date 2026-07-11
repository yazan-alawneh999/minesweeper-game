plugins {
    alias(libs.plugins.kotlearn.domainModule)
}

kotlin {

    sourceSets {

        commonMain.dependencies {
            implementation(projects.data.game)
            implementation(projects.data.settings)
        }

    }

}

