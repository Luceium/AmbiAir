import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.serialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
            implementation("com.google.firebase:firebase-messaging-ktx")
            implementation("com.google.firebase:firebase-analytics-ktx")
            implementation("io.ktor:ktor-client-android:3.1.3")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.1.3")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.kermit)
            implementation(libs.messagebarkmp)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            api("dev.icerock.moko:biometry:0.4.0")
            api("dev.icerock.moko:biometry-compose:0.4.0")
            api(libs.geo)
            api(libs.geo.compose)
            api(libs.permissions)
            api(libs.permissions.compose)
            implementation(libs.permissions.location)
            implementation(libs.konnectivity)
            implementation("io.ktor:ktor-client-core:3.1.3")
            implementation("io.coil-kt.coil3:coil-compose:3.2.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.2.0")

            implementation("io.github.vinceglb:filekit-dialogs-compose:0.10.0-beta04")
        }
    }
}

android {
    namespace = "tech.luceium.ambiair"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "tech.luceium.ambiair"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        val mapbox = project.loadLocalProperty(
            path = "local.properties",
            propertyName = "mapbox",
        )
        buildConfigField("String", "mapbox", mapbox)
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures{
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.runtime.android)
    debugImplementation(compose.uiTooling)
    commonMainApi("dev.icerock.moko:biometry:0.4.0")
    commonMainApi("dev.icerock.moko:biometry-compose:0.4.0")
    commonMainApi(libs.geo)
    commonMainApi(libs.geo.compose)
    commonMainApi(libs.permissions)
    commonMainApi(libs.permissions.compose)
    commonMainImplementation(libs.permissions.location)
}

fun Project.loadLocalProperty(
    path: String,
    propertyName: String,
): String {
    val localProperties = Properties()
    val localPropertiesFile = project.rootProject.file(path)
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
        return localProperties.getProperty(propertyName)
    } else {
        throw GradleException("can not find property : $propertyName")
    }

}
