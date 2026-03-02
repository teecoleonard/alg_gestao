# 📋 Novo Sistema de Visualização de Contratos

## 🎯 Visão Geral

O sistema de contratos foi completamente reorganizado para facilitar a navegação e visualização, eliminando a confusão causada pela visualização por mês e implementando uma estrutura mais intuitiva com 3 abas principais.

## 📱 Estrutura das Abas

### 1. **Todos** 
Exibe todos os contratos não arquivados do sistema, independente do status.

**Características:**
- Mostra contratos em qualquer status (Pendente, Assinado, Em Andamento)
- Exclui automaticamente contratos arquivados
- Ordenados por data de emissão (mais recentes primeiro)

### 2. **Em Andamento**
Exibe apenas contratos com status "EM_ANDAMENTO".

**Características:**
- Filtra contratos que estão ativamente em uso
- Ideal para acompanhamento do dia a dia
- Facilita identificação de contratos ativos

### 3. **Arquivados**
Exibe contratos que foram arquivados (finalizados há mais de 6 meses).

**Características:**
- Contratos finalizados e arquivados
- Sistema de arquivamento automático após 6 meses
- Possibilidade de desarquivar se necessário

---

## 🔍 Sistema de Filtros

### Filtros por Mês
Cada aba possui chips de filtro para selecionar um mês específico:

- **Todos os meses**: Remove o filtro, mostra todos os contratos da aba
- **Últimos 12 meses**: Chips individuais para cada mês (Ex: "Jan/25", "Fev/25")

### Busca por Cliente
Campo de busca em tempo real que filtra contratos pelo nome do cliente.

**Como usar:**
1. Digite o nome do cliente no campo de busca
2. A lista é filtrada automaticamente enquanto você digita
3. Funciona em conjunto com os filtros de aba e mês

---

## 🗄️ Sistema de Arquivamento

### Arquivamento Automático

Contratos finalizados são automaticamente arquivados após 6 meses:

```kotlin
// Critérios para arquivamento automático:
- Status: FINALIZADO
- Data de vencimento: Mais de 6 meses atrás
- Não está arquivado ainda
```

### Arquivamento Manual

Administradores e funcionários podem arquivar/desarquivar contratos manualmente:

**Endpoints da API:**
- `POST /api/contratos/:id/arquivar` - Arquiva um contrato
- `POST /api/contratos/:id/desarquivar` - Desarquiva um contrato
- `POST /api/contratos/arquivar-automatico` - Executa arquivamento automático em lote

---

## 🔔 Sistema de Notificações

### Contratos Vencidos
Notificações são geradas automaticamente para contratos vencidos:

**Critérios:**
- Status diferente de FINALIZADO
- Data de vencimento anterior à data atual
- Não está arquivado

**Exemplo de Notificação:**
```
🔴 Contrato Vencido
Contrato #2024-001 de Cliente ABC venceu há 5 dia(s)
```

### Contratos Próximos do Vencimento
Alertas para contratos que vencem em até 7 dias:

**Critérios:**
- Status diferente de FINALIZADO
- Data de vencimento entre hoje e 7 dias à frente
- Não está arquivado

**Exemplo de Notificação:**
```
🟡 Contrato Próximo do Vencimento
Contrato #2024-002 de Cliente XYZ vence em 3 dia(s)
```

### Endpoint de Notificações
```
GET /api/contratos/notificacoes-vencimento
```

**Resposta:**
```json
{
  "success": true,
  "vencidos": [...],
  "proximosVencimento": [...],
  "totalVencidos": 2,
  "totalProximosVencimento": 5
}
```

---

## 🔄 Ciclo de Vida do Contrato

```
┌─────────────┐
│  PENDENTE   │  Contrato criado, aguardando assinatura
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  ASSINADO   │  Contrato assinado digitalmente
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ EM_ANDAMENTO│  Equipamentos em uso pelo cliente
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ FINALIZADO  │  Todos equipamentos devolvidos
└──────┬──────┘
       │
       │ (após 6 meses)
       ▼
┌─────────────┐
│  ARQUIVADO  │  Contrato arquivado automaticamente
└─────────────┘
```

---

## 💾 Estrutura do Banco de Dados

### Campos Adicionados no Modelo `Contrato`:

```sql
ALTER TABLE Contrato 
ADD COLUMN arquivado BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE Contrato 
ADD COLUMN data_arquivamento DATETIME NULL;
```

### Índices para Performance:

```sql
CREATE INDEX idx_contrato_arquivado ON Contrato(arquivado);
CREATE INDEX idx_contrato_data_venc ON Contrato(dataVenc);
CREATE INDEX idx_contrato_status ON Contrato(status_contrato);
```

---

## 🎨 Melhorias na Interface

### Antes (Sistema Antigo):
- ❌ Visualização confusa por mês
- ❌ Contratos de meses anteriores difíceis de encontrar
- ❌ Navegação entre múltiplas telas
- ❌ Falta de organização clara

### Depois (Sistema Novo):
- ✅ 3 abas claras e intuitivas
- ✅ Filtros de mês dentro de cada aba
- ✅ Busca em tempo real
- ✅ Sistema de arquivamento automático
- ✅ Notificações de vencimento
- ✅ Navegação simplificada

---

## 📊 Endpoints da API

### Arquivamento
```
POST /api/contratos/:id/arquivar
POST /api/contratos/:id/desarquivar
POST /api/contratos/arquivar-automatico
```

### Notificações
```
GET /api/contratos/notificacoes-vencimento
```

### Contratos (Existentes)
```
GET /api/contratos
GET /api/contratos/:id
POST /api/contratos
PUT /api/contratos/:id
DELETE /api/contratos/:id
```

---

## 🚀 Como Usar

### Para Visualizar Contratos:

1. **Ver todos os contratos:**
   - Abra o app
   - Clique em "Contratos" no Dashboard
   - Aba "Todos" já estará selecionada

2. **Ver apenas contratos em andamento:**
   - Clique na aba "Em Andamento"
   - Visualize apenas os contratos ativos

3. **Ver contratos arquivados:**
   - Clique na aba "Arquivados"
   - Visualize contratos finalizados há mais de 6 meses

4. **Filtrar por mês:**
   - Clique no chip do mês desejado (Ex: "Jan/25")
   - A lista será filtrada para aquele mês

5. **Buscar por cliente:**
   - Digite no campo de busca
   - Lista filtrada em tempo real

### Para Arquivar Contratos:

**Arquivamento Automático:**
```bash
# Executar manualmente (como admin):
POST /api/contratos/arquivar-automatico
```

**Arquivamento Manual:**
- Use os endpoints da API para arquivar/desarquivar contratos específicos

---

## 📝 Notas Importantes

1. **Contratos arquivados** não aparecem nas abas "Todos" e "Em Andamento"
2. **Arquivamento automático** roda periodicamente no servidor (configurável)
3. **Notificações** são geradas quando contratos estão vencidos ou próximos do vencimento
4. **Filtros** podem ser combinados (aba + mês + busca)
5. **Navegação** direta do Dashboard para a tela de contratos (sem tela intermediária)

---

## 🔧 Manutenção

### Verificar Contratos para Arquivamento:
```sql
SELECT 
    id, contratoNum, cliente_id, status_contrato, dataVenc, arquivado,
    DATEDIFF(CURDATE(), dataVenc) as dias_desde_vencimento
FROM Contrato
WHERE status_contrato = 'FINALIZADO'
  AND arquivado = false
  AND dataVenc < DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
ORDER BY dataVenc ASC;
```

### Executar Migração:
```bash
# No servidor MySQL:
mysql -u root -p < add-arquivamento-contratos.sql
```

---

## ✅ Checklist de Implementação

- [x] Criar TabLayout com 3 abas
- [x] Adicionar chips de filtro por mês
- [x] Implementar busca em tempo real
- [x] Adicionar campos de arquivamento no modelo
- [x] Criar endpoints de arquivamento na API
- [x] Implementar arquivamento automático
- [x] Sistema de notificações de vencimento
- [x] Atualizar navegação do Dashboard
- [x] Criar documentação
- [x] Remover sistema de visualização por mês antigo

---

**Data de Implementação:** Outubro 2024  
**Versão:** 2.0  
**Status:** ✅ Completo

