# Anekon - Android CI/CD Assistant

Una aplicación Android para gestionar builds de GitHub Actions con AutoFix automático usando IA.

## Características

- **AutoFix**: Detecta builds fallidos, analiza errores con IA y aplica correcciones automáticamente
- **Múltiples proveedores de IA**: MiniMax Pro, OpenAI, Google Gemini, Anthropic Claude, Ollama local
- **Almacenamiento seguro**: API keys encriptadas con Android Keystore (EncryptedSharedPreferences)
- **GitHub Actions Integration**: Monitoriza workflows, ve logs, rerun builds
- **Constructor de Apps**: Genera código de apps Android con IA
- **Chat con IA**: Asistente para desarrollo

## Seguridad

Las API keys se almacenan de forma segura:
- **EncryptedSharedPreferences** con encriptación AES-256-GCM
- **Android Keystore** para gestión de claves criptográficas
- Nunca se suben a GitHub ni se exponen en código

## Requisitos

- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Android SDK 34

## Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/anekon.git
cd anekon
```

### 2. Configurar el SDK de Android

```bash
cp local.properties.example local.properties
# Edita local.properties y agrega tu SDK path
```

### 3. Configurar GitHub Actions (opcional para release)

Ve a Settings > Secrets en tu repositorio y agrega:
- `KEYSTORE_BASE64`: Tu keystore codificado en base64
- `KEYSTORE_PASSWORD`: Contraseña del keystore
- `KEY_ALIAS`: Alias del key
- `KEY_PASSWORD`: Contraseña del key

### 4. Ejecutar en desarrollo

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## API Keys

La app maneja API keys de forma segura. Ingresa tus keys desde la pantalla de configuración:

| Proveedor | Tipo | Uso |
|-----------|------|-----|
| MiniMax Pro | Pago | Análisis avanzado de errores |
| MiniMax Free | Free | Pruebas de estrés |
| OpenAI | Free | GPT-3.5/4 |
| Google Gemini | Free | Gemini Pro |
| Anthropic Claude | Pago | Claude 3 |
| Local/Ollama | Free | Para desarrollo local |

## GitHub Actions

El proyecto incluye workflows automatizados:

- `android-ci.yml`: Compilación, tests, lint, análisis estático

### Secrets necesarios para Release

```bash
# Generar keystore (solo una vez)
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key

# Codificar keystore para GitHub
base64 release.jks | tr -d '\n'
```

## Arquitectura

```
app/
├── data/
│   ├── api/          # Servicios Retrofit
│   ├── local/        # Room Database
│   └── security/      # SecureApiKeyManager
├── domain/
│   ├── model/        # Modelos de dominio
│   └── usecase/      # Casos de uso
└── ui/
    ├── screens/      # Pantallas Compose
    ├── theme/        # Tema de la app
    └── navigation/   # Navegación
```

## Licencia

MIT License
