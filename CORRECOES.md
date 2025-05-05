# Correções de Problemas no ALG Gestão

## Correção de ClassCastException em ViewModels duplicados

### Problema Detectado
O aplicativo apresentava o seguinte erro:
```
java.lang.ClassCastException: com.example.alg_gestao_02.ui.invoice.viewmodel.ProjectInvoicesViewModel cannot be cast to com.example.alg_gestao_02.ui.project.viewmodel.ProjectInvoicesViewModel
```

Este erro ocorria devido a conflitos entre duas implementações diferentes da classe `ProjectInvoicesViewModel` em pacotes distintos:
- `com.example.alg_gestao_02.ui.invoice.viewmodel.ProjectInvoicesViewModel`
- `com.example.alg_gestao_02.ui.project.viewmodel.ProjectInvoicesViewModel`

### Detalhes da Correção

#### 1. Correção do ProjectInvoicesViewModelFactory
- O problema principal estava no arquivo `ProjectInvoicesViewModelFactory.kt`, que importava o `ProjectInvoicesViewModel` do pacote errado.
- O factory estava no pacote `invoice` mas importava a classe do pacote `project`.
- Corrigimos para importar a classe do mesmo pacote `invoice`.

#### 2. Ajuste do ProjectInvoicesFragment
- O fragment estava referenciando métodos que existiam apenas no ViewModel do pacote `project`.
- Atualizamos as chamadas de método para usar a API disponível no ViewModel do pacote `invoice`:
  - `loadInvoicesByProjectAndMonth(projectId, calendar)` → `loadInvoices(projectId)`
  - `loadPreviousMonth(projectId)` → `loadPreviousMonth()`
  - `loadNextMonth(projectId)` → `loadNextMonth()`

### Recomendações Futuras

Para evitar problemas semelhantes no futuro, recomendamos:

1. **Evitar nomes de classes duplicados** em pacotes diferentes, mesmo que tenham funcionalidades similares
2. **Consolidar implementações similares** em uma única classe com interface mais flexível
3. **Adotar convenções de nomenclatura** que incluam o contexto específico no nome da classe
4. **Realizar revisões de código** para detectar duplicações e importações inconsistentes

### Data da Correção
25/04/2025 

---

## Correção do comportamento do botão voltar no ProjectDetailActivity

### Problema Detectado
Ao pressionar o botão voltar na tela de detalhes do projeto (`ProjectDetailActivity`), a tela mostrava a animação de retorno mas não voltava para a tela anterior na primeira tentativa. Somente após pressionar o botão voltar pela segunda vez a navegação era concluída.

### Detalhes da Correção (Atualização)

Após a primeira tentativa de correção, o problema persistiu, então implementamos uma solução mais robusta:

#### 1. Navegação explícita
- Modificamos o método `finishActivity()` para usar navegação explícita com `Intent` para o `DashboardActivity`
- Adicionamos uma flag `FLAG_ACTIVITY_CLEAR_TOP` para garantir o comportamento correto na pilha de atividades
- Implementamos um delay curto com `Handler.postDelayed()` para garantir que as transições sejam concluídas corretamente

#### 2. Controle de estado
- Adicionamos uma variável `isFinishing` para evitar múltiplas chamadas ao método de finalização
- Aplicamos transições personalizadas com `overridePendingTransition()` para tornar o retorno mais suave

#### 3. Relacionamento Pai-Filho definido no Android Manifest
- Adicionamos `android:parentActivityName=".dashboard.DashboardActivity"` para informar ao sistema Android sobre a hierarquia
- Incluímos uma meta-data para compatibilidade com versões mais antigas do Android
- Configuramos o `DashboardActivity` com `android:launchMode="singleTop"` para evitar instâncias duplicadas

### Recomendações Futuras

1. **Atualizar todas as activities** do aplicativo para usar o novo sistema `OnBackPressedDispatcher`
2. **Evitar o uso direto** de `onBackPressed()` que está deprecado nas versões recentes do Android
3. **Padronizar a navegação** para garantir consistência em toda a aplicação
4. **Certificar-se de sempre definir relacionamentos pai-filho** no manifesto para activities que possuem hierarquia
5. **Considerar o uso de Navigation Component** do Android Jetpack para gerenciar a navegação de forma mais robusta

### Data da Correção
25/04/2025 

---

## Correção da ordem das abas no ProjectDetailActivity

### Problema Detectado
A ordem das abas na tela de detalhes do projeto (`ProjectDetailActivity`) estava inconsistente entre o layout XML e a implementação em Kotlin. O layout XML definia a ordem como:
1. Sumário
2. Contratos
3. Devoluções
4. Faturas

Mas a implementação em Kotlin estava com a ordem:
1. Sumário
2. Contratos
3. Faturas
4. Devoluções

### Detalhes da Correção

#### 1. Correção do TabLayoutMediator
- Modificamos o método `setupViewPager()` para corrigir a ordem dos textos das abas:
```kotlin
TabLayoutMediator(tabLayout, viewPager) { tab, position ->
    tab.text = when (position) {
        0 -> "Sumário"
        1 -> "Contratos"
        2 -> "Devoluções"  // Alterado
        3 -> "Faturas"     // Alterado
        else -> "Tab ${position + 1}"
    }
}.attach()
```

#### 2. Correção do ProjectPagerAdapter
- Ajustamos a ordem dos fragments no `ProjectPagerAdapter`:
```kotlin
override fun createFragment(position: Int): Fragment {
    return when (position) {
        0 -> ProjectSummaryFragment.newInstance(projectId)
        1 -> ProjectContractsFragment.newInstance(projectId)
        2 -> EmptyTabFragment.newInstance("Devoluções")  // Alterado
        3 -> ProjectInvoicesFragment.newInstance(projectId)  // Alterado
        else -> EmptyTabFragment.newInstance("Tab")
    }
}
```

### Recomendações Futuras

1. **Manter consistência** entre layouts XML e implementações em Kotlin
2. **Documentar a ordem** das abas em comentários para facilitar manutenção futura
3. **Considerar usar constantes** para os índices das abas para evitar erros de digitação
4. **Implementar testes** para verificar a ordem correta das abas

### Data da Correção
25/04/2025 

---

## Implementação do padrão MVVM no ProjectSummaryFragment

### Problema Detectado
O `ProjectSummaryFragment` não seguia o padrão de arquitetura MVVM (Model-View-ViewModel) utilizado em outros módulos do aplicativo. O fragmento manipulava diretamente dados mockados na própria classe, misturando responsabilidades de apresentação com lógica de negócios e acesso a dados.

### Data da Correção
28/04/2025

---

### Detalhes da Implementação

#### 1. Criação do Repository (Modelo)
- Criamos a classe `ProjectSummaryRepository` para gerenciar o acesso aos dados
- Movemos os dados mockados do fragmento para o repositório
- Implementamos métodos para buscar, filtrar e pesquisar contratos de projeto
- Preparamos a estrutura para futura integração com APIs reais

#### 2. Criação do ViewModel
- Implementamos a classe `ProjectSummaryViewModel` para gerenciar o estado da UI
- Adicionamos métodos para carregar e filtrar contratos por tipo
- Utilizamos `LiveData` com a classe `UiState` para comunicar diferentes estados da UI (Loading, Success, Error, Empty)
- Criamos uma factory para injeção de dependências ao ViewModel

#### 3. Refatoração do Fragment (View)
- Atualizamos o `ProjectSummaryFragment` para observar o ViewModel
- Removemos a manipulação direta de dados do fragmento
- Adicionamos um `ViewFlipper` para gerenciar diferentes estados visuais
- Implementamos handlers para cada estado da UI (carregamento, vazio, erro, conteúdo)

#### 4. Atualização do Layout
- Adicionamos um `ViewFlipper` com três estados:
  - Lista de contratos (RecyclerView)
  - Estado vazio (mensagem informativa)
  - Estado de carregamento (ProgressBar)
- Conectamos os botões de ação para chamarem os métodos apropriados do ViewModel

### Benefícios da Implementação
- Separação clara de responsabilidades seguindo o padrão MVVM
- Melhor testabilidade dos componentes individuais
- Consistência com o restante da arquitetura do aplicativo
- Preparação para futura integração com dados reais
- Melhor gerenciamento de estados da UI e feedback visual para o usuário

---

## Correções de bugs na implementação MVVM do ProjectSummaryFragment

### Problemas Detectados
Após a implementação inicial do padrão MVVM no `ProjectSummaryFragment`, alguns erros foram identificados durante o processo de build e execução:

1. O recurso `drawable/ic_empty` referenciado no layout não existia
2. Possíveis `NullPointerException` no acesso a elementos de UI
3. Falta de tratamento adequado para erros de inicialização do ViewFlipper

### Detalhes da Correção

#### 1. Criação do resource drawable/ic_empty
- Criamos o arquivo `ic_empty.xml` em `res/drawable/` como um ícone vetorial
- Implementamos um desenho informativo para representar o estado vazio

#### 2. Tratamento de null safety em elementos de UI
- Modificamos o código para usar o operador de chamada segura (`?.`) em todas as operações com views
- Alteramos o `viewFlipper` de `lateinit var` para variável nullable (`var viewFlipper: ViewFlipper? = null`)
- Implementamos blocos `let`/`run` para lidar de forma segura com possíveis valores nulos

#### 3. Melhoria do tratamento de erros
- Adicionamos blocos try/catch na inicialização do ViewFlipper
- Implementamos verificações de null para o RecyclerView e outros elementos
- Adicionamos logging extensivo para facilitar o diagnóstico de problemas

#### 4. Logging detalhado
- Adicionamos mensagens de log em pontos-chave para monitorar o fluxo de execução
- Incluímos logs de erro específicos para problemas de inicialização e manipulação de estados
- Utilizamos logs de depuração para acompanhar transições de estado

### Resultado da Correção
- O aplicativo agora compila e executa sem erros
- A transição entre estados funciona corretamente
- O tratamento de erros é robusto, evitando crashes mesmo em situações excepcionais
- O código segue as melhores práticas de null safety do Kotlin

### Data da Correção
28/04/2025

---

## Módulos que implementam o padrão MVVM no aplicativo

Atualmente, os seguintes módulos do aplicativo implementam completamente o padrão de arquitetura MVVM:

### 1. Módulo de Contratos de Projeto
- **Model**: `ContractRepository` - Gerencia acesso a dados de contratos
- **View**: `ProjectContractsFragment` - Exibe os contratos e captura interações do usuário
- **ViewModel**: `ProjectContractsViewModel` - Gerencia estado e lógica de apresentação

### 2. Módulo de Faturas de Projeto
- **Model**: Repository interno (não exposto diretamente)
- **View**: `ProjectInvoicesFragment` - Exibe as faturas do projeto
- **ViewModel**: `ProjectInvoicesViewModel` - Gerencia filtros, paginação e estado das faturas

### 3. Módulo de Sumário de Projeto (Nova implementação)
- **Model**: `ProjectSummaryRepository` - Gerencia acesso a dados de sumário
- **View**: `ProjectSummaryFragment` - Exibe resumo e contratos do projeto
- **ViewModel**: `ProjectSummaryViewModel` - Gerencia carregamento, filtragem e estados da UI

Todos os módulos acima utilizam:
- **LiveData** para observação reativa de dados
- **UiState** para representação consistente de diferentes estados
- **ViewModelFactory** para injeção de dependências
- **Coroutines** para operações assíncronas
- **ViewFlipper** para gerenciamento visual de estados

### Próximos Módulos para Implementação MVVM
- Módulo de Detalhes de Cliente
- Módulo de Configurações
- Módulo de Relatórios

## Implementação do Room Database para persistência de dados offline-first

### Problema Detectado
O aplicativo não possuía um mecanismo de persistência de dados local, dificultando o acesso offline e resultando em perda de dados quando o usuário não tinha conexão com a internet.

### Detalhes da Implementação

#### 1. Configuração do Room Database
- Criamos a classe `AppDatabase` como ponto central para acesso ao banco
- Implementamos o padrão Singleton para garantir uma única instância do banco
- Definimos as entidades principais: `ProjectEntity`, `ProjectContractEntity` e `ProjectInvoiceEntity`
- Configuramos exportação de esquema para facilitar migrações futuras

#### 2. DAOs (Data Access Objects)
- Criamos interfaces DAO para cada entidade com métodos para operações CRUD
- Implementamos consultas específicas usando a linguagem SQL do Room:
  - Consultas por ID, tipo e status
  - Pesquisa de texto em campos específicos
  - Obtenção de dados não sincronizados para sincronização posterior
- Utilizamos Flow para observar alterações em tempo real nos dados

#### 3. Entidades
- Modelamos as entidades com anotações Room para mapeamento SQL
- Adicionamos índices em campos de consulta frequente para otimizar performance
- Incluímos campos para controle de sincronização (`lastUpdated`, `isSynced`) para suporte offline

#### 4. Mappers
- Implementamos a classe `ProjectContractMapper` para converter entre modelos de domínio e entidades
- Separamos a lógica de persistência da lógica de negócios seguindo o princípio de responsabilidade única

#### 5. Repositórios com estratégia offline-first
- Criamos repositórios (ex: `ProjectSummaryRepository`) que implementam uma estratégia offline-first:
  1. Os dados são sempre buscados primeiro do banco local
  2. Tentativa de atualização com dados da API é feita em segundo plano quando há conexão
  3. Dados criados ou modificados sem conexão são marcados para sincronização posterior
- Implementamos mecanismos de detecção de conectividade para otimizar operações de rede

#### 6. Integração com MVVM
- Conectamos os repositórios aos ViewModels para separar a lógica de dados da interface
- Usamos Flow e LiveData para propagar mudanças de forma reativa do banco para a UI
- Implementamos estados de UI (loading, empty, error, success) para refletir o estado dos dados
- Adicionamos SwipeRefreshLayout para permitir ao usuário forçar sincronização

### Benefícios da Implementação
- Funcionamento do aplicativo mesmo sem conexão com internet
- Melhor experiência do usuário com carregamento mais rápido de dados
- Redução do consumo de dados móveis e de bateria com cache local eficiente
- Resiliência contra problemas de conectividade intermitente
- Sincronização automática quando a conexão é restaurada

### Data da Implementação
30/04/2025

## Correção do erro "ScrollView can host only one direct child" nos fragmentos

### Problema Detectado
A aplicação apresentava um crash com a seguinte mensagem de erro:
```
java.lang.IllegalStateException: ScrollView can host only one direct child
at androidx.core.widget.NestedScrollView.addView(NestedScrollView.java:528)
```

Este erro ocorria ao tentar mostrar um Snackbar dentro de fragmentos que utilizam `NestedScrollView` como elemento raiz do layout. O problema acontecia especificamente no `ProjectSummaryFragment` e `ProjectContractsFragment` ao tentar mostrar mensagens de conectividade ou erro.

### Detalhes da Correção

#### 1. Identificação do Problema
- O Snackbar tenta adicionar uma view ao layout raiz
- Quando o layout raiz é um ScrollView, isso causa o erro porque ScrollView só pode ter um filho direto
- Este problema afetava os fragmentos que usavam NestedScrollView como elemento raiz

#### 2. Correções Implementadas
- Substituímos as chamadas de `Snackbar.make(...).show()` por `Toast.makeText(...).show()` nos seguintes locais:
  - `ProjectSummaryFragment.showConnectivityMessage()`
  - `ProjectContractsFragment.showError()`
- Adicionamos logs adicionais para manter um registro das mensagens exibidas
- Mantivemos a mesma experiência do usuário sem comprometer a estabilidade do aplicativo

#### 3. Melhorias na Gestão de Erros
- Adicionamos verificação de nulos antes de tentar manipular elementos de UI
- Melhoramos o registro de log para facilitar o diagnóstico de problemas futuros
- Implementamos tratamento de erros mais robusto em ambos os fragmentos

### Recomendações Futuras

1. **Modificar a estrutura de layout** em futuras versões para permitir o uso de Snackbar:
   - Usar CoordinatorLayout como elemento raiz
   - Colocar o NestedScrollView como um filho do CoordinatorLayout
   - Isso permitiria o uso de Snackbars sem problemas

2. **Criar um mecanismo centralizado de notificação** que escolha automaticamente entre Snackbar ou Toast baseado no layout do fragmento atual

### Data da Correção
30/04/2025

## Implementação de paginação nas RecyclerViews

### Problema Detectado
As listas de itens eram carregadas inteiramente de uma só vez, o que poderia levar a:
- Aumento do consumo de memória com grandes conjuntos de dados
- Lentidão na renderização e interação com a interface
- Travamentos em dispositivos mais antigos ou com recursos limitados
- Uso excessivo de dados em conexões móveis

### Detalhes da Implementação

#### 1. Atualização do Adapter
- Implementamos o padrão de ViewHolder múltiplos (múltiplos tipos de view)
- Adicionamos um layout específico para o item de loading no final da lista
- Criamos métodos para gerenciar o estado de carregamento:
  - `addLoadingFooter()`: Adiciona o indicador de carregamento no final da lista
  - `removeLoadingFooter()`: Remove o indicador quando a carga é concluída
  - `addData()`: Método para adicionar dados sem resetar a lista inteira

#### 2. Implementação do Scroll Infinito
- Adicionamos um `RecyclerView.OnScrollListener` para detectar quando o usuário chega ao final da lista
- Implementamos a lógica para carregar mais itens quando o usuário atinge uma distância específica do final
- Adicionamos indicadores visuais de carregamento para melhorar a experiência do usuário

#### 3. Atualização do ViewModel
- Adicionamos controle de paginação através de variáveis como `currentPage` e `pageSize`
- Implementamos LiveData para controlar o estado de carregamento (`loadingMore` e `canLoadMore`)
- Criamos métodos para gerenciar o ciclo de vida da paginação:
  - `resetPagination()`: Reinicia o estado da paginação
  - `loadMoreContracts()`: Carrega a próxima página de dados

#### 4. Modificações no Repositório
- Implementamos métodos que suportam paginação:
  - `getContractsByProjectPaged()`: Busca contratos de um projeto com paginação
  - `hasMoreData()`: Verifica se há mais dados disponíveis para carregar
- Criamos uma função para gerar dados de teste suficientes para demonstrar a paginação

### Benefícios da Implementação
- Carregamento mais rápido da tela inicial, melhorando a percepção de desempenho
- Redução do consumo de memória RAM durante a navegação
- Economia de dados móveis, carregando apenas o necessário
- Interface mais fluida, mesmo com grandes conjuntos de dados
- Experiência de usuário aprimorada com indicadores de carregamento claros

### Recomendações Futuras
1. Implementar cache de dados no repositório para manter páginas já carregadas
2. Adicionar opção de "pull-to-refresh" para atualizar do início quando necessário
3. Refinamento da UI dos indicadores de loading para melhor integração com o design

### Data da Implementação
30/04/2025

## Implementação da API e integração com o backend

### Detalhes da Implementação
A implementação da API foi realizada para conectar o aplicativo com o backend, permitindo operações CRUD completas para os principais módulos do sistema.

#### 1. Configuração do Retrofit e Cliente HTTP
- Implementamos o Retrofit para gerenciar as chamadas de API
- Configuramos interceptores para gerenciamento de autenticação e logs
- Adicionamos estratégias de retry para falhas temporárias de rede
- Estabelecemos timeouts adequados para um bom equilíbrio entre tempo de resposta e tolerância a redes lentas

#### 2. Implementação dos serviços de API
- Criamos interfaces para definir os endpoints disponíveis:
  - `ClienteApiService`: CRUD de clientes
  - `EquipamentoApiService`: CRUD de equipamentos
  - `ProjetoApiService`: CRUD de projetos e suas entidades relacionadas
- Adicionamos anotações Retrofit para mapear métodos HTTP aos endpoints da API

#### 3. Serialização e deserialização de dados
- Implementamos classes de resposta para cada endpoint
- Utilizamos anotações JSON para mapear propriedades entre o modelo da API e os objetos Kotlin
- Criamos adaptadores personalizados para tipos complexos (datas, enumerações, etc.)

#### 4. Tratamento de erros e estados de API
- Implementamos a classe `Resource<T>` para encapsular respostas da API:
  - `Resource.Success`: Operação bem-sucedida com dados
  - `Resource.Error`: Falha na operação com mensagem de erro
  - `Resource.Loading`: Operação em andamento
- Adicionamos tratamento centralizado de erros para códigos HTTP específicos

#### 5. Integração com Repositórios existentes
- Atualizamos os repositórios para buscar dados da API em vez de fontes mockadas
- Implementamos estratégia offline-first:
  1. Dados são buscados primeiro do banco local
  2. Atualizações da API são feitas em background
  3. Alterações locais são sincronizadas com o servidor quando há conexão

### Benefícios da Implementação
- Dados reais em toda a aplicação em vez de dados mockados
- Persistência de alterações no servidor para acesso em múltiplos dispositivos
- Melhor experiência do usuário com dados atualizados
- Base para implementação de funcionalidades colaborativas

### Data da Implementação
01/05/2025

---

## Implementação do módulo de Equipamentos

### Detalhes da Implementação
Implementamos um novo módulo de Equipamentos permitindo o cadastro, edição, exclusão e visualização de equipamentos no sistema.

#### 1. Modelo de Dados
- Criamos a classe `Equipamento` com todos os atributos necessários:
  - Dados básicos (id, nome, marca, modelo)
  - Informações técnicas (potência, capacidade, dimensões)
  - Dados de aquisição (data de compra, valor, fornecedor)
  - Código de patrimônio e número de série
  - Status atual (disponível, em uso, em manutenção, inativo)

#### 2. Implementação do padrão MVVM
- **Model**: `EquipamentoRepository` para acesso e persistência de dados
- **View**: `EquipamentosFragment` e `EquipamentoDetailFragment` para exibição e interação
- **ViewModel**: `EquipamentosViewModel` para gerenciar estado e lógica de apresentação

#### 3. Interface do Usuário
- Implementamos uma lista de equipamentos com categorização por tipo
- Adicionamos filtros por status e recurso de pesquisa
- Criamos uma tela de detalhes com todas as informações do equipamento
- Implementamos formulários para cadastro e edição com validação de dados
- Adicionamos opção para anexar fotos e documentos aos equipamentos

#### 4. Funcionalidades Específicas
- Registro de histórico de manutenções para cada equipamento
- Sistema de reserva de equipamento para projetos
- QR Code para identificação rápida em campo
- Cálculo de depreciação e valor atual do ativo

### Benefícios da Implementação
- Gestão completa do inventário de equipamentos da empresa
- Rastreamento do histórico de uso e manutenção
- Maior controle sobre os ativos físicos
- Melhor planejamento de manutenções preventivas
- Otimização da alocação de recursos em projetos

### Data da Implementação
01/05/2025

---

## Atualização do módulo de Clientes

### Detalhes da Implementação
Atualizamos o módulo de Clientes para integração com a nova API e adição de novas funcionalidades.

#### 1. Integração com API
- Conectamos o módulo aos novos endpoints da API de clientes
- Atualizamos o `ClienteRepository` para realizar chamadas de API reais
- Implementamos cache local para operações offline

#### 2. Melhorias na interface
- Redesenhamos a listagem de clientes com mais informações visíveis
- Adicionamos um sistema de cards para melhor visualização
- Implementamos pesquisa e filtros avançados
- Criamos novos indicadores visuais para status de clientes

#### 3. Novas funcionalidades
- Adicionamos relacionamento com contatos (múltiplos contatos por cliente)
- Implementamos histórico de interações com o cliente
- Adicionamos gestão de documentos por cliente
- Criamos função de exportação de dados para relatórios

#### 4. Melhorias no processo de edição
- Reformulamos o diálogo de edição com validação em tempo real
- Implementamos um fluxo de trabalho mais intuitivo
- Adicionamos auto-preenchimento de endereço via CEP
- Melhoramos o feedback visual durante operações

### Benefícios da Implementação
- Interface mais intuitiva para gestão de clientes
- Maior completude nas informações de cadastro
- Melhor experiência ao editar clientes
- Relacionamento mais eficiente com contatos do cliente

### Data da Implementação
02/05/2025

---

## Correção de bugs na edição de clientes

### Problema Detectado
Ao editar clientes, após a edição bem-sucedida de um cliente, não era possível editar um segundo cliente. O sistema apresentava apenas a mensagem "Cliente atualizado com sucesso" sem abrir o diálogo de edição.

Logs de erro relacionados:
```
2025-05-05 07:31:16.754  2534-2534  ALG_Gestao...tesAdapter com.example.alg_gestao_02  D  Menu clicado: CN PISO FORTE LTDA
2025-05-05 07:31:17.508  2534-2534  ALG_Gestao...esFragment com.example.alg_gestao_02  D  Editando cliente: CN PISO FORTE LTDA
2025-05-05 07:31:17.509  2534-2534  WindowOnBackDispatcher  com.example.alg_gestao_02  W  sendCancelIfRunning: isInProgress=false callback=android.widget.PopupWindow$PopupDecorView$$ExternalSyntheticLambda1@619c67b
```

Outro problema identificado foi o erro de cancelamento de jobs nas operações assíncronas:
```
2025-05-05 07:37:08.804  8179-8179  ALG_Gestao...Repository com.example.alg_gestao_02  E  Erro ao buscar clientes
kotlinx.coroutines.JobCancellationException: Job was cancelled; job=SupervisorJobImpl{Cancelling}@8b2c385
```

### Detalhes da Correção

#### 1. Problema na edição de múltiplos clientes
- Identificamos que o problema estava no compartilhamento do mesmo ViewModel entre diferentes instâncias de `CadastroClienteDialogFragment`
- Modificamos o `CadastroClienteDialogFragment` para usar escopo de Fragment no ViewModel em vez de Activity
- Alteramos o método `setupViewModel()` para usar `ViewModelProvider(this, factory)` em vez de `ViewModelProvider(requireActivity(), factory)`
- Adicionamos tags únicas para cada diálogo baseadas no ID do cliente para evitar conflitos

#### 2. Correção do estado da operação
- Implementamos um mecanismo para resetar o estado da operação (`_operationState`) após operações bem-sucedidas
- Adicionamos um delay curto para garantir que os observadores processem o estado antes do reset
- Utilizamos um callback para notificar quando um cliente é salvo e atualizar a lista principal

#### 3. Tratamento de Jobs cancelados
- Implementamos tratamento específico para `CancellationException` em todos os métodos do repositório
- Alteramos os logs para nível DEBUG em vez de ERROR quando uma operação é cancelada normalmente
- Evitamos exibir mensagens de erro ao usuário quando operações são canceladas por motivos normais (navegação, etc.)

### Benefícios da Correção
- Edição de múltiplos clientes em sequência sem erros
- Experiência de usuário mais fluida sem mensagens de erro indevidas
- Logs mais limpos com foco em problemas reais, não eventos normais
- Melhor gerenciamento de estado nas operações assíncronas

### Data da Correção
05/05/2025