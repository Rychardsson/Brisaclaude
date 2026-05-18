<template>
  <div class="career-page">
    <div class="career-shell">
      <section class="page-header-card">
        <div class="page-header-top">
          <div class="header-content">
            <p class="eyebrow">Acompanhamento</p>
            <h1>Carreira</h1>
            <p class="subtitle">
              Monitore os egressos por até 24 meses após a conclusão, com foco na empregabilidade,
              no cargo atual e nos checkpoints de acompanhamento por programa e turma.
            </p>
          </div>

          <div class="top-actions">
            <button type="button" class="primary-btn" :disabled="!quickFollowUpTarget || displayingDemoData" @click="openQuickFollowUp">
              Registrar acompanhamento
            </button>
          </div>
        </div>

        <div class="stats-grid">
          <article class="stat-card stat-card-primary">
            <div class="stat-label">Egressos elegíveis</div>
            <div class="stat-value">{{ formatNumber(filteredCareerRows.length) }}</div>
            <div class="stat-note">{{ activeWindowSummary }}</div>
          </article>

          <article class="stat-card stat-card-teal">
            <div class="stat-label">Em acompanhamento</div>
            <div class="stat-value">{{ formatNumber(trackedCount) }}</div>
            <div class="stat-note">{{ trackingCoverageLabel }}</div>
          </article>

          <article class="stat-card stat-card-success">
            <div class="stat-label">Empregados</div>
            <div class="stat-value">{{ formatNumber(employedCount) }}</div>
            <div class="stat-note">{{ employmentRateLabel }}</div>
          </article>

          <article class="stat-card stat-card-warning">
            <div class="stat-label">Desempregados</div>
            <div class="stat-value">{{ formatNumber(unemployedCount) }}</div>
            <div class="stat-note">Última leitura registrada</div>
          </article>

          <article class="stat-card stat-card-neutral">
            <div class="stat-label">Pendências</div>
            <div class="stat-value">{{ formatNumber(dueRows.length) }}</div>
            <div class="stat-note">Checkpoints atrasados ou sem resposta</div>
          </article>
        </div>
      </section>

      <section class="table-card">
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
              placeholder="Buscar por egresso, empresa, cargo, programa ou turma..."
            />
          </div>

          <div class="filters-actions">
            <select v-model="selectedProgramId" class="filter-select">
              <option value="">Todos os programas</option>
              <option v-for="program in programOptions" :key="program.id" :value="program.id">
                {{ program.label }}
              </option>
            </select>

            <select v-model="selectedClassId" class="filter-select">
              <option value="">Todas as turmas</option>
              <option v-for="classItem in classOptions" :key="classItem.id" :value="classItem.id">
                {{ classItem.label }}
              </option>
            </select>

            <select v-model="selectedStatus" class="filter-select">
              <option value="ALL">Todos os status</option>
              <option value="EMPREGADO">Empregado</option>
              <option value="DESEMPREGADO">Desempregado</option>
              <option value="SEM_RESPOSTA">Sem resposta</option>
              <option value="SEM_ACOMPANHAMENTO">Sem acompanhamento</option>
              <option value="PENDENTE">Pendentes</option>
            </select>

            <select v-model="selectedWindow" class="filter-select">
              <option value="ACTIVE_24">Até 24 meses</option>
              <option value="UP_TO_6">Até 6 meses</option>
              <option value="FROM_6_TO_12">De 6 a 12 meses</option>
              <option value="FROM_12_TO_24">De 12 a 24 meses</option>
              <option value="OVER_24">Mais de 24 meses</option>
              <option value="ALL">Todas as janelas</option>
            </select>
          </div>
        </div>

        <div class="toolbar-row">
          <div class="toolbar-note">
            {{ filterSummary }}
          </div>

          <button type="button" class="ghost-btn" @click="resetFilters">
            Limpar filtros
          </button>
        </div>

        <div v-if="loading" class="state-row">
          <div class="spinner"></div>
          <span>Carregando programas, turmas e egressos...</span>
        </div>

        <div v-else-if="careerRows.length === 0" class="state-row state-row-empty">
          <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
          <div>
            <strong>Nenhum egresso elegível encontrado</strong>
            <p>Esta aba usa matrículas de alunos com data de conclusão preenchida.</p>
          </div>
        </div>

        <template v-else>
          <div v-if="displayingDemoData" class="feedback-banner feedback-demo">
            <strong>Dados de exemplo</strong>
            <span>{{ demoBannerMessage }}</span>
          </div>

          <div v-else-if="errorMessage" class="feedback-banner feedback-error">
            {{ errorMessage }}
          </div>

          <section class="automation-card">
            <div class="automation-header">
              <div>
                <p class="automation-eyebrow">Automação de carreira</p>
                <h2>Automação de acompanhamento por e-mail</h2>
                <p>
                  As preferências abaixo já são salvas no backend. O disparo automático segue em preparação,
                  mas o painel já centraliza o escopo, os checkpoints e o histórico recente dessa automação.
                </p>
              </div>

              <button
                type="button"
                class="toggle-pill"
                :class="{ 'toggle-pill-active': automationEnabled }"
                @click="toggleAutomationEnabled"
              >
                <span class="toggle-pill-dot"></span>
                {{ automationEnabled ? 'Ativada' : 'Pausada' }}
              </button>
            </div>

            <div class="automation-inline-note">
              {{ automationActionNote }}
            </div>

            <div class="automation-grid">
              <article class="automation-panel">
                <div class="automation-panel-header">
                  <div>
                    <h3>Configuração</h3>
                    <p>Defina o escopo e os checkpoints da automação.</p>
                  </div>
                  <span>{{ automationScopeLabel }}</span>
                </div>

                <div class="form-grid automation-form-grid">
                  <label class="field">
                    <span>Programa</span>
                    <select v-model="automationScopeProgramId">
                      <option value="">Todos os programas</option>
                      <option v-for="program in programOptions" :key="`automation-program-${program.id}`" :value="program.id">
                        {{ program.label }}
                      </option>
                    </select>
                  </label>

                  <label class="field">
                    <span>Turma</span>
                    <select v-model="automationScopeClassId">
                      <option value="">Todas as turmas</option>
                      <option v-for="classItem in automationClassOptions" :key="`automation-class-${classItem.id}`" :value="classItem.id">
                        {{ classItem.label }}
                      </option>
                    </select>
                  </label>

                  <label class="field">
                    <span>Canal</span>
                    <select disabled>
                      <option>E-mail</option>
                    </select>
                  </label>

                  <label class="field">
                    <span>Modo</span>
                    <select disabled>
                      <option>Disparo semestral automático</option>
                    </select>
                  </label>

                  <div class="field field-full">
                    <span>Checkpoints ativos</span>
                    <div class="checkpoint-row">
                      <button
                        v-for="month in CHECKPOINTS"
                        :key="`checkpoint-${month}`"
                        type="button"
                        class="checkpoint-pill"
                        :class="{ 'checkpoint-pill-active': isAutomationCheckpointEnabled(month) }"
                        @click="toggleAutomationCheckpoint(month)"
                      >
                        {{ month }} meses
                      </button>
                    </div>
                  </div>

                  <label class="field field-full">
                    <span>Assunto do e-mail</span>
                    <input
                      v-model="automationSubject"
                      type="text"
                      placeholder="Ex: Acompanhamento de carreira - BRISA"
                    />
                  </label>

                  <label class="field field-full">
                    <span>Mensagem-base</span>
                    <textarea
                      v-model="automationMessage"
                      rows="5"
                      placeholder="Digite aqui a mensagem inicial do acompanhamento."
                    ></textarea>
                  </label>
                </div>

                <div class="automation-actions">
                  <button type="button" class="ghost-btn" :disabled="automationSaving" @click="handleAutomationAction('save')">
                    Salvar automação
                  </button>
                  <button type="button" class="ghost-btn" :disabled="automationSaving" @click="handleAutomationAction('test')">
                    Enviar teste
                  </button>
                  <button type="button" class="primary-btn" :disabled="automationSaving" @click="toggleAutomationEnabled">
                    {{ automationEnabled ? 'Pausar automação' : 'Ativar automação' }}
                  </button>
                </div>
              </article>

              <article class="automation-panel">
                <div class="automation-panel-header">
                  <div>
                    <h3>Próximo disparo</h3>
                    <p>Resumo do envio que a automação faria com a configuração atual.</p>
                  </div>
                  <span>{{ formatNumber(automationScopedRows.length) }} egressos no escopo</span>
                </div>

                <div class="automation-summary-grid">
                  <article class="automation-summary-card">
                    <span>Quando</span>
                    <strong>{{ nextAutomationBatch.dateLabel }}</strong>
                    <small>{{ nextAutomationBatch.title }}</small>
                  </article>

                  <article class="automation-summary-card">
                    <span>Elegíveis</span>
                    <strong>{{ formatNumber(nextAutomationBatch.count) }}</strong>
                    <small>{{ nextAutomationBatch.helper }}</small>
                  </article>
                </div>

                <div class="automation-subsection">
                  <div class="automation-subheader">
                    <h4>Destinatários previstos</h4>
                    <span>{{ automationPreviewRecipients.length }} em destaque</span>
                  </div>

                  <div v-if="automationPreviewRecipients.length" class="automation-list">
                    <article
                      v-for="row in automationPreviewRecipients"
                      :key="`automation-row-${row.key}`"
                      class="automation-list-item"
                    >
                      <div class="automation-list-main">
                        <strong>{{ row.name }}</strong>
                        <span>{{ row.programName }} | {{ row.classCode }}</span>
                      </div>

                      <div class="automation-list-side">
                        <span class="agenda-chip">
                          {{ row.dueCheckpoint ? `${row.dueCheckpoint}m` : row.nextCheckpointLabel }}
                        </span>
                        <small>
                          {{ row.dueCheckpoint ? 'Pronto para envio' : `Previsto em ${formatDate(row.scheduledCheckpointDate)}` }}
                        </small>
                      </div>
                    </article>
                  </div>

                  <div v-else class="empty-inline">
                    Nenhum destinatário previsto com a configuração atual.
                  </div>
                </div>

                <div class="automation-subsection">
                  <div class="automation-subheader">
                    <h4>Histórico recente</h4>
                    <span>{{ automationHistory.length }} registro(s)</span>
                  </div>

                  <div v-if="automationHistory.length" class="automation-history-list">
                    <article
                      v-for="item in automationHistory"
                      :key="item.id"
                      class="automation-history-item"
                    >
                      <div class="automation-list-main">
                        <strong>{{ item.title }}</strong>
                        <span>{{ item.dateLabel }}</span>
                      </div>

                      <div class="automation-history-meta">
                        <span>{{ item.sentLabel }}</span>
                        <small>{{ item.note }}</small>
                      </div>
                    </article>
                  </div>

                  <div v-else class="empty-inline">
                    Nenhuma ação de automação foi registrada ainda.
                  </div>
                </div>
              </article>
            </div>
          </section>

          <div class="analytics-grid">
            <article class="insight-card">
              <div class="insight-header">
                <div>
                  <h2>Status por tempo de conclusão</h2>
                  <p>Último status conhecido dentro de cada faixa de acompanhamento.</p>
                </div>
              </div>

              <div class="window-chart">
                <div v-for="bucket in statusBuckets" :key="bucket.key" class="window-row">
                  <div class="window-label-group">
                    <strong>{{ bucket.label }}</strong>
                    <span>{{ formatNumber(bucket.total) }} egresso(s)</span>
                  </div>

                  <div class="window-track">
                    <template v-if="bucket.total > 0">
                      <span class="window-segment employed" :style="{ width: `${bucket.employedPercentage}%` }"></span>
                      <span class="window-segment unemployed" :style="{ width: `${bucket.unemployedPercentage}%` }"></span>
                      <span class="window-segment no-response" :style="{ width: `${bucket.noResponsePercentage}%` }"></span>
                      <span class="window-segment pending" :style="{ width: `${bucket.pendingPercentage}%` }"></span>
                    </template>
                  </div>

                  <div class="window-meta">
                    <span>{{ bucket.employed }} emp.</span>
                    <span>{{ bucket.unemployed }} desemp.</span>
                    <span>{{ bucket.noResponse }} sem resp.</span>
                    <span>{{ bucket.pending }} pend.</span>
                  </div>
                </div>
              </div>
            </article>

            <article class="insight-card">
              <div class="insight-header">
                <div>
                  <h2>Cargos mais frequentes</h2>
                  <p>Ranking dos cargos registrados para egressos empregados.</p>
                </div>
              </div>

              <div v-if="topPositions.length" class="ranking-list">
                <div v-for="item in topPositions" :key="item.label" class="ranking-item">
                  <div class="ranking-top">
                    <strong>{{ item.label }}</strong>
                    <span>{{ formatNumber(item.count) }}</span>
                  </div>
                  <div class="ranking-bar">
                    <span class="ranking-fill" :style="{ width: `${item.percentage}%` }"></span>
                  </div>
                </div>
              </div>

              <div v-else class="empty-inline">
                Nenhum cargo registrado ainda. Use o acompanhamento para preencher empresa e cargo dos egressos.
              </div>
            </article>

            <article class="insight-card">
              <div class="insight-header">
                <div>
                  <h2>Agenda de acompanhamento</h2>
                  <p>Quem precisa de uma nova leitura dentro da janela de 24 meses.</p>
                </div>
              </div>

              <div v-if="dueRows.length" class="agenda-list">
                <button
                  v-for="row in dueRows.slice(0, 6)"
                  :key="row.key"
                  type="button"
                  class="agenda-item"
                  :disabled="row.isDemo"
                  @click="openFollowUpModal(row)"
                >
                  <div class="agenda-main">
                    <strong>{{ row.name }}</strong>
                    <span>{{ row.programName }} | {{ row.classCode }}</span>
                  </div>
                  <div class="agenda-side">
                    <span class="agenda-chip">{{ row.nextCheckpointLabel }}</span>
                    <small>{{ row.monthsSinceCompletion }} meses desde a conclusão</small>
                  </div>
                </button>
              </div>

              <div v-else class="empty-inline">
                Nenhum checkpoint pendente com os filtros atuais.
              </div>
            </article>
          </div>

          <div class="table-scroll">
            <table class="career-table">
              <thead>
                <tr>
                  <th>Egresso</th>
                  <th>Programa / Turma</th>
                  <th>Conclusão</th>
                  <th>Janela atual</th>
                  <th>Status</th>
                  <th>Empresa / Cargo</th>
                  <th>Último acompanhamento</th>
                  <th>Próximo marco</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in filteredCareerRows" :key="row.key">
                  <td>
                    <div class="person-cell">
                      <div class="person-avatar">{{ initials(row.name) }}</div>
                      <div class="person-main">
                        <div class="person-name">{{ row.name }}</div>
                        <div class="person-sub">{{ row.email || 'Sem e-mail informado' }}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="meta-stack">
                      <strong>{{ row.programName }}</strong>
                      <span>{{ row.classCode }}</span>
                    </div>
                  </td>
                  <td>
                    <div class="meta-stack">
                      <strong>{{ formatDate(row.completionDate) }}</strong>
                      <span>{{ row.monthsSinceCompletion }} mes(es) desde a conclusão</span>
                    </div>
                  </td>
                  <td>
                    <span class="window-pill" :class="row.windowTone">
                      {{ row.windowLabel }}
                    </span>
                  </td>
                  <td>
                    <span class="status-pill" :class="row.statusTone">
                      {{ row.statusLabel }}
                    </span>
                  </td>
                  <td>
                    <div class="meta-stack">
                      <strong>{{ row.company || '-' }}</strong>
                      <span>{{ row.position || 'Cargo não informado' }}</span>
                    </div>
                  </td>
                  <td>{{ row.lastSurveyDate ? formatDate(row.lastSurveyDate) : 'Sem leitura' }}</td>
                  <td>
                    <div class="meta-stack">
                      <strong>{{ row.nextCheckpointLabel }}</strong>
                      <span>{{ row.followUps.length }} registro(s)</span>
                    </div>
                  </td>
                  <td class="actions-cell">
                    <button type="button" class="text-btn" :disabled="row.isDemo" @click="openFollowUpModal(row)">
                      {{ row.isDemo ? 'Somente exemplo' : 'Atualizar' }}
                    </button>
                    <button type="button" class="text-btn" :disabled="row.isDemo" @click="openPersonProfile(row)">
                      {{ row.isDemo ? 'Somente exemplo' : 'Ver pessoa' }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </section>

      <div v-if="followUpModalRow" class="modal-overlay" @click.self="closeFollowUpModal">
        <div class="modal-card modal-large">
          <div class="modal-header">
            <div>
              <h2>Atualizar acompanhamento</h2>
              <p>{{ followUpModalRow.name }} | {{ followUpModalRow.programName }} | {{ followUpModalRow.classCode }}</p>
            </div>
            <button type="button" class="modal-close" @click="closeFollowUpModal">x</button>
          </div>

          <div class="modal-body">
            <div class="details-grid">
              <div class="detail-item">
                <span>Conclusão</span>
                <strong>{{ formatDate(followUpModalRow.completionDate) }}</strong>
              </div>
              <div class="detail-item">
                <span>Tempo desde a conclusão</span>
                <strong>{{ followUpModalRow.monthsSinceCompletion }} meses</strong>
              </div>
              <div class="detail-item">
                <span>Status atual</span>
                <strong>{{ followUpModalRow.statusLabel }}</strong>
              </div>
              <div class="detail-item">
                <span>Próximo marco</span>
                <strong>{{ followUpModalRow.nextCheckpointLabel }}</strong>
              </div>
            </div>

            <div class="form-grid">
              <label class="field">
                <span>Data da leitura *</span>
                <input v-model="followUpForm.surveyDate" type="date" />
              </label>

              <label class="field">
                <span>Status *</span>
                <select v-model="followUpForm.status">
                  <option value="">Selecione</option>
                  <option value="EMPREGADO">Empregado</option>
                  <option value="DESEMPREGADO">Desempregado</option>
                  <option value="SEM_RESPOSTA">Sem resposta</option>
                </select>
              </label>

              <label class="field">
                <span>Empresa</span>
                <input v-model="followUpForm.company" type="text" placeholder="Ex: Empresa X" />
              </label>

              <label class="field">
                <span>Cargo</span>
                <input v-model="followUpForm.position" type="text" placeholder="Ex: Desenvolvedor(a) Jr" />
              </label>

              <label class="field field-full">
                <span>Observações</span>
                <textarea v-model="followUpForm.notes" rows="4" placeholder="Notas da conversa, fonte da informação ou próximo passo"></textarea>
              </label>
            </div>

            <div class="history-section">
              <div class="history-header">
                <h3>Histórico recente</h3>
                <span>{{ followUpModalRow.followUps.length }} registro(s)</span>
              </div>

              <div v-if="followUpModalRow.followUps.length" class="history-list">
                <article v-for="item in followUpModalRow.followUps" :key="item.id" class="history-item">
                  <div class="history-top">
                    <strong>{{ formatDate(item.surveyDate) }}</strong>
                    <span class="status-pill" :class="followUpTone(item.status)">
                      {{ formatTrackingStatus(item.status) }}
                    </span>
                  </div>
                  <div class="history-copy">
                    <span>{{ item.company || 'Empresa não informada' }}</span>
                    <span>{{ item.position || 'Cargo não informado' }}</span>
                  </div>
                  <p v-if="item.notes" class="history-notes">{{ item.notes }}</p>
                </article>
              </div>

              <div v-else class="empty-inline">
                Nenhum acompanhamento registrado ainda para este egresso.
              </div>
            </div>

            <div class="info-banner">
              Esta primeira versão salva o acompanhamento apenas no navegador atual, para permitir a validação da UX antes da integração completa com o backend.
            </div>

            <div v-if="formError" class="feedback-banner feedback-error">
              {{ formError }}
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="ghost-btn" @click="closeFollowUpModal">
              Cancelar
            </button>
            <button type="button" class="primary-btn" :disabled="followUpSaving" @click="saveFollowUp">
              {{ followUpSaving ? 'Salvando...' : 'Salvar leitura' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { careerService } from '@/services/careerService';
import { classService } from '@/services/classService';
import { enrollmentService } from '@/services/enrollmentService';
import { logService } from '@/services/logService';
import { programService } from '@/services/programService';

const CHECKPOINTS = [6, 12, 18, 24];
const DEMO_CAREER_SEED = [
  {
    key: 'demo:career:1',
    enrollmentId: 'demo-enrollment-1',
    personId: 'demo-person-1',
    classId: 'demo-class-1',
    programId: 'demo-program-1',
    name: 'Ana Beatriz Silva',
    email: 'ana.silva@exemplo.com',
    programName: 'Programa Jovem Tech',
    classCode: 'JT-2025-A',
    completionDate: monthsAgoDate(8),
    followUps: [
      {
        id: 'demo-followup-1',
        key: 'demo:career:1',
        enrollmentId: 'demo-enrollment-1',
        peopleId: 'demo-person-1',
        classId: 'demo-class-1',
        programId: 'demo-program-1',
        surveyDate: monthsAgoDate(6),
        status: 'EMPREGADO',
        company: 'Inova Tech',
        position: 'Analista de Suporte',
        notes: 'Primeira inserção profissional após a conclusão.'
      }
    ]
  },
  {
    key: 'demo:career:2',
    enrollmentId: 'demo-enrollment-2',
    personId: 'demo-person-2',
    classId: 'demo-class-2',
    programId: 'demo-program-1',
    name: 'Carlos Henrique Souza',
    email: 'carlos.souza@exemplo.com',
    programName: 'Programa Jovem Tech',
    classCode: 'JT-2024-B',
    completionDate: monthsAgoDate(14),
    followUps: [
      {
        id: 'demo-followup-2',
        key: 'demo:career:2',
        enrollmentId: 'demo-enrollment-2',
        peopleId: 'demo-person-2',
        classId: 'demo-class-2',
        programId: 'demo-program-1',
        surveyDate: monthsAgoDate(5),
        status: 'SEM_RESPOSTA',
        company: '',
        position: '',
        notes: 'Contato inicial sem retorno até o momento.'
      }
    ]
  },
  {
    key: 'demo:career:3',
    enrollmentId: 'demo-enrollment-3',
    personId: 'demo-person-3',
    classId: 'demo-class-3',
    programId: 'demo-program-2',
    name: 'Mariana Costa Lima',
    email: 'mariana.lima@exemplo.com',
    programName: 'Programa Dados em Ação',
    classCode: 'DA-2024-A',
    completionDate: monthsAgoDate(22),
    followUps: [
      {
        id: 'demo-followup-3',
        key: 'demo:career:3',
        enrollmentId: 'demo-enrollment-3',
        peopleId: 'demo-person-3',
        classId: 'demo-class-3',
        programId: 'demo-program-2',
        surveyDate: monthsAgoDate(18),
        status: 'DESEMPREGADO',
        company: '',
        position: '',
        notes: 'Em busca de recolocação na área de dados.'
      }
    ]
  }
];

const router = useRouter();

const loading = ref(false);
const errorMessage = ref('');
const searchTerm = ref('');
const selectedProgramId = ref('');
const selectedClassId = ref('');
const selectedStatus = ref('ALL');
const selectedWindow = ref('ACTIVE_24');

const programs = ref([]);
const classes = ref([]);
const enrollments = ref([]);
const followUps = ref([]);
const automationLogItems = ref([]);

const followUpModalRow = ref(null);
const followUpSaving = ref(false);
const formError = ref('');
const followUpForm = ref(defaultFollowUpForm());
const automationSaving = ref(false);
const automationEnabled = ref(true);
const automationScopeProgramId = ref('');
const automationScopeClassId = ref('');
const automationSubject = ref('Acompanhamento de carreira - BRISA');
const automationMessage = ref(
  'Olá! Queremos saber como está sua trajetória profissional após a conclusão do programa. Seu retorno ajuda no acompanhamento dos egressos.'
);
const automationCheckpoints = ref([...CHECKPOINTS]);
const automationActionNote = ref('Carregando a configuração de automação salva no backend...');

const programOptions = computed(() => {
  const known = new Map();

  programs.value.forEach((program) => {
    if (!program?.id) return;
    known.set(String(program.id), {
      id: String(program.id),
      label: program.name || `Programa ${program.id}`
    });
  });

  classes.value.forEach((classItem) => {
    const program = classItem?.program;
    if (!program?.id || known.has(String(program.id))) return;
    known.set(String(program.id), {
      id: String(program.id),
      label: program.name || `Programa ${program.id}`
    });
  });

  if (!realCareerRows.value.length) {
    demoCareerRows.value.forEach((row) => {
      if (!row.programId || known.has(String(row.programId))) return;
      known.set(String(row.programId), {
        id: String(row.programId),
        label: row.programName || `Programa ${row.programId}`
      });
    });
  }

  return Array.from(known.values()).sort((a, b) => a.label.localeCompare(b.label, 'pt-BR'));
});

const classOptions = computed(() => {
  const items = classes.value
    .filter((classItem) => {
      if (!selectedProgramId.value) return true;
      return resolveProgramIdFromClass(classItem) === selectedProgramId.value;
    })
    .map((classItem) => ({
      id: String(classItem.id),
      label: `${classItem.code || `Turma ${classItem.id}`} | ${classItem.program?.name || 'Programa'}`
    }));

  if (!realCareerRows.value.length) {
    demoCareerRows.value.forEach((row) => {
      if (selectedProgramId.value && row.programId !== selectedProgramId.value) return;
      if (items.some((item) => item.id === row.classId)) return;
      items.push({
        id: row.classId,
        label: `${row.classCode} | ${row.programName}`
      });
    });
  }

  return items.sort((a, b) => a.label.localeCompare(b.label, 'pt-BR'));
});

const automationClassOptions = computed(() => {
  const items = classes.value
    .filter((classItem) => {
      if (!automationScopeProgramId.value) return true;
      return resolveProgramIdFromClass(classItem) === automationScopeProgramId.value;
    })
    .map((classItem) => ({
      id: String(classItem.id),
      label: `${classItem.code || `Turma ${classItem.id}`} | ${classItem.program?.name || 'Programa'}`
    }));

  if (!realCareerRows.value.length) {
    demoCareerRows.value.forEach((row) => {
      if (automationScopeProgramId.value && row.programId !== automationScopeProgramId.value) return;
      if (items.some((item) => item.id === row.classId)) return;
      items.push({
        id: row.classId,
        label: `${row.classCode} | ${row.programName}`
      });
    });
  }

  return items.sort((a, b) => a.label.localeCompare(b.label, 'pt-BR'));
});

const followUpsByKey = computed(() => {
  const grouped = {};

  followUps.value.forEach((item) => {
    const key = resolveFollowUpKey(item);
    if (!key) return;
    if (!grouped[key]) grouped[key] = [];
    grouped[key].push(item);
  });

  Object.keys(grouped).forEach((key) => {
    grouped[key].sort((left, right) => String(right.surveyDate || '').localeCompare(String(left.surveyDate || '')));
  });

  return grouped;
});

const realCareerRows = computed(() => {
  const today = new Date();

  return enrollments.value
    .filter((enrollment) => isStudentEnrollment(enrollment))
    .filter((enrollment) => hasCompletionDate(enrollment))
    .map((enrollment) => buildCareerRow(enrollment, followUpsByKey.value, today))
    .filter(Boolean)
    .sort((left, right) => {
      if (left.isDue !== right.isDue) return left.isDue ? -1 : 1;
      return compareDateValues(right.completionDate, left.completionDate);
    });
});

const demoCareerRows = computed(() => {
  const today = new Date();

  return DEMO_CAREER_SEED.map((item) => buildDemoCareerRow(item, followUpsByKey.value, today)).sort((left, right) => {
    if (left.isDue !== right.isDue) return left.isDue ? -1 : 1;
    return compareDateValues(right.completionDate, left.completionDate);
  });
});

const displayingDemoData = computed(() => !realCareerRows.value.length && demoCareerRows.value.length > 0);

const demoBannerMessage = computed(() => {
  if (errorMessage.value) {
    return 'Os dados reais não puderam ser carregados agora. A tela está exibindo exemplos apenas para visualização.';
  }

  return 'Ainda não há egressos concluídos disponíveis nesta tela. Por isso, ela está exibindo exemplos apenas para visualização.';
});

const careerRows = computed(() => {
  if (realCareerRows.value.length) return realCareerRows.value;
  return demoCareerRows.value;
});

const filteredCareerRows = computed(() => {
  let rows = careerRows.value;

  if (selectedProgramId.value) {
    rows = rows.filter((row) => row.programId === selectedProgramId.value);
  }

  if (selectedClassId.value) {
    rows = rows.filter((row) => row.classId === selectedClassId.value);
  }

  rows = rows.filter((row) => matchesWindow(row.monthsSinceCompletion, selectedWindow.value));

  if (selectedStatus.value === 'EMPREGADO') rows = rows.filter((row) => row.latestStatus === 'EMPREGADO');
  if (selectedStatus.value === 'DESEMPREGADO') rows = rows.filter((row) => row.latestStatus === 'DESEMPREGADO');
  if (selectedStatus.value === 'SEM_RESPOSTA') rows = rows.filter((row) => row.latestStatus === 'SEM_RESPOSTA');
  if (selectedStatus.value === 'SEM_ACOMPANHAMENTO') rows = rows.filter((row) => row.latestStatus === 'SEM_ACOMPANHAMENTO');
  if (selectedStatus.value === 'PENDENTE') rows = rows.filter((row) => row.isDue);

  const term = normalizeText(searchTerm.value);
  if (!term) return rows;

  return rows.filter((row) => {
    const haystack = [
      row.name,
      row.email,
      row.programName,
      row.classCode,
      row.company,
      row.position
    ].map(normalizeText).join(' ');

    return haystack.includes(term);
  });
});

const enabledAutomationCheckpoints = computed(() =>
  CHECKPOINTS.filter((month) => automationCheckpoints.value.includes(month))
);

const automationScopedRows = computed(() => {
  let rows = careerRows.value.filter((row) => row.monthsSinceCompletion <= 24);

  if (automationScopeProgramId.value) {
    rows = rows.filter((row) => row.programId === automationScopeProgramId.value);
  }

  if (automationScopeClassId.value) {
    rows = rows.filter((row) => row.classId === automationScopeClassId.value);
  }

  return rows;
});

const automationDueRows = computed(() =>
  automationScopedRows.value.filter(
    (row) => row.dueCheckpoint && enabledAutomationCheckpoints.value.includes(row.dueCheckpoint)
  )
);

const automationUpcomingRows = computed(() =>
  automationScopedRows.value
    .filter((row) => !row.dueCheckpoint && row.nextCheckpoint && enabledAutomationCheckpoints.value.includes(row.nextCheckpoint))
    .map((row) => ({
      ...row,
      scheduledCheckpointDate: addMonthsToDateValue(row.completionDate, row.nextCheckpoint)
    }))
    .sort((left, right) => compareDateValues(left.scheduledCheckpointDate, right.scheduledCheckpointDate))
);

const automationPreviewRecipients = computed(() => {
  if (automationDueRows.value.length) return automationDueRows.value.slice(0, 4);
  return automationUpcomingRows.value.slice(0, 4);
});

const automationScopeLabel = computed(() => {
  const parts = [];

  if (automationScopeProgramId.value) {
    const program = programOptions.value.find((item) => item.id === automationScopeProgramId.value);
    if (program) parts.push(program.label);
  }

  if (automationScopeClassId.value) {
    const classItem = automationClassOptions.value.find((item) => item.id === automationScopeClassId.value);
    if (classItem) parts.push(classItem.label);
  }

  return parts.length ? parts.join(' | ') : 'Escopo amplo';
});

const nextAutomationBatch = computed(() => {
  if (!automationEnabled.value) {
    return {
      title: 'Automação pausada',
      dateLabel: '--',
      count: 0,
      helper: 'Ative a automação para estimar o próximo envio.'
    };
  }

  if (!enabledAutomationCheckpoints.value.length) {
    return {
      title: 'Sem checkpoints ativos',
      dateLabel: '--',
      count: 0,
      helper: 'Selecione pelo menos um checkpoint para o disparo.'
    };
  }

  if (automationDueRows.value.length) {
    return {
      title: 'Disparo imediato',
      dateLabel: 'Hoje',
      count: automationDueRows.value.length,
      helper: 'Egressos com checkpoint em aberto no escopo atual.'
    };
  }

  const upcoming = automationUpcomingRows.value[0];
  if (upcoming) {
    const dateKey = toDateKey(upcoming.scheduledCheckpointDate);
    const count = automationUpcomingRows.value.filter((row) => toDateKey(row.scheduledCheckpointDate) === dateKey).length;

    return {
      title: 'Próxima campanha prevista',
      dateLabel: formatDate(upcoming.scheduledCheckpointDate),
      count,
      helper: 'Envio estimado para o próximo checkpoint habilitado.'
    };
  }

  return {
    title: 'Sem disparos previstos',
    dateLabel: '--',
    count: 0,
    helper: 'Não há egressos dentro da janela configurada.'
  };
});

const automationHistory = computed(() => automationLogItems.value);

const trackedCount = computed(() => filteredCareerRows.value.filter((row) => row.followUps.length > 0).length);
const employedCount = computed(() => filteredCareerRows.value.filter((row) => row.latestStatus === 'EMPREGADO').length);
const unemployedCount = computed(() => filteredCareerRows.value.filter((row) => row.latestStatus === 'DESEMPREGADO').length);
const dueRows = computed(() => filteredCareerRows.value.filter((row) => row.isDue));
const quickFollowUpTarget = computed(() => dueRows.value[0] || filteredCareerRows.value[0] || null);

const activeWindowSummary = computed(() => {
  if (selectedWindow.value === 'ACTIVE_24') return 'Janela padrão de 24 meses';
  if (selectedWindow.value === 'UP_TO_6') return 'Recorte até 6 meses';
  if (selectedWindow.value === 'FROM_6_TO_12') return 'Recorte entre 6 e 12 meses';
  if (selectedWindow.value === 'FROM_12_TO_24') return 'Recorte entre 12 e 24 meses';
  if (selectedWindow.value === 'OVER_24') return 'Recorte acima de 24 meses';
  return 'Sem restrição de janela';
});

const trackingCoverageLabel = computed(() => {
  if (!filteredCareerRows.value.length) return 'Nenhum egresso no recorte atual';
  return `${formatPercent(trackedCount.value / filteredCareerRows.value.length)} do recorte com pelo menos uma leitura`;
});

const employmentRateLabel = computed(() => {
  const comparable = filteredCareerRows.value.filter((row) => ['EMPREGADO', 'DESEMPREGADO'].includes(row.latestStatus));
  if (!comparable.length) return 'Aguardando status comparáveis';
  const employed = comparable.filter((row) => row.latestStatus === 'EMPREGADO').length;
  return `${formatPercent(employed / comparable.length)} entre leituras comparáveis`;
});

const statusBuckets = computed(() => {
  const windows = [
    { key: '0-6', label: '0 a 6 meses', min: 0, max: 6 },
    { key: '7-12', label: '7 a 12 meses', min: 7, max: 12 },
    { key: '13-18', label: '13 a 18 meses', min: 13, max: 18 },
    { key: '19-24', label: '19 a 24 meses', min: 19, max: 24 }
  ];

  return windows.map((windowRange) => {
    const rows = filteredCareerRows.value.filter((row) => row.monthsSinceCompletion >= windowRange.min && row.monthsSinceCompletion <= windowRange.max);
    const total = rows.length;
    const employed = rows.filter((row) => row.latestStatus === 'EMPREGADO').length;
    const unemployed = rows.filter((row) => row.latestStatus === 'DESEMPREGADO').length;
    const noResponse = rows.filter((row) => row.latestStatus === 'SEM_RESPOSTA').length;
    const pending = rows.filter((row) => row.latestStatus === 'SEM_ACOMPANHAMENTO').length;

    return {
      key: windowRange.key,
      label: windowRange.label,
      total,
      employed,
      unemployed,
      noResponse,
      pending,
      employedPercentage: total ? (employed / total) * 100 : 0,
      unemployedPercentage: total ? (unemployed / total) * 100 : 0,
      noResponsePercentage: total ? (noResponse / total) * 100 : 0,
      pendingPercentage: total ? (pending / total) * 100 : 0
    };
  });
});

const topPositions = computed(() => {
  const counts = new Map();

  filteredCareerRows.value
    .filter((row) => row.latestStatus === 'EMPREGADO' && row.position)
    .forEach((row) => {
      const key = row.position.trim();
      counts.set(key, (counts.get(key) || 0) + 1);
    });

  const max = Math.max(...Array.from(counts.values()), 0);

  return Array.from(counts.entries())
    .map(([label, count]) => ({
      label,
      count,
      percentage: max ? (count / max) * 100 : 0
    }))
    .sort((left, right) => right.count - left.count)
    .slice(0, 6);
});

const filterSummary = computed(() => {
  const parts = [];

  if (selectedProgramId.value) {
    const item = programOptions.value.find((program) => program.id === selectedProgramId.value);
    if (item) parts.push(`programa: ${item.label}`);
  }

  if (selectedClassId.value) {
    const item = classOptions.value.find((classItem) => classItem.id === selectedClassId.value);
    if (item) parts.push(`turma: ${item.label}`);
  }

  if (selectedStatus.value !== 'ALL') parts.push(`status: ${selectedStatus.value.toLowerCase().replaceAll('_', ' ')}`);
  if (selectedWindow.value !== 'ACTIVE_24') parts.push(activeWindowSummary.value.toLowerCase());
  if (searchTerm.value.trim()) parts.push(`busca: ${searchTerm.value.trim()}`);

  return parts.length ? parts.join(' | ') : 'Sem filtros adicionais. Exibindo a janela padrão de acompanhamento.';
});

watch(selectedProgramId, () => {
  if (selectedClassId.value && !classOptions.value.some((item) => item.id === selectedClassId.value)) {
    selectedClassId.value = '';
  }
});

watch(automationScopeProgramId, () => {
  if (automationScopeClassId.value && !automationClassOptions.value.some((item) => item.id === automationScopeClassId.value)) {
    automationScopeClassId.value = '';
  }
});

onMounted(() => {
  void loadData();
});

async function loadData() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const [programsData, classesData, enrollmentsData, followUpsData, automationSettings] = await Promise.all([
      programService.getAll().catch(() => []),
      classService.getAll().catch(() => []),
      enrollmentService.getAll(),
      careerService.getFollowUps().catch(() => []),
      careerService.getAutomationSettings().catch(() => null)
    ]);

    programs.value = Array.isArray(programsData) ? programsData : [];
    classes.value = Array.isArray(classesData) ? classesData : [];
    enrollments.value = Array.isArray(enrollmentsData) ? enrollmentsData : [];
    followUps.value = Array.isArray(followUpsData)
      ? [...followUpsData].sort((left, right) => String(right.surveyDate || '').localeCompare(String(left.surveyDate || '')))
      : [];
    hydrateAutomationSettings(automationSettings);
    await loadAutomationHistory();
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || 'Não foi possível carregar os dados de carreira.';
  } finally {
    loading.value = false;
  }
}

function openPersonProfile(row) {
  if (row?.isDemo) return;
  router.push(`/people/${row.personId}`);
}

function openQuickFollowUp() {
  if (!quickFollowUpTarget.value || displayingDemoData.value) return;
  openFollowUpModal(quickFollowUpTarget.value);
}

function openFollowUpModal(row) {
  if (row?.isDemo) return;
  followUpModalRow.value = row;
  formError.value = '';
  followUpSaving.value = false;

  const latest = row.followUps[0] || null;
  followUpForm.value = {
    surveyDate: todayInputValue(),
    status: latest?.status || '',
    company: latest?.company || '',
    position: latest?.position || '',
    notes: ''
  };
}

function closeFollowUpModal() {
  followUpModalRow.value = null;
  formError.value = '';
  followUpSaving.value = false;
}

async function saveFollowUp() {
  if (!followUpModalRow.value) return;

  if (!followUpForm.value.surveyDate || !followUpForm.value.status) {
    formError.value = 'Preencha a data da leitura e o status do acompanhamento.';
    return;
  }

  if (followUpForm.value.status === 'EMPREGADO' && !followUpForm.value.position.trim()) {
    formError.value = 'Informe pelo menos o cargo quando o status for empregado.';
    return;
  }

  followUpSaving.value = true;
  formError.value = '';

  try {
    const entry = await careerService.createFollowUp({
      enrollmentId: Number(followUpModalRow.value.enrollmentId),
      peopleId: Number(followUpModalRow.value.personId),
      classId: Number(followUpModalRow.value.classId),
      programId: followUpModalRow.value.programId ? Number(followUpModalRow.value.programId) : null,
      surveyDate: followUpForm.value.surveyDate,
      status: followUpForm.value.status,
      company: followUpForm.value.company.trim(),
      position: followUpForm.value.position.trim(),
      notes: followUpForm.value.notes.trim()
    });

    followUps.value = [entry, ...followUps.value]
      .sort((left, right) => String(right.surveyDate || '').localeCompare(String(left.surveyDate || '')));

    closeFollowUpModal();
  } catch (error) {
    formError.value = error.response?.data?.message || error.message || 'Não foi possível salvar o acompanhamento.';
  } finally {
    followUpSaving.value = false;
  }
}

function resetFilters() {
  searchTerm.value = '';
  selectedProgramId.value = '';
  selectedClassId.value = '';
  selectedStatus.value = 'ALL';
  selectedWindow.value = 'ACTIVE_24';
}

function toggleAutomationEnabled() {
  automationEnabled.value = !automationEnabled.value;
  automationActionNote.value = automationEnabled.value
    ? 'Automação marcada como ativa. Salve para persistir essa preferência no backend.'
    : 'Automação marcada como pausada. Salve para persistir essa preferência no backend.';
}

function isAutomationCheckpointEnabled(month) {
  return automationCheckpoints.value.includes(month);
}

function toggleAutomationCheckpoint(month) {
  if (isAutomationCheckpointEnabled(month)) {
    automationCheckpoints.value = automationCheckpoints.value.filter((item) => item !== month);
    automationActionNote.value = `Checkpoint de ${month} meses desativado. Salve para aplicar no backend.`;
    return;
  }

  automationCheckpoints.value = [...automationCheckpoints.value, month].sort((left, right) => left - right);
  automationActionNote.value = `Checkpoint de ${month} meses ativado. Salve para aplicar no backend.`;
}

async function handleAutomationAction(action) {
  automationSaving.value = true;

  try {
    const payload = buildAutomationPayload();

    if (action === 'save') {
      const response = await careerService.updateAutomationSettings(payload);
      hydrateAutomationSettings(response);
      automationActionNote.value = 'Configuração de automação salva com sucesso no backend.';
    } else if (action === 'test') {
      const response = await careerService.sendAutomationTest(payload);
      hydrateAutomationSettings(response?.settings || null);
      automationActionNote.value = response?.message || 'Teste de automação registrado com sucesso.';
    }

    await loadAutomationHistory();
  } catch (error) {
    automationActionNote.value = error.response?.data?.message || error.message || 'Não foi possível atualizar a automação agora.';
  } finally {
    automationSaving.value = false;
  }
}

function buildCareerRow(enrollment, groupedFollowUps, today) {
  const person = enrollment?.people || {};
  const classItem = resolveClassFromEnrollment(enrollment);
  const program = resolveProgramFromEnrollment(enrollment, classItem);
  const completionDate = enrollment?.completionDate;

  if (!completionDate || !person?.id || !classItem?.id) return null;

  const monthsSinceCompletion = diffInMonths(completionDate, today);
  if (monthsSinceCompletion < 0) return null;

  const key = buildCareerKey(enrollment);
  const history = Array.isArray(groupedFollowUps[key]) ? groupedFollowUps[key] : [];
  const latest = history[0] || null;
  const dueCheckpoint = getDueCheckpoint(completionDate, history, monthsSinceCompletion);
  const nextCheckpoint = getNextCheckpoint(monthsSinceCompletion, dueCheckpoint);

  return {
    key,
    enrollmentId: String(enrollment.id),
    personId: String(person.id),
    classId: String(classItem.id),
    programId: program?.id ? String(program.id) : '',
    name: person.name || 'Pessoa sem nome',
    email: person.email || '',
    programName: program?.name || 'Programa',
    classCode: classItem.code || `Turma ${classItem.id}`,
    completionDate,
    monthsSinceCompletion,
    windowLabel: formatWindowLabel(monthsSinceCompletion),
    windowTone: getWindowTone(monthsSinceCompletion),
    latestStatus: normalizeTrackingStatus(latest?.status),
    statusLabel: formatTrackingStatus(latest?.status),
    statusTone: followUpTone(latest?.status),
    company: latest?.company || '',
    position: latest?.position || '',
    lastSurveyDate: latest?.surveyDate || '',
    followUps: history,
    isDue: Boolean(dueCheckpoint),
    dueCheckpoint,
    nextCheckpoint,
    nextCheckpointLabel: formatNextCheckpointLabel(dueCheckpoint, nextCheckpoint, monthsSinceCompletion)
  };
}

function buildDemoCareerRow(seed, groupedFollowUps, today) {
  const history = mergeFollowUpHistory(seed.followUps, groupedFollowUps[seed.key]);
  const latest = history[0] || null;
  const monthsSinceCompletion = diffInMonths(seed.completionDate, today);
  const dueCheckpoint = getDueCheckpoint(seed.completionDate, history, monthsSinceCompletion);
  const nextCheckpoint = getNextCheckpoint(monthsSinceCompletion, dueCheckpoint);

  return {
    key: seed.key,
    enrollmentId: seed.enrollmentId,
    personId: seed.personId,
    classId: seed.classId,
    programId: seed.programId,
    name: seed.name,
    email: seed.email,
    programName: seed.programName,
    classCode: seed.classCode,
    completionDate: seed.completionDate,
    monthsSinceCompletion,
    windowLabel: formatWindowLabel(monthsSinceCompletion),
    windowTone: getWindowTone(monthsSinceCompletion),
    latestStatus: normalizeTrackingStatus(latest?.status),
    statusLabel: formatTrackingStatus(latest?.status),
    statusTone: followUpTone(latest?.status),
    company: latest?.company || '',
    position: latest?.position || '',
    lastSurveyDate: latest?.surveyDate || '',
    followUps: history,
    isDue: Boolean(dueCheckpoint),
    dueCheckpoint,
    nextCheckpoint,
    nextCheckpointLabel: formatNextCheckpointLabel(dueCheckpoint, nextCheckpoint, monthsSinceCompletion),
    isDemo: true
  };
}

function mergeFollowUpHistory(baseItems = [], extraItems = []) {
  const merged = new Map();

  [...baseItems, ...(Array.isArray(extraItems) ? extraItems : [])].forEach((item) => {
    if (!item?.id) return;
    merged.set(String(item.id), item);
  });

  return Array.from(merged.values()).sort((left, right) => String(right.surveyDate || '').localeCompare(String(left.surveyDate || '')));
}

function resolveClassFromEnrollment(enrollment) {
  if (enrollment?.classModel?.id) return enrollment.classModel;
  const classId = enrollment?.classModel?.id ?? enrollment?.classId;
  return classes.value.find((item) => String(item.id) === String(classId)) || null;
}

function resolveProgramFromEnrollment(enrollment, classItem) {
  if (enrollment?.classModel?.program?.id) return enrollment.classModel.program;
  if (classItem?.program?.id) return classItem.program;
  const programId = classItem?.program?.id ?? classItem?.programId;
  return programs.value.find((item) => String(item.id) === String(programId)) || null;
}

function resolveProgramIdFromClass(classItem) {
  return classItem?.program?.id ? String(classItem.program.id) : '';
}

function hasCompletionDate(enrollment) {
  return Boolean(enrollment?.completionDate);
}

function isStudentEnrollment(enrollment) {
  const roleName = normalizeText(enrollment?.academicRole?.name || '');
  return roleName.includes('aluno');
}

function buildCareerKey(enrollment) {
  return [
    enrollment?.people?.id || 'person',
    enrollment?.classModel?.id || 'class',
    enrollment?.id || 'enrollment'
  ].join(':');
}

function resolveFollowUpKey(item) {
  if (item?.key) return String(item.key);

  return [
    item?.peopleId || item?.personId || 'person',
    item?.classId || 'class',
    item?.enrollmentId || item?.id || 'enrollment'
  ].join(':');
}

function getDueCheckpoint(completionDate, history, monthsSinceCompletion) {
  for (const checkpoint of CHECKPOINTS) {
    if (monthsSinceCompletion < checkpoint) continue;
    if (!hasCheckpointCoverage(completionDate, history, checkpoint)) {
      return checkpoint;
    }
  }

  return null;
}

function getNextCheckpoint(monthsSinceCompletion, dueCheckpoint) {
  if (dueCheckpoint) return dueCheckpoint;
  return CHECKPOINTS.find((checkpoint) => checkpoint > monthsSinceCompletion) || null;
}

function hasCheckpointCoverage(completionDate, history, checkpoint) {
  const checkpointIndex = CHECKPOINTS.indexOf(checkpoint);
  const nextCheckpoint = CHECKPOINTS[checkpointIndex + 1] || null;

  return history.some((item) => {
    const months = diffInMonths(completionDate, item.surveyDate);
    if (months < checkpoint) return false;
    if (!nextCheckpoint) return months >= checkpoint;
    return months >= checkpoint && months < nextCheckpoint;
  });
}

function formatNextCheckpointLabel(dueCheckpoint, nextCheckpoint, monthsSinceCompletion) {
  if (monthsSinceCompletion > 24 && !dueCheckpoint) return 'Ciclo encerrado';
  if (dueCheckpoint) return `Atrasado ${dueCheckpoint}m`;
  if (nextCheckpoint) return `Previsto ${nextCheckpoint}m`;
  return 'Acompanhado';
}

function followUpTone(status) {
  const normalized = normalizeTrackingStatus(status);
  if (normalized === 'EMPREGADO') return 'success';
  if (normalized === 'DESEMPREGADO') return 'warning';
  if (normalized === 'SEM_RESPOSTA') return 'error';
  return 'neutral';
}

function formatTrackingStatus(status) {
  const normalized = normalizeTrackingStatus(status);
  if (normalized === 'EMPREGADO') return 'Empregado';
  if (normalized === 'DESEMPREGADO') return 'Desempregado';
  if (normalized === 'SEM_RESPOSTA') return 'Sem resposta';
  return 'Sem acompanhamento';
}

function normalizeTrackingStatus(status) {
  const normalized = normalizeText(status);
  if (normalized === 'empregado') return 'EMPREGADO';
  if (normalized === 'desempregado') return 'DESEMPREGADO';
  if (normalized === 'sem_resposta' || normalized === 'sem resposta') return 'SEM_RESPOSTA';
  return 'SEM_ACOMPANHAMENTO';
}

function formatWindowLabel(months) {
  if (months <= 6) return 'Até 6 meses';
  if (months <= 12) return 'De 6 a 12 meses';
  if (months <= 24) return 'De 12 a 24 meses';
  return 'Acima de 24 meses';
}

function getWindowTone(months) {
  if (months <= 6) return 'window-primary';
  if (months <= 12) return 'window-teal';
  if (months <= 24) return 'window-warning';
  return 'window-neutral';
}

function matchesWindow(months, filterValue) {
  if (filterValue === 'ALL') return true;
  if (filterValue === 'ACTIVE_24') return months <= 24;
  if (filterValue === 'UP_TO_6') return months <= 6;
  if (filterValue === 'FROM_6_TO_12') return months > 6 && months <= 12;
  if (filterValue === 'FROM_12_TO_24') return months > 12 && months <= 24;
  if (filterValue === 'OVER_24') return months > 24;
  return true;
}

function compareDateValues(left, right) {
  const leftDate = parseDateValue(left);
  const rightDate = parseDateValue(right);
  return leftDate.getTime() - rightDate.getTime();
}

function addMonthsToDateValue(value, months) {
  const date = parseDateValue(value);
  if (Number.isNaN(date.getTime())) return new Date('');

  const copy = new Date(date);
  copy.setMonth(copy.getMonth() + Number(months || 0));
  return copy;
}

function toDateKey(value) {
  const date = parseDateValue(value);
  if (Number.isNaN(date.getTime())) return '';
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function diffInMonths(fromValue, toValue = new Date()) {
  const start = parseDateValue(fromValue);
  const end = parseDateValue(toValue);

  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) return 0;

  let months = (end.getFullYear() - start.getFullYear()) * 12;
  months += end.getMonth() - start.getMonth();

  if (end.getDate() < start.getDate()) {
    months -= 1;
  }

  return Math.max(months, 0);
}

function parseDateValue(value) {
  if (value instanceof Date) return value;

  const text = String(value || '').trim();
  if (!text) return new Date('');

  const match = text.match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (match) {
    return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
  }

  return new Date(text);
}

function formatDate(value) {
  const date = parseDateValue(value);
  if (Number.isNaN(date.getTime())) return '-';

  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(date);
}

function formatNumber(value) {
  return new Intl.NumberFormat('pt-BR').format(Number(value || 0));
}

function formatPercent(value) {
  return `${Math.round((value || 0) * 100)}%`;
}

function initials(value) {
  const parts = String(value || '')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2);

  return parts.map((item) => item[0]?.toUpperCase() || '').join('') || 'EG';
}

function normalizeText(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .trim();
}

function todayInputValue() {
  return toInputDate(new Date());
}

function monthsAgoDate(months) {
  const date = new Date();
  date.setMonth(date.getMonth() - months);
  return toInputDate(date);
}

function toInputDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function defaultFollowUpForm() {
  return {
    surveyDate: todayInputValue(),
    status: '',
    company: '',
    position: '',
    notes: ''
  };
}

function buildAutomationPayload() {
  return {
    enabled: automationEnabled.value,
    programId: automationScopeProgramId.value ? Number(automationScopeProgramId.value) : null,
    classId: automationScopeClassId.value ? Number(automationScopeClassId.value) : null,
    subject: automationSubject.value.trim(),
    message: automationMessage.value.trim(),
    checkpoints: [...automationCheckpoints.value].sort((left, right) => left - right)
  };
}

function hydrateAutomationSettings(settings) {
  if (!settings || typeof settings !== 'object') return;

  automationEnabled.value = Boolean(settings.enabled);
  automationScopeProgramId.value = settings.programId ? String(settings.programId) : '';
  automationScopeClassId.value = settings.classId ? String(settings.classId) : '';
  automationSubject.value = settings.subject || 'Acompanhamento de carreira - BRISA';
  automationMessage.value = settings.message || '';
  automationCheckpoints.value = Array.isArray(settings.checkpoints) && settings.checkpoints.length
    ? [...settings.checkpoints].map((item) => Number(item)).filter(Number.isFinite).sort((left, right) => left - right)
    : [...CHECKPOINTS];

  automationActionNote.value = settings.updatedAt
    ? `Configuração sincronizada com o backend em ${formatDateTime(settings.updatedAt)}.`
    : 'Configuração de automação carregada do backend.';
}

async function loadAutomationHistory() {
  try {
    const response = await logService.getLogs({
      entityType: 'CareerAutomation',
      page: 0,
      size: 8,
      sortBy: 'createdAt',
      sortDirection: 'DESC'
    });

    const items = Array.isArray(response?.content) ? response.content : Array.isArray(response) ? response : [];
    automationLogItems.value = items.map(mapAutomationLog);
  } catch (error) {
    automationLogItems.value = [];
  }
}

function mapAutomationLog(log) {
  let details = {};

  if (typeof log?.details === 'string') {
    try {
      details = JSON.parse(log.details);
    } catch (error) {
      details = {};
    }
  } else if (log?.details && typeof log.details === 'object') {
    details = log.details;
  }

  const operation = String(details.operation || '').toUpperCase();
  const checkpoints = Array.isArray(details.checkpoints) && details.checkpoints.length
    ? details.checkpoints.join(', ')
    : CHECKPOINTS.join(', ');

  return {
    id: `automation-log-${log.id}`,
    title: operation === 'TEST' ? 'Teste de automação registrado' : 'Configuração de automação atualizada',
    dateLabel: formatDateTime(log.createdAt),
    sentLabel: operation === 'TEST' ? 'Teste salvo' : 'Preferências salvas',
    note: `${log.description || 'Ação registrada no backoffice.'} Checkpoints: ${checkpoints}.`
  };
}

function formatDateTime(value) {
  const date = parseDateValue(value);
  if (Number.isNaN(date.getTime())) return '-';

  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}
</script>

<style scoped>
.career-page {
  min-height: 100%;
  background: #eef3f8;
  padding: 14px 16px 20px;
}

.career-shell {
  max-width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header-card,
.table-card,
.stat-card,
.insight-card,
.modal-card {
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

.header-content {
  min-width: min(760px, 100%);
}

.eyebrow {
  margin: 0 0 2px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.18em;
  color: #6a7a90;
  text-transform: uppercase;
}

.header-content h1 {
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
  max-width: 860px;
  line-height: 1.6;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.ghost-btn,
.primary-btn,
.filter-select,
.search-input,
.text-btn,
.agenda-item {
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
  white-space: nowrap;
}

.ghost-btn {
  background: #fff;
  color: #13233f;
}

.primary-btn {
  background: #14b8a6;
  color: #fff;
  border-color: #14b8a6;
  box-shadow: 0 8px 16px rgba(20, 184, 166, 0.2);
}

.ghost-btn:hover,
.primary-btn:hover,
.agenda-item:hover {
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
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.stats-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  align-items: stretch;
}

.stat-card {
  padding: 14px 14px 12px;
  min-height: 118px;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.stat-card-primary {
  background: linear-gradient(180deg, #ffffff 0%, #f5f8ff 100%);
}

.stat-card-teal {
  background: linear-gradient(180deg, #ffffff 0%, #f4fdf9 100%);
}

.stat-card-success {
  background: linear-gradient(180deg, #ffffff 0%, #f4fff8 100%);
}

.stat-card-warning {
  background: linear-gradient(180deg, #ffffff 0%, #fffaf2 100%);
}

.stat-card-neutral {
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.stat-label {
  color: #6a7a90;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  min-height: 16px;
}

.stat-value {
  margin-top: 10px;
  color: #13233f;
  font-size: 28px;
  line-height: 1;
  font-weight: 800;
}

.stat-note {
  margin-top: auto;
  padding-top: 12px;
  color: #8a98ab;
  font-size: 11px;
  line-height: 1.45;
  min-height: 32px;
  display: flex;
  align-items: flex-end;
}

.table-card {
  overflow: hidden;
}

.filters-row {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 14px 18px 0;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 260px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #97a4b6;
  pointer-events: none;
}

.search-input,
.filter-select {
  height: 40px;
  border: 1px solid #d9e2ec;
  border-radius: 12px;
  background: #fff;
  color: #13233f;
  box-sizing: border-box;
}

.search-input {
  width: 100%;
  padding: 0 14px 0 44px;
  outline: none;
}

.search-input:focus,
.filter-select:focus {
  border-color: #14b8a6;
  box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.1);
}

.filters-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-select {
  min-width: 180px;
  padding: 0 12px;
  outline: none;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 18px 14px;
  border-bottom: 1px solid #e6edf5;
  flex-wrap: wrap;
}

.toolbar-note {
  color: #50619e;
  font-size: 13px;
}

.state-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 24px 18px;
  color: #41546e;
}

.state-row-error {
  color: #b42318;
}

.state-row-empty strong {
  display: block;
  color: #13233f;
  margin-bottom: 3px;
}

.state-row-empty p {
  margin: 0;
}

.spinner {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  border: 3px solid rgba(20, 184, 166, 0.2);
  border-top-color: #14b8a6;
  animation: spin 0.8s linear infinite;
}

.analytics-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr) minmax(320px, 0.8fr);
  gap: 14px;
  padding: 18px;
  align-items: stretch;
}

.insight-card {
  padding: 16px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.insight-header {
  min-height: 68px;
}

.insight-header h2 {
  margin: 0;
  font-size: 18px;
  color: #13233f;
}

.insight-header p {
  margin: 6px 0 0;
  color: #6a7a90;
  font-size: 13px;
  line-height: 1.5;
}

.window-chart {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ranking-list,
.agenda-list {
  margin-top: 16px;
}

.window-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.window-label-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
}

.window-label-group strong {
  color: #13233f;
}

.window-label-group span {
  color: #6a7a90;
}

.window-track {
  height: 12px;
  border-radius: 999px;
  background: #eef2f7;
  overflow: hidden;
  display: flex;
}

.window-segment {
  height: 100%;
}

.window-segment.employed {
  background: #10b981;
}

.window-segment.unemployed {
  background: #f59e0b;
}

.window-segment.no-response {
  background: #ef4444;
}

.window-segment.pending {
  background: #94a3b8;
}

.window-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  color: #6a7a90;
  font-size: 12px;
}

.ranking-list,
.agenda-list,
.history-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ranking-item {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 12px;
}

.ranking-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.ranking-top strong {
  color: #13233f;
  font-size: 14px;
}

.ranking-top span {
  color: #6a7a90;
  font-size: 12px;
  font-weight: 700;
}

.ranking-bar {
  width: 100%;
  height: 8px;
  border-radius: 999px;
  background: #eef2f7;
  overflow: hidden;
}

.ranking-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #14b8a6, #2dd4bf);
}

.agenda-item {
  width: 100%;
  border: 1px solid #dfe7f1;
  background: #fff;
  border-radius: 14px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.agenda-item:hover {
  border-color: #9be1d8;
  box-shadow: 0 8px 16px rgba(20, 184, 166, 0.08);
}

.agenda-main {
  min-width: 0;
}

.agenda-main strong,
.agenda-main span,
.agenda-side small {
  display: block;
}

.agenda-main strong {
  color: #13233f;
  font-size: 14px;
}

.agenda-main span,
.agenda-side small {
  margin-top: 4px;
  color: #6a7a90;
  font-size: 12px;
}

.agenda-side {
  text-align: right;
  flex-shrink: 0;
}

.agenda-chip,
.window-pill,
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
}

.agenda-chip {
  background: #fff7ed;
  color: #b45309;
}

.window-pill.window-primary {
  background: #eff6ff;
  color: #1d4ed8;
}

.window-pill.window-teal {
  background: #ecfdf5;
  color: #047857;
}

.window-pill.window-warning {
  background: #fff7ed;
  color: #b45309;
}

.window-pill.window-neutral {
  background: #f1f5f9;
  color: #475569;
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

.empty-inline {
  margin-top: 16px;
  border: 1px dashed #d7dfea;
  border-radius: 14px;
  background: #fbfdff;
  color: #6a7a90;
  font-size: 13px;
  line-height: 1.5;
  padding: 18px;
}

.table-scroll {
  width: 100%;
  overflow-x: auto;
  border-top: 1px solid #e6edf5;
}

.career-table {
  width: 100%;
  min-width: 1180px;
  border-collapse: collapse;
}

.career-table th,
.career-table td {
  padding: 14px 12px;
  border-bottom: 1px solid #e6edf5;
  text-align: left;
  vertical-align: top;
  font-size: 14px;
}

.career-table th {
  color: #6a7a90;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.person-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.person-avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #1f285f 0%, #0f766e 100%);
  color: #fff;
  font-weight: 800;
  flex-shrink: 0;
}

.person-main,
.meta-stack {
  min-width: 0;
}

.person-name {
  color: #13233f;
  font-weight: 700;
}

.person-sub,
.meta-stack span {
  margin-top: 4px;
  display: block;
  color: #6a7a90;
  font-size: 12px;
}

.meta-stack strong {
  display: block;
  color: #13233f;
}

.actions-cell {
  white-space: nowrap;
}

.text-btn {
  appearance: none;
  border: 0;
  background: transparent;
  color: #0f766e;
  cursor: pointer;
  font-weight: 700;
  padding: 0;
}

.text-btn:disabled {
  color: #94a3b8;
  cursor: not-allowed;
}

.text-btn + .text-btn {
  margin-left: 12px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.54);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
  padding: 20px;
}

.modal-card {
  width: min(880px, 100%);
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-large {
  max-width: 880px;
}

.modal-header,
.modal-footer {
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.modal-header {
  border-bottom: 1px solid #e6edf5;
}

.modal-footer {
  border-top: 1px solid #e6edf5;
  justify-content: flex-end;
}

.modal-header h2 {
  margin: 0;
  color: #13233f;
  font-size: 20px;
}

.modal-header p {
  margin: 6px 0 0;
  color: #6a7a90;
  font-size: 14px;
}

.modal-close {
  appearance: none;
  border: none;
  background: #f1f5f9;
  color: #334155;
  border-radius: 10px;
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 28px;
  line-height: 1;
}

.modal-body {
  padding: 20px;
  overflow: auto;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.detail-item {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 14px;
  background: #fff;
}

.detail-item span {
  display: block;
  color: #6b7280;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.detail-item strong {
  display: block;
  margin-top: 8px;
  color: #13233f;
  font-size: 15px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-top: 18px;
}

.field {
  display: block;
}

.field span {
  display: block;
  margin-bottom: 8px;
  color: #6a7a90;
  font-size: 13px;
  font-weight: 700;
}

.field input,
.field textarea,
.field select {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #cfd7e6;
  border-radius: 12px;
  background: #fff;
  padding: 12px 14px;
  font: inherit;
  color: #13233f;
}

.field textarea {
  resize: vertical;
}

.field-full {
  grid-column: 1 / -1;
}

.history-section {
  margin-top: 22px;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.history-header h3 {
  margin: 0;
  color: #13233f;
  font-size: 16px;
}

.history-header span {
  color: #6a7a90;
  font-size: 12px;
  font-weight: 700;
}

.history-item {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 12px 14px;
  background: #fff;
}

.history-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.history-top strong {
  color: #13233f;
}

.history-copy {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
  color: #6a7a90;
  font-size: 13px;
}

.history-notes {
  margin: 8px 0 0;
  color: #50619e;
  font-size: 13px;
  line-height: 1.5;
}

.info-banner,
.feedback-banner {
  margin-top: 18px;
  border-radius: 14px;
  padding: 14px 16px;
  font-size: 14px;
  line-height: 1.5;
}

.table-card > .feedback-banner {
  margin: 18px 18px 0;
}

.info-banner {
  background: #eef6ff;
  color: #1d4ed8;
}

.feedback-error {
  background: #fff2ef;
  color: #c2410c;
}

.feedback-demo {
  margin-bottom: 16px;
  background: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.feedback-demo strong {
  display: block;
  margin-bottom: 4px;
}

.automation-card {
  margin: 18px;
  padding: 18px;
  border: 1px solid #dfe7f1;
  border-radius: 20px;
  background: linear-gradient(180deg, #fbfefe 0%, #f7fafc 100%);
}

.automation-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.automation-eyebrow {
  margin: 0 0 4px;
  color: #0f766e;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.automation-header h2 {
  margin: 0;
  color: #13233f;
  font-size: 24px;
}

.automation-header p {
  margin: 8px 0 0;
  color: #6a7a90;
  font-size: 14px;
  line-height: 1.5;
  max-width: 720px;
}

.toggle-pill {
  appearance: none;
  border: 1px solid #d7e1ec;
  border-radius: 999px;
  background: #fff;
  color: #475569;
  padding: 8px 12px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: 700;
}

.toggle-pill-active {
  border-color: #8ee1d8;
  background: #ecfdf5;
  color: #0f766e;
}

.toggle-pill-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: currentColor;
  opacity: 0.9;
}

.automation-inline-note {
  margin-top: 14px;
  border: 1px solid #dbe4ee;
  border-radius: 14px;
  background: #f8fafc;
  color: #52627b;
  padding: 12px 14px;
  font-size: 13px;
  line-height: 1.5;
}

.automation-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 14px;
  align-items: stretch;
}

.automation-panel {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  padding: 16px;
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.automation-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.automation-panel-header h3 {
  margin: 0;
  color: #13233f;
  font-size: 18px;
}

.automation-panel-header p {
  margin: 6px 0 0;
  color: #6a7a90;
  font-size: 13px;
  line-height: 1.5;
}

.automation-panel-header > span {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  border: 1px solid #cfece7;
  background: #f0fdf9;
  color: #0f766e;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
}

.automation-form-grid {
  margin-top: 16px;
}

.checkpoint-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.checkpoint-pill {
  appearance: none;
  border: 1px solid #d8e2ee;
  border-radius: 999px;
  background: #fff;
  color: #475569;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 700;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.checkpoint-pill-active {
  border-color: #82d7ce;
  background: #ecfdf5;
  color: #0f766e;
}

.automation-actions {
  margin-top: auto;
  padding-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.automation-summary-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.automation-summary-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #fbfdff;
  padding: 14px;
}

.automation-summary-card span,
.automation-summary-card small {
  display: block;
}

.automation-summary-card span {
  color: #6a7a90;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.automation-summary-card strong {
  display: block;
  margin-top: 8px;
  color: #13233f;
  font-size: 26px;
  line-height: 1.05;
}

.automation-summary-card small {
  margin-top: 8px;
  color: #6a7a90;
  font-size: 12px;
  line-height: 1.45;
}

.automation-subsection {
  margin-top: 18px;
}

.automation-subheader {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.automation-subheader h4 {
  margin: 0;
  color: #13233f;
  font-size: 14px;
}

.automation-subheader span {
  color: #6a7a90;
  font-size: 12px;
}

.automation-list,
.automation-history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.automation-list-item,
.automation-history-item {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  padding: 12px 14px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.automation-list-main {
  min-width: 0;
}

.automation-list-main strong,
.automation-list-main span {
  display: block;
}

.automation-list-main strong {
  color: #13233f;
  font-size: 14px;
}

.automation-list-main span {
  margin-top: 4px;
  color: #6a7a90;
  font-size: 12px;
}

.automation-list-side,
.automation-history-meta {
  flex-shrink: 0;
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.automation-list-side small,
.automation-history-meta small {
  color: #6a7a90;
  font-size: 12px;
}

.automation-history-meta span {
  color: #13233f;
  font-size: 13px;
  font-weight: 700;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1280px) {
  .stats-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .automation-grid,
  .analytics-grid {
    grid-template-columns: 1fr;
  }

  .automation-summary-grid,
  .details-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .career-page {
    padding: 12px;
  }

  .page-header-card,
  .automation-card,
  .insight-card,
  .modal-body {
    padding: 16px;
  }

  .header-content h1 {
    font-size: 28px;
  }

  .stats-grid,
  .automation-summary-grid,
  .details-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .automation-card {
    margin: 16px 12px 0;
  }

  .filters-row,
  .toolbar-row,
  .automation-header,
  .automation-panel-header,
  .automation-subheader,
  .top-actions,
  .modal-header,
  .modal-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .search-box,
  .filter-select,
  .toggle-pill,
  .ghost-btn,
  .primary-btn {
    width: 100%;
  }

  .automation-list-item,
  .automation-history-item,
  .agenda-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .automation-list-side,
  .automation-history-meta,
  .agenda-side {
    text-align: left;
  }
}
</style>
