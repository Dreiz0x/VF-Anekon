package com.anekon.ci.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anekon.ci.data.security.SecureApiKeyManager
import com.anekon.ci.data.security.ValidationResult
import com.anekon.ci.domain.model.AIProviderType
import com.anekon.ci.ui.theme.AnekonColors
import kotlinx.coroutines.launch

/**
 * Pantalla de Configuración - FIXED VERSION
 * Usa rememberSaveable para persistir estado correctamente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    apiKeyManager: SecureApiKeyManager? = null
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Token GitHub - usa rememberSaveable para persistir
    var githubToken by rememberSaveable { mutableStateOf("") }
    var showGitHubToken by rememberSaveable { mutableStateOf(false) }
    var githubTokenSaved by rememberSaveable { mutableStateOf(false) }

    // API Keys state - CARGA INMEDIATA desde SecureApiKeyManager
    var minimaxProKey by rememberSaveable { mutableStateOf("") }
    var minimaxProSaved by rememberSaveable { mutableStateOf(false) }

    var minimaxFreeKey by rememberSaveable { mutableStateOf("") }
    var minimaxFreeSaved by rememberSaveable { mutableStateOf(false) }

    var openaiKey by rememberSaveable { mutableStateOf("") }
    var openaiSaved by rememberSaveable { mutableStateOf(false) }

    var anthropicKey by rememberSaveable { mutableStateOf("") }
    var anthropicSaved by rememberSaveable { mutableStateOf(false) }

    var geminiKey by rememberSaveable { mutableStateOf("") }
    var geminiSaved by rememberSaveable { mutableStateOf(false) }

    var localEndpoint by rememberSaveable { mutableStateOf("http://localhost:11434") }

    // UI state
    var isValidating by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var snackbarMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // Validation states por provider
    var minimaxProValidation by rememberSaveable { mutableStateOf<ValidationResult?>(null) }
    var minimaxFreeValidation by rememberSaveable { mutableStateOf<ValidationResult?>(null) }
    var openaiValidation by rememberSaveable { mutableStateOf<ValidationResult?>(null) }
    var anthropicValidation by rememberSaveable { mutableStateOf<ValidationResult?>(null) }
    var geminiValidation by rememberSaveable { mutableStateOf<ValidationResult?>(null) }
    var localValidation by rememberSaveable { mutableStateOf<ValidationResult?>(null) }

    // Load existing keys on first composition
    LaunchedEffect(Unit) {
        apiKeyManager?.let { manager ->
            githubToken = manager.getGitHubToken() ?: ""
            githubTokenSaved = githubToken.isNotBlank()

            val minimaxPro = manager.getApiKey(AIProviderType.MINIMAX_PRO)
            if (!minimaxPro.isNullOrBlank()) {
                minimaxProKey = minimaxPro
                minimaxProSaved = true
            }

            val minimaxFree = manager.getApiKey(AIProviderType.MINIMAX_FREE)
            if (!minimaxFree.isNullOrBlank()) {
                minimaxFreeKey = minimaxFree
                minimaxFreeSaved = true
            }

            val openai = manager.getApiKey(AIProviderType.OPENAI)
            if (!openai.isNullOrBlank()) {
                openaiKey = openai
                openaiSaved = true
            }

            val anthropic = manager.getApiKey(AIProviderType.ANTHROPIC)
            if (!anthropic.isNullOrBlank()) {
                anthropicKey = anthropic
                anthropicSaved = true
            }

            val gemini = manager.getApiKey(AIProviderType.GEMINI)
            if (!gemini.isNullOrBlank()) {
                geminiKey = gemini
                geminiSaved = true
            }

            localEndpoint = manager.getLocalEndpoint() ?: "http://localhost:11434"
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AnekonColors.BackgroundPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AnekonColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ============ Seguridad ============
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AnekonColors.Success.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = AnekonColors.Success,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "API keys almacenadas con AES-256 + Android Keystore. Nunca se suben a GitHub.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AnekonColors.Success,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ============ GitHub Token ============
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "Cuenta GitHub")
            }

            item {
                SecureInputField(
                    label = "GitHub Token",
                    placeholder = "ghp_xxxxxxxxxxxx",
                    value = githubToken,
                    onValueChange = { githubToken = it; githubTokenSaved = false },
                    isPassword = true,
                    showPassword = showGitHubToken,
                    onTogglePassword = { showGitHubToken = !showGitHubToken },
                    isSaved = githubTokenSaved,
                    onSave = {
                        apiKeyManager?.saveGitHubToken(githubToken)
                        githubTokenSaved = true
                        snackbarMessage = "Token guardado de forma segura"
                        scope.launch {
                            snackbarHostState.showSnackbar("Token guardado de forma segura")
                        }
                    }
                )
            }

            // ============ API Keys ============
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "API Keys de IA")
            }

            // MiniMax Pro
            item {
                ApiKeyInput(
                    title = "MiniMax Pro",
                    description = "Máxima calidad (abab6.5s)",
                    badge = "PAGO",
                    badgeColor = AnekonColors.Accent,
                    icon = Icons.Default.Star,
                    value = minimaxProKey,
                    onValueChange = { minimaxProKey = it; minimaxProSaved = false },
                    isSaved = minimaxProSaved,
                    validation = minimaxProValidation,
                    isValidating = isValidating,
                    onSave = {
                        apiKeyManager?.saveApiKey(AIProviderType.MINIMAX_PRO, minimaxProKey)
                        minimaxProSaved = true
                        scope.launch {
                            snackbarHostState.showSnackbar("MiniMax Pro guardado de forma segura")
                        }
                    },
                    onDelete = {
                        apiKeyManager?.deleteApiKey(AIProviderType.MINIMAX_PRO)
                        minimaxProKey = ""
                        minimaxProSaved = false
                        minimaxProValidation = null
                    },
                    onValidate = {
                        isValidating = true
                        scope.launch {
                            minimaxProValidation = apiKeyManager?.validateApiKey(AIProviderType.MINIMAX_PRO, minimaxProKey)
                            isValidating = false
                        }
                    }
                )
            }

            // MiniMax Free
            item {
                ApiKeyInput(
                    title = "MiniMax Free",
                    description = "Tier gratuito",
                    badge = "FREE",
                    badgeColor = AnekonColors.Success,
                    icon = Icons.Default.Star,
                    value = minimaxFreeKey,
                    onValueChange = { minimaxFreeKey = it; minimaxFreeSaved = false },
                    isSaved = minimaxFreeSaved,
                    validation = minimaxFreeValidation,
                    isValidating = isValidating,
                    onSave = {
                        apiKeyManager?.saveApiKey(AIProviderType.MINIMAX_FREE, minimaxFreeKey)
                        minimaxFreeSaved = true
                        scope.launch {
                            snackbarHostState.showSnackbar("MiniMax Free guardado")
                        }
                    },
                    onDelete = {
                        apiKeyManager?.deleteApiKey(AIProviderType.MINIMAX_FREE)
                        minimaxFreeKey = ""
                        minimaxFreeSaved = false
                        minimaxFreeValidation = null
                    },
                    onValidate = {
                        isValidating = true
                        scope.launch {
                            minimaxFreeValidation = apiKeyManager?.validateApiKey(AIProviderType.MINIMAX_FREE, minimaxFreeKey)
                            isValidating = false
                        }
                    }
                )
            }

            // OpenAI
            item {
                ApiKeyInput(
                    title = "OpenAI",
                    description = "GPT-3.5/4",
                    badge = "FREE",
                    badgeColor = AnekonColors.Success,
                    icon = Icons.Default.Psychology,
                    value = openaiKey,
                    onValueChange = { openaiKey = it; openaiSaved = false },
                    isSaved = openaiSaved,
                    validation = openaiValidation,
                    isValidating = isValidating,
                    onSave = {
                        apiKeyManager?.saveApiKey(AIProviderType.OPENAI, openaiKey)
                        openaiSaved = true
                        scope.launch {
                            snackbarHostState.showSnackbar("OpenAI guardado")
                        }
                    },
                    onDelete = {
                        apiKeyManager?.deleteApiKey(AIProviderType.OPENAI)
                        openaiKey = ""
                        openaiSaved = false
                        openaiValidation = null
                    },
                    onValidate = {
                        isValidating = true
                        scope.launch {
                            openaiValidation = apiKeyManager?.validateApiKey(AIProviderType.OPENAI, openaiKey)
                            isValidating = false
                        }
                    }
                )
            }

            // Google Gemini
            item {
                ApiKeyInput(
                    title = "Google Gemini",
                    description = "Gemini Pro",
                    badge = "FREE",
                    badgeColor = AnekonColors.Success,
                    icon = Icons.Default.AutoAwesome,
                    value = geminiKey,
                    onValueChange = { geminiKey = it; geminiSaved = false },
                    isSaved = geminiSaved,
                    validation = geminiValidation,
                    isValidating = isValidating,
                    onSave = {
                        apiKeyManager?.saveApiKey(AIProviderType.GEMINI, geminiKey)
                        geminiSaved = true
                        scope.launch {
                            snackbarHostState.showSnackbar("Gemini guardado")
                        }
                    },
                    onDelete = {
                        apiKeyManager?.deleteApiKey(AIProviderType.GEMINI)
                        geminiKey = ""
                        geminiSaved = false
                        geminiValidation = null
                    },
                    onValidate = {
                        isValidating = true
                        scope.launch {
                            geminiValidation = apiKeyManager?.validateApiKey(AIProviderType.GEMINI, geminiKey)
                            isValidating = false
                        }
                    }
                )
            }

            // Anthropic
            item {
                ApiKeyInput(
                    title = "Anthropic Claude",
                    description = "Claude 3",
                    badge = "PAGO",
                    badgeColor = AnekonColors.Accent,
                    icon = Icons.Default.Human,
                    value = anthropicKey,
                    onValueChange = { anthropicKey = it; anthropicSaved = false },
                    isSaved = anthropicSaved,
                    validation = anthropicValidation,
                    isValidating = isValidating,
                    onSave = {
                        apiKeyManager?.saveApiKey(AIProviderType.ANTHROPIC, anthropicKey)
                        anthropicSaved = true
                        scope.launch {
                            snackbarHostState.showSnackbar("Claude guardado")
                        }
                    },
                    onDelete = {
                        apiKeyManager?.deleteApiKey(AIProviderType.ANTHROPIC)
                        anthropicKey = ""
                        anthropicSaved = false
                        anthropicValidation = null
                    },
                    onValidate = {
                        isValidating = true
                        scope.launch {
                            anthropicValidation = apiKeyManager?.validateApiKey(AIProviderType.ANTHROPIC, anthropicKey)
                            isValidating = false
                        }
                    }
                )
            }

            // Local/Ollama
            item {
                LocalEndpointInput(
                    title = "Local / Ollama",
                    description = "Ollama, LM Studio, etc.",
                    badge = "FREE",
                    badgeColor = AnekonColors.Success,
                    icon = Icons.Default.Computer,
                    value = localEndpoint,
                    onValueChange = { localEndpoint = it },
                    validation = localValidation,
                    isValidating = isValidating,
                    onSave = {
                        apiKeyManager?.saveLocalEndpoint(localEndpoint)
                        scope.launch {
                            snackbarHostState.showSnackbar("Endpoint guardado")
                        }
                    },
                    onValidate = {
                        isValidating = true
                        scope.launch {
                            localValidation = apiKeyManager?.validateApiKey(AIProviderType.LOCAL, localEndpoint)
                            isValidating = false
                        }
                    }
                )
            }

            // ============ Danger Zone ============
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Zona de Peligro")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AnekonColors.Error.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { showDeleteConfirm = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = AnekonColors.Error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Borrar todas las keys",
                                style = MaterialTheme.typography.titleMedium,
                                color = AnekonColors.Error,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Eliminar TODAS las API keys almacenadas",
                                style = MaterialTheme.typography.bodySmall,
                                color = AnekonColors.TextMuted
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = AnekonColors.TextMuted
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = AnekonColors.BackgroundSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = AnekonColors.Error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¿Borrar todas las keys?", color = AnekonColors.TextPrimary)
                }
            },
            text = {
                Text(
                    "Esta acción eliminará TODAS las API keys y tokens. No se puede deshacer.",
                    color = AnekonColors.TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        apiKeyManager?.clearAllKeys()
                        githubToken = ""
                        githubTokenSaved = false
                        minimaxProKey = ""
                        minimaxProSaved = false
                        minimaxProValidation = null
                        minimaxFreeKey = ""
                        minimaxFreeSaved = false
                        minimaxFreeValidation = null
                        openaiKey = ""
                        openaiSaved = false
                        openaiValidation = null
                        anthropicKey = ""
                        anthropicSaved = false
                        anthropicValidation = null
                        geminiKey = ""
                        geminiSaved = false
                        geminiValidation = null
                        showDeleteConfirm = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Todas las keys han sido eliminadas")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AnekonColors.Error)
                ) {
                    Text("Borrar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = AnekonColors.TextMuted)
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = AnekonColors.Accent,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SecureInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    isSaved: Boolean,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = AnekonColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isSaved) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AnekonColors.Success,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Guardado",
                            style = MaterialTheme.typography.bodySmall,
                            color = AnekonColors.Success
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(text = placeholder, color = AnekonColors.TextMuted)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AnekonColors.Accent,
                    unfocusedBorderColor = AnekonColors.BackgroundTertiary,
                    cursorColor = AnekonColors.Accent,
                    focusedTextColor = AnekonColors.TextPrimary,
                    unfocusedTextColor = AnekonColors.TextPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPassword && !showPassword) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                trailingIcon = if (isPassword) {
                    {
                        IconButton(onClick = onTogglePassword) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                                tint = AnekonColors.TextMuted
                            )
                        }
                    }
                } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onSave()
                        focusManager.clearFocus()
                    }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onSave()
                    focusManager.clearFocus()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) AnekonColors.Success else AnekonColors.Accent
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSaved) "Actualizar" else "Guardar")
            }
        }
    }
}

@Composable
private fun ApiKeyInput(
    title: String,
    description: String,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    isSaved: Boolean,
    validation: ValidationResult?,
    isValidating: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onValidate: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isEditing by rememberSaveable { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AnekonColors.Accent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AnekonColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.TextMuted
                    )
                }
                if (isSaved) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Configurado",
                        tint = AnekonColors.Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Validation status
            validation?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                ValidationStatus(result = result)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isEditing || !isSaved) {
                // Input mode
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(text = "Pega tu API key aquí", color = AnekonColors.TextMuted)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AnekonColors.Accent,
                        unfocusedBorderColor = AnekonColors.BackgroundTertiary,
                        cursorColor = AnekonColors.Accent,
                        focusedTextColor = AnekonColors.TextPrimary,
                        unfocusedTextColor = AnekonColors.TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (value.isNotBlank()) {
                                onSave()
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isEditing = false
                            if (!isSaved) value = ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.TextMuted),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            if (value.isNotBlank()) {
                                onSave()
                                isEditing = false
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = value.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AnekonColors.Success
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar")
                    }
                }
            } else {
                // View/Action mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Validate button
                    OutlinedButton(
                        onClick = onValidate,
                        modifier = Modifier.weight(1f),
                        enabled = !isValidating,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.Accent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isValidating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AnekonColors.Accent
                            )
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Validar")
                    }

                    // Edit button
                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.Accent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }

                    // Delete button
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.Error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalEndpointInput(
    title: String,
    description: String,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    validation: ValidationResult?,
    isValidating: Boolean,
    onSave: () -> Unit,
    onValidate: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AnekonColors.Accent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AnekonColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.TextMuted
                    )
                }
            }

            validation?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                ValidationStatus(result = result)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(text = "http://localhost:11434", color = AnekonColors.TextMuted)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AnekonColors.Accent,
                    unfocusedBorderColor = AnekonColors.BackgroundTertiary,
                    cursorColor = AnekonColors.Accent,
                    focusedTextColor = AnekonColors.TextPrimary,
                    unfocusedTextColor = AnekonColors.TextPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = AnekonColors.TextMuted
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onSave()
                        focusManager.clearFocus()
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onValidate,
                    modifier = Modifier.weight(1f),
                    enabled = !isValidating,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AnekonColors.Accent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AnekonColors.Accent
                        )
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Probar")
                }
                Button(
                    onClick = {
                        onSave()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AnekonColors.Success),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun ValidationStatus(result: ValidationResult) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (result) {
            is ValidationResult.Valid -> {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AnekonColors.Success,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Válida - Conectada",
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.Success
                )
            }
            is ValidationResult.Invalid -> {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = AnekonColors.Error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = result.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.Error
                )
            }
            is ValidationResult.Error -> {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AnekonColors.Warning,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.Warning
                )
            }
        }
    }
}
