plugins {
    `kotlin-dsl`
}

dependencies {

    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)

}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = libs.plugins.kotlearn.kotlinMultiplatform.get().pluginId
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }

        register("androidLibrary") {
            id = libs.plugins.kotlearn.androidLibrary.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        register("domainModule") {
            id = libs.plugins.kotlearn.domainModule.get().pluginId
            implementationClass = "DomainModuleConventionPlugin"
        }

        register("dataModule") {
            id = libs.plugins.kotlearn.dataModule.get().pluginId
            implementationClass = "DataModuleConventionPlugin"
        }

        register("featureModule") {
            id = libs.plugins.kotlearn.featureModule.get().pluginId
            implementationClass = "FeatureModuleConventionPlugin"
        }
    }
}