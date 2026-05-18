# Feature: Cadastro de Grupos com Reuniões Semanais

**Status**: ✅ FASE 1 COMPLETA | 🚀 PRONTO PARA TESTES  
**Data**: 2026-05-14  
**Desenvolvedor**: Claude Copilot

---

## 📋 Resumo da Feature

Sistema completo para criar grupos de projeto na etapa de imersão com:
- ✅ Seleção de alunos (filtrados apenas para imersão)
- ✅ Escolha de orientador
- ✅ Dados de empresa/instituição
- ✅ Nome + resumo do projeto
- ✅ **Geração automática de reuniões semanais** até fim da turma

---

## ✅ O QUE FOI IMPLEMENTADO (FASE 1)

### Backend (Java/Spring Boot 3.1.1)

#### **1. Modelos de Dados**

**ProjectGroupModel** (`src/main/java/com/example/brisa/models/ProjectGroupModel.java`)
- Expandido com novos campos:
  ```java
  @Enumerated(EnumType.ORDINAL)
  private DayOfWeek weeklyMeetingDay;  // Dia da semana (MON, TUE, etc)
  
  private LocalDate firstMeetingDate;  // Primeira reunião
  
  private Set<ProjectGroupMeetingModel> meetings;  // Reuniões geradas
  ```

**ProjectGroupMeetingModel** (NOVO)
- `src/main/java/com/example/brisa/models/ProjectGroupMeetingModel.java`
- Tabela: `project_group_meetings`
- Campos: id, project_group_id (FK), meeting_date, status, created_at

#### **2. Repositórios**

- ✅ `ProjectGroupMeetingRepository` - Queries para reuniões
  - `findByProjectGroupIdOrderByMeetingDateAsc()`
  - `findByProjectGroupIdAndMeetingDateBetween()`
  - `deleteByProjectGroupId()`

- ✅ `PeopleProjectGroupRepository` (NOVO)
  - Relacionamento muitos-para-muitos entre People e ProjectGroup
  - `deleteByProjectGroupId()`

#### **3. Service**

**ProjectGroupService** (NOVO)
- `src/main/java/com/example/brisa/services/ProjectGroupService.java`

**Métodos principais:**

```java
// Criar grupo com validações e geração automática de reuniões
createProjectGroup(Long classId, ProjectGroupCreateRequestDTO)

// Gerar reuniões semanais automaticamente
generateWeeklyMeetings(ProjectGroupModel)

// Listar grupos da turma
getGroupsByClass(Long classId)

// Detalhe completo do grupo
getGroupDetail(Long groupId)

// Atualizar grupo
updateProjectGroup(Long groupId, ProjectGroupCreateRequestDTO)

// Deletar grupo (cascade: reuniões + membros)
deleteProjectGroup(Long groupId)
```

**Validações implementadas:**
- ✅ Alunos devem estar na etapa de imersão
- ✅ Orientador deve ser pessoa válida do sistema
- ✅ Empresa/instituição deve existir (ou null se opcional)
- ✅ Datas: first_meeting_date >= hoje
- ✅ Turma deve ter data_fim definida para gerar reuniões

#### **4. DTOs**

- ✅ `ProjectGroupCreateRequestDTO`
  - Entrada para criar grupo

- ✅ `ProjectGroupResponseDTO`
  - Saída para listar grupos (resumido)

- ✅ `ProjectGroupDetailResponseDTO`
  - Saída com detalhes (membros + reuniões)

#### **5. Controller**

**ProjectGroupController** (NOVO)
- `src/main/java/com/example/brisa/controllers/ProjectGroupController.java`

**Endpoints implementados:**

| Método | Rota | Função |
|--------|------|--------|
| POST | `/api/classes/{classId}/groups` | Criar novo grupo |
| GET | `/api/classes/{classId}/groups` | Listar grupos da turma |
| GET | `/api/classes/{classId}/groups/{groupId}` | Detalhe do grupo |
| PUT | `/api/classes/{classId}/groups/{groupId}` | Editar grupo |
| DELETE | `/api/classes/{classId}/groups/{groupId}` | Deletar grupo |
| GET | `/api/classes/{classId}/immersion-students` | Listar alunos da imersão |

**Exemplo de resposta (criar):**
```json
{
  "message": "Grupo criado com sucesso",
  "group": {
    "id": 1,
    "projectTheme": "App de Delivery",
    "description": "Plataforma de entrega de alimentos",
    "projectCompanyName": "TechCorp",
    "leaderName": "Dr. Silva",
    "memberCount": 5,
    "weeklyMeetingDay": 1,
    "firstMeetingDate": "2026-05-20",
    "upcomingMeetingsCount": 8
  }
}
```

---

### Frontend (Vue 3 + Vite)

#### **1. Componente Modal**

**GroupCreateModal.vue** (NOVO)
- `src/components/GroupCreateModal.vue`

**Features:**
- ✅ Input: Nome do projeto
- ✅ Textarea: Resumo do projeto
- ✅ Select: Empresa/Instituição (com lista carregada)
- ✅ Select com busca: Orientador (carrega todas as pessoas)
- ✅ Multi-select: Alunos (com busca, filtrados para imersão)
- ✅ Datepicker: Primeira reunião
- ✅ Select: Dia da semana (0-6)
- ✅ Input: Link do repositório (opcional)
- ✅ Validações completas no frontend
- ✅ Feedback de erro/sucesso

#### **2. Service API**

**groupService.js** (NOVO)
- `src/services/groupService.js`

```javascript
export const groupService = {
  createGroup(classId, data)           // POST
  getGroupsByClass(classId)             // GET lista
  getGroupDetail(classId, groupId)      // GET detalhe
  updateGroup(classId, groupId, data)   // PUT
  deleteGroup(classId, groupId)         // DELETE
  getImmersionStudents(classId)         // GET alunos imersão
}
```

#### **3. Integração em ClassDetailsView**

**Arquivo**: `src/views/ClassDetailsView.vue`

**Mudanças:**
1. ✅ Import: `import GroupCreateModal from '@/components/GroupCreateModal.vue'`
2. ✅ Import: `import { groupService } from '@/services/groupService'`
3. ✅ Components: Adicionado `GroupCreateModal` à lista
4. ✅ Ref: `const showGroupCreateModal = ref(false)`
5. ✅ Método: `onGroupCreated(group)` - callback quando grupo é criado
6. ✅ Botão: Conectado ao modal (linha ~750, na seção de imersão)
7. ✅ Template: `<GroupCreateModal :is-open="showGroupCreateModal" :class-id="classId" @close="..." @group-created="..." />`

**Localização do botão**: Aba "Etapas" → Sub-aba "Imersão" → Seção "imersao-actions"

---

## 🔧 BUILD & COMPILE STATUS

✅ **Backend**: `mvn clean compile` - SUCESSO  
✅ **Frontend**: `npm run build` - SUCESSO  

---

## 🧪 COMO TESTAR (FASE 1)

### Pré-requisitos
1. Backend rodando: `cd BRISA-BACKEND && .\mvnw.cmd spring-boot:run`
2. Frontend rodando: `cd BRISA-FRONTEND && npm run dev`
3. Database com dados (turmas com etapa de imersão, pessoas, instituições)

### Passos para testar

1. **Navegar para uma turma com imersão**:
   ```
   http://localhost:5173/programs/2/classes/3
   ```
   _(Substitua os IDs conforme necessário)_

2. **Ir para aba "Etapas"** (já está visível no topo)

3. **Mudar para sub-aba "Imersão"** (botão no topo da seção)

4. **Clicar em "Cadastrar Grupo"** (botão com ícone de grupo)

5. **Preencher formulário**:
   - Nome: "Projeto X"
   - Resumo: "Descrição do projeto"
   - Empresa: Selecionar (opcional)
   - Orientador: Selecionar (buscar nome)
   - Alunos: Selecionar pelo menos 1 (só aparecem alunos da imersão)
   - Primeira reunião: Escolher data futura
   - Dia da semana: Escolher (ex: Segunda-feira)
   - Repositório: Opcional

6. **Clicar "Criar Grupo"**

7. **Verificar resposta**:
   - ✅ Mensagem de sucesso deve aparecer
   - ✅ Modal fecha
   - ✅ Página recarrega (se implementado)

---

## 🚀 PRÓXIMOS PASSOS (FASE 2)

### 1. Listagem de Grupos na UI ⭐ PRIORITÁRIO

**Arquivo**: `src/views/ClassDetailsView.vue`

**O que fazer:**
- [ ] Carregar grupos ao abrir a aba de imersão
- [ ] Exibir lista de grupos criados (já existe `imersaoGroups` ref)
- [ ] Mapear resposta da API para estrutura esperada
- [ ] Atualizar `imersaoMetricsCards` com contagem real de grupos

**Código esperado:**
```javascript
// Na seção de dados da imersão:
const loadImersaoGroups = async () => {
  try {
    const response = await groupService.getGroupsByClass(classId.value);
    imersaoGroups.value = response.data.map(group => ({
      id: group.id,
      name: group.projectTheme,
      mentor: group.leaderName,
      project: group.projectTheme,
      students: group.memberCount,
      partialAverage: '-',  // Será preenchido depois
      finalAverage: '-',    // Será preenchido depois
      status: 'Ativo',
      statusClass: 'status-active'
    }));
  } catch (error) {
    console.error('Erro ao carregar grupos:', error);
  }
};
```

### 2. Expansão de Grupo (Detalhe)

**O que fazer:**
- [ ] Implementar detalhe completo do grupo
- [ ] Exibir lista de alunos
- [ ] Mostrar reuniões agendadas
- [ ] Mostrar avaliações (parcial/final)
- [ ] Tab: Presença/Assiduidade

### 3. Edição de Grupo

**O que fazer:**
- [ ] Modal de edição similar ao de criação
- [ ] Campos editáveis (nome, resumo, empresa, orientador)
- [ ] Botão "Editar" em cada grupo
- [ ] Validações (alunos podem ser adicionados/removidos?)

### 4. Deleção de Grupo

**O que fazer:**
- [ ] Confirmação antes de deletar
- [ ] Cascade automático (reuniões + membros deletados)
- [ ] Botão "Deletar" com ícone de lixo
- [ ] Mensagem de sucesso

### 5. Apresentações (Avaliações)

**O que fazer:**
- [ ] Buscar datas de apresentação (parcial/final) da turma
- [ ] Exibir datas no grupo
- [ ] Permitir entrada de notas (parcial/final)
- [ ] Modal para submeter notas

### 6. Reuniões Semanais - UI

**O que fazer:**
- [ ] Listar reuniões do grupo
- [ ] Marcar presença por reunião
- [ ] Atualizar status (SCHEDULED → COMPLETED → CANCELLED)
- [ ] Relatório de assiduidade

### 7. Validações Adicionais (Backend)

**O que fazer:**
- [ ] Impedir duplicação de alunos no mesmo grupo
- [ ] Impedir aluno em 2+ grupos simultaneamente
- [ ] Validar que todos os alunos estão na mesma etapa
- [ ] Verificar capacidade máxima de alunos por grupo (se houver)

### 8. Testes

**O que fazer:**
- [ ] Teste unitário: `createProjectGroup()`
- [ ] Teste unitário: `generateWeeklyMeetings()`
- [ ] Teste integração: Endpoint POST `/api/classes/{classId}/groups`
- [ ] Teste integração: Endpoint GET `/api/classes/{classId}/groups`
- [ ] Teste E2E: Criar grupo e verificar reuniões geradas

---

## 📁 Estrutura de Arquivos Criados

### Backend
```
BRISA-BACKEND/src/main/java/com/example/brisa/
├── models/
│   ├── ProjectGroupModel.java (MODIFICADO)
│   └── ProjectGroupMeetingModel.java (NOVO)
├── repositories/
│   ├── ProjectGroupMeetingRepository.java (NOVO)
│   └── PeopleProjectGroupRepository.java (NOVO)
├── services/
│   └── ProjectGroupService.java (NOVO)
├── controllers/
│   └── ProjectGroupController.java (NOVO)
└── dtos/
    ├── ProjectGroupCreateRequestDTO.java (NOVO)
    ├── ProjectGroupResponseDTO.java (NOVO)
    └── ProjectGroupDetailResponseDTO.java (NOVO)
```

### Frontend
```
BRISA-FRONTEND/src/
├── components/
│   └── GroupCreateModal.vue (NOVO)
├── services/
│   └── groupService.js (NOVO)
└── views/
    └── ClassDetailsView.vue (MODIFICADO)
```

---

## 🔌 Endpoints Disponíveis

### Create Group
```bash
POST /api/classes/3/groups
Content-Type: application/json

{
  "projectTheme": "App de Delivery",
  "description": "Plataforma para entrega de alimentos",
  "projectCompanyId": 1,
  "leaderId": 5,
  "memberIds": [10, 11, 12],
  "weeklyMeetingDay": 1,
  "firstMeetingDate": "2026-05-20",
  "repositoryLink": "https://github.com/..."
}
```

### List Groups
```bash
GET /api/classes/3/groups
```

### Get Group Detail
```bash
GET /api/classes/3/groups/1
```

### Update Group
```bash
PUT /api/classes/3/groups/1
Content-Type: application/json
{ /* campos a atualizar */ }
```

### Delete Group
```bash
DELETE /api/classes/3/groups/1
```

### Get Immersion Students
```bash
GET /api/classes/3/immersion-students
```

---

## 📝 Notas Importantes

1. **Geração de Reuniões**:
   - Reuniões são criadas automaticamente quando o grupo é criado
   - Status inicial: `SCHEDULED`
   - Período: De `firstMeetingDate` até `classEndDate`
   - Frequência: Uma por semana no `weeklyMeetingDay`

2. **Filtro de Alunos**:
   - Frontend carrega automáticamente alunos da imersão
   - Backend valida que selecionados estão realmente na imersão
   - Usa `StageCandidateModel` com `stage.name = "imersao"`

3. **Deletar Grupo**:
   - Delete CASCADE em:
     - `project_group_meetings` (todas as reuniões)
     - `people_project_groups` (todos os membros)
   - O grupo em si é deletado

4. **Conventions**:
   - Use `groupService` para todas as chamadas API no frontend
   - Frontend passa `classId` em todas as chamadas
   - Backend valida `classId` antes de processar
   - Respostas sempre incluem `message` + `data`

---

## 🐛 Troubleshooting

### Modal não abre
- Verificar se `showGroupCreateModal` está em `true`
- Verificar imports em `ClassDetailsView.vue`

### Erro ao criar grupo
- Ver console do browser (F12) para mensagem de erro
- Verificar se todos os campos estão preenchidos
- Confirmar se backend está rodando

### Alunos não aparecem
- Verificar se existem pessoas na etapa de imersão
- Conferir se `StageCandidateModel` está criado corretamente
- Verificar nome da etapa (deve ser "imersao" em lowercase)

### Reuniões não são geradas
- Verificar se `classEndDate` está definida
- Verificar se `firstMeetingDate` < `classEndDate`
- Ver logs do backend em `ProjectGroupService.generateWeeklyMeetings()`

---

## 📞 Contato / Suporte

Código criado em: **2026-05-14 18:36**  
Desenvolvedor: **Claude Copilot (Haiku 4.5)**

Para continuar a Fase 2, comece pela **Listagem de Grupos na UI** (prioridade 1).

## 🛠️ Evolução recente (2026-05-15)

- Implementado carregamento dinâmico de grupos de imersão no frontend (Fase 2 - item 1):
  - Arquivo modificado: `BRISA-FRONTEND/src/views/ClassDetailsView.vue`
  - Função adicionada: `loadImersaoGroups()` — chama `groupService.getGroupsByClass(classId)` e mapeia resposta para `imersaoGroups`.
  - Gatilho: observa `etapasSubTab` e carrega grupos quando `'imersao'` é selecionado.
  - Atualiza cartões de métricas (`imersaoMetricsCards`) com contagem de grupos e alunos.

- Substituídos dados de exemplo por carregamento real via API (mock removido).

- Observações:
  - A implementação é defensiva quanto ao formato da resposta da API (aceita `response.data` como array ou objeto com `groups`).
  - Erros de rede são logados no console; pode-se exibir feedback visual se desejar.

- Próximos passos recomendados:
  1. Ajustar a renderização da lista (UX) conforme design
  2. Implementar detalhe do grupo (GET /groups/{id}) e UI de expansão
  3. Criar modal de edição e endpoint de atualização (PUT)
  4. Implementar deleção com confirmação e testes de integração

---

**Status**: Fase 1: Concluída. Fase 2: Em progresso (Listagem de grupos - frontend implementada).


