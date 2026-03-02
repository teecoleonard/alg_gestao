# 🏗️ ALG Gestão — Sistema Completo de Locação e Contratos

Aplicativo Android nativo (100% Kotlin) para operar todo o ciclo de locação de equipamentos da ALG: prospecção, contratos, logística, devoluções, financeiro e analytics. Este README centraliza **toda a documentação funcional e técnica** do app.

---

## 🔄 Ciclo Operacional do Negócio

| Etapa | Onde acontece | Persistência | O que é disparado |
|-------|---------------|--------------|-------------------|
| **1. Criar Cliente** | `ClientesFragment` → `CadastroClienteDialogFragment` | `ClienteRepository` chama `ApiService.createCliente()` e sincroniza `AppDatabase` | Histórico do cliente passa a ser exibido em `ClientDetailsFragment` |
| **2. Criar Contrato** | `ContratosFragment` → `CadastroContratoDialogFragment` + `EquipamentoContratoDialogFragment` | `ContractRepository` envia payload completo com cliente, equipamentos e período; Room guarda rascunho offline | API gera `Contrato`, `EquipamentoContrato` e devoluções pendentes atomizadas |
| **3. Coletar Assinaturas** | `SignatureCaptureFragment` + `PdfService` | Arquivo da assinatura é convertido em base64, armazenado via `/api/contratos/:numero/assinatura` | `status_assinatura` e `status_contrato` passam a `ASSINADO` |
| **4. Gerar Devolução** | `DevolucoesFragment` → `DevolucaoDetailsDialogFragment` | `DevolucaoRepository` obtém itens criados automaticamente no contrato; operador define quantidades recebidas | Atualiza `Devolucao` e status por item, alimentando dashboards de atraso |
| **5. Devolver Equipamento** | Mesma tela de devolução, botão “Confirmar devolução” | `EquipamentoContratoRepository` ajusta estoque disponível | Se todos os itens forem devolvidos, `StatusContrato` sugere finalização |
| **6. Finalizar Contrato** | `AtualizarStatusBottomSheet` acessível pelo card ou diálogo de detalhes | PATCH `/api/contratos/:id/status` via `ContratoRepository` | `ContratosFragment` move registro para aba “Arquivados” após 6 meses |

---

## 🏛️ Arquitetura de Desenvolvimento

- **Camadas MVVM + Repository** (`ui/*`, `viewmodel`, `data/repository`): separa apresentação, regras de negócio e acesso a dados.
- **Retrofit + OkHttp** (`data/api/ApiClient.kt`, `AuthInterceptor.kt`) com headers dinâmicos (token + seleção de banco).
- **Room Database** (`data/db`) para cache local dos widgets do dashboard, filtros recentes e relatórios offline.
- **Coroutines + Flow** em todos os repositórios para lidar com estados `Loading/Success/Error` modelados por `utils/Resource`.
- **Gestão de sessão** via `SessionManager` + `AuthRepository`: refresh silencioso e logout forçado em 401.
- **Serviços especializados**:
  - `PdfService` e `PdfViewerFragment` geram/visualizam contratos.
  - `ReportService` produz relatórios financeiros em segundo plano.
  - `NotificationManager` + `DashboardViewModel` exibem alertas de contratos vencidos.

---

## 🧱 Organização dos Módulos

```
app/src/main/java/com/example/alg_gestao_02/
├── auth/ (LoginActivity, RegisterActivity, AuthRepository)
├── dashboard/ (DashboardFragment, DashboardViewModel, cards com métricas)
├── ui/
│   ├── cliente/ (CRUD completo + ClientDetailsFragment)
│   ├── contrato/ e contrato/adapter (cards, detalhes, assinatura, PDF)
│   ├── equipamento/ (cadastro, estoque, detalhes rápidos)
│   ├── devolucao/ (workflow de recebimento e confirmação)
│   ├── financial/ (Receita por cliente, Resumo mensal, filtros avançados)
│   └── common/ (BaseFragment, dialogs, tratamento de erros)
├── data/
│   ├── api/ (ApiService, interceptors, DTOs, integração ViaCEP)
│   ├── db/  (AppDatabase, DAOs de cache)
│   ├── models/ (contratos, devoluções, métricas, enums)
│   └── repository/ (fachadas assíncronas chamadas pelos ViewModels)
├── service/ (PdfService, ReportService)
├── manager/ (NotificationManager)
└── utils/ (CurrencyUtils, FilterManager, SessionManager, PdfUtils, etc.)
```

---

## 🔐 Autenticação e Segurança

- Login e registro com `LoginViewModel`/`RegisterViewModel` e validação de CPF/CNPJ (`TextMaskUtils`).
- Sessões persistem por 30 dias via `SessionManager`; tokens ficam em `EncryptedSharedPreferences`.
- `AuthInterceptor` injeta `Authorization: Bearer <token>` e trata código 401 para forçar logout seguro.
- Controles de permissão por perfil (admin x operador) refletem nas opções de UI (ex.: arquivar contrato).

---

## 📊 Dashboards e Financeiro

- `DashboardFragment` consome `/api/dashboard/resumo` e popula cards para receita, contratos ativos, equipamentos e devoluções. Os dados são armazenados em Room para fallback offline.
- `FinancialFragment` e `ReceitaClientesFragment` usam `FinancialViewModel` com filtros:
  - Diálogo “Escolher período” (versão 1.1.0) decide entre mês atual, período customizado ou “último com dados”.
  - Filtragem local garante consistência mesmo se a API retornar dados fora do range solicitado.
- `ResumoMensalClienteActivity` mostra detalhamento e gera PDFs com `ReportService`.

---

## 📄 PDF, Assinaturas e Status

- `SignatureCaptureFragment` captura a assinatura do cliente usando `SignatureView`, converte para base64 e envia para `ContratoRepository`.
- `PdfService` gera contratos em PDF, armazenando-os localmente com `FileProvider` para compartilhar.
- `ContratosAdapter` exibe badges de status (cores/ícones definidos em `data/models/StatusContrato.kt`) conforme a implementação descrita em `STATUS_CONTRATO_UI_IMPLEMENTADO.md`.
- `AtualizarStatusBottomSheet` valida transições (`PENDENTE → ASSINADO → EM_ANDAMENTO → FINALIZADO`) e sincroniza via API.

---

## 📦 Devoluções e Estoque

- Ao criar um contrato, a API gera automaticamente um item de devolução por equipamento. O app consome em `DevolucoesFragment`.
- Cada item tem status próprio (pendente, parcial, concluído). Operador informa quantidades recebidas, fotos e observações.
- `DevolucaoRepository` sincroniza devoluções e atualiza equipamentos vinculados, refletindo disponibilidade em `EquipamentosFragment`.

---

## 🔁 Interação com a API `api-sql`

- Todas as chamadas HTTP partem de `data/api/ApiService.kt` usando Retrofit. Principais endpoints:
  - `POST /api/auth/login`, `POST /api/auth/register`
  - `GET/POST /api/clientes`, `GET /api/clientes/:id`
  - `GET/POST /api/contratos`, `POST /api/contratos/:id/arquivar`
  - `GET/POST /api/devolucoes`, `POST /api/devolucoes/:id/finalizar`
  - `GET /api/dashboard/resumo`, `GET /api/financial/receita-clientes`
- O app envia `X-Database` quando o operador escolhe trabalhar em modo teste (útil para treinamentos). O middleware `databaseContext` da API seleciona o banco correto.

---

## ⚙️ Build, Configuração e DevOps

1. **Pré-requisitos**
   - Android Studio Giraffe+, JDK 17 (`build.gradle.kts` já configurado com `compileSdk=34`).
   - Dispositivo/Emulador com Android 7.0+ e 2 GB RAM.

2. **Configurar endpoint**
   - Ajuste `ApiClient.BASE_URL` em `data/api/ApiService.kt`.
   - Defina `BuildConfig.BACKEND_URL` via `gradle.properties` se quiser separar builds `debug`/`release`.

3. **Build e testes**
   ```bash
   ./gradlew clean assembleDebug
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

4. **Observabilidade**
   - `LogAnalyzer` concentra logs do app.
   - `ErrorHandler` padroniza mensagens e fallback visual.

---

## 🧪 Estratégia de Testes

- **Unitários**: ViewModels e repositórios com mocks do `ApiService` (use `./gradlew test`).
- **Instrumentação**: Fluxo completo (login → criação de contrato → devolução) via Espresso (`./gradlew connectedAndroidTest`).
- **Testes manuais guiados**: siga o ciclo operacional descrito no topo; cada etapa possui checklists em `STATUS_CONTRATO_UI_IMPLEMENTADO.md` e `NOVO_SISTEMA_CONTRATOS.md`.

---

## 🗂️ Referências Rápidas

- `NOVO_SISTEMA_CONTRATOS.md`: detalha abas, filtros, arquivamento e endpoints envolvidos.
- `STATUS_CONTRATO_UI_IMPLEMENTADO.md`: documentação visual do badge e fluxo de alteração de status.
- `CHANGELOG.md`: histórico de releases (1.0.0 → 1.1.0).

---

## 👨‍💻 Autor

**Leonardo Henrique**  
📧 [leonardo4q@gmail.com](mailto:leonardo4q@gmail.com)  
🐙 [@teecoleonard](https://github.com/teecoleonard)  
💼 [LinkedIn](https://www.linkedin.com/in/leonardohenriquedejesussilva/)

---
