# ALG Gestão

Um aplicativo Android moderno para gestão de contratos, clientes e equipamentos, desenvolvido com Kotlin seguindo o padrão de arquitetura MVVM (Model-View-ViewModel).

<p align="center">
    <img src="https://github.com/user-attachments/assets/515cc939-ff3a-48df-bd70-40b6f95ef59a" width="400" alt="Logo ALG Gestão">
</p>

## Sobre

ALG Gestão é uma aplicação para gerenciamento de contratos, clientes e equipamentos, desenvolvida para facilitar o controle e a administração de recursos em empresas de locação. O aplicativo segue os princípios de design Material 3 e implementa o padrão de arquitetura MVVM para garantir a manutenibilidade e testabilidade do código.

## Funcionalidades

### Módulo de Clientes
- Cadastro e edição de informações de clientes
- Visualização detalhada de dados de clientes 
- Pesquisa por nome de cliente
- Visualização de contratos associados a cada cliente
- Gestão de status de devoluções (pendentes, devolvidas, com problemas)

### Módulo de Contratos
- Criação, edição e exclusão de contratos
- Associação de equipamentos a contratos
- Cálculo automático de valores com base nos equipamentos associados
- Busca e filtragem de contratos por cliente
- Visualização detalhada de informações de contratos (local de obra, período, entregas)

### Módulo de Equipamentos
- Cadastro e gerenciamento de equipamentos
- Detalhes técnicos (potência, capacidade, dimensões)
- Histórico de manutenções e rastreamento de uso
- Informações financeiras (valor de aquisição, depreciação)
- Status de disponibilidade (disponível, em uso, em manutenção)

### Dashboard
- Visão geral de contratos, clientes e equipamentos
- Acesso rápido aos módulos principais
- Interface intuitiva com cards informativos
- Pull-to-refresh para atualização de dados

## Arquitetura

O projeto segue a arquitetura MVVM (Model-View-ViewModel) com os seguintes componentes:

### Camadas
- **Model**: Repositórios e fontes de dados (API, banco local)
- **View**: Fragments, Activities e adaptadores para interface do usuário
- **ViewModel**: Gerenciamento de estado e lógica de apresentação

### Componentes
- **Room Database**: Persistência de dados local e suporte offline-first
- **Retrofit**: Cliente HTTP para comunicação com a API REST
- **LiveData**: Observação reativa de dados
- **Navigation Component**: Gerenciamento de navegação entre telas
- **ViewBinding**: Vinculação de views sem findViewById()
- **Coroutines**: Operações assíncronas e concorrentes

## Implementações Recentes

### Correções e Melhorias
- Implementação do padrão MVVM no ProjectSummaryFragment
- Correção de problemas com ClassCastException em ViewModels duplicados
- Correção do comportamento do botão voltar no ProjectDetailActivity
- Ajuste na ordem das abas no ProjectDetailActivity
- Implementação de Room Database para persistência offline-first
- Correção do erro "ScrollView can host only one direct child" nos fragmentos
- Implementação de paginação nas RecyclerViews

### Novas Funcionalidades
- Integração completa com API backend
- Implementação do módulo de Equipamentos
- Atualização do módulo de Clientes
- Busca por nome de cliente nos módulos de Contratos e Clientes
- Interação com os cards de status de devolução
- Correção do SwipeRefreshLayout no Dashboard

## Requisitos

- Android 7.0 (API 24) ou superior
- Acesso à internet para sincronização com o servidor
- Permissões: INTERNET, ACCESS_NETWORK_STATE

## Configuração do Projeto

### Pré-requisitos
- Android Studio Giraffe (2023.1.1) ou superior
- JDK 11
- Kotlin 1.8.0+

### Como Executar
1. Clone o repositório
2. Abra o projeto no Android Studio
3. Sincronize o projeto com os arquivos Gradle
4. Execute no emulador ou dispositivo físico

## Tecnologias Utilizadas

- **Linguagem**: Kotlin
- **Interface**: Material 3 Components, ViewBinding
- **Arquitetura**: MVVM
- **Persistência**: Room Database
- **Rede**: Retrofit, OkHttp
- **Concorrência**: Coroutines, Flow
- **Navegação**: Navigation Component
- **Outras**: Lottie (animações), ViewFlipper (estados de UI)

## Estrutura do Código

```
com.example.alg_gestao_02/
├── data/
│   ├── api/            # Serviços de API e cliente HTTP
│   ├── db/             # Configuração do Room e DAOs
│   ├── models/         # Modelos de dados e entidades
│   └── repository/     # Implementações de repositórios
├── ui/
│   ├── auth/           # Telas de autenticação
│   ├── cliente/        # Módulo de clientes
│   ├── contrato/       # Módulo de contratos
│   ├── dashboard/      # Dashboard principal
│   └── equipamento/    # Módulo de equipamentos
└── utils/              # Classes utilitárias
```

## Contribuição

Para contribuir com o projeto:

1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença [MIT](https://opensource.org/licenses/MIT).

## Contato

ALG Gestão - [website](https://alggestao.com.br)
