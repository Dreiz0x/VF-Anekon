@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.anekon.ci.ui.screens.projectcreator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anekon.ci.domain.model.DependencyInjection
import com.anekon.ci.domain.model.LicenseType
import com.anekon.ci.domain.model.NavigationType
import com.anekon.ci.domain.model.ProjectArchitecture
import com.anekon.ci.domain.model.ProjectTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCreatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProjectCreatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var expandedSection by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✨ Creador de Proyectos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // ====== TARJETA: Info Básica ======
            item {
                SectionCard(
                    title = "📱 Información de la App",
                    icon = Icons.Default.Android,
                    isExpanded = expandedSection == "basic",
                    onToggle = { expandedSection = if (expandedSection == "basic") null else "basic" }
                ) {
                    OutlinedTextField(
                        value = state.appName,
                        onValueChange = { viewModel.updateAppName(it) },
                        label = { Text("Nombre de la App") },
                        placeholder = { Text("Mi App Increíble") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = state.packageName,
                        onValueChange = { viewModel.updatePackageName(it) },
                        label = { Text("Package Name") },
                        placeholder = { Text("com.miapp") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = state.projectName,
                        onValueChange = { viewModel.updateProjectName(it) },
                        label = { Text("Nombre del Proyecto (carpeta)") },
                        placeholder = { Text("mi-app") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = state.appDescription,
                        onValueChange = { viewModel.updateAppDescription(it) },
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("Una app increíble...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }
            
            // ====== TARJETA: Templates ======
            item {
                SectionCard(
                    title = "🎨 Templates",
                    icon = Icons.Default.Dashboard,
                    isExpanded = expandedSection == "templates",
                    onToggle = { expandedSection = if (expandedSection == "templates") null else "templates" }
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.availableTemplates) { template ->
                            TemplateCard(
                                template = template,
                                isSelected = state.selectedTemplate?.id == template.id,
                                onClick = { viewModel.selectTemplate(template) }
                            )
                        }
                    }
                }
            }
            
            // ====== TARJETA: Arquitectura ======
            item {
                SectionCard(
                    title = "🏗️ Arquitectura",
                    icon = Icons.Default.AccountTree,
                    isExpanded = expandedSection == "architecture",
                    onToggle = { expandedSection = if (expandedSection == "architecture") null else "architecture" }
                ) {
                    Text(
                        text = "Selecciona la estructura de tu proyecto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ArchitectureOption.entries.forEach { option ->
                        ArchitectureRadioItem(
                            option = option,
                            isSelected = state.architecture == option.architecture,
                            onClick = { viewModel.setArchitecture(option.architecture) }
                        )
                    }
                }
            }
            
            // ====== TARJETA: Dependencias ======
            item {
                SectionCard(
                    title = "📦 Inyección de Dependencias",
                    icon = Icons.Default.Inventory,
                    isExpanded = expandedSection == "di",
                    onToggle = { expandedSection = if (expandedSection == "di") null else "di" }
                ) {
                    DIOption.entries.forEach { option ->
                        DIPill(
                            option = option,
                            isSelected = state.dependencyInjection == option.di,
                            onClick = { viewModel.setDependencyInjection(option.di) }
                        )
                    }
                }
            }
            
            // ====== TARJETA: Navegación ======
            item {
                SectionCard(
                    title = "🔀 Navegación",
                    icon = Icons.Default.Navigation,
                    isExpanded = expandedSection == "nav",
                    onToggle = { expandedSection = if (expandedSection == "nav") null else "nav" }
                ) {
                    NavigationOption.entries.forEach { option ->
                        NavChip(
                            option = option,
                            isSelected = state.navigation == option.nav,
                            onClick = { viewModel.setNavigation(option.nav) }
                        )
                    }
                }
            }
            
            // ====== TARJETA: Opciones ======
            item {
                SectionCard(
                    title = "⚙️ Opciones",
                    icon = Icons.Default.Settings,
                    isExpanded = expandedSection == "options",
                    onToggle = { expandedSection = if (expandedSection == "options") null else "options" }
                ) {
                    OptionSwitch(
                        title = "Room Database",
                        subtitle = "Base de datos local SQLite",
                        icon = Icons.Default.Storage,
                        checked = state.useRoom,
                        onToggle = { viewModel.toggleRoom() }
                    )
                    
                    OptionSwitch(
                        title = "Coroutines",
                        subtitle = "Programación asíncrona",
                        icon = Icons.Default.RunCircle,
                        checked = state.useCoroutines,
                        onToggle = { viewModel.toggleCoroutines() }
                    )
                    
                    OptionSwitch(
                        title = "GitHub Actions",
                        subtitle = "CI/CD automático",
                        icon = Icons.Default.AutoAwesome,
                        checked = state.enableGithubActions,
                        onToggle = { viewModel.toggleGithubActions() }
                    )
                }
            }
            
            // ====== TARJETA: GitHub Actions Config ======
            if (state.enableGithubActions) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        SectionCard(
                            title = "🔑 Configuración Git",
                            icon = Icons.Default.Key,
                            isExpanded = expandedSection == "git",
                            onToggle = { expandedSection = if (expandedSection == "git") null else "git" }
                        ) {
                            OutlinedTextField(
                                value = state.gitAuthor,
                                onValueChange = { viewModel.setGitAuthor(it) },
                                label = { Text("Autor") },
                                placeholder = { Text("Tu Nombre") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = state.gitEmail,
                                onValueChange = { viewModel.setGitEmail(it) },
                                label = { Text("Email") },
                                placeholder = { Text("tu@email.com") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Licencia:",
                                style = MaterialTheme.typography.labelLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(LicenseType.entries.toList()) { license ->
                                    LicenseChip(
                                        license = license,
                                        isSelected = state.licenseType == license,
                                        onClick = { viewModel.setLicenseType(license) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // ====== BOTÓN GENERAR ======
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.isGenerating) {
                        LinearProgressIndicator(
                            progress = state.generationProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Generando... ${(state.generationProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.generateProject() },
                        enabled = !state.isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isGenerating) "Generando..." else "🚀 Generar Proyecto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Archivos generados
                    if (state.generatedFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "📁 Archivos generados:",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                state.generatedFiles.take(10).forEach { file ->
                                    Text(
                                        text = "• $file",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (state.generatedFiles.size > 10) {
                                    Text(
                                        text = "... y ${state.generatedFiles.size - 10} más",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
    
    // Diálogos de error/éxito
    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("❌ Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
    
    state.successMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccess() },
            title = { Text("✅ ¡Éxito!") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { viewModel.clearSuccess() }) {
                    Text("Descargar ZIP")
                }
            }
        )
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: ProjectTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = template.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (template.useRoom) {
                    FeatureChip("Room")
                }
                if (template.githubActionsEnabled) {
                    FeatureChip("CI")
                }
            }
        }
    }
}

@Composable
fun FeatureChip(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp
        )
    }
}

enum class ArchitectureOption(
    val architecture: ProjectArchitecture,
    val title: String,
    val description: String
) {
    MVVM(ProjectArchitecture.MVVM, "MVVM", "Simple y efectivo"),
    CLEAN(ProjectArchitecture.CLEAN, "Clean", "Profesional, escalable"),
    MVP(ProjectArchitecture.MVP, "MVP", "Separación clara")
}

@Composable
fun ArchitectureRadioItem(
    option: ArchitectureOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = option.title,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class DIOption(
    val di: DependencyInjection,
    val title: String
) {
    HILT(DependencyInjection.HILT, "Hilt"),
    KOIN(DependencyInjection.KOIN, "Koin"),
    MANUAL(DependencyInjection.MANUAL, "Manual")
}

@Composable
fun DIPill(
    option: DIOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(option.title) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null
    )
}

enum class NavigationOption(
    val nav: NavigationType,
    val title: String
) {
    JETPACK(NavigationType.JETPACK, "Jetpack Nav"),
    COMPOSE(NavigationType.COMPOSE, "Nav Compose"),
    MANUAL(NavigationType.MANUAL, "Manual")
}

@Composable
fun NavChip(
    option: NavigationOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(option.title) }
    )
}

@Composable
fun OptionSwitch(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}

@Composable
fun LicenseChip(
    license: LicenseType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(license.displayName) }
    )
}
