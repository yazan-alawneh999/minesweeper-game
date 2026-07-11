plugins {
    alias(libs.plugins.kotlearn.featureModule)
}

kotlin {

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {

        }
        commonMain.dependencies {
            implementation(projects.ui.core)
        }

        desktopMain.dependencies {
            
        }
    }
}
