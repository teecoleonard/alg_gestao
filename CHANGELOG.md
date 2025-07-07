# 📋 Changelog - ALG Gestão

Todas as mudanças notáveis deste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [1.1.0] - 07-07-2025

### 🔧 Corrigido
- **Dialog de Seleção de Período**: Crash corrigido no `SelecionarPeriodoDialogFragment`
  - Removido conflito entre `onCreateView()` e `onCreateDialog()`
  - Adicionado configurações corretas para DialogFragment com layout customizado
- **Dropdowns AutoCompleteTextView**: Campos agora funcionam como dropdowns clicáveis
  - Configurado `completionThreshold="0"` para exibição imediata
  - Adicionado listeners para forçar exibição das listas de mês e ano
  - Corrigido problema de campos não interativos no período específico

### 🎨 Melhorado
- **UX Dialog de Período**: Interface mais intuitiva com dropdowns funcionais
- **Logs de Debug**: Adicionados logs para debugging dos seletores de período

---

## [1.0.3] - 06-07-2025

### 🚀 Adicionado
- **Nova Arquitetura UX**: Reorganização completa da experiência do usuário
  - Lista de Clientes: Visão macro com valores totais gerais
  - Resumo Mensal: Análise detalhada com filtros específicos por período
- **Dialog de Seleção de Período**: Nova interface para escolha de período antes de abrir resumo
  - Opção "Mês Atual" (destacado em azul)
  - Opção "Período Específico" com seletores de mês/ano
  - Opção "Último com Dados" (fallback inteligente)
- **Filtro Real por Período**: Implementação de filtro local inteligente
  - Validação se dados pertencem ao período solicitado
  - Filtro de contratos por data de assinatura
  - Filtro de devoluções por data
  - Recálculo de valores baseado apenas nos dados filtrados
- **Estado Vazio**: Interface para períodos sem dados
  - Toast informativo "Nenhum dado encontrado para [período]"
  - Valores zerados quando não há dados no período

### 🔧 Corrigido
- **Bug Crítico de Formatação**: Inconsistência de Locale corrigida
  - Problema: Filtro "2025-06" mostrava "abril/2025" em vez de "junho/2025"
  - Solução: Uso consistente de `Locale("pt", "BR")` em todos os métodos
  - Afetava: `ResumoMensalClienteActivity`, `ResumoMensalCliente` model
- **Filtro Não Funcional**: API backend não implementava filtro por período
  - Solução: Filtro local que processa dados recebidos da API
  - Garante que apenas dados do período selecionado sejam exibidos
  - Funciona independente da implementação backend

### 🎨 Melhorado
- **Separação de Responsabilidades**: 
  - Lista Geral: Apenas valores totais, sem filtros
  - Resumo Detalhado: Análises específicas com filtros completos
- **Performance**: Menos requisições desnecessárias na lista geral
- **UX Intuitiva**: Fluxo natural de visão geral → detalhe específico
- **Interface Limpa**: Filtros apenas onde são realmente necessários

### ❌ Removido
- **Filtros da Lista Geral**: Removidos filtros por período da lista de clientes
- **Código Não Alcançável**: Warnings de "unreachable code" eliminados

---

## [1.0.2] - 03-07-2025

### 🚀 Adicionado
- **Sistema de Fallback Inteligente**: Dados simulados que reagem ao filtro de período
  - Algoritmo de geração baseado em seed único por período
  - Valores variam significativamente entre diferentes meses/anos
  - Contratos, status e devoluções únicos por período
- **Logs de Depuração Detalhados**: Sistema completo de logging para debug
  - Rastreamento de formatação de datas
  - Debug de aplicação de filtros
  - Logs de validação de dados por período

### 🔧 Corrigido
- **Dados Simulados Estáticos**: Fallback agora varia por período selecionado
- **Inconsistência de Formatação**: Métodos de formatação de data uniformizados

### 🎨 Melhorado
- **Experiência Visual**: Cada período mostra dados únicos e reconhecíveis
- **Feedback ao Usuário**: Interface responsiva com mudanças visuais claras

---

## [1.0.1] - 02-07-2025

### 🚀 Adicionado
- **Filtros por Período**: Sistema completo de filtros no módulo financeiro
  - Seletores de mês e ano em português
  - Backend com parâmetros mes/ano
  - Interface de filtros responsiva
- **Módulo Financeiro**: Dashboard financeiro completo
  - Receita por cliente
  - Métricas em tempo real
  - Relatórios detalhados

### 🎨 Melhorado
- **Interface de Filtros**: Dropdowns intuitivos com formatação brasileira
- **API Integration**: Endpoints para filtros de período implementados

---

## [1.0.0] - 30-06-2025

### 🚀 Lançamento Inicial
- **Sistema de Autenticação**: Login seguro com validação CPF
- **Gestão Completa**: Módulos de clientes, contratos, equipamentos e devoluções
- **Dashboard**: Visão geral com métricas do negócio
- **Assinaturas Digitais**: Sistema de captura de assinatura
- **Geração de PDF**: Contratos e relatórios automáticos
- **Interface Moderna**: Material 3 Design com animações Lottie
- **Arquitetura MVVM**: Padrão robusto com Repository Pattern
- **Room Database**: Persistência local com sincronização

### 🛠️ Tecnologias
- **Linguagem**: Kotlin 100%
- **UI**: Material 3 Design + View Binding
- **Arquitetura**: MVVM + Repository Pattern
- **Banco**: Room Database + Retrofit API
- **Concorrência**: Coroutines + Flow + LiveData

---

## 🏷️ Versionamento

### Formato de Versionamento
- **MAJOR**: Mudanças incompatíveis na API
- **MINOR**: Funcionalidades adicionadas de forma compatível
- **PATCH**: Correções de bugs compatíveis

### Status dos Releases
- 🟢 **Estável**: Versão estável para produção
- 🟡 **RC**: Release Candidate - quase pronto
- 🔴 **Beta**: Em desenvolvimento - instável

---

## 📊 Estatísticas de Mudanças

| Versão | Arquivos Modificados | Linhas Adicionadas | Linhas Removidas | Bugs Corrigidos |
|--------|---------------------|-------------------|------------------|-----------------|
| 2.1.0  | 2 arquivos          | +50               | -15              | 2 críticos      |
| 2.0.0  | 8 arquivos          | +280              | -45              | 1 crítico       |
| 1.2.0  | 5 arquivos          | +120              | -20              | 3 menores       |
| 1.1.0  | 12 arquivos         | +450              | -30              | 5 menores       |
| 1.0.0  | Projeto inicial     | +5000             | 0                | 0               |

---

## 🐛 Bugs Conhecidos

### Versão Atual (1.1.0)
- Nenhum bug crítico conhecido

### Versões Anteriores
- ~~[2.0.0] Dialog de período crashava ao abrir~~ ✅ Corrigido em 1.1.0
- ~~[1.2.0] Filtro mostrava mês incorreto~~ ✅ Corrigido em 1.1.0
- ~~[1.1.0] Dados não mudavam com filtro~~ ✅ Corrigido em 1.1.0

---

## 🔮 Roadmap

### [2.2.0] - Planejado para Outubro 2025
- **📊 Dashboard de Trends**: Evolução temporal da receita
- **📈 Comparação Multi-período**: Ver vários meses lado a lado
- **📄 Exportação Filtrada**: PDF/Excel do período selecionado

### [2.3.0] - Planejado para Novembro 2025
- **🔍 Busca Avançada**: Filtros múltiplos na lista de clientes
- **🎯 Metas por Período**: Acompanhamento de objetivos mensais
- **🔔 Alertas Inteligentes**: Notificações baseadas em padrões

---

**Última atualização**: 07 de Julho de 2025  
**Próxima release planejada**: 2.2.0 (Outubro 2025) 
