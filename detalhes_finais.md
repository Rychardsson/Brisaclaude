# Detalhes finais — passos a partir de agora

1. Confirmar o escopo exato (BRISA-FRONTEND, BRISA-BACKEND e se o bundle Brisa-ONE entra ou permanece apenas como protótipo visual).
2. Fazer o inventário completo de mocks e dados fixos no frontend e no bundle separado, com busca por "mock", "fake", "dummy" e arrays hardcoded.
3. Mapear os mocks já encontrados e seus locais: `BRISA-FRONTEND/src/views/ProgramRegistration/ProgramRegistrationView.vue` (stageList default em `restoreDefaultStages`), `BRISA-FRONTEND/src/views/ClassDetailsView.vue` (flag `useRealImersaoGroups` e dados de imersão), `Brisa-ONE/src/app/pages/pessoas.tsx` (const `mockPessoas`, `tabs`, `stats`).
4. Definir o contrato de API para cada bloco de dados hoje mockado (quais campos, nomes e formatos) e alinhar com o banco/DTOs existentes.
5. Verificar no backend se já existem endpoints e retornos suficientes; quando faltar, ajustar controllers/DTOs/services para expor os dados reais.
6. Ajustar os services do frontend para consumir os endpoints corretos e normalizar os campos para o formato usado nas telas.
7. Substituir os mocks no BRISA-FRONTEND: remover `restoreDefaultStages` fixo e carregar etapas via API; ativar carregamento real de grupos de imersão e eliminar fallback mock; garantir estado de loading/empty.
8. Decidir o destino do Brisa-ONE: se entrar no escopo, substituir `mockPessoas` e stats por consumo de API; se ficar fora, manter como protótipo e registrar que não entra no fluxo oficial.
9. Remover flags, comentários e dados fixos relacionados a mocks para evitar regressão.
10. Garantir conexão com o banco (PostgreSQL), seeds necessários e dados reais para validação visual das telas.
11. Rodar a aplicação ponta a ponta (backend + frontend) e conferir Programas, Turmas, Pessoas e Imersão com dados reais, sem qualquer mock.

## Alterações realizadas

- **2026-05-19:** Em `BRISA-FRONTEND/src/views/ClassDetailsView.vue`, o botão "Enviar mensagem" do banner de alertas do nivelamento foi ligado ao mesmo modal do botão principal (`showSendMessageModal = true`).
- **2026-05-19:** Em `BRISA-BACKEND/src/main/java/com/example/brisa/dtos/program/ProgramOverviewItemDTO.java` e `.../services/ProgramIntegrationService.java`, adicionados contadores de `inscricao` e `selecao` por etapa para suportar o destaque correto das fases.
- **2026-05-19:** Em `BRISA-FRONTEND/src/views/ProgramsView.vue`, o destaque das etapas passou a considerar se há alunos por etapa (inscrição/seleção/nivelamento/imersão/encerrado) e os conectores seguem a última etapa ativa.
- **2026-05-19:** Em `BRISA-FRONTEND/src/views/ClassDetailsView.vue`, o ciclo do programa agora marca etapas como ativas quando há alunos na etapa correspondente.
- **2026-05-19:** Em `BRISA-FRONTEND/src/views/ProgramsView.vue` e `BRISA-FRONTEND/src/views/ClassDetailsView.vue`, a etapa **Inscrição** passa a ficar verde quando a data de início (`applicationStartDate`) já foi atingida.
- **2026-05-19:** Em `BRISA-FRONTEND/src/views/ClassDetailsView.vue`, o modal de detalhes do curso ganhou botão "Enviar mensagem" com assunto/mensagem pré-preenchidos pelo curso; o modal de envio agora usa `v-model` para assunto, corpo e destinatários.
- **2026-05-19:** Em `BRISA-FRONTEND/src/views/ClassDetailsView.vue`, o modal de envio de mensagem recebeu `z-index` maior (`modal-overlay-top`) para ficar acima do modal do curso.
- **2026-05-19:** Em `BRISA-FRONTEND/src/views/ProgramsView.vue` e `BRISA-FRONTEND/src/views/ClassDetailsView.vue`, o botão "Pessoas" agora envia `turmaId` na query ao abrir `/people`, permitindo seleção direta da turma na tela Pessoas.
- **2026-05-19:** Em `BRISA-FRONTEND/src/components/figma-generated/pages/pessoas.vue`, adicionado filtro avançado "Turma", leitura de query params (`turmaId`/`programa`) na montagem e filtragem de pessoas por turma (usa enrollments para determinar vínculo).
- **2026-05-19:** Em `BRISA-FRONTEND/src/views/ProgramsView.vue` e `BRISA-FRONTEND/src/views/ClassDetailsView.vue`, o redirecionamento para `/people` passou a enviar **ambos** os filtros (`programaId` e `turmaId`) quando disponíveis.
- **2026-05-19:** Em `BRISA-FRONTEND/src/components/figma-generated/pages/pessoas.vue`, a tela Pessoas passou a aplicar os filtros avançados de **programa e turma** via query params e também no processamento de `filteredPeople`.
- **2026-05-19:** No backend (`PeopleModel`, `PeopleRepository`, `PeopleService`, `PeopleIntegrationService`), implementado **soft delete** para pessoa com coluna `soft_deleted` e exclusão lógica (`true/false`), além de leitura padrão apenas de pessoas ativas.
- **2026-05-19:** Em `BRISA-FRONTEND/src/components/figma-generated/pages/pessoas.vue`, o botão de 3 pontos por linha ganhou menu de ações com opção **Apagar pessoa** (chama `DELETE /people/:id`, agora soft delete), com confirmação e recarga da listagem.
- **2026-05-19:** Em `BRISA-FRONTEND/src/views/PessoaPerfilView.vue`, voltou o botão de **3 pontinhos** na tela de pessoa única, com menu contendo **Apagar pessoa** (soft delete) e confirmação.
- **2026-05-19:** Removido de vez o fluxo de detalhes de curso `/programs/:programId/classes/:classId/courses/:courseId`: excluídos `CourseDetailsView.vue`, rota `CourseDetails`, cliente `courseService.getCourseDetails`, endpoint backend `GET /api/courses/{courseId}/details/class/{classId}` (`CourseController` + `CourseService`) e DTOs exclusivos desse fluxo (`CourseDetailsDTO` e `CourseStudentProgressionDTO`).
- **2026-05-19:** Em `BRISA-FRONTEND/src/views/ProgramsView.vue`, o botão **Dashboard** da turma deixou de abrir `/home` e passou a abrir `/dashboard` com os filtros avançados já aplicados via query (`programaId` e `turmaId`, além do rótulo `programa`).
