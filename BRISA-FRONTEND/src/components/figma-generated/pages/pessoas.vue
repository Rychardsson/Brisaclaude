<template>
  <div class="people-page">
    <div class="people-shell">
      <section class="page-header-card">
        <div class="page-header-top">
          <div>
            <h1>Pessoas</h1>
            <p class="subtitle">
              Gerencie todas as pessoas cadastradas no sistema e acompanhe seus vínculos com programas e etapas.
            </p>
          </div>

          <div class="top-actions">
          <button type="button" class="ghost-btn" @click="showTemplateModal = true">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="7 10 12 15 17 10"></polyline>
              <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
            Modelo de planilha
          </button>

            <button type="button" class="ghost-btn" @click="showCreateModal = true">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="8.5" cy="7" r="4"></circle>
                <line x1="20" y1="8" x2="20" y2="14"></line>
                <line x1="23" y1="11" x2="17" y2="11"></line>
              </svg>
              Nova pessoa
            </button>

            <button type="button" class="primary-btn" @click="showUploadModal = true">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              Submeter planilha
            </button>
          </div>
        </div>

        <div class="stats-grid">
          <article class="stat-card stat-card-primary">
            <div class="stat-label">Total de Pessoas</div>
            <div class="stat-row stat-row-single">
              <div class="stat-value">{{ formatNumber(totalPeople) }}</div>
            </div>
            <div class="stat-note">Pessoas cadastradas</div>
          </article>

          <article class="stat-card stat-card-success">
            <div class="stat-label">Pessoas Ativas</div>
            <div class="stat-row">
              <div class="stat-value">{{ formatNumber(activePeopleCount) }}</div>
              <div class="stat-trend">{{ formatTrend(activePeopleTrend) }}</div>
            </div>
          </article>

          <article class="stat-card stat-card-info">
            <div class="stat-label">Em Programas Ativos</div>
            <div class="stat-row">
              <div class="stat-value">{{ formatNumber(activeProgramsCount) }}</div>
              <div class="stat-trend">{{ formatTrend(activeProgramsTrend) }}</div>
            </div>
          </article>

          <article class="stat-card stat-card-warning">
            <div class="stat-label">Em Nivelamento</div>
            <div class="stat-row">
              <div class="stat-value">{{ formatNumber(levelingCount) }}</div>
              <div class="stat-trend">{{ formatTrend(levelingTrend) }}</div>
            </div>
          </article>

          <article class="stat-card stat-card-primary-soft">
            <div class="stat-label">Em Imersão</div>
            <div class="stat-row">
              <div class="stat-value">{{ formatNumber(immersionCount) }}</div>
              <div class="stat-trend">{{ formatTrend(immersionTrend) }}</div>
            </div>
          </article>

          <article class="stat-card stat-card-teal">
            <div class="stat-label">Novos Cadastros (30d)</div>
            <div class="stat-row">
              <div class="stat-value">{{ formatNumber(newRegistrationsCount) }}</div>
              <div class="stat-trend">{{ formatTrend(newRegistrationsTrend) }}</div>
            </div>
          </article>
        </div>
      </section>

      <section class="table-card">
        <!-- Section 1: Tabs -->
        <div class="tabs-section">
          <div class="tabs-bar">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              type="button"
              class="tab-item"
              :class="{ active: activeTab === tab.value }"
              @click="activeTab = tab.value"
            >
              {{ tab.label }}
              <span class="tab-count">{{ formatNumber(tab.count) }}</span>
            </button>
          </div>
        </div>

        <!-- Section 2: Search Bar -->
        <div class="search-section">
          <div class="filters-row">
            <div class="search-box">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="search-icon">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              <input
                v-model="searchTerm"
                type="text"
                class="search-input"
                placeholder="Buscar por nome, CPF, e-mail ou identificador..."
              />
            </div>

            <div class="filters-actions">
              <button type="button" class="filters-button" @click="showAdvancedFilters = !showAdvancedFilters">
                Filtros avançados
                <span class="filters-badge">{{ advancedFiltersCount }}</span>
              </button>

              <button type="button" class="search-button" @click="runSearch">
                Pesquisar
              </button>
            </div>
          </div>

          <div v-if="showAdvancedFilters" class="advanced-filters">
            <div class="filters-grid">
              <!-- Row 1: Programa, Etapa, Status, Cota -->
              <div class="filter-group">
                <label>Programa</label>
                <select v-model="advancedFilters.programa">
                  <option value="">Todos</option>
                  <option v-for="prog in programOptions" :key="prog.id" :value="prog.id">
                    {{ prog.name || prog.label }}
                  </option>
                </select>
              </div>
              <div class="filter-group">
                <label>Etapa</label>
                <select v-model="advancedFilters.etapa">
                  <option value="">Todas</option>
                  <option v-for="stage in stageOptions" :key="stage.id" :value="stage.id">
                    {{ stage.name || stage.label }}
                  </option>
                </select>
              </div>
              <div class="filter-group">
                <label>Status</label>
                <select v-model="advancedFilters.status">
                  <option value="">Todos</option>
                  <option v-for="status in statusOptions" :key="status" :value="status">
                    {{ status }}
                  </option>
                </select>
              </div>
              <div class="filter-group">
                <label>Cota</label>
                <select v-model="advancedFilters.cota">
                  <option value="">Todas</option>
                  <option v-for="quota in quotaOptions" :key="quota" :value="quota">
                    {{ quota }}
                  </option>
                </select>
              </div>

              <!-- Row 2: Gênero, Estado, Cidade, Instituição -->
              <div class="filter-group">
                <label>Gênero</label>
                <select v-model="advancedFilters.genero">
                  <option value="">Todos</option>
                  <option v-for="gender in genderOptions" :key="gender" :value="gender">
                    {{ gender }}
                  </option>
                </select>
              </div>
              <div class="filter-group">
                <label>Estado de residência</label>
                <select v-model="advancedFilters.estado">
                  <option value="">Todos</option>
                  <option v-for="state in stateOptions" :key="state" :value="state">
                    {{ state }}
                  </option>
                </select>
              </div>
              <div class="filter-group">
                <label>Cidade de residência</label>
                <input v-model="advancedFilters.cidade" type="text" placeholder="Digite a cidade" />
              </div>
              <div class="filter-group">
                <label>Instituição</label>
                <input v-model="advancedFilters.instituicao" type="text" placeholder="Digite a instituição" />
              </div>
            </div>
          </div>
        </div>

        <!-- Section 3: Columns & Table -->
        <div class="table-section">
          <div ref="columnsControlRef" class="columns-control">
            <button
              type="button"
              class="columns-button"
              :class="{ active: showColumnsMenu }"
              aria-haspopup="menu"
              :aria-expanded="showColumnsMenu"
              @click.stop="showColumnsMenu = !showColumnsMenu"
            >
              <Columns3 class="columns-icon" :size="16" />
              Colunas
            </button>

            <div v-if="showColumnsMenu" class="columns-menu" @click.stop>
              <label v-for="option in columnOptions" :key="option.key" class="columns-option">
                <input v-model="visibleColumns[option.key]" type="checkbox" />
                <span>{{ option.label }}</span>
              </label>
            </div>
          </div>

          <div v-if="loading" class="state-row">
            <div class="spinner"></div>
            <span>Carregando pessoas...</span>
          </div>

          <div v-else-if="error" class="state-row state-row-error">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            <span>{{ error }}</span>
          </div>

          <div v-else-if="filteredPeople.length === 0" class="state-row state-row-empty">
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <div>
              <strong>Nenhuma pessoa encontrada</strong>
              <p>Altere a busca ou os filtros para exibir registros.</p>
            </div>
          </div>

          <div v-else class="table-scroll">
          <table class="people-table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>CPF</th>
                <th>E-mail</th>
                <th v-if="visibleColumns.gender">Gênero</th>
                <th v-if="visibleColumns.age">Idade</th>
                <th v-if="visibleColumns.quota">Cota</th>
                <th v-if="visibleColumns.cityUf">Cidade/UF</th>
                <th v-if="visibleColumns.institution">Instituição</th>
                <th v-if="visibleColumns.educationLevel">Tipo de formação</th>
                <th v-if="visibleColumns.course">Curso</th>
                <th>Programas</th>
                <th>Etapa Atual</th>
                <th>Status</th>
                <th v-if="visibleColumns.updatedAt">Última atualização</th>
                <th class="actions-col">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="person in paginatedPeople" :key="person.id">
                <td>
                  <div class="person-cell">
                    <div class="person-avatar">{{ getInitials(person.name) }}</div>
                    <div class="person-main">
                      <div class="person-name">{{ person.name }}</div>
                    </div>
                  </div>
                </td>
                <td>
                  <span v-if="person.cpf" class="mono">{{ formatCPF(person.cpf) }}</span>
                  <span v-else class="muted">-</span>
                </td>
                <td>
                  <span v-if="person.email" class="cell-text">{{ person.email }}</span>
                  <span v-else class="muted">-</span>
                </td>
                <td v-if="visibleColumns.gender">
                  <span v-if="person.gender" class="cell-text">{{ formatGender(person.gender) }}</span>
                  <span v-else class="muted">-</span>
                </td>
                <td v-if="visibleColumns.age">
                  <span v-if="person.birthDate" class="cell-text">{{ formatAge(person.birthDate) }}</span>
                  <span v-else class="muted">-</span>
                </td>
                <td v-if="visibleColumns.quota">
                  <span class="cell-text">{{ formatQuota(person) }}</span>
                </td>
                <td v-if="visibleColumns.cityUf">
                  <span class="cell-text">{{ formatCityUf(person) }}</span>
                </td>
                <td v-if="visibleColumns.institution">
                  <span class="cell-text">{{ formatInstitution(person) }}</span>
                </td>
                <td v-if="visibleColumns.educationLevel">
                  <span v-if="person.educationLevel" class="cell-text">{{ person.educationLevel }}</span>
                  <span v-else class="muted">-</span>
                </td>
                <td v-if="visibleColumns.course">
                  <span class="cell-text">{{ formatCourse(person) }}</span>
                </td>
                <td>
                  <div v-if="personMeta(person).programs.length" class="chips">
                    <span v-for="program in personMeta(person).programs.slice(0, 1)" :key="program" class="chip">{{ program }}</span>
                    <span v-if="personMeta(person).extraPrograms > 0" class="chip chip-muted">+{{ personMeta(person).extraPrograms }}</span>
                  </div>
                  <span v-else class="muted">-</span>
                </td>
                <td>
                  <span v-if="personMeta(person).stage" class="stage-pill">{{ personMeta(person).stage }}</span>
                  <span v-else class="muted">-</span>
                </td>
                <td>
                  <span class="status-pill" :class="statusClass(personMeta(person).status)">
                    {{ personMeta(person).status }}
                  </span>
                </td>
                <td v-if="visibleColumns.updatedAt">
                  <span class="cell-text">{{ formatUpdatedAt(person) }}</span>
                </td>
                <td class="actions-col actions-cell">
                  <button type="button" class="icon-btn" title="Visualizar" @click="viewDetails(person)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  </button>
                  <button type="button" class="icon-btn" title="Editar" @click="editPerson(person)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                    </svg>
                  </button>
                  <button type="button" class="icon-btn" title="Mais ações" @click="openMoreActions(person)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="5" r="1"></circle>
                      <circle cx="12" cy="12" r="1"></circle>
                      <circle cx="12" cy="19" r="1"></circle>
                    </svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="filteredPeople.length > 0 && totalPages > 1" class="pagination-row">
          <div class="pagination-summary">
            Mostrando {{ pageStart }} a {{ pageEnd }} de {{ filteredPeople.length }} pessoas
          </div>

          <div class="pagination-controls">
            <button type="button" class="page-btn" :disabled="currentPage === 1" @click="prevPage">Anterior</button>

            <div class="page-numbers">
              <template v-for="(page, index) in visiblePages" :key="`${page}-${index}`">
                <span v-if="page === '...'" class="page-ellipsis">...</span>
                <button
                  v-else
                  type="button"
                  class="page-btn page-btn-number"
                  :class="{ active: page === currentPage }"
                  @click="goToPage(page)"
                >
                  {{ page }}
                </button>
              </template>
            </div>

            <button type="button" class="page-btn" :disabled="currentPage === totalPages" @click="nextPage">Próximo</button>
          </div>
        </div>
        </div>
      </section>
    </div>

    <div v-if="showUploadModal" class="modal-overlay" @click="closeUploadModal">
      <div class="modal-card modal-card-large upload-modal" @click.stop>
        <div class="modal-head">
          <div>
            <h2>Submeter planilha de alunos</h2>
            <p class="modal-subtitle">Envie uma planilha com os dados dos alunos e revise a prévia antes de confirmar o cadastro.</p>
          </div>
          <button type="button" class="modal-close" @click="closeUploadModal" aria-label="Fechar">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body upload-body">
          <div
            class="upload-area upload-area-large"
            :class="{ 'drag-over': isDragging, 'has-file': selectedFile }"
            @dragover.prevent="handleDragOver"
            @dragleave.prevent="handleDragLeave"
            @drop.prevent="handleDrop"
            @click="fileInput.click()"
          >
            <input type="file" @change="handleFileSelect" accept=".xlsx,.xls,.csv" ref="fileInput" class="hidden-input" />
            <div class="upload-icon-wrap">
              <svg v-if="!selectedFile" xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="upload-icon">
                <polyline points="16 16 12 12 8 16"></polyline>
                <line x1="12" y1="12" x2="12" y2="21"></line>
                <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"></path>
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="upload-icon upload-icon-ok">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <polyline points="9 15 11 17 15 13"></polyline>
              </svg>
            </div>
            <div class="upload-text">
              <p class="upload-main-text">{{ selectedFile ? selectedFile.name : 'Arraste a planilha aqui ou clique para selecionar' }}</p>
              <p class="upload-sub-text">{{ selectedFile ? 'Clique para trocar o arquivo' : 'Formatos aceitos: .xlsx, .csv' }}</p>
            </div>
            <button type="button" class="secondary-btn upload-select-btn" @click.stop="fileInput.click()">Selecionar arquivo</button>
          </div>

          <div class="upload-destination">
            <div class="form-grid upload-grid">
              <div class="form-group">
                <label>Programa de destino *</label>
                <select v-model="uploadForm.programaId">
                  <option value="" disabled>Selecione</option>
                  <option v-for="programa in programOptions" :key="programa.id" :value="programa.id">{{ programa.label }}</option>
                </select>
              </div>

              <div class="form-group">
                <label>Turma de destino *</label>
                <select v-model="uploadForm.turmaId">
                  <option value="" disabled>Selecione</option>
                  <option v-for="turma in classOptions" :key="turma.id" :value="turma.id">{{ turma.label }}</option>
                </select>
              </div>

              <div class="form-group">
                <label>Etapa inicial *</label>
                <select v-model="uploadForm.etapaId" :disabled="!uploadForm.turmaId || uploadStageLoading">
                  <option value="" disabled>Selecione</option>
                  <option v-for="etapa in uploadStageOptions" :key="etapa.id" :value="etapa.id">{{ etapa.label }}</option>
                </select>
              </div>

              <div class="form-group">
                <label>Status inicial *</label>
                <select v-model="uploadForm.statusInicial">
                  <option value="" disabled>Selecione</option>
                  <option v-for="status in statusOptions" :key="status" :value="status">{{ status }}</option>
                </select>
              </div>
            </div>
          </div>

          <div v-if="uploadError" class="alert alert-error">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ uploadError }}
          </div>
          <div v-if="uploadSuccess" class="alert alert-success">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
            {{ uploadSuccess }}
          </div>
          <div v-if="uploadStageError" class="alert alert-error">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ uploadStageError }}
          </div>

          <div v-if="uploadResult" class="summary-grid">
            <div class="summary-card summary-blue">
              <div class="summary-label">Total de linhas</div>
              <div class="summary-value">{{ uploadTotalCount }}</div>
            </div>
            <div class="summary-card summary-green">
              <div class="summary-label">Novos cadastros</div>
              <div class="summary-value">{{ uploadNewCount }}</div>
            </div>
            <div class="summary-card summary-amber">
              <div class="summary-label">Já existentes</div>
              <div class="summary-value">{{ uploadExistingCount }}</div>
            </div>
            <div class="summary-card summary-red">
              <div class="summary-label">Alertas</div>
              <div class="summary-value">{{ uploadAlertCount }}</div>
            </div>
          </div>

          <div v-if="uploadResult" class="upload-tabs">
            <button type="button" class="upload-tab" :class="{ active: uploadTab === 'novos' }" @click="uploadTab = 'novos'">
              Novos cadastros ({{ uploadNewCount }})
            </button>
            <button type="button" class="upload-tab" :class="{ active: uploadTab === 'existentes' }" @click="uploadTab = 'existentes'">
              Já cadastrados ({{ uploadExistingCount }})
            </button>
          </div>

          <div v-if="uploadResult" class="upload-table">
            <table v-if="uploadTab === 'novos'" class="upload-table-el">
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>CPF</th>
                  <th>E-mail</th>
                  <th>Cota</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!uploadTableRows.length">
                  <td colspan="5" class="upload-empty">Nenhum novo cadastro encontrado.</td>
                </tr>
                <tr v-for="(row, index) in uploadTableRows" :key="`novo-${index}`">
                  <td>{{ row.name || '-' }}</td>
                  <td>{{ formatCPF(row.cpf) }}</td>
                  <td>{{ row.email || '-' }}</td>
                  <td>{{ row.cota || '-' }}</td>
                  <td><span class="upload-badge upload-badge-success">{{ row.status || 'Pronto' }}</span></td>
                </tr>
              </tbody>
            </table>

            <table v-else class="upload-table-el">
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>CPF</th>
                  <th>E-mail</th>
                  <th>Alerta</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!uploadTableRows.length">
                  <td colspan="4" class="upload-empty">Nenhum cadastro existente encontrado.</td>
                </tr>
                <tr v-for="(row, index) in uploadTableRows" :key="`existente-${index}`">
                  <td>{{ row.name || '-' }}</td>
                  <td>{{ formatCPF(row.cpf) }}</td>
                  <td>{{ row.email || '-' }}</td>
                  <td><span class="upload-badge upload-badge-warning">{{ row.issue || 'Pessoa já cadastrada' }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="modal-actions upload-footer">
          <button type="button" class="secondary-btn modal-secondary" @click="closeUploadModal">Cancelar</button>
          <button type="button" class="secondary-btn" :disabled="!uploadResult || (!uploadExistingCount && !uploadAlertCount)" @click="downloadInconsistenciesReport">
            Baixar relatório de inconsistências
          </button>
          <button type="button" class="primary-btn" :disabled="uploadDisabled" @click="uploadFile">
            {{ uploading ? 'Enviando...' : 'Confirmar importação' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
      <div class="modal-card modal-card-large create-modal" @click.stop>
        <div class="modal-head">
          <div>
            <h2>Cadastrar nova pessoa</h2>
            <p class="modal-subtitle">Preencha os dados pessoais, acadêmicos e o vínculo inicial da pessoa com um programa/turma.</p>
          </div>
          <button type="button" class="modal-close" @click="closeCreateModal" aria-label="Fechar">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <form class="create-form" @submit.prevent="handleCreate">
          <div class="modal-body">
            <div class="modal-section">
              <h3 class="modal-section-title">Vínculo com programa/turma</h3>
              <div class="form-grid">
                <div class="form-group">
                  <label>Programa *</label>
                  <select v-model="createForm.programaId">
                    <option value="" disabled>Selecione</option>
                    <option v-for="programa in programOptions" :key="programa.id" :value="programa.id">{{ programa.label }}</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Turma *</label>
                  <select v-model="createForm.turmaId" required>
                    <option value="" disabled>Selecione</option>
                    <option v-for="turma in classOptions" :key="turma.id" :value="turma.id">{{ turma.label }}</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Etapa inicial *</label>
                  <select v-model="createForm.etapaId" :disabled="!createForm.turmaId || stageLoading" required>
                    <option value="" disabled>Selecione</option>
                    <option v-for="etapa in stageOptions" :key="etapa.id" :value="etapa.id">{{ etapa.label }}</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Status inicial</label>
                  <select v-model="createForm.statusInicial">
                    <option value="" disabled>Selecione</option>
                    <option v-for="status in statusOptions" :key="status" :value="status">{{ status }}</option>
                  </select>
                </div>
              </div>
            </div>

            <div class="modal-section">
              <h3 class="modal-section-title">Dados pessoais</h3>
              <div class="form-grid">
                <div class="form-group full-width">
                  <label>Nome do aluno *</label>
                  <input v-model="createForm.nome" type="text" placeholder="Digite o nome completo" required />
                </div>

                <div class="form-group">
                  <label>Data de nascimento *</label>
                  <input v-model="createForm.dataNascimento" type="date" required />
                </div>

                <div class="form-group">
                  <label>Gênero</label>
                  <select v-model="createForm.genero">
                    <option value="" disabled>Selecione</option>
                    <option v-for="genero in genderOptions" :key="genero" :value="genero">{{ genero }}</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Cota *</label>
                  <select v-model="createForm.cota">
                    <option value="" disabled>Selecione</option>
                    <option v-for="cota in quotaOptions" :key="cota" :value="cota">{{ cota }}</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>CPF *</label>
                  <input v-model="createForm.cpf" type="text" placeholder="000.000.000-00" @input="formatCPFInput" required />
                </div>

                <div class="form-group">
                  <label>E-mail *</label>
                  <input v-model="createForm.email" type="email" placeholder="email@exemplo.com" required />
                </div>

                <div class="form-group">
                  <label>Telefone</label>
                  <input v-model="createForm.telefone" type="text" placeholder="(00) 00000-0000" />
                </div>

                <div class="form-group">
                  <label>Estado de residência</label>
                  <select v-model="createForm.estado">
                    <option value="" disabled>Selecione</option>
                    <option v-for="estado in stateOptions" :key="estado" :value="estado">{{ estado }}</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Cidade de residência</label>
                  <input v-model="createForm.cidade" type="text" placeholder="Cidade" />
                </div>
              </div>
            </div>

            <div class="modal-section">
              <h3 class="modal-section-title">Dados acadêmicos</h3>
              <div class="form-grid">
                <div class="form-group full-width">
                  <label>Tipo de formação</label>
                  <select v-model="createForm.tipoFormacao">
                    <option value="" disabled>Selecione</option>
                    <option v-for="tipo in educationTypeOptions" :key="tipo" :value="tipo">{{ tipo }}</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Instituição</label>
                  <input v-model="createForm.instituicao" type="text" placeholder="Instituição" />
                </div>

                <div class="form-group">
                  <label>Curso</label>
                  <input v-model="createForm.curso" type="text" placeholder="Curso" />
                </div>

                <div class="form-group">
                  <label>Status da formação</label>
                  <select v-model="createForm.statusFormacao">
                    <option value="" disabled>Selecione</option>
                    <option v-for="status in educationStatusOptions" :key="status" :value="status">{{ status }}</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Data de conclusão</label>
                  <input v-model="createForm.dataConclusao" type="date" />
                </div>
              </div>
            </div>

            <div v-if="referenceError" class="alert alert-error">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {{ referenceError }}
            </div>

            <div v-if="stageError" class="alert alert-error">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {{ stageError }}
            </div>

            <div v-if="createError" class="alert alert-error">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {{ createError }}
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="secondary-btn modal-secondary" @click="closeCreateModal">Cancelar</button>
            <button type="submit" class="primary-btn" :disabled="createDisabled">
              {{ creating ? 'Cadastrando...' : 'Cadastrar pessoa' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showTemplateModal" class="modal-overlay" @click="showTemplateModal = false">
      <div class="modal-card modal-card-small" @click.stop>
        <div class="modal-head">
          <h2>Modelo de planilha de alunos</h2>
          <button type="button" class="modal-close" @click="showTemplateModal = false" aria-label="Fechar">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <p class="template-text">Baixe um arquivo modelo com todas as colunas necessárias para cadastro e vínculo dos alunos ao programa.</p>
        
        <div class="template-columns">
          <div class="template-columns-title">Colunas principais:</div>
          <ul class="template-columns-list">
            <li v-for="column in templateColumns" :key="column">{{ column }}</li>
          </ul>
        </div>
        
        <div class="modal-actions template-actions">
          <a 
            href="/Modelo_Planilha_Pessoas.xlsx" 
            download="Modelo_Planilha_Pessoas.xlsx" 
            class="primary-btn" 
            style="text-decoration: none; display: flex; justify-content: center; align-items: center;"
            @click="showTemplateModal = false"
          >
            Baixar modelo .xlsx
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Columns3 } from 'lucide-vue-next';
import * as XLSX from 'xlsx';
import { peopleService } from '@/services/peopleService';
import { enrollmentService } from '@/services/enrollmentService';
import { stageService } from '@/services/stageService';

const router = useRouter();

const people = ref([]);
const enrollments = ref([]);
const stageCandidates = ref([]);
const loading = ref(false);
const error = ref(null);
const searchTerm = ref('');
const activeTab = ref('active');
const showAdvancedFilters = ref(false);
const showTemplateModal = ref(false);
const showColumnsMenu = ref(false);
const columnsControlRef = ref(null);
const visibleColumns = reactive({
  gender: false,
  age: false,
  quota: false,
  cityUf: false,
  institution: false,
  educationLevel: false,
  course: false,
  updatedAt: false
});
const columnOptions = [
  { key: 'gender', label: 'Gênero' },
  { key: 'age', label: 'Idade' },
  { key: 'quota', label: 'Cota' },
  { key: 'cityUf', label: 'Cidade/UF' },
  { key: 'institution', label: 'Instituição' },
  { key: 'educationLevel', label: 'Tipo de formação' },
  { key: 'course', label: 'Curso' },
  { key: 'updatedAt', label: 'Última atualização' }
];
const templateColumns = [
  'Nome completo',
  'E-mail',
  'CPF',
  'Data de Nascimento', 
  'Gênero',
  'Cota',
  'Escolaridade',
  'Curso',
  'Tipo de formação',
  'Status da formação',
  'Cidade',
  'Estado'
];
const fallbackStatusOptions = ['Ativa', 'Pendente', 'Concluída', 'Reprovada', 'Desclassificada'];
const fallbackQuotaOptions = ['Ampla Concorrência', 'PCD/Neurodivergente', 'Negro/Pardo', 'Mulher', '45+'];
const fallbackGenderOptions = ['Feminino', 'Masculino', 'Outro', 'Não informado'];
const fixedEducationTypeOptions = [
  'Engenharia de Computação, Ciência da Computação ou outros cursos relacionados a TIC',
  'Outros cursos de ciências exatas ou tecnológicos',
  'Técnico na área de exatas concluído',
  'Engenharia (exceto de Software/Computação)'
];
const fallbackEducationStatusOptions = ['Cursando', 'Concluído', 'Trancado', 'Não informado'];
const fallbackStateOptions = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
  'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
  'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
];
const advancedFilters = ref({
  programa: '',
  etapa: '',
  status: '',
  cota: '',
  genero: '',
  estado: '',
  cidade: '',
  instituicao: ''
});

const showUploadModal = ref(false);
const showCreateModal = ref(false);
const referenceData = ref(null);
const referenceLoading = ref(false);
const referenceError = ref(null);
const classStageOptions = ref([]);
const stageLoading = ref(false);
const stageError = ref(null);
const uploadForm = ref({
  programaId: '',
  turmaId: '',
  etapaId: '',
  statusInicial: ''
});
const uploadStageOptions = ref([]);
const uploadStageLoading = ref(false);
const uploadStageError = ref(null);
const uploadTab = ref('novos');
const uploadPreview = ref({
  rows: [],
  newRows: [],
  existingRows: [],
  alertRows: []
});
const selectedFile = ref(null);
const uploading = ref(false);
const uploadError = ref(null);
const uploadSuccess = ref(null);
const uploadResult = ref(null);
const fileInput = ref(null);
const isDragging = ref(false);
const currentPage = ref(1);
const itemsPerPage = 12;

const createForm = ref({
  programaId: '',
  turmaId: '',
  etapaId: '',
  statusInicial: '',
  nome: '',
  dataNascimento: '',
  genero: '',
  cota: '',
  cpf: '',
  email: '',
  telefone: '',
  linkedin: '',
  endereco: '',
  estado: '',
  cidade: '',
  cep: '',
  complementoEndereco: '',
  tipoFormacao: '',
  instituicao: '',
  curso: '',
  statusFormacao: '',
  dataConclusao: ''
});
const creating = ref(false);
const createError = ref(null);

const programOptions = computed(() => referenceData.value?.programas || []);
const classOptions = computed(() => referenceData.value?.turmas || []);
const stageOptions = computed(() => classStageOptions.value);
const statusOptions = computed(() => {
  const options = referenceData.value?.statusOptions || [];
  return options.length ? options : fallbackStatusOptions;
});
const quotaOptions = computed(() => {
  const options = referenceData.value?.cotaOptions || [];
  return options.length ? options : fallbackQuotaOptions;
});
const genderOptions = computed(() => {
  const options = referenceData.value?.generoOptions || [];
  return options.length ? options : fallbackGenderOptions;
});
const stateOptions = computed(() => {
  const options = referenceData.value?.estadoOptions || [];
  return options.length ? options : fallbackStateOptions;
});
const educationTypeOptions = computed(() => fixedEducationTypeOptions);
const educationStatusOptions = computed(() => {
  const options = referenceData.value?.statusFormacaoOptions || [];
  return options.length ? options : fallbackEducationStatusOptions;
});

const normalize = (value) => (value ?? '').toString().toLowerCase();
const hasValue = (value) => !!(value ?? '').toString().trim();

const stageLabel = (name) => {
  const normalized = normalize(name);
  if (normalized.includes('imersao')) return 'Imersao';
  if (normalized.includes('nivelamento')) return 'Nivelamento';
  if (normalized.includes('selecao') || normalized.includes('inscricao')) return 'Inscricao';
  return name || '-';
};

const stagePriority = (label) => {
  const normalized = normalize(label);
  if (normalized.includes('imersao')) return 0;
  if (normalized.includes('nivelamento')) return 1;
  if (normalized.includes('inscricao') || normalized.includes('selecao')) return 2;
  return 3;
};

const formatNumber = (value) => new Intl.NumberFormat('pt-BR').format(Number(value || 0));

const formatDateTime = (value) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(date);
};

const formatDate = (value) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' }).format(date);
};

const formatAge = (value) => {
  if (!value) return '-';
  const birthDate = new Date(value);
  if (Number.isNaN(birthDate.getTime())) return '-';

  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age -= 1;
  }

  return age >= 0 ? String(age) : '-';
};

const formatGender = (value) => {
  if (!hasValue(value)) return '-';
  const normalized = normalize(value);
  if (['m', 'male', 'masculino'].includes(normalized)) return 'Masculino';
  if (['f', 'female', 'feminino'].includes(normalized)) return 'Feminino';
  if (['other', 'outro', 'outros'].includes(normalized)) return 'Outro';
  return value.toString().trim().charAt(0).toUpperCase() + value.toString().trim().slice(1).toLowerCase();
};

const formatCityUf = (person) => {
  const parts = [person.city, person.state].filter(hasValue);
  return parts.length ? parts.join(' / ') : '-';
};

const formatInstitution = (person) => personMeta(person).institutions[0] || '-';

const formatCourse = (person) => person.course || person.courseName || '-';

const formatQuota = (person) => person.cota || person.quota || '-';

const formatUpdatedAt = (person) => formatDateTime(personMeta(person).updatedAt || person.createdAt);

const formatCPF = (cpf) => {
  if (!cpf) return '-';
  const digits = cpf.replace(/\D/g, '');
  if (digits.length !== 11) return cpf;
  return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
};

const createCpfDigits = computed(() => (createForm.value.cpf || '').replace(/\D/g, ''));
const createDisabled = computed(() => (
  creating.value
  || stageLoading.value
  || !createForm.value.nome
  || !createForm.value.email
  || createCpfDigits.value.length !== 11
  || !createForm.value.dataNascimento
  || !createForm.value.turmaId
  || !createForm.value.etapaId
));

const uploadTotalCount = computed(() => uploadResult.value?.totalProcessed || 0);
const uploadNewCount = computed(() => uploadPreview.value.newRows.length || uploadResult.value?.successfullyInserted || 0);
const uploadExistingCount = computed(() => uploadPreview.value.existingRows.length || uploadResult.value?.alreadyExists || 0);
const uploadAlertCount = computed(() => uploadPreview.value.alertRows.length || 0);
const uploadTableRows = computed(() => (uploadTab.value === 'existentes'
  ? uploadPreview.value.existingRows
  : uploadPreview.value.newRows));
const uploadMissingFields = computed(() => {
  const missing = [];
  if (!uploadForm.value.programaId) missing.push('Programa de destino');
  if (!uploadForm.value.turmaId) missing.push('Turma de destino');
  if (!uploadForm.value.etapaId) missing.push('Etapa inicial');
  if (!uploadForm.value.statusInicial) missing.push('Status inicial');
  if (!selectedFile.value) missing.push('Arquivo');
  return missing;
});
const uploadDisabled = computed(() => uploading.value || uploadMissingFields.value.length > 0);

const getInitials = (name) => {
  if (!name) return 'P';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
};

const isRecent30d = (person) => {
  if (!person.createdAt) return false;
  const created = new Date(person.createdAt).getTime();
  if (Number.isNaN(created)) return false;
  return (Date.now() - created) <= 30 * 24 * 60 * 60 * 1000;
};

const isRecent7d = (person) => {
  if (!person.createdAt) return false;
  const created = new Date(person.createdAt).getTime();
  if (Number.isNaN(created)) return false;
  return (Date.now() - created) <= 7 * 24 * 60 * 60 * 1000;
};

const DAYS_30 = 30 * 24 * 60 * 60 * 1000;

const getTimestamp = (value) => {
  if (!value) return null;
  const parsed = new Date(value).getTime();
  return Number.isNaN(parsed) ? null : parsed;
};

const inRange = (value, start, end) => {
  const ts = getTimestamp(value);
  return ts !== null && ts >= start && ts < end;
};

const activityDate = (person) => personMeta(person).updatedAt || person.updatedAt || person.createdAt;

const getRangeCounts = (predicate, dateGetter) => {
  const now = Date.now();
  const currentStart = now - DAYS_30;
  const previousStart = now - DAYS_30 * 2;
  const current = people.value.filter((person) => predicate(person) && inRange(dateGetter(person), currentStart, now)).length;
  const previous = people.value.filter((person) => predicate(person) && inRange(dateGetter(person), previousStart, currentStart)).length;
  return { current, previous };
};

const percentChange = (current, previous) => {
  if (previous === 0) return current > 0 ? 100 : 0;
  return Math.round(((current - previous) / previous) * 100);
};

const formatTrend = (value) => {
  const sign = value < 0 ? '-' : '+';
  return `${sign}${Math.abs(value)}%`;
};

const isCompleteProfile = (person) => [person.name, person.email, person.cpf, person.birthDate, person.gender, person.educationLevel, person.address, person.city].every(hasValue);

const activeStatusSet = new Set(['ativo', 'em andamento', 'matriculado', 'active', 'atva']);
const finishedStatusSet = new Set(['concluido', 'concluida', 'finalizado', 'finalizada', 'done']);

const personMetaMap = computed(() => {
  const map = new Map();

  const enrollmentByPerson = new Map();
  for (const enrollment of enrollments.value) {
    if (!enrollment?.people?.id) continue;
    const personId = enrollment.people.id;
    const list = enrollmentByPerson.get(personId) || [];
    list.push(enrollment);
    enrollmentByPerson.set(personId, list);
  }

  const candidatesByPerson = new Map();
  for (const candidate of stageCandidates.value) {
    if (!candidate?.peopleId) continue;
    const list = candidatesByPerson.get(candidate.peopleId) || [];
    list.push(candidate);
    candidatesByPerson.set(candidate.peopleId, list);
  }

  for (const person of people.value) {
    const personEnrollments = (enrollmentByPerson.get(person.id) || []).slice().sort((a, b) => {
      const aDate = new Date(a.enrollmentDate || a.createdAt || 0).getTime();
      const bDate = new Date(b.enrollmentDate || b.createdAt || 0).getTime();
      return bDate - aDate;
    });

    const personCandidates = (candidatesByPerson.get(person.id) || []).slice().sort((a, b) => {
      const aDate = new Date(a.updatedAt || a.createdAt || 0).getTime();
      const bDate = new Date(b.updatedAt || b.createdAt || 0).getTime();
      return bDate - aDate;
    });

    const programs = [];
    const institutions = [];
    let latestUpdatedAt = person.createdAt || null;

    for (const enrollment of personEnrollments) {
      const programName = enrollment.classModel?.program?.name || enrollment.classModel?.program?.code;
      const institutionName = enrollment.classModel?.location?.acronym || enrollment.classModel?.location?.name;
      if (programName && !programs.includes(programName)) programs.push(programName);
      if (institutionName && !institutions.includes(institutionName)) institutions.push(institutionName);

      const candidateTime = enrollment.completionDate || enrollment.enrollmentDate;
      if (candidateTime) {
        const parsed = new Date(candidateTime).getTime();
        if (!Number.isNaN(parsed)) {
          const current = latestUpdatedAt ? new Date(latestUpdatedAt).getTime() : 0;
          if (parsed > current) latestUpdatedAt = candidateTime;
        }
      }
    }

    const levelingCandidate = personCandidates.find((candidate) => normalize(candidate.stageName) === 'nivelamento');
    const immersionCandidate = personCandidates.find((candidate) => normalize(candidate.stageName) === 'imersao' || normalize(candidate.stageName) === 'imersão');
    const latestCandidate = personCandidates[0];
    if (latestCandidate?.updatedAt) latestUpdatedAt = latestCandidate.updatedAt;

    let status = 'Pendente';
    if (immersionCandidate) status = 'Imersão';
    else if (levelingCandidate) status = 'Nivelamento';
    else if (personEnrollments.some((enrollment) => activeStatusSet.has(normalize(enrollment.status)))) status = 'Ativa';
    else if (personEnrollments.some((enrollment) => !finishedStatusSet.has(normalize(enrollment.status)) && enrollment.status)) status = 'Ativa';
    else if (personEnrollments.length > 0) status = 'Ativa';

    map.set(person.id, {
      programs,
      institutions,
      stage: immersionCandidate ? 'Imersão' : levelingCandidate ? 'Nivelamento' : status === 'Ativa' ? 'Programa ativo' : '-',
      status,
      updatedAt: latestUpdatedAt,
      active: status === 'Ativa' || status === 'Nivelamento' || status === 'Imersão',
      inProgram: personEnrollments.length > 0,
      leveling: Boolean(levelingCandidate),
      immersion: Boolean(immersionCandidate)
    });
  }

  return map;
});

const personMeta = (person) => personMetaMap.value.get(person.id) || {
  programs: [],
  institutions: [],
  stage: '-',
  status: isCompleteProfile(person) ? 'Completa' : 'Pendente',
  updatedAt: person.createdAt,
  active: false,
  inProgram: false,
  leveling: false,
  immersion: false
};

const totalPeople = computed(() => people.value.length);
const activePeopleCount = computed(() => people.value.filter((person) => personMeta(person).active || personMeta(person).inProgram).length);
const activeProgramsCount = computed(() => people.value.filter((person) => personMeta(person).inProgram).length);
const levelingCount = computed(() => people.value.filter((person) => personMeta(person).leveling).length);
const immersionCount = computed(() => people.value.filter((person) => personMeta(person).immersion).length);
const newRegistrationsCount = computed(() => people.value.filter(isRecent30d).length);
const activePeopleTrend = computed(() => {
  const { current, previous } = getRangeCounts(
    (person) => personMeta(person).active || personMeta(person).inProgram,
    (person) => activityDate(person)
  );
  return percentChange(current, previous);
});
const activeProgramsTrend = computed(() => {
  const { current, previous } = getRangeCounts(
    (person) => personMeta(person).inProgram,
    (person) => activityDate(person)
  );
  return percentChange(current, previous);
});
const levelingTrend = computed(() => {
  const { current, previous } = getRangeCounts(
    (person) => personMeta(person).leveling,
    (person) => activityDate(person)
  );
  return percentChange(current, previous);
});
const immersionTrend = computed(() => {
  const { current, previous } = getRangeCounts(
    (person) => personMeta(person).immersion,
    (person) => activityDate(person)
  );
  return percentChange(current, previous);
});
const newRegistrationsTrend = computed(() => {
  const { current, previous } = getRangeCounts(
    (person) => Boolean(person.createdAt),
    (person) => person.createdAt
  );
  return percentChange(current, previous);
});

const tabs = computed(() => ([
  { value: 'active', label: 'Pessoas Ativas', count: activePeopleCount.value },
  { value: 'programs', label: 'Programas em Andamento', count: activeProgramsCount.value },
  { value: 'all', label: 'Todas as Pessoas', count: totalPeople.value }
]));

const advancedFiltersCount = computed(() => Object.values(advancedFilters.value).filter(Boolean).length);

const filteredPeople = computed(() => {
  let list = [...people.value];

  if (searchTerm.value) {
    const term = normalize(searchTerm.value);
    list = list.filter((person) => [
      person.name,
      person.cpf,
      person.email,
      person.address,
      person.city,
      person.educationLevel,
      person.gender,
      personMeta(person).institutions.join(' '),
      personMeta(person).programs.join(' '),
      personMeta(person).stage,
      personMeta(person).status
    ].some((field) => normalize(field).includes(term)));
  }

  if (activeTab.value === 'active') {
    list = list.filter((person) => personMeta(person).active || personMeta(person).inProgram);
  } else if (activeTab.value === 'programs') {
    list = list.filter((person) => personMeta(person).inProgram);
  } else if (activeTab.value === 'history') {
    list = list.filter((person) => personMeta(person).updatedAt || person.createdAt);
  }

  if (advancedFilters.value.hasCpf) list = list.filter((person) => hasValue(person.cpf));
  if (advancedFilters.value.hasEmail) list = list.filter((person) => hasValue(person.email));
  if (advancedFilters.value.completeOnly) list = list.filter(isCompleteProfile);
  if (advancedFilters.value.recentOnly) list = list.filter(isRecent30d);

  return list;
});

const totalPages = computed(() => Math.ceil(filteredPeople.value.length / itemsPerPage));

const paginatedPeople = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return filteredPeople.value.slice(start, start + itemsPerPage);
});

const pageStart = computed(() => filteredPeople.value.length ? ((currentPage.value - 1) * itemsPerPage) + 1 : 0);
const pageEnd = computed(() => Math.min(currentPage.value * itemsPerPage, filteredPeople.value.length));

const filteredSummary = computed(() => {
  const parts = [];
  if (searchTerm.value) parts.push(`busca: ${searchTerm.value}`);
  if (activeTab.value !== 'all') parts.push(`aba: ${tabs.value.find((tab) => tab.value === activeTab.value)?.label || activeTab.value}`);
  if (advancedFilters.value.hasCpf) parts.push('com CPF');
  if (advancedFilters.value.hasEmail) parts.push('com e-mail');
  if (advancedFilters.value.completeOnly) parts.push('completos');
  if (advancedFilters.value.recentOnly) parts.push('recentes');
  return parts.length ? parts.join(' • ') : 'Sem filtros ativos';
});

const visiblePages = computed(() => {
  const total = totalPages.value;
  if (total <= 1) return [1];

  const current = currentPage.value;
  const pages = [];

  if (total <= 5) {
    for (let i = 1; i <= total; i += 1) pages.push(i);
    return pages;
  }

  if (current <= 3) return [1, 2, 3, 4, '...', total];
  if (current >= total - 2) return [1, '...', total - 3, total - 2, total - 1, total];
  return [1, '...', current - 1, current, current + 1, '...', total];
});

const statusClass = (status) => {
  const normalized = normalize(status);
  if (normalized === 'ativa') return 'status-success';
  if (normalized === 'nivelamento') return 'status-warning';
  if (normalized === 'imersão' || normalized === 'imersao') return 'status-info';
  if (normalized === 'pendente') return 'status-pending';
  return 'status-neutral';
};

const goToPage = (page) => {
  if (typeof page !== 'number') return;
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
};

const prevPage = () => goToPage(currentPage.value - 1);
const nextPage = () => goToPage(currentPage.value + 1);

const resetFilters = () => {
  searchTerm.value = '';
  activeTab.value = 'active';
  advancedFilters.value = { hasCpf: false, hasEmail: false, completeOnly: false, recentOnly: false };
  closeColumnsMenu();
};

const runSearch = () => {
  currentPage.value = 1;
  showAdvancedFilters.value = false;
  closeColumnsMenu();
};

const loadData = async () => {
  loading.value = true;
  error.value = null;
  try {
    const [peopleData, enrollmentsData, candidatesData] = await Promise.all([
      peopleService.getAll(),
      enrollmentService.getAll(),
      stageService.getAllCandidates()
    ]);
    people.value = peopleData;
    enrollments.value = enrollmentsData;
    stageCandidates.value = candidatesData;
  } catch (err) {
    error.value = `Erro ao carregar pessoas: ${err.response?.data?.message || err.message}`;
  } finally {
    loading.value = false;
  }
};

const loadReferenceData = async () => {
  referenceLoading.value = true;
  referenceError.value = null;
  try {
    referenceData.value = await peopleService.getReferenceData();
  } catch (err) {
    referenceError.value = `Erro ao carregar opções: ${err.response?.data?.message || err.message}`;
  } finally {
    referenceLoading.value = false;
  }
};

const viewDetails = (person) => {
  router.push(`/people/${person.id}`);
};

const editPerson = (person) => {
  router.push(`/people/${person.id}?edit=true`);
};

const viewHistory = (person) => {
  router.push(`/people/${person.id}`);
};

const openMoreActions = () => {
  // Placeholder visual button matching the Figma row actions.
};

const downloadTemplate = () => {
  showTemplateModal.value = true;
};

const formatCPFInput = (event) => {
  let value = event.target.value.replace(/\D/g, '');
  if (value.length > 11) value = value.slice(0, 11);
  value = value.replace(/(\d{3})(\d)/, '$1.$2');
  value = value.replace(/(\d{3})(\d)/, '$1.$2');
  value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  createForm.value.cpf = value;
};

const resetUploadResults = () => {
  uploadError.value = null;
  uploadSuccess.value = null;
  uploadResult.value = null;
  uploadPreview.value = { rows: [], newRows: [], existingRows: [], alertRows: [] };
  uploadTab.value = 'novos';
};

const normalizeHeader = (value) => normalize(value).replace(/\s+/g, ' ').trim();

const getColumnIndex = (headers, names, fallbackIndex) => {
  for (const name of names) {
    const index = headers.indexOf(normalizeHeader(name));
    if (index >= 0) return index;
  }
  return fallbackIndex;
};

const parseUploadFile = async (file) => {
  try {
    const buffer = await file.arrayBuffer();
    const workbook = XLSX.read(buffer, { type: 'array' });
    const sheetName = workbook.SheetNames[0];
    if (!sheetName) return [];

    const sheet = workbook.Sheets[sheetName];
    const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });
    if (!rows.length) return [];

    const headers = rows[0].map((value) => normalizeHeader(value));
    const idxName = getColumnIndex(headers, ['Nome', 'Name'], 0);
    const idxBirthDate = getColumnIndex(headers, ['Data de nascimento', 'Nascimento', 'Birth date'], 1);
    const idxState = getColumnIndex(headers, ['Estado', 'UF'], 2);
    const idxInstitution = getColumnIndex(headers, ['Instituição', 'Instituicao'], 3);
    const idxCpf = getColumnIndex(headers, ['CPF', 'Cpf'], 4);
    const idxGender = getColumnIndex(headers, ['Gênero', 'Genero'], 5);
    const idxCity = getColumnIndex(headers, ['Cidade'], 6);
    const idxCourse = getColumnIndex(headers, ['Curso'], 7);
    const idxEmail = getColumnIndex(headers, ['E-mail', 'Email'], 8);
    const idxCota = getColumnIndex(headers, ['Cota'], 9);
    const idxEducationType = getColumnIndex(headers, ['Tipo de formação', 'Tipo de formacao', 'Formação'], 10);
    const idxEducationStatus = getColumnIndex(headers, ['Status da formação', 'Status da formacao', 'Status'], 11);

    return rows.slice(1).map((row, index) => {
      const record = {
        rowNumber: index + 2,
        name: String(row[idxName] ?? '').trim(),
        birthDate: String(row[idxBirthDate] ?? '').trim(),
        state: String(row[idxState] ?? '').trim(),
        institution: String(row[idxInstitution] ?? '').trim(),
        cpf: String(row[idxCpf] ?? '').trim(),
        gender: String(row[idxGender] ?? '').trim(),
        city: String(row[idxCity] ?? '').trim(),
        course: String(row[idxCourse] ?? '').trim(),
        email: String(row[idxEmail] ?? '').trim(),
        cota: String(row[idxCota] ?? '').trim(),
        educationType: String(row[idxEducationType] ?? '').trim(),
        educationStatus: String(row[idxEducationStatus] ?? '').trim()
      };

      const hasAnyValue = [
        record.name,
        record.birthDate,
        record.state,
        record.institution,
        record.cpf,
        record.gender,
        record.city,
        record.course,
        record.email,
        record.cota,
        record.educationType,
        record.educationStatus
      ].some(hasValue);
      return hasAnyValue ? record : null;
    }).filter(Boolean);
  } catch (err) {
    return [];
  }
};

const buildUploadPreview = (rows, result) => {
  const duplicates = new Set((result?.duplicatePersons || []).map((name) => normalize(name)));
  const newRows = [];
  const existingRows = [];
  const alertRows = [];
  const expectedFields = [
    { key: 'name', label: 'Nome' },
    { key: 'birthDate', label: 'Data de nascimento' },
    { key: 'state', label: 'Estado' },
    { key: 'institution', label: 'Instituição' },
    { key: 'cpf', label: 'CPF' },
    { key: 'gender', label: 'Gênero' },
    { key: 'city', label: 'Cidade' },
    { key: 'course', label: 'Curso' },
    { key: 'email', label: 'E-mail' },
    { key: 'cota', label: 'Cota' },
    { key: 'educationType', label: 'Tipo de formação' },
    { key: 'educationStatus', label: 'Status da formação' }
  ];

  rows.forEach((row) => {
    const issues = [];
    expectedFields.forEach((field) => {
      if (!hasValue(row[field.key])) {
        issues.push(`${field.label} ausente`);
      }
    });

    const isDuplicate = duplicates.has(normalize(row.name));
    if (isDuplicate) issues.unshift('Pessoa já cadastrada');

    if (issues.length) {
      const entry = { ...row, issue: issues.join(', ') };
      if (isDuplicate) {
        existingRows.push(entry);
      } else {
        alertRows.push(entry);
      }
      return;
    }

    newRows.push(row);
  });

  return { rows, newRows, existingRows, alertRows };
};

const downloadInconsistenciesReport = () => {
  const existingRows = uploadPreview.value.existingRows;
  const alertRows = uploadPreview.value.alertRows;
  if (!existingRows.length && !alertRows.length) return;

  const existingSheetData = [
    ['Nome', 'E-mail', 'CPF', 'Motivo'],
    ...existingRows.map((row) => [row.name, row.email, formatCPF(row.cpf), row.issue || 'Pessoa já cadastrada'])
  ];
  const alertSheetData = [
    ['Linha', 'Nome', 'E-mail', 'CPF', 'Motivo'],
    ...alertRows.map((row) => [row.rowNumber, row.name, row.email, formatCPF(row.cpf), row.issue || 'Campos obrigatórios ausentes'])
  ];

  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, XLSX.utils.aoa_to_sheet(existingSheetData), 'Ja existentes');
  XLSX.utils.book_append_sheet(workbook, XLSX.utils.aoa_to_sheet(alertSheetData), 'Alertas');
  XLSX.writeFile(workbook, 'relatorio-inconsistencias.xlsx');
};

const toNullableNumber = (value) => {
  if (value === '' || value === null || value === undefined) return null;
  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
};

const handleCreate = async () => {
  const cpfDigits = createCpfDigits.value;

  if (!createForm.value.nome || !createForm.value.email || cpfDigits.length !== 11 || !createForm.value.dataNascimento || !createForm.value.turmaId || !createForm.value.etapaId) {
    createError.value = 'Preencha os campos obrigatórios marcados com *.';
    return;
  }

  creating.value = true;
  createError.value = null;

  try {
    await peopleService.createLink({
      ...createForm.value,
      cpf: cpfDigits,
      programaId: toNullableNumber(createForm.value.programaId),
      turmaId: toNullableNumber(createForm.value.turmaId),
      etapaId: toNullableNumber(createForm.value.etapaId),
      dataNascimento: createForm.value.dataNascimento || null,
      dataConclusao: createForm.value.dataConclusao || null
    });
    closeCreateModal();
    await loadData();
  } catch (err) {
    const details = err.response?.data?.details;
    if (Array.isArray(details) && details.length) {
      createError.value = details.join(' ');
    } else {
      createError.value = err.response?.data?.message || err.message || 'Erro ao cadastrar pessoa';
    }
  } finally {
    creating.value = false;
  }
};

const closeCreateModal = () => {
  showCreateModal.value = false;
  createError.value = null;
  stageError.value = null;
  classStageOptions.value = [];
  createForm.value = {
    programaId: '',
    turmaId: '',
    etapaId: '',
    statusInicial: '',
    nome: '',
    dataNascimento: '',
    genero: '',
    cota: '',
    cpf: '',
    email: '',
    telefone: '',
    linkedin: '',
    endereco: '',
    estado: '',
    cidade: '',
    cep: '',
    complementoEndereco: '',
    tipoFormacao: '',
    instituicao: '',
    curso: '',
    statusFormacao: '',
    dataConclusao: ''
  };
};

const handleFileSelect = (event) => {
  selectedFile.value = event.target.files[0];
  resetUploadResults();
};

const handleDragOver = () => { isDragging.value = true; };
const handleDragLeave = () => { isDragging.value = false; };

const handleDrop = (event) => {
  isDragging.value = false;
  const file = event.dataTransfer.files[0];
  if (file && (file.name.endsWith('.xlsx') || file.name.endsWith('.xls') || file.name.endsWith('.csv'))) {
    selectedFile.value = file;
    resetUploadResults();
  } else if (file) {
    uploadError.value = 'Formato inválido. Use arquivos .xlsx, .xls ou .csv.';
  }
};

const uploadFile = async () => {
  if (uploadMissingFields.value.length > 0) {
    uploadError.value = `Preencha os campos obrigatórios: ${uploadMissingFields.value.join(', ')}.`;
    return;
  }

  uploading.value = true;
  uploadError.value = null;
  uploadSuccess.value = null;
  uploadResult.value = null;

  try {
    const parsedRows = await parseUploadFile(selectedFile.value);
    const response = await peopleService.importExcel(selectedFile.value);
    uploadResult.value = response;
    uploadPreview.value = buildUploadPreview(parsedRows, response);
    uploadTab.value = 'novos';
    uploadSuccess.value = `${response.successfullyInserted || 0} pessoa(s) importada(s) com sucesso!`;
    await loadData();
  } catch (err) {
    uploadError.value = `Erro ao importar arquivo: ${err.response?.data?.message || err.message}`;
  } finally {
    uploading.value = false;
  }
};

const closeUploadModal = () => {
  showUploadModal.value = false;
  selectedFile.value = null;
  resetUploadResults();
  uploadForm.value = {
    programaId: '',
    turmaId: '',
    etapaId: '',
    statusInicial: ''
  };
  uploadStageOptions.value = [];
  uploadStageError.value = null;
  uploadStageLoading.value = false;
  uploading.value = false;
  isDragging.value = false;
  if (fileInput.value) fileInput.value.value = '';
};

const closeColumnsMenu = () => {
  showColumnsMenu.value = false;
};

const handleDocumentClick = (event) => {
  const control = columnsControlRef.value;
  if (!control) return;
  if (!control.contains(event.target)) {
    closeColumnsMenu();
  }
};

watch([searchTerm, activeTab, advancedFilters], () => {
  currentPage.value = 1;
}, { deep: true });

watch(filteredPeople, () => {
  if (currentPage.value > totalPages.value && totalPages.value > 0) {
    currentPage.value = totalPages.value;
  }
});

watch(() => createForm.value.turmaId, async (turmaId) => {
  if (!turmaId) {
    classStageOptions.value = [];
    createForm.value.etapaId = '';
    stageError.value = null;
    return;
  }

  stageLoading.value = true;
  stageError.value = null;
  try {
    const stages = await stageService.getByClassId(turmaId);
    classStageOptions.value = stages
      .map((stage) => ({ id: stage.id, label: stageLabel(stage.name) }))
      .sort((a, b) => {
        const priorityDiff = stagePriority(a.label) - stagePriority(b.label);
        if (priorityDiff !== 0) return priorityDiff;
        return a.label.localeCompare(b.label, 'pt-BR');
      });

    if (!classStageOptions.value.some((option) => option.id === createForm.value.etapaId)) {
      createForm.value.etapaId = '';
    }
  } catch (err) {
    classStageOptions.value = [];
    createForm.value.etapaId = '';
    stageError.value = `Erro ao carregar etapas: ${err.response?.data?.message || err.message}`;
  } finally {
    stageLoading.value = false;
  }
});

watch(() => uploadForm.value.turmaId, async (turmaId) => {
  if (!turmaId) {
    uploadStageOptions.value = [];
    uploadForm.value.etapaId = '';
    uploadStageError.value = null;
    return;
  }

  uploadStageLoading.value = true;
  uploadStageError.value = null;
  try {
    const stages = await stageService.getByClassId(turmaId);
    uploadStageOptions.value = stages
      .map((stage) => ({ id: stage.id, label: stageLabel(stage.name) }))
      .sort((a, b) => {
        const priorityDiff = stagePriority(a.label) - stagePriority(b.label);
        if (priorityDiff !== 0) return priorityDiff;
        return a.label.localeCompare(b.label, 'pt-BR');
      });

    if (!uploadStageOptions.value.some((option) => option.id === uploadForm.value.etapaId)) {
      uploadForm.value.etapaId = '';
    }
  } catch (err) {
    uploadStageOptions.value = [];
    uploadForm.value.etapaId = '';
    uploadStageError.value = `Erro ao carregar etapas: ${err.response?.data?.message || err.message}`;
  } finally {
    uploadStageLoading.value = false;
  }
});

onMounted(() => {
  loadData();
  loadReferenceData();
  document.addEventListener('click', handleDocumentClick);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick);
});
</script>

<style scoped>
.people-page {
  min-height: 100%;
  background: #eef3f8;
  padding: 14px 16px 20px;
}

.people-shell {
  max-width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header-card,
.table-card {
  background: #fff;
  border: 1px solid #dfe7f1;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(13, 27, 42, 0.05);
}

.page-header-card {
  padding: 18px 18px 16px;
}

.page-header-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0 0 2px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.18em;
  color: #6a7a90;
  text-transform: uppercase;
}

.page-header-top h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.1;
  color: #13233f;
  font-weight: 800;
}

.subtitle {
  margin: 6px 0 0;
  color: #6a7a90;
  font-size: 14px;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.ghost-btn,
.primary-btn,
.secondary-btn,
.filters-button,
.tab-item,
.page-btn,
.icon-btn {
  font: inherit;
}

.ghost-btn,
.primary-btn {
  height: 40px;
  border-radius: 12px;
  border: 1px solid #d1dbe8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 14px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
  font-weight: 700;
}

.ghost-btn {
  background: #fff;
  color: #13233f;
}

.primary-btn {
  background: #14b8a6;
  color: #fff;
  border-color: #14b8a6;
  box-shadow: 0 8px 16px rgba(20, 184, 166, 0.20);
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

.ghost-btn:hover,
.primary-btn:hover,
.filters-button:hover,
.page-btn:hover,
.icon-btn:hover {
  transform: translateY(-1px);
}

.stats-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.stat-card {
  border: 1px solid #dce5ef;
  border-radius: 14px;
  background: #fff;
  padding: 14px 14px 12px;
  min-height: 92px;
  position: relative;
  overflow: hidden;
}

.stat-label {
  color: #6a7a90;
  font-size: 12px;
  font-weight: 600;
}

.stat-row {
  margin-top: 8px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.stat-row-single {
  justify-content: flex-start;
}

.stat-value {
  color: #13233f;
  font-size: 26px;
  line-height: 1;
  font-weight: 800;
}

.stat-trend {
  color: #14b8a6;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.stat-note {
  margin-top: 6px;
  font-size: 11px;
  color: #8a98ab;
}

.table-card {
  padding: 0;
  overflow: visible;
}

.tabs-section {
  border-bottom: 1px solid #e2eaf2;
}

.search-section {
  background: #f8fafc;
  border-bottom: 1px solid #e2eaf2;
  padding: 16px 14px;
}

.table-section {
  padding: 14px 18px;
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
  background: transparent;
  border: 0;
  padding: 12px 2px 14px;
  border-bottom: 2px solid transparent;
  color: #5f728d;
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tab-item.active {
  color: #0f766e;
  border-bottom-color: #14b8a6;
}

.tab-count {
  background: #eef2f7;
  color: #8a98ab;
  border-radius: 999px;
  padding: 2px 7px;
  font-size: 11px;
  font-weight: 700;
}

.filters-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 0;
  max-width: 100%;
}

.filters-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #97a4b6;
  pointer-events: none;
  width: 18px;
  height: 18px;
}

.search-input {
  width: 100%;
  box-sizing: border-box;
  height: 48px;
  border-radius: 12px;
  border: 1px solid #d9e2ec;
  background: #fff;
  padding: 0 14px 0 44px;
  color: #13233f;
  outline: none;
  font-size: 14px;
}

.search-input:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.10);
}

.filters-button {
  height: 48px;
  border-radius: 12px;
  padding: 0 14px;
  border: 1px solid #d8e1eb;
  background: #fff;
  color: #13233f;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.filters-button:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.columns-control {
  position: relative;
  display: inline-flex;
  flex-direction: column;
  padding-bottom: 10px;
}

.search-button {
  height: 48px;
  border-radius: 12px;
  padding: 0 18px;
  border: 1px solid #14b8a6;
  background: #14b8a6;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.search-button:hover {
  background: #0d9488;
  border-color: #0d9488;
  box-shadow: 0 10px 18px rgba(13, 148, 136, 0.20);
}

.columns-button {
  height: 34px;
  border-radius: 10px;
  border: 1px solid #d8e1eb;
  background: #fff;
  color: #13233f;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  cursor: pointer;
}

.columns-button.active,
.columns-button:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.columns-icon {
  flex-shrink: 0;
}

.columns-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  z-index: 1000;
  width: 260px;
  max-height: 400px;
  padding: 10px 0;
  border-radius: 11px;
  border: 1px solid #d8e1eb;
  background: #fff;
  box-shadow: 0 14px 26px rgba(15, 23, 42, 0.12);
  overflow-y: auto;
}

.columns-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 7px 14px;
  color: #13233f;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.columns-option:hover {
  background: #f8fafc;
}

.columns-option input {
  appearance: none;
  -webkit-appearance: none;
  width: 12px;
  height: 12px;
  margin: 0;
  border: 1px solid #4b5563;
  border-radius: 2px;
  background: #fff;
  flex-shrink: 0;
  transition: background 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
}

.columns-option input:checked {
  background: #2f3440;
  border-color: #2f3440;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.columns-option input:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.16);
  border-color: #14b8a6;
}

.columns-option span {
  line-height: 1.2;
}

.filters-badge {
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  background: #d6f3ec;
  color: #14a6a6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
}

.advanced-filters {
  padding: 0 14px 14px;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group label {
  font-size: 13px;
  font-weight: 600;
  color: #405069;
}

.filter-group select,
.filter-group input {
  height: 38px;
  border-radius: 10px;
  border: 1px solid #d9e2ec;
  background: #fff;
  padding: 0 12px;
  color: #13233f;
  font-size: 13px;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.filter-group select:focus,
.filter-group input:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.10);
}


.filter-check {
  border: 1px solid #e0e8f1;
  border-radius: 12px;
  background: #fbfdff;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #405069;
  font-size: 13px;
}

.state-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 24px 18px;
  color: #41546e;
}

.state-row-error { color: #b42318; }

.state-row-empty {
  color: #41546e;
}

.table-scroll {
  overflow-x: auto;
  overflow-y: visible;
}

.people-table {
  width: 100%;
  min-width: 100%;
  table-layout: auto;
  border-collapse: collapse;
}

.people-table thead th {
  text-align: left;
  padding: 16px 18px;
  color: #6a7a90;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  background: #f7f9fc;
  border-top: 1px solid #e2eaf2;
  border-bottom: 1px solid #e2eaf2;
}

.people-table tbody td {
  padding: 14px 18px;
  border-bottom: 1px solid #eef3f8;
  color: #13233f;
  vertical-align: middle;
}

.people-table tbody tr:hover {
  background: #fbfdff;
}

.person-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.person-avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, #13233f 0%, #1f4b89 100%);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 800;
  flex-shrink: 0;
}

.person-name {
  font-size: 14px;
  font-weight: 800;
}

.cell-text,
.muted {
  color: #6a7a90;
  font-size: 13px;
}

.mono {
  font-variant-numeric: tabular-nums;
  color: #41546e;
  font-size: 13px;
}

.chips {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.chip,
.stage-pill,
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 4px 9px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.chip {
  background: #edf3ff;
  color: #3765e8;
}

.chip-muted {
  background: #eef2f7;
  color: #607084;
}

.stage-pill {
  background: #eef2f7;
  color: #44556c;
}

.status-pill.status-success { background: #dcfce7; color: #15803d; }
.status-pill.status-warning { background: #fef3c7; color: #b45309; }
.status-pill.status-info { background: #dbeafe; color: #2563eb; }
.status-pill.status-pending { background: #eef2f7; color: #526377; }
.status-pill.status-neutral { background: #eef2f7; color: #526377; }

.actions-col,
.actions-cell {
  text-align: right;
}

.actions-cell {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}

.icon-btn {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  border: 0;
  background: transparent;
  color: #64748b;
  display: inline-grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-left: 0;
}

.icon-btn:hover {
  background: #f1f5f9;
}

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  padding: 14px 18px 16px;
  border-top: 1px solid #e2eaf2;
}

.pagination-summary {
  color: #6a7a90;
  font-size: 13px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-left: auto;
}

.page-numbers {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  align-items: center;
}

.page-btn {
  height: 32px;
  padding: 0 11px;
  border-radius: 10px;
  border: 1px solid #d8e1eb;
  background: #fff;
  color: #41546e;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover {
  background: #f8fafc;
  border-color: #cfd9e6;
}

.page-btn-number {
  min-width: 32px;
}

.page-btn.active {
  background: #fff;
  border-color: #99f6e4;
  color: #0f766e;
  box-shadow: inset 0 0 0 1px rgba(20, 184, 166, 0.12);
}

.page-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.page-ellipsis {
  color: #9aa7b8;
  padding: 0 2px;
}

.spinner {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 3px solid rgba(20, 184, 166, 0.2);
  border-top-color: #14b8a6;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(8, 15, 28, 0.56);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 18px;
}

.modal-card {
  width: min(760px, 100%);
  max-height: 92vh;
  overflow: auto;
  background: #fff;
  border-radius: 20px;
  padding: 22px;
  box-shadow: 0 22px 60px rgba(8, 15, 28, 0.28);
}

.modal-card-large {
  width: min(980px, 100%);
}

.modal-card-small {
  width: min(540px, 100%);
}

.modal-card h2 {
  margin: 0 0 18px;
  color: #13233f;
  font-size: 20px;
  font-weight: 800;
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.modal-head h2 {
  margin: 0;
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  display: inline-grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.upload-modal {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.upload-modal .modal-head {
  padding: 18px 22px 14px;
  border-bottom: 1px solid #e2eaf2;
  align-items: flex-start;
}

.upload-body {
  padding: 16px 22px 10px;
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-area-large {
  padding: 26px 18px;
  border-radius: 16px;
  background: #ffffff;
  border: 2px dashed #d3deeb;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.upload-area-large .upload-text {
  text-align: center;
}

.upload-select-btn {
  height: 36px;
  padding: 0 16px;
}

.upload-destination {
  border-top: 1px solid #e2eaf2;
  padding-top: 14px;
}

.upload-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  border-radius: 12px;
  padding: 12px 14px;
  border: 1px solid transparent;
}

.summary-label {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 6px;
}

.summary-value {
  font-size: 20px;
  font-weight: 700;
}

.summary-blue {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1d4ed8;
}

.summary-green {
  background: #ecfdf3;
  border-color: #bbf7d0;
  color: #15803d;
}

.summary-amber {
  background: #fffbeb;
  border-color: #fde68a;
  color: #b45309;
}

.summary-red {
  background: #fff1f2;
  border-color: #fecdd3;
  color: #be123c;
}

.upload-tabs {
  display: flex;
  gap: 12px;
  border-bottom: 1px solid #e2eaf2;
}

.upload-tab {
  background: none;
  border: 0;
  padding: 10px 0;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
  border-bottom: 2px solid transparent;
  cursor: pointer;
}

.upload-tab.active {
  color: #0f766e;
  border-bottom-color: #14b8a6;
}

.upload-table {
  border: 1px solid #e2eaf2;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.upload-table-el {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.upload-table-el thead {
  background: #f8fafc;
  color: #64748b;
}

.upload-table-el th,
.upload-table-el td {
  padding: 10px 14px;
  text-align: left;
  border-bottom: 1px solid #e2eaf2;
}

.upload-table-el tbody tr:last-child td {
  border-bottom: 0;
}

.upload-empty {
  text-align: center;
  color: #94a3b8;
  padding: 18px 0;
}

.upload-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.upload-badge-success {
  background: #ecfdf3;
  color: #15803d;
}

.upload-badge-warning {
  background: #fef3c7;
  color: #b45309;
}

.create-modal {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.create-modal .modal-head {
  padding: 18px 22px 14px;
  border-bottom: 1px solid #e2eaf2;
  align-items: flex-start;
}

.create-modal .modal-head h2 {
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.modal-subtitle {
  margin: 4px 0 0;
  color: #6a7a90;
  font-size: 14px;
  line-height: 1.4;
}

.create-form {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.modal-body {
  padding: 16px 22px 8px;
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
}

.modal-section + .modal-section {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #e2eaf2;
}

.modal-section-title {
  margin: 0 0 12px;
  color: #0f172a;
  font-size: 18px;
  font-weight: 600;
}

.upload-area {
  border: 2px dashed #cfd8e4;
  border-radius: 18px;
  background: #f8fbfe;
  padding: 26px 18px;
  text-align: center;
  cursor: pointer;
}

.upload-area.drag-over {
  border-color: #14b8a6;
  background: #effcf9;
}

.upload-area.has-file {
  border-color: #2ea66c;
  background: #f0fbf5;
}

.hidden-input { display: none; }

.upload-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.upload-icon { color: #14b8a6; }
.upload-icon-ok { color: #2ea66c; }

.upload-main-text {
  margin: 0;
  color: #13233f;
  font-size: 15px;
  font-weight: 700;
}

.upload-sub-text,
.template-text {
  margin: 6px 0 0;
  color: #6a7a90;
  font-size: 13px;
}

.template-columns {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.template-columns-title {
  color: #13233f;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 10px;
}

.template-columns-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px 16px;
  color: #475569;
  font-size: 12px;
}

.template-columns-list li {
  position: relative;
  padding-left: 14px;
}

.template-columns-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 6px;
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #64748b;
}

.upload-link {
  color: #14b8a6;
  text-decoration: underline;
}

.file-name {
  word-break: break-word;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
  flex-wrap: wrap;
}

.upload-footer {
  margin-top: 0;
  padding: 14px 22px 18px;
  border-top: 1px solid #e2eaf2;
  background: #f8fafc;
}

.create-modal .modal-actions {
  margin-top: 0;
  padding: 14px 22px 18px;
  border-top: 1px solid #e2eaf2;
}

.modal-secondary {
  background: #fff;
  border: 1px solid #d7e1eb;
  color: #1f2a3d;
}

.create-modal .form-grid {
  gap: 12px 18px;
}

.create-modal .form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.create-modal .form-group input,
.create-modal .form-group select {
  font-size: 16px;
  font-weight: 400;
  color: #0f172a;
}

.create-modal .form-group input:disabled,
.create-modal .form-group select:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

.create-modal .primary-btn,
.create-modal .secondary-btn {
  font-size: 14px;
  font-weight: 500;
}

.template-actions {
  justify-content: stretch;
}

.template-actions .primary-btn {
  width: 100%;
}

.primary-btn,
.secondary-btn {
  height: 40px;
  border-radius: 12px;
  border: 0;
  padding: 0 14px;
  font-weight: 700;
  cursor: pointer;
}

.primary-btn {
  color: #fff;
  background: #14b8a6;
}

.secondary-btn {
  color: #13233f;
  background: #eef2f7;
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.alert {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 12px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.alert-error { background: #fff0f0; color: #b42318; }
.alert-success { background: #edf9f3; color: #14915e; }
.alert-warning { background: #fff8e6; color: #9d6508; flex-direction: column; }

.alert-warning-head {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.duplicate-list {
  display: grid;
  gap: 8px;
  margin-top: 10px;
}

.duplicate-item {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.66);
}

.template-preview {
  margin-top: 14px;
  padding: 12px 14px;
  background: #f8fbfe;
  border: 1px solid #dfe7f1;
  border-radius: 12px;
  color: #13233f;
  overflow: auto;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  color: #13233f;
  font-size: 13px;
  font-weight: 700;
}

.form-group input,
.form-group select {
  width: 100%;
  box-sizing: border-box;
  height: 42px;
  border-radius: 12px;
  border: 1px solid #d7e1eb;
  padding: 0 12px;
  color: #13233f;
  background: #fff;
  outline: none;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.1);
}

@media (max-width: 1280px) {
  .stats-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .advanced-filters {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .upload-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filters-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filters-button {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 720px) {
  .people-page {
    padding: 12px;
  }

  .stats-grid,
  .advanced-filters,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .upload-grid,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .page-header-top h1 {
    font-size: 28px;
  }

  .top-actions {
    width: 100%;
  }

  .ghost-btn,
  .primary-btn {
    width: 100%;
  }

  .pagination-row {
    justify-content: center;
  }
}
</style>