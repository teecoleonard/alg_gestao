# Technical Context: Alg Gestão

## Core Technologies
- **Android SDK**: 34 (Android 14)
- **Kotlin**: 1.9.0
- **Jetpack Components**:
  - ViewModel
  - LiveData
  - Navigation Component
  - Room (for local storage)
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Database**: SQLite via Room

## API Integration
- **Base URL**: https://api.alg.com.br
- **Authentication**: JWT
- **Key Endpoints**:
  - `/api/contratos` (contracts)
  - `/api/equipamentos` (equipment)
  - `/api/devolucoes` (returns)
  - `/api/clientes` (clients)

## Architecture
- **MVVM Pattern** with Clean Architecture principles
- **Repository Pattern** for data access
- **Modular Structure**:
  - `:app` (main module)
  - `:core` (shared components)
  - `:feature:contracts`
  - `:feature:equipment`
  - `:feature:returns`

## Key Components
1. **Data Layer**:
   - API service interfaces
   - Database entities
   - Repositories

2. **Domain Layer**:
   - Business logic
   - Use cases
   - Domain models

3. **Presentation Layer**:
   - ViewModels
   - UI components (Activities/Fragments)
   - Navigation

## Development Tools
- **Android Studio** (latest stable)
- **Gradle** (KTS scripts)
- **Git** (version control)
- **Firebase** (analytics/crash reporting)
