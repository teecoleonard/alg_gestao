# ALG Gestão

Aplicativo Android completo para gestão de contratos, clientes e equipamentos de locação.

## 🚀 Funcionalidades Principais

### 📊 Dashboard
- Visão geral de contratos, clientes e equipamentos
- Cards informativos com navegação rápida
- Pull-to-refresh para atualização de dados
- Interface moderna com Material 3

### 👥 Gestão de Clientes  
- Cadastro completo de clientes
- Busca e filtragem por nome
- Visualização de detalhes e histórico
- Contratos e devoluções associados

### 📋 Contratos
- Criação e edição de contratos
- Associação de equipamentos
- Cálculo automático de valores
- Gerenciamento de períodos e locais de obra
- Sistema de assinaturas digitais

### 🔧 Equipamentos
- Cadastro de equipamentos para locação
- Controle de disponibilidade
- Informações técnicas (potência, capacidade, dimensões)
- Histórico de uso e manutenções
- Valores de aquisição e depreciação

### 📦 Devoluções
- Controle de itens devolvidos
- Status: Pendente, Devolvido, Avariado, Faltante
- Rastreamento por número de devolução
- Histórico completo de devoluções

### 🔐 Autenticação
- Sistema de login seguro
- Gerenciamento de sessões
- Diferentes níveis de acesso

## 🛠️ Tecnologias

- **Linguagem**: Kotlin
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **Interface**: Material 3 Design
- **Banco**: Room Database
- **API**: Retrofit + OkHttp
- **Navegação**: Navigation Component
- **Concorrência**: Coroutines + Flow
- **Outras**: ViewBinding, Lottie, SwipeRefreshLayout

## 📱 Requisitos

- Android 7.0+ (API 24)
- Internet para sincronização
- Permissões: INTERNET, ACCESS_NETWORK_STATE

## 🚀 Como executar

1. **Clone o projeto**
   ```bash
   git clone [https://github.com/teecoleonard/alg_gestao.git]
   cd alg_gestao
   ```

2. **Abra no Android Studio**
   - Android Studio Giraffe+ recomendado
   - JDK 17

3. **Execute**
   - Sincronize o Gradle
   - Execute no dispositivo/emulador

## 📁 Estrutura do Projeto

```
app/src/main/java/com.example.alg_gestao_02/
├── auth/                 # Autenticação
├── dashboard/            # Dashboard principal  
├── ui/
│   ├── cliente/         # Módulo de clientes
│   ├── contrato/        # Módulo de contratos
│   ├── equipamento/     # Módulo de equipamentos
│   └── devolucao/       # Módulo de devoluções
├── data/
│   ├── api/             # Serviços de API
│   ├── db/              # Room Database
│   ├── models/          # Modelos de dados
│   └── repository/      # Repositórios
└── utils/               # Utilitários
```

## 🎨 Design

Interface moderna seguindo as diretrizes do Material 3:
- Cores: Azul primário (#3843FF) e Verde secundário (#4CD080)
- Tipografia: Fonte Poppins
- Componentes: Cards, FABs, Bottom Sheets, Dialogs

---

**ALG Gestão** - Solução completa para gestão de locações
