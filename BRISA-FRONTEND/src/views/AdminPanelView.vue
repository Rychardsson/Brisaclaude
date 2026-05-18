<template>
  <div class="admin-panel-page">
    <div class="admin-panel-shell">
    <section class="page-header-card">
      <div class="page-header-top">
      <div class="header-content">
        <p class="eyebrow">Backoffice</p>
        <h1>Painel de Administração</h1>
        <p class="hero-text">
          Centralize as rotinas operacionais do sistema com foco em submissão de planilhas,
          ajustes rápidos no catálogo de cursos e cadastro manual de acessos.
        </p>
      </div>

      <div class="top-actions">
        <a href="/Modelo_Planilha_Pessoas.xlsx" download class="ghost-btn hero-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="7 10 12 15 17 10"></polyline>
            <line x1="12" y1="15" x2="12" y2="3"></line>
          </svg>
          Baixar planilha modelo
        </a>

        <div class="global-status-card" :class="globalStatusClass">
          <span class="status-dot"></span>
          <div>
            <strong>{{ globalStatusTitle }}</strong>
            <p>{{ globalStatusText }}</p>
          </div>
        </div>
      </div>
      </div>
    </section>

    <section class="stats-grid">
      <article class="summary-card summary-card-teal">
        <span class="summary-label">Submissões recentes</span>
        <strong class="summary-value">{{ formatNumber(historyRows.length) }}</strong>
        <span class="summary-note">{{ historySourceLabel }}</span>
      </article>

      <article class="summary-card summary-card-ink">
        <span class="summary-label">Cursos no catálogo</span>
        <strong class="summary-value">{{ formatNumber(courses.length) }}</strong>
        <span class="summary-note">{{ formatNumber(courseStats.withoutArea) }} sem área definida</span>
      </article>

      <article class="summary-card summary-card-amber">
        <span class="summary-label">Usuários carregados</span>
        <strong class="summary-value">{{ formatNumber(createdUsers.length) }}</strong>
        <span class="summary-note">Lista inicial vinda da API do backoffice</span>
      </article>

      <article class="summary-card summary-card-sand">
        <span class="summary-label">Última carga</span>
        <strong class="summary-value summary-value-small">{{ latestSubmissionHeadline }}</strong>
        <span class="summary-note">{{ latestSubmissionDetail }}</span>
      </article>
    </section>

    <section class="table-card">
      <div class="tabs-bar">
        <button
          type="button"
          class="tab-item"
          :class="{ active: activeTab === 'imports' }"
          @click="activeTab = 'imports'"
        >
          Importação de Planilhas
        </button>
        <button
          type="button"
          class="tab-item"
          :class="{ active: activeTab === 'courses' }"
          @click="activeTab = 'courses'"
        >
          Cursos
        </button>
        <button
          type="button"
          class="tab-item"
          :class="{ active: activeTab === 'users' }"
          @click="activeTab = 'users'"
        >
          Usuários
        </button>
      </div>

      <div v-if="activeTab === 'imports'" class="workspace-body imports-body">
        <div class="imports-layout">
          <div class="main-column">
            <section class="section-card">
              <div class="section-header">
                <div>
                  <h2>Submissão de planilhas</h2>
                  <p class="section-copy">
                    A primeira versão do backoffice concentra a importação de pessoas e orienta o administrador
                    para os fluxos específicos de etapas e progressões já existentes no sistema.
                  </p>
                </div>

                <span class="section-badge">Importação principal: Pessoas</span>
              </div>

              <div class="import-lanes">
                <article class="lane-card lane-card-active">
                  <div class="lane-head">
                    <span class="lane-title">Pessoas</span>
                    <span class="lane-pill">Aqui no painel</span>
                  </div>
                  <p class="lane-copy">
                    Atualize em massa a base de pessoas usando a planilha modelo já homologada no sistema.
                  </p>
                </article>

                <article class="lane-card">
                  <div class="lane-head">
                    <span class="lane-title">Candidatos por etapa</span>
                    <span class="lane-pill lane-pill-muted">Módulo dedicado</span>
                  </div>
                  <p class="lane-copy">
                    Use o fluxo de Programas e Etapas quando a planilha depender da turma e da etapa selecionada.
                  </p>
                  <button type="button" class="lane-link" @click="openProgramsModule">
                    Abrir Programas
                  </button>
                </article>

                <article class="lane-card">
                  <div class="lane-head">
                    <span class="lane-title">Progressões e cursos da turma</span>
                    <span class="lane-pill lane-pill-muted">Módulo dedicado</span>
                  </div>
                  <p class="lane-copy">
                    Continue usando o fluxo por turma quando a atualização de planilha for vinculada a cursos e progressões.
                  </p>
                  <button type="button" class="lane-link" @click="openProgramsModule">
                    Abrir Turmas e Cursos
                  </button>
                </article>
              </div>

              <div
                class="dropzone"
                :class="dropzoneClass"
                @click="openFilePicker"
                @dragover.prevent="handleDragOver"
                @dragleave.prevent="handleDragLeave"
                @drop.prevent="handleDrop"
              >
                <input
                  ref="fileInput"
                  type="file"
                  class="hidden-input"
                  accept=".xlsx,.xls,.csv"
                  @change="handleFileSelection"
                />

                <template v-if="uploadState === 'idle' && !selectedFile">
                  <div class="dropzone-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                      <path d="M12 16V4"></path>
                      <path d="m7 9 5-5 5 5"></path>
                      <path d="M20 16.5A4.5 4.5 0 0 0 17.5 8h-1A6.5 6.5 0 1 0 4 14.3"></path>
                      <path d="M8 19h8"></path>
                    </svg>
                  </div>
                  <strong>Arraste a planilha aqui ou clique para selecionar</strong>
                  <p>Formatos aceitos: .xlsx, .xls e .csv</p>
                </template>

                <template v-else-if="isUploadBusy">
                  <div class="dropzone-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                      <path d="M21 12a9 9 0 1 1-3.6-7.2"></path>
                      <path d="M21 3v6h-6"></path>
                    </svg>
                  </div>
                  <strong>{{ selectedFile?.name }}</strong>
                  <p>{{ uploadStatusText }}</p>
                  <div class="progress-shell">
                    <div class="progress-fill" :style="{ width: `${uploadProgress}%` }"></div>
                  </div>
                </template>

                <template v-else-if="uploadState === 'success' && uploadResult">
                  <div class="dropzone-icon success">
                    <svg xmlns="http://www.w3.org/2000/svg" width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                      <path d="M20 6 9 17l-5-5"></path>
                    </svg>
                  </div>
                  <strong>{{ selectedFile?.name }}</strong>
                  <p>
                    {{ uploadResult.successfullyInserted || 0 }} registro(s) inserido(s)
                    <span v-if="uploadResult.alreadyExists"> · {{ uploadResult.alreadyExists }} já existia(m)</span>
                  </p>
                </template>

                <template v-else-if="uploadState === 'error'">
                  <div class="dropzone-icon warning">
                    <svg xmlns="http://www.w3.org/2000/svg" width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                      <circle cx="12" cy="12" r="10"></circle>
                      <path d="M12 8v4"></path>
                      <path d="M12 16h.01"></path>
                    </svg>
                  </div>
                  <strong>Não foi possível concluir a importação</strong>
                  <p>{{ uploadError }}</p>
                </template>

                <template v-else>
                  <div class="dropzone-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                      <polyline points="14 2 14 8 20 8"></polyline>
                    </svg>
                  </div>
                  <strong>{{ selectedFile?.name }}</strong>
                  <p>{{ previewSummary }}</p>
                </template>
              </div>

              <div class="actions-row">
                <button type="button" class="ghost-btn" @click="openFilePicker">
                  Selecionar arquivo
                </button>
                <button type="button" class="ghost-btn" :disabled="isUploadBusy" @click="resetImportPanel">
                  Limpar
                </button>
                <button
                  type="button"
                  class="primary-btn"
                  :disabled="!canSubmitImport"
                  @click="submitPeopleImport"
                >
                  {{ isUploadBusy ? 'Enviando...' : 'Confirmar submissão' }}
                </button>
              </div>

              <div v-if="previewHeaders.length && previewRows.length" class="preview-block">
                <div class="preview-header">
                  <h3>Prévia da planilha</h3>
                  <span>{{ previewRowCount }} linha(s) detectada(s)</span>
                </div>

                <div class="table-scroll">
                  <table class="data-table compact-table">
                    <thead>
                      <tr>
                        <th v-for="header in previewHeaders" :key="header">{{ header }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, rowIndex) in previewRows" :key="`preview-${rowIndex}`">
                        <td v-for="(cell, cellIndex) in row" :key="`cell-${rowIndex}-${cellIndex}`">
                          {{ cell || '-' }}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </section>

            <section class="section-card">
              <div class="section-header">
                <div>
                  <h2>Histórico de submissões</h2>
                  <p class="section-copy">
                    {{ historySourceLabel }} para manter rastreabilidade das últimas cargas e reprocessamentos.
                  </p>
                </div>

                <button type="button" class="ghost-btn" @click="loadImportHistory">
                  Atualizar histórico
                </button>
              </div>

              <div v-if="importHistoryLoading" class="empty-shell">
                <div class="spinner"></div>
                <span>Carregando histórico...</span>
              </div>

              <div v-else-if="historyRows.length === 0" class="empty-shell">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 11H5a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-6a2 2 0 0 0-2-2h-4"></path>
                  <path d="M12 2v12"></path>
                  <path d="m16 10-4 4-4-4"></path>
                </svg>
                <span>Nenhuma submissão recente encontrada.</span>
              </div>

              <div v-else class="table-scroll">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Origem</th>
                      <th>Tipo</th>
                      <th>Arquivo / registro</th>
                      <th>Data</th>
                      <th>Status</th>
                      <th>Resumo</th>
                      <th>Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in historyRows" :key="item.id">
                      <td>{{ item.sourceLabel }}</td>
                      <td>{{ item.typeLabel }}</td>
                      <td>{{ item.fileName }}</td>
                      <td>{{ formatDateTime(item.createdAt) }}</td>
                      <td>
                        <span class="status-pill" :class="statusPillClass(item.status)">
                          {{ item.statusLabel }}
                        </span>
                      </td>
                      <td>{{ item.summary }}</td>
                      <td>
                        <button
                          v-if="item.status === 'warning' && item.duplicatePersons?.length"
                          type="button"
                          class="text-btn"
                          @click="downloadDuplicateReport(item)"
                        >
                          Baixar relatório
                        </button>
                        <span v-else class="muted-text">-</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </section>
          </div>

          <div class="side-column">
            <section class="section-card state-card">
              <div class="section-header tight">
                <div>
                  <h2>Estado atual</h2>
                  <p class="section-copy">A zona de submissão muda de acordo com o momento da operação.</p>
                </div>
              </div>

              <div class="state-list">
                <article class="state-row-card" :class="{ active: uploadState === 'idle' }">
                  <strong>Ocioso</strong>
                  <p>Aguardando uma nova seleção de planilha.</p>
                </article>
                <article class="state-row-card" :class="{ active: uploadState === 'validating' || uploadState === 'uploading' }">
                  <strong>Validando / Enviando</strong>
                  <p>Evite sair da página até a conclusão do envio.</p>
                </article>
                <article class="state-row-card" :class="{ active: uploadState === 'processing' }">
                  <strong>Processando</strong>
                  <p>Você pode navegar entre as abas enquanto a resposta final é aguardada.</p>
                </article>
                <article class="state-row-card" :class="{ active: uploadState === 'success' }">
                  <strong>Concluído com sucesso</strong>
                  <p>O resumo da carga fica disponível no histórico logo abaixo.</p>
                </article>
                <article class="state-row-card" :class="{ active: uploadState === 'error' }">
                  <strong>Concluído com atenção</strong>
                  <p>Reveja a mensagem, ajuste a planilha e submeta novamente.</p>
                </article>
              </div>
            </section>

            <section class="section-card">
              <div class="section-header tight">
                <div>
                  <h2>Atalhos operacionais</h2>
                  <p class="section-copy">Acesse rapidamente os módulos que já tratam fluxos específicos do sistema.</p>
                </div>
              </div>

              <div class="shortcut-list">
                <button type="button" class="shortcut-card" @click="openPeopleModule">
                  <strong>Pessoas</strong>
                  <span>Consultar base completa e cadastros detalhados.</span>
                </button>
                <button type="button" class="shortcut-card" @click="openProgramsModule">
                  <strong>Programas e Turmas</strong>
                  <span>Acessar etapas, turmas e imports vinculados ao contexto de cada programa.</span>
                </button>
                <button type="button" class="shortcut-card" @click="openLogsModule">
                  <strong>Logs do sistema</strong>
                  <span>Auditar eventos administrativos e conferências de importação.</span>
                </button>
              </div>
            </section>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'courses'" class="workspace-body">
        <section class="section-card">
          <div class="section-header">
            <div>
              <h2>Gestão de cursos</h2>
              <p class="section-copy">
                Catálogo global para ajustes rápidos e criação manual sem depender do fluxo por turma.
              </p>
            </div>

            <button type="button" class="primary-btn" @click="openCourseDrawer()">
              + Novo curso
            </button>
          </div>

          <div class="toolbar">
            <div class="search-shell">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              <input v-model="courseSearch" type="text" placeholder="Buscar por nome, descrição ou área..." />
            </div>

            <div class="meta-inline">
              <span>{{ formatNumber(courseStats.total) }} curso(s)</span>
              <span>{{ formatNumber(courseStats.totalWorkload) }}h somadas</span>
            </div>
          </div>

          <div v-if="coursesLoading" class="empty-shell">
            <div class="spinner"></div>
            <span>Carregando cursos...</span>
          </div>

          <div v-else-if="coursesError" class="feedback-banner feedback-error">
            {{ coursesError }}
          </div>

          <div v-else-if="filteredCourses.length === 0" class="empty-shell">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
              <path d="M22 6.5V17.5A2.5 2.5 0 0 1 19.5 20H6.5A2.5 2.5 0 0 1 4 17.5V6.5A2.5 2.5 0 0 1 6.5 4H19.5A2.5 2.5 0 0 1 22 6.5Z"></path>
            </svg>
            <span>Nenhum curso encontrado para essa busca.</span>
          </div>

          <div v-else class="table-scroll">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>Área</th>
                  <th>Subárea</th>
                  <th>Carga horária</th>
                  <th>Descrição</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="course in filteredCourses" :key="course.id">
                  <td>{{ course.name }}</td>
                  <td>{{ course.knowledgeArea?.name || 'Não definida' }}</td>
                  <td>{{ course.subArea?.name || '-' }}</td>
                  <td>{{ formatNumber(course.workload || 0) }}h</td>
                  <td>{{ truncate(course.description, 74) }}</td>
                  <td class="actions-cell">
                    <button type="button" class="text-btn" @click="openCourseDrawer(course)">
                      Editar
                    </button>
                    <button type="button" class="text-btn danger" @click="removeCourse(course)">
                      Excluir
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>

      <div v-else class="workspace-body">
        <section class="section-card">
          <div class="section-header">
            <div>
              <h2>Usuários do sistema</h2>
              <p class="section-copy">
                Primeira etapa do backoffice focada em criação manual e consulta rápida de contas já existentes.
              </p>
            </div>

            <button type="button" class="primary-btn" @click="openUserModal">
              + Novo usuário
            </button>
          </div>

          <div class="feedback-banner feedback-info">
            O backend atual já suporta cadastro e busca por login. A edição avançada de permissões poderá ser conectada depois, sem refazer esta interface.
          </div>

          <div class="toolbar users-toolbar">
            <div class="search-shell">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              <input
                v-model="userSearch"
                type="text"
                placeholder="Buscar por login..."
                @keyup.enter="searchUsers"
              />
            </div>

            <select v-model="userRoleFilter" class="filter-select">
              <option value="ALL">Todos os perfis</option>
              <option value="ADMIN">Administrador</option>
              <option value="USER">Usuário</option>
            </select>

            <button type="button" class="ghost-btn" @click="searchUsers">
              Pesquisar
            </button>
          </div>

          <div v-if="userError" class="feedback-banner feedback-error">
            {{ userError }}
          </div>

          <div v-if="usersLoading" class="empty-shell">
            <div class="spinner"></div>
            <span>Buscando usuários...</span>
          </div>

          <div v-else-if="visibleUsers.length === 0" class="empty-shell">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M20 8v6"></path>
              <path d="M23 11h-6"></path>
            </svg>
            <span>Crie um usuário manualmente ou pesquise por um login existente.</span>
          </div>

          <div v-else class="user-grid">
            <article
              v-for="user in visibleUsers"
              :key="user.id"
              class="user-card"
              @click="openUserDetails(user)"
            >
              <div class="user-card-top">
                <div class="avatar-badge">
                  {{ initials(user.login) }}
                </div>
                <div class="user-main">
                  <strong>{{ user.login }}</strong>
                  <span>{{ user.email || 'E-mail carregado sob demanda' }}</span>
                </div>
              </div>

              <div class="user-meta">
                <span class="status-pill" :class="statusPillClass(user.enabled === false ? 'warning' : 'success')">
                  {{ user.enabled === false ? 'Acesso desativado' : 'Acesso ativo' }}
                </span>
                <span class="status-pill neutral">
                  {{ user.role || 'USER' }}
                </span>
              </div>

              <div class="user-card-footer">
                <span>{{ user.verifiedEmail ? 'E-mail verificado' : 'E-mail pendente' }}</span>
                <button type="button" class="text-btn" @click.stop="openUserDetails(user)">
                  Ver detalhes
                </button>
              </div>
            </article>
          </div>
        </section>
      </div>
    </section>

    <Teleport to="body">
      <div v-if="courseDrawerOpen" class="drawer-overlay" @click="closeCourseDrawer">
        <aside class="drawer-panel" @click.stop>
          <div class="drawer-header">
            <div>
              <h2>{{ courseForm.id ? 'Editar curso' : 'Novo curso' }}</h2>
              <p>Cadastre rapidamente cursos que depois poderão ser vinculados às turmas.</p>
            </div>
            <button type="button" class="drawer-close" @click="closeCourseDrawer">×</button>
          </div>

          <div class="drawer-body">
            <label class="field">
              <span>Nome *</span>
              <input v-model="courseForm.name" type="text" placeholder="Ex: Lógica de Programação" />
            </label>

            <label class="field">
              <span>Carga horária *</span>
              <input v-model="courseForm.workload" type="number" min="0" step="1" placeholder="Ex: 40" />
            </label>

            <label class="field">
              <span>Descrição</span>
              <textarea v-model="courseForm.description" rows="6" placeholder="Resumo do conteúdo ou objetivo do curso"></textarea>
            </label>

            <div class="drawer-note">
              Esta versão usa apenas os campos já suportados pelo backend atual para não conflitar com outras frentes do time.
            </div>

            <div v-if="courseFormError" class="feedback-banner feedback-error">
              {{ courseFormError }}
            </div>
          </div>

          <div class="drawer-footer">
            <button type="button" class="ghost-btn" @click="closeCourseDrawer">
              Cancelar
            </button>
            <button type="button" class="primary-btn" :disabled="courseSaving" @click="saveCourse">
              {{ courseSaving ? 'Salvando...' : 'Salvar curso' }}
            </button>
          </div>
        </aside>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showUserModal" class="modal-overlay" @click="closeUserModal">
        <div class="modal-card" @click.stop>
          <div class="modal-header">
            <div>
              <h2>Novo usuário</h2>
              <p>Crie acessos manuais para equipe administrativa ou operacional.</p>
            </div>
            <button type="button" class="drawer-close" @click="closeUserModal">×</button>
          </div>

          <div class="modal-body">
            <div class="form-grid">
              <label class="field">
                <span>Login *</span>
                <input v-model="userForm.login" type="text" placeholder="usuario.backoffice" />
              </label>

              <label class="field">
                <span>E-mail *</span>
                <input v-model="userForm.email" type="email" placeholder="usuario@empresa.com" />
              </label>

              <label class="field">
                <span>Senha *</span>
                <input v-model="userForm.password" type="password" placeholder="Defina uma senha" />
              </label>

              <label class="field">
                <span>Gênero *</span>
                <select v-model="userForm.gender">
                  <option value="">Selecione</option>
                  <option value="MASCULINO">Masculino</option>
                  <option value="FEMININO">Feminino</option>
                  <option value="OUTRO">Outro</option>
                </select>
              </label>

              <label class="field full-span">
                <span>Data de nascimento *</span>
                <input v-model="userForm.dateOfBirth" type="date" />
              </label>
            </div>

            <div class="drawer-note">
              Novos usuários entram com perfil padrão de uso definido pelo backend atual.
            </div>

            <div v-if="userFormError" class="feedback-banner feedback-error">
              {{ userFormError }}
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="ghost-btn" @click="closeUserModal">
              Cancelar
            </button>
            <button type="button" class="primary-btn" :disabled="userSaving" @click="saveUser">
              {{ userSaving ? 'Criando...' : 'Criar usuário' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="selectedUser" class="modal-overlay" @click="closeUserDetails">
        <div class="modal-card details-card" @click.stop>
          <div class="modal-header">
            <div>
              <h2>{{ selectedUser.login }}</h2>
              <p>Dados de consulta rápida da conta no sistema.</p>
            </div>
            <button type="button" class="drawer-close" @click="closeUserDetails">×</button>
          </div>

          <div class="modal-body details-body">
            <div class="details-grid">
              <div class="detail-item">
                <span>Login</span>
                <strong>{{ selectedUser.login }}</strong>
              </div>
              <div class="detail-item">
                <span>E-mail</span>
                <strong>{{ selectedUser.email || '-' }}</strong>
              </div>
              <div class="detail-item">
                <span>Perfil</span>
                <strong>{{ selectedUser.role || 'USER' }}</strong>
              </div>
              <div class="detail-item">
                <span>Status</span>
                <strong>{{ selectedUser.enabled === false ? 'Desativado' : 'Ativo' }}</strong>
              </div>
              <div class="detail-item">
                <span>Verificação</span>
                <strong>{{ selectedUser.verifiedEmail ? 'E-mail verificado' : 'Pendente' }}</strong>
              </div>
              <div class="detail-item">
                <span>Nascimento</span>
                <strong>{{ selectedUser.dateOfBirth ? formatShortDate(selectedUser.dateOfBirth) : '-' }}</strong>
              </div>
              <div class="detail-item">
                <span>Gênero</span>
                <strong>{{ formatGender(selectedUser.gender) }}</strong>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div class="toast-stack">
        <div v-for="toast in toasts" :key="toast.id" class="toast-card" :class="toast.tone">
          <strong>{{ toast.title }}</strong>
          <span>{{ toast.message }}</span>
        </div>
      </div>
    </Teleport>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import * as XLSX from 'xlsx';
import api from '@/services/api';
import { courseService } from '@/services/courseService';
import { logService } from '@/services/logService';
import { peopleService } from '@/services/peopleService';

const router = useRouter();

const ADMIN_IMPORT_HISTORY_KEY = 'admin-panel-import-history';
const activeTab = ref('imports');
const toasts = ref([]);

const courses = ref([]);
const coursesLoading = ref(false);
const coursesError = ref('');
const courseSearch = ref('');
const courseDrawerOpen = ref(false);
const courseSaving = ref(false);
const courseFormError = ref('');
const courseForm = ref({
  id: null,
  name: '',
  description: '',
  workload: ''
});

const fileInput = ref(null);
const isDragging = ref(false);
const selectedFile = ref(null);
const uploadState = ref('idle');
const uploadProgress = ref(0);
const uploadError = ref('');
const uploadResult = ref(null);
const previewHeaders = ref([]);
const previewRows = ref([]);
const previewRowCount = ref(0);
const sessionImportHistory = ref(readStoredList(ADMIN_IMPORT_HISTORY_KEY));
const remoteImportHistory = ref([]);
const importHistoryLoading = ref(false);

const createdUsers = ref([]);
const usersLoading = ref(false);
const userError = ref('');
const userSearch = ref('');
const userRoleFilter = ref('ALL');
const searchedUsers = ref([]);
const selectedUser = ref(null);
const showUserModal = ref(false);
const userSaving = ref(false);
const userFormError = ref('');
const userForm = ref({
  login: '',
  email: '',
  password: '',
  gender: '',
  dateOfBirth: ''
});

let uploadProgressTimer = null;
let toastCounter = 0;

const isUploadBusy = computed(() => ['validating', 'uploading', 'processing'].includes(uploadState.value));

const dropzoneClass = computed(() => ({
  dragging: isDragging.value,
  busy: isUploadBusy.value,
  success: uploadState.value === 'success',
  warning: uploadState.value === 'error'
}));

const canSubmitImport = computed(() => Boolean(selectedFile.value) && !isUploadBusy.value);

const uploadStatusText = computed(() => {
  if (uploadState.value === 'validating') return 'Validando estrutura da planilha...';
  if (uploadState.value === 'uploading') return 'Enviando arquivo para o servidor...';
  if (uploadState.value === 'processing') return 'Processando dados e aguardando resposta final...';
  return 'Pronto para envio';
});

const previewSummary = computed(() => {
  if (!selectedFile.value) return 'Nenhum arquivo selecionado';
  if (!previewRowCount.value) return 'Arquivo pronto para submissão';
  return `${previewRowCount.value} linha(s) identificada(s) para conferência rápida`;
});

const historyRows = computed(() => (
  sessionImportHistory.value.length > 0 ? sessionImportHistory.value : remoteImportHistory.value
));

const historySourceLabel = computed(() => (
  sessionImportHistory.value.length > 0
    ? 'Exibindo o histórico salvo neste navegador para a operação do backoffice'
    : 'Exibindo os últimos registros de importação encontrados nos logs do sistema'
));

const courseStats = computed(() => ({
  total: courses.value.length,
  totalWorkload: courses.value.reduce((acc, course) => acc + Number(course.workload || 0), 0),
  withoutArea: courses.value.filter((course) => !course.knowledgeArea?.name).length
}));

const filteredCourses = computed(() => {
  const term = normalize(courseSearch.value);
  if (!term) return courses.value;
  return courses.value.filter((course) => {
    const haystack = [
      course.name,
      course.description,
      course.knowledgeArea?.name,
      course.subArea?.name
    ].map(normalize).join(' ');
    return haystack.includes(term);
  });
});

const visibleUsers = computed(() => {
  const source = searchedUsers.value.length > 0 || userSearch.value.trim()
    ? searchedUsers.value
    : createdUsers.value;

  return source.filter((user) => {
    if (userRoleFilter.value === 'ALL') return true;
    return String(user.role || 'USER').toUpperCase() === userRoleFilter.value;
  });
});

const latestSubmission = computed(() => historyRows.value[0] || null);

const latestSubmissionHeadline = computed(() => {
  if (!latestSubmission.value) return 'Nenhuma ainda';
  return latestSubmission.value.status === 'warning'
    ? 'Concluída com atenção'
    : latestSubmission.value.status === 'error'
      ? 'Falha registrada'
      : 'Concluída com sucesso';
});

const latestSubmissionDetail = computed(() => {
  if (!latestSubmission.value) return 'Selecione uma planilha para começar.';
  return latestSubmission.value.summary;
});

const globalStatusTitle = computed(() => {
  if (isUploadBusy.value) return 'Processamento em andamento';
  if (uploadState.value === 'success') return 'Última carga concluída';
  if (uploadState.value === 'error') return 'Carga concluída com atenção';
  return 'Sem processamentos ativos';
});

const globalStatusText = computed(() => {
  if (isUploadBusy.value) return '1 planilha em processamento no momento.';
  if (uploadState.value === 'success' && uploadResult.value) {
    return `${uploadResult.value.successfullyInserted || 0} registro(s) inserido(s) na última submissão.`;
  }
  if (uploadState.value === 'error' && uploadError.value) return uploadError.value;
  return 'Nenhum envio em background nesta tela agora.';
});

const globalStatusClass = computed(() => {
  if (isUploadBusy.value) return 'status-processing';
  if (uploadState.value === 'success') return 'status-success';
  if (uploadState.value === 'error') return 'status-warning';
  return 'status-idle';
});

onMounted(async () => {
  window.addEventListener('beforeunload', handleBeforeUnload);
  await Promise.all([loadCourses(), loadImportHistory(), loadUsers()]);
});

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);
  stopUploadProgressSimulation();
});

function readStoredList(key) {
  try {
    const raw = localStorage.getItem(key);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    return [];
  }
}

function writeStoredList(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}

function normalize(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .trim();
}

function formatNumber(value) {
  return new Intl.NumberFormat('pt-BR').format(Number(value || 0));
}

function formatDateTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(date);
}

function formatShortDate(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(date);
}

function formatGender(value) {
  if (!value) return '-';
  const normalized = String(value).toUpperCase();
  if (normalized === 'MASCULINO') return 'Masculino';
  if (normalized === 'FEMININO') return 'Feminino';
  if (normalized === 'OUTRO') return 'Outro';
  return String(value);
}

function truncate(value, maxLength = 64) {
  const text = String(value || '').trim();
  if (!text) return '-';
  return text.length > maxLength ? `${text.slice(0, maxLength)}...` : text;
}

function initials(value) {
  const parts = String(value || '')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2);
  return parts.map((part) => part.charAt(0).toUpperCase()).join('') || 'US';
}

function statusPillClass(status) {
  if (status === 'success') return 'success';
  if (status === 'warning') return 'warning';
  if (status === 'error') return 'error';
  return 'neutral';
}

function pushToast(title, message, tone = 'info') {
  const id = `${Date.now()}-${toastCounter += 1}`;
  toasts.value.push({ id, title, message, tone });
  window.setTimeout(() => {
    toasts.value = toasts.value.filter((toast) => toast.id !== id);
  }, 4200);
}

function handleBeforeUnload(event) {
  if (!isUploadBusy.value) return;
  event.preventDefault();
  event.returnValue = '';
}

function openProgramsModule() {
  router.push('/programs');
}

function openPeopleModule() {
  router.push('/people');
}

function openLogsModule() {
  router.push('/logs');
}

async function loadCourses() {
  coursesLoading.value = true;
  coursesError.value = '';
  try {
    const response = await courseService.getAll();
    courses.value = Array.isArray(response) ? response : [];
  } catch (error) {
    coursesError.value = `Erro ao carregar cursos: ${error.response?.data?.message || error.message}`;
  } finally {
    coursesLoading.value = false;
  }
}

function openCourseDrawer(course = null) {
  courseFormError.value = '';
  courseForm.value = course
    ? {
        id: course.id,
        name: course.name || '',
        description: course.description || '',
        workload: course.workload ?? ''
      }
    : {
        id: null,
        name: '',
        description: '',
        workload: ''
      };
  courseDrawerOpen.value = true;
}

function closeCourseDrawer() {
  courseDrawerOpen.value = false;
  courseFormError.value = '';
  courseSaving.value = false;
}

async function saveCourse() {
  if (!courseForm.value.name.trim()) {
    courseFormError.value = 'Informe o nome do curso.';
    return;
  }

  const workload = Number(courseForm.value.workload);
  if (Number.isNaN(workload) || workload < 0) {
    courseFormError.value = 'Informe uma carga horária válida.';
    return;
  }

  courseSaving.value = true;
  courseFormError.value = '';

  const payload = {
    name: courseForm.value.name.trim(),
    description: courseForm.value.description.trim(),
    workload
  };

  try {
    if (courseForm.value.id) {
      await courseService.update(courseForm.value.id, payload);
      pushToast('Curso atualizado', 'As alterações foram salvas no catálogo.', 'success');
    } else {
      await courseService.create(payload);
      pushToast('Curso criado', 'O novo curso já está disponível para uso.', 'success');
    }
    await loadCourses();
    closeCourseDrawer();
  } catch (error) {
    courseFormError.value = error.response?.data?.message || error.message || 'Não foi possível salvar o curso.';
  } finally {
    courseSaving.value = false;
  }
}

async function removeCourse(course) {
  const confirmed = window.confirm(`Deseja excluir o curso "${course.name}"?`);
  if (!confirmed) return;

  try {
    await courseService.delete(course.id);
    pushToast('Curso removido', 'O curso foi excluído do catálogo.', 'success');
    await loadCourses();
  } catch (error) {
    pushToast('Erro ao excluir', error.response?.data?.message || error.message, 'error');
  }
}

function openFilePicker() {
  fileInput.value?.click();
}

function handleDragOver() {
  isDragging.value = true;
}

function handleDragLeave() {
  isDragging.value = false;
}

function handleDrop(event) {
  isDragging.value = false;
  const file = event.dataTransfer?.files?.[0];
  if (file) {
    void setSelectedFile(file);
  }
}

async function handleFileSelection(event) {
  const file = event.target.files?.[0];
  if (file) {
    await setSelectedFile(file);
  }
}

async function setSelectedFile(file) {
  selectedFile.value = file;
  uploadState.value = 'idle';
  uploadProgress.value = 0;
  uploadError.value = '';
  uploadResult.value = null;
  previewHeaders.value = [];
  previewRows.value = [];
  previewRowCount.value = 0;

  try {
    const workbook = XLSX.read(await file.arrayBuffer(), { type: 'array' });
    const sheetName = workbook.SheetNames[0];
    if (!sheetName) return;

    const sheet = workbook.Sheets[sheetName];
    const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });
    const meaningfulRows = rows.filter(
      (row) => Array.isArray(row) && row.some((cell) => String(cell ?? '').trim() !== '')
    );

    if (!meaningfulRows.length) return;

    previewHeaders.value = (meaningfulRows[0] || []).map((header, index) => {
      const value = String(header || '').trim();
      return value || `Coluna ${index + 1}`;
    });

    previewRows.value = meaningfulRows.slice(1, 6).map((row) => (
      previewHeaders.value.map((_, index) => String(row[index] ?? '').trim())
    ));

    previewRowCount.value = Math.max(meaningfulRows.length - 1, 0);
  } catch (error) {
    previewHeaders.value = [];
    previewRows.value = [];
    previewRowCount.value = 0;
  }
}

function resetImportPanel() {
  stopUploadProgressSimulation();
  uploadState.value = 'idle';
  uploadProgress.value = 0;
  uploadError.value = '';
  uploadResult.value = null;
  selectedFile.value = null;
  previewHeaders.value = [];
  previewRows.value = [];
  previewRowCount.value = 0;
  if (fileInput.value) {
    fileInput.value.value = '';
  }
}

function startUploadProgressSimulation() {
  stopUploadProgressSimulation();
  uploadProgressTimer = window.setInterval(() => {
    if (uploadProgress.value < 26) {
      uploadState.value = 'validating';
    } else if (uploadProgress.value < 66) {
      uploadState.value = 'uploading';
    } else {
      uploadState.value = 'processing';
    }

    if (uploadProgress.value < 90) {
      uploadProgress.value += Math.max(2, Math.round((92 - uploadProgress.value) / 8));
    }
  }, 260);
}

function stopUploadProgressSimulation() {
  if (uploadProgressTimer) {
    window.clearInterval(uploadProgressTimer);
    uploadProgressTimer = null;
  }
}

function persistImportHistory(items) {
  sessionImportHistory.value = items;
  writeStoredList(ADMIN_IMPORT_HISTORY_KEY, items);
}

function appendImportHistory(item) {
  const nextItems = [item, ...sessionImportHistory.value].slice(0, 12);
  persistImportHistory(nextItems);
}

async function submitPeopleImport() {
  if (!selectedFile.value || isUploadBusy.value) return;

  uploadState.value = 'validating';
  uploadProgress.value = 12;
  uploadError.value = '';
  uploadResult.value = null;
  startUploadProgressSimulation();

  try {
    const response = await peopleService.importExcel(selectedFile.value);
    stopUploadProgressSimulation();
    uploadProgress.value = 100;
    uploadState.value = 'success';
    uploadResult.value = response;

    const historyItem = {
      id: `local-${Date.now()}`,
      sourceLabel: 'Sessão atual',
      typeLabel: 'Pessoas',
      fileName: selectedFile.value.name,
      createdAt: new Date().toISOString(),
      status: Number(response.alreadyExists || 0) > 0 ? 'warning' : 'success',
      statusLabel: Number(response.alreadyExists || 0) > 0 ? 'Concluído com alerta' : 'Concluído',
      summary: `${response.successfullyInserted || 0} inserido(s) · ${response.alreadyExists || 0} já existente(s)`,
      duplicatePersons: response.duplicatePersons || []
    };

    appendImportHistory(historyItem);
    pushToast(
      'Importação concluída',
      `${response.successfullyInserted || 0} registro(s) foram inseridos com sucesso.`,
      Number(response.alreadyExists || 0) > 0 ? 'warning' : 'success'
    );

    if (activeTab.value !== 'imports') {
      pushToast('Atualização finalizada', 'A planilha terminou de ser processada em background nesta tela.', 'info');
    }
  } catch (error) {
    stopUploadProgressSimulation();
    uploadProgress.value = 100;
    uploadState.value = 'error';
    uploadError.value = error.response?.data?.message || error.message || 'Falha ao importar a planilha.';

    appendImportHistory({
      id: `local-${Date.now()}`,
      sourceLabel: 'Sessão atual',
      typeLabel: 'Pessoas',
      fileName: selectedFile.value.name,
      createdAt: new Date().toISOString(),
      status: 'error',
      statusLabel: 'Falha',
      summary: truncate(uploadError.value, 96),
      duplicatePersons: []
    });

    pushToast('Falha na importação', uploadError.value, 'error');
  } finally {
    await loadImportHistory();
  }
}

function mapImportLog(log) {
  let parsedDetails = {};
  if (typeof log.details === 'string') {
    try {
      parsedDetails = JSON.parse(log.details);
    } catch (error) {
      parsedDetails = {};
    }
  } else if (log.details && typeof log.details === 'object') {
    parsedDetails = log.details;
  }

  const successCount = Number(parsedDetails.successCount || 0);
  const errorCount = Number(parsedDetails.errorCount || 0);
  const action = String(log.action || '');

  return {
    id: `log-${log.id}`,
    sourceLabel: 'Logs do sistema',
    typeLabel: action === 'STAGE_CANDIDATES_IMPORT' ? 'Candidatos da etapa' : 'Pessoas',
    fileName: 'Registro automático',
    createdAt: log.createdAt,
    status: errorCount > 0 ? 'warning' : 'success',
    statusLabel: errorCount > 0 ? 'Concluído com alerta' : 'Concluído',
    summary: successCount || errorCount
      ? `${successCount} sucesso(s) · ${errorCount} ocorrência(s)`
      : truncate(log.description, 96),
    duplicatePersons: []
  };
}

async function loadImportHistory() {
  importHistoryLoading.value = true;
  try {
    const response = await logService.getLogs({
      page: 0,
      size: 16,
      sortBy: 'createdAt',
      sortDirection: 'DESC'
    });

    const items = Array.isArray(response?.content) ? response.content : Array.isArray(response) ? response : [];
    remoteImportHistory.value = items
      .filter((log) => String(log.action || '').includes('IMPORT'))
      .map(mapImportLog);
  } catch (error) {
    remoteImportHistory.value = [];
  } finally {
    importHistoryLoading.value = false;
  }
}

function downloadDuplicateReport(item = null) {
  const source = item || {
    duplicatePersons: uploadResult.value?.duplicatePersons || []
  };

  const duplicates = Array.isArray(source.duplicatePersons) ? source.duplicatePersons : [];
  if (!duplicates.length) return;

  const workbook = XLSX.utils.book_new();
  const worksheet = XLSX.utils.aoa_to_sheet([
    ['Nome', 'Motivo'],
    ...duplicates.map((name) => [name, 'Cadastro já existente no sistema'])
  ]);

  XLSX.utils.book_append_sheet(workbook, worksheet, 'Conflitos');
  XLSX.writeFile(workbook, `relatorio-importacao-${Date.now()}.xlsx`);
}

function normalizeUserRecord(data) {
  return {
    id: data.id,
    login: data.login || data.username || '',
    email: data.email || '',
    role: data.role || 'USER',
    verifiedEmail: Boolean(data.verifiedEmail),
    gender: data.gender || '',
    dateOfBirth: data.dateOfBirth || '',
    enabled: data.enabled !== undefined ? data.enabled : true,
    profilePicture: data.profilePicture || ''
  };
}

async function loadUsers() {
  usersLoading.value = true;
  userError.value = '';

  try {
    const response = await api.get('/users', {
      params: {
        limit: 24
      }
    });

    createdUsers.value = Array.isArray(response.data)
      ? response.data.map(normalizeUserRecord)
      : [];
  } catch (error) {
    createdUsers.value = [];
    userError.value = error.response?.data?.message || error.message || 'Erro ao carregar usuários.';
  } finally {
    usersLoading.value = false;
  }
}

async function searchUsers() {
  const query = userSearch.value.trim();
  if (query.length < 2) {
    searchedUsers.value = [];
    userError.value = 'Digite pelo menos 2 caracteres para pesquisar.';
    return;
  }

  usersLoading.value = true;
  userError.value = '';

  try {
    const response = await api.get(`/users/search/${encodeURIComponent(query)}`);
    const baseResults = Array.isArray(response.data) ? response.data : [];

    const detailedResults = await Promise.all(
      baseResults.slice(0, 8).map(async (user) => {
        try {
          const detailResponse = await api.get(`/users/${user.id}`);
          return normalizeUserRecord(detailResponse.data);
        } catch (error) {
          return normalizeUserRecord(user);
        }
      })
    );

    searchedUsers.value = detailedResults;

    if (!detailedResults.length) {
      userError.value = 'Nenhum usuário encontrado para essa busca.';
    }
  } catch (error) {
    if (error.response?.status === 204) {
      searchedUsers.value = [];
      userError.value = 'Nenhum usuário encontrado para essa busca.';
    } else {
      searchedUsers.value = [];
      userError.value = error.response?.data?.message || error.message || 'Erro ao buscar usuários.';
    }
  } finally {
    usersLoading.value = false;
  }
}

function openUserModal() {
  userFormError.value = '';
  userForm.value = {
    login: '',
    email: '',
    password: '',
    gender: '',
    dateOfBirth: ''
  };
  showUserModal.value = true;
}

function closeUserModal() {
  showUserModal.value = false;
  userSaving.value = false;
  userFormError.value = '';
}

async function saveUser() {
  if (!userForm.value.login.trim() || !userForm.value.email.trim() || !userForm.value.password || !userForm.value.gender || !userForm.value.dateOfBirth) {
    userFormError.value = 'Preencha todos os campos obrigatórios.';
    return;
  }

  userSaving.value = true;
  userFormError.value = '';

  try {
    const response = await api.post('/auth/register', {
      login: userForm.value.login.trim(),
      email: userForm.value.email.trim(),
      password: userForm.value.password,
      gender: userForm.value.gender,
      dateOfBirth: userForm.value.dateOfBirth
    });

    const normalized = normalizeUserRecord(response.data);
    await loadUsers();
    pushToast('Usuário criado', `A conta "${normalized.login}" foi cadastrada com sucesso.`, 'success');
    closeUserModal();
  } catch (error) {
    userFormError.value = error.response?.data?.message || error.response?.data || error.message || 'Não foi possível criar o usuário.';
  } finally {
    userSaving.value = false;
  }
}

async function openUserDetails(user) {
  selectedUser.value = normalizeUserRecord(user);

  if (user.email && user.role && user.dateOfBirth) return;

  try {
    const response = await api.get(`/users/${user.id}`);
    selectedUser.value = normalizeUserRecord(response.data);
  } catch (error) {
    pushToast('Detalhes parciais', 'Alguns dados do usuário não puderam ser carregados agora.', 'warning');
  }
}

function closeUserDetails() {
  selectedUser.value = null;
}
</script>

<style scoped>
.admin-panel-page {
  --bg: #eef3f8;
  --surface: #ffffff;
  --surface-strong: #ffffff;
  --ink: #13233f;
  --muted: #6a7a90;
  --line: #dfe7f1;
  --teal: #0f766e;
  --teal-soft: #dff4ee;
  --amber: #d97706;
  --amber-soft: #fff1db;
  --red: #c2410c;
  --red-soft: #ffede7;
  --sand: #ede4d6;
  --navy: #1f285f;
  min-height: calc(100vh - 64px);
  padding: 14px 16px 20px;
  background: #eef3f8;
  color: var(--ink);
}

.admin-panel-shell {
  max-width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header-card,
.table-card,
.summary-card,
.section-card,
.modal-card,
.drawer-panel {
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: 0 8px 24px rgba(13, 27, 42, 0.05);
}

.page-header-card {
  display: grid;
  gap: 14px;
  padding: 18px 18px 16px;
  border-radius: 20px;
}

.page-header-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.header-content {
  min-width: min(720px, 100%);
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--muted);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.header-content h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.1;
  color: var(--ink);
  font-weight: 800;
}

.subtitle {
  margin: 6px 0 0;
  max-width: 760px;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.6;
}

.hero-text {
  margin: 6px 0 0;
  max-width: 760px;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.6;
}

.top-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.hero-btn {
  min-width: 240px;
  width: auto;
  align-self: flex-end;
  justify-content: center;
}

.global-status-card {
  min-width: min(320px, 100%);
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid transparent;
}

.global-status-card strong {
  display: block;
  font-size: 15px;
}

.global-status-card p {
  margin: 4px 0 0;
  color: inherit;
  opacity: 0.86;
  font-size: 13px;
  line-height: 1.45;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  flex-shrink: 0;
}

.status-idle {
  background: #f8fafc;
  border-color: #d8e1eb;
  color: #475569;
}

.status-idle .status-dot {
  background: #94a3b8;
}

.status-processing {
  background: #ecfdf5;
  border-color: #b7ebcd;
  color: #0f766e;
}

.status-processing .status-dot {
  background: #0f766e;
  box-shadow: 0 0 0 8px rgba(15, 118, 110, 0.12);
}

.status-success {
  background: #effcf5;
  border-color: #bbf7d0;
  color: #166534;
}

.status-success .status-dot {
  background: #22c55e;
}

.status-warning {
  background: #fff7ed;
  border-color: #fed7aa;
  color: #b45309;
}

.status-warning .status-dot {
  background: #f59e0b;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.summary-card {
  border-radius: 14px;
  padding: 14px 14px 12px;
  min-height: 102px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.summary-card-teal {
  background: linear-gradient(180deg, #ffffff 0%, #f4fdf9 100%);
}

.summary-card-ink {
  background: linear-gradient(180deg, #ffffff 0%, #f6f8ff 100%);
}

.summary-card-amber {
  background: linear-gradient(180deg, #ffffff 0%, #fffaf1 100%);
}

.summary-card-sand {
  background: linear-gradient(180deg, #ffffff 0%, #fbfaf7 100%);
}

.summary-label {
  color: var(--muted);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.summary-value {
  font-size: 28px;
  line-height: 1.05;
  color: var(--ink);
  font-weight: 800;
}

.summary-value-small {
  font-size: 23px;
}

.summary-note {
  color: #8a98ab;
  font-size: 11px;
  line-height: 1.45;
}

.table-card {
  border-radius: 20px;
  overflow: hidden;
}

.tabs-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px 0;
  border-bottom: 1px solid #e2eaf2;
  overflow-x: auto;
}

.tab-item {
  appearance: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  color: #5f728d;
  font: inherit;
  font-weight: 600;
  padding: 12px 2px 14px;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  white-space: nowrap;
}

.tab-item:hover {
  color: #2a3566;
}

.tab-item.active {
  color: var(--teal);
  border-bottom-color: #14b8a6;
}

.tab-label {
  display: inline-flex;
  align-items: center;
}

.tab-count {
  background: #eef2f7;
  color: #8a98ab;
  border-radius: 999px;
  padding: 2px 7px;
  font-size: 11px;
  font-weight: 700;
}

.tab-item.active .tab-count {
  color: #14b8a6;
  background: #e7f8f5;
}

.workspace-body {
  padding: 18px;
}

.imports-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(300px, 0.85fr);
  gap: 16px;
}

.main-column,
.side-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  border-radius: 16px;
  padding: 18px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.section-header.tight {
  margin-bottom: 14px;
}

.section-header h2,
.drawer-header h2,
.modal-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--ink);
}

.section-copy,
.drawer-header p,
.modal-header p {
  margin: 8px 0 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.55;
}

.section-badge {
  flex-shrink: 0;
  border-radius: 999px;
  background: #edf7f4;
  color: var(--teal);
  padding: 9px 12px;
  font-size: 12px;
  font-weight: 700;
}

.import-lanes {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.lane-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px;
  background: var(--surface-strong);
}

.lane-card-active {
  background: linear-gradient(180deg, #ffffff 0%, #f4fdf9 100%);
  border-color: #b7ebcd;
}

.lane-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.lane-title {
  font-weight: 800;
  color: var(--ink);
}

.lane-pill {
  border-radius: 999px;
  background: var(--teal-soft);
  color: var(--teal);
  padding: 5px 9px;
  font-size: 11px;
  font-weight: 700;
}

.lane-pill-muted {
  background: #eef2f6;
  color: #64748b;
}

.lane-copy {
  min-height: 72px;
  margin: 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.55;
}

.lane-link,
.text-btn {
  appearance: none;
  border: 0;
  background: transparent;
  color: var(--teal);
  cursor: pointer;
  font: inherit;
  font-weight: 700;
  padding: 0;
  margin-top: 14px;
}

.text-btn.danger {
  color: var(--red);
}

.dropzone {
  position: relative;
  min-height: 232px;
  border: 2px dashed #d9e2ec;
  border-radius: 20px;
  background:
    linear-gradient(180deg, #ffffff 0%, #f8fbfd 100%),
    radial-gradient(circle at top right, rgba(20, 184, 166, 0.08), transparent 32%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
  padding: 24px;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.dropzone:hover,
.dropzone.dragging {
  transform: translateY(-1px);
  border-color: #8fdccd;
  box-shadow: inset 0 0 0 1px rgba(20, 184, 166, 0.08);
}

.dropzone.busy {
  cursor: progress;
}

.dropzone.success {
  border-color: rgba(34, 197, 94, 0.36);
  background: linear-gradient(180deg, #f8fff9 0%, #edf9f0 100%);
}

.dropzone.warning {
  border-color: rgba(245, 158, 11, 0.36);
  background: linear-gradient(180deg, #fffdf7 0%, #fff8ea 100%);
}

.dropzone strong {
  font-size: 18px;
  color: var(--ink);
}

.dropzone p {
  margin: 0;
  max-width: 420px;
  color: var(--muted);
  line-height: 1.55;
}

.dropzone-icon {
  width: 72px;
  height: 72px;
  border-radius: 22px;
  display: grid;
  place-items: center;
  color: var(--teal);
  background: rgba(15, 118, 110, 0.10);
}

.dropzone-icon.success {
  color: #16a34a;
  background: rgba(34, 197, 94, 0.12);
}

.dropzone-icon.warning {
  color: #d97706;
  background: rgba(245, 158, 11, 0.12);
}

.hidden-input {
  display: none;
}

.progress-shell {
  width: min(100%, 420px);
  height: 10px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.08);
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #14b8a6 0%, #0f766e 100%);
  transition: width 0.22s ease;
}

.actions-row,
.drawer-footer,
.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 16px;
}

.actions-row > .ghost-btn,
.actions-row > .primary-btn,
.drawer-footer > .ghost-btn,
.drawer-footer > .primary-btn,
.modal-footer > .ghost-btn,
.modal-footer > .primary-btn {
  min-width: 150px;
}

.preview-block {
  margin-top: 18px;
  border-top: 1px solid rgba(31, 40, 95, 0.08);
  padding-top: 16px;
}

.preview-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
}

.preview-header span {
  color: var(--muted);
  font-size: 13px;
}

.table-scroll {
  width: 100%;
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 760px;
}

.data-table th,
.data-table td {
  padding: 14px 12px;
  border-bottom: 1px solid #e6edf5;
  text-align: left;
  vertical-align: top;
  font-size: 14px;
}

.data-table th {
  color: var(--muted);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.compact-table {
  min-width: 520px;
}

.compact-table th,
.compact-table td {
  padding: 12px 10px;
}

.actions-cell {
  white-space: nowrap;
}

.muted-text {
  color: #94a3b8;
}

.empty-shell {
  min-height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px dashed #d7dfea;
  border-radius: 16px;
  color: var(--muted);
  text-align: center;
  background: #fbfdff;
}

.spinner {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  border: 3px solid rgba(15, 118, 110, 0.18);
  border-top-color: var(--teal);
  animation: spin 0.8s linear infinite;
}

.state-card {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(249, 250, 247, 0.98) 100%),
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.08), transparent 30%);
}

.state-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.state-row-card {
  border: 1px solid rgba(31, 40, 95, 0.08);
  border-radius: 16px;
  padding: 14px;
  background: var(--surface-strong);
}

.state-row-card strong {
  display: block;
  font-size: 14px;
}

.state-row-card p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.5;
}

.state-row-card.active {
  border-color: rgba(15, 118, 110, 0.26);
  box-shadow: inset 0 0 0 1px rgba(15, 118, 110, 0.04);
}

.shortcut-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.shortcut-card {
  appearance: none;
  border: 1px solid rgba(31, 40, 95, 0.08);
  background: var(--surface-strong);
  border-radius: 16px;
  padding: 14px;
  text-align: left;
  cursor: pointer;
  font: inherit;
  color: var(--ink);
}

.shortcut-card strong {
  display: block;
  margin-bottom: 6px;
}

.shortcut-card span {
  display: block;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.45;
}

.shortcut-card:hover {
  border-color: rgba(15, 118, 110, 0.26);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.users-toolbar {
  align-items: center;
  justify-content: flex-start;
}

.search-shell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 280px;
  flex: 1;
  min-height: 40px;
  background: var(--surface-strong);
  border: 1px solid #d9e2ec;
  border-radius: 12px;
  padding: 0 14px;
}

.search-shell svg {
  color: #97a4b6;
  flex-shrink: 0;
}

.search-shell input,
.field input,
.field textarea,
.field select,
.filter-select {
  width: 100%;
  border: 0;
  background: transparent;
  color: var(--ink);
  font: inherit;
  outline: none;
}

.search-shell input,
.filter-select {
  min-height: 40px;
}

.filter-select {
  appearance: none;
  min-width: 210px;
  width: auto;
  flex: 0 0 210px;
}

.meta-inline {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--muted);
  font-size: 13px;
}

.feedback-banner {
  border-radius: 14px;
  padding: 14px 16px;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 14px;
}

.feedback-info {
  background: #eef6ff;
  color: #1d4ed8;
}

.feedback-error {
  background: #fff2ef;
  color: #c2410c;
}

.user-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.user-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
  background: var(--surface-strong);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.user-card:hover {
  transform: translateY(-1px);
  border-color: #9be1d8;
  box-shadow: 0 8px 18px rgba(20, 184, 166, 0.08);
}

.user-card-top {
  display: flex;
  gap: 12px;
  align-items: center;
}

.avatar-badge {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #1f285f 0%, #0f766e 100%);
  color: white;
  font-weight: 800;
  flex-shrink: 0;
}

.user-main {
  min-width: 0;
}

.user-main strong,
.user-main span {
  display: block;
}

.user-main span {
  color: var(--muted);
  font-size: 13px;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
}

.status-pill.success {
  background: #ecfdf5;
  color: #166534;
}

.status-pill.warning {
  background: #fff7ed;
  color: #b45309;
}

.status-pill.error {
  background: #fff1f2;
  color: #be123c;
}

.status-pill.neutral {
  background: #eef2f6;
  color: #475569;
}

.user-card-footer {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--muted);
  font-size: 13px;
}

.ghost-btn,
.primary-btn {
  appearance: none;
  height: 40px;
  border-radius: 12px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  line-height: 1;
  white-space: nowrap;
  flex-shrink: 0;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.ghost-btn {
  border: 1px solid #d1dbe8;
  background: var(--surface-strong);
  color: var(--ink);
}

.primary-btn {
  border: 1px solid var(--teal);
  background: #14b8a6;
  color: white;
  box-shadow: 0 8px 16px rgba(20, 184, 166, 0.20);
}

.ghost-btn:hover,
.primary-btn:hover,
.lane-link:hover,
.text-btn:hover,
.shortcut-card:hover {
  transform: translateY(-1px);
}

.ghost-btn:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.primary-btn:hover {
  background: #0d9488;
  border-color: #0d9488;
  box-shadow: 0 12px 20px rgba(13, 148, 136, 0.24);
}

.ghost-btn:disabled,
.primary-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.drawer-overlay,
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.42);
  backdrop-filter: blur(3px);
  z-index: 1200;
  display: flex;
  justify-content: flex-end;
}

.modal-overlay {
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.drawer-panel {
  width: min(100%, 520px);
  height: 100%;
  border-radius: 28px 0 0 28px;
  padding: 22px;
  display: flex;
  flex-direction: column;
}

.modal-card {
  width: min(100%, 620px);
  border-radius: 24px;
  padding: 22px;
}

.details-card {
  width: min(100%, 720px);
}

.drawer-header,
.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(31, 40, 95, 0.08);
}

.drawer-close {
  appearance: none;
  border: 0;
  background: #eef2f6;
  color: #334155;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}

.drawer-body,
.modal-body {
  padding-top: 16px;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
}

.field {
  display: block;
}

.field + .field {
  margin-top: 14px;
}

.field span {
  display: block;
  margin-bottom: 8px;
  color: var(--muted);
  font-size: 13px;
  font-weight: 700;
}

.field input,
.field textarea,
.field select,
.filter-select {
  box-sizing: border-box;
  border: 1px solid #cfd7e6;
  border-radius: 12px;
  background: var(--surface-strong);
  padding: 12px 14px;
}

.field textarea {
  resize: vertical;
}

.drawer-note {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f7f8fb;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.55;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.full-span {
  grid-column: 1 / -1;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-item {
  border: 1px solid rgba(31, 40, 95, 0.08);
  border-radius: 16px;
  padding: 14px;
  background: var(--surface-strong);
}

.detail-item span {
  display: block;
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.detail-item strong {
  display: block;
  margin-top: 8px;
  color: var(--ink);
  font-size: 15px;
}

.toast-stack {
  position: fixed;
  right: 22px;
  bottom: 22px;
  z-index: 1400;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast-card {
  width: min(360px, calc(100vw - 32px));
  padding: 14px 16px;
  border-radius: 16px;
  box-shadow: 0 20px 38px rgba(15, 23, 42, 0.16);
  background: white;
  border: 1px solid rgba(31, 40, 95, 0.08);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toast-card strong {
  font-size: 14px;
}

.toast-card span {
  font-size: 13px;
  line-height: 1.45;
}

.toast-card.success {
  border-color: rgba(34, 197, 94, 0.28);
}

.toast-card.warning {
  border-color: rgba(245, 158, 11, 0.28);
}

.toast-card.error {
  border-color: rgba(244, 63, 94, 0.28);
}

.toast-card.info {
  border-color: rgba(15, 118, 110, 0.22);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1180px) {
  .imports-layout {
    grid-template-columns: 1fr;
  }

  .top-actions {
    align-items: stretch;
  }

  .hero-btn {
    width: 100%;
    align-self: stretch;
  }

  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .import-lanes,
  .user-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .admin-panel-page {
    padding: 16px 14px 24px;
  }

  .page-header-card,
  .workspace-body,
  .section-card,
  .modal-card {
    padding: 16px;
  }

  .header-content h1 {
    font-size: 30px;
  }

  .stats-grid,
  .form-grid,
  .details-grid {
    grid-template-columns: 1fr;
  }

  .section-header,
  .actions-row,
  .modal-footer,
  .drawer-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-shell,
  .filter-select {
    min-width: 0;
  }

  .hero-btn,
  .actions-row > .ghost-btn,
  .actions-row > .primary-btn,
  .drawer-footer > .ghost-btn,
  .drawer-footer > .primary-btn,
  .modal-footer > .ghost-btn,
  .modal-footer > .primary-btn {
    width: 100%;
    min-width: 0;
  }

  .filter-select {
    width: 100%;
    flex-basis: auto;
  }

  .drawer-panel {
    width: 100%;
    border-radius: 24px 24px 0 0;
    height: auto;
    max-height: 92vh;
    margin-top: auto;
  }

  .tabs-bar {
    gap: 0;
  }
}
</style>
