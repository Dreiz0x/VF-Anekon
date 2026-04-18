package com.anekon.ci.ui.screens.repoanalyzer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekon.ci.ui.theme.AnekonColors
import kotlinx.coroutines.launch

/**
 * RepoAnalyzer - Selecciona repositorio y analiza/arregla errores
 */
data class RepoInfo(
    val name: String,
    val owner: String,
    val description: String,
    val language: String,
    val stars: Int,
    val lastBuild: String,
    val buildStatus: BuildStatus
)

enum class BuildStatus {
    SUCCESS, FAILED, RUNNING, UNKNOWN
}

data class FileChange(
    val filePath: String,
    val changeType: ChangeType,
    val description: String,
    val content: String,
    val linesAdded: Int = 0,
    val linesRemoved: Int = 0
)

enum class ChangeType {
    CREATE, MODIFY, DELETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoAnalyzerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCodeReview: (List<FileChange>) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Repositorios", "Análisis", "Cambios")

    var repoUrl by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisComplete by remember { mutableStateOf(false) }
    var showFileChanges by remember { mutableStateOf(false) }
    var selectedRepo by remember { mutableStateOf<RepoInfo?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AnekonColors.Amber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = AnekonColors.Amber,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Repo Analyzer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Selecciona y repara", style = MaterialTheme.typography.bodySmall, color = AnekonColors.TextMuted)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AnekonColors.BackgroundSecondary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AnekonColors.BackgroundPrimary)
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AnekonColors.BackgroundSecondary)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, tab ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AnekonColors.Amber,
                            selectedLabelColor = AnekonColors.BackgroundPrimary
                        )
                    )
                }
            }

            when (selectedTab) {
                0 -> ReposTab(
                    repoUrl = repoUrl,
                    onRepoUrlChange = { repoUrl = it },
                    isAnalyzing = isAnalyzing,
                    onAnalyze = {
                        isAnalyzing = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(2000)
                            isAnalyzing = false
                            analysisComplete = true
                            selectedRepo = RepoInfo(
                                name = repoUrl.substringAfterLast("/").ifEmpty { "mi-proyecto" },
                                owner = repoUrl.substringAfter("github.com/").substringBefore("/").ifEmpty { "usuario" },
                                description = "Repositorio seleccionado para análisis",
                                language = "Kotlin",
                                stars = 0,
                                lastBuild = "Hace 2h",
                                buildStatus = BuildStatus.FAILED
                            )
                        }
                    }
                )

                1 -> AnalysisTab(
                    selectedRepo = selectedRepo,
                    isAnalyzing = isAnalyzing,
                    onStartAnalysis = {
                        isAnalyzing = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(3000)
                            isAnalyzing = false
                            showFileChanges = true
                            selectedTab = 2
                        }
                    }
                )

                2 -> ChangesTab(
                    showFileChanges = showFileChanges,
                    onReviewChanges = { changes ->
                        onNavigateToCodeReview(changes)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReposTab(
    repoUrl: String,
    onRepoUrlChange: (String) -> Unit,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Ingresa la URL del repositorio",
                style = MaterialTheme.typography.titleMedium,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            OutlinedTextField(
                value = repoUrl,
                onValueChange = onRepoUrlChange,
                label = { Text("github.com/usuario/proyecto") },
                placeholder = { Text("Ej: github.com/miusuario/miapp-android") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AnekonColors.Amber,
                    cursorColor = AnekonColors.Amber
                ),
                leadingIcon = {
                    Icon(Icons.Default.Link, contentDescription = null, tint = AnekonColors.TextMuted)
                },
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Button(
                onClick = onAnalyze,
                enabled = repoUrl.isNotBlank() && !isAnalyzing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AnekonColors.Amber),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AnekonColors.BackgroundPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizando...")
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizar Repositorio")
                }
            }
        }

        item {
            Text(
                text = "Repositorios recientes",
                style = MaterialTheme.typography.titleMedium,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(sampleRepos) { repo ->
            RecentRepoCard(repo = repo)
        }
    }
}

@Composable
private fun RecentRepoCard(repo: RepoInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AnekonColors.BackgroundSecondary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (repo.buildStatus) {
                            BuildStatus.SUCCESS -> AnekonColors.Success.copy(alpha = 0.2f)
                            BuildStatus.FAILED -> AnekonColors.Error.copy(alpha = 0.2f)
                            BuildStatus.RUNNING -> AnekonColors.Amber.copy(alpha = 0.2f)
                            BuildStatus.UNKNOWN -> AnekonColors.TextMuted.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (repo.buildStatus) {
                        BuildStatus.SUCCESS -> Icons.Default.CheckCircle
                        BuildStatus.FAILED -> Icons.Default.Cancel
                        BuildStatus.RUNNING -> Icons.Default.PlayCircle
                        BuildStatus.UNKNOWN -> Icons.Default.Help
                    },
                    contentDescription = null,
                    tint = when (repo.buildStatus) {
                        BuildStatus.SUCCESS -> AnekonColors.Success
                        BuildStatus.FAILED -> AnekonColors.Error
                        BuildStatus.RUNNING -> AnekonColors.Amber
                        BuildStatus.UNKNOWN -> AnekonColors.TextMuted
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = AnekonColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = repo.owner,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AnekonColors.TextMuted
            )
        }
    }
}

@Composable
private fun AnalysisTab(
    selectedRepo: RepoInfo?,
    isAnalyzing: Boolean,
    onStartAnalysis: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (selectedRepo != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AnekonColors.BackgroundSecondary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Repositorio Seleccionado",
                            style = MaterialTheme.typography.labelMedium,
                            color = AnekonColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedRepo.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = AnekonColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedRepo.owner,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AnekonColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AnalysisStat(label = "Lenguaje", value = selectedRepo.language)
                            AnalysisStat(label = "Último Build", value = selectedRepo.lastBuild)
                            AnalysisStat(
                                label = "Estado",
                                value = when (selectedRepo.buildStatus) {
                                    BuildStatus.SUCCESS -> "OK"
                                    BuildStatus.FAILED -> "Fallido"
                                    BuildStatus.RUNNING -> "Ejecutando"
                                    BuildStatus.UNKNOWN -> "?"
                                }
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onStartAnalysis,
                    enabled = !isAnalyzing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AnekonColors.Amber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AnekonColors.BackgroundPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analizando con IA...")
                    } else {
                        Icon(Icons.Default.Psychology, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Análisis con AI")
                    }
                }
            }

            if (isAnalyzing) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AnekonColors.BackgroundSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AnalysisStep(
                                step = 1,
                                title = "Revisando logs de build",
                                isComplete = false,
                                isActive = true
                            )
                            AnalysisStep(
                                step = 2,
                                title = "Identificando errores",
                                isComplete = false,
                                isActive = false
                            )
                            AnalysisStep(
                                step = 3,
                                title = "Generando fixes",
                                isComplete = false,
                                isActive = false
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AnekonColors.BackgroundSecondary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = AnekonColors.TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Selecciona un repositorio",
                            style = MaterialTheme.typography.titleMedium,
                            color = AnekonColors.TextPrimary
                        )
                        Text(
                            text = "Ve a la pestaña Repositorios para seleccionar uno",
                            style = MaterialTheme.typography.bodySmall,
                            color = AnekonColors.TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = AnekonColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AnekonColors.TextMuted
        )
    }
}

@Composable
private fun AnalysisStep(
    step: Int,
    title: String,
    isComplete: Boolean,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isComplete -> AnekonColors.Success
                        isActive -> AnekonColors.Amber
                        else -> AnekonColors.TextMuted.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AnekonColors.BackgroundPrimary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = AnekonColors.BackgroundPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive || isComplete) AnekonColors.TextPrimary else AnekonColors.TextMuted,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ChangesTab(
    showFileChanges: Boolean,
    onReviewChanges: (List<FileChange>) -> Unit
) {
    val sampleChanges = remember {
        listOf(
            FileChange(
                filePath = "app/src/main/java/com/example/MainActivity.kt",
                changeType = ChangeType.MODIFY,
                description = "Corrige NullPointerException en onCreate()",
                content = """package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Tu app aquí
                }
            }
        }
    }
}
""",
                linesAdded = 12,
                linesRemoved = 8
            ),
            FileChange(
                filePath = "app/build.gradle.kts",
                changeType = ChangeType.MODIFY,
                description = "Actualiza versión de Kotlin a 1.9.22",
                content = """plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
}""",
                linesAdded = 15,
                linesRemoved = 10
            ),
            FileChange(
                filePath = "app/src/main/AndroidManifest.xml",
                changeType = ChangeType.MODIFY,
                description = "Agrega permisos necesarios",
                content = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Mi App"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApp">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.MyApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>""",
                linesAdded = 8,
                linesRemoved = 3
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Archivos a Modificar",
                style = MaterialTheme.typography.titleMedium,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "La IA ha identificado los siguientes cambios necesarios",
                style = MaterialTheme.typography.bodySmall,
                color = AnekonColors.TextMuted
            )
        }

        if (showFileChanges) {
            items(sampleChanges) { change ->
                FileChangeCard(
                    change = change,
                    onViewContent = { /* Show dialog with full content */ }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Copy all changes */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copiar Todo")
                    }

                    Button(
                        onClick = { onReviewChanges(sampleChanges) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AnekonColors.Amber),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aplicar")
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AnekonColors.BackgroundSecondary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = AnekonColors.TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sin cambios pendientes",
                            style = MaterialTheme.typography.titleMedium,
                            color = AnekonColors.TextPrimary
                        )
                        Text(
                            text = "Analiza un repositorio para ver los cambios",
                            style = MaterialTheme.typography.bodySmall,
                            color = AnekonColors.TextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FileChangeCard(
    change: FileChange,
    onViewContent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AnekonColors.BackgroundSecondary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (change.changeType) {
                                ChangeType.CREATE -> AnekonColors.Success.copy(alpha = 0.2f)
                                ChangeType.MODIFY -> AnekonColors.Amber.copy(alpha = 0.2f)
                                ChangeType.DELETE -> AnekonColors.Error.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (change.changeType) {
                            ChangeType.CREATE -> "CREAR"
                            ChangeType.MODIFY -> "MODIFICAR"
                            ChangeType.DELETE -> "ELIMINAR"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (change.changeType) {
                            ChangeType.CREATE -> AnekonColors.Success
                            ChangeType.MODIFY -> AnekonColors.Amber
                            ChangeType.DELETE -> AnekonColors.Error
                        }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "+${change.linesAdded} -${change.linesRemoved}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AnekonColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = change.filePath,
                style = MaterialTheme.typography.titleSmall,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = change.description,
                style = MaterialTheme.typography.bodySmall,
                color = AnekonColors.TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onViewContent) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver Código")
                }
            }
        }
    }
}

// Sample data
private val sampleRepos = listOf(
    RepoInfo(
        name = "mi-app-android",
        owner = "usuario",
        description = "App de gestión de tareas",
        language = "Kotlin",
        stars = 12,
        lastBuild = "Hace 1h",
        buildStatus = BuildStatus.FAILED
    ),
    RepoInfo(
        name = "anekon-ci",
        owner = "anekon",
        description = "CI/CD con IA para Android",
        language = "Kotlin",
        stars = 5,
        lastBuild = "Hace 3h",
        buildStatus = BuildStatus.SUCCESS
    ),
    RepoInfo(
        name = "flutter-app",
        owner = "usuario",
        description = "App en Flutter",
        language = "Dart",
        stars = 3,
        lastBuild = "Ayer",
        buildStatus = BuildStatus.RUNNING
    )
)