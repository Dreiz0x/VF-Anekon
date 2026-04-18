package com.anekon.ci.domain.model

enum class ProjectArchitecture {
    MVVM,       // Model-View-ViewModel tradicional
    CLEAN,      // Clean Architecture (UI/Domain/Data)
    MVP         // Model-View-Presenter
}

enum class DependencyInjection {
    HILT,       // Hilt (recomendado)
    KOIN,       // Koin (más simple)
    MANUAL      // Sin DI (para aprender)
}

enum class NavigationType {
    JETPACK,    // Jetpack Navigation Component
    COMPOSE,    // Navigation Compose
    MANUAL      // Sin navegación automática
}

data class ProjectTemplate(
    val id: String,
    val name: String,
    val description: String,
    val architecture: ProjectArchitecture,
    val dependencyInjection: DependencyInjection,
    val navigation: NavigationType,
    val useRoom: Boolean,
    val useCompose: Boolean = true,
    val useCoroutines: Boolean = true,
    val githubActionsEnabled: Boolean = true,
    val licenseType: LicenseType = LicenseType.APACHE2
)

enum class LicenseType(val displayName: String, val spdxId: String) {
    APACHE2("Apache 2.0", "Apache-2.0"),
    MIT("MIT", "MIT"),
    GPL3("GPL 3.0", "GPL-3.0"),
    BSD3("BSD 3-Clause", "BSD-3-Clause"),
    NONE("Sin licencia", "UNLICENSED")
}

object ProjectTemplates {
    val availableTemplates = listOf(
        ProjectTemplate(
            id = "basic-compose",
            name = "Compose Básico",
            description = "Proyecto simple con Jetpack Compose y Hilt. Ideal para comenzar.",
            architecture = ProjectArchitecture.MVVM,
            dependencyInjection = DependencyInjection.HILT,
            navigation = NavigationType.COMPOSE,
            useRoom = false,
            githubActionsEnabled = true
        ),
        ProjectTemplate(
            id = "clean-arch",
            name = "Clean Architecture",
            description = "Arquitectura limpia completa con capas UI/Domain/Data. Para proyectos profesionales.",
            architecture = ProjectArchitecture.CLEAN,
            dependencyInjection = DependencyInjection.HILT,
            navigation = NavigationType.COMPOSE,
            useRoom = true,
            githubActionsEnabled = true
        ),
        ProjectTemplate(
            id = "room-heavy",
            name = "Base de Datos",
            description = "Proyecto focado en Room database con múltiples entidades y DAOs.",
            architecture = ProjectArchitecture.MVVM,
            dependencyInjection = DependencyInjection.HILT,
            navigation = NavigationType.JETPACK,
            useRoom = true,
            githubActionsEnabled = true
        ),
        ProjectTemplate(
            id = "minimal",
            name = "Minimal",
            description = "Proyecto mínimo sin dependencias extra. Solo Compose básico.",
            architecture = ProjectArchitecture.MVVM,
            dependencyInjection = DependencyInjection.MANUAL,
            navigation = NavigationType.COMPOSE,
            useRoom = false,
            githubActionsEnabled = false
        )
    )
    
    fun getTemplateById(id: String): ProjectTemplate? = 
        availableTemplates.find { it.id == id }
}
