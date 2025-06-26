# 🏗️ ALG Gestão - Sistema Completo de Gestão de Locação

[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://developer.android.com/about/versions/nougat/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Aplicativo Android completo para gestão de contratos, clientes e equipamentos de locação com interface moderna e funcionalidades avançadas.**

---

## 🚀 Funcionalidades Principais

### 🎨 **Interface & UX**
- **Splash Screen** animado com transições suaves
- **Design Material 3** com tipografia Poppins
- **Animações** fluidas em toda a aplicação
- **Navigation Drawer** com menu lateral completo
- **Dark/Light Theme** support (futuro)

### 🔐 **Sistema de Autenticação**
- **Login seguro** com validação CPF
- **Cadastro de usuários** com níveis de acesso
- **Gerenciamento de sessões** automático (30 dias)
- **Logout** com limpeza completa de dados
- **Recuperação de sessão** automática
- **Máscara de CPF** inteligente

### 📊 **Dashboard Avançado**
- **Visão geral** em tempo real do negócio
- **Cards informativos** com métricas importantes:
  - Receita total mensal
  - Contratos ativos
  - Equipamentos disponíveis
  - Devoluções pendentes
- **Pull-to-refresh** para atualização
- **Navegação rápida** entre módulos
- **Estatísticas visuais** e indicadores

### 👥 **Gestão Completa de Clientes**
- **Cadastro detalhado** de clientes pessoa física/jurídica
- **Validação automática** de CPF/CNPJ
- **Busca avançada** e filtragem por nome
- **Histórico completo** de relacionamento
- **Detalhes do cliente** com informações consolidadas:
  - Contratos associados
  - Histórico de devoluções
  - Status de pagamentos
- **Edição** e **exclusão** de registros
- **Navegação contextual** para contratos do cliente

### 📋 **Sistema de Contratos Robusto**
- **Criação de contratos** com wizard intuitivo
- **Associação múltipla** de equipamentos
- **Cálculo automático** de valores e períodos
- **Gerenciamento de locais** de obra e entrega
- **Sistema de assinaturas digitais** integrado:
  - Captura de assinatura via touch
  - Validação de assinatura
  - Contratos assinados vs pendentes
- **Geração de PDF** automática dos contratos
- **Edição completa** de contratos existentes
- **Status tracking** (Ativo, Pendente, Assinado, etc.)

### 🔧 **Controle de Equipamentos**
- **Cadastro completo** de equipamentos para locação
- **Informações técnicas** detalhadas:
  - Potência e capacidade
  - Dimensões físicas
  - Especificações técnicas
- **Controle de disponibilidade** em tempo real
- **Histórico de uso** e manutenções
- **Valores de aquisição** e depreciação
- **Status** (Disponível, Locado, Manutenção, etc.)
- **Associação inteligente** aos contratos

### 📦 **Sistema de Devoluções Avançado**
- **Controle detalhado** de itens devolvidos
- **Status múltiplos**: Pendente, Devolvido, Avariado, Faltante
- **Rastreamento** por número de devolução
- **Processamento de devoluções** com:
  - Quantidade devolvida
  - Observações técnicas
  - Data e hora efetiva
- **Histórico completo** de todas as devoluções
- **Integração** com contratos e equipamentos
- **Alertas automáticos** para devoluções em atraso

### 💰 **Módulo Financeiro Completo**
- **Dashboard financeiro** com métricas em tempo real
- **Relatórios detalhados** por período
- **Análise de receitas** e tendências
- **Controle de inadimplência**
- **Geração de relatórios PDF** financeiros
- **Exportação** e **compartilhamento** de dados
- **Gráficos visuais** de performance

### 📄 **Sistema de PDF & Documentos**
- **Geração automática** de PDFs para:
  - Contratos completos
  - Comprovantes de devolução
  - Relatórios financeiros
- **Visualizador de PDF** integrado
- **Compartilhamento** via apps externos
- **Armazenamento** local e em nuvem
- **Templates** personalizáveis

### 🔔 **Sistema de Notificações**
- **Notificações inteligentes** para:
  - Novos contratos criados
  - Clientes cadastrados
  - Equipamentos disponíveis
  - Devoluções pendentes/concluídas
- **Painel de notificações** no drawer
- **Contadores** de notificações não lidas
- **Histórico** completo de notificações
- **Marcar como lida** individual ou em lote

### 🛠️ **Recursos Técnicos Avançados**
- **Integração com API REST** robusta
- **Cache inteligente** de dados
- **Sincronização** automática em background
- **Validação** de campos em tempo real
- **Máscaras automáticas** para CPF, CNPJ, telefone
- **Filtros avançados** em todas as listagens
- **Busca semântica** por texto
- **Estados de loading** e error handling
- **Logs detalhados** para debugging

---

## 🛠️ Tecnologias e Arquitetura

### **Core Technologies**
- **Linguagem**: Kotlin 100%
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **Interface**: Material 3 Design + View Binding
- **Banco Local**: Room Database com TypeConverters
- **API**: Retrofit + OkHttp + Gson
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
│   (UI/ViewModels)│◄──►│   (Use Cases)   │◄──►│  (Repositories) │
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
git clone https://github.com/seu-usuario/alg_gestao.git
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
├── 🔐 auth/                    # Sistema de autenticação
│   ├── LoginActivity.kt        # Tela de login
│   ├── LoginViewModel.kt       # Lógica de autenticação
│   ├── RegisterActivity.kt     # Cadastro de usuários
│   └── RegisterViewModel.kt    # Lógica de registro
│
├── 📊 dashboard/              # Dashboard principal
│   ├── DashboardActivity.kt   # Activity principal
│   ├── DashboardFragment.kt   # Fragment do dashboard
│   └── fragments/             # Sub-fragments do dashboard
│
├── 🎨 ui/                     # Interface do usuário
│   ├── 👥 cliente/           # Módulo de clientes
│   │   ├── ClienteFragment.kt # Lista de clientes
│   │   ├── ClientDetailsFragment.kt # Detalhes do cliente
│   │   ├── CadastroClienteDialogFragment.kt # Cadastro
│   │   └── viewmodel/         # ViewModels do módulo
│   │
│   ├── 📋 contrato/          # Módulo de contratos
│   │   ├── ContratosFragment.kt # Lista de contratos
│   │   ├── ContratoDetailsDialogFragment.kt # Detalhes
│   │   ├── CadastroContratoDialogFragment.kt # Cadastro
│   │   ├── SignatureCaptureFragment.kt # Captura de assinatura
│   │   ├── PdfViewerFragment.kt # Visualizador de PDF
│   │   └── adapter/           # Adapters personalizados
│   │
│   ├── 🔧 equipamento/       # Módulo de equipamentos
│   │   ├── EquipamentosFragment.kt # Lista de equipamentos
│   │   ├── EquipamentoDetailsDialogFragment.kt # Detalhes
│   │   ├── CadastroEquipamentoDialogFragment.kt # Cadastro
│   │   └── viewmodel/         # ViewModels do módulo
│   │
│   ├── 📦 devolucao/         # Módulo de devoluções
│   │   ├── DevolucoesFragment.kt # Lista de devoluções
│   │   ├── DevolucaoDetailsDialogFragment.kt # Detalhes
│   │   └── viewmodel/         # ViewModels do módulo
│   │
│   ├── 💰 financial/         # Módulo financeiro
│   │   ├── FinancialFragment.kt # Dashboard financeiro
│   │   ├── ReportFragment.kt  # Relatórios
│   │   └── viewmodel/         # ViewModels financeiros
│   │
│   └── 🔧 common/            # Componentes comuns
│       ├── LoadingDialog.kt   # Dialog de carregamento
│       ├── ConfirmDialog.kt   # Confirmações
│       └── BaseFragment.kt    # Fragment base
│
├── 🗄️ data/                   # Camada de dados
│   ├── 🌐 api/               # Serviços de API
│   │   ├── ApiService.kt      # Interface da API
│   │   ├── ApiConfig.kt       # Configurações
│   │   ├── AuthInterceptor.kt # Interceptador de auth
│   │   └── NetworkUtils.kt    # Utilitários de rede
│   │
│   ├── 🗃️ db/                # Banco de dados local
│   │   ├── AppDatabase.kt     # Configuração do Room
│   │   ├── entities/          # Entidades do banco
│   │   └── dao/               # Data Access Objects
│   │
│   ├── 📋 models/            # Modelos de dados
│   │   ├── User.kt           # Modelo de usuário
│   │   ├── Cliente.kt        # Modelo de cliente
│   │   ├── Contrato.kt       # Modelo de contrato
│   │   ├── Equipamento.kt    # Modelo de equipamento
│   │   ├── Devolucao.kt      # Modelo de devolução
│   │   └── FinancialMetrics.kt # Métricas financeiras
│   │
│   └── 🔄 repository/        # Repositórios
│       ├── ClienteRepository.kt # Lógica de clientes
│       ├── ContratoRepository.kt # Lógica de contratos
│       ├── EquipamentoRepository.kt # Lógica de equipamentos
│       └── DevolucaoRepository.kt # Lógica de devoluções
│
├── 🛠️ service/               # Serviços especializados
│   ├── PdfService.kt         # Geração de PDFs
│   ├── ReportService.kt      # Relatórios
│   └── NotificationService.kt # Notificações
│
├── 🔔 manager/               # Gerenciadores
│   ├── NotificationManager.kt # Gestor de notificações
│   └── SessionManager.kt     # Gestor de sessões
│
├── 🎛️ utils/                 # Utilitários
│   ├── SessionManager.kt     # Gerenciamento de sessão
│   ├── TextMaskUtils.kt      # Máscaras de texto
│   ├── ValidationUtils.kt    # Validações
│   ├── DateUtils.kt          # Manipulação de datas
│   ├── CurrencyUtils.kt      # Formatação monetária
│   ├── PdfUtils.kt           # Utilitários PDF
│   ├── ShareUtils.kt         # Compartilhamento
│   ├── LogUtils.kt           # Sistema de logs
│   └── NetworkUtils.kt       # Utilitários de rede
│
└── 🧩 adapter/               # Adapters do RecyclerView
    ├── ClientesAdapter.kt    # Adapter de clientes
    ├── ContratosAdapter.kt   # Adapter de contratos
    ├── EquipamentosAdapter.kt # Adapter de equipamentos
    ├── DevolucoesAdapter.kt  # Adapter de devoluções
    └── NotificationAdapter.kt # Adapter de notificações
```

---

## 🎨 Screenshots e Interface

### **Telas Principais**
```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   Splash    │  │    Login    │  │  Dashboard  │  │   Drawer    │
│     🎬      │→ │     🔐      │→ │     📊      │↔ │     📱      │
└─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘

┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Clientes   │  │  Contratos  │  │Equipamentos │  │ Devoluções  │
│     👥      │  │     📋      │  │     🔧      │  │     📦      │
└─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘
```

### **Fluxos de Trabalho**
1. **Login** → **Dashboard** → **Módulo específico**
2. **Cadastro de Cliente** → **Criação de Contrato** → **Associação de Equipamentos**
3. **Contrato** → **Assinatura Digital** → **Geração de PDF**
4. **Devolução** → **Processamento** → **Relatório PDF**

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

## 📋 Roadmap e Features Futuras

### **v2.0 - Em Desenvolvimento**
- [ ] **Modo Offline** completo
- [ ] **Sincronização** bidirecional
- [ ] **Dashboard Analytics** avançado
- [ ] **Push Notifications** remotas
- [ ] **Backup automático** para nuvem
- [ ] **Multi-empresa** support
- [ ] **Relatórios** customizáveis
- [ ] **API de integração** para terceiros

### **v2.1 - Planejado**
- [ ] **App para Tablet** otimizado
- [ ] **Modo escuro** completo
- [ ] **Biometria** para login
- [ ] **Integração** com sistemas ERP
- [ ] **Workflow** de aprovações
- [ ] **Chat** interno entre usuários

---

## 🤝 Contribuição

### **Como Contribuir**
1. **Fork** o projeto
2. **Crie** uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. **Commit** suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. **Push** para a branch (`git push origin feature/nova-feature`)
5. **Abra** um Pull Request

### **Padrões de Código**
- **Kotlin Coding Conventions**
- **MVVM Architecture**
- **Clean Code** principles
- **Documentation** em português
- **Commit messages** semânticos

---

## 📄 Licença

Este projeto está licenciado sob a **MIT License** - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 👨‍💻 Autor

**Leonardo Henrique**  
📧 Email: [seu-email@example.com](mailto:seu-email@example.com)  
🐙 GitHub: [@teecoleonard](https://github.com/teecoleonard)  
💼 LinkedIn: [Leonardo Henrique](https://linkedin.com/in/leonardo-henrique)

---

## 🙏 Agradecimentos

- **Material Design Team** pela inspiração visual
- **Android Jetpack** pelos componentes robustos
- **Comunidade Kotlin** pelo suporte contínuo
- **Equipe de teste** pelos feedbacks valiosos

---

<div align="center">
  <h3>🚀 ALG Gestão - Gestão Inteligente de Locação 🚀</h3>
  <p><em>Transformando a gestão de equipamentos com tecnologia moderna</em></p>
  
  [![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/teecoleonard/alg_gestao)
  [![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)](https://github.com/teecoleonard/alg_gestao/releases)
  [![Stars](https://img.shields.io/github/stars/teecoleonard/alg_gestao.svg)](https://github.com/teecoleonard/alg_gestao/stargazers)
</div>
