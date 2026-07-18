plugins {
    alias(libs.plugins.kotlearn.featureModule)
}

kotlin {

    sourceSets {

        commonMain.dependencies {
            implementation(projects.ui.core)
            implementation(projects.domain.game)
            implementation(projects.domain.settings)
        }

    }
}