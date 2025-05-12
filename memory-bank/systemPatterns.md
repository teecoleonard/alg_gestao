# System Patterns: Alg Gestão

## Architecture Overview
Android application following MVVM pattern with:
- Presentation Layer (UI components)
- Domain Layer (business logic)
- Data Layer (persistence)

## Key Components
1. **Authentication Module**
   - Login/Register flows
   - Session management

2. **Dashboard Module**
   - Summary widgets
   - Quick actions

3. **Contract Management**
   - CRUD operations
   - Status tracking
   - Equipment assignment

4. **Equipment Tracking**
   - Inventory management
   - Maintenance records

5. **Client Management**
   - Client profiles
   - Contract history

## Data Flow
1. UI → ViewModel → Repository → Database
2. Database → Repository → ViewModel → UI

## Design Patterns
- MVVM (Model-View-ViewModel)
- Repository pattern
- Singleton (for shared preferences)
- Observer (LiveData for UI updates)
- Dependency Injection (likely using Hilt)

## API Integration
- RESTful API with Node.js/Express backend
- JWT authentication
- Endpoints organized by resource:
  - /api/clientes
  - /api/contratos  
  - /api/equipamentos
  - /api/usuarios
  - /api/devolucoes (new returns management endpoints)
- Uses Retrofit in Android client
- Swagger documentation available at /api-docs

### Returns API Endpoints
1. POST /api/devolucoes - Create new return item
2. GET /api/devolucoes - List all return items
3. GET /api/devolucoes/{id} - Get return by ID
4. GET /api/devolucoes/contrato/{contratoId} - Get returns by contract
5. GET /api/devolucoes/dev-num/{devNum} - Get returns by return number
6. PUT /api/devolucoes/{id} - Process/update return
7. DELETE /api/devolucoes/{id} - Delete return

## Navigation
- Single Activity with Fragments
- Navigation component for fragment transactions
- Bottom navigation for main sections
