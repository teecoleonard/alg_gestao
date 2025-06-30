# 🏗️ ALG Gestão - Gestão de Locação de Equipamentos

[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://developer.android.com/about/versions/nougat/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Aplicativo Android completo para gestão de contratos, clientes e equipamentos de locação com interface moderna e funcionalidades avançadas.**

<!-- 📸 ADICIONAR AQUI: Banner principal ou logo do aplicativo -->
![ALG Gestão Banner](screenshots/banner.png)

---
 
## 🚀 Funcionalidades Principais

### 🎨 **Interface & UX**
- Splash Screen animado com Material 3 Design
- Tipografia Poppins e animações fluidas
- Navigation Drawer completo

<!-- 📸 ADICIONAR AQUI: Screenshots do Splash Screen e Interface principal -->
<div align="center">
  <img src="screenshots/splash_screen.png" width="250" alt="Splash Screen"/>
  <img src="screenshots/main_interface.png" width="250" alt="Interface Principal"/>
  <img src="screenshots/navigation_drawer.png" width="250" alt="Navigation Drawer"/>
</div>

### 🔐 **Autenticação**
- Login seguro com validação CPF e máscara automática
- Gerenciamento de sessões (30 dias) e recuperação automática
- Cadastro de usuários com diferentes níveis de acesso

<!-- 📸 ADICIONAR AQUI: Telas de Login e Cadastro -->
<div align="center">
  <img src="screenshots/login_screen.png" width="250" alt="Tela de Login"/>
  <img src="screenshots/register_screen.png" width="250" alt="Tela de Cadastro"/>
</div>

### 📊 **Dashboard**
- Visão geral em tempo real com métricas do negócio
- Cards informativos: receita, contratos ativos, equipamentos, devoluções
- Pull-to-refresh e navegação rápida entre módulos

<!-- 📸 ADICIONAR AQUI: Dashboard principal com métricas -->
<div align="center">
  <img src="screenshots/dashboard_main.png" width="300" alt="Dashboard Principal"/>
  <img src="screenshots/dashboard_metrics.png" width="300" alt="Métricas do Dashboard"/>
</div>

### 👥 **Gestão de Clientes**
- Cadastro completo PF/PJ com validação CPF/CNPJ
- Busca avançada, histórico de relacionamento
- Detalhes consolidados com contratos e devoluções associados

<!-- 📸 ADICIONAR AQUI: Telas de gestão de clientes -->
<div align="center">
  <img src="screenshots/clients_list.png" width="250" alt="Lista de Clientes"/>
  <img src="screenshots/client_details.png" width="250" alt="Detalhes do Cliente"/>
  <img src="screenshots/client_form.png" width="250" alt="Cadastro de Cliente"/>
</div>

### 📋 **Sistema de Contratos**
- Criação com wizard intuitivo e associação múltipla de equipamentos
- Assinaturas digitais via touch com validação
- Geração automática de PDF e controle de status

<!-- 📸 ADICIONAR AQUI: Sistema de contratos e assinaturas -->
<div align="center">
  <img src="screenshots/contracts_list.png" width="200" alt="Lista de Contratos"/>
  <img src="screenshots/contract_wizard.png" width="200" alt="Wizard de Criação"/>
  <img src="screenshots/signature_capture.png" width="200" alt="Captura de Assinatura"/>
  <img src="screenshots/contract_pdf.png" width="200" alt="PDF do Contrato"/>
</div>

### 🔧 **Controle de Equipamentos**
- Cadastro detalhado com especificações técnicas
- Controle de disponibilidade em tempo real
- Histórico, valores e associação inteligente aos contratos

<!-- 📸 ADICIONAR AQUI: Gestão de equipamentos -->
<div align="center">
  <img src="screenshots/equipment_list.png" width="250" alt="Lista de Equipamentos"/>
  <img src="screenshots/equipment_details.png" width="250" alt="Detalhes do Equipamento"/>
  <img src="screenshots/equipment_form.png" width="250" alt="Cadastro de Equipamento"/>
</div>

### 📦 **Sistema de Devoluções**
- Controle com múltiplos status e rastreamento
- Processamento detalhado com observações
- Alertas automáticos para devoluções em atraso

### 💰 **Módulo Financeiro**
- Dashboard com métricas em tempo real
- Receita por cliente com filtros avançados (nome, status de pagamento)
- Notificações educativas para diferenças entre valor bruto vs receita real
- Relatórios detalhados e análise de tendências
- Geração de PDFs e exportação de dados

<!-- 📸 ADICIONAR AQUI: Módulo financeiro e relatórios -->
<div align="center">
  <img src="screenshots/financial_dashboard.png" width="300" alt="Dashboard Financeiro"/>
  <img src="screenshots/financial_reports.png" width="300" alt="Relatórios Financeiros"/>
</div>

### 📄 **PDFs & Documentos**
- Geração automática para contratos, devoluções e relatórios
- Visualizador integrado e compartilhamento
- Templates personalizáveis

### 🔔 **Notificações**
- Sistema inteligente para eventos importantes
- Notificações educativas não intrusivas para explicar funcionalidades
- Painel no drawer com contadores e histórico
- Controle individual e em lote

---

## 🛠️ Tecnologias e Arquitetura

### **Core Technologies**
- **Linguagem**: Kotlin 100%
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **Interface**: Material 3 Design + View Binding
- **Banco Local**: Room Database com TypeConverters
- **API**: Retrofit + OkHttp + Gson com tratamento robusto de erros
- **Navegação**: Navigation Component + Safe Args

### **Libraries & Components**
- **Concorrência**: Coroutines + Flow + LiveData
- **Animações**: Lottie + Android Animations
- **UI Components**: Material Design Components 3
- **File Handling**: FileProvider + MediaStore
- **PDF Generation**: External API + Base64 encoding
- **Networking**: Retrofit2 + Interceptors
- **Storage**: SharedPreferences + Room Database

### **Architecture Pattern**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Presentation  │    │     Domain      │    │      Data       │
│  (UI/ViewModels)│◄──►│   (Use Cases)   │◄──►│  (Repositories) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                                              │
         ▼                                              ▼
┌─────────────────┐                          ┌─────────────────┐
│   Navigation    │                          │   API Service   │
│   & Fragments   │                          │   & Database    │
└─────────────────┘                          └─────────────────┘
```

---

## 📱 Requisitos do Sistema

### **Dispositivo**
- **Android**: 7.0+ (API level 24)
- **RAM**: Mínimo 2GB recomendado
- **Armazenamento**: 100MB livres
- **Internet**: Conexão estável para sincronização

### **Permissões**
- `INTERNET` - Para comunicação com API
- `ACCESS_NETWORK_STATE` - Verificação de conectividade
- `READ_EXTERNAL_STORAGE` - Leitura de arquivos
- `WRITE_EXTERNAL_STORAGE` - Salvamento de PDFs (Android ≤ 28)
- `MANAGE_EXTERNAL_STORAGE` - Acesso total (Android 11+)

---

## 🚀 Instalação e Configuração

### **1. Clone o Repositório**
```bash
git clone https://github.com/teecoleonard/alg_gestao.git
cd alg_gestao
```

### **2. Configuração do Ambiente**
- **Android Studio**: Giraffe (2022.3.1) ou superior
- **JDK**: OpenJDK 17
- **Gradle**: 8.0+
- **Kotlin**: 1.9.0+

### **3. Configuração da API**
```kotlin
// Em data/api/ApiConfig.kt
object ApiConfig {
    const val BASE_URL = "https://sua-api.com/"
    const val PDF_SERVICE_URL = "https://seu-servico-pdf.com/"
}
```

### **4. Build e Execução**
```bash
# Sincronizar dependências
./gradlew sync

# Build debug
./gradlew assembleDebug

# Executar testes
./gradlew test
```

---

## 📁 Estrutura Detalhada do Projeto

```
app/src/main/java/com.example.alg_gestao_02/
├── 🔐 auth/                                 # Sistema de autenticação
│   ├── LoginActivity.kt                      # Tela de login
│   ├── LoginViewModel.kt                     # Lógica de autenticação
│   ├── RegisterActivity.kt                   # Cadastro de usuários
│   └── RegisterViewModel.kt                  # Lógica de registro
│
├── 📊 dashboard/                           # Dashboard principal
│   ├── DashboardActivity.kt                 # Activity principal
│   ├── DashboardFragment.kt                 # Fragment do dashboard
│   └── fragments/                           # Sub-fragments do dashboard
│
├── 🎨 ui/                                  # Interface do usuário
│   ├── 👥 cliente/                         # Módulo de clientes
│   │   ├── ClienteFragment.kt               # Lista de clientes
│   │   ├── ClientDetailsFragment.kt         # Detalhes do cliente
│   │   ├── CadastroClienteDialogFragment.kt # Cadastro
│   │   └── viewmodel/                       # ViewModels do módulo
│   │
│   ├── 📋 contrato/                         # Módulo de contratos
│   │   ├── ContratosFragment.kt              # Lista de contratos
│   │   ├── ContratoDetailsDialogFragment.kt  # Detalhes
│   │   ├── CadastroContratoDialogFragment.kt # Cadastro
│   │   ├── SignatureCaptureFragment.kt       # Captura de assinatura
│   │   ├── PdfViewerFragment.kt              # Visualizador de PDF
│   │   └── adapter/                          # Adapters personalizados
│   │
│   ├── 🔧 equipamento/                         # Módulo de equipamentos
│   │   ├── EquipamentosFragment.kt              # Lista de equipamentos
│   │   ├── EquipamentoDetailsDialogFragment.kt  # Detalhes
│   │   ├── CadastroEquipamentoDialogFragment.kt # Cadastro
│   │   └── viewmodel/                           # ViewModels do módulo
│   │
│   ├── 📦 devolucao/                        # Módulo de devoluções
│   │   ├── DevolucoesFragment.kt             # Lista de devoluções
│   │   ├── DevolucaoDetailsDialogFragment.kt # Detalhes
│   │   └── viewmodel/                        # ViewModels do módulo
│   │
│   ├── 💰 financial/                       # Módulo financeiro
│   │   ├── FinancialFragment.kt             # Dashboard financeiro
│   │   ├── ReceitaClientesFragment.kt       # Receita por cliente com filtros
│   │   ├── ReportFragment.kt                # Relatórios
│   │   └── viewmodel/                       # ViewModels financeiros
│   │
│   └── 🔧 common/                          # Componentes comuns
│       ├── LoadingDialog.kt                 # Dialog de carregamento
│       ├── ConfirmDialog.kt                 # Confirmações
│       └── BaseFragment.kt                  # Fragment base
│
├── 🗄️ data/                                # Camada de dados
│   ├── 🌐 api/                             # Serviços de API
│   │   ├── ApiService.kt                    # Interface da API
│   │   ├── ApiConfig.kt                     # Configurações
│   │   ├── AuthInterceptor.kt               # Interceptador de auth
│   │   └── NetworkUtils.kt                  # Utilitários de rede
│   │
│   ├── 🗃️ db/                             # Banco de dados local
│   │   ├── AppDatabase.kt                  # Configuração do Room
│   │   ├── entities/                       # Entidades do banco
│   │   └── dao/                            # Data Access Objects
│   │
│   ├── 📋 models/                        # Modelos de dados
│   │   ├── User.kt                        # Modelo de usuário
│   │   ├── Cliente.kt                     # Modelo de cliente
│   │   ├── Contrato.kt                    # Modelo de contrato
│   │   ├── Equipamento.kt                 # Modelo de equipamento
│   │   ├── Devolucao.kt                   # Modelo de devolução
│   │   └── FinancialMetrics.kt            # Métricas financeiras
│   │
│   └── 🔄 repository/                    # Repositórios
│       ├── ClienteRepository.kt           # Lógica de clientes
│       ├── ContratoRepository.kt          # Lógica de contratos
│       ├── EquipamentoRepository.kt       # Lógica de equipamentos
│       └── DevolucaoRepository.kt         # Lógica de devoluções
│
├── 🛠️ service/                           # Serviços especializados
│   ├── PdfService.kt                      # Geração de PDFs
│   ├── ReportService.kt                   # Relatórios
│   └── NotificationService.kt             # Notificações
│
├── 🔔 manager/                           # Gerenciadores
│   ├── NotificationManager.kt             # Gestor de notificações
│   └── SessionManager.kt                  # Gestor de sessões
│
├── 🎛️ utils/                             # Utilitários
│   ├── SessionManager.kt                  # Gerenciamento de sessão
│   ├── TextMaskUtils.kt                   # Máscaras de texto
│   ├── ValidationUtils.kt                 # Validações
│   ├── DateUtils.kt                       # Manipulação de datas
│   ├── CurrencyUtils.kt                   # Formatação monetária
│   ├── PdfUtils.kt                        # Utilitários PDF
│   ├── ShareUtils.kt                      # Compartilhamento
│   ├── LogUtils.kt                        # Sistema de logs
│   └── NetworkUtils.kt                    # Utilitários de rede
│
└── 🧩 adapter/                           # Adapters do RecyclerView
    ├── ClientesAdapter.kt                 # Adapter de clientes
    ├── ContratosAdapter.kt                # Adapter de contratos
    ├── EquipamentosAdapter.kt             # Adapter de equipamentos
    ├── DevolucoesAdapter.kt               # Adapter de devoluções
    └── NotificationAdapter.kt             # Adapter de notificações
```

---

## 📸 Screenshots e Galeria

### **Visão Geral da Interface**
<!-- 📸 ADICIONAR AQUI: Montagem com todas as telas principais -->
<div align="center">
  <img src="screenshots/app_overview.png" width="800" alt="Visão Geral do ALG Gestão"/>
</div>

### **Fluxo Completo de Trabalho**
<!-- 📸 ADICIONAR AQUI: Sequência do fluxo principal -->
<div align="center">
  <img src="screenshots/workflow_complete.png" width="700" alt="Fluxo Completo de Trabalho"/>
</div>

### **Funcionalidades em Ação**
<!-- 📸 ADICIONAR AQUI: GIFs ou screenshots das principais funcionalidades -->
<div align="center">
  <img src="screenshots/features_demo.gif" width="300" alt="Demo das Funcionalidades"/>
  <img src="screenshots/signature_demo.gif" width="300" alt="Demo de Assinatura"/>
</div>

### **Temas e Responsividade**
<!-- 📸 ADICIONAR AQUI: Screenshots em diferentes tamanhos de tela -->
<div align="center">
  <img src="screenshots/phone_portrait.png" width="200" alt="Celular Vertical"/>
  <img src="screenshots/phone_landscape.png" width="350" alt="Celular Horizontal"/>
  <img src="screenshots/tablet_view.png" width="400" alt="Tablet"/>
</div>

### **Fluxos de Navegação**
```
🎬 Splash → 🔐 Login → 📊 Dashboard → 📱 Navigation Drawer
                                ↓
👥 Clientes ←→ 📋 Contratos ←→ 🔧 Equipamentos ←→ 📦 Devoluções
                                ↓
                        💰 Financeiro ←→ 📄 Relatórios
```

### **Principais Jornadas do Usuário**
1. **🔐 Autenticação** → **📊 Dashboard** → **Navegação rápida**
2. **👥 Cadastro de Cliente** → **📋 Criação de Contrato** → **🔧 Seleção de Equipamentos**
3. **📋 Contrato** → **✍️ Assinatura Digital** → **📄 Geração de PDF**
4. **📦 Devolução** → **✅ Processamento** → **📊 Relatório Financeiro**

---

## 🧪 Testing

### **Testes Implementados**
- **Unit Tests**: ViewModels e Repositories
- **Integration Tests**: API calls e Database
- **UI Tests**: Fluxos principais

### **Executar Testes**
```bash
# Testes unitários
./gradlew test

# Testes instrumentados
./gradlew connectedAndroidTest

# Todos os testes
./gradlew check
```

---

## 📄 Licença

Este projeto está licenciado sob a **MIT License** - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 👨‍💻 Autor

**Leonardo Henrique**  
📧 Email: [leonardo4q@example.com](mailto:leonardo4q@example.com)  
🐙 GitHub: [@teecoleonard](https://github.com/teecoleonard)  
💼 LinkedIn: [Leonardo Henrique](https://www.linkedin.com/in/leonardohenriquedejesussilva/)

---

<div align="center">
  <h3>🚀 ALG Gestão - Gestão Inteligente de Locação 🚀</h3>
  <p><em>Transformando a gestão de equipamentos com tecnologia moderna</em></p>
  
  [![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/teecoleonard/alg_gestao)
  [![Version](https://img.shields.io/badge/Version-1.1.0-blue.svg)](https://github.com/teecoleonard/alg_gestao/releases)
</div>
