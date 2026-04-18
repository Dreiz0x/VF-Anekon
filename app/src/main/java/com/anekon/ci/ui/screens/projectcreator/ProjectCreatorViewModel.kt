package com.anekon.ci.ui.screens.projectcreator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anekon.ci.domain.model.LicenseType
import com.anekon.ci.domain.model.ProjectArchitecture
import com.anekon.ci.domain.model.DependencyInjection
import com.anekon.ci.domain.model.NavigationType
import com.anekon.ci.domain.model.ProjectTemplate
import com.anekon.ci.domain.model.ProjectTemplates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectCreatorState(
    val packageName: String = "com.myapp",
    val appName: String = "MiApp",
    val projectName: String = "mi-app",
    val appDescription: String = "",
    val selectedTemplate: ProjectTemplate? = null,
    val architecture: ProjectArchitecture = ProjectArchitecture.MVVM,
    val dependencyInjection: DependencyInjection = DependencyInjection.HILT,
    val navigation: NavigationType = NavigationType.COMPOSE,
    val useRoom: Boolean = false,
    val useCoroutines: Boolean = true,
    val enableGithubActions: Boolean = true,
    val licenseType: LicenseType = LicenseType.APACHE2,
    val gitAuthor: String = "",
    val gitEmail: String = "",
    val isGenerating: Boolean = false,
    val generationProgress: Float = 0f,
    val generatedFiles: List<String> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProjectCreatorViewModel @Inject constructor() : ViewModel() {
    
    private val _state = MutableStateFlow(ProjectCreatorState())
    val state: StateFlow<ProjectCreatorState> = _state.asStateFlow()
    
    val availableTemplates = ProjectTemplates.availableTemplates
    
    fun updatePackageName(name: String) {
        val sanitized = name.filter { it.isLetterOrDigit() || it == '.' }
            .lowercase()
            .trim()
        _state.update { it.copy(packageName = sanitized) }
    }
    
    fun updateAppName(name: String) {
        val cleanName = name.filter { it.isLetterOrDigit() || it.isWhitespace() }
        _state.update { 
            it.copy(
                appName = cleanName,
                projectName = cleanName.lowercase()
                    .split("\\s+".toRegex())
                    .joinToString("-")
                    .filter { c -> c.isLetterOrDigit() || c == '-' }
            )
        }
    }
    
    fun updateProjectName(name: String) {
        val sanitized = name.filter { it.isLetterOrDigit() || it == '-' }
            .lowercase()
        _state.update { it.copy(projectName = sanitized) }
    }
    
    fun updateAppDescription(desc: String) {
        _state.update { it.copy(appDescription = desc) }
    }
    
    fun selectTemplate(template: ProjectTemplate) {
        _state.update { 
            it.copy(
                selectedTemplate = template,
                architecture = template.architecture,
                dependencyInjection = template.dependencyInjection,
                navigation = template.navigation,
                useRoom = template.useRoom,
                useCoroutines = template.useCoroutines,
                enableGithubActions = template.githubActionsEnabled
            )
        }
    }
    
    fun setArchitecture(arch: ProjectArchitecture) {
        _state.update { it.copy(architecture = arch) }
    }
    
    fun setDependencyInjection(di: DependencyInjection) {
        _state.update { it.copy(dependencyInjection = di) }
    }
    
    fun setNavigation(nav: NavigationType) {
        _state.update { it.copy(navigation = nav) }
    }
    
    fun toggleRoom() {
        _state.update { it.copy(useRoom = !it.useRoom) }
    }
    
    fun toggleCoroutines() {
        _state.update { it.copy(useCoroutines = !it.useCoroutines) }
    }
    
    fun toggleGithubActions() {
        _state.update { it.copy(enableGithubActions = !it.enableGithubActions) }
    }
    
    fun setLicenseType(license: LicenseType) {
        _state.update { it.copy(licenseType = license) }
    }
    
    fun setGitAuthor(author: String) {
        _state.update { it.copy(gitAuthor = author) }
    }
    
    fun setGitEmail(email: String) {
        _state.update { it.copy(gitEmail = email) }
    }
    
    fun validateInputs(): Boolean {
        val current = _state.value
        
        if (current.packageName.isBlank() || !current.packageName.contains('.')) {
            _state.update { it.copy(error = "Package name inválido (ej: com.miapp)") }
            return false
        }
        
        if (current.appName.isBlank()) {
            _state.update { it.copy(error = "Nombre de app requerido") }
            return false
        }
        
        if (current.projectName.isBlank()) {
            _state.update { it.copy(error = "Nombre de proyecto requerido") }
            return false
        }
        
        if (current.enableGithubActions && current.gitAuthor.isBlank()) {
            _state.update { it.copy(error = "Autor de Git requerido para GitHub Actions") }
            return false
        }
        
        if (current.enableGithubActions && current.gitEmail.isBlank()) {
            _state.update { it.copy(error = "Email de Git requerido para GitHub Actions") }
            return false
        }
        
        return true
    }
    
    fun generateProject() {
        if (!validateInputs()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null, generationProgress = 0f) }
            
            try {
                val current = _state.value
                val files = mutableListOf<String>()
                
                // Fase 1: Configuración base (10%)
                _state.update { it.copy(generationProgress = 0.1f) }
                
                // build.gradle.kts principal
                files.add("build.gradle.kts")
                _state.update { it.copy(generationProgress = 0.2f, generatedFiles = files.toList()) }
                
                // settings.gradle.kts
                files.add("settings.gradle.kts")
                _state.update { it.copy(generationProgress = 0.3f, generatedFiles = files.toList()) }
                
                // gradle.properties
                files.add("gradle.properties")
                _state.update { it.copy(generationProgress = 0.35f, generatedFiles = files.toList()) }
                
                // gradle/wrapper/
                files.add("gradle/wrapper/gradle-wrapper.jar")
                files.add("gradle/wrapper/gradle-wrapper.properties")
                _state.update { it.copy(generationProgress = 0.4f, generatedFiles = files.toList()) }
                
                // Fase 2: Módulo app (40%)
                files.add("app/build.gradle.kts")
                _state.update { it.copy(generationProgress = 0.5f, generatedFiles = files.toList()) }
                
                files.add("app/src/main/AndroidManifest.xml")
                _state.update { it.copy(generationProgress = 0.55f, generatedFiles = files.toList()) }
                
                files.add("app/src/main/java/${current.packageName.replace(".", "/")}/MainActivity.kt")
                _state.update { it.copy(generationProgress = 0.6f, generatedFiles = files.toList()) }
                
                // Fase 3: Estructura según arquitectura (70%)
                if (current.architecture == ProjectArchitecture.CLEAN) {
                    files.add("app/src/main/java/${current.packageName.replace(".", "/")}/domain/model/")
                    files.add("app/src/main/java/${current.packageName.replace(".", "/")}/domain/usecase/")
                    files.add("app/src/main/java/${current.packageName.replace(".", "/")}/data/repository/")
                }
                
                if (current.useRoom) {
                    files.add("app/src/main/java/${current.packageName.replace(".", "/")}/data/local/dao/")
                    files.add("app/src/main/java/${current.packageName.replace(".", "/")}/data/local/entity/")
                    files.add("app/src/main/java/${current.packageName.replace(".", "/")}/data/local/AppDatabase.kt")
                }
                
                _state.update { it.copy(generationProgress = 0.7f, generatedFiles = files.toList()) }
                
                // Fase 4: Recursos (85%)
                files.add("app/src/main/res/values/strings.xml")
                files.add("app/src/main/res/values/themes.xml")
                files.add("app/src/main/res/drawable/")
                _state.update { it.copy(generationProgress = 0.85f, generatedFiles = files.toList()) }
                
                // Fase 5: GitHub Actions (100%)
                if (current.enableGithubActions) {
                    files.add(".github/workflows/android-ci.yml")
                    files.add(".github/workflows/release.yml")
                }
                
                files.add("README.md")
                files.add("LICENSE")
                
                _state.update { 
                    it.copy(
                        generationProgress = 1f,
                        generatedFiles = files.toList(),
                        isGenerating = false,
                        successMessage = "✅ Proyecto '${current.appName}' listo para descargar!"
                    )
                }
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isGenerating = false,
                        error = "Error al generar proyecto: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
    
    fun resetState() {
        _state.value = ProjectCreatorState()
    }
}
