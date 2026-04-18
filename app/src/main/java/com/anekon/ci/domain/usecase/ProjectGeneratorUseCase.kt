package com.anekon.ci.domain.usecase

import android.content.Context
import com.anekon.ci.domain.model.DependencyInjection
import com.anekon.ci.domain.model.LicenseType
import com.anekon.ci.domain.model.NavigationType
import com.anekon.ci.domain.model.ProjectArchitecture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ProjectConfig(
    val packageName: String,
    val appName: String,
    val projectName: String,
    val appDescription: String,
    val architecture: ProjectArchitecture,
    val dependencyInjection: DependencyInjection,
    val navigation: NavigationType,
    val useRoom: Boolean,
    val useCoroutines: Boolean,
    val enableGithubActions: Boolean,
    val licenseType: LicenseType,
    val gitAuthor: String,
    val gitEmail: String
)

@Singleton
class ProjectGeneratorUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun generateProject(config: ProjectConfig): Result<File> = withContext(Dispatchers.IO) {
        try {
            val baseDir = File(context.cacheDir, "projects/${config.projectName}")
            baseDir.mkdirs()
            
            // 1. Gradle files
            generateBuildGradleKts(baseDir, config)
            generateSettingsGradleKts(baseDir, config)
            generateGradleProperties(baseDir)
            
            // 2. App module
            generateAppBuildGradleKts(baseDir, config)
            generateAndroidManifest(baseDir, config)
            generateMainActivity(baseDir, config)
            generateApplicationClass(baseDir, config)
            
            // 3. Resources
            generateResources(baseDir, config)
            
            // 4. Architecture structure
            generateArchitectureStructure(baseDir, config)
            
            // 5. GitHub Actions
            if (config.enableGithubActions) {
                generateGithubWorkflows(baseDir, config)
            }
            
            // 6. Documentation
            generateReadme(baseDir, config)
            generateLicense(baseDir, config)
            generateGitignore(baseDir)
            
            Result.success(baseDir)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateBuildGradleKts(baseDir: File, config: ProjectConfig) {
        val content = """
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    ${if (config.useRoom) "id(\"com.google.devtools.ksp\") version \"1.9.22-1.0.17\" apply false" else ""}
}
"""
        File(baseDir, "build.gradle.kts").writeText(content)
    }
    
    private fun generateSettingsGradleKts(baseDir: File, config: ProjectConfig) {
        val content = """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "${config.appName}"
include(":app")
"""
        File(baseDir, "settings.gradle.kts").writeText(content)
    }
    
    private fun generateGradleProperties(baseDir: File) {
        val content = """
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
android.nonTransitiveRClass=true
"""
        File(baseDir, "gradle.properties").writeText(content)
    }
    
    private fun generateAppBuildGradleKts(baseDir: File, config: ProjectConfig) {
        val roomDeps = if (config.useRoom) """
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
""" else ""
        
        val coroutinesDeps = if (config.useCoroutines) """
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
""" else ""

        val content = """
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    ${if (config.useRoom) "id(\"com.google.devtools.ksp\")" else ""}
    kotlin("kapt")
}

android {
    namespace = "${config.packageName}"
    compileSdk = 34

    defaultConfig {
        applicationId = "${config.packageName}"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Navigation
${when (config.navigation) {
    NavigationType.COMPOSE -> """
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")
"""
    NavigationType.JETPACK -> """
    // Jetpack Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
"""
    else -> ""
}}

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Retrofit (para APIs)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil (imágenes)
    implementation("io.coil-kt:coil-compose:2.5.0")

$roomDeps
$coroutinesDeps

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}
"""
        File(baseDir, "app/build.gradle.kts").writeText(content)
    }
    
    private fun generateAndroidManifest(baseDir: File, config: ProjectConfig) {
        val manifestDir = File(baseDir, "app/src/main")
        manifestDir.mkdirs()
        
        val content = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".AnekonApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.${config.appName.replace(" ", "")}">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.${config.appName.replace(" ", "")}">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
"""
        File(manifestDir, "AndroidManifest.xml").writeText(content)
    }
    
    private fun generateMainActivity(baseDir: File, config: ProjectConfig) {
        val pkgPath = config.packageName.replace(".", "/")
        val mainDir = File(baseDir, "app/src/main/java/$pkgPath")
        mainDir.mkdirs()
        
        val navImport = when (config.navigation) {
            NavigationType.COMPOSE -> """
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.route
import androidx.navigation.navigation"""
            else -> ""
        }
        
        val navSetup = when (config.navigation) {
            NavigationType.COMPOSE -> """
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(navController)
            }
        }"""
            else -> """
        setContent {
            ${config.appName.replace(" ", "")}Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }"""
        }
        
        val content = """
package ${config.packageName}

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier$navImport

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ${config.appName.replace(" ", "")}Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    $navSetup
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello ${'$'}name!")
}
"""
        File(mainDir, "MainActivity.kt").writeText(content)
    }
    
    private fun generateApplicationClass(baseDir: File, config: ProjectConfig) {
        val pkgPath = config.packageName.replace(".", "/")
        val mainDir = File(baseDir, "app/src/main/java/$pkgPath")
        mainDir.mkdirs()
        
        val content = """
package ${config.packageName}

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AnekonApplication : Application()
"""
        File(mainDir, "AnekonApplication.kt").writeText(content)
    }
    
    private fun generateResources(baseDir: File, config: ProjectConfig) {
        val resDir = File(baseDir, "app/src/main/res")
        resDir.mkdirs()
        
        // strings.xml
        val valuesDir = File(resDir, "values")
        valuesDir.mkdirs()
        File(valuesDir, "strings.xml").writeText("""
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">${config.appName}</string>
    <string name="app_description">${config.appDescription}</string>
</resources>
""")
        
        // themes.xml
        File(valuesDir, "themes.xml").writeText("""
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.${config.appName.replace(" ", "")}" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
""")
    }
    
    private fun generateArchitectureStructure(baseDir: File, config: ProjectConfig) {
        val pkgPath = config.packageName.replace(".", "/")
        val srcDir = File(baseDir, "app/src/main/java/$pkgPath")
        
        when (config.architecture) {
            ProjectArchitecture.CLEAN -> {
                File(srcDir, "domain/model").mkdirs()
                File(srcDir, "domain/usecase").mkdirs()
                File(srcDir, "data/repository").mkdirs()
                File(srcDir, "data/local/dao").mkdirs()
                File(srcDir, "data/local/entity").mkdirs()
                File(srcDir, "ui/theme").mkdirs()
                File(srcDir, "di").mkdirs()
            }
            ProjectArchitecture.MVVM -> {
                File(srcDir, "ui/screens").mkdirs()
                File(srcDir, "ui/viewmodel").mkdirs()
                File(srcDir, "data/repository").mkdirs()
                if (config.useRoom) {
                    File(srcDir, "data/local/dao").mkdirs()
                    File(srcDir, "data/local/entity").mkdirs()
                }
            }
            ProjectArchitecture.MVP -> {
                File(srcDir, "presenter").mkdirs()
                File(srcDir, "view").mkdirs()
                File(srcDir, "model").mkdirs()
            }
        }
    }
    
    private fun generateGithubWorkflows(baseDir: File, config: ProjectConfig) {
        val workflowsDir = File(baseDir, ".github/workflows")
        workflowsDir.mkdirs()
        
        val content = """
name: Android CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Setup JDK
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Android SDK
      uses: android-actions/setup-android@v3
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build Debug APK
      run: ./gradlew assembleDebug
    
    - name: Upload Debug APK
      uses: actions/upload-artifact@v4
      with:
        name: debug-apk
        path: app/build/outputs/apk/debug/app-debug.apk
"""
        File(workflowsDir, "android-ci.yml").writeText(content)
    }
    
    private fun generateReadme(baseDir: File, config: ProjectConfig) {
        val content = """
# ${config.appName}

${config.appDescription}

## 📱 Características

- Arquitectura: ${config.architecture.name}
- Inyección de Dependencias: ${config.dependencyInjection.name}
- Navegación: ${config.navigation.name}
${if (config.useRoom) "- Base de Datos: Room" else ""}
${if (config.useCoroutines) "- Programación Asíncrona: Coroutines" else ""}

## 🚀 Instalación

1. Clona el repositorio
2. Abre en Android Studio
3. Sincroniza Gradle
4. Ejecuta en emulador o dispositivo

## 📄 Licencia

Este proyecto está bajo la licencia ${config.licenseType.displayName}.
Ver archivo [LICENSE](LICENSE) para más detalles.
"""
        File(baseDir, "README.md").writeText(content)
    }
    
    private fun generateLicense(baseDir: File, config: ProjectConfig) {
        val content = when (config.licenseType) {
            LicenseType.MIT -> """
MIT License

Copyright (c) ${java.time.Year.now()} ${config.gitAuthor}

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
"""
            LicenseType.APACHE2 -> """
Apache License, Version 2.0

Copyright ${java.time.Year.now()} ${config.gitAuthor}

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
"""
            else -> ""
        }
        File(baseDir, "LICENSE").writeText(content)
    }
    
    private fun generateGitignore(baseDir: File) {
        val content = """
# Built application files
*.apk
*.aar
*.ap_
*.aab

# Files for the ART/Dalvik VM
*.dex

# Java class files
*.class

# Generated files
bin/
gen/
out/

# Gradle files
.gradle/
build/

# Local configuration file (SDK path, etc)
local.properties

# IntelliJ
*.iml
*.ipr
*.iws
.idea/

# Keystore files
*.jks
*.keystore

# OS-specific files
.DS_Store
Thumbs.db
"""
        File(baseDir, ".gitignore").writeText(content)
    }
}
