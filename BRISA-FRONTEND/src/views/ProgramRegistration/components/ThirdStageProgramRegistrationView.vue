<template>
  <div class="step-container">
    
    <div class="step-header">
       <h2>Etapa 2 — Imersão</h2>
       <p>Configure projetos, avaliações, presença e benefícios</p>
    </div>

    <div class="tabs-container">
       <div class="tab-pill" :class="{ active: activeTab === 'visao-geral' }" @click="activeTab = 'visao-geral'">Visão Geral</div>
       <div class="tab-pill" :class="{ active: activeTab === 'projetos' }" @click="activeTab = 'projetos'">Projetos e Grupos</div>
       <div class="tab-pill" :class="{ active: activeTab === 'presenca' }" @click="activeTab = 'presenca'">Presença</div>
       <div class="tab-pill" :class="{ active: activeTab === 'avaliacoes' }" @click="activeTab = 'avaliacoes'">Avaliações</div>
       <div class="tab-pill" :class="{ active: activeTab === 'beneficios' }" @click="activeTab = 'beneficios'">Benefícios</div>
    </div>

    <div v-if="activeTab === 'visao-geral'" class="card-section">
       <div class="section-title">
          <h3>Configuração Geral da Imersão</h3>
          <p>Dados principais da etapa de imersão</p>
       </div>
       <div class="form-row two-cols">
         <div class="form-group">
           <label>Nome da Etapa</label>
           <input v-model="imersaoForm.nome" type="text" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Modalidade</label>
           <select v-model="imersaoForm.modalidade" class="form-input form-select">
             <option value="Híbrida">Híbrida</option>
             <option value="Presencial">Presencial</option>
             <option value="Online">Online</option>
           </select>
         </div>
       </div>
       <div v-if="imersaoForm.modalidade !== 'Online'" class="form-row two-cols">
         <div class="form-group">
           <label>Local da Imersão</label>
           <input v-model="imersaoForm.local" type="text" placeholder="Ex: Campus Principal - Sala 101" class="form-input"/>
         </div>
       </div>
       <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px;">
         <div class="form-group">
           <label>Carga Horária Total (horas)</label>
           <input v-model="imersaoForm.cargaHoraria" type="number" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Duração (meses)</label>
           <input v-model="imersaoForm.duracaoMeses" type="number" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Média de Horas Semanais</label>
           <input v-model="imersaoForm.mediaHoras" type="number" class="form-input"/>
         </div>
       </div>
       
       <div class="form-row two-cols">
         <div class="form-group relative">
           <label>Data de Início</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.imerStart" @input="$emit('parse-date-input', 'imerStart')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'imerStart')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           <div v-if="activeDatePicker === 'imerStart'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('imerStart', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
         
         <div class="form-group relative">
           <label>Data de Fim</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.imerEnd" @input="$emit('parse-date-input', 'imerEnd')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'imerEnd')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           <div v-if="activeDatePicker === 'imerEnd'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('imerEnd', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
       </div>
    </div>

    <div v-if="activeTab === 'projetos'" class="card-section">
       <div class="section-title">
          <h3>Projetos e Formação de Grupos</h3>
          <p>Configure a estrutura de projetos e equipes</p>
       </div>
       <div class="list-item-row toggle-row-simple" :style="imersaoForm.hasProjetos ? 'border-bottom: none; padding-bottom: 8px;' : ''">
         <span class="list-item-title">Projetos serão definidos nesta etapa?</span>
         <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.hasProjetos" class="toggle-input"><span class="toggle-slider"></span></label>
       </div>
       <div v-if="imersaoForm.hasProjetos" class="nested-block">
          <div class="list-item-row toggle-row-simple" style="padding-bottom: 16px;">
            <span class="list-item-title">Grupos serão montados pelo sistema/coordenação?</span>
            <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.hasGrupos" class="toggle-input"><span class="toggle-slider"></span></label>
          </div>
          <div class="form-row two-cols">
             <div class="form-group"><label>Tamanho Mínimo do Grupo</label><input v-model="imersaoForm.minGroup" type="number" class="form-input"/></div>
             <div class="form-group"><label>Tamanho Máximo do Grupo</label><input v-model="imersaoForm.maxGroup" type="number" class="form-input"/></div>
          </div>
       </div>
    </div>

    <div v-if="activeTab === 'presenca'" class="card-section">
       <div class="section-title">
          <h3>Regras de Frequência e Presença</h3>
          <p>Configure as políticas de presença e justificativas</p>
       </div>

       <div class="warning-alert-box">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
          <span>As regras de presença são aplicadas automaticamente pelo sistema e geram notificações aos alunos e coordenação.</span>
       </div>

       <div class="list-item-row toggle-row-simple">
          <span class="list-item-title">Encontros Presenciais Obrigatórios</span>
          <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.presenca.encontrosObrigatorios" class="toggle-input"><span class="toggle-slider"></span></label>
       </div>

       <div class="list-item-row toggle-row-simple" :style="imersaoForm.presenca.frequenciaSemanal ? 'border-bottom: none; padding-bottom: 0;' : ''">
          <span class="list-item-title">Reunião Semanal com Orientador</span>
          <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.presenca.frequenciaSemanal" class="toggle-input"><span class="toggle-slider"></span></label>
       </div>

       <div v-if="imersaoForm.presenca.frequenciaSemanal" class="nested-block" style="padding-top: 16px;">
          <div class="form-row">
             <div class="form-group"><label>Frequência Mínima (%)</label><input v-model="imersaoForm.presenca.minFrequencia" type="number" class="form-input"/></div>
          </div>
       </div>

       <div class="list-item-row toggle-row-simple" style="margin-top: 16px;">
          <span class="list-item-title">Contabilização de Faltas</span>
          <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.presenca.contabilizarFaltas" class="toggle-input"><span class="toggle-slider"></span></label>
       </div>

       <div class="rules-container" style="margin-top: 24px;">
          <h4 class="sub-section-label">Regras de Advertência e Exclusão</h4>
          <div class="rule-card">
             <div class="rule-text"><strong>2ª falta injustificada — Advertência</strong><p>Sistema notifica automaticamente</p></div>
             <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.presenca.regraAdvertencia" class="toggle-input"><span class="toggle-slider"></span></label>
          </div>
          <div class="rule-card">
             <div class="rule-text"><strong>3ª falta injustificada — Exclusão</strong><p>Exclusão automática do programa</p></div>
             <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.presenca.regraExclusao3" class="toggle-input"><span class="toggle-slider"></span></label>
          </div>
          <div class="rule-card">
             <div class="rule-text"><strong>6ª falta total — Exclusão</strong><p>Independente de justificativa</p></div>
             <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.presenca.regraExclusao6" class="toggle-input"><span class="toggle-slider"></span></label>
          </div>
       </div>

       <div class="form-group" style="margin-top: 24px;">
          <label>Responsável pela Análise de Justificativas</label>
          <input v-model="imersaoForm.presenca.responsavelJustificativa" type="text" placeholder="Ex: Coordenação Acadêmica" class="form-input"/>
       </div>
    </div>

    <div v-if="activeTab === 'avaliacoes'" class="card-section">
       <div class="section-title">
          <h3>Sistema de Avaliações</h3>
          <p>Configure as avaliações parcial e final com critérios múltiplos</p>
       </div>

       <div class="grading-group-header">
          <span>Avaliação Parcial</span> <span class="weight-label">Peso: {{ imersaoForm.avaliacoes.pesoParcial }}%</span>
       </div>
       <div class="criteria-list">
          <div class="criteria-item"><span>1. Nota do Grupo do Projeto</span> <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.avaliacoes.parcialCriterios.grupo" class="toggle-input"><span class="toggle-slider"></span></label></div>
          <div class="criteria-item sub"><span>Avaliação pela banca</span> <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.avaliacoes.parcialCriterios.banca" class="toggle-input"><span class="toggle-slider"></span></label></div>
          <div class="criteria-item sub"><span>Apresentação do projeto</span> <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.avaliacoes.parcialCriterios.apresentacao" class="toggle-input"><span class="toggle-slider"></span></label></div>
          <div class="criteria-item"><span>2. Nota de Participação</span> <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.avaliacoes.parcialCriterios.participacao" class="toggle-input"><span class="toggle-slider"></span></label></div>
          <div class="criteria-item"><span>3. Nota de Comportamento e Desempenho</span> <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.avaliacoes.parcialCriterios.comportamento" class="toggle-input"><span class="toggle-slider"></span></label></div>
       </div>

       <div class="grading-group-header" style="margin-top: 32px;">
          <span>Avaliação Final</span> <span class="weight-label">Peso: {{ imersaoForm.avaliacoes.pesoFinal }}%</span>
       </div>
       <div class="criteria-list">
          <div class="criteria-item"><span>1. Nota do Grupo do Projeto</span> <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.avaliacoes.finalCriterios.grupo" class="toggle-input"><span class="toggle-slider"></span></label></div>
          <div class="criteria-item sub"><span>Apresentação final</span> <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.avaliacoes.finalCriterios.apresentacao" class="toggle-input"><span class="toggle-slider"></span></label></div>
          <div class="criteria-item"><span>2. Nota de Participação</span> <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.avaliacoes.finalCriterios.participacao" class="toggle-input"><span class="toggle-slider"></span></label></div>
       </div>

       <div class="formula-box" style="margin-top: 32px; background-color: #0f172a;">
          <span class="formula-label" style="color: #94a3b8;">Fórmula de Cálculo Final</span>
          <div class="formula-text" style="color: white; font-family: monospace;">
             Nota Final = (Avaliação Parcial x {{ imersaoForm.avaliacoes.pesoParcial }}%) + (Avaliação Final x {{ imersaoForm.avaliacoes.pesoFinal }}%)
          </div>
       </div>

       <div class="form-group" style="margin-top: 24px; margin-bottom: 24px;">
         <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
           <label style="margin: 0;">Peso da Avaliação Parcial</label>
           <div style="display: flex; align-items: center; gap: 6px;">
             <input type="number" v-model.number="imersaoForm.avaliacoes.pesoParcial" @keydown.enter="$event.target.blur()" @input="validateEvaluationWeights" class="quota-input" style="color: #1a233a; font-weight: 500; padding: 4px 8px; width: 60px; text-align: center;" min="0" max="100"/>
             <span style="font-size: 13px; color: #6b7280; font-weight: 500;">%</span>
           </div>
         </div>
         <input type="range" class="form-range" v-model.number="imersaoForm.avaliacoes.pesoParcial" @input="validateEvaluationWeights" min="0" max="100" :style="{ background: `linear-gradient(to right, #1e1b4b ${imersaoForm.avaliacoes.pesoParcial}%, #e5e7eb ${imersaoForm.avaliacoes.pesoParcial}%)` }">
       </div>

       <div class="form-group" style="margin-bottom: 24px;">
         <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
           <label style="margin: 0;">Peso da Avaliação Final</label>
           <div style="display: flex; align-items: center; gap: 6px;">
             <input type="number" v-model.number="imersaoForm.avaliacoes.pesoFinal" @keydown.enter="$event.target.blur()" @input="validateEvaluationWeights" class="quota-input" style="color: #1a233a; font-weight: 500; padding: 4px 8px; width: 60px; text-align: center;" min="0" max="100"/>
             <span style="font-size: 13px; color: #6b7280; font-weight: 500;">%</span>
           </div>
         </div>
         <input type="range" class="form-range" v-model.number="imersaoForm.avaliacoes.pesoFinal" @input="validateEvaluationWeights" min="0" max="100" :style="{ background: `linear-gradient(to right, #1e1b4b ${imersaoForm.avaliacoes.pesoFinal}%, #e5e7eb ${imersaoForm.avaliacoes.pesoFinal}%)` }">
       </div>

       <div v-if="isEvaluationWeightExceeded" style="padding: 12px; background-color: #fee2e2; border: 1px solid #fecaca; border-radius: 6px; margin-bottom: 24px;">
         <p style="margin: 0; color: #991b1b; font-size: 13px; font-weight: 500;">
           ⚠️ A soma da Avaliação Parcial + Avaliação Final não pode ultrapassar 100%<br/>
           <span style="font-size: 12px;">Total atual: <strong>{{ totalEvaluationWeight }}%</strong></span>
         </p>
       </div>

       <div class="form-group" style="margin-top: 24px;">
          <label>Regra de Aprovação Final</label>
          <select v-model="imersaoForm.avaliacoes.regraAprovacao" class="form-input form-select">
            <option value="Baseado em % da maior nota">Baseado em % da maior nota</option>
            <option value="Nota mínima fixa">Nota mínima fixa</option>
          </select>
       </div>
    </div>

    <div v-if="activeTab === 'beneficios'" class="card-section">
       <div class="section-title">
          <h3>Bolsa e Auxílios</h3>
          <p>Configure os benefícios financeiros do programa</p>
       </div>

       <div class="list-item-row toggle-row-simple" :style="imersaoForm.beneficios.hasBolsa ? 'border-bottom: none;' : ''">
          <span class="list-item-title">Programa oferece bolsa?</span>
          <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.beneficios.hasBolsa" class="toggle-input"><span class="toggle-slider"></span></label>
       </div>

       <div v-if="imersaoForm.beneficios.hasBolsa" class="nested-block">
          <div class="form-row two-cols">
             <div class="form-group"><label>Valor da Bolsa (R$)</label><input v-model="imersaoForm.beneficios.valorBolsa" type="text" class="form-input"/></div>
             <div class="form-group"><label>Quantidade de Meses</label><input v-model="imersaoForm.beneficios.mesesBolsa" type="number" class="form-input"/></div>
          </div>
          <div class="list-item-row toggle-row-simple"><span class="list-item-title">Exigir conta bancária em nome do aluno</span><label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.beneficios.exigirContaBanco" class="toggle-input"><span class="toggle-slider"></span></label></div>
          <div class="list-item-row toggle-row-simple" style="border: none;"><span class="list-item-title">Exigir assinatura digital de recibo</span><label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.beneficios.exigirAssinaturaRecibo" class="toggle-input"><span class="toggle-slider"></span></label></div>
       </div>

       <div class="section-title" style="margin-top: 32px;">
          <h3>Equipamentos em Comodato</h3>
          <p>Empréstimo de equipamentos aos participantes</p>
       </div>

       <div class="list-item-row toggle-row-simple" :style="imersaoForm.beneficios.hasNotebook ? 'border-bottom: none;' : ''">
          <span class="list-item-title">Oferece notebook em comodato?</span>
          <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.beneficios.hasNotebook" class="toggle-input"><span class="toggle-slider"></span></label>
       </div>

       <div v-if="imersaoForm.beneficios.hasNotebook" class="nested-block">
          <div class="form-group"><label>Modelo/Especificação do Equipamento</label><input v-model="imersaoForm.beneficios.modeloNotebook" type="text" class="form-input" placeholder="Ex: Notebook Intel i5, 8GB RAM..."/></div>
          
          <div class="form-group relative">
            <label>Prazo de Devolução</label>
            <div class="date-input-wrapper">
              <input v-model="displayDates.prazoDevolucaoNotebook" @input="$emit('parse-date-input', 'prazoDevolucaoNotebook')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
              <svg @click="$emit('open-date-picker', 'prazoDevolucaoNotebook')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
            </div>
            <div v-if="activeDatePicker === 'prazoDevolucaoNotebook'" class="custom-calendar">
                <div class="calendar-header">
                  <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                  <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                  <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
                </div>
                <div class="calendar-grid">
                  <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                  <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('prazoDevolucaoNotebook', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
                </div>
            </div>
          </div>
       </div>

       <div class="section-title" style="margin-top: 32px;">
          <h3>Contratos e Documentação</h3>
          <p>Requisitos legais e documentais</p>
       </div>
       <div class="list-item-row toggle-row-simple" style="border-bottom: 1px solid #f3f4f6;">
          <span class="list-item-title">Exigir assinatura de contrato?</span>
          <label class="toggle-switch"><input type="checkbox" v-model="imersaoForm.beneficios.exigirContrato" class="toggle-input"><span class="toggle-slider"></span></label>
       </div>
       <div class="form-group" style="margin-top: 16px;"><label>Prazo para Assinatura dos Contratos (dias)</label><input v-model="imersaoForm.beneficios.prazoContrato" type="number" class="form-input"/></div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'ThirdStageProgramRegistrationView',
  props: {
    currentStep: { type: Number, required: true },
    imersaoForm: { type: Object, required: true },
    displayDates: { type: Object, required: true },
    activeDatePicker: { type: String, default: null },
    calendarDate: { type: Date, required: true },
    weekDays: { type: Array, required: true },
    monthNames: { type: Array, required: true },
    calendarDays: { type: Array, required: true },
    isSelectedDay: { type: Function, required: true },
    isToday: { type: Function, required: true }
  },
  data() {
    return {
      activeTab: 'visao-geral',
      isEvaluationWeightExceeded: false
    };
  },

  computed: {
    totalEvaluationWeight() {
      const parcial = Number(this.imersaoForm.avaliacoes.pesoParcial) || 0;
      const final = Number(this.imersaoForm.avaliacoes.pesoFinal) || 0;
      return parcial + final;
    }
  },

  methods: {
    validateEvaluationWeights() {
      this.isEvaluationWeightExceeded = this.totalEvaluationWeight > 100;
      if (this.isEvaluationWeightExceeded) {
        this.$emit('evaluation-weights-invalid');
      } else {
        this.$emit('evaluation-weights-valid');
      }
    }
  }
}
</script>

<style scoped>
.tabs-container { display: flex; background-color: #f3f4f6; border-radius: 8px; padding: 4px; gap: 4px; margin-bottom: 24px; }
.tab-pill { flex: 1; text-align: center; padding: 8px 16px; font-size: 14px; font-weight: 600; color: var(--slate-600); cursor: pointer; border-radius: 6px; transition: all 0.2s; }
.tab-pill.active { background-color: white; color: var(--teal-600); box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); }
.warning-alert-box { display: flex; align-items: center; gap: 12px; background-color: #fffbeb; border: 1px solid #fef3c7; color: #92400e; padding: 12px 16px; border-radius: 8px; font-size: 13px; margin-bottom: 20px; }
.rule-card { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; margin-bottom: 8px; }
.rule-text strong { display: block; font-size: 13px; color: #1a233a; }
.rule-text p { font-size: 12px; color: #6b7280; margin: 0; }
.grading-group-header { display: flex; justify-content: space-between; font-weight: 600; font-size: 14px; color: #1a233a; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid #f3f4f6; }
.weight-label { color: #0d9488; }
.criteria-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; font-size: 13px; color: #374151; }
.criteria-item.sub { padding-left: 24px; color: #6b7280; font-size: 12px; }
.nested-block { margin-top: 8px; padding-left: 20px; border-left: 2px solid #a7f3d0; }
.sub-section-label { font-size: 13px; font-weight: 600; color: #4b5563; margin-bottom: 12px; }
</style>
