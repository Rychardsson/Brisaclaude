<template>
  <div class="home">
    <div class="home-header">
      <h1>Residência em Software - TIC 16</h1>
    </div>
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Carregando dados...</p>
    </div>
    <div v-else>
      <!-- Seção Mapa + Residências Side by Side -->
      <div class="map-residences-section">
        <!-- Mapa à esquerda -->
        <div class="map-card-centered">
          <div class="map-card-header">
            <h2 class="residences-title">Projetos Pelo Brasil</h2>
            <button class="btn-zoom" @click="mapModalOpen = true" title="Ampliar mapa">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                <line x1="11" y1="8" x2="11" y2="14"/><line x1="8" y1="11" x2="14" y2="11"/>
              </svg>
              Ampliar
            </button>
          </div>
          <BrazilMap
            :activeStates="activeStates"
            :selectedState="selectedState"
            :stateColors="stateColors"
            @select="selectState"
          />
          <p class="map-hint-text">Clique nos estados para explorar as residências</p>
        </div>

        <!-- Residências no Brasil à direita (40%) -->
        <div class="residences-card">

          <!-- VISÃO GLOBAL (nenhum estado selecionado) -->
          <template v-if="!selectedState">
            <h2 class="residences-title">Residências pelo Brasil</h2>
            <div class="stats-grid-3">
              <div class="stat-box-large">
                <span class="stat-label">PROGRAMAS ATIVOS</span>
                <span class="stat-number">{{ globalStats.totalPrograms }}</span>
              </div>
              <div class="stat-box-large">
                <span class="stat-label">ALUNOS ATIVOS</span>
                <span class="stat-number">{{ globalStats.totalStudents }}</span>
              </div>
              <div class="stat-box-large">
                <span class="stat-label">INSTITUIÇÕES PARCEIRAS</span>
                <span class="stat-number">{{ institutions.length }}</span>
              </div>
            </div>

            <div class="states-section">
              <h3 class="subsection-title">Estados com Entidades</h3>
              <div class="state-badges-vertical">
                <span
                  v-for="uf in activeStates"
                  :key="uf"
                  class="state-badge-compact"
                  :style="{ background: getStateColor(uf) }"
                  @click="selectState(uf)"
                >
                  {{ stateNames[uf] }}
                </span>
              </div>
            </div>

            <div class="activity-timeline-section">
              <h3 class="subsection-title">Atividade Recente</h3>
              <div class="timeline">
                <div v-for="(activity, index) in recentActivities" :key="index" class="timeline-item">
                  <div class="timeline-dot" :style="{ background: getStateColor(activity.state) }"></div>
                  <div class="timeline-content">
                    <p class="timeline-text">{{ activity.text }}</p>
                    <span class="timeline-time">{{ activity.time }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- VISÃO DO ESTADO SELECIONADO -->
          <template v-else>
            <!-- Cabeçalho do estado -->
            <div class="state-detail-header">
              <div class="state-detail-title-row">
                <span
                  class="state-detail-badge"
                  :style="{ background: getStateColor(selectedState) }"
                >
                  {{ selectedState }}
                </span>
                <h2 class="residences-title">{{ stateNames[selectedState] }}</h2>
              </div>
              <button class="btn-clear-state" @click="selectedState = null">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
                Limpar seleção
              </button>
            </div>

            <!-- Stats do estado -->
            <div class="stats-grid-3">
              <div class="stat-box-large" :style="{ borderColor: getStateColor(selectedState) + '44' }">
                <span class="stat-label">PROGRAMAS</span>
                <span class="stat-number" :style="{ color: getStateColor(selectedState) }">
                  {{ stateStats.totalPrograms }}
                </span>
              </div>
              <div class="stat-box-large" :style="{ borderColor: getStateColor(selectedState) + '44' }">
                <span class="stat-label">ALUNOS</span>
                <span class="stat-number" :style="{ color: getStateColor(selectedState) }">
                  {{ stateStats.totalStudents }}
                </span>
              </div>
              <div class="stat-box-large" :style="{ borderColor: getStateColor(selectedState) + '44' }">
                <span class="stat-label">INSTITUIÇÕES</span>
                <span class="stat-number" :style="{ color: getStateColor(selectedState) }">
                  {{ stateStats.institutionCount }}
                </span>
              </div>
            </div>

            <!-- Instituições do estado -->
            <div class="states-section" v-if="stateStats.institutionNames && stateStats.institutionNames.length">
              <h3 class="subsection-title">Instituições</h3>
              <div class="institution-list">
                <div
                  v-for="inst in stateStats.institutionNames"
                  :key="inst"
                  class="institution-item"
                  :style="{ borderLeftColor: getStateColor(selectedState) }"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                    <polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                  {{ inst }}
                </div>
              </div>
            </div>

            <!-- Turmas do estado -->
            <div class="states-section" v-if="stateStats.classNames && stateStats.classNames.length">
              <h3 class="subsection-title">Turmas</h3>
              <div class="class-list">
                <div
                  v-for="cls in stateStats.classNames"
                  :key="cls"
                  class="class-item"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                  {{ cls }}
                </div>
              </div>
            </div>

            <!-- Sem dados -->
            <div v-if="stateStats.totalPrograms === 0" class="empty-state-info">
              <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              <p>Nenhum dado encontrado para {{ stateNames[selectedState] }}.</p>
            </div>
          </template>

        </div>
      </div>

    </div>

    <!-- Modal Zoom do Mapa -->
    <Teleport to="body">
      <div v-if="mapModalOpen" class="map-modal-overlay" @click.self="mapModalOpen = false">
        <div class="map-modal">
          <div class="map-modal-header">
            <span class="map-modal-title">Selecione um Estado</span>
            <button class="btn-close-modal" @click="mapModalOpen = false">✕</button>
          </div>
          <div class="map-modal-body">
            <BrazilMap
              :activeStates="activeStates"
              :selectedState="selectedState"
              :stateColors="stateColors"
              @select="selectStateFromModal"
            />
          </div>
          <div class="map-modal-footer">
            <div class="state-badges">
              <span
                v-for="uf in activeStates"
                :key="uf"
                class="state-badge"
                :style="{ background: getStateColor(uf), outline: selectedState === uf ? '3px solid #1F285F' : 'none' }"
                @click="selectStateFromModal(uf)"
              >
                {{ stateNames[uf] }}
              </span>
            </div>
            <p v-if="!selectedState" class="hint-text">Clique em um estado ativo para filtrar</p>
            <p v-else class="hint-text selected-hint">
              Estado selecionado:
              <strong :style="{ color: getStateColor(selectedState) }">{{ stateNames[selectedState] }}</strong>
              — <span class="link-clear" @click="selectedState = null; mapModalOpen = false">limpar seleção</span>
            </p>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { programService } from '@/services/programService';
import { classService } from '@/services/classService';
import { institutionService } from '@/services/institutionService';
import { logService } from '@/services/logService';
import BrazilMap from '@/components/dashboard/BrazilMap.vue';

export default {
  name: 'HomeView',
  components: { BrazilMap },
  setup() {
    const loading = ref(true);
    const programs = ref([]);
    const classes = ref([]);
    const institutions = ref([]);
    const selectedState = ref(null);
    const mapModalOpen = ref(false);

    const stateNames = {
      AC: 'Acre', AL: 'Alagoas', AM: 'Amazonas', AP: 'Amapá',
      BA: 'Bahia', CE: 'Ceará', DF: 'Distrito Federal', ES: 'Espírito Santo',
      GO: 'Goiás', MA: 'Maranhão', MG: 'Minas Gerais', MS: 'Mato Grosso do Sul',
      MT: 'Mato Grosso', PA: 'Pará', PB: 'Paraíba', PE: 'Pernambuco',
      PI: 'Piauí', PR: 'Paraná', RJ: 'Rio de Janeiro', RN: 'Rio Grande do Norte',
      RO: 'Rondônia', RR: 'Roraima', RS: 'Rio Grande do Sul', SC: 'Santa Catarina',
      SE: 'Sergipe', SP: 'São Paulo', TO: 'Tocantins'
    };

    const stateColors = {
      AL: '#27ae60', BA: '#1abc9c', MG: '#2980b9', PR: '#e74c3c',
      SP: '#8e44ad', RJ: '#f39c12', CE: '#16a085', PE: '#d35400',
      RS: '#c0392b', SC: '#2c3e50'
    };

    const logs = ref([]);

    const formatTimeAgo = (date) => {
      if (!date) return 'agora';
      const now = new Date();
      const diff = now - new Date(date);
      const seconds = Math.floor(diff / 1000);
      const minutes = Math.floor(seconds / 60);
      const hours = Math.floor(minutes / 60);
      const days = Math.floor(hours / 24);
      
      if (seconds < 60) return 'agora';
      if (minutes < 60) return `há ${minutes}m`;
      if (hours < 24) return `há ${hours}h`;
      return `há ${days}d`;
    };

    const getActivityDescription = (log) => {
      // Parse do details se for string
      let details = {};
      if (typeof log.details === 'string') {
        try {
          details = JSON.parse(log.details);
        } catch (e) {
          details = {};
        }
      } else {
        details = log.details || {};
      }
      
      const description = log.description || '';
      
      // Extrai o nome entre aspas simples ou duplas
      const extractName = (text) => {
        // Procura por padrões: 'nome' ou "nome"
        let match = text.match(/'([^']*)'/);
        if (match) return match[1];
        match = text.match(/"([^"]*)"/);
        if (match) return match[1];
        return '';
      };
      
      // Tenta puxar o nome de várias fontes
      const name = details.entityName || details.name || details.personName || details.fullName || extractName(description) || '';
      
      // Se a description já tem o nome, usa ela diretamente mas formata melhor
      if (description.includes('People') && description.includes('criado')) {
        const extracted = extractName(description);
        return extracted ? `Pessoa "${extracted}" adicionada ao sistema` : description;
      }
      if (description.includes('People') && description.includes('excluído')) {
        const extracted = extractName(description);
        return extracted ? `Pessoa "${extracted}" foi removida do sistema` : description;
      }
      if (description.includes('People') && description.includes('atualizado')) {
        const extracted = extractName(description);
        return extracted ? `Dados de "${extracted}" foram atualizados` : description;
      }
      
      // Se temos um nome específico, usa as descrições customizadas
      if (name) {
        const descriptions = {
          'PROGRAM_CREATE': `Programa "${name}" adicionado em ${details.location?.state || details.state || ''}`,
          'PROGRAM_UPDATE': `Programa "${name}" foi atualizado`,
          'PROGRAM_DELETE': `Programa "${name}" foi removido`,
          
          'CLASS_CREATE': `Turma "${name}" criada na ${details.location?.acronym || details.institution || 'instituição'}`,
          'CLASS_UPDATE': `Turma "${name}" foi atualizada`,
          'CLASS_DELETE': `Turma "${name}" foi removida`,
          
          'PEOPLE_CREATE': `Pessoa "${name}" adicionada ao sistema`,
          'PEOPLE_UPDATE': `Dados de "${name}" foram atualizados`,
          'PEOPLE_DELETE': `Pessoa "${name}" foi removida do sistema`,
          'PEOPLE_IMPORT': `${details.quantity || details.count || '?'} pessoas importadas com sucesso`,
          
          'INSTITUTION_CREATE': `Instituição "${name}" cadastrada em ${details.location?.state || details.state || ''}`,
          'INSTITUTION_UPDATE': `Instituição "${name}" foi atualizada`,
          'INSTITUTION_DELETE': `Instituição "${name}" foi removida`,
          
          'ENROLLMENT_CREATE': `Matrícula realizada para ${details.quantity || details.count || 1} ${(details.quantity || details.count || 1) > 1 ? 'alunos' : 'aluno'}`,
          'ENROLLMENT_UPDATE': `Matrícula foi atualizada`,
          'ENROLLMENT_DELETE': `Matrícula foi removida`,
          
          'STAGE_CREATE': `Etapa "${name}" foi criada`,
          'STAGE_UPDATE': `Etapa "${name}" foi atualizada`,
          'STAGE_DELETE': `Etapa "${name}" foi removida`,
          'STAGE_CANDIDATES_IMPORT': `${details.quantity || details.count || '?'} candidatos importados para a etapa`,
          
          'USER_LOGIN': `Acesso ao sistema realizado`,
          'USER_LOGOUT': `Saída do sistema`,
          'USER_REGISTER': `Nova conta criada`,
          
          'SYSTEM_ERROR': `Erro no sistema: ${log.description || 'erro desconhecido'}`
        };
        
        if (descriptions[log.action]) {
          let finalDescription = descriptions[log.action];
          return finalDescription.charAt(0).toUpperCase() + finalDescription.slice(1);
        }
      }
      
      // Fallback: usa a description do backend se tudo mais falhar
      let finalDescription = description || 'Ação no sistema';
      return finalDescription.charAt(0).toUpperCase() + finalDescription.slice(1);
    };

    const getStateFromLog = (log) => {
      return log.details?.state || log.details?.location?.state || 'SP';
    };

    const recentActivities = computed(() => {
      if (!logs.value || logs.value.length === 0) return [];
      
      return logs.value
        .slice(0, 6)
        .map(log => ({
          text: getActivityDescription(log),
          state: getStateFromLog(log),
          time: formatTimeAgo(log.createdAt)
        }));
    });

    const loadLogs = async () => {
      try {
        const response = await logService.getLogs({
          page: 0,
          size: 10,
          sortBy: 'createdAt',
          sortDirection: 'DESC'
        });
        logs.value = response.content || response || [];
      } catch (err) {
        console.error('Erro ao carregar logs da home:', err);
        logs.value = [];
      }
    };

    const activeStates = computed(() => {
      const states = new Set();
      classes.value.forEach(c => {
        if (c.location?.state) states.add(c.location.state);
      });
      if (states.size === 0) return ['AL', 'BA', 'MG', 'PR'];
      return Array.from(states);
    });

    const globalStats = computed(() => {
      const totalStudents = classes.value.reduce((acc, c) => acc + (c.enrollmentCount || 0), 0);
      return {
        totalPrograms: programs.value.length,
        totalStudents,
        stage1: Math.ceil(programs.value.length * 0.33),
        stage2: programs.value.length - Math.ceil(programs.value.length * 0.33)
      };
    });

    const stateStats = computed(() => {
      if (!selectedState.value) return {};
      const uf = selectedState.value;
      const stateClasses = classes.value.filter(c => c.location?.state === uf);
      const stateInstitutions = institutions.value.filter(i => i.state === uf);
      const totalStudents = stateClasses.reduce((acc, c) => acc + (c.enrollmentCount || 0), 0);
      const statePrograms = new Set(stateClasses.map(c => c.program?.id).filter(Boolean)).size;
      const institutionNames = stateInstitutions.map(i => i.name || i.acronym).filter(Boolean);
      const classNames = stateClasses.map(c => c.name || c.code).filter(Boolean);
      return {
        totalPrograms: statePrograms || stateClasses.length,
        totalStudents,
        institutionCount: stateInstitutions.length,
        institutionNames,
        classNames
      };
    });

    const getStateColor = (uf) => stateColors[uf] || '#667eea';

    const selectState = (uf) => {
      if (!activeStates.value.includes(uf)) return;
      selectedState.value = selectedState.value === uf ? null : uf;
    };

    const selectStateFromModal = (uf) => {
      if (!activeStates.value.includes(uf)) return;
      selectedState.value = selectedState.value === uf ? null : uf;
      mapModalOpen.value = false;
    };

    const loadData = async () => {
      loading.value = true;
      try {
        const [progs, cls, insts] = await Promise.all([
          programService.getAll(),
          classService.getAll(),
          institutionService.getAll()
        ]);
        programs.value = Array.isArray(progs) ? progs : [];
        classes.value = Array.isArray(cls) ? cls : [];
        institutions.value = Array.isArray(insts) ? insts : [];
        
        // Carregar logs para atividades recentes
        await loadLogs();
      } catch (err) {
        console.error('Erro ao carregar home:', err);
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      loadData();
    });

    return {
      loading, programs, classes, institutions, logs,
      selectedState, mapModalOpen, recentActivities,
      stateNames, stateColors,
      activeStates, globalStats, stateStats,
      getStateColor, selectState, selectStateFromModal
    };
  }
};
</script>

<style scoped>
.home {
  padding: 28px 32px;
  max-width: 1400px;
  margin: 0 auto;
  background: #eaf1fb;
  min-height: 100vh;
}
.home-header h1 {
  color: #1F285F;
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 24px;
}

/* Loading */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px;
  gap: 16px;
  color: #666;
}
.spinner {
  width: 40px; height: 40px;
  border: 3px solid #dde6f0;
  border-top-color: #1F285F;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Map + Stats */
.map-stats-section {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 20px;
  margin-bottom: 32px;
}

/* Mapa + Residências Side by Side (60/40) */
.map-residences-section {
  display: grid;
  grid-template-columns: 6fr 4fr;
  gap: 24px;
  margin-bottom: 40px;
  align-items: stretch;
}

.map-card-centered {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(31,40,95,0.08);
  width: 100%;
  display: flex;
  flex-direction: column;
}

.map-card-centered .map-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.map-hint-text {
  text-align: center;
  color: #888;
  font-size: 13px;
  margin-top: 16px;
  margin-bottom: 0;
}

.residences-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(31,40,95,0.08);
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

.residences-title {
  font-size: 18px;
  font-weight: 700;
  color: #1F285F;
  margin: 0;
}

.subsection-title {
  font-size: 15px;
  font-weight: 700;
  color: #1F285F;
  margin: 0;
}

.stats-grid-3 {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.stat-box-large {
  border: 1.5px solid #dde6f0;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-box-large .stat-label {
  font-size: 14px;
  font-weight: 600;
  color: #888;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.stat-box-large .stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1F285F;
  line-height: 1;
}

.states-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.states-title {
  font-size: 13px;
  font-weight: 600;
  color: #888;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  margin: 0;
}

.state-badges-vertical {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.state-badge-compact {
  padding: 5px 10px;
  border-radius: 16px;
  color: white;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.2s;
}

.state-badge-compact:hover {
  opacity: 0.85;
  transform: translateY(-1px);
}

/* Timeline / Atividade Recente */
.activity-timeline-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  overflow-y: auto;
  padding-right: 6px;
}

.timeline::-webkit-scrollbar {
  width: 4px;
}

.timeline::-webkit-scrollbar-track {
  background: transparent;
}

.timeline::-webkit-scrollbar-thumb {
  background: #dde6f0;
  border-radius: 2px;
}

.timeline-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 3px;
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.timeline-text {
  font-size: 15px;
  font-weight: 500;
  color: #1F285F;
  margin: 0;
  line-height: 1.3;
}

.timeline-time {
  font-size: 13px;
  color: #888;
  font-weight: 400;
}

/* Seção Residências */
.residences-section {
  margin-bottom: 32px;
}

.residences-container {
  display: flex;
  justify-content: center;
}

/* Map card */
.map-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(31,40,95,0.08);
}
.map-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.btn-zoom {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #eaf1fb;
  border: 1.5px solid #c8d8ee;
  border-radius: 8px;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #1F285F;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-zoom:hover { background: #d5e4f5; }

/* Stats Card */
.stats-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(31,40,95,0.08);
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 100%;
}
.stats-title {
  font-size: 20px;
  font-weight: 700;
  color: #1F285F;
  margin: 0;
}
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.stat-box {
  border: 1.5px solid #dde6f0;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.stat-label {
  font-size: 11px;
  font-weight: 600;
  color: #888;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #1F285F;
  line-height: 1;
}
.state-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.state-badge {
  padding: 6px 14px;
  border-radius: 20px;
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.2s;
}
.state-badge:hover { opacity: 0.85; transform: translateY(-1px); }
.hint-text {
  color: #888;
  font-size: 13px;
  text-align: center;
  margin: 0;
}
.btn-clear-state {
  background: none;
  border: 1.5px solid #dde6f0;
  border-radius: 8px;
  padding: 8px 16px;
  color: #1F285F;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  align-self: flex-start;
  transition: all 0.2s;
}
.btn-clear-state:hover { background: #eaf1fb; }

/* Stage Sections */
.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1F285F;
  margin-bottom: 12px;
}
.stage-section { margin-bottom: 32px; }
.stage-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(31,40,95,0.08);
}

/* Modal */
.map-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(10, 20, 60, 0.45);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.map-modal {
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 48px rgba(31,40,95,0.22);
  width: 90vw;
  max-width: 1100px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.25s ease;
  overflow: hidden;
}
@keyframes slideUp {
  from { transform: translateY(24px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.map-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px;
  border-bottom: 1.5px solid #eaf1fb;
}
.map-modal-title {
  font-size: 17px;
  font-weight: 700;
  color: #1F285F;
}
.btn-close-modal {
  background: #eaf1fb;
  border: none;
  border-radius: 8px;
  width: 32px; height: 32px;
  font-size: 14px;
  color: #1F285F;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-close-modal:hover { background: #d5e4f5; }

.map-modal-body {
  padding: 8px 16px;
  flex: 1;
  overflow: auto;
}

.map-modal-footer {
  padding: 16px 24px 20px;
  border-top: 1.5px solid #eaf1fb;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.selected-hint strong { font-weight: 700; }
.link-clear {
  color: #2980b9;
  cursor: pointer;
  text-decoration: underline;
  font-size: 13px;
}

/* Estado selecionado — cabeçalho */
.state-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.state-detail-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.state-detail-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.btn-clear-state {
  display: flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: 1.5px solid #dde6f0;
  border-radius: 8px;
  padding: 6px 12px;
  color: #666;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.btn-clear-state:hover {
  background: #ffebee;
  border-color: #ef9a9a;
  color: #c62828;
}

/* Listas de instituições e turmas */
.institution-list,
.class-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 160px;
  overflow-y: auto;
  padding-right: 4px;
}

.institution-list::-webkit-scrollbar,
.class-list::-webkit-scrollbar {
  width: 3px;
}
.institution-list::-webkit-scrollbar-thumb,
.class-list::-webkit-scrollbar-thumb {
  background: #dde6f0;
  border-radius: 2px;
}

.institution-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8fafd;
  border-left: 3px solid #1F285F;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #1F285F;
  transition: background 0.15s;
}
.institution-item:hover { background: #eaf1fb; }
.institution-item svg { color: #0288d1; flex-shrink: 0; }

.class-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  background: #f8fafd;
  border-radius: 6px;
  font-size: 13px;
  color: #444;
  transition: background 0.15s;
}
.class-item:hover { background: #eaf1fb; }
.class-item svg { color: #888; flex-shrink: 0; }

/* Empty state */
.empty-state-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px;
  color: #aaa;
  text-align: center;
}
.empty-state-info p {
  margin: 0;
  font-size: 14px;
}

/* Responsive */
@media (max-width: 900px) {
  .map-stats-section { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: 1fr 1fr; }
}
</style>
