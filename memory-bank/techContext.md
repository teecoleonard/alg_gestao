# Technical Context: Alg Gestão

## Core Technologies
- **Language**: Kotlin
- **Platform**: Android
- **Minimum SDK**: API 24
- **Architecture**: MVVM

## Jetpack Components
- ViewModel
- LiveData
- Room Database
- Navigation Component
- Data Binding

## Development Setup
1. **IDE**: Android Studio
2. **Build System**: Gradle (Kotlin DSL)
3. **Version Control**: Git
4. **Dependency Management**: Gradle with version catalogs

## Key Dependencies
- AndroidX libraries
- Material Components
- Room Persistence Library
- Retrofit (likely for API calls)
- Hilt (likely for DI)

## Backend API
- **Technology**: Node.js with Express
- **Database**: MySQL with Sequelize ORM
- **Authentication**: JWT
- **Documentation**: Swagger UI
- **Key Dependencies**:
  - express
  - sequelize
  - mysql2
  - jsonwebtoken
  - swagger-ui-express

## Build Configuration
- Multi-module structure
- Shared version catalog (libs.versions.toml)
- Separate build files per module
- Proguard rules for release builds

## Testing
- JUnit for unit tests
- AndroidJUnitRunner for instrumentation tests
- Espresso for UI tests
