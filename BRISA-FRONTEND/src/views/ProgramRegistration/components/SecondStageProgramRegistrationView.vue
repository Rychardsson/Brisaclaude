<template>
  <div class="step-container">
    
    <div class="step-header">
       <h2>Etapa 1 — Nivelamento</h2>
       <p>Configure os cursos, avaliações e critérios de aprovação</p>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Configuração Geral</h3>
          <p>Dados principais da etapa de nivelamento</p>
       </div>
       
       <div class="form-row two-cols">
         <div class="form-group">
           <label>Nome da Etapa</label>
           <input v-model="nivelamentoForm.title" type="text" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Modalidade</label>
           <select v-model="nivelamentoForm.modality" class="form-input form-select">
             <option value="Online">Online</option>
             <option value="Presencial">Presencial</option>
             <option value="Híbrida">Híbrida</option>
             <option value="Remota Assíncrona">Remota Assíncrona</option>
           </select>
         </div>
       </div>

       <div class="form-row three-cols-special">
         <div class="form-group">
           <label>Carga Horária Total</label>
           <input v-model="nivelamentoForm.workload" type="text" class="form-input"/>
         </div>
         
         <div class="form-group relative">
           <label>Data de Início</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.nivStart" @input="$emit('parse-date-input', 'nivStart')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'nivStart')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           
           <div v-if="activeDatePicker === 'nivStart'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('nivStart', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
         
         <div class="form-group relative">
           <label>Data de Fim</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.nivEnd" @input="$emit('parse-date-input', 'nivEnd')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'nivEnd')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           
           <div v-if="activeDatePicker === 'nivEnd'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('nivEnd', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
       </div>

       <div class="form-group" style="margin-bottom: 0;">
         <label>Plataforma Utilizada</label>
         <input v-model="nivelamentoForm.platform" type="text" placeholder="Ex: Moodle, Google Classroom..." class="form-input"/>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title" style="margin-bottom: 8px;">
          <h3>Cursos do Nivelamento</h3>
          <p>Configure os cursos obrigatórios e opcionais</p>
       </div>
       
       <div style="font-size: 13px; margin-bottom: 16px;">
          <span style="color: #0d9488; font-weight: 500;">{{ reqCoursesCount }} obrigatórios</span> 
          <span style="color: #d1d5db; margin: 0 8px;">|</span> 
          <span style="color: #6b7280;">{{ optCoursesCount }} opcionais</span>
       </div>

       <div class="course-table">
          <div class="course-header">
             <span>Curso</span>
             <span>C.H.</span>
             <span>Obrigatório</span>
             <span>Pontua</span>
             <span>Status</span>
          </div>
          
          <div v-for="(course, idx) in nivelamentoForm.courses" :key="idx" class="course-row">
             <span class="course-name">{{ course.name }}</span>
             <span class="course-hours">{{ course.hours }}</span>
             
             <label class="toggle-switch">
               <input type="checkbox" v-model="course.required" class="toggle-input">
               <span class="toggle-slider"></span>
             </label>
             
             <label class="toggle-switch">
               <input type="checkbox" v-model="course.scores" class="toggle-input">
               <span class="toggle-slider"></span>
             </label>
             
             <div>
               <span class="badge-active">{{ course.status }}</span>
             </div>
          </div>
       </div>
       
       <button class="btn-dashed-add" style="margin-top: 16px;" @click="$emit('open-new-course-modal')">
         <span>+</span> Adicionar Novo Curso
       </button>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Regra de Conclusão dos Cursos</h3>
          <p>Defina quando um curso é considerado concluído</p>
       </div>
       
       <div class="list-item-row toggle-row-simple">
         <span class="list-item-title">Exigir conclusão de atividades</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.completionRules.requireActivities" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
       
       <div class="list-item-row toggle-row-simple" :style="nivelamentoForm.completionRules.requireMinScore ? 'border-bottom: none; padding-bottom: 8px;' : ''">
         <span class="list-item-title">Exigir acerto mínimo em exercícios</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.completionRules.requireMinScore" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
       
       <div v-if="nivelamentoForm.completionRules.requireMinScore" class="form-group" style="margin-top: 0; padding-bottom: 16px; border-bottom: 1px solid #f3f4f6;">
         <input v-model="nivelamentoForm.completionRules.minScoreValue" type="text" class="form-input" style="background-color: #f9fafb;"/>
       </div>

       <div class="list-item-row toggle-row-simple" style="margin-bottom: 0; border: none; padding-bottom: 0; margin-top: 16px;">
         <span class="list-item-title">Exigir avaliação final do curso</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.completionRules.requireFinalEval" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Prova Final</h3>
          <p>Configuração da avaliação final da etapa</p>
       </div>
       
       <div class="green-toggle-box">
          <span class="list-item-title" style="color: #065f46;">Habilitar prova final</span>
          <label class="toggle-switch">
            <input type="checkbox" v-model="nivelamentoForm.finalExam.active" class="toggle-input">
            <span class="toggle-slider"></span>
          </label>
       </div>
       
       <div class="form-row three-cols-special" style="margin-top: 24px; grid-template-columns: 1.5fr 1fr 1fr;">
         
         <div class="form-group relative">
           <label>Data</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.nivExamDate" @input="$emit('parse-date-input', 'nivExamDate')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10" :disabled="!nivelamentoForm.finalExam.active"/>
             <svg v-if="nivelamentoForm.finalExam.active" @click="$emit('open-date-picker', 'nivExamDate')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           
           <div v-if="activeDatePicker === 'nivExamDate'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('nivExamDate', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
         
         <div class="form-group relative">
           <label>Horário</label>
           <input 
             v-model="nivelamentoForm.finalExam.time" 
             @input="$emit('parse-time-input')"
             type="text" 
             placeholder="00:00" 
             maxlength="5"
             class="form-input" 
             :disabled="!nivelamentoForm.finalExam.active"
           />
         </div>

         <div class="form-group">
           <label>Duração (minutos)</label>
           <input v-model="nivelamentoForm.finalExam.duration" type="number" class="form-input" :disabled="!nivelamentoForm.finalExam.active"/>
         </div>
       </div>

       <div class="form-row two-cols">
         <div class="form-group">
           <label>Quantidade de Questões</label>
           <input v-model="nivelamentoForm.finalExam.questionsCount" type="number" min="40" max="40" readonly class="form-input" :disabled="!nivelamentoForm.finalExam.active"/>
         </div>
         <div class="form-group">
           <label>Tipo</label>
           <select v-model="nivelamentoForm.finalExam.type" class="form-input form-select" :disabled="!nivelamentoForm.finalExam.active">
             <option value="Múltipla Escolha">Múltipla Escolha</option>
             <option value="Dissertativa">Dissertativa</option>
           </select>
         </div>
       </div>

       <div class="list-item-row toggle-row-simple" style="border: none; padding: 0 0 16px 0;">
         <span class="list-item-title">Questões apresentadas sequencialmente</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.finalExam.sequential" class="toggle-input" :disabled="!nivelamentoForm.finalExam.active">
           <span class="toggle-slider"></span>
         </label>
       </div>
       
       <div class="list-item-row toggle-row-simple" style="border: none; padding: 0 0 16px 0;">
         <span class="list-item-title">Permitir voltar em questões anteriores</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.finalExam.allowBack" class="toggle-input" :disabled="!nivelamentoForm.finalExam.active">
           <span class="toggle-slider"></span>
         </label>
       </div>

       <div class="list-item-row toggle-row-simple" style="margin-bottom: 0; border: none; padding: 0;">
         <span class="list-item-title">Botão enviar obrigatório ao finalizar</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.finalExam.mandatorySubmit" class="toggle-input" :disabled="!nivelamentoForm.finalExam.active">
           <span class="toggle-slider"></span>
         </label>
       </div>
    </div>

    <div class="card-section" style="border: 1px solid #e5e7eb;"> 
       <div class="section-title">
          <h3 style="color: #0f172a;">Cálculo da Nota e Aprovação</h3>
          <p>Configure a fórmula de pontuação e critérios de classificação</p>
       </div>
       
       <div class="formula-box">
         <span class="formula-label">Fórmula de Cálculo</span>
         <div class="formula-text">
           Nota Final = (Nota Prova × {{ nivelamentoForm.grading.examWeight }}%) + (Cursos Opcionais × {{ nivelamentoForm.grading.optionalWeight }}%) + Bônus Localidade
         </div>
       </div>
       
       <div class="form-group" style="margin-bottom: 24px;">
         <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
           <label style="margin: 0;">Pontuação Máxima da Prova (%)</label>
           <div style="display: flex; align-items: center; gap: 6px;">
             <input 
               type="number" 
               v-model.number="nivelamentoForm.grading.examWeight" 
               @keydown.enter="$event.target.blur()"
               @input="validateWeights"
               class="quota-input" 
               style="color: #1a233a; font-weight: 500; padding: 4px 8px; width: 60px; text-align: center;" 
               min="0" max="100" 
             />
             <span style="font-size: 13px; color: #6b7280; font-weight: 500;">%</span>
           </div>
         </div>
         <input 
           type="range" 
           v-model.number="nivelamentoForm.grading.examWeight" 
           @input="validateWeights"
           class="form-range" 
           min="0" max="100"
           :style="{ background: `linear-gradient(to right, #1e1b4b ${nivelamentoForm.grading.examWeight}%, #e5e7eb ${nivelamentoForm.grading.examWeight}%)` }"
         >
       </div>

       <div class="form-group" style="margin-bottom: 24px;">
         <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
           <label style="margin: 0;">Pontos por Cursos Não Obrigatórios (%)</label>
           <div style="display: flex; align-items: center; gap: 6px;">
             <input 
               type="number" 
               v-model.number="nivelamentoForm.grading.optionalWeight" 
               @keydown.enter="$event.target.blur()"
               @input="validateWeights"
               class="quota-input" 
               style="color: #1a233a; font-weight: 500; padding: 4px 8px; width: 60px; text-align: center;" 
               min="0" max="100" 
             />
             <span style="font-size: 13px; color: #6b7280; font-weight: 500;">%</span>
           </div>
         </div>
         <input 
           type="range" 
           v-model.number="nivelamentoForm.grading.optionalWeight" 
           @input="validateWeights"
           class="form-range" 
           min="0" max="100"
           :style="{ background: `linear-gradient(to right, #1e1b4b ${nivelamentoForm.grading.optionalWeight}%, #e5e7eb ${nivelamentoForm.grading.optionalWeight}%)` }"
         >
       </div>

       <div v-if="isTotalWeightExceeded" style="padding: 12px; background-color: #fee2e2; border: 1px solid #fecaca; border-radius: 6px; margin-bottom: 24px;">
         <p style="margin: 0; color: #991b1b; font-size: 13px; font-weight: 500;">
           ⚠️ A soma da Pontuação da Prova + Cursos Não Obrigatórios não pode ultrapassar 100%<br/>
           <span style="font-size: 12px;">Total atual: <strong>{{ totalGradingWeight }}%</strong></span>
         </p>
       </div>

       <div class="form-group">
         <label>Bônus por Localidade/Residência</label>
         <input v-model="nivelamentoForm.grading.bonusLocation" type="number" class="form-input"/>
       </div>

       <div class="form-group" style="margin-bottom: 32px;">
         <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
           <label style="margin: 0;">Nota Mínima para Aprovação</label>
           <div style="display: flex; align-items: center; gap: 6px;">
             <input 
               type="number" 
               v-model="nivelamentoForm.grading.minPassingScore" 
               @keydown.enter="$event.target.blur()"
               class="quota-input" 
               style="color: #1a233a; font-weight: 500; padding: 4px 8px; width: 60px; text-align: center;" 
               min="0" max="100" 
             />
             <span style="font-size: 13px; color: #6b7280; font-weight: 500;">pontos</span>
           </div>
         </div>
         <input 
           type="range" 
           v-model="nivelamentoForm.grading.minPassingScore" 
           class="form-range" 
           min="0" max="100"
           :style="{ background: `linear-gradient(to right, #1e1b4b ${nivelamentoForm.grading.minPassingScore}%, #e5e7eb ${nivelamentoForm.grading.minPassingScore}%)` }"
         >
       </div>

       <div class="list-item-row toggle-row-simple">
         <span class="list-item-title">Regra de corte: % da maior nota da turma</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.grading.cutOffRule" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
       
       <div class="form-group">
         <label>Quantidade de Aprovados para Próxima Etapa</label>
         <input v-model="nivelamentoForm.grading.approvedCount" type="number" class="form-input"/>
       </div>

       <div class="list-item-row toggle-row-simple" style="border: none; padding: 0 0 16px 0;">
         <span class="list-item-title">Gerar lista preliminar</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.grading.generatePrelimList" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
       
       <div class="list-item-row toggle-row-simple" style="border: none; padding: 0 0 16px 0;">
         <span class="list-item-title">Permitir recurso</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.grading.allowAppeals" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>

       <div class="list-item-row toggle-row-simple" style="margin-bottom: 0; border: none; padding: 0;">
         <span class="list-item-title">Gerar lista final</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="nivelamentoForm.grading.generateFinalList" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Certificação</h3>
          <p>Configure a emissão de certificados</p>
       </div>
       
       <div class="green-toggle-box" style="margin-bottom: 24px;">
          <span class="list-item-title" style="color: #065f46;">Emitir certificado</span>
          <label class="toggle-switch">
            <input type="checkbox" v-model="nivelamentoForm.certification.active" class="toggle-input">
            <span class="toggle-slider"></span>
          </label>
       </div>

       <div class="form-group">
         <label>Critério para Certificação</label>
         <select v-model="nivelamentoForm.certification.criteria" class="form-input form-select" :disabled="!nivelamentoForm.certification.active">
           <option value="Aprovação na etapa">Aprovação na etapa</option>
           <option value="Conclusão dos cursos">Conclusão dos cursos</option>
         </select>
       </div>

       <div class="form-group" style="margin-bottom: 0;">
         <label>Texto Padrão do Certificado</label>
         <textarea v-model="nivelamentoForm.certification.defaultText" placeholder="Ex: Certificamos que concluiu com aproveitamento..." class="form-textarea" rows="2" :disabled="!nivelamentoForm.certification.active"></textarea>
       </div>
    </div>

  </div>
</template>

<script>
export default {
  // Nome oficial do componente filho que gerencia a Etapa 1 - Nivelamento
  name: 'SecondStageProgramRegistrationView',
  
  // Propriedades (Props) recebidas do componente Pai que são vitais para renderizar e controlar os inputs, modais e cálculos
  props: {
    // Objeto central do nivelamento que guarda as chaves de Cursos, Prova Final e o motor de Notas (Grading)
    nivelamentoForm: { type: Object, required: true },
    
    // Objeto de controle das datas convertidas para o layout visual (DD/MM/AAAA)
    displayDates: { type: Object, required: true },
    
    // String que avisa o sistema qual popup de data está com o foco/aberto no momento
    activeDatePicker: { type: String, default: null },
    
    // Objeto de Data (Date Javascript) que ancora o Mês e Ano do topo do calendário aberto
    calendarDate: { type: Date, required: true },
    
    // Arrays auxiliares em PT-BR para iterar e gerar os layouts da tabela de calendário numérico
    weekDays: { type: Array, required: true },
    monthNames: { type: Array, required: true },
    calendarDays: { type: Array, required: true },
    
    // Variáveis pré-calculadas pelo Pai informando os totais estatísticos dos cursos
    reqCoursesCount: { type: Number, required: true },
    optCoursesCount: { type: Number, required: true },
    
    // Funções vindas do Pai usadas pelos v-for do calendário para descobrir e pintar o botão/dia correto
    isSelectedDay: { type: Function, required: true },
    isToday: { type: Function, required: true }
  },

  data() {
    return {
      isTotalWeightExceeded: false
    }
  },

  computed: {
    totalGradingWeight() {
      const exam = Number(this.nivelamentoForm.grading.examWeight) || 0;
      const optional = Number(this.nivelamentoForm.grading.optionalWeight) || 0;
      return exam + optional;
    }
  },

  methods: {
    validateWeights() {
      this.isTotalWeightExceeded = this.totalGradingWeight > 100;
      if (this.isTotalWeightExceeded) {
        this.$emit('weights-invalid');
      } else {
        this.$emit('weights-valid');
      }
    }
  }
}
</script>

<style scoped>
/* O CSS Global do Pai fará todo o trabalho perfeitamente. Nenhuma classe extra necessária aqui! */
</style>
