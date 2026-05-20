<template>
  <div class="stage-chart">
    <!-- Legenda -->
    <div class="chart-legend">
      <span class="legend-dot" :style="{ background: color }"></span>
      <span class="legend-label">{{ stageLabel }}</span>
      <span v-if="secondary" class="legend-dot secondary"></span>
      <span v-if="secondary" class="legend-label">Avaliação X</span>
    </div>
    <div class="chart-content">
      <!-- Gráfico de barras -->
      <div class="bars-section">
        <div class="filter-row">
          <select class="filter-select">
            <option>xxxxxxx</option>
          </select>
        </div>
        <div v-if="classes.length === 0" class="no-data">
          Nenhuma turma disponível
        </div>
        <div v-else class="bars-list">
          <div
            v-for="(item, index) in chartItems"
            :key="index"
            class="bar-row clickable"
            @click="selectClass(item)"
          >
            <span class="bar-label">{{ item.label }}</span>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: item.progress + '%', background: color }"></div>
              <span v-if="secondary" class="bar-date secondary-date" :style="{ left: item.progress2 + '%' }">{{ item.date }}</span>
              <span class="bar-dot" :style="{ left: item.progress + '%', background: color }"></span>
              <span v-if="secondary" class="bar-dot secondary-dot" :style="{ left: item.progress2 + '%' }"></span>
            </div>
          </div>
          <div class="x-axis">
            <span v-for="n in [10,20,30,40,50,60,70,80,90,100]" :key="n" class="axis-label">{{ n }}%</span>
          </div>
        </div>
      </div>
      <!-- Tabela de ranking -->
      <div class="ranking-section">
        <div class="ranking-header">
          <button class="btn-classify">
            Classificação
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            </svg>
          </button>
          <button class="btn-filter">
            Filtros
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
            </svg>
          </button>
        </div>
        <div class="ranking-table">
          <div class="ranking-head">
            <span>TURMA</span>
            <span>%</span>
          </div>
          <div
            v-for="(item, index) in rankingItems"
            :key="index"
            class="ranking-row clickable"
            :class="{ 'row-even': index % 2 === 0 }"
            @click="selectClass(item)"
          >
            <span class="turma-name">{{ item.name }}</span>
            <span class="turma-pct">{{ item.pct }}%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

export default {
  name: 'StageChart',
  props: {
    classes: { type: Array, default: () => [] },
    stageLabel: { type: String, default: 'Avaliação' },
    color: { type: String, default: '#1F285F' },
    secondary: { type: Boolean, default: false }
  },
  setup(props) {
    const router = useRouter();

    const chartItems = computed(() => {
      if (props.classes.length === 0) {
        return [
          { label: 'BA\nUFBA', progress: 95, progress2: 60, date: 'dd/mm/aaaa', classId: null, name: 'BA - UFBA' },
          { label: 'AL\nUFAL', progress: 55, progress2: 40, date: 'dd/mm/aaaa', classId: null, name: 'AL - UFAL' },
          { label: 'MG\nUFMG', progress: 10, progress2: 5,  date: 'dd/mm/aaaa', classId: null, name: 'MG - UFMG' },
          { label: 'PR\nUFPR', progress: 35, progress2: 20, date: 'dd/mm/aaaa', classId: null, name: 'PR - UFPR' },
        ];
      }
      return props.classes.map((c, i) => {
        const uf = c.location?.state || '??';
        const acronym = c.location?.acronym || c.code || `T${i + 1}`;
        return {
          label: `${uf}\n${acronym}`,
          name: `${uf} - ${acronym}`,
          classId: c.id,
          programId: c.program?.id,
          progress: Math.floor(Math.random() * 80) + 10,
          progress2: Math.floor(Math.random() * 50) + 5,
          date: c.endDate ? new Date(c.endDate).toLocaleDateString('pt-BR') : 'dd/mm/aaaa'
        };
      });
    });

    const rankingItems = computed(() => {
      if (props.classes.length === 0) {
        return [
          { name: 'BA - UFBA', pct: 98, classId: null },
          { name: 'AL - UFAL', pct: 98, classId: null },
          { name: 'TURMA X',   pct: 98, classId: null },
          { name: 'TURMA X',   pct: 98, classId: null },
          { name: 'TURMA X',   pct: 98, classId: null },
          { name: 'TURMA X',   pct: 98, classId: null },
        ];
      }
      return props.classes.map((c, i) => ({
        name: c.location?.acronym ? `${c.location.state} - ${c.location.acronym}` : c.code,
        pct: Math.floor(Math.random() * 20) + 80,
        classId: c.id,
        programId: c.program?.id
      })).sort((a, b) => b.pct - a.pct);
    });

    const selectClass = (item) => {
      if (!item.classId) return;
      router.push({
        name: 'ClassDetails',
        params: {
          programId: item.programId,
          classId: item.classId
        },
        query: {
          tab: 'etapas',
          subTab: 'nivelamento'
        },
      });
    };

    return { chartItems, rankingItems, selectClass };
  }
};
</script>

<style scoped>
.stage-chart { width: 100%; }
.chart-legend {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  justify-content: flex-end;
}
.legend-dot { width: 14px; height: 14px; border-radius: 3px; }
.legend-dot.secondary { background: #667eea; }
.legend-label { font-size: 13px; color: #444; }
.chart-content {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 24px;
}
.filter-row { margin-bottom: 16px; }
.filter-select {
  border: 1px solid #dde6f0;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 13px;
  color: #444;
  background: white;
}
.bars-list { display: flex; flex-direction: column; gap: 12px; }
.bar-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.clickable {
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s;
}
.bar-row.clickable:hover { background: #f0f6ff; }
.bar-label {
  font-size: 11px;
  color: #555;
  font-weight: 600;
  width: 52px;
  text-align: right;
  white-space: pre-line;
  line-height: 1.3;
  flex-shrink: 0;
}
.bar-track {
  flex: 1;
  height: 22px;
  background: #eaf1fb;
  border-radius: 4px;
  position: relative;
  overflow: visible;
}
.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
}
.bar-date {
  position: absolute;
  right: -72px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 10px;
  color: #888;
  white-space: nowrap;
}
.secondary-date {
  position: absolute;
  top: -18px;
  right: auto;
  transform: none;
  font-size: 10px;
  color: #888;
}
.bar-dot {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translate(50%, -50%);
  width: 10px; height: 10px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 1px 4px rgba(0,0,0,0.2);
}
.secondary-dot { background: #667eea !important; top: 50%; }
.x-axis {
  display: flex;
  justify-content: space-between;
  padding-left: 64px;
  margin-top: 8px;
}
.axis-label { font-size: 10px; color: #aaa; }
.ranking-section { display: flex; flex-direction: column; height: 100%; }
.ranking-header {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-bottom: 10px;
}
.btn-classify, .btn-filter {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: opacity 0.2s;
}
.btn-classify { background: #1F285F; color: white; }
.btn-filter { background: #eaf1fb; color: #1F285F; border: 1px solid #dde6f0; }
.btn-classify:hover, .btn-filter:hover { opacity: 0.85; }
.ranking-table { border-radius: 10px; overflow: hidden; border: 1px solid #dde6f0; }
.ranking-head {
  display: flex;
  justify-content: space-between;
  padding: 8px 16px;
  font-size: 11px;
  font-weight: 700;
  color: #888;
  background: #f5f8ff;
  text-transform: uppercase;
}
.ranking-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-top: 1px solid #eaf1fb;
  font-size: 13px;
}
.ranking-row.clickable:hover { background: #eaf1fb !important; }
.row-even { background: #fafcff; }
.turma-name { color: #1F285F; font-weight: 600; }
.turma-pct { color: #1F285F; font-weight: 700; }
.no-data {
  text-align: center;
  color: #aaa;
  padding: 32px;
  font-size: 14px;
}
@media (max-width: 768px) {
  .chart-content { grid-template-columns: 1fr; }
}
</style>