plugins {
    alias(libs.plugins.kotlearn.featureModule)
}

kotlin {

    sourceSets {

        commonMain.dependencies {
            implementation(projects.ui.core)
            implementation(projects.core.audio)
            implementation(projects.domain.game)
            implementation(projects.domain.settings)

            implementation(kotlin("test"))
        }

    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.resources {
    publicResClass = true
    generateResClass = always
}