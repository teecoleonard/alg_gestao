package com.example.alg_gestao_02.ui.orcamento.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.data.models.Material
import com.example.alg_gestao_02.data.repository.EquipamentoRepository
import com.example.alg_gestao_02.data.repository.MaterialRepository
import com.example.alg_gestao_02.service.OrcamentoItemDTO
import com.example.alg_gestao_02.service.OrcamentoMetaDTO
import com.example.alg_gestao_02.service.OrcamentoRequestDTO
import com.example.alg_gestao_02.service.PdfResponse
import com.example.alg_gestao_02.service.PdfService
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch

/** Período de locação de um equipamento no orçamento. */
enum class PeriodoOrcamento(val label: String) {
    DIARIO("Diário"),
    SEMANAL("Semanal"),
    QUINZENAL("Quinzenal"),
    MENSAL("Mensal");

    companion object {
        fun fromOrdinalSafe(index: Int): PeriodoOrcamento =
            values().getOrElse(index) { MENSAL }
    }
}

/** Linha de equipamento no orçamento (uid identifica a linha para edição/remoção). */
data class OrcamentoEquipLinha(
    val uid: Long,
    val equipamentoId: Int,
    val nome: String,
    val periodo: PeriodoOrcamento,
    val quantidade: Int,
    val valorUnitario: Double
) {
    val valorTotal: Double get() = quantidade * valorUnitario
}

/** Linha de material no orçamento. */
data class OrcamentoMaterialLinha(
    val uid: Long,
    val materialId: Int,
    val nome: String,
    val codigo: String?,
    val quantidade: Int,
    val valorUnitario: Double
) {
    val valorTotal: Double get() = quantidade * valorUnitario
}

class OrcamentoViewModel(
    private val equipamentoRepository: EquipamentoRepository = EquipamentoRepository(),
    private val materialRepository: MaterialRepository = MaterialRepository(),
    private val pdfService: PdfService = PdfService()
) : ViewModel() {

    private val _equipItens = MutableLiveData<List<OrcamentoEquipLinha>>(emptyList())
    val equipItens: LiveData<List<OrcamentoEquipLinha>> = _equipItens

    private val _materialItens = MutableLiveData<List<OrcamentoMaterialLinha>>(emptyList())
    val materialItens: LiveData<List<OrcamentoMaterialLinha>> = _materialItens

    private var proximoUid = 1L
    private fun novoUid(): Long = proximoUid++

    // ---------- Catálogo (busca sob demanda) ----------

    fun carregarCatalogoEquipamentos(
        onResult: (List<Equipamento>) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = equipamentoRepository.getEquipamentos()
            if (result.isSuccess()) {
                onResult(result.data ?: emptyList())
            } else {
                onError(result.message ?: "Erro ao carregar equipamentos")
            }
        }
    }

    fun carregarCatalogoMateriais(
        onResult: (List<Material>) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = materialRepository.getMateriais()
            if (result.isSuccess()) {
                onResult((result.data ?: emptyList()).filter { it.ativo })
            } else {
                onError(result.message ?: "Erro ao carregar materiais")
            }
        }
    }

    // ---------- Manipulação das linhas ----------

    fun valorPorPeriodo(equipamento: Equipamento, periodo: PeriodoOrcamento): Double = when (periodo) {
        PeriodoOrcamento.DIARIO -> equipamento.precoDiaria
        PeriodoOrcamento.SEMANAL -> equipamento.precoSemanal
        PeriodoOrcamento.QUINZENAL -> equipamento.precoQuinzenal
        PeriodoOrcamento.MENSAL -> equipamento.precoMensal
    }

    fun adicionarEquipamento(
        equipamentoId: Int,
        nome: String,
        periodo: PeriodoOrcamento,
        quantidade: Int,
        valorUnitario: Double
    ) {
        val linha = OrcamentoEquipLinha(
            uid = novoUid(),
            equipamentoId = equipamentoId,
            nome = nome,
            periodo = periodo,
            quantidade = quantidade.coerceAtLeast(1),
            valorUnitario = valorUnitario.coerceAtLeast(0.0)
        )
        _equipItens.value = _equipItens.value.orEmpty() + linha
    }

    /**
     * Adição em lote: para cada equipamento selecionado e cada período escolhido, cria uma linha
     * (quantidade 1, valor do catálogo para o período). Atualiza a LiveData uma única vez.
     */
    fun adicionarEquipamentosLote(equipamentos: List<Equipamento>, periodos: List<PeriodoOrcamento>) {
        if (equipamentos.isEmpty() || periodos.isEmpty()) return
        val atual = _equipItens.value.orEmpty().toMutableList()
        for (eq in equipamentos) {
            for (p in periodos) {
                atual.add(
                    OrcamentoEquipLinha(
                        uid = novoUid(),
                        equipamentoId = eq.id,
                        nome = eq.nomeEquip,
                        periodo = p,
                        quantidade = 1,
                        valorUnitario = valorPorPeriodo(eq, p).coerceAtLeast(0.0)
                    )
                )
            }
        }
        _equipItens.value = atual
    }

    /** Adição em lote de materiais (quantidade 1, valor do catálogo). Atualiza a LiveData uma vez. */
    fun adicionarMateriaisLote(materiais: List<Material>) {
        if (materiais.isEmpty()) return
        val atual = _materialItens.value.orEmpty().toMutableList()
        for (m in materiais) {
            atual.add(
                OrcamentoMaterialLinha(
                    uid = novoUid(),
                    materialId = m.id,
                    nome = m.nome,
                    codigo = m.codigo,
                    quantidade = 1,
                    valorUnitario = m.valorUnitario.coerceAtLeast(0.0)
                )
            )
        }
        _materialItens.value = atual
    }

    fun atualizarEquipamento(
        uid: Long,
        periodo: PeriodoOrcamento,
        quantidade: Int,
        valorUnitario: Double
    ) {
        _equipItens.value = _equipItens.value.orEmpty().map { linha ->
            if (linha.uid == uid) {
                linha.copy(
                    periodo = periodo,
                    quantidade = quantidade.coerceAtLeast(1),
                    valorUnitario = valorUnitario.coerceAtLeast(0.0)
                )
            } else {
                linha
            }
        }
    }

    fun removerEquipamento(uid: Long) {
        _equipItens.value = _equipItens.value.orEmpty().filterNot { it.uid == uid }
    }

    fun adicionarMaterial(
        materialId: Int,
        nome: String,
        codigo: String?,
        quantidade: Int,
        valorUnitario: Double
    ) {
        val linha = OrcamentoMaterialLinha(
            uid = novoUid(),
            materialId = materialId,
            nome = nome,
            codigo = codigo,
            quantidade = quantidade.coerceAtLeast(1),
            valorUnitario = valorUnitario.coerceAtLeast(0.0)
        )
        _materialItens.value = _materialItens.value.orEmpty() + linha
    }

    fun atualizarMaterial(uid: Long, quantidade: Int, valorUnitario: Double) {
        _materialItens.value = _materialItens.value.orEmpty().map { linha ->
            if (linha.uid == uid) {
                linha.copy(
                    quantidade = quantidade.coerceAtLeast(1),
                    valorUnitario = valorUnitario.coerceAtLeast(0.0)
                )
            } else {
                linha
            }
        }
    }

    fun removerMaterial(uid: Long) {
        _materialItens.value = _materialItens.value.orEmpty().filterNot { it.uid == uid }
    }

    // ---------- Totais ----------

    fun subtotalEquipamentos(): Double = _equipItens.value.orEmpty().sumOf { it.valorTotal }
    fun subtotalMateriais(): Double = _materialItens.value.orEmpty().sumOf { it.valorTotal }
    fun totalGeral(): Double = subtotalEquipamentos() + subtotalMateriais()
    fun temItens(): Boolean =
        _equipItens.value.orEmpty().isNotEmpty() || _materialItens.value.orEmpty().isNotEmpty()

    // ---------- Geração do PDF ----------

    private fun montarRequest(
        destinatario: String?,
        validadeDias: Int?,
        observacoes: String?
    ): OrcamentoRequestDTO {
        val equipamentos = _equipItens.value.orEmpty().map { linha ->
            OrcamentoItemDTO(
                descricao = linha.nome,
                periodo = linha.periodo.label,
                quantidade = linha.quantidade,
                valorUnitario = linha.valorUnitario,
                valorTotal = linha.valorTotal
            )
        }
        val materiais = _materialItens.value.orEmpty().map { linha ->
            OrcamentoItemDTO(
                descricao = linha.nome,
                codigo = linha.codigo,
                quantidade = linha.quantidade,
                valorUnitario = linha.valorUnitario,
                valorTotal = linha.valorTotal
            )
        }
        return OrcamentoRequestDTO(
            orcamento = OrcamentoMetaDTO(
                destinatario = destinatario?.takeIf { it.isNotBlank() },
                validadeDias = validadeDias,
                observacoes = observacoes?.takeIf { it.isNotBlank() }
            ),
            equipamentos = equipamentos,
            materiais = materiais
        )
    }

    fun gerarOrcamento(
        destinatario: String?,
        validadeDias: Int?,
        observacoes: String?,
        onSuccess: (PdfResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!temItens()) {
            onError("Adicione ao menos um item ao orçamento")
            return
        }
        viewModelScope.launch {
            val request = montarRequest(destinatario, validadeDias, observacoes)
            val result = pdfService.gerarPdfOrcamento(request)
            result.fold(
                onSuccess = { onSuccess(it) },
                onFailure = { e ->
                    LogUtils.error("OrcamentoViewModel", "Erro ao gerar orçamento", e)
                    onError(e.message ?: "Erro ao gerar orçamento")
                }
            )
        }
    }
}

class OrcamentoViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrcamentoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrcamentoViewModel() as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
