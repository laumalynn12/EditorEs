import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystoreFile = rootProject.file("signing/release.keystore")
val signingProperties = Properties().apply {
    val file = rootProject.file("signing/signing.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.editor.es"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.editor.es"
        minSdk = 28
        targetSdk = 28
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        jniLibs.useLegacyPackaging = true
    }

    if (keystoreFile.exists()) {
        signingConfigs.create("release").apply {
            storeFile = keystoreFile
            storePassword = signingProperties.getProperty("storePassword", "")
            keyAlias = signingProperties.getProperty("keyAlias", "")
            keyPassword = signingProperties.getProperty("keyPassword", "")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }

    lint {
        disable += "ExpiredTargetSdkVersion"
    }

    androidResources {
        localeFilters += listOf("en")
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        resources.pickFirsts += listOf(
            "license/README.dom.txt",
            "license/LICENSE.dom-documentation.txt",
            "license/NOTICE",
            "license/LICENSE.dom-software.txt",
            "license/LICENSE"
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.androidx.desugar.jdk.libs)
    implementation("io.github.rosemoe:editor")
    implementation("io.github.rosemoe:language-textmate")
    implementation("io.github.rosemoe:editor-lsp")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:1.0.0")
    implementation(project(":terminal-view"))
    implementation("org.apache.commons:commons-compress:1.28.0")
    implementation("org.tukaani:xz:1.12")
    implementation("com.android.tools.smali:smali-dexlib2:3.0.5")
    implementation("com.android.tools.build:apksig:8.7.3")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation("com.github.tiny-computer:avnc:030e6c032e")
}
