<template>
  <div class="figma-page">
    <ConfirmDialog ref="confirmDialog" />

    <div v-if="loading" class="state-box">Carregando...</div>
    <div v-else-if="error" class="state-box state-error">{{ error }}</div>

    <template v-else>
      <header class="hero">
        <div class="hero-inner">
          <div class="hero-top">
            <button class="back-btn" @click="goBack">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 12H5M12 19l-7-7 7-7" />
              </svg>
              <span>Voltar para Programas</span>
            </button>

            <div class="hero-actions">
              <button class="btn-primary" @click="editProgram">
                <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 20h9" />
                  <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z" />
                </svg>
                <span>Editar programa</span>
              </button>
            </div>
          </div>

          <div class="hero-main">
            <h1>Turma {{ classData.code || '-' }}</h1>

            <div class="badge-row">
              <span class="badge badge-teal">{{ classData.program?.name || 'Programa' }}</span>
              <span class="badge" :class="statusBadgeClass">{{ classStatusLabel }}</span>
              <span class="badge badge-purple">Etapa: {{ currentStageLabel }}</span>
            </div>

            <div class="meta-row">
              <span class="meta-item">
                <svg class="meta-icon" xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z" />
                </svg>
                {{ partnerLabel }}
              </span>
              <span class="meta-item">
                <svg class="meta-icon" xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 10c0 5-8 12-8 12s-8-7-8-12a8 8 0 0 1 16 0Z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                {{ classData.location?.name || '-' }}
              </span>
              <span class="meta-item">
                <svg class="meta-icon" xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M8 2v4" />
                  <path d="M16 2v4" />
                  <rect width="18" height="18" x="3" y="4" rx="2" />
                  <path d="M3 10h18" />
                </svg>
                {{ classPeriodLabel }}
              </span>
            </div>
          </div>
        </div>

        <div class="hero-cards">
          <article class="hero-card">
            <span class="hero-label">Inscritos</span>
            <strong>{{ totalCandidates || 612 }}</strong>
            <small>+48 em lista</small>
            <small class="hero-support">Candidatos cadastrados</small>
          </article>
          <article class="hero-card">
            <span class="hero-label">Nivelamento</span>
            <strong>{{ overviewTopCards.nivelamentoSelecionados }}</strong>
            <small>{{ overviewTopCards.nivelamentoAtivos }} ativos</small>
            <small class="hero-support">Selecionados</small>
          </article>
          <article class="hero-card">
            <span class="hero-label">Imersão</span>
            <strong class="teal-strong">{{ overviewTopCards.imersaoAprovados }}</strong>
            <small>{{ overviewTopCards.imersaoGrupos }} grupos</small>
            <small class="hero-support">Aprovados para fase final</small>
          </article>
          <article class="hero-card">
            <span class="hero-label">Alertas Críticos</span>
            <strong class="warning-strong">{{ overviewTopCards.alertasCriticos }}</strong>
            <small class="warning-small">{{ overviewTopCards.alertasRisco }} em risco</small>
            <small class="hero-support">Pendências ativas</small>
          </article>
        </div>

        <div class="tabs">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            type="button"
            class="tab"
            :class="{ active: activeTab === tab.id }"
            @click="activeTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </div>
      </header>

      <main class="content">
        <section v-if="activeTab === 'visao-geral'" class="stack">
          <article class="status-card">
            <div class="status-head">
              <h3>Situação Atual</h3>
              <span class="pill pill-green">{{ classStatusLabel }}</span>
              <span class="pill pill-purple">Etapa: {{ currentStageLabel }}</span>
            </div>

            <div class="status-grid">
              <div>
                <span>Período da etapa</span>
                <strong>{{ currentStagePeriod }}</strong>
              </div>
              <div>
                <span>Próximo marco</span>
                <strong class="teal-strong">{{ currentStageMilestone }}</strong>
              </div>
              <div>
                <span>Data</span>
                <strong>{{ currentStageMilestoneDate }}</strong>
              </div>
              <div>
                <span>Modelo</span>
                <strong>{{ classModelLabel }}</strong>
              </div>
              <div>
                <span>Carga horária</span>
                <strong>{{ classWorkloadLabel }}</strong>
              </div>
              <div>
                <span>Local</span>
                <strong>{{ classData.location?.name || '-' }}</strong>
              </div>
            </div>

            <p class="status-note">
              A turma está em desenvolvimento dos projetos, com avaliação parcial como próximo marco.
            </p>
          </article>

          <article class="panel">
            <div class="panel-head">
              <h3>Ciclo do Programa</h3>
              <div class="panel-progress">Progresso Geral <strong>{{ overviewProgressPct }}%</strong></div>
            </div>

            <div class="progress-track">
              <div class="progress-fill" :style="{ width: `${overviewProgressPct}%` }" />
            </div>

            <div class="cycle">
              <div v-for="(item, index) in overviewCycle" :key="item" class="cycle-item">
                <div class="cycle-pill" :class="{ done: index < overviewCurrentCycleIndex, current: index === overviewCurrentCycleIndex }">
                  {{ item }}
                </div>
                <div v-if="index < overviewCycle.length - 1" class="cycle-line" :class="{ done: index < overviewCurrentCycleIndex }" />
              </div>
            </div>
          </article>

          <section class="overview-block">
            <h3 class="overview-title">Resumo por Etapa</h3>
            <div class="summary-grid">
              <article v-for="card in overviewStageCards" :key="card.title" class="summary-card">
                <div class="summary-card-head">
                  <h4>{{ card.title }}</h4>
                  <span class="pill" :class="card.statusClass">{{ card.status }}</span>
                </div>
                <div class="summary-card-list">
                  <div v-for="item in card.items" :key="item.label">
                    <span>{{ item.label }}</span>
                    <strong :class="item.valueClass">{{ item.value }}</strong>
                  </div>
                </div>
                <button type="button" class="details-link">Ver detalhes →</button>
              </article>
            </div>
          </section>

          <article class="panel">
            <h3 class="section-subtitle">Cronograma Resumido</h3>
            <div class="timeline-list">
              <div v-for="item in overviewTimeline" :key="item.label" class="timeline-row">
                <div class="timeline-left">
                  <span class="timeline-check">✓</span>
                  <span class="timeline-label">{{ item.label }}</span>
                </div>
                <div class="timeline-right">
                  <span class="timeline-date">{{ item.date }}</span>
                  <span class="pill pill-green">{{ item.status }}</span>
                </div>
              </div>
            </div>
            <button type="button" class="details-link timeline-link">Ver cronograma completo →</button>
          </article>

          <section class="overview-block">
            <h3 class="overview-title">Distribuição dos Alunos</h3>
            <div class="distribution-grid">
              <article class="panel distribution-panel">
                <h4>Por Cota</h4>
                <div class="quota-list">
                  <div v-for="item in quotaDistribution" :key="item.label" class="quota-row">
                    <div class="quota-head">
                      <span>{{ item.label }}</span>
                      <strong>{{ item.value }}%</strong>
                    </div>
                    <div class="quota-track">
                      <div class="quota-fill" :style="{ width: `${item.value}%`, background: item.color }" />
                    </div>
                  </div>
                </div>
              </article>

              <article class="panel distribution-panel">
                <h4>Por Gênero</h4>
                <div class="gender-grid">
                  <div v-for="item in genderDistribution" :key="item.label" class="gender-card">
                    <strong>{{ item.value }}%</strong>
                    <span>{{ item.label }}</span>
                  </div>
                </div>
              </article>

              <article class="panel distribution-panel">
                <h4>Por Cidade/UF</h4>
                <div class="simple-list">
                  <div v-for="item in cityDistribution" :key="item.label">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                </div>
              </article>

              <article class="panel distribution-panel">
                <h4>Por Tipo de Formação</h4>
                <div class="simple-list">
                  <div v-for="item in educationDistribution" :key="item.label">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                </div>
              </article>
            </div>
          </section>

          <article class="panel updates-panel">
            <h3 class="section-subtitle">Últimas Atualizações</h3>
            <div class="updates-list">
              <div v-for="item in overviewUpdates" :key="item.action" class="update-row">
                <span class="update-dot" :class="item.dotClass" />
                <span class="update-action">{{ item.action }}</span>
                <div class="update-meta">
                  <span>{{ item.author }}</span>
                  <span>{{ item.date }}</span>
                  <span class="pill" :class="item.statusClass">{{ item.status }}</span>
                </div>
              </div>
            </div>
          </article>
        </section>

        <section v-else-if="activeTab === 'pessoas'" class="stack">
          <article class="panel people-panel">
            <div class="people-filters">
              <div class="people-search-row">
                <div class="people-search-box">
                  <svg xmlns="http://www.w3.org/2000/svg" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="people-search-icon">
                    <circle cx="11" cy="11" r="8" />
                    <path d="m21 21-4.35-4.35" />
                  </svg>
                  <input
                    v-model="peopleSearch"
                    type="text"
                    class="people-search-input"
                    placeholder="Buscar por nome, CPF ou e-mail..."
                  />
                </div>
                <button type="button" class="people-search-btn">Pesquisar</button>
              </div>

              <div class="people-select-row">
                <select v-model="peopleFilterStage" class="people-filter-select">
                  <option value="">Etapa</option>
                  <option v-for="option in peopleStageOptions" :key="option" :value="option">{{ option }}</option>
                </select>
                <select v-model="peopleFilterStatus" class="people-filter-select">
                  <option value="">Status</option>
                  <option v-for="option in peopleStatusOptions" :key="option" :value="option">{{ option }}</option>
                </select>
                <select v-model="peopleFilterQuota" class="people-filter-select">
                  <option value="">Cota</option>
                  <option v-for="option in peopleQuotaOptions" :key="option" :value="option">{{ option }}</option>
                </select>
                <select v-model="peopleFilterCity" class="people-filter-select">
                  <option value="">Cidade</option>
                  <option v-for="option in peopleCityOptions" :key="option" :value="option">{{ option }}</option>
                </select>
                <select v-model="peopleFilterGender" class="people-filter-select">
                  <option value="">Gênero</option>
                  <option v-for="option in peopleGenderOptions" :key="option" :value="option">{{ option }}</option>
                </select>
              </div>
            </div>

            <div v-if="peopleLoading" class="state-box">Carregando pessoas da turma...</div>
            <div v-else-if="peopleError" class="state-box state-error">{{ peopleError }}</div>
            <div v-else-if="filteredClassPeople.length === 0" class="state-box">Nenhuma pessoa vinculada a esta turma.</div>

            <template v-else>
              <div class="people-table-wrap">
                <table class="people-table-v2">
                  <thead>
                    <tr>
                      <th>Nome</th>
                      <th>CPF</th>
                      <th>E-mail</th>
                      <th>Gênero</th>
                      <th>Idade</th>
                      <th>Cota</th>
                      <th>Cidade</th>
                      <th>Etapa Atual</th>
                      <th>Status</th>
                      <th class="actions-col">Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="person in paginatedClassPeople" :key="person.id">
                      <td class="name-cell">{{ person.name }}</td>
                      <td>{{ formatCPF(person.cpf) }}</td>
                      <td>{{ person.email || '-' }}</td>
                      <td>{{ person.gender || '-' }}</td>
                      <td>{{ person.age }}</td>
                      <td>
                        <span v-if="person.quota !== '-'" class="quota-pill">{{ person.quota }}</span>
                        <span v-else>-</span>
                      </td>
                      <td>{{ person.city }}</td>
                      <td>{{ person.stage }}</td>
                      <td>
                        <span class="status-pill" :class="person.statusClass">{{ person.status }}</span>
                      </td>
                      <td class="actions-col">
                        <button type="button" class="icon-btn" title="Visualizar pessoa" @click="viewPerson(person)">
                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                            <circle cx="12" cy="12" r="3" />
                          </svg>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="people-pagination">
                <div class="people-page-summary">
                  Mostrando {{ classPeoplePageStart }}-{{ classPeoplePageEnd }} de {{ filteredClassPeople.length }} pessoas vinculadas a este programa
                </div>
                <div class="people-page-controls">
                  <button type="button" class="page-btn" :disabled="classPeoplePage === 1" @click="classPeoplePrevPage">Anterior</button>
                  <div class="page-numbers">
                    <button
                      v-for="page in classPeopleVisiblePages"
                      :key="page"
                      type="button"
                      class="page-btn page-btn-number"
                      :class="{ active: page === classPeoplePage }"
                      @click="classPeoplePage = page"
                    >
                      {{ page }}
                    </button>
                  </div>
                  <button type="button" class="page-btn" :disabled="classPeoplePage === classPeopleTotalPages" @click="classPeopleNextPage">Próximo</button>
                </div>
              </div>
            </template>
          </article>
        </section>

        <section v-else-if="activeTab === 'processo-seletivo'" class="stack">
          <div class="metrics-row">
            <article v-for="card in selectionProcessMetricsCards" :key="card.label" class="metric-card">
              <span class="metric-label">{{ card.label }}</span>
              <strong :class="card.valueClass">{{ card.value }}</strong>
            </article>
          </div>

          <article class="panel">
            <div class="panel-head">
              <h3>Distribuição de Vagas por Cotas</h3>
              <button type="button" class="btn-primary" @click="openUpdateSelectionModal">Atualizar dados</button>
            </div>
            <div class="quota-distribution">
              <article v-for="item in selectionQuotaCards" :key="item.label" class="quota-item">
                <strong>{{ item.value }}</strong>
                <span>{{ item.label }}</span>
              </article>
            </div>
          </article>

          <div class="alert-banner alert-warning">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 9v4" />
              <path d="M12 17h.01" />
              <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z" />
            </svg>
            <div class="alert-copy">
              <strong>{{ selectionConflictSummary }}</strong>
              <p>Isso indica possível conflito de inscrição</p>
            </div>
            <button type="button" class="alert-link" @click="toggleSelectionConflicts">{{ selectionConflictButtonLabel }}</button>
          </div>

          <article class="panel">
            <div class="people-table-wrap">
              <table class="people-table-v2">
                <thead>
                  <tr>
                    <th>Nome</th>
                    <th>CPF</th>
                    <th>E-mail</th>
                    <th>Gênero</th>
                    <th>Idade</th>
                    <th>Cota</th>
                    <th>Cidade</th>
                    <th>Formação</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in paginatedSelectionProcessRows" :key="item.id">
                    <td class="name-cell">
                      <span>{{ item.name }}</span>
                      <span v-if="item.conflict" class="selection-conflict-icon" title="Conflito em outro programa">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M12 9v4" />
                          <path d="M12 17h.01" />
                          <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z" />
                        </svg>
                      </span>
                    </td>
                    <td>{{ formatCPF(item.cpf) }}</td>
                    <td>{{ item.email }}</td>
                    <td>{{ item.gender }}</td>
                    <td>{{ item.age }}</td>
                    <td>
                      <span v-if="item.quota !== '-'" class="quota-pill">{{ item.quota }}</span>
                      <span v-else>-</span>
                    </td>
                    <td>{{ item.city }}</td>
                    <td>{{ item.education }}</td>
                    <td>
                      <span class="status-pill" :class="item.statusClass">{{ item.status }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="people-pagination">
              <div class="people-page-summary">
                {{ selectionProcessPageSummary }}
              </div>
              <div class="people-page-controls">
                <button type="button" class="page-btn" :disabled="selectionProcessPage === 1" @click="selectionProcessPrevPage">Anterior</button>
                <div class="page-numbers">
                  <button
                    v-for="page in selectionProcessVisiblePages"
                    :key="page"
                    type="button"
                    class="page-btn page-btn-number"
                    :class="{ active: selectionProcessPage === page }"
                    @click="selectionProcessPage = page"
                  >
                    {{ page }}
                  </button>
                </div>
                <button type="button" class="page-btn" :disabled="selectionProcessPage === selectionProcessTotalPages" @click="selectionProcessNextPage">Próximo</button>
              </div>
            </div>
          </article>

        </section>

        <section v-else class="stack">
          <!-- Sub-tabs for Etapas -->
          <div class="bg-white border-b border-slate-200">
            <div class="px-6">
              <div class="tabs-bg flex items-center gap-1">
                <button type="button" @click="etapasSubTab = 'nivelamento'" :class="['tab-btn', etapasSubTab === 'nivelamento' ? 'active' : '']">Nivelamento</button>
                <button type="button" @click="etapasSubTab = 'imersao'" :class="['tab-btn', etapasSubTab === 'imersao' ? 'active' : '']">Imersão</button>
              </div>
            </div>
          </div>

          <div v-if="etapasSubTab === 'nivelamento'" class="space-y-4">
             <div class="nivelamento-cards">
              <div class="n-card">
                <div class="label">Alunos no nivelamento</div>
                <div class="value">{{ getNivelamentoStudents.length }}</div>
              </div>
               <div class="n-card teal">
                <div class="label">Ativos</div>
                <div class="value">{{ courseStats.inProgress + courseStats.completed }}</div>
              </div>
              <div class="n-card">
                <div class="label">Cursos obrigatórios</div>
                <div class="value">{{ courseItems.filter(c => c.required).length }}</div>
              </div>
              <div class="n-card">
                <div class="label">Cursos não obrigatórios</div>
                <div class="value">{{ courseItems.filter(c => !c.required).length }}</div>
              </div>
               <div class="n-card teal">
                <div class="label">Conclusão obrigatórios</div>
                <div class="value">{{ courseItems.length ? Math.round((courseItems.filter(c=>c.required && c.pctCompleted).length / Math.max(1, courseItems.filter(c=>c.required).length)) * 100) + '%' : '—' }}</div>
              </div>
               <div class="n-card amber">
                <div class="label">Nota de corte prova</div>
                <div class="value">39</div>
              </div>
               <div class="n-card red">
                <div class="label">Alertas</div>
                <div class="value">{{ courseItems.filter(c => c.completionPct < 40).length }}</div>
              </div>
            </div>

            <div class="email-banner">
              <div style="display:flex;align-items:center;gap:12px;justify-content:space-between;width:100%;">
                <div style="display:flex;align-items:center;gap:12px;">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 1v11"/><path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z"/></svg>
                  <div>
                    <span style="font-weight:600;color:#075985;">Último e-mail enviado: </span>
                    <span style="color:#0c4a6e;">{{ lastEmailInfo }}</span>
                  </div>
                </div>
                <span class="email-banner-status" v-if="lastEmailInfo && lastEmailInfo !== 'Nenhum e-mail enviado recentemente'">Enviado</span>
              </div>
            </div>

            <div class="nivelamento-actions">
              <button type="button" class="btn-outline" @click="openUpdateSelectionModal"><span>Atualizar dados</span></button>
              <button type="button" class="btn-outline" @click="showSubmitCoursesModal = true">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="17 8 12 3 7 8" />
                  <line x1="12" y1="3" x2="12" y2="15" />
                </svg>
                <span>Submeter dados dos cursos</span>
              </button>
              <button type="button" class="btn-outline" @click="showSubmitProvaNotasModal = true">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="17 8 12 3 7 8" />
                  <line x1="12" y1="3" x2="12" y2="15" />
                </svg>
                <span>Submeter notas da prova</span>
              </button>
              <div class="spacer"></div>
              <button type="button" class="btn-primary" @click="showSendMessageModal = true"><span>Enviar mensagem</span></button>
            </div>
          </div>

          <template v-if="etapasSubTab === 'nivelamento'">
          <!-- Seção Cursos do Nivelamento -->
          <article class="panel">
           <div class="panel-head">
             <h3>Cursos do Nivelamento</h3>
             <button v-if="courseItems && courseItems.length" type="button" class="btn-primary" @click="openAddCoursesModal">Gerenciar cursos</button>
           </div>

           <div class="courses-list">
             <div v-if="!courseItems || courseItems.length === 0" class="no-data">Nenhum curso encontrado para o nivelamento.</div>

             <div
               v-for="course in courseItems"
               :key="course?.id"
               class="course-card-new"
               role="button"
               tabindex="0"
               @click="openCourseDetails(course)"
               @keydown.enter.prevent="openCourseDetails(course)"
               @keydown.space.prevent="openCourseDetails(course)"
             >
               <div class="course-left-new">
                 <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="course-icon">
                   <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                   <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z" />
                 </svg>
                 <span class="course-name-new">{{ course?.name || 'Sem nome' }}</span>
                 <span v-if="course?.required" class="course-badge course-badge-required">Obrigatório</span>
                 <span v-if="course?.knowledgeArea" class="course-badge">{{ course.knowledgeArea }}</span>
               </div>

               <div class="course-right-new">
                 <div class="course-stats-new">
                   <div class="course-stats-text">{{ course?.completedCount || 0 }} concluídos · {{ course?.pendingCount || 0 }} pendentes</div>
                   <div class="progress-container">
                     <div class="progress-bar">
                       <div class="progress-fill" :style="{ width: (course?.completionPct || 0) + '%', backgroundColor: getCompletionColor(course?.completionPct || 0) }"></div>
                     </div>
                     <span class="progress-pct">{{ course?.completionPct || 0 }}%</span>
                    </div>
                 </div>
                 <div class="course-media">Média: {{ course?.completionPct || 0 }}</div>
                </div>
              </div>
           </div>

           <div class="alert-banner alert-warning">
             <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3.05L13.71 3.86a2 2 0 0 0-3.42 0z" />
               <line x1="12" y1="9" x2="12" y2="13" />
               <line x1="12" y1="17" x2="12.01" y2="17" />
             </svg>
             <div class="alert-copy">
               <strong>Cursos obrigatórios com pendências</strong>
               <p>37 alunos ainda possuem pendências em cursos obrigatórios</p>
             </div>
             <button type="button" class="alert-link">Enviar mensagem</button>
           </div>
          </article>

          <!-- Seção Prova Final do Nivelamento -->
          <article class="panel">
           <div class="panel-head">
             <h3>Prova Final do Nivelamento</h3>
           </div>

           <div class="exam-stats">
             <div class="exam-stat-card">
               <div class="exam-stat-label">Data da prova</div>
               <div class="exam-stat-value">{{ overviewTimeline[5]?.date || '30/04' }}</div>
             </div>
             <div class="exam-stat-card">
               <div class="exam-stat-label">Questões</div>
               <div class="exam-stat-value">--</div>
             </div>
             <div class="exam-stat-card">
               <div class="exam-stat-label">Duração</div>
               <div class="exam-stat-value">--</div>
             </div>
             <div class="exam-stat-card">
               <div class="exam-stat-label">Média geral</div>
               <div class="exam-stat-value teal">--</div>
             </div>
             <div class="exam-stat-card">
               <div class="exam-stat-label">Maior nota</div>
               <div class="exam-stat-value emerald">--</div>
             </div>
             <div class="exam-stat-card">
               <div class="exam-stat-label">Nota de corte</div>
               <div class="exam-stat-value amber">--</div>
             </div>
           </div>

           <div class="exam-criteria">
             <p><strong>Critério de aprovação:</strong> A aprovação no nivelamento considera nota igual ou superior a 50% da maior nota obtida na turma, além da conclusão dos cursos obrigatórios.</p>
           </div>
          </article>

          <!-- Seção Alunos do Nivelamento -->
          <article class="panel">
           <div class="panel-head">
             <h3>Alunos do Nivelamento</h3>
           </div>

           <div class="students-table">
             <table>
               <thead>
                 <tr>
                   <th>Nome</th>
                   <th>CPF</th>
                   <th>Progresso médio</th>
                   <th>Status</th>
                 </tr>
               </thead>
               <tbody>
                 <tr v-for="student in getNivelamentoStudents" :key="student.id">
                   <td>{{ student.name || student.id }}</td>
                   <td>{{ student.cpf || '-' }}</td>
                   <td>
                     <div class="student-progress">
                       <div class="student-progress-bar">
                         <div class="student-progress-fill" :style="{ width: student.avg + '%' }"></div>
                       </div>
                       <small>{{ student.avg }}%</small>
                     </div>
                   </td>
                   <td><span class="status-pill" :class="student.avg===100 ? 'status-approved' : 'status-inprogress'">{{ student.avg===100 ? 'Concluído' : 'Em andamento' }}</span></td>
                 </tr>
               </tbody>
             </table>
           </div>
          </article>
          </template>

          <div v-else class="imersao-section">
            <div class="imersao-metrics-grid">
              <div v-for="metric in imersaoMetricsCards" :key="metric.label" class="imersao-metric-card">
                <span class="imersao-metric-label">{{ metric.label }}</span>
                <strong :class="metric.valueClass || ''">{{ metric.value }}</strong>
                <small v-if="metric.support">{{ metric.support }}</small>
              </div>
            </div>

            <div class="imersao-actions">
              <button type="button" class="btn-outline" @click="showImportImersaoModal = true">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 5 17 10" />
                  <line x1="12" y1="5" x2="12" y2="17" />
                </svg>
                <span>Importar alunos da imersão</span>
              </button>

              <button type="button" class="btn-outline" @click="showSubmitNotasImersaoModal = true">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 5 17 10" />
                  <line x1="12" y1="5" x2="12" y2="17" />
                </svg>
                <span>Submeter notas</span>
              </button>

              <button type="button" class="btn-outline" @click="showAtualizarPresencaImersaoModal = true">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M3 12a9 9 0 1 0 3-6.7" />
                  <polyline points="3 3 3 9 9 9" />
                </svg>
                <span>Atualizar presença</span>
              </button>

              <button type="button" class="btn-primary" @click="showGroupCreateModal = true">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <line x1="19" y1="8" x2="19" y2="14" />
                  <line x1="22" y1="11" x2="16" y2="11" />
                </svg>
                <span>Cadastrar grupo</span>
              </button>
            </div>

            <div class="imersao-groups-list">
              <div v-for="group in imersaoGroups" :key="group.id" class="imersao-group-item">
                <button type="button" class="imersao-group-card" @click="toggleImersaoGroup(group.id)">
                  <div class="imersao-group-main">
                    <div class="imersao-group-title">
                      <strong>{{ group.name }}</strong>
                      <span class="imersao-group-status" :class="group.statusClass">{{ group.status }}</span>
                    </div>
                    <div class="imersao-group-sub">
                      <span>Orientador:</span>
                      <strong>{{ group.mentor }}</strong>
                    </div>
                  </div>

                  <div class="imersao-group-project">
                    <span>Projeto:</span>
                    <strong>{{ group.project }}</strong>
                  </div>

                  <div class="imersao-group-students">
                    <span>Alunos:</span>
                    <strong>{{ group.students }}</strong>
                  </div>

                  <div class="imersao-group-avg">
                    <span>Média parcial:</span>
                    <strong class="teal">{{ group.partialAverage }}</strong>
                    <span>Final:</span>
                    <strong class="teal">{{ group.finalAverage }}</strong>
                  </div>

                  <div class="imersao-group-arrow" aria-hidden="true">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline v-if="imersaoExpandedGroupId === group.id" points="6 9 12 15 18 9" />
                      <polyline v-else points="9 18 15 12 9 6" />
                    </svg>
                  </div>
                </button>

                <div v-if="imersaoExpandedGroupId === group.id" class="imersao-group-expanded">
                  <div class="imersao-group-tabs">
                    <button type="button" class="imersao-group-tab-btn" :class="{ active: getImersaoGroupTab(group.id) === 'resumo' }" @click="setImersaoGroupTab(group.id, 'resumo')">Resumo</button>
                    <button type="button" class="imersao-group-tab-btn" :class="{ active: getImersaoGroupTab(group.id) === 'parcial' }" @click="setImersaoGroupTab(group.id, 'parcial')">Avaliação Parcial</button>
                    <button type="button" class="imersao-group-tab-btn" :class="{ active: getImersaoGroupTab(group.id) === 'final' }" @click="setImersaoGroupTab(group.id, 'final')">Avaliação Final</button>
                    <button type="button" class="imersao-group-tab-btn" :class="{ active: getImersaoGroupTab(group.id) === 'presenca' }" @click="setImersaoGroupTab(group.id, 'presenca')">Presença</button>
                  </div>

                  <div v-if="getImersaoGroupTab(group.id) === 'resumo'" class="imersao-group-panel">
                    <div class="imersao-group-meta">
                      <p><strong>Projeto:</strong> {{ group.project }}</p>
                      <p><strong>Empresa parceira:</strong> {{ group.partnerCompany }}</p>
                      <p class="muted">Última atualização de notas: {{ group.lastGradesUpdate }}</p>
                    </div>
                    <table class="imersao-group-table">
                      <thead>
                        <tr>
                          <th>Aluno</th>
                          <th>Média Parcial</th>
                          <th>Média Final</th>
                          <th>Situação</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="student in group.studentsDetails" :key="student.id">
                          <td>{{ student.name }}</td>
                          <td :class="{ 'warning-strong': Number(student.partial) < 4 }">{{ student.partial }}</td>
                          <td :class="{ 'warning-strong': Number(student.final) < 4 }">{{ student.final }}</td>
                          <td><span class="imersao-situation-pill" :class="student.situationClass">{{ student.situation }}</span></td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div v-else-if="getImersaoGroupTab(group.id) === 'parcial'" class="imersao-group-panel">
                    <table class="imersao-group-table">
                      <thead>
                        <tr>
                          <th>Aluno</th>
                          <th>Nota parcial</th>
                          <th>Observação</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="student in group.studentsDetails" :key="`p-${student.id}`">
                          <td>{{ student.name }}</td>
                          <td :class="{ 'warning-strong': Number(student.partial) < 4 }">{{ student.partial }}</td>
                          <td>{{ Number(student.partial) < 4 ? 'Acompanhamento necessário' : 'Desempenho esperado' }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div v-else-if="getImersaoGroupTab(group.id) === 'final'" class="imersao-group-panel">
                    <table class="imersao-group-table">
                      <thead>
                        <tr>
                          <th>Aluno</th>
                          <th>Nota final</th>
                          <th>Situação</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="student in group.studentsDetails" :key="`f-${student.id}`">
                          <td>{{ student.name }}</td>
                          <td :class="{ 'warning-strong': Number(student.final) < 4 }">{{ student.final }}</td>
                          <td><span class="imersao-situation-pill" :class="student.situationClass">{{ student.situation }}</span></td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div v-else class="imersao-group-panel">
                    <table class="imersao-group-table">
                      <thead>
                        <tr>
                          <th>Aluno</th>
                          <th>Última reunião</th>
                          <th>Presença</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="student in group.studentsDetails" :key="`a-${student.id}`">
                          <td>{{ student.name }}</td>
                          <td>{{ group.lastMeetingDate }}</td>
                          <td>
                            <span class="imersao-situation-pill" :class="student.attendedLastMeeting ? 'status-regular' : 'status-warning'">
                              {{ student.attendedLastMeeting ? 'Presente' : 'Ausente' }}
                            </span>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </template>

    <!-- Modal: Create Stage -->
    <div v-if="showCreateStageModal" class="modal-overlay" @click="closeCreateStageModal">
      <div class="modal" @click.stop>
        <h2>Criar Nova Etapa</h2>
        <div class="form-row">
          <label for="stageName">Nome da Etapa *</label>
          <input id="stageName" v-model="newStage.name" type="text" class="field" placeholder="Ex: Seleção, Entrevista, Prova..." />
        </div>
        <div class="form-row">
          <label for="stageDescription">Descrição</label>
          <textarea id="stageDescription" v-model="newStage.description" class="field" rows="3" placeholder="Descrição da etapa (opcional)"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn-outline" @click="closeCreateStageModal">Cancelar</button>
          <button class="btn-primary" :disabled="!newStage.name || creatingStage" @click="createStage">
            {{ creatingStage ? 'Criando...' : 'Criar' }}
          </button>
        </div>
        <div v-if="stageError" class="state-error">{{ stageError }}</div>
      </div>
    </div>

    <!-- Modal: Update Selection Process -->
    <div v-if="showUpdateSelectionModal" class="modal-overlay" @click="closeUpdateSelectionModal">
      <div :class="['modal', selectedUpdateAction === 'lista-espera' ? 'modal-waitlist' : 'modal-large']" @click.stop>
        <div class="modal-header">
          <h2>Atualizar dados do processo seletivo</h2>
          <button type="button" class="modal-close" @click="closeUpdateSelectionModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div v-if="!selectedUpdateAction" class="modal-content update-selection-home">
          <p class="modal-subtitle">Escolha uma das ações abaixo para atualizar os dados do processo seletivo:</p>
          
          <div class="update-actions-grid">
            <button type="button" class="update-action-card" @click="openIndividualRegistration">
              <div class="card-icon icon-people">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </div>
              <h4>Cadastrar aluno individualmente</h4>
              <p>Adicione um novo candidato manualmente ao processo seletivo</p>
            </button>

            <button type="button" class="update-action-card" @click="selectedUpdateAction = 'import-inscricoes'">
              <div class="card-icon icon-upload">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="17 8 12 3 7 8" />
                  <line x1="12" y1="3" x2="12" y2="15" />
                </svg>
              </div>
              <h4>Importar planilha de inscritos</h4>
              <p>Envie uma planilha com a lista de candidatos inscritos</p>
            </button>

            <button type="button" class="update-action-card" @click="selectedUpdateAction = 'import-aprovados'">
              <div class="card-icon icon-check">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                  <polyline points="22 4 12 14.01 9 11.01" />
                </svg>
              </div>
              <h4>Importar planilha de aprovados</h4>
              <p>Atualize os status dos candidatos aprovados via planilha</p>
            </button>

            <button type="button" class="update-action-card" @click="selectedUpdateAction = 'lista-espera'">
              <div class="card-icon icon-clock">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
              </div>
              <h4>Atualizar lista de espera</h4>
              <p>Gerencie a lista de espera e convocações adicionais</p>
            </button>
          </div>

          <div class="modal-actions modal-actions-footer">
            <button type="button" class="btn-outline" @click="closeUpdateSelectionModal">Cancelar</button>
          </div>
        </div>

        <!-- Individual Registration -->
        <div v-else-if="selectedUpdateAction === 'individual'" class="modal-content modal-large individual-modal">
          <div class="modal-back">
            <button type="button" @click="closeIndividualRegistration" class="back-link">← Voltar</button>
          </div>
          <h3>Cadastrar aluno individualmente</h3>

          <div class="form-grid individual-form-grid">
            <div class="candidate-field-row">
              <label for="regName">Nome completo</label>
              <input id="regName" v-model="newCandidateForm.name" type="text" class="field" placeholder="Digite o nome completo" />
            </div>
            <div class="candidate-field-row">
              <label for="regCPF">CPF</label>
              <input id="regCPF" v-model="newCandidateForm.cpf" type="text" class="field" placeholder="000.000.000-00" />
            </div>
            <div class="candidate-field-row">
              <label for="regEmail">E-mail</label>
              <input id="regEmail" v-model="newCandidateForm.email" type="email" class="field" placeholder="exemplo@email.com" />
            </div>
            <div class="candidate-field-row">
              <label for="regGender">Gênero</label>
              <select id="regGender" v-model="newCandidateForm.gender" class="field">
                <option value="">Selecione</option>
                <option value="Feminino">Feminino</option>
                <option value="Masculino">Masculino</option>
                <option value="Outro">Outro</option>
                <option value="Não informado">Não informado</option>
              </select>
            </div>
            <div class="candidate-field-row">
              <label for="regBirthDate">Data de nascimento</label>
              <input id="regBirthDate" v-model="newCandidateForm.birthDate" type="text" class="field" placeholder="dd/mm/aaaa" />
            </div>
            <div class="candidate-field-row">
              <label for="regCity">Cidade/UF</label>
              <input id="regCity" v-model="newCandidateForm.city" type="text" class="field" placeholder="Maceió - AL" />
            </div>
            <div class="candidate-field-row">
              <label for="regEducation">Tipo de formação</label>
              <select id="regEducation" v-model="newCandidateForm.education" class="field">
                <option value="">Selecione</option>
                <option value="Graduação em andamento">Graduação em andamento</option>
                <option value="Graduação concluída">Graduação concluída</option>
                <option value="Curso técnico">Curso técnico</option>
                <option value="Outros cursos de exatas/tecnologia">Outros cursos de exatas/tecnologia</option>
              </select>
            </div>
            <div class="candidate-field-row">
              <label for="regInstitution">Instituição</label>
              <input id="regInstitution" v-model="newCandidateForm.institution" type="text" class="field" placeholder="Nome da instituição" />
            </div>
            <div class="candidate-field-row">
              <label for="regQuota">Cota</label>
              <select id="regQuota" v-model="newCandidateForm.quota" class="field">
                <option value="">Selecione</option>
                <option value="Ampla concorrência">Ampla concorrência</option>
                <option value="PCD/Neurodivergente">PCD/Neurodivergente</option>
                <option value="Negro/Pardo">Negro/Pardo</option>
                <option value="Mulher">Mulher</option>
                <option value="45+">45+</option>
              </select>
            </div>
            <div class="candidate-field-row">
              <label for="regStatus">Status inicial</label>
              <select id="regStatus" v-model="newCandidateForm.status" class="field">
                <option value="">Selecione</option>
                <option value="Inscrito">Inscrito</option>
                <option value="Em análise">Em análise</option>
                <option value="Aprovado">Aprovado</option>
                <option value="Lista de espera">Lista de espera</option>
              </select>
            </div>
          </div>

          <div class="info-box info-blue">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <div>
              <strong>Validação automática</strong>
              <p>O sistema verificará automaticamente se este CPF já está vinculado a outro programa vigente.</p>
            </div>
          </div>

          <div v-if="registrationError" class="state-error registration-error">{{ registrationError }}</div>

          <div class="modal-actions">
            <button type="button" class="btn-outline" @click="closeIndividualRegistration">Cancelar</button>
            <button type="button" class="btn-primary" :disabled="registeringCandidate" @click="registerCandidateIndividually">
              {{ registeringCandidate ? 'Cadastrando...' : 'Cadastrar aluno' }}
            </button>
          </div>
        </div>

        <!-- Import Inscricoes -->
        <div v-else-if="selectedUpdateAction === 'import-inscricoes'" class="modal-content import-inscricoes-modal">
          <div class="modal-back">
            <button type="button" @click="selectedUpdateAction = null" class="back-link">← Voltar</button>
          </div>
          <h3>Importar planilha de inscritos</h3>
          <p class="modal-desc">Envie uma planilha Excel ou CSV com os dados dos candidatos inscritos. A planilha deve conter as seguintes colunas:</p>

          <div class="columns-grid">
            <div class="column-item">Nome completo</div>
            <div class="column-item">CPF</div>
            <div class="column-item">E-mail</div>
            <div class="column-item">Gênero</div>
            <div class="column-item">Data de nascimento</div>
            <div class="column-item">Cidade/UF</div>
            <div class="column-item">Tipo de formação</div>
            <div class="column-item">Instituição</div>
            <div class="column-item">Cota</div>
          </div>

          <div class="file-upload-area">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="17 8 12 3 7 8" />
              <line x1="12" y1="3" x2="12" y2="15" />
            </svg>
            <p>Clique para selecionar ou arraste o arquivo</p>
            <small>Formatos aceitos: .xlsx, .xls, .csv (máx. 10MB)</small>
          </div>

          <div class="info-box info-yellow">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3.05h16.94a2 2 0 0 0 1.71-3.05L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
            <div>
              <strong>Validação de conflitos</strong>
              <p>Após o envio, o sistema verificará automaticamente se algum CPF já está vinculado a outro programa vigente e sinalizará os conflitos.</p>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-outline" @click="selectedUpdateAction = null">Cancelar</button>
            <button type="button" class="btn-primary">Importar planilha</button>
          </div>
        </div>

        <!-- Import Aprovados -->
        <div v-else-if="selectedUpdateAction === 'import-aprovados'" class="modal-content import-aprovados-modal">
          <div class="modal-back">
            <button type="button" @click="selectedUpdateAction = null" class="back-link">← Voltar</button>
          </div>
          <h3>Importar planilha de aprovados</h3>
          <p class="modal-desc">Envie uma planilha com a lista final de candidatos aprovados. O sistema atualizará automaticamente o status de cada candidato.</p>

          <div class="columns-grid">
            <div class="columns-grid-title">Colunas necessárias:</div>
            <div class="column-item"><strong>CPF</strong></div>
            <div class="column-item"><strong>Status</strong> (Aprovado / Lista de espera)</div>
            <div class="column-item"><strong>Nome completo</strong></div>
          </div>

          <div class="file-upload-area">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <p>Clique para selecionar ou arraste o arquivo</p>
            <small>Formatos aceitos: .xlsx, .xls, .csv (máx. 10MB)</small>
          </div>

          <div class="info-box info-green">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <div>
              <strong>Atualização automática de status</strong>
              <p>Candidatos listados na planilha terão seu status atualizado para "Aprovado" ou "Lista de espera". Os demais candidatos serão marcados como "Não selecionado".</p>
            </div>
          </div>

          <div class="info-box info-yellow">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3.05h16.94a2 2 0 0 0 1.71-3.05L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
            <div>
              <strong>Verificação de conflitos</strong>
              <p>O sistema verificará se algum aprovado já está vinculado a outro programa vigente e sinalizará esses casos para análise manual.</p>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-outline" @click="selectedUpdateAction = null">Cancelar</button>
            <button type="button" class="btn-primary">Importar e atualizar</button>
          </div>
        </div>

        <!-- Lista de Espera -->
        <div v-else-if="selectedUpdateAction === 'lista-espera'" class="modal-content modal-large waitlist-modal">
          <div class="modal-back">
            <button type="button" @click="selectedUpdateAction = null" class="back-link">← Voltar</button>
          </div>
          <h3>Atualizar lista de espera</h3>

          <div class="waitlist-stats">
            <div class="stat-item">
              <span>Candidatos em espera</span>
              <strong>48</strong>
            </div>
            <div class="stat-item">
              <span>Vagas disponíveis</span>
              <strong class="teal-strong">250</strong>
            </div>
            <div class="stat-item">
              <span>Vagas preenchidas</span>
              <strong>250</strong>
            </div>
          </div>

          <p class="modal-desc">Gerencie convocações adicionais para preencher vagas disponíveis a partir da lista de espera.</p>

          <div class="waitlist-form-row waitlist-form-row--count">
            <label for="convokeCount">Quantidade de convocações</label>
            <input id="convokeCount" v-model="waitlistForm.convokeCount" type="number" class="field" placeholder="Digite a quantidade" />
            <small>Máximo: 48 candidatos disponíveis</small>
          </div>

          <div class="waitlist-form-row">
            <label for="convokeDate">Prazo para confirmação</label>
            <input id="convokeDate" v-model="waitlistForm.convokeDate" type="text" class="field" placeholder="dd/mm/aaaa" />
          </div>

          <div class="waitlist-form-row">
            <label for="convokeNotes">Observações (opcional)</label>
            <textarea id="convokeNotes" v-model="waitlistForm.notes" class="field" rows="3" placeholder="Adicione informações relevantes sobre esta convocação..." />
          </div>

          <div class="info-box info-blue">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <div>
              <strong>Convocação automática</strong>
              <p>Os candidatos convocados receberão notificação por e-mail e terão seu status atualizado para "Em análise" até a confirmação.</p>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-outline" @click="selectedUpdateAction = null">Cancelar</button>
            <button type="button" class="btn-primary">Convocar candidatos</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showEditStageModal" class="modal-overlay" @click="closeEditStageModal">
      <div class="modal" @click.stop>
        <h2>Editar Etapa</h2>
        <div class="form-row">
          <label for="editStageName">Nome da Etapa *</label>
          <input id="editStageName" v-model="editingStage.name" type="text" class="field" />
        </div>
        <div class="form-row">
          <label for="editStageDescription">Descrição</label>
          <textarea id="editStageDescription" v-model="editingStage.description" class="field" rows="3" />
        </div>
        <div class="modal-actions">
          <button class="btn-outline" @click="closeEditStageModal">Cancelar</button>
          <button class="btn-primary" :disabled="!editingStage.name || updatingStage" @click="updateStage">
            {{ updatingStage ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
        <div v-if="editStageError" class="state-error">{{ editStageError }}</div>
      </div>
    </div>

    <!-- Modal: Submit Courses Data -->
    <div v-if="showSubmitCoursesModal" class="modal-overlay" @click="showSubmitCoursesModal = false">
      <div class="modal modal-large" @click.stop>
        <div class="modal-header">
          <h2>Submeter dados dos cursos</h2>
          <button type="button" class="modal-close" @click="showSubmitCoursesModal = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="modal-content submit-cursos-modal">
          <p class="modal-desc">Envie a planilha com os dados de conclusão e notas dos cursos</p>

          <div class="columns-grid">
            <div class="columns-grid-title">A planilha deve conter as seguintes colunas:</div>
            <div class="column-item"><strong>CPF</strong></div>
            <div class="column-item"><strong>Nome</strong></div>
            <div class="column-item"><strong>Curso</strong></div>
            <div class="column-item"><strong>Percentual de conclusão</strong></div>
            <div class="column-item"><strong>Nota</strong></div>
            <div class="column-item"><strong>Status</strong></div>
            <div class="column-item"><strong>Data de atualização</strong></div>
          </div>

          <div class="file-upload-area">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="17 8 12 3 7 8" />
              <line x1="12" y1="3" x2="12" y2="15" />
            </svg>
            <p>Clique para selecionar ou arraste o arquivo</p>
            <small>Formatos aceitos: .xlsx, .xls, .csv (máx. 10MB)</small>
          </div>

          <div class="info-box info-blue">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <div>
              <strong>Após o envio, o sistema processará:</strong>
              <ul style="margin: 8px 0 0 0; padding-left: 20px;">
                <li>Atualização dos percentuais de conclusão</li>
                <li>Registro das notas obtidas</li>
                <li>Identificação de alunos com pendências em cursos obrigatórios</li>
                <li>Verificação de conflitos com outros programas vigentes</li>
              </ul>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-outline" @click="showSubmitCoursesModal = false">Cancelar</button>
            <button type="button" class="btn-primary">Submeter planilha</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Submit Test Grades -->
    <div v-if="showSubmitProvaNotasModal" class="modal-overlay" @click="showSubmitProvaNotasModal = false">
      <div class="modal modal-large" @click.stop>
        <div class="modal-header">
          <h2>Submeter notas da prova</h2>
          <button type="button" class="modal-close" @click="showSubmitProvaNotasModal = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="modal-content submit-prova-notas-modal">
          <p class="modal-desc">Envie a planilha com as notas finais e o desempenho por questão</p>

          <div class="columns-grid">
            <p class="columns-grid-title">A planilha deve conter as seguintes colunas:</p>
            <div class="column-item"><strong>Obrigatórias:</strong> CPF, Nome, Nota final, Tempo de conclusão</div>
            <div class="column-item"><strong>Questões:</strong> Q1, Q2, Q3... Q80 (respostas ou pontuação por questão)</div>
            <div class="column-item"><strong>Opcional:</strong> Área/Assunto (para análise por tema)</div>
          </div>

          <input
            type="file"
            ref="fileInputTestGrades"
            style="display: none"
            accept=".xlsx,.xls,.csv"
          />

          <div class="file-upload-area" @click="$refs.fileInputTestGrades?.click()">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="17 8 12 3 7 8" />
              <line x1="12" y1="3" x2="12" y2="15" />
            </svg>
            <p>Clique para selecionar ou arraste o arquivo</p>
            <small>Formatos aceitos: .xlsx, .xls, .csv (máx. 10MB)</small>
          </div>

          <div class="info-box">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <div>
              <strong>Após o envio, o sistema processará:</strong>
              <ul style="margin: 8px 0 0 0; padding-left: 20px;">
                <li>Atualização das notas finais dos alunos</li>
                <li>Cálculo do desempenho por questão</li>
                <li>Análise de pontos fortes e fracos</li>
                <li>Identificação de questões com baixo desempenho</li>
              </ul>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-outline" @click="showSubmitProvaNotasModal = false">Cancelar</button>
            <button type="button" class="btn-primary">Submeter notas</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Import Students from Imersão -->
    <div v-if="showImportImersaoModal" class="modal-overlay" @click="showImportImersaoModal = false">
      <div class="modal modal-large" @click.stop>
        <div class="modal-header">
          <h2>Importar alunos da imersão</h2>
          <button type="button" class="modal-close" @click="showImportImersaoModal = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="modal-content import-imersao-modal">
          <input
            type="file"
            ref="fileInputImportImersao"
            style="display: none"
            accept=".xlsx,.xls,.csv"
          />

          <div class="file-upload-area" @click="$refs.fileInputImportImersao?.click()">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 5 17 10" />
              <line x1="12" y1="5" x2="12" y2="17" />
            </svg>
            <p>Envie a planilha com os alunos da imersão</p>
            <small>Formatos aceitos: .xlsx, .xls, .csv</small>
          </div>

          <div class="info-box info-yellow">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3.05h16.94a2 2 0 0 0 1.71-3.05L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
            <div>
              <strong>Validação automática de conflitos</strong>
              <p>O sistema verificará se algum aluno já está vinculado a outro programa vigente.</p>
            </div>
          </div>
        </div>

        <div class="modal-actions-footer import-imersao-footer">
          <button type="button" class="btn-outline" @click="showImportImersaoModal = false">Cancelar</button>
          <button type="button" class="btn-primary">Importar</button>
        </div>
      </div>
    </div>

    <!-- Modal: Submit Imersão Grades -->
    <div v-if="showSubmitNotasImersaoModal" class="modal-overlay" @click="showSubmitNotasImersaoModal = false">
      <div class="modal modal-large" @click.stop>
        <div class="modal-header">
          <h2>Submeter notas</h2>
          <button type="button" class="modal-close" @click="showSubmitNotasImersaoModal = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="modal-content submit-imersao-notas-modal">
          <div class="submit-imersao-type-label">Tipo de avaliação</div>

          <div class="submit-imersao-type-tabs">
            <button
              type="button"
              class="submit-imersao-type-btn"
              :class="{ active: tipoAvaliacaoImersao === 'parcial' }"
              @click="tipoAvaliacaoImersao = 'parcial'"
            >
              Avaliação Parcial
            </button>
            <button
              type="button"
              class="submit-imersao-type-btn"
              :class="{ active: tipoAvaliacaoImersao === 'final' }"
              @click="tipoAvaliacaoImersao = 'final'"
            >
              Avaliação Final
            </button>
          </div>

          <input
            type="file"
            ref="fileInputSubmitNotasImersao"
            style="display: none"
            accept=".xlsx,.xls,.csv"
          />

          <div class="file-upload-area" @click="$refs.fileInputSubmitNotasImersao?.click()">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 5 17 10" />
              <line x1="12" y1="5" x2="12" y2="17" />
            </svg>
            <p>Envie a planilha de notas</p>
            <small>Formatos aceitos: .xlsx, .xls, .csv</small>
          </div>
        </div>

        <div class="modal-actions-footer submit-imersao-notas-footer">
          <button type="button" class="btn-outline" @click="showSubmitNotasImersaoModal = false">Cancelar</button>
          <button type="button" class="btn-primary">Submeter notas</button>
        </div>
      </div>
    </div>

    <!-- Modal: Update Attendance (Imersão) -->
    <div v-if="showAtualizarPresencaImersaoModal" class="modal-overlay" @click="showAtualizarPresencaImersaoModal = false">
      <div class="modal modal-large" @click.stop>
        <div class="modal-header">
          <h2>Atualizar presença</h2>
          <button type="button" class="modal-close" @click="showAtualizarPresencaImersaoModal = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="modal-content update-presenca-imersao-modal">
          <div class="presenca-filters-grid">
            <div class="presenca-field">
              <label for="presenca-grupo">Grupo</label>
              <select id="presenca-grupo" v-model="imersaoPresencaForm.groupId" class="field">
                <option v-for="group in imersaoPresencaGroups" :key="group.id" :value="group.id">{{ group.name }}</option>
              </select>
            </div>

            <div class="presenca-field">
              <label for="presenca-reuniao">Reunião semanal</label>
              <select id="presenca-reuniao" v-model="imersaoPresencaForm.meetingDate" class="field">
                <option v-for="meeting in presencaMeetingOptions" :key="meeting.value" :value="meeting.value">{{ meeting.label }}</option>
              </select>
            </div>
          </div>

          <div class="presenca-list-head">
            <strong>Alunos do grupo</strong>
            <span>Marque presença ou ausência na data selecionada.</span>
          </div>

          <div class="presenca-list">
            <div v-for="student in presencaRows" :key="student.id" class="presenca-row">
              <div class="presenca-student-name">{{ student.name }}</div>
              <div class="presenca-actions">
                <button
                  type="button"
                  class="presenca-action-btn"
                  :class="{ active: student.present }"
                  @click="setPresenceValue(student.id, true)"
                >
                  Presente
                </button>
                <button
                  type="button"
                  class="presenca-action-btn presenca-action-btn--danger"
                  :class="{ active: student.present === false }"
                  @click="setPresenceValue(student.id, false)"
                >
                  Ausente
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-actions-footer update-presenca-footer">
          <button type="button" class="btn-outline" @click="showAtualizarPresencaImersaoModal = false">Cancelar</button>
          <button type="button" class="btn-primary">Salvar presença</button>
        </div>
      </div>
    </div>

    <!-- Modal: Send Message -->
    <div v-if="showSendMessageModal" class="modal-overlay" @click="showSendMessageModal = false">
      <div class="modal modal-large" @click.stop>
        <div class="modal-header">
          <h2>Enviar mensagem aos alunos</h2>
          <button type="button" class="modal-close" @click="showSendMessageModal = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="modal-content send-message-modal">
          <p class="modal-desc">Comunique os alunos sobre pendências na conclusão dos cursos obrigatórios do nivelamento.</p>

          <div class="send-message-section">
            <label class="section-label">Destinatários</label>
            <div class="radio-option">
              <input type="radio" id="all-students" name="recipients" value="all" checked />
              <label for="all-students">Todos os alunos do nivelamento</label>
            </div>
            <div class="radio-option">
              <input type="radio" id="pending-students" name="recipients" value="pending" />
              <label for="pending-students">Apenas alunos com cursos obrigatórios pendentes</label>
            </div>

            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-label">Total selecionados:</span>
                <span class="stat-value">37</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Com pendências:</span>
                <span class="stat-value pending">37</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Sem pendências:</span>
                <span class="stat-value">0</span>
              </div>
            </div>
          </div>

          <div class="send-message-section">
            <label for="email-subject" class="section-label">Assunto do e-mail</label>
            <input
              id="email-subject"
              type="text"
              placeholder="Pendência na conclusão dos cursos obrigatórios"
              class="text-input"
            />
          </div>

          <div class="send-message-section">
            <label for="message-body" class="section-label">Mensagem</label>
            <textarea
              id="message-body"
              class="textarea-input"
              rows="6"
              placeholder="Olá, identificamos que você ainda possui pendências em um ou mais cursos obrigatórios da etapa de Nivelamento. A conclusão desses cursos é necessária para continuar no processo. Acesse a plataforma e regularize sua situação dentro do prazo."
            >Olá, identificamos que você ainda possui pendências em um ou mais cursos obrigatórios da etapa de Nivelamento. A conclusão desses cursos é necessária para continuar no processo. Acesse a plataforma e regularize sua situação dentro do prazo.</textarea>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-outline" @click="showSendMessageModal = false">Cancelar</button>
            <button type="button" class="btn-primary">Enviar mensagem</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showCourseDetailsModal && selectedCourseItem" class="modal-overlay" @click="closeCourseDetailsModal">
      <div class="modal modal-large course-details-modal" @click.stop>
        <div class="modal-header">
          <div>
            <h2>{{ selectedCourseItem?.name || 'Detalhes do curso' }}</h2>
            <div class="course-details-badges">
              <span v-if="selectedCourseItem?.required" class="course-badge course-badge-required">Obrigatório</span>
              <span v-if="selectedCourseItem?.knowledgeArea" class="course-badge">{{ selectedCourseItem.knowledgeArea }}</span>
            </div>
          </div>
          <button type="button" class="modal-close" @click="closeCourseDetailsModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="modal-content course-details-content">
          <div class="course-detail-stats">
            <div class="course-detail-stat">
              <div class="course-detail-label">Carga horária</div>
              <div class="course-detail-value">{{ selectedCourseItem?.workloadHours ? `${selectedCourseItem.workloadHours}h` : '--' }}</div>
            </div>
            <div class="course-detail-stat">
              <div class="course-detail-label">Média de conclusão</div>
              <div class="course-detail-value teal">{{ selectedCourseItem?.completionPct || 0 }}%</div>
            </div>
            <div class="course-detail-stat">
              <div class="course-detail-label">Concluídos</div>
              <div class="course-detail-value">{{ selectedCourseItem?.completedCount || 0 }}</div>
            </div>
            <div class="course-detail-stat">
              <div class="course-detail-label">Pendentes</div>
              <div class="course-detail-value amber">{{ selectedCourseItem?.pendingCount || 0 }}</div>
            </div>
          </div>

          <div class="alert-banner alert-warning">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3.05L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
            <div class="alert-copy">
              <strong>Alunos com pendência</strong>
              <p>{{ selectedCourseItem?.pendingCount || 0 }} alunos ainda não concluíram este curso</p>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-outline" @click="closeCourseDetailsModal">Fechar</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Create Group -->
    <GroupCreateModal 
      v-if="classId"
      :is-open="showGroupCreateModal"
      :class-id="classId"
      @close="showGroupCreateModal = false"
      @group-created="onGroupCreated"
    />
  </div>
</template>

<script>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { classService } from '@/services/classService';
import { enrollmentService } from '@/services/enrollmentService';
import { peopleService } from '@/services/peopleService';
import { stageService } from '@/services/stageService';
import { courseService } from '@/services/courseService';
import { groupService } from '@/services/groupService';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import GroupCreateModal from '@/components/GroupCreateModal.vue';

const cycle = ['Inscrição', 'Seleção', 'Nivelamento', 'Imersão', 'Encerrado'];
const overviewCycle = ['Inscrição', 'Processo Seletivo', 'Nivelamento', 'Prova', 'Imersão', 'Avaliação Final', 'Encerramento'];
const overviewStageCards = [
  {
    title: 'Processo Seletivo',
    status: 'Concluído',
    statusClass: 'pill-green',
    items: [
      { label: 'Inscritos', value: '612' },
      { label: 'Vagas nivelamento', value: '250' },
      { label: 'Pendências doc.', value: '22', valueClass: 'warning-strong' },
      { label: 'Conflitos', value: '3', valueClass: 'warning-strong' },
    ],
  },
  {
    title: 'Nivelamento',
    status: 'Concluído',
    statusClass: 'pill-green',
    items: [
      { label: 'Selecionados', value: '250' },
      { label: 'Ativos', value: '229' },
      { label: 'Cursos obrigatórios', value: '6' },
      { label: 'Prova final', value: '22/10/2025' },
    ],
  },
  {
    title: 'Imersão',
    status: 'Em andamento',
    statusClass: 'pill-teal',
    items: [
      { label: 'Aprovados', value: '50' },
      { label: 'Alunos ativos', value: '44' },
      { label: 'Grupos formados', value: '10' },
      { label: 'Avaliação Final', value: '30/11/2025' },
    ],
  },
];
const overviewTimeline = [
  { label: 'Período de Inscrição', date: '01/02 - 28/02', status: 'Concluído' },
  { label: 'Divulgação Resultado Preliminar', date: '05/03', status: 'Concluído' },
  { label: 'Período de Recursos', date: '06/03 - 10/03', status: 'Concluído' },
  { label: 'Resultado Final', date: '15/03', status: 'Concluído' },
  { label: 'Confirmação de Participação', date: '16/03 - 20/03', status: 'Concluído' },
  { label: 'Nivelamento', date: '21/03 - 30/04', status: 'Concluído' },
];
const quotaDistribution = [
  { label: 'Ampla Concorrência', value: 45, color: '#3b82f6' },
  { label: 'Mulheres', value: 25, color: '#a855f7' },
  { label: 'Negros/Pardos', value: 15, color: '#f59e0b' },
  { label: 'PCD/Neurodivergente', value: 10, color: '#14b8a6' },
  { label: '45+', value: 5, color: '#64748b' },
];
const genderDistribution = [
  { label: 'Feminino', value: 42 },
  { label: 'Masculino', value: 56 },
  { label: 'Outro', value: 1 },
  { label: 'Não inf.', value: 1 },
];
const cityDistribution = [
  { label: 'Maceió - AL', value: 32 },
  { label: 'Arapiraca - AL', value: 8 },
  { label: 'Rio Largo - AL', value: 5 },
  { label: 'Outros municípios', value: 5 },
];
const educationDistribution = [
  { label: 'Graduação em andamento', value: 28 },
  { label: 'Graduação concluída', value: 15 },
  { label: 'Curso técnico', value: 5 },
  { label: 'Outros cursos', value: 2 },
];
const overviewUpdates = [
  {
    action: 'Planilha de notas da prova importada',
    author: 'Ana Souza',
    date: '24/10/2025 14:32',
    status: 'Concluído',
    statusClass: 'pill-green',
    dotClass: 'dot-green',
  },
  {
    action: 'Dados dos cursos atualizados',
    author: 'Carlos Lima',
    date: '21/10/2025 09:18',
    status: 'Concluído',
    statusClass: 'pill-green',
    dotClass: 'dot-green',
  },
  {
    action: 'Lista de aprovados para imersão atualizada',
    author: 'Mariana Torres',
    date: '30/10/2025 16:05',
    status: 'Concluído',
    statusClass: 'pill-green',
    dotClass: 'dot-green',
  },
  {
    action: 'Contratos pendentes identificados',
    author: 'Sistema',
    date: '05/11/2025 10:44',
    status: 'Atenção',
    statusClass: 'pill-amber',
    dotClass: 'dot-amber',
  },
  {
    action: 'Notas da avaliação parcial aguardando envio',
    author: 'Sistema',
    date: '25/02/2026 08:20',
    status: 'Pendente',
    statusClass: 'pill-slate',
    dotClass: 'dot-slate',
  },
];
const overviewTopCards = {
  nivelamentoSelecionados: 250,
  nivelamentoAtivos: 229,
  imersaoAprovados: 50,
  imersaoGrupos: 10,
  alertasCriticos: 7,
  alertasRisco: 3,
};

const selectionQuotaLabels = [
  'Ampla Concorrência',
  'PCD/Neurodivergente',
  'Negro/Pardo',
  'Mulheres',
  '45+',
];

export default {
  name: 'ClassDetailsView',
  components: { ConfirmDialog, GroupCreateModal },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const classData = ref({});
    const stages = ref([]);
    const stageCandidatesCount = ref({});
    const classEnrollments = ref([]);
    const allEnrollments = ref([]);
    const allClasses = ref([]);
    const loading = ref(false);
    const peopleLoading = ref(false);
    const error = ref(null);
    const peopleError = ref(null);
    const activeTab = ref('visao-geral');
    const tabs = [
      { id: 'visao-geral', label: 'Visão Geral' },
      { id: 'pessoas', label: 'Pessoas' },
      { id: 'processo-seletivo', label: 'Processo Seletivo' },
      { id: 'etapas', label: 'Etapas' },
    ];

    const showCreateStageModal = ref(false);
    const creatingStage = ref(false);
    const stageError = ref(null);
    const showEditStageModal = ref(false);
    const updatingStage = ref(false);
    const editStageError = ref(null);
    const confirmDialog = ref();
    const newStage = ref({ name: '', description: '' });
    const editingStage = ref({ id: null, name: '', description: '' });
    const showUpdateSelectionModal = ref(false);
    const showSubmitCoursesModal = ref(false);
    const showSubmitProvaNotasModal = ref(false);
    const showImportImersaoModal = ref(false);
    const showSubmitNotasImersaoModal = ref(false);
    const showAtualizarPresencaImersaoModal = ref(false);
    const tipoAvaliacaoImersao = ref('parcial');
    const showSendMessageModal = ref(false);
    const showCourseDetailsModal = ref(false);
    const selectedCourseItem = ref(null);
    const showGroupCreateModal = ref(false);
    const selectedUpdateAction = ref(null);
    const registeringCandidate = ref(false);
    const registrationError = ref('');
    const newCandidateForm = ref({
      name: '',
      cpf: '',
      email: '',
      gender: '',
      birthDate: '',
      city: '',
      education: '',
      institution: '',
      quota: '',
      status: '',
    });
    const waitlistForm = ref({
      convokeCount: '',
      convokeDate: '',
      notes: '',
    });
    const imersaoMetricsCards = ref([
      { label: 'Total de grupos', value: '10', valueClass: '' },
      { label: 'Total de alunos', value: '50', valueClass: 'teal-strong' },
      { label: 'Alunos ativos', value: '44', valueClass: 'teal-strong' },
      { label: 'Nota de corte parcial', value: '3,75', valueClass: 'warning-strong' },
      { label: 'Nota de corte final', value: '75% da maior', valueClass: 'warning-strong' },
      { label: 'Alunos em risco', value: '3', valueClass: 'danger-strong' },
    ]);
    const imersaoGroups = ref([
      {
        id: 1,
        name: 'Grupo 03',
        status: 'OK',
        statusClass: 'is-ok',
        mentor: 'Prof. João Silva',
        project: 'Plataforma de Gestão Acadêmica',
        partnerCompany: 'BRISA',
        lastGradesUpdate: '25/04/2026',
        lastMeetingDate: '24/04/2026',
        students: 5,
        partialAverage: '7.8',
        finalAverage: '8.4',
        studentsDetails: [
          { id: 'g03-1', name: 'João Silva', partial: '4.2', final: '4.5', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
          { id: 'g03-2', name: 'Maria Santos', partial: '3.8', final: '4.1', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
          { id: 'g03-3', name: 'Carlos Oliveira', partial: '4.5', final: '4.7', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
          { id: 'g03-4', name: 'Ana Costa', partial: '4.0', final: '4.3', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
          { id: 'g03-5', name: 'Pedro Lima', partial: '3.5', final: '3.8', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: false },
        ],
      },
      {
        id: 2,
        name: 'Grupo 07',
        status: 'OK',
        statusClass: 'is-ok',
        mentor: 'Prof. Maria Santos',
        project: 'Sistema de Controle de Estoque',
        partnerCompany: 'BRISA',
        lastGradesUpdate: '25/04/2026',
        lastMeetingDate: '24/04/2026',
        students: 4,
        partialAverage: '8.2',
        finalAverage: '8.9',
        studentsDetails: [
          { id: 'g07-1', name: 'Fernanda Lima', partial: '4.4', final: '4.8', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
          { id: 'g07-2', name: 'Gustavo Rocha', partial: '4.1', final: '4.6', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
          { id: 'g07-3', name: 'Helena Costa', partial: '3.9', final: '4.4', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
          { id: 'g07-4', name: 'Igor Nascimento', partial: '4.0', final: '4.5', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
        ],
      },
      {
        id: 3,
        name: 'Grupo 12',
        status: 'Atenção',
        statusClass: 'is-warning',
        mentor: 'Prof. Carlos Oliveira',
        project: 'App de Mobilidade Urbana',
        partnerCompany: 'BRISA',
        lastGradesUpdate: '25/04/2026',
        lastMeetingDate: '24/04/2026',
        students: 5,
        partialAverage: '6.5',
        finalAverage: '7.2',
        studentsDetails: [
          { id: 'g12-1', name: 'João Victor Melo', partial: '3.6', final: '3.9', situation: 'Atenção', situationClass: 'status-warning', attendedLastMeeting: false },
          { id: 'g12-2', name: 'Karina Souza', partial: '3.8', final: '4.0', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
          { id: 'g12-3', name: 'Lucas Monteiro', partial: '4.0', final: '4.1', situation: 'Regular', situationClass: 'status-regular', attendedLastMeeting: true },
          { id: 'g12-4', name: 'Mariana Lopes', partial: '3.7', final: '3.9', situation: 'Atenção', situationClass: 'status-warning', attendedLastMeeting: false },
          { id: 'g12-5', name: 'Nicolas Barros', partial: '3.5', final: '3.8', situation: 'Atenção', situationClass: 'status-warning', attendedLastMeeting: true },
        ],
      },
    ]);
    const imersaoExpandedGroupId = ref(1);
    const imersaoGroupTabs = ref({
      1: 'resumo',
      2: 'resumo',
      3: 'resumo',
    });
    const toggleImersaoGroup = (groupId) => {
      imersaoExpandedGroupId.value = imersaoExpandedGroupId.value === groupId ? null : groupId;
    };
    const setImersaoGroupTab = (groupId, tabId) => {
      imersaoGroupTabs.value[groupId] = tabId;
    };
    const getImersaoGroupTab = (groupId) => imersaoGroupTabs.value[groupId] || 'resumo';
    const imersaoPresencaGroups = ref([
      {
        id: 'grupo-03',
        name: 'Grupo 03',
        meetings: [
          { value: '2026-03-03', label: '03/03/2026' },
          { value: '2026-03-10', label: '10/03/2026' },
          { value: '2026-03-17', label: '17/03/2026' },
        ],
        students: [
          { id: 'g03-s1', name: 'Ana Beatriz Santos' },
          { id: 'g03-s2', name: 'Bruno Almeida' },
          { id: 'g03-s3', name: 'Camila Ferreira' },
          { id: 'g03-s4', name: 'Diego Araújo' },
          { id: 'g03-s5', name: 'Ester Oliveira' },
        ],
      },
      {
        id: 'grupo-07',
        name: 'Grupo 07',
        meetings: [
          { value: '2026-03-04', label: '04/03/2026' },
          { value: '2026-03-11', label: '11/03/2026' },
          { value: '2026-03-18', label: '18/03/2026' },
        ],
        students: [
          { id: 'g07-s1', name: 'Fernanda Lima' },
          { id: 'g07-s2', name: 'Gustavo Rocha' },
          { id: 'g07-s3', name: 'Helena Costa' },
          { id: 'g07-s4', name: 'Igor Nascimento' },
        ],
      },
      {
        id: 'grupo-12',
        name: 'Grupo 12',
        meetings: [
          { value: '2026-03-05', label: '05/03/2026' },
          { value: '2026-03-12', label: '12/03/2026' },
          { value: '2026-03-19', label: '19/03/2026' },
        ],
        students: [
          { id: 'g12-s1', name: 'João Victor Melo' },
          { id: 'g12-s2', name: 'Karina Souza' },
          { id: 'g12-s3', name: 'Lucas Monteiro' },
          { id: 'g12-s4', name: 'Mariana Lopes' },
          { id: 'g12-s5', name: 'Nicolas Barros' },
        ],
      },
    ]);
    const imersaoPresencaForm = ref({
      groupId: 'grupo-03',
      meetingDate: '2026-03-03',
    });
    const presencaDraft = ref({});
    const selectedPresencaGroup = computed(() => imersaoPresencaGroups.value.find((group) => group.id === imersaoPresencaForm.value.groupId) || null);
    const presencaMeetingOptions = computed(() => selectedPresencaGroup.value?.meetings || []);
    const getPresenceValue = (groupId, meetingDate, studentId) => {
      const value = presencaDraft.value?.[groupId]?.[meetingDate]?.[studentId];
      return typeof value === 'boolean' ? value : true;
    };
    const setPresenceValue = (studentId, present) => {
      const { groupId, meetingDate } = imersaoPresencaForm.value;
      if (!groupId || !meetingDate) return;
      if (!presencaDraft.value[groupId]) presencaDraft.value[groupId] = {};
      if (!presencaDraft.value[groupId][meetingDate]) presencaDraft.value[groupId][meetingDate] = {};
      presencaDraft.value[groupId][meetingDate][studentId] = present;
    };
    const presencaRows = computed(() => {
      const group = selectedPresencaGroup.value;
      if (!group) return [];
      const meetingDate = imersaoPresencaForm.value.meetingDate;
      return group.students.map((student) => ({
        ...student,
        present: getPresenceValue(group.id, meetingDate, student.id),
      }));
    });

    const programId = computed(() => route.params.programId);
    const classId = computed(() => route.params.classId);
    const peopleSearch = ref('');
    const peopleFilterStage = ref('');
    const peopleFilterStatus = ref('');
    const peopleFilterQuota = ref('');
    const peopleFilterCity = ref('');
    const peopleFilterGender = ref('');
    const classPeoplePage = ref(1);
    const classPeoplePageSize = 5;
    const selectionProcessPage = ref(1);
    const selectionProcessPageSize = 6;
    const selectionConflictOnly = ref(false);

    const normalizeText = (value) =>
      String(value ?? '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase();

    const formatEducation = (value) => {
      const raw = String(value ?? '').trim();
      if (!raw) return '-';
      return raw;
    };

    const normalizeQuotaLabel = (value) => {
      const raw = String(value ?? '').trim();
      const normalized = normalizeText(raw);
      if (!normalized || raw === '-') return '-';
      if (normalized.includes('ampla')) return 'Ampla Concorrência';
      if (normalized.includes('pcd') || normalized.includes('neuro')) return 'PCD/Neurodivergente';
      if (normalized.includes('negro') || normalized.includes('pardo')) return 'Negro/Pardo';
      if (normalized.includes('mulher')) return 'Mulheres';
      if (normalized.includes('45')) return '45+';
      return raw;
    };

    const parseDateValue = (value) => {
      if (!value) return null;
      const date = new Date(value);
      return Number.isNaN(date.getTime()) ? null : date;
    };

    const firstAvailableDate = (...values) => values.map(parseDateValue).find(Boolean) || null;

    const getClassDateRange = (classModel) => ({
      start: firstAvailableDate(
        classModel?.applicationStartDate,
        classModel?.startDate,
        classModel?.levelingStartDate,
        classModel?.immersionStartDate
      ),
      end: firstAvailableDate(
        classModel?.certificateIssueDate,
        classModel?.endDate,
        classModel?.finalEvaluationDate,
        classModel?.immersionEndDate,
        classModel?.levelingEndDate,
        classModel?.applicationEndDate
      ),
    });

    const rangesOverlap = (a, b) => {
      if (!a?.start || !a?.end || !b?.start || !b?.end) return false;
      return a.end.getTime() >= b.start.getTime() && b.end.getTime() >= a.start.getTime();
    };

    const isConflictRelevantStatus = (status) => {
      const normalized = normalizeText(status);
      if (!normalized) return true;
      return !normalized.includes('cancel') && !normalized.includes('inativ') && !normalized.includes('deslig') && !normalized.includes('nao-realiz');
    };

    const mapCycle = (value) => {
      const normalized = normalizeText(value);
      if (normalized.includes('inscri')) return 'Inscrição';
      if (normalized.includes('sele')) return 'Seleção';
      if (normalized.includes('nivel')) return 'Nivelamento';
      if (normalized.includes('imers')) return 'Imersão';
      if (normalized.includes('encer')) return 'Encerrado';
      return '';
    };

    const classStatusLabel = computed(() => {
      const value = normalizeText(classData.value?.status);
      if (value.includes('encerr')) return 'Encerrado';
      if (value.includes('inscri') || value.includes('espera')) return 'Inscrição aberta';
      if (value.includes('andamento') || value.includes('ativo') || value.includes('active')) return 'Em andamento';
      return classData.value?.status || 'Sem status';
    });

    const statusBadgeClass = computed(() => {
      const value = normalizeText(classData.value?.status);
      if (value.includes('encerr')) return 'badge-slate';
      if (value.includes('inscri') || value.includes('espera')) return 'badge-blue';
      return 'badge-green';
    });

    const currentStageIndex = computed(() => {
      const fromClass = mapCycle(classData.value?.currentStage || classData.value?.status);
      if (fromClass) return cycle.indexOf(fromClass);

      let maxIndex = 0;
      stages.value.forEach((stage) => {
        const index = cycle.indexOf(mapCycle(stage?.name || ''));
        if (index > maxIndex) maxIndex = index;
      });
      return maxIndex;
    });

    const currentStageLabel = computed(() => cycle[currentStageIndex.value] || 'Inscrição');
    const nextStageLabel = computed(() => cycle[currentStageIndex.value + 1] || 'Ciclo concluído');
    const progressPct = computed(() => Math.round((currentStageIndex.value / (cycle.length - 1)) * 100));
    const totalCandidates = computed(() => Object.values(stageCandidatesCount.value || {}).reduce((sum, item) => sum + Number(item || 0), 0));
    const classModelLabel = computed(() => classData.value?.model || classData.value?.modality || 'Híbrido');
    const classWorkloadLabel = computed(() => {
      const workload = classData.value?.workload || classData.value?.totalHours;
      return workload ? `${workload}h` : '480h';
    });
    const classPeriodLabel = computed(() => {
      if (!classData.value?.startDate || !classData.value?.endDate) return 'Ago/2025 → Jun/2026';
      const months = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
      const start = new Date(classData.value.startDate);
      const end = new Date(classData.value.endDate);
      return `${months[start.getMonth()]}/${start.getFullYear()} → ${months[end.getMonth()]}/${end.getFullYear()}`;
    });
    const partnerLabel = computed(
      () =>
        classData.value?.partnerInstitution?.name ||
        classData.value?.program?.partnerInstitution?.name ||
        classData.value?.program?.partnerName ||
        classData.value?.program?.institutionName ||
        'EASY/UFAL'
    );
    const currentStagePeriod = computed(() => classData.value?.stagePeriod || '17/11/2025 a 18/05/2026');
    const currentStageMilestone = computed(() => classData.value?.nextMilestone || 'Avaliação parcial');
    const currentStageMilestoneDate = computed(() => classData.value?.nextMilestoneDate || '03/03/2026');
    const overviewCurrentCycleIndex = computed(() => {
      const normalized = normalizeText(classData.value?.currentStage || classData.value?.status || '');
      if (normalized.includes('imersa')) return 4;
      if (normalized.includes('nivel')) return 2;
      if (normalized.includes('sele')) return 1;
      if (normalized.includes('prova')) return 3;
      if (normalized.includes('encer')) return 6;
      if (normalized.includes('avali')) return 5;
      return 0;
    });
    const overviewProgressPct = computed(() => Math.round((overviewCurrentCycleIndex.value / (overviewCycle.length - 1)) * 100));

    const stageBucket = computed(() => {
      const summary = { selecao: 0, nivelamento: 0, imersao: 0 };
      stages.value.forEach((stage) => {
        const value = normalizeText(stage?.name || '');
        if (value.includes('sele')) summary.selecao += 1;
        if (value.includes('nivel')) summary.nivelamento += 1;
        if (value.includes('imers')) summary.imersao += 1;
      });
      return summary;
    });

    const formatCPF = (cpf) => {
      if (!cpf) return '-';
      const digits = String(cpf).replace(/\D/g, '');
      if (digits.length !== 11) return cpf;
      return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    };

    const formatGender = (value) => {
      const raw = String(value || '').trim();
      const normalized = normalizeText(raw);
      if (!normalized) return '-';
      if (['m', 'male', 'masculino'].includes(normalized)) return 'Masculino';
      if (['f', 'female', 'feminino'].includes(normalized)) return 'Feminino';
      if (['outro', 'other'].includes(normalized)) return 'Outro';
      return raw.charAt(0).toUpperCase() + raw.slice(1).toLowerCase();
    };

    const formatAge = (birthDate) => {
      if (!birthDate) return '-';
      const date = new Date(birthDate);
      if (Number.isNaN(date.getTime())) return '-';
      const today = new Date();
      let age = today.getFullYear() - date.getFullYear();
      const monthDiff = today.getMonth() - date.getMonth();
      if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < date.getDate())) age -= 1;
      return age >= 0 ? String(age) : '-';
    };

    const formatCity = (person) => {
      const city = person?.city || person?.cidade || '';
      const state = person?.state || person?.estado || '';
      const parts = [city, state].filter((item) => String(item || '').trim());
      if (!parts.length) return '-';
      if (parts.length === 2) return `${parts[0]} - ${parts[1]}`;
      return parts[0];
    };

    const normalizeEnrollmentStatus = (status) => {
      const normalized = normalizeText(status);
      if (!normalized) return { label: 'Ativa', className: 'status-active' };
      if (normalized.includes('cancel') || normalized.includes('inativ') || normalized.includes('deslig') || normalized.includes('nao-realiz')) {
        return { label: 'Inativa', className: 'status-inactive' };
      }
      if (normalized.includes('pend')) return { label: 'Pendente', className: 'status-pending' };
      if (normalized.includes('conclu') || normalized.includes('realizado') || normalized.includes('finaliz')) {
        return { label: 'Concluída', className: 'status-done' };
      }
      return { label: 'Ativa', className: 'status-active' };
    };

    const classPeopleRows = computed(() => {
      const byPersonId = new Map();
      for (const enrollment of classEnrollments.value || []) {
        const person = enrollment?.people || {};
        if (!person?.id) continue;
        const existing = byPersonId.get(person.id);
        const currentTime = new Date(enrollment?.updatedAt || enrollment?.enrollmentDate || enrollment?.createdAt || 0).getTime();
        const existingTime = existing
          ? new Date(existing.enrollment?.updatedAt || existing.enrollment?.enrollmentDate || existing.enrollment?.createdAt || 0).getTime()
          : -1;
        if (!existing || currentTime > existingTime) byPersonId.set(person.id, { person, enrollment });
      }

      return Array.from(byPersonId.values())
        .map(({ person, enrollment }) => {
          const status = normalizeEnrollmentStatus(enrollment?.status);
          const quota = normalizeQuotaLabel(person?.quotaCategory || person?.cota || person?.quota || '-');
          return {
            id: person.id,
            name: person?.name || '-',
            cpf: person?.cpf || '',
            email: person?.email || '-',
            gender: formatGender(person?.gender),
            age: formatAge(person?.birthDate),
            quota: quota || '-',
            city: formatCity(person),
            educationLevel: person?.educationLevel || person?.education || '-',
            stage: currentStageLabel.value || '-',
            status: status.label,
            statusClass: status.className,
          };
        })
        .sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'));
    });

    const classDateRange = computed(() => ({
      start: firstAvailableDate(
        classData.value?.applicationStartDate,
        classData.value?.startDate,
        classData.value?.levelingStartDate,
        classData.value?.immersionStartDate
      ),
      end: firstAvailableDate(
        classData.value?.certificateIssueDate,
        classData.value?.endDate,
        classData.value?.finalEvaluationDate,
        classData.value?.immersionEndDate,
        classData.value?.levelingEndDate,
        classData.value?.applicationEndDate
      ),
    }));

    const allClassesById = computed(() =>
      new Map((allClasses.value || []).map((item) => [Number(item.id), item]))
    );

    const selectionConflictPeopleIds = computed(() => {
      const conflicts = new Set();
      const currentClassId = Number(classId.value);
      const currentRange = classDateRange.value;

      if (!currentRange.start || !currentRange.end) {
        return conflicts;
      }

      for (const row of classPeopleRows.value || []) {
        const enrollments = (allEnrollments.value || []).filter((enrollment) => {
          const personId = Number(enrollment?.people?.id);
          const otherClassId = Number(enrollment?.classModel?.id);
          return personId === Number(row.id) && otherClassId !== currentClassId;
        });

        const hasConflict = enrollments.some((enrollment) => {
          if (!isConflictRelevantStatus(enrollment?.status)) {
            return false;
          }

          const otherClass = allClassesById.value.get(Number(enrollment?.classModel?.id));
          const otherRange = getClassDateRange(otherClass);
          return rangesOverlap(currentRange, otherRange);
        });

        if (hasConflict) {
          conflicts.add(row.id);
        }
      }

      return conflicts;
    });

    const selectionProcessBaseRows = computed(() => {
      const totalRows = classPeopleRows.value.length;
      const conflictCount = selectionConflictPeopleIds.value.size;
      const eligibleRows = Math.max(totalRows - conflictCount, 0);
      const approvedLimit = Math.min(Number(classData.value?.defaultLevelingCapacity ?? 250), eligibleRows);
      const waitlistLimit = Math.min(Math.max(Math.round(totalRows * 0.08), 0), Math.max(eligibleRows - approvedLimit, 0));
      const inscriptionLimit = Math.min(Math.max(Math.round(totalRows * 0.12), 0), Math.max(eligibleRows - approvedLimit - waitlistLimit, 0));

      let nonConflictIndex = 0;

      return classPeopleRows.value.map((row) => {
        const conflict = selectionConflictPeopleIds.value.has(row.id);
        let status = 'Não selecionado';
        let statusClass = 'status-inactive';

        if (conflict) {
          status = 'Conflito em outro programa';
          statusClass = 'status-inactive';
        } else if (nonConflictIndex < approvedLimit) {
          status = 'Aprovado';
          statusClass = 'status-active';
        } else if (nonConflictIndex < approvedLimit + waitlistLimit) {
          status = 'Lista de espera';
          statusClass = 'status-pending';
        } else if (nonConflictIndex < approvedLimit + waitlistLimit + inscriptionLimit) {
          status = 'Inscrito';
          statusClass = 'status-pending';
        }

        if (!conflict) {
          nonConflictIndex += 1;
        }

        return {
          ...row,
          education: formatEducation(row.educationLevel),
          conflict,
          status,
          statusClass,
        };
      });
    });

    const selectionProcessRows = computed(() => {
      if (!selectionConflictOnly.value) {
        return selectionProcessBaseRows.value;
      }
      return selectionProcessBaseRows.value.filter((row) => row.conflict);
    });

    const selectionQuotaCards = computed(() => {
      const rows = selectionProcessBaseRows.value;
      const counts = selectionQuotaLabels.reduce((acc, label) => {
        acc[label] = 0;
        return acc;
      }, {});

      rows.forEach((row) => {
        const label = normalizeQuotaLabel(row.quota);
        if (Object.prototype.hasOwnProperty.call(counts, label)) {
          counts[label] += 1;
        }
      });

      return selectionQuotaLabels.map((label) => ({
        label,
        value: counts[label] || 0,
      }));
    });

    const selectionConflictCount = computed(() => selectionProcessBaseRows.value.filter((row) => row.conflict).length);
    const selectionWaitlistCount = computed(() => selectionProcessBaseRows.value.filter((row) => row.status === 'Lista de espera').length);
    const selectionProcessTotalPages = computed(() => {
      const total = Math.ceil(selectionProcessRows.value.length / selectionProcessPageSize);
      return Math.max(total, 1);
    });
    const paginatedSelectionProcessRows = computed(() => {
      const start = (selectionProcessPage.value - 1) * selectionProcessPageSize;
      return selectionProcessRows.value.slice(start, start + selectionProcessPageSize);
    });
    const selectionProcessPageStart = computed(() => {
      if (!selectionProcessRows.value.length) return 0;
      return (selectionProcessPage.value - 1) * selectionProcessPageSize + 1;
    });
    const selectionProcessPageEnd = computed(() =>
      Math.min(selectionProcessPage.value * selectionProcessPageSize, selectionProcessRows.value.length)
    );
    const selectionProcessVisiblePages = computed(() => {
      const total = selectionProcessTotalPages.value;
      const pages = [];
      const start = Math.max(1, selectionProcessPage.value - 1);
      const end = Math.min(total, start + 2);
      for (let i = start; i <= end; i += 1) pages.push(i);
      if (pages.length < 3) {
        const first = Math.max(1, end - 2);
        pages.length = 0;
        for (let i = first; i <= end; i += 1) pages.push(i);
      }
      return pages;
    });
    const selectionProcessMetricsCards = computed(() => [
      { label: 'Total de inscritos', value: selectionProcessBaseRows.value.length, valueClass: '' },
      {
        label: 'Vagas para nivelamento',
        value: Number(classData.value?.defaultLevelingCapacity ?? 250),
        valueClass: 'teal-strong',
      },
      {
        label: 'Data final de inscrição',
        value: formatDate(classData.value?.applicationEndDate || classData.value?.endDate),
        valueClass: '',
      },
      {
        label: 'Lista de espera',
        value: selectionWaitlistCount.value,
        valueClass: 'warning-strong',
      },
    ]);
    const selectionConflictSummary = computed(() => {
      const count = selectionConflictCount.value;
      return `${count} ${count === 1 ? 'aluno encontrado' : 'alunos encontrados'} em outro programa vigente`;
    });
    const selectionConflictButtonLabel = computed(() => (selectionConflictOnly.value ? 'Ver todos →' : 'Ver conflitos →'));
    const selectionProcessPageSummary = computed(() => {
      const total = selectionProcessRows.value.length;
      if (!total) return 'Nenhum inscrito encontrado';
      return `Mostrando ${selectionProcessPageStart.value}-${selectionProcessPageEnd.value} de ${total} inscritos`;
    });

    const openUpdateSelectionModal = () => {
      selectedUpdateAction.value = null;
      showUpdateSelectionModal.value = true;
    };

    // ── Nivelamento : carregar cursos, progressões e estatísticas ─────────────
    const courses = ref([]);
    const progressions = ref([]);
    const assignments = ref([]);
    const loadingNivelamento = ref(false);

    const loadNivelamentoData = async () => {
      loadingNivelamento.value = true;
      try {
        const asgs = await courseService.getAssignmentsByClassId(classId.value).catch(() => []);
        const progs = await courseService.getProgressionsByClassId(classId.value).catch(() => []);
        const all = await courseService.getAll().catch(() => []);
        assignments.value = Array.isArray(asgs) ? asgs : [];
        progressions.value = Array.isArray(progs) ? progs : [];

        const assignedIds = Array.from(new Set(assignments.value.map(a => a.course?.id).filter(Boolean)));
        if (assignedIds.length > 0) {
          courses.value = Array.isArray(all) ? all.filter(c => assignedIds.includes(c.id)) : [];
        } else {
          courses.value = [];
        }
      } catch (err) {
        console.error('Erro ao carregar nivelamento:', err);
      } finally {
        loadingNivelamento.value = false;
      }
    };

    const courseItems = computed(() => {
      if (!courses.value.length) return [];
      return courses.value.map(course => {
        const courseProgressions = progressions.value.filter(p => p.course?.id === course.id);
        const total = courseProgressions.length || 1;
        const notStarted = courseProgressions.filter(p => (String(p.status || '').toLowerCase()).includes('não iniciado') || p.status === 'não iniciado').length;
        const inProgress = courseProgressions.filter(p => (String(p.status || '').toLowerCase()).includes('em andamento') || p.status === 'em andamento').length;
        const completed = courseProgressions.filter(p => (String(p.status || '').toLowerCase()).includes('concluído') || p.status === 'concluído').length;
        const pending = Math.max(total - completed, 0);
        const workloadHours = Number(course.workloadHours || course.workload || course.hourLoad || course.cargaHoraria || 0);
        const avgCompletion = courseProgressions.length ? Math.round((courseProgressions.reduce((acc, p) => acc + Number(p.completionPercentage || p.completionPct || p.completion || 0), 0)) / total) : 0; // eslint-disable-line
        return {
          id: course.id,
          name: course.name,
          knowledgeArea: course.knowledgeArea?.name,
          required: assignments.value.find(a => a.course?.id === course.id)?.required !== false,
          completionPct: avgCompletion,
          pctNotStarted: Math.round((notStarted / total) * 100),
          pctInProgress: Math.round((inProgress / total) * 100),
          pctCompleted: Math.round((completed / total) * 100),
          completedCount: completed,
          pendingCount: pending,
          workloadHours,
          assigned: assignedIdsIncludes(assignments.value, course.id),
        };
      });
    });

    function assignedIdsIncludes(list, id) {
      try { return list.some(x => x.course?.id === id); } catch (e) { return false; }
    }

    const courseStats = computed(() => ({
      total: courseItems.value.length,
      notStarted: courseItems.value.filter(c => c.completionPct === 0).length,
      inProgress: courseItems.value.filter(c => c.completionPct > 0 && c.completionPct < 100).length,
      completed: courseItems.value.filter(c => c.completionPct === 100).length,
    }));

    const getCompletionColor = (pct) => {
      if (pct >= 80) return '#27ae60';
      if (pct >= 40) return '#f39c12';
      return '#e74c3c';
    };

    const getNivelamentoStudents = computed(() => {
      const map = {};
      (progressions.value || []).forEach(p => {
        const pid = p.people?.id || p.peopleId || p.personId || p.person?.id || null;
        if (!pid) return;
        if (!map[pid]) {
          map[pid] = {
            id: pid,
            name: p.people?.name || p.person?.name || p.personName || '',
            cpf: p.people?.cpf || p.person?.cpf || '' ,
            total: 0,
            sum: 0
          };
        }
        const pct = Number(p.completionPercentage ?? p.completionPct ?? p.completion ?? 0);
        map[pid].total += 1;
        map[pid].sum += isNaN(pct) ? 0 : pct;
      });
      return Object.values(map).map(s => ({
        id: s.id,
        name: s.name,
        cpf: s.cpf,
        avg: s.total ? Math.round(s.sum / s.total) : 0
      }));
    });

        // Helpers: assign/remove/goTo for courses
        const assignCourse = async (course) => {
          try {
            await courseService.assignCourseToClass(course.id, classId.value, true);
            await loadNivelamentoData();
          } catch (err) {
            alert(`Erro ao adicionar curso: ${err.response?.data?.message || err.message}`);
          }
        };

        const removeCourse = async (course) => {
          const confirmed = await confirmDialog.value.show(`Remover o curso "${course.name}" da turma?`, 'Remover');
          if (!confirmed) return;
          try {
            await courseService.removeCourseFromClass(course.id, classId.value);
            await loadNivelamentoData();
          } catch (err) {
            alert(`Erro ao remover curso: ${err.response?.data?.message || err.message}`);
          }
        };

        const goToCourse = (course) => {
          router.push({ name: 'ClassCourses', params: { programId: programId.value, classId: classId.value }, query: { courseId: course.id } });
        };
        const openCourseDetails = (course) => {
          selectedCourseItem.value = course;
          showCourseDetailsModal.value = true;
        };
        const closeCourseDetailsModal = () => {
          showCourseDetailsModal.value = false;
          selectedCourseItem.value = null;
        };

        // Load nivelamento data when Etapas tab is opened
        const etapasSubTab = ref('nivelamento');
        // When false, keep frontend mocks for imersaoGroups instead of replacing them with API results
        const useRealImersaoGroups = ref(false);
        const lastEmailInfo = computed(() => {
          const info = classData.value?.lastEmailSent;
          if (info && info.date) {
            const d = new Date(info.date);
            return `${d.toLocaleDateString('pt-BR')} às ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} por ${info.author || '—'} → ${info.count || 0} alunos com pendências`;
          }
          return 'Nenhum e-mail enviado recentemente';
        });

        watch(() => activeTab.value, (tab) => {
          if (tab === 'etapas') loadNivelamentoData();
        });

        // Load imersao groups when sub-tab switches to 'imersao'
        const loadImersaoGroups = async () => {
          try {
            const res = await groupService.getGroupsByClass(classId.value);
            const groups = (res && res.data && (Array.isArray(res.data) ? res.data : res.data.groups)) || res.data || res || [];
            imersaoGroups.value = (groups || []).map((g) => ({
              id: g.id,
              name: g.projectTheme || g.name || '-',
              status: g.status || 'Ativo',
              statusClass: (g.status === 'Atenção') ? 'is-warning' : 'is-ok',
              mentor: g.leaderName || g.leader || '-',
              project: g.projectTheme || g.project || '-',
              partnerCompany: g.projectCompanyName || g.projectCompany || '',
              lastGradesUpdate: g.lastGradesUpdate || '-',
              lastMeetingDate: g.lastMeetingDate || '-',
              students: g.memberCount ?? (g.members?.length ?? 0),
              partialAverage: g.partialAverage ?? '-',
              finalAverage: g.finalAverage ?? '-',
              studentsDetails: (g.members || g.studentDetails || []).map((m) => ({
                id: m.id,
                name: m.name,
                partial: m.partial ?? '-',
                final: m.final ?? '-',
                situation: m.situation || 'Regular',
                situationClass: (m.situation === 'Atenção') ? 'status-warning' : 'status-regular',
                attendedLastMeeting: !!m.attendedLastMeeting,
              })),
            }));
            // update metrics
            imersaoMetricsCards.value[0].value = String(imersaoGroups.value.length || 0);
            imersaoMetricsCards.value[1].value = String(imersaoGroups.value.reduce((s, g) => s + (g.students || 0), 0));
          } catch (err) {
            console.error('Erro ao carregar grupos:', err);
          }
        };

        watch(() => etapasSubTab.value, (tab) => {
          if (tab === 'imersao' && useRealImersaoGroups.value) loadImersaoGroups();
        });

        // Manual trigger to refresh groups from API when needed
        const refreshImersaoGroupsFromApi = async () => {
          useRealImersaoGroups.value = true;
          await loadImersaoGroups();
        };

    // Expose to template
    


    const closeUpdateSelectionModal = () => {
      showUpdateSelectionModal.value = false;
      selectedUpdateAction.value = null;
    };

    const peopleStageOptions = computed(() => [...new Set(classPeopleRows.value.map((row) => row.stage).filter((item) => item && item !== '-'))]);
    const peopleStatusOptions = computed(() => [...new Set(classPeopleRows.value.map((row) => row.status).filter((item) => item && item !== '-'))]);
    const peopleQuotaOptions = computed(() => [...new Set(classPeopleRows.value.map((row) => row.quota).filter((item) => item && item !== '-'))]);
    const peopleCityOptions = computed(() => [...new Set(classPeopleRows.value.map((row) => row.city).filter((item) => item && item !== '-'))]);
    const peopleGenderOptions = computed(() => [...new Set(classPeopleRows.value.map((row) => row.gender).filter((item) => item && item !== '-'))]);

    const filteredClassPeople = computed(() => {
      const term = normalizeText(peopleSearch.value);
      return classPeopleRows.value.filter((row) => {
        if (term) {
          const searchMatch = [row.name, row.cpf, row.email].some((field) => normalizeText(field).includes(term));
          if (!searchMatch) return false;
        }
        if (peopleFilterStage.value && row.stage !== peopleFilterStage.value) return false;
        if (peopleFilterStatus.value && row.status !== peopleFilterStatus.value) return false;
        if (peopleFilterQuota.value && row.quota !== peopleFilterQuota.value) return false;
        if (peopleFilterCity.value && row.city !== peopleFilterCity.value) return false;
        if (peopleFilterGender.value && row.gender !== peopleFilterGender.value) return false;
        return true;
      });
    });

    const classPeopleTotalPages = computed(() => {
      const total = Math.ceil(filteredClassPeople.value.length / classPeoplePageSize);
      return Math.max(total, 1);
    });

    const paginatedClassPeople = computed(() => {
      const start = (classPeoplePage.value - 1) * classPeoplePageSize;
      return filteredClassPeople.value.slice(start, start + classPeoplePageSize);
    });

    const classPeoplePageStart = computed(() => {
      if (!filteredClassPeople.value.length) return 0;
      return (classPeoplePage.value - 1) * classPeoplePageSize + 1;
    });

    const classPeoplePageEnd = computed(() => Math.min(classPeoplePage.value * classPeoplePageSize, filteredClassPeople.value.length));

    const classPeopleVisiblePages = computed(() => {
      const total = classPeopleTotalPages.value;
      const pages = [];
      const start = Math.max(1, classPeoplePage.value - 1);
      const end = Math.min(total, start + 2);
      for (let i = start; i <= end; i += 1) pages.push(i);
      if (pages.length < 3) {
        const first = Math.max(1, end - 2);
        pages.length = 0;
        for (let i = first; i <= end; i += 1) pages.push(i);
      }
      return pages;
    });

    const loadStages = async () => {
      try {
        stages.value = await stageService.getByClassId(classId.value);
        stageCandidatesCount.value = await stageService.getCandidatesCountByClassId(classId.value);
      } catch (err) {
        console.error('Erro ao carregar etapas:', err);
      }
    };

    const loadClassPeople = async () => {
      peopleLoading.value = true;
      peopleError.value = null;
      try {
        classEnrollments.value = await enrollmentService.getByClassId(classId.value);
      } catch (err) {
        peopleError.value = `Erro ao carregar pessoas da turma: ${err.response?.data?.message || err.message}`;
        classEnrollments.value = [];
      } finally {
        peopleLoading.value = false;
      }
    };

    const loadSelectionProcessContext = async () => {
      try {
        const [enrollments, classes] = await Promise.all([
          enrollmentService.getAll(),
          classService.getAll(),
        ]);

        allEnrollments.value = enrollments || [];
        allClasses.value = classes || [];
      } catch (err) {
        console.error('Erro ao carregar contexto do processo seletivo:', err);
        allEnrollments.value = [];
        allClasses.value = [];
      }
    };

    const loadClassDetails = async () => {
      loading.value = true;
      error.value = null;
      try {
        classData.value = await classService.getById(classId.value);
        await Promise.all([loadStages(), loadClassPeople(), loadSelectionProcessContext()]);
      } catch (err) {
        error.value = `Erro ao carregar detalhes da turma: ${err.response?.data?.message || err.message}`;
      } finally {
        loading.value = false;
      }
    };

    const getStageCandidatesCount = (stageId) => stageCandidatesCount.value[stageId] || 0;

    const createStage = async () => {
      creatingStage.value = true;
      stageError.value = null;
      try {
        await stageService.create({
          name: newStage.value.name,
          classId: classId.value,
          description: newStage.value.description,
        });
        await loadStages();
        closeCreateStageModal();
      } catch (err) {
        stageError.value = `Erro ao criar etapa: ${err.response?.data?.message || err.message}`;
      } finally {
        creatingStage.value = false;
      }
    };

    const openEditStageModal = (stage) => {
      editingStage.value = {
        id: stage.id,
        name: stage.name,
        description: stage.description || '',
      };
      showEditStageModal.value = true;
    };

    const updateStage = async () => {
      updatingStage.value = true;
      editStageError.value = null;
      try {
        await stageService.update(editingStage.value.id, {
          name: editingStage.value.name,
          description: editingStage.value.description,
        });
        await loadStages();
        closeEditStageModal();
      } catch (err) {
        editStageError.value = `Erro ao atualizar etapa: ${err.response?.data?.message || err.message}`;
      } finally {
        updatingStage.value = false;
      }
    };

    const deleteStage = async (stageId) => {
      const confirmed = await confirmDialog.value.show('Tem certeza que deseja deletar esta etapa?', 'Deletar');
      if (!confirmed) return;
      try {
        await stageService.delete(stageId);
        await loadStages();
      } catch (err) {
        alert(`Erro ao deletar etapa: ${err.response?.data?.message || err.message}`);
      }
    };

    const goToStageDetails = (stage) => {
      const normalized = (stage.name || '').normalize('NFD').replace(/\p{Diacritic}/gu, '').toUpperCase();
      const query = normalized === 'SELECAO' || normalized === 'NIVELAMENTO' ? { openAdd: 'true' } : {};
      router.push({
        name: 'StageDetails',
        params: { programId: programId.value, classId: classId.value, stageId: stage.id },
        query,
      });
    };

    const displayStageName = (name) => {
      if (!name) return '';
      const normalized = name.normalize('NFD').replace(/\p{Diacritic}/gu, '').toUpperCase();
      if (normalized === 'SELECAO') return 'SELEÇÃO';
      if (normalized === 'IMERSAO') return 'IMERSÃO';
      return name;
    };

    const goToPeople = () => {
      const programName = classData.value?.program?.name || 'Programa';
      const classCode = classData.value?.code || '-';
      router.push({ path: '/people', query: { programa: `${programName} - Turma ${classCode}` } });
    };

    const viewPerson = (person) => {
      if (!person?.id) return;
      router.push(`/people/${person.id}`);
    };

    const classPeoplePrevPage = () => {
      if (classPeoplePage.value <= 1) return;
      classPeoplePage.value -= 1;
    };

    const classPeopleNextPage = () => {
      if (classPeoplePage.value >= classPeopleTotalPages.value) return;
      classPeoplePage.value += 1;
    };

    const selectionProcessPrevPage = () => {
      if (selectionProcessPage.value <= 1) return;
      selectionProcessPage.value -= 1;
    };

    const selectionProcessNextPage = () => {
      if (selectionProcessPage.value >= selectionProcessTotalPages.value) return;
      selectionProcessPage.value += 1;
    };

    const toggleSelectionConflicts = () => {
      selectionConflictOnly.value = !selectionConflictOnly.value;
    };

    const goToClassCourses = () => {
      router.push({
        name: 'ClassCourses',
        params: { programId: programId.value, classId: classId.value },
      });
    };

    const editProgram = () => {
      if (!programId.value) {
        router.push({ path: '/programs/register' });
        return;
      }
      router.push({ path: '/programs/register', query: { edit: String(programId.value) } });
    };

    const closeCreateStageModal = () => {
      showCreateStageModal.value = false;
      newStage.value = { name: '', description: '' };
      stageError.value = null;
    };

    const closeEditStageModal = () => {
      showEditStageModal.value = false;
      editingStage.value = { id: null, name: '', description: '' };
      editStageError.value = null;
    };

    const closeIndividualRegistration = () => {
      selectedUpdateAction.value = null;
      registrationError.value = '';
      registeringCandidate.value = false;
      newCandidateForm.value = {
        name: '',
        cpf: '',
        email: '',
        gender: '',
        birthDate: '',
        city: '',
        education: '',
        institution: '',
        quota: '',
        status: '',
      };
    };

    const openIndividualRegistration = () => {
      registrationError.value = '';
      selectedUpdateAction.value = 'individual';
    };

    const parseBrazilianDate = (value) => {
      if (!value) return null;
      const raw = String(value).trim();
      if (/^\d{4}-\d{2}-\d{2}$/.test(raw)) return raw;
      const match = raw.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
      if (!match) return null;
      const [, day, month, year] = match;
      return `${year}-${month}-${day}`;
    };

    const firstSelectionStageId = computed(() => {
      const stage = (stages.value || []).find((item) => normalizeText(item?.name) === 'selecao')
        || (stages.value || []).find((item) => normalizeText(item?.name) === 'seleção')
        || (stages.value || [])[0];

      return stage?.id || null;
    });

    const registerCandidateIndividually = async () => {
      registeringCandidate.value = true;
      registrationError.value = '';

      try {
        const birthDate = parseBrazilianDate(newCandidateForm.value.birthDate);
        if (!birthDate) {
          throw new Error('Informe uma data de nascimento válida no formato dd/mm/aaaa.');
        }

        const stageId = firstSelectionStageId.value;
        if (!stageId) {
          throw new Error('Nenhuma etapa disponível para vincular o candidato.');
        }

        await peopleService.createLink({
          programaId: Number(programId.value),
          turmaId: Number(classId.value),
          etapaId: Number(stageId),
          statusInicial: newCandidateForm.value.status,
          nome: newCandidateForm.value.name,
          dataNascimento: birthDate,
          genero: newCandidateForm.value.gender,
          cota: newCandidateForm.value.quota,
          cpf: newCandidateForm.value.cpf,
          email: newCandidateForm.value.email,
          cidade: newCandidateForm.value.city,
          tipoFormacao: newCandidateForm.value.education,
          instituicao: newCandidateForm.value.institution,
        });

        await Promise.all([loadClassDetails(), loadClassPeople(), loadSelectionProcessContext()]);
        closeIndividualRegistration();
      } catch (error) {
        registrationError.value = error?.response?.data?.message || error.message || 'Erro ao cadastrar aluno.';
      } finally {
        registeringCandidate.value = false;
      }
    };

    const formatDate = (date) => {
      if (!date) return '-';
      return new Date(date).toLocaleDateString('pt-BR');
    };

    const onGroupCreated = (group) => {
      // Atualizar lista de grupos se necessário
      // ou apenas fechar o modal (que já é feito pelo evento close)
      console.log('Grupo criado:', group);
      // Recarregar dados se necessário
      loadClassDetails();
    };

    const goBack = () => router.back();

    onMounted(() => {
      loadClassDetails();
    });

    watch(
      [peopleSearch, peopleFilterStage, peopleFilterStatus, peopleFilterQuota, peopleFilterCity, peopleFilterGender],
      () => {
        classPeoplePage.value = 1;
      }
    );

    watch([selectionConflictOnly, selectionProcessRows], () => {
      selectionProcessPage.value = 1;
    });

    watch(classPeopleTotalPages, (total) => {
      if (classPeoplePage.value > total) classPeoplePage.value = total;
    });

    watch(selectionProcessTotalPages, (total) => {
      if (selectionProcessPage.value > total) selectionProcessPage.value = total;
    });
    watch(
      () => imersaoPresencaForm.value.groupId,
      (groupId) => {
        const group = imersaoPresencaGroups.value.find((item) => item.id === groupId);
        imersaoPresencaForm.value.meetingDate = group?.meetings?.[0]?.value || '';
      }
    );

    return {
      activeTab,
      programId,
      classId,
      classData,
      cityDistribution,
      classPeopleNextPage,
      classPeoplePage,
      classPeoplePageEnd,
      classPeoplePageStart,
      classPeoplePrevPage,
      classPeopleTotalPages,
      classPeopleVisiblePages,
      classStatusLabel,
      closeCreateStageModal,
      closeEditStageModal,
      closeIndividualRegistration,
      confirmDialog,
      createStage,
      creatingStage,
      currentStageIndex,
      currentStageLabel,
      currentStageMilestone,
      currentStageMilestoneDate,
      currentStagePeriod,
      cycle,
      deleteStage,
      displayStageName,
      editProgram,
      educationDistribution,
      editStageError,
      editingStage,
      error,
      formatDate,
      formatCPF,
      genderDistribution,
      filteredClassPeople,
      getStageCandidatesCount,
      goBack,
      goToClassCourses,
      goToPeople,
      goToStageDetails,
      classModelLabel,
      classPeriodLabel,
      classWorkloadLabel,
      loading,
      openUpdateSelectionModal,
      openIndividualRegistration,
      newCandidateForm,
      newStage,
      nextStageLabel,
      openEditStageModal,
      overviewCurrentCycleIndex,
      overviewCycle,
      overviewProgressPct,
      overviewStageCards,
      overviewTimeline,
      overviewTopCards,
      overviewUpdates,
      paginatedClassPeople,
      partnerLabel,
      peopleCityOptions,
      peopleError,
      peopleFilterCity,
      peopleFilterGender,
      peopleFilterQuota,
      peopleFilterStage,
      peopleFilterStatus,
      peopleGenderOptions,
      peopleLoading,
      peopleQuotaOptions,
      peopleSearch,
      peopleStageOptions,
      peopleStatusOptions,
      registrationError,
      registeringCandidate,
      selectionConflictButtonLabel,
      selectionConflictCount,
      selectionConflictOnly,
      selectionConflictSummary,
      selectionProcessMetricsCards,
      selectionProcessNextPage,
      selectionProcessPage,
      selectionProcessPageEnd,
      selectionProcessPageStart,
      selectionProcessPageSummary,
      selectionProcessPrevPage,
      selectionProcessRows,
      selectionProcessTotalPages,
      selectionProcessVisiblePages,
      selectionQuotaCards,
      selectionWaitlistCount,
      paginatedSelectionProcessRows,
      toggleSelectionConflicts,
      quotaDistribution,
      progressPct,
      closeUpdateSelectionModal,
      openUpdateSelectionModal,
      showCreateStageModal,
      showUpdateSelectionModal,
      showSubmitCoursesModal,
      showSubmitProvaNotasModal,
      showImportImersaoModal,
      showSubmitNotasImersaoModal,
      showAtualizarPresencaImersaoModal,
      tipoAvaliacaoImersao,
      showSendMessageModal,
      showCourseDetailsModal,
      selectedCourseItem,
      showGroupCreateModal,
      showEditStageModal,
      stageBucket,
      stageError,
      stages,
      statusBadgeClass,
      tabs,
      etapasSubTab,
      lastEmailInfo,
      totalCandidates,
      updateStage,
      updatingStage,
      selectedUpdateAction,
      waitlistForm,
      imersaoMetricsCards,
      imersaoGroups,
      imersaoExpandedGroupId,
      toggleImersaoGroup,
      setImersaoGroupTab,
      getImersaoGroupTab,
      imersaoPresencaGroups,
      imersaoPresencaForm,
      presencaMeetingOptions,
      presencaRows,
      setPresenceValue,
      viewPerson,
      // Nivelamento / Etapas helpers
      courses,
      courseItems,
      courseStats,
      getNivelamentoStudents,
      getCompletionColor,
      loadingNivelamento,
      loadNivelamentoData,
      assignCourse,
      removeCourse,
      goToCourse,
      openCourseDetails,
      closeCourseDetailsModal,
      onGroupCreated,
    };
  },
};
</script>

<style scoped>
.figma-page {
  --brand-900: #171f4a;
  --brand-800: #2a3566;
  --brand-600: #6377ba;
  --brand-300: #d1d6e8;
  --slate-50: #f5f7fa;
  --slate-100: #f4f5fa;
  --slate-200: #d1d6e8;
  --slate-400: #50619e;
  --slate-600: #50619e;
  --slate-700: #171f4a;
  --teal-500: #14b8a6;
  --teal-600: #0d9488;
  --teal-50: #f0fdfa;
  --green-700: #047857;
  --green-100: #dcfce7;
  --blue-700: #1d4ed8;
  --blue-100: #eff6ff;
  --purple-700: #6d28d9;
  --purple-100: #f3e8ff;
  --danger-700: #be123c;
  --danger-100: #ffe4e6;
  min-height: 100vh;
  background: var(--slate-50);
  padding: 0;
}

.hero,
.content {
  max-width: none;
  margin: 0;
}

.hero {
  background: #fff;
  border: none;
  border-bottom: 1px solid var(--slate-200);
  overflow: hidden;
  box-shadow: none;
}

.hero-inner {
  padding: 24px 32px 0;
}

.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.back-btn {
  border: none;
  background: transparent;
  color: var(--slate-600);
  font-size: 14px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.back-btn:hover {
  color: var(--brand-900);
}

.hero-actions {
  display: flex;
  gap: 8px;
}

.hero-main {
  margin-top: 12px;
}

.hero-main h1 {
  margin: 0;
  color: var(--brand-900);
  font-size: 24px;
  line-height: 1.2;
  font-weight: 600;
}

.badge-row {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  border-radius: 999px;
  border: 1px solid transparent;
  padding: 6px 12px;
  font-size: 14px;
  font-weight: 600;
}

.badge-teal {
  background: var(--teal-50);
  color: #115e59;
  border-color: #99f6e4;
}

.badge-green {
  background: var(--green-100);
  color: var(--green-700);
  border-color: #bbf7d0;
}

.badge-blue {
  background: var(--blue-100);
  color: var(--blue-700);
  border-color: #bfdbfe;
}

.badge-slate {
  background: var(--slate-100);
  color: var(--slate-700);
  border-color: var(--brand-300);
}

.badge-purple {
  background: var(--purple-100);
  color: var(--purple-700);
  border-color: #ddd6fe;
}

.meta-row {
  margin-top: 10px;
  color: var(--slate-600);
  font-size: 14px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.meta-icon {
  color: var(--slate-600);
}

.hero-cards {
  margin-top: 20px;
  padding: 0 32px 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.hero-card {
  background: var(--slate-100);
  border: 1px solid var(--slate-200);
  border-radius: 10px;
  padding: 12px;
}

.hero-label {
  display: block;
  color: var(--slate-600);
  font-size: 12px;
}

.hero-card strong {
  display: block;
  margin-top: 3px;
  color: var(--brand-900);
  font-size: 30px;
  line-height: 1.2;
  font-weight: 600;
}

.hero-card small {
  display: block;
  margin-top: 4px;
  color: var(--slate-600);
  font-size: 12px;
}

.hero-support {
  margin-top: 2px !important;
}

.warning-small {
  color: #ea580c !important;
}

.tabs {
  border-top: 1px solid var(--slate-200);
  padding: 0 32px;
  display: flex;
  gap: 2px;
  overflow-x: auto;
}

.tab {
  border: none;
  border-bottom: 2px solid transparent;
  background: transparent;
  padding: 13px 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--slate-600);
  cursor: pointer;
  white-space: nowrap;
}

.tab.active {
  color: var(--teal-600);
  border-bottom-color: var(--teal-500);
}

.content {
  margin-top: 0;
  padding: 24px 32px;
}

.stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-card {
  background: linear-gradient(90deg, var(--teal-50) 0%, #f5f3ff 100%);
  border: 1px solid #99f6e4;
  border-radius: 12px;
  padding: 14px;
}

.status-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-head h3 {
  margin: 0;
  color: var(--brand-900);
  font-size: 18px;
}

.pill {
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
}

.pill-green {
  color: var(--green-700);
  background: var(--green-100);
}

.pill-purple {
  color: var(--purple-700);
  background: var(--purple-100);
}

.pill-teal {
  color: #0f766e;
  background: #ccfbf1;
}

.pill-amber {
  color: #b45309;
  background: #fef3c7;
}

.pill-slate {
  color: #64748b;
  background: #e2e8f0;
}

.status-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.status-grid span {
  display: block;
  font-size: 12px;
  color: var(--slate-600);
}

.status-grid strong {
  display: block;
  margin-top: 2px;
  color: var(--brand-900);
  font-size: 14px;
}

.teal-strong {
  color: var(--teal-600) !important;
}

.status-note {
  margin: 10px 0 0;
  padding-top: 10px;
  border-top: 1px solid #99f6e4;
  color: var(--slate-600);
  font-size: 14px;
}

.panel {
  background: #fff;
  border: 1px solid var(--brand-300);
  border-radius: 12px;
  padding: 14px;
  box-shadow: none;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.panel-head h3,
.panel h3 {
  margin: 0;
  color: var(--brand-900);
  font-size: 18px;
}

.panel h4 {
  margin: 0 0 10px;
  color: var(--brand-900);
  font-size: 15px;
}

.panel-progress {
  font-size: 13px;
  color: var(--slate-600);
}

.panel-progress strong {
  color: var(--brand-600);
}

.progress-track {
  background: #dbe2f0;
  border-radius: 999px;
  height: 8px;
}

.progress-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--teal-500) 0%, var(--teal-600) 100%);
}

.cycle {
  margin-top: 11px;
  display: flex;
  gap: 4px;
  overflow-x: auto;
}

.cycle-item {
  display: flex;
  align-items: center;
}

.cycle-pill {
  font-size: 12px;
  color: var(--slate-600);
  background: var(--slate-100);
  border: 1px solid var(--brand-300);
  border-radius: 8px;
  padding: 6px 10px;
  white-space: nowrap;
}

.cycle-pill.done {
  color: var(--green-700);
  background: var(--green-100);
  border-color: #bbf7d0;
}

.cycle-pill.current {
  color: #115e59;
  background: var(--teal-50);
  border-color: #99f6e4;
}

.cycle-line {
  width: 14px;
  height: 2px;
  background: #cbd5e1;
}

.cycle-line.done {
  background: var(--teal-500);
}

.overview-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.overview-title {
  margin: 0;
  color: var(--brand-900);
  font-size: 28px;
  line-height: 1.1;
  font-weight: 700;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  background: #fff;
  border: 1px solid var(--brand-300);
  border-radius: 12px;
  padding: 14px;
}

.summary-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.summary-card-head h4 {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  color: var(--brand-900);
  line-height: 1.1;
}

.summary-card-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-card-list div {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.summary-card-list span {
  color: var(--slate-700);
  font-size: 15px;
}

.summary-card-list strong {
  color: var(--brand-900);
  font-size: 30px;
  font-weight: 700;
}

.warning-strong {
  color: #ea580c !important;
}

.details-link {
  margin-top: 10px;
  border: none;
  background: transparent;
  color: var(--teal-600);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.section-subtitle {
  margin: 0;
  color: var(--brand-900);
  font-size: 28px;
  line-height: 1.1;
  font-weight: 700;
}

.timeline-list {
  margin-top: 12px;
}

.timeline-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 56px;
  border-top: 1px solid var(--slate-200);
}

.timeline-left {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.timeline-check {
  width: 21px;
  height: 21px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #ccfbf1;
  color: #059669;
  font-size: 13px;
  font-weight: 700;
}

.timeline-label {
  color: var(--brand-900);
  font-size: 30px;
  font-weight: 600;
  line-height: 1.1;
}

.timeline-right {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.timeline-date {
  color: var(--slate-700);
  font-size: 15px;
}

.timeline-link {
  margin-top: 8px;
  width: 100%;
}

.distribution-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.distribution-panel h4 {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.1;
}

.quota-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quota-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.quota-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.quota-head span {
  color: var(--slate-700);
  font-size: 18px;
}

.quota-head strong {
  color: var(--brand-900);
  font-size: 21px;
}

.quota-track {
  width: 100%;
  height: 8px;
  border-radius: 999px;
  background: #e2e8f0;
  overflow: hidden;
}

.quota-fill {
  height: 100%;
  border-radius: 999px;
}

.gender-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.gender-card {
  text-align: center;
}

.gender-card strong {
  display: block;
  color: var(--brand-900);
  font-size: 36px;
  line-height: 1;
}

.gender-card span {
  display: block;
  margin-top: 6px;
  color: var(--slate-700);
  font-size: 18px;
}

.simple-list {
  margin-top: 10px;
}

.simple-list div {
  min-height: 46px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  border-top: 1px solid var(--slate-200);
}

.simple-list span {
  color: var(--slate-700);
  font-size: 18px;
}

.simple-list strong {
  color: var(--brand-900);
  font-size: 21px;
}

.updates-panel {
  padding-bottom: 10px;
}

.updates-list {
  margin-top: 10px;
}

.update-row {
  min-height: 46px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 10px;
  border-top: 1px solid var(--slate-200);
}

.update-dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
}

.dot-green {
  background: #10b981;
}

.dot-amber {
  background: #f59e0b;
}

.dot-slate {
  background: #94a3b8;
}

.update-action {
  color: var(--brand-900);
  font-size: 17px;
}

.update-meta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--slate-700);
  font-size: 14px;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.mini-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mini-list div {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  border-bottom: 1px solid #edf2ff;
  padding-bottom: 5px;
  color: var(--slate-600);
  font-size: 13px;
}

.mini-list strong {
  color: var(--brand-900);
}

.metrics {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.metric {
  border: 1px solid var(--brand-300);
  background: var(--slate-100);
  border-radius: 10px;
  padding: 10px;
}

.metric span {
  display: block;
  color: var(--slate-600);
  font-size: 12px;
}

.metric strong {
  display: block;
  margin-top: 4px;
  color: var(--brand-900);
  font-size: 20px;
}

.muted {
  margin: 6px 0 0;
  color: var(--slate-600);
  font-size: 14px;
}

.actions-inline {
  margin-top: 12px;
}

.stage-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}

.stage-card {
  border: 1px solid var(--brand-300);
  border-radius: 10px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 170px;
}

.stage-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.stage-top h4 {
  margin: 0;
  color: var(--brand-900);
  font-size: 17px;
}

.stage-count {
  border-radius: 999px;
  border: 1px solid #bfdbfe;
  background: var(--blue-100);
  color: var(--blue-700);
  font-size: 12px;
  font-weight: 700;
  padding: 4px 8px;
  white-space: nowrap;
}

.stage-card p {
  margin: 10px 0 0;
  color: var(--slate-600);
  font-size: 14px;
  flex-grow: 1;
}

.stage-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.figma-page .btn-outline,
.figma-page .btn-primary,
.figma-page .btn-danger {
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  padding: 8px 12px;
  cursor: pointer;
  border: 1px solid transparent;
}

.figma-page .btn-outline span,
.figma-page .btn-primary span,
.figma-page .btn-danger span {
  font-weight: 700;
}

.figma-page .btn-outline {
  color: var(--brand-900);
  border-color: var(--brand-300);
  background: #fff;
}

.figma-page .btn-outline:hover {
  background: var(--slate-100);
}

.figma-page .btn-primary {
  color: #fff;
  background: var(--teal-500);
  border-color: var(--teal-500);
}

.figma-page .btn-primary:hover {
  background: var(--teal-600);
  border-color: var(--teal-600);
}

.figma-page .btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-danger {
  color: var(--danger-700);
  border-color: #fecdd3;
  background: #fff;
}

.btn-danger:hover {
  background: var(--danger-100);
}

.state-box {
  border: 1px solid var(--brand-300);
  border-radius: 12px;
  background: #fff;
  padding: 20px;
  text-align: center;
  color: var(--slate-600);
}

.state-error {
  color: var(--danger-700);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 16px;
}

.modal {
  width: min(520px, 100%);
  background: #fff;
  border: 1px solid var(--brand-300);
  border-radius: 12px;
  padding: 16px;
}

.modal h2 {
  margin: 0 0 12px;
  color: var(--brand-900);
  font-size: 22px;
}

.form-row {
  margin-bottom: 10px;
}

.form-row label {
  display: block;
  margin-bottom: 5px;
  font-size: 13px;
  font-weight: 700;
  color: var(--slate-700);
}

.field {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid var(--brand-300);
  border-radius: 8px;
  padding: 10px;
  font-size: 14px;
  color: var(--brand-900);
  font-family: inherit;
}

.field:focus {
  outline: none;
  border-color: var(--teal-500);
  box-shadow: 0 0 0 2px rgba(20, 184, 166, 0.2);
}

.modal-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.individual-modal {
  width: min(1000px, calc(100vw - 24px));
  max-width: 1000px;
}

.individual-modal .form-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
}

.individual-modal .candidate-field-row {
  display: block !important;
  width: 100%;
  margin-bottom: 0;
}

.individual-modal .candidate-field-row label {
  display: block;
  width: 100%;
  margin-bottom: 6px;
}

.individual-modal .candidate-field-row .field {
  width: 100%;
}

.registration-error {
  margin-top: 8px;
}

/* People Tab Styles */
.people-panel {
  padding-bottom: 12px;
}

.people-filters {
  margin-bottom: 20px;
  padding: 12px 0;
  border-bottom: 1px solid var(--slate-200);
}

.people-search-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.people-search-box {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.people-search-icon {
  position: absolute;
  left: 12px;
  color: var(--slate-400);
  pointer-events: none;
}

.people-search-input {
  width: 100%;
  border: 1px solid var(--brand-300);
  border-radius: 8px;
  padding: 10px 10px 10px 36px;
  font-size: 14px;
  color: var(--brand-900);
  box-sizing: border-box;
}

.people-search-input::placeholder {
  color: var(--slate-400);
}

.people-search-input:focus {
  outline: none;
  border-color: var(--teal-500);
  box-shadow: 0 0 0 2px rgba(20, 184, 166, 0.2);
}

.people-search-btn {
  border: 1px solid var(--brand-300);
  background: #fff;
  color: var(--brand-900);
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.people-search-btn:hover {
  background: var(--slate-100);
}

.people-select-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.people-filter-select {
  flex: 1;
  min-width: 150px;
  border: 1px solid var(--brand-300);
  background: #fff;
  color: var(--brand-900);
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  cursor: pointer;
}

.people-filter-select:focus {
  outline: none;
  border-color: var(--teal-500);
  box-shadow: 0 0 0 2px rgba(20, 184, 166, 0.2);
}

.people-table-wrap {
  overflow-x: auto;
  margin-bottom: 12px;
}

.people-table-v2 {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
}

.people-table-v2 thead {
  background: var(--slate-50);
  border-bottom: 2px solid var(--slate-200);
}

.people-table-v2 th {
  padding: 12px;
  text-align: left;
  font-size: 13px;
  font-weight: 700;
  color: var(--slate-700);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.people-table-v2 td {
  padding: 12px;
  border-bottom: 1px solid var(--slate-200);
  font-size: 14px;
  color: var(--brand-900);
}

.people-table-v2 tbody tr:hover {
  background: var(--slate-50);
}

.name-cell {
  font-weight: 600;
  color: var(--brand-900);
}

.selection-conflict-icon {
  display: inline-flex;
  align-items: center;
  margin-left: 6px;
  color: #f97316;
  vertical-align: middle;
}

.quota-pill {
  display: inline-block;
  border-radius: 999px;
  background: var(--teal-100);
  color: var(--teal-700);
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
}

.status-pill {
  display: inline-block;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
}

.status-pill.status-active {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.status-pill.status-inactive {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.status-pill.status-pending {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

.status-pill.status-completed {
  background: #dbeafe;
  color: #1e40af;
  border: 1px solid #bfdbfe;
}

.actions-col {
  text-align: center;
  width: 50px;
}

.icon-btn {
  border: none;
  background: transparent;
  color: var(--slate-600);
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: var(--slate-200);
  color: var(--brand-900);
}

.people-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: var(--slate-50);
  border-radius: 8px;
  gap: 16px;
  flex-wrap: wrap;
}

.people-page-summary {
  font-size: 13px;
  color: var(--slate-600);
  flex-shrink: 0;
}

.people-page-controls {
  display: flex;
  gap: 4px;
  align-items: center;
  flex-wrap: wrap;
}

.page-btn {
  border: 1px solid var(--brand-300);
  background: #fff;
  color: var(--brand-900);
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.page-btn:hover:not(:disabled) {
  background: var(--slate-100);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn-number {
  min-width: 34px;
  padding: 6px 8px;
}

.page-btn-number.active {
  background: var(--teal-500);
  color: #fff;
  border-color: var(--teal-500);
}

/* Selection Process Tab Styles */
.metrics-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.metric-card {
  background: #fff;
  border: 1px solid var(--brand-300);
  border-radius: 10px;
  padding: 16px 12px;
  text-align: center;
}

.metric-label {
  display: block;
  color: var(--slate-600);
  font-size: 12px;
  margin-bottom: 8px;
}

.metric-card strong {
  display: block;
  color: var(--brand-900);
  font-size: 32px;
  font-weight: 600;
}

.quota-distribution {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.quota-item {
  background: var(--slate-50);
  border-radius: 10px;
  padding: 16px 12px;
  text-align: center;
  border: 1px solid var(--slate-200);
}

.quota-item strong {
  display: block;
  color: var(--brand-900);
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.quota-item span {
  display: block;
  color: var(--slate-600);
  font-size: 12px;
}

.alert-banner {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px 16px;
  border-radius: 10px;
  margin-top: 12px;
  margin-bottom: 16px;
  border-left: 4px solid;
}

.alert-banner svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.alert-copy {
  flex: 1;
}

.alert-warning {
  background: #fffbeb;
  border-color: #f59e0b;
  color: #92400e;
}

.alert-warning strong {
  color: #78350f;
}

.alert-warning p {
  margin: 2px 0 8px;
  font-size: 13px;
  color: #92400e;
}

.alert-link {
  background: none;
  border: none;
  color: #f59e0b;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
  margin-left: auto;
  white-space: nowrap;
}

.alert-link:hover {
  text-decoration: underline;
}

.modal.modal-large {
  width: min(1000px, calc(100vw - 32px));
  max-width: min(1000px, calc(100vw - 32px));
  max-height: calc(100vh - 32px);
  overflow: hidden;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.modal.modal-waitlist {
  width: min(920px, calc(100vw - 24px));
  max-width: 920px;
  max-height: calc(100vh - 24px);
  overflow: hidden;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--slate-200);
}

.modal-header h2 {
  margin: 0;
  color: var(--brand-900);
  font-size: 20px;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  color: var(--brand-900);
}

.modal-content {
  padding: 4px 24px 24px;
  overflow-y: auto;
  width: 100%;
  box-sizing: border-box;
  flex: 1 1 auto;
}

.modal-content.waitlist-modal {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.modal.modal-large .modal-content.update-selection-home {
  display: block !important;
  width: 100% !important;
  max-width: none !important;
  align-self: stretch;
}

.modal-subtitle {
  color: var(--slate-600);
  font-size: 14px;
  margin: 0 0 18px;
}

.modal.modal-large .update-actions-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  width: 100% !important;
  max-width: none !important;
  margin: 0 0 20px;
  justify-items: stretch;
}

.modal.modal-large .update-action-card {
  border: 2px solid #dbe4f0;
  background: #fff;
  border-radius: 10px;
  padding: 24px 26px 20px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: none;
  min-height: 168px;
  appearance: none;
  color: var(--brand-900);
}

.modal.modal-large .update-action-card:hover,
.modal.modal-large .update-action-card:focus-visible {
  background: #ecfdf8;
  border-color: var(--teal-500);
  box-shadow: 0 4px 12px rgba(20, 184, 166, 0.12);
  outline: none;
  transform: translateY(-1px);
}

.modal.modal-large .update-action-card:hover .card-icon,
.modal.modal-large .update-action-card:focus-visible .card-icon {
  color: var(--teal-500);
}

.modal.modal-large .update-action-card:hover h4,
.modal.modal-large .update-action-card:focus-visible h4,
.modal.modal-large .update-action-card:hover p,
.modal.modal-large .update-action-card:focus-visible p {
  color: inherit;
}

.modal.modal-large .card-icon {
  width: 34px;
  height: 34px;
  border-radius: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
  color: var(--teal-500);
  background: transparent;
}

.modal.modal-large .icon-people {
  color: var(--teal-500);
}

.modal.modal-large .icon-upload {
  color: var(--teal-500);
}

.modal.modal-large .icon-check {
  color: var(--teal-500);
}

.modal.modal-large .icon-clock {
  color: var(--teal-500);
}

.modal.modal-large .update-action-card h4 {
  margin: 0 0 8px;
  color: var(--brand-900);
  font-size: 17px;
  font-weight: 600;
}

.modal.modal-large .update-action-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.45;
}

.modal-actions-footer {
  border-top: 1px solid var(--slate-200);
  padding: 16px 24px 18px;
  margin-top: 0;
  width: 100%;
  box-sizing: border-box;
}

.modal-back {
  margin-bottom: 16px;
}

.back-link {
  background: none;
  border: none;
  color: var(--teal-600);
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  font-size: 14px;
}

.back-link:hover {
  text-decoration: underline;
}

.modal-content h3 {
  margin: 0 0 16px;
  color: var(--brand-900);
  font-size: 18px;
  font-weight: 600;
}

.modal.modal-large .modal-content.import-inscricoes-modal {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.modal.modal-large .import-inscricoes-modal {
  display: block;
  gap: 14px;
}

.modal.modal-large .modal-content.import-aprovados-modal {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.modal.modal-large .import-aprovados-modal {
  display: block;
  gap: 14px;
}

.modal.modal-large .import-aprovados-modal > * {
  width: 100%;
  box-sizing: border-box;
}

.modal.modal-large .import-aprovados-modal h3 {
  margin-bottom: 4px;
  font-size: 17px;
}

.modal.modal-large .import-aprovados-modal .modal-desc {
  margin-bottom: 2px;
  font-size: 14px;
  line-height: 1.5;
}

.modal.modal-large .import-aprovados-modal .columns-grid {
  margin-bottom: 2px;
}

.modal.modal-large .import-inscricoes-modal > * {
  width: 100%;
  box-sizing: border-box;
}

.modal.modal-large .import-inscricoes-modal h3 {
  margin-bottom: 4px;
  font-size: 17px;
}

.modal.modal-large .import-inscricoes-modal .modal-desc {
  margin-bottom: 2px;
  font-size: 14px;
  line-height: 1.5;
}

.modal.modal-large .modal-content.submit-cursos-modal {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.modal.modal-large .submit-cursos-modal {
  display: block;
  gap: 14px;
}

.modal.modal-large .submit-cursos-modal > * {
  width: 100%;
  box-sizing: border-box;
}

.modal.modal-large .submit-cursos-modal h3 {
  margin-bottom: 4px;
  font-size: 17px;
}

.modal.modal-large .submit-cursos-modal .modal-desc {
  margin-bottom: 2px;
  font-size: 14px;
  line-height: 1.5;
}

.modal.modal-large .modal-content.submit-prova-notas-modal {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.modal.modal-large .submit-prova-notas-modal {
  display: block;
  gap: 14px;
}

.modal.modal-large .submit-prova-notas-modal > * {
  width: 100%;
  box-sizing: border-box;
}

.modal.modal-large .submit-prova-notas-modal h3 {
  margin-bottom: 4px;
  font-size: 17px;
}

.modal.modal-large .submit-prova-notas-modal .modal-desc {
  margin-bottom: 2px;
  font-size: 14px;
  line-height: 1.5;
}

.modal.modal-large .modal-content.import-imersao-modal {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.modal.modal-large .import-imersao-modal {
  display: block;
  gap: 14px;
}

.modal.modal-large .import-imersao-modal > * {
  width: 100%;
  box-sizing: border-box;
}

.modal.modal-large .import-imersao-modal .file-upload-area {
  margin-bottom: 14px;
}

.import-imersao-footer {
  background: var(--slate-50);
  border-top: 1px solid var(--slate-200);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal.modal-large .modal-content.submit-imersao-notas-modal {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.modal.modal-large .submit-imersao-notas-modal > * {
  width: 100%;
  box-sizing: border-box;
}

.submit-imersao-type-label {
  color: var(--slate-700);
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.submit-imersao-type-tabs {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 14px;
}

.submit-imersao-type-btn {
  height: 40px;
  border: 1px solid var(--slate-200);
  background: #fff;
  color: var(--slate-700);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.submit-imersao-type-btn.active {
  border-color: var(--teal-500);
  color: #0f766e;
  background: #ecfeff;
}

.submit-imersao-notas-modal .file-upload-area {
  margin-bottom: 0;
}

.submit-imersao-notas-footer {
  background: var(--slate-50);
  border-top: 1px solid var(--slate-200);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal.modal-large .modal-content.update-presenca-imersao-modal {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.presenca-filters-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.presenca-field label {
  display: block;
  margin-bottom: 6px;
  color: var(--slate-700);
  font-size: 13px;
  font-weight: 600;
}

.presenca-list-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 10px;
}

.presenca-list-head strong {
  color: var(--brand-900);
  font-size: 14px;
}

.presenca-list-head span {
  color: var(--slate-600);
  font-size: 12px;
}

.presenca-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.presenca-row {
  border: 1px solid var(--slate-200);
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.presenca-student-name {
  color: var(--brand-900);
  font-size: 14px;
  font-weight: 600;
}

.presenca-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.presenca-action-btn {
  border: 1px solid var(--slate-200);
  background: #fff;
  color: var(--slate-700);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  height: 30px;
  padding: 0 10px;
  cursor: pointer;
}

.presenca-action-btn.active {
  border-color: var(--teal-500);
  background: #ecfeff;
  color: #0f766e;
}

.presenca-action-btn--danger.active {
  border-color: #fb7185;
  background: #fff1f2;
  color: #be123c;
}

.update-presenca-footer {
  background: var(--slate-50);
  border-top: 1px solid var(--slate-200);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-desc {
  color: var(--slate-600);
  font-size: 14px;
  margin-bottom: 16px;
}

.modal.modal-large .modal-content.send-message-modal {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.modal.modal-large .send-message-modal {
  display: block;
  gap: 14px;
}

.modal.modal-large .send-message-modal > * {
  width: 100%;
  box-sizing: border-box;
}

.send-message-section {
  margin-bottom: 16px;
}

.section-label {
  display: block;
  margin-bottom: 10px;
  color: var(--brand-900);
  font-size: 14px;
  font-weight: 600;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.radio-option input[type="radio"] {
  cursor: pointer;
  width: 18px;
  height: 18px;
  margin: 0;
  accent-color: var(--teal-500);
}

.radio-option label {
  margin: 0;
  cursor: pointer;
  color: var(--slate-700);
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 12px;
  padding: 12px;
  background: var(--slate-50);
  border-radius: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--slate-600);
  font-weight: 500;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--brand-900);
}

.stat-value.pending {
  color: #ea580c;
}

.text-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--slate-300);
  border-radius: 6px;
  font-size: 14px;
  color: var(--slate-700);
  background: white;
  box-sizing: border-box;
  font-family: inherit;
}

.text-input:focus {
  outline: none;
  border-color: var(--teal-500);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.textarea-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--slate-300);
  border-radius: 6px;
  font-size: 14px;
  color: var(--slate-700);
  background: white;
  box-sizing: border-box;
  font-family: inherit;
  resize: vertical;
  min-height: 120px;
}

.textarea-input:focus {
  outline: none;
  border-color: var(--teal-500);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.columns-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
  background: var(--slate-50);
  padding: 14px 16px;
  border-radius: 10px;
  margin-bottom: 2px;
  width: 100%;
  box-sizing: border-box;
}

.columns-grid-title {
  grid-column: 1 / -1;
  margin-bottom: 2px;
  color: var(--brand-900);
  font-size: 14px;
  font-weight: 600;
}

.modal.modal-large .import-inscricoes-modal .columns-grid {
  margin-bottom: 2px;
}

.column-item {
  color: var(--slate-700);
  font-size: 13px;
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 8px;
}

.column-item:before {
  content: '●';
  color: var(--teal-500);
  font-size: 10px;
}

.modal.modal-large .file-upload-area {
  border: 2px dashed var(--brand-300);
  border-radius: 10px;
  padding: 36px 18px;
  text-align: center;
  background: var(--slate-50);
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
  box-sizing: border-box;
}

.modal.modal-large .file-upload-area:hover {
  border-color: var(--teal-500);
  background: #f0fdfa;
}

.modal.modal-large .file-upload-area svg {
  margin-bottom: 12px;
  color: var(--slate-400);
}

.modal.modal-large .file-upload-area p {
  margin: 0;
  color: var(--brand-900);
  font-weight: 600;
  font-size: 14px;
}

.modal.modal-large .file-upload-area small {
  display: block;
  margin-top: 6px;
  color: var(--slate-600);
  font-size: 12px;
}

.info-box {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  margin-bottom: 14px;
  border-left: 4px solid;
  width: 100%;
  box-sizing: border-box;
}

.info-box svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.info-box strong {
  display: block;
  margin-bottom: 4px;
  font-size: 13px;
}

.info-box p {
  margin: 0;
  font-size: 12px;
  line-height: 1.45;
}

.modal.modal-large .import-inscricoes-modal .modal-actions {
  margin-top: 8px;
  width: 100%;
}

.modal.modal-large .import-inscricoes-modal .modal-actions .btn-primary,
.modal.modal-large .import-inscricoes-modal .modal-actions .btn-outline {
  padding: 10px 16px;
  font-size: 14px;
}

.info-blue {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #1e40af;
}

.info-blue strong {
  color: #1e40af;
}

.info-yellow {
  background: #fffbeb;
  border-color: #f59e0b;
  color: #92400e;
}

.info-yellow strong {
  color: #78350f;
}

.info-green {
  background: #ecfdf5;
  border-color: #10b981;
  color: #065f46;
}

.info-green strong {
  color: #065f46;
}

.modal-content.waitlist-modal .waitlist-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  background: #f7f9fd;
  padding: 12px 16px;
  border: 1px solid #dde5f4;
  border-radius: 12px;
  margin-bottom: 14px;
}

.modal-content.waitlist-modal .stat-item {
  text-align: center;
  display: flex;
  flex-direction: column-reverse;
  align-items: center;
  justify-content: center;
  min-height: 54px;
}

.modal-content.waitlist-modal .stat-item span {
  display: block;
  color: #5b6b94;
  font-size: 11px;
  margin-top: 6px;
  line-height: 1.15;
}

.modal-content.waitlist-modal .stat-item strong {
  display: block;
  color: var(--brand-900);
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
}

.modal-content.waitlist-modal .waitlist-form-row {
  display: block;
  margin-bottom: 14px;
  width: 100%;
  box-sizing: border-box;
}

.modal-content.waitlist-modal .waitlist-form-row--count {
  display: block;
}

.modal-content.waitlist-modal .waitlist-form-row label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 700;
  color: var(--slate-700);
}

.modal-content.waitlist-modal .waitlist-form-row .field {
  width: 100%;
}

.modal-content.waitlist-modal textarea.field {
  resize: none;
}

.modal-content.waitlist-modal .waitlist-form-row small {
  display: block;
  margin-top: 6px;
  color: var(--slate-600);
  font-size: 12px;
  line-height: 1.45;
}

@media (max-width: 1200px) {
  .hero-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .status-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .distribution-grid {
    grid-template-columns: 1fr;
  }

  .timeline-label {
    font-size: 24px;
  }

  .grid-3 {
    grid-template-columns: 1fr;
  }

  .metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .metrics-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .quota-distribution {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .update-actions-grid {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .columns-grid {
    grid-template-columns: 1fr;
  }

  .waitlist-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .figma-page {
    padding: 10px;
  }

  .hero-inner {
    padding: 14px 14px 0;
  }

  .hero-top {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-actions {
    width: 100%;
    display: block;
  }

  .hero-main h1 {
    font-size: 22px;
  }

  .hero-cards {
    padding: 0 14px 14px;
    grid-template-columns: 1fr;
  }

  .tabs {
    padding: 0 14px;
  }

  .status-grid,
  .metrics {
    grid-template-columns: 1fr;
  }

  .overview-title,
  .section-subtitle {
    font-size: 24px;
  }

  .summary-card-head h4,
  .timeline-label,
  .distribution-panel h4 {
    font-size: 20px;
  }

  .summary-card-list strong,
  .gender-card strong {
    font-size: 22px;
  }

  .quota-head span,
  .simple-list span,
  .gender-card span {
    font-size: 14px;
  }

  .quota-head strong,
  .simple-list strong {
    font-size: 16px;
  }

  .update-row {
    grid-template-columns: auto 1fr;
    padding: 8px 0;
  }

  .update-meta {
    grid-column: 2;
    justify-self: start;
    flex-wrap: wrap;
    font-size: 12px;
  }

  .people-search-row {
    flex-direction: column;
  }

  .people-filter-select {
    font-size: 12px;
    padding: 8px 10px;
    min-width: 100px;
  }

  .people-table-v2 th,
  .people-table-v2 td {
    padding: 8px;
    font-size: 12px;
  }

  .people-pagination {
    flex-direction: column;
    gap: 8px;
  }

  .people-page-summary {
    font-size: 12px;
    text-align: center;
    width: 100%;
  }

  .people-page-controls {
    justify-content: center;
    width: 100%;
  }
}

/* Scoped styles for Nivelamento additions */
.nivelamento-tabs { padding: 0 24px; }
.tab-btn { background: transparent; border: none; padding: 10px 14px; cursor: pointer; font-weight:600; color:var(--slate-600); border-bottom:2px solid transparent; }
.tab-btn.active { color:var(--teal-600); border-bottom-color:var(--teal-600); }
.nivelamento-cards { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap:12px; padding: 12px 24px; }
.n-card { background:#fff; border:1px solid var(--slate-200); border-radius:8px; padding:10px 12px; display:flex; flex-direction:column; justify-content:center; min-height:64px; box-sizing:border-box; }
.n-card .label { font-size:12px; color:var(--slate-600); margin-bottom:8px; }
.n-card .value { font-size:24px; font-weight:700; color:var(--slate-900); line-height:1; }
.n-card.teal .value { color:var(--teal-600); }
.n-card.amber .value { color:#d97706; }
.n-card.red .value { color:#dc2626; }
.email-banner { background:#f0f9ff; border:1px solid #dbeafe; border-radius:8px; padding:12px 16px; margin:0 24px; }
.email-banner-status {
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}
.courses-panel { margin: 12px 24px; }
.course-card { display:flex; justify-content:space-between; align-items:center; padding:12px; border-bottom:1px solid var(--slate-100); }
.students-table table { width:100%; border-collapse:collapse; }
.students-table th, .students-table td { padding:8px 6px; text-align:left; border-bottom:1px solid var(--slate-100); }

/* Actions row */
.nivelamento-actions { margin:12px 24px; display:flex; gap:8px; align-items:center; }
.nivelamento-actions .spacer { flex:1; }
.nivelamento-actions .btn-outline,
.nivelamento-actions .btn-primary {
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  line-height: 1;
}
.nivelamento-actions .btn-outline svg {
  flex-shrink: 0;
}
.nivelamento-actions .btn-primary {
  padding: 0 14px;
}

.danger-strong {
  color: #dc2626 !important;
}

.imersao-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.imersao-metrics-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.imersao-metric-card {
  background: var(--slate-100);
  border: 1px solid var(--slate-200);
  border-radius: 10px;
  padding: 12px;
}

.imersao-metric-label {
  display: block;
  color: var(--slate-600);
  font-size: 12px;
}

.imersao-metric-card strong {
  display: block;
  margin-top: 6px;
  color: var(--brand-900);
  font-size: 34px;
  line-height: 1;
  font-weight: 700;
}

.imersao-metric-card small {
  display: block;
  margin-top: 4px;
  color: var(--slate-600);
  font-size: 12px;
}

.imersao-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.imersao-actions .btn-outline,
.imersao-actions .btn-primary {
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  line-height: 1;
}

.imersao-actions .btn-primary {
  padding: 0 14px;
}

.imersao-groups-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.imersao-group-item {
  border: 1px solid var(--slate-200);
  border-radius: 10px;
  background: #fff;
  overflow: hidden;
}

.imersao-group-card {
  width: 100%;
  border: none;
  background: transparent;
  border-radius: 0;
  padding: 14px 16px;
  display: grid;
  grid-template-columns: 1.4fr 1.4fr auto 1.1fr auto;
  align-items: center;
  gap: 16px;
  text-align: left;
  cursor: pointer;
}

.imersao-group-card:hover {
  background: #f8fafc;
}

.imersao-group-main {
  min-width: 0;
}

.imersao-group-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.imersao-group-title strong {
  color: var(--brand-900);
  font-size: 16px;
  line-height: 1.1;
}

.imersao-group-status {
  border-radius: 8px;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid transparent;
  white-space: nowrap;
}

.imersao-group-status.is-ok {
  background: #d1fae5;
  color: #047857;
  border-color: #a7f3d0;
}

.imersao-group-status.is-warning {
  background: #fef3c7;
  color: #b45309;
  border-color: #fde68a;
}

.imersao-group-sub {
  margin-top: 8px;
  display: flex;
  gap: 4px;
  align-items: center;
  flex-wrap: wrap;
}

.imersao-group-sub span,
.imersao-group-project span,
.imersao-group-students span,
.imersao-group-avg span {
  color: var(--slate-600);
  font-size: 14px;
}

.imersao-group-sub strong,
.imersao-group-project strong,
.imersao-group-students strong,
.imersao-group-avg strong {
  color: var(--brand-900);
  font-size: 14px;
}

.imersao-group-project,
.imersao-group-students,
.imersao-group-avg {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
}

.imersao-group-project strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.imersao-group-avg strong.teal,
.imersao-group-students strong {
  color: var(--teal-600);
}

.imersao-group-arrow {
  color: #94a3b8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.imersao-group-expanded {
  border-top: 1px solid var(--slate-200);
  padding: 12px 16px 14px;
}

.imersao-group-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--slate-200);
  margin-bottom: 12px;
}

.imersao-group-tab-btn {
  border: none;
  background: transparent;
  color: var(--slate-700);
  font-size: 14px;
  font-weight: 500;
  padding: 8px 10px;
  border-bottom: 2px solid transparent;
  cursor: pointer;
}

.imersao-group-tab-btn.active {
  color: var(--teal-600);
  border-bottom-color: var(--teal-500);
  font-weight: 600;
}

.imersao-group-meta p {
  margin: 0 0 4px;
  color: var(--slate-700);
  font-size: 14px;
}

.imersao-group-meta .muted {
  margin: 8px 0 10px;
}

.imersao-group-table {
  width: 100%;
  border-collapse: collapse;
}

.imersao-group-table th,
.imersao-group-table td {
  text-align: left;
  border-top: 1px solid var(--slate-200);
  padding: 10px 12px;
  font-size: 14px;
}

.imersao-group-table th {
  color: var(--slate-600);
  font-weight: 600;
  border-top: none;
}

.imersao-situation-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
}

.imersao-situation-pill.status-regular {
  color: #047857;
  background: #d1fae5;
}

.imersao-situation-pill.status-warning {
  color: #b45309;
  background: #fef3c7;
}
.nivelamento-status-pill {
  margin-left: 4px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  background: #dbeafe;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  padding: 0 10px;
}

/* Course cards layout */
.courses-list {
  display: flex;
  flex-direction: column;
  gap: 17px;
}
.course-card-new {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid var(--slate-200);
  border-radius: 8px;
  transition: all 0.2s;
  text-align: left;
  background: #fff;
  cursor: pointer;
}
.course-card-new:hover {
  border-color: var(--teal-300);
  background: #f0fdfa;
}
.course-left-new {
  display: flex;
  align-items: center;
  gap: 17px;
  flex: 1;
}
.course-icon {
  color: var(--slate-400);
  flex-shrink: 0;
}
.course-name-new {
  font-size: 14px;
  font-weight: 600;
  color: var(--slate-900);
}
.course-badge {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 6px;
  background: var(--slate-100);
  color: var(--slate-600);
  border: 1px solid var(--slate-200);
  white-space: nowrap;
}
.course-badge-required {
  background: #fee2e2;
  color: #dc2626;
  border-color: #fecaca;
}
.course-right-new {
  display: flex;
  align-items: center;
  gap: 24px;
}
.course-stats-new {
  text-align: right;
  min-width: 200px;
}
.progress-bar {
  width: 128px;
  height: 8px;
  background: var(--slate-200);
  border-radius: 999px;
  overflow: hidden;
  display: inline-block;
}
.progress-fill {
  height: 100%;
  background: var(--teal-500);
  border-radius: 999px;
}
.course-media {
  font-size: 14px;
  color: var(--slate-700);
  font-weight: 600;
  min-width: 60px;
  text-align: right;
}
.course-stats-text {
  font-size: 12px;
  color: var(--slate-400);
  margin-bottom: 8px;
}

.course-details-modal {
  width: min(760px, calc(100vw - 32px)) !important;
  max-width: min(760px, calc(100vw - 32px)) !important;
}

.modal.modal-large .modal-content.course-details-content {
  display: block !important;
  width: 100% !important;
  min-width: 0 !important;
  max-width: none !important;
  flex: 1 1 auto;
  align-self: stretch;
}

.modal.modal-large .course-details-content > * {
  width: 100%;
  box-sizing: border-box;
}

.course-details-badges {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.course-detail-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.course-detail-stat {
  background: var(--slate-50);
  border: 1px solid var(--slate-200);
  border-radius: 8px;
  padding: 12px;
}

.course-detail-label {
  color: var(--slate-600);
  font-size: 12px;
  margin-bottom: 6px;
}

.course-detail-value {
  color: var(--brand-900);
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
}

.course-detail-value.teal {
  color: var(--teal-600);
}

.course-detail-value.amber {
  color: #d97706;
}
.progress-container {
  display: flex;
  align-items: center;
  gap: 8px;
}
.progress-pct {
  font-size: 14px;
  font-weight: 600;
  color: var(--slate-900);
  min-width: 32px;
  text-align: right;
}

/* Exam statistics cards */
.exam-stats {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.exam-stat-card {
  background: var(--slate-50);
  border: 1px solid var(--slate-200);
  border-radius: 8px;
  padding: 12px;
}
.exam-stat-label {
  font-size: 12px;
  color: var(--slate-600);
  margin-bottom: 8px;
}
.exam-stat-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--slate-900);
}
.exam-stat-value.teal {
  color: var(--teal-600);
}
.exam-stat-value.emerald {
  color: #047857;
}
.exam-stat-value.amber {
  color: #d97706;
}

/* Exam criteria box */
.exam-criteria {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 12px;
}
.exam-criteria p {
  font-size: 14px;
  color: #1e40af;
  margin: 0;
  line-height: 1.5;
}

/* Students table styling */
.students-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.students-table thead {
  background: var(--slate-50);
  border-bottom: 1px solid var(--slate-200);
}
.students-table th {
  padding: 12px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--slate-600);
  text-transform: uppercase;
}
.students-table tbody tr {
  border-bottom: 1px solid var(--slate-200);
}
.students-table tbody tr:hover {
  background: var(--slate-50);
}
.students-table td {
  padding: 12px;
}
.student-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}
.student-progress-bar {
  flex: 1;
  height: 6px;
  background: var(--slate-200);
  border-radius: 3px;
  overflow: hidden;
  min-width: 100px;
}
.student-progress-fill {
  height: 100%;
  background: var(--teal-600);
  border-radius: 3px;
}
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}
.status-approved {
  background: #dcfce7;
  color: #047857;
}
.status-inprogress {
  background: #fef3c7;
  color: #92400e;
}

/* Tabs background stripe */
.tabs-bg { display:inline-flex !important; background: #ffffff !important; padding:8px 10px !important; border-radius:8px; align-items:center; gap:6px; z-index:2; border:1px solid var(--slate-100) !important; box-shadow: 0 1px 2px rgba(2,6,23,0.04); }
.tabs-bg .tab-btn { background: transparent !important; border: none !important; }
.tab-btn { padding: 8px 12px; cursor: pointer; font-weight:600; color:var(--slate-600); border-bottom:2px solid transparent; background: transparent; }
.tab-btn.active { color:var(--teal-600); border-bottom-color:var(--teal-600); }

/* ensure the tabs-bg doesn't stretch full width */
.tabs-bg { box-shadow: none; }

@media (max-width: 1200px) {
  .nivelamento-cards { grid-template-columns: repeat(4, minmax(0, 1fr)); }
  .imersao-metrics-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .imersao-group-card { grid-template-columns: 1fr; gap: 8px; }
  .imersao-group-arrow { display: none; }
}
@media (max-width: 760px) {
  .nivelamento-cards { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .imersao-metrics-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .presenca-filters-grid { grid-template-columns: 1fr; }
  .presenca-row { flex-direction: column; align-items: flex-start; }
  .imersao-group-tabs { overflow-x: auto; white-space: nowrap; }
  .imersao-group-table { display: block; overflow-x: auto; }
}

</style>
