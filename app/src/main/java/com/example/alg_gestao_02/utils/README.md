# Sistema Centralizado de Tratamento de Erros

Este documento descreve o sistema centralizado de tratamento de erros implementado no aplicativo ALG Gestão, que proporciona uma forma consistente e eficiente de lidar com erros em toda a aplicação.

## Visão Geral

O sistema centralizado de tratamento de erros do ALG Gestão visa:

1. **Padronizar** as mensagens de erro em toda a aplicação
2. **Centralizar** a lógica de tratamento de erros 
3. **Melhorar a experiência do usuário** com mensagens amigáveis
4. **Facilitar a manutenção** do código
5. **Simplificar o monitoramento** de erros

## Componentes do Sistema

### 1. ErrorHandler

A classe principal que centraliza o tratamento de erros. Ela fornece:

- Categorização de erros (rede, API, banco de dados, etc.)
- Mensagens de erro amigáveis para o usuário
- Métodos para exibir erros em Fragments e Activities
- Registro de erros no sistema de log
- Preparação para integração com sistemas de monitoramento remoto

```kotlin
class ErrorHandler {
    companion object {
        enum class ErrorType { NETWORK, API, DATABASE, AUTH, VALIDATION, UNKNOWN }
        
        fun handleError(fragment: Fragment, exception: Throwable, actionLabel: String? = null, onRetry: (() -> Unit)? = null) { ... }
        
        fun handleError(context: Context, exception: Throwable, tag: String) { ... }
        
        fun logError(tag: String, exception: Throwable, message: String? = null) { ... }
    }
}
```

### 2. ErrorViewModel

Uma classe base para ViewModels que fornece funcionalidades de tratamento de erro:

- Captura e classifica exceções
- Emite eventos de erro através de LiveData
- Fornece suporte para ações de "retry" (tentar novamente)

```kotlin
open class ErrorViewModel {
    val errorEvent: LiveData<ErrorEvent> = _errorEvent
    
    protected fun handleException(exception: Throwable, tag: String, actionable: Boolean = false) { ... }
}
```

### 3. BaseFragment

Uma classe base para Fragments que implementa o tratamento padronizado de erros:

- Observa eventos de erro dos ViewModels
- Exibe mensagens de erro consistentes
- Fornece mecanismo padrão para ações de "retry"

```kotlin
abstract class BaseFragment : Fragment() {
    protected abstract fun getErrorViewModels(): List<ErrorViewModel>
    
    protected open fun onErrorRetry(errorEvent: ErrorViewModel.ErrorEvent) { ... }
}
```

## Como Usar

### 1. Para ViewModels:

```kotlin
class MeuViewModel : ViewModel() {
    // Adicionar o ErrorViewModel
    val errorHandler = ErrorViewModel()
    
    fun carregarDados() {
        viewModelScope.launch {
            try {
                // ... tentar carregar dados
            } catch (e: Exception) {
                // Usar o tratamento centralizado
                errorHandler.handleException(e, "MeuViewModel", true)
                
                // Atualizar o estado da UI
                _meuEstado.value = MeuEstado.Erro(e.message)
            }
        }
    }
}
```

### 2. Para Fragments:

```kotlin
class MeuFragment : BaseFragment() {
    private lateinit var viewModel: MeuViewModel
    
    // Implementar método abstrato
    override fun getErrorViewModels(): List<ErrorViewModel> {
        return listOf(viewModel.errorHandler)
    }
    
    // Implementar ação de retry (opcional)
    override fun onErrorRetry(errorEvent: ErrorViewModel.ErrorEvent) {
        viewModel.carregarDados()
    }
}
```

### 3. Para Activities:

```kotlin
class MinhaActivity : AppCompatActivity() {
    private lateinit var viewModel: MeuViewModel
    
    // Observar erros manualmente
    private fun setupViewModel() {
        viewModel.errorHandler.errorEvent.observe(this) { errorEvent ->
            ErrorHandler.handleError(
                context = this,
                exception = errorEvent.exception,
                tag = "MinhaActivity"
            )
        }
    }
}
```

## Vantagens

1. **Código mais limpo**: Não é necessário repetir código de tratamento de erro em cada tela
2. **Consistência**: Todas as mensagens de erro têm o mesmo estilo e comportamento
3. **Manutenibilidade**: Alterações no tratamento de erros são feitas em um único lugar
4. **UX melhorada**: Mensagens de erro amigáveis em vez de mensagens técnicas
5. **Monitoramento**: Facilita a implementação de rastreamento de erros

## Expansão Futura

O sistema está preparado para futuras expansões como:

- Integração com Firebase Crashlytics ou outras ferramentas de monitoramento
- Tratamento específico para diferentes tipos de API ou erros de negócio
- Análise de tendências de erros para identificar problemas recorrentes 