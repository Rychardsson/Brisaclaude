<template>
  <div class="step-container">
    
    <div class="step-header">
       <h2>Etapa 0 — Inscrição</h2>
       <p>Configure o formulário de inscrição e elegibilidade</p>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Informações da Etapa</h3>
          <p>Dados básicos da etapa de inscrição</p>
       </div>
       
       <div class="form-row two-cols">
         <div class="form-group">
           <label>Nome da Etapa</label>
           <input v-model="inscriptionForm.title" type="text" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Tipo</label>
           <select v-model="inscriptionForm.type" class="form-input form-select">
             <option value="Inscrição / Triagem">Inscrição / Triagem</option>
             <option value="Avaliação">Avaliação</option>
           </select>
         </div>
       </div>

       <div class="form-group">
         <label>Descrição</label>
         <textarea v-model="inscriptionForm.desc" class="form-textarea" rows="2"></textarea>
       </div>

       <div class="form-row three-cols-special">
         
         <div class="form-group relative">
           <label>Início do Período</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.inscStart" @input="$emit('parse-date-input', 'inscStart')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'inscStart')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           
           <div v-if="activeDatePicker === 'inscStart'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('inscStart', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
         
         <div class="form-group relative">
           <label>Fim do Período</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.inscEnd" @input="$emit('parse-date-input', 'inscEnd')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'inscEnd')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           
           <div v-if="activeDatePicker === 'inscEnd'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('inscEnd', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
         
         <div class="form-group">
           <label>Situação</label>
           <select v-model="inscriptionForm.situation" class="form-input form-select">
             <option value="Obrigatória">Obrigatória</option>
             <option value="Opcional">Opcional</option>
           </select>
         </div>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Elegibilidade</h3>
          <p>Requisitos e regras de participação</p>
       </div>
       
       <div class="list-item-row toggle-row-simple">
         <span class="list-item-title">Exigir LinkedIn?</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="inscriptionForm.requireLinkedin" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
       
       <div class="list-item-row toggle-row-simple">
         <span class="list-item-title">Exigir CPF válido?</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="inscriptionForm.requireCPF" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>

       <div class="list-item-row toggle-row-simple" style="margin-bottom: 0;">
         <span class="list-item-title">Permitir apenas uma inscrição por pessoa?</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="inscriptionForm.singleRegistration" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Formulário de Inscrição</h3>
          <p>Campos que o candidato deverá preencher</p>
       </div>
       
       <div v-for="(field, index) in inscriptionForm.fields" :key="'field-'+index" class="list-item-row">
          <div class="list-item-info">
             <span class="list-item-title">{{ field.name }}</span>
             <span v-if="field.required" class="badge-required">Obrigatório</span>
          </div>
          <label class="toggle-switch">
            <input type="checkbox" v-model="field.required" class="toggle-input">
            <span class="toggle-slider"></span>
          </label>
       </div>
       
       <button class="btn-dashed-add" @click="$emit('open-custom-field-modal')">
         <span>+</span> Adicionar Campo Personalizado
       </button>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Documentos Exigidos</h3>
          <p>Arquivos que devem ser anexados na inscrição</p>
       </div>
       
       <div v-for="(doc, index) in inscriptionForm.documents" :key="'doc-'+index" class="list-item-row">
          <div class="list-item-info-stacked">
             <div style="display: flex; align-items: center;">
                <span class="list-item-title">{{ doc.name }}</span>
             </div>
             <span class="list-item-sub">Tipos aceitos: {{ doc.types }}</span>
          </div>
          <div style="display: flex; align-items: center;">
             <span v-if="doc.required" class="badge-required" style="margin-right: 16px;">Obrigatório</span>
             <label class="toggle-switch">
               <input type="checkbox" v-model="doc.required" class="toggle-input">
               <span class="toggle-slider"></span>
             </label>
          </div>
       </div>
       
       <button class="btn-dashed-add" @click="$emit('open-custom-document-modal')">
         <span>+</span> Adicionar Documento
       </button>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Configuração de Cotas</h3>
          <p>Defina os percentuais de vagas reservadas</p>
       </div>
       
       <div class="quota-row">
         <span class="list-item-title">Ampla concorrência</span>
         <div class="quota-input-wrapper">
           <input type="number" v-model="inscriptionForm.quotas.ampla" class="quota-input"/>
           <span class="quota-symbol">%</span>
         </div>
       </div>
       <div class="quota-row">
         <span class="list-item-title">PCD / Neurodivergentes</span>
         <div class="quota-input-wrapper">
           <input type="number" v-model="inscriptionForm.quotas.pcd" class="quota-input"/>
           <span class="quota-symbol">%</span>
         </div>
       </div>
       <div class="quota-row">
         <span class="list-item-title">Negros e pardos</span>
         <div class="quota-input-wrapper">
           <input type="number" v-model="inscriptionForm.quotas.negros" class="quota-input"/>
           <span class="quota-symbol">%</span>
         </div>
       </div>
       <div class="quota-row">
         <span class="list-item-title">Mulheres</span>
         <div class="quota-input-wrapper">
           <input type="number" v-model="inscriptionForm.quotas.mulheres" class="quota-input"/>
           <span class="quota-symbol">%</span>
         </div>
       </div>
       <div class="quota-row" style="border-bottom: none; margin-bottom: 16px;">
         <span class="list-item-title">45+</span>
         <div class="quota-input-wrapper">
           <input type="number" v-model="inscriptionForm.quotas.age45" class="quota-input"/>
           <span class="quota-symbol">%</span>
         </div>
       </div>

       <div class="list-item-row toggle-row-simple">
         <span class="list-item-title">Permitir apenas uma cota por candidato</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="inscriptionForm.quotas.singleQuota" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
       <div class="list-item-row toggle-row-simple" style="margin-bottom: 0;">
         <span class="list-item-title">Reverter vagas não preenchidas para ampla concorrência</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="inscriptionForm.quotas.revertUnfilled" class="toggle-input">
           <span class="toggle-slider"></span>
         </label>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Classificação para a Próxima Etapa</h3>
          <p>Configure a seleção de candidatos para o nivelamento</p>
       </div>
       
       <div class="green-toggle-box">
          <span class="list-item-title" style="color: #065f46;">Esta etapa seleciona candidatos para a próxima?</span>
          <label class="toggle-switch">
            <input type="checkbox" v-model="inscriptionForm.classification.active" class="toggle-input">
            <span class="toggle-slider"></span>
          </label>
       </div>
       
       <div class="form-row two-cols" style="margin-top: 24px;">
         <div class="form-group">
           <label>Número de Classificados</label>
           <input v-model="inscriptionForm.classification.count" type="number" class="form-input" :disabled="!inscriptionForm.classification.active"/>
         </div>
         <div class="form-group">
           <label>Critério de Classificação</label>
           <select v-model="inscriptionForm.classification.criteria" class="form-input form-select" :disabled="!inscriptionForm.classification.active">
             <option value="Ordem de inscrição">Ordem de inscrição</option>
             <option value="Sorteio">Sorteio</option>
             <option value="Nota em teste">Nota em teste</option>
           </select>
         </div>
       </div>

       <div class="form-group">
         <label>Critério de Desempate</label>
         <input v-model="inscriptionForm.classification.tiebreaker" type="text" placeholder="Ex: Maior idade, ordem de inscrição..." class="form-input" :disabled="!inscriptionForm.classification.active"/>
       </div>

       <div class="list-item-row toggle-row-simple" style="border: none; padding: 0 0 16px 0;">
         <span class="list-item-title">Gerar lista de espera</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="inscriptionForm.classification.waitlist" class="toggle-input" :disabled="!inscriptionForm.classification.active">
           <span class="toggle-slider"></span>
         </label>
       </div>
       
       <div class="list-item-row toggle-row-simple" style="border: none; padding: 0 0 16px 0;">
         <span class="list-item-title">Permitir recursos</span>
         <label class="toggle-switch">
           <input type="checkbox" v-model="inscriptionForm.classification.allowAppeals" class="toggle-input" :disabled="!inscriptionForm.classification.active">
           <span class="toggle-slider"></span>
         </label>
       </div>

       <div class="form-group" style="margin-bottom: 0;">
         <label>Prazo de Recurso</label>
         <input v-model="inscriptionForm.classification.appealDeadline" type="text" placeholder="Dias após a divulgação" class="form-input" :disabled="!inscriptionForm.classification.allowAppeals || !inscriptionForm.classification.active"/>
       </div>
    </div>
  </div>
</template>

<script>
export default {
  // Nome oficial deste componente (Filho do ProgramRegistrationView)
  name: 'FirstStageProgramRegistrationView',
  
  // Propriedades (Props): Dados vitais que este componente precisa receber do componente Pai
  props: {
    // Objeto contendo todos os dados, seleções, toggles e arrays da aba de Inscrição
    inscriptionForm: { type: Object, required: true },
    
    // Objeto usado para renderizar as datas de forma amigável (DD/MM/AAAA) sem alterar o valor original ISO
    displayDates: { type: Object, required: true },
    
    // String indicando se há algum popup de calendário aberto no momento e em qual campo ele está
    activeDatePicker: { type: String, default: null },
    
    // O objeto de Data nativo usado para ancorar o topo do calendário (Mês e Ano visualizados)
    calendarDate: { type: Date, required: true },
    
    // Nomes dos dias da semana (Dom, Seg...)
    weekDays: { type: Array, required: true },
    
    // Nomes dos meses (Jan, Fev...)
    monthNames: { type: Array, required: true },
    
    // Matriz matemática que monta a tabela de dias numéricos exatos de um mês no calendário
    calendarDays: { type: Array, required: true },
    
    // Função do Pai usada para verificar se o dia renderizado é o mesmo salvo no form (pinta a bolinha de azul)
    isSelectedDay: { type: Function, required: true },
    
    // Função do Pai usada para verificar se o dia renderizado é literalmente "hoje" (pinta o número de negrito)
    isToday: { type: Function, required: true }
  }
}
</script>

<style scoped>
/* O CSS Global do Pai fará todo o trabalho perfeitamente. Nenhuma classe extra necessária aqui! */
</style>