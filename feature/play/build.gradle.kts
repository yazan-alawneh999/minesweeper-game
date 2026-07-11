plugins {
    alias(libs.plugins.kotlearn.featureModule)
}

kotlin {

    sourceSets {

        commonMain.dependencies {
            implementation(projects.ui.core)
            implementation(projects.domain.game)

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