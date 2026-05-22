<template>
  <div class="step-container">
    
    <div class="step-header">
       <h2>Dados do Programa</h2>
       <p>Preencha as informaÃ§Ãµes gerais do edital e do programa</p>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>InformaÃ§Ãµes BÃ¡sicas</h3>
          <p>Dados principais do programa e edital</p>
       </div>
       
       <div class="form-row two-cols">
         <div class="form-group">
           <label>Nome do Programa <span class="required">*</span></label>
           <input v-model="formData.programName" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Programa de FormaÃ§Ã£o em Tecnologia" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Nome da Turma/EdiÃ§Ã£o <span class="required">*</span></label>
           <input v-model="formData.batchName" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Turma 2026.1" class="form-input"/>
         </div>
       </div>

       <div class="form-group">
         <label>Executora <span class="required">*</span></label>
         <input v-model="formData.executor" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Universidade Federal de Tecnologia" class="form-input"/>
       </div>

       <div class="form-row three-cols-special">
         <div class="form-group">
           <label>Entidade de Fomento <span class="required">*</span></label>
           <input v-model="formData.fundingEntity" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Softex" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Coordenador Geral <span class="required">*</span></label>
           <input v-model="formData.generalCoordinator" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Nome do coordenador" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Valor do Programa <span class="required">*</span></label>
           <input v-model="formData.programValue" @keydown.enter="$event.target.blur()" type="number" min="0" step="0.01" placeholder="Ex: 150000.00" class="form-input"/>
         </div>
       </div>

<div class="form-group" style="margin-bottom: 0;">
         <label>Objetivo do Programa <span class="required">*</span></label>
         <textarea v-model="formData.objective" placeholder="Descreva o propÃ³sito e os resultados esperados deste programa..." class="form-textarea" rows="3"></textarea>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Localidade e Suporte</h3>
          <p>Onde o programa ocorre e canais de atendimento</p>
       </div>
       
       <div class="form-row two-cols">
         <div class="form-group">
           <label>Localidade (Cidade/Estado)</label>
           <input v-model="formData.location" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: SÃ£o Paulo, SP (ou Nacional)" class="form-input"/>
         </div>
         
         <div class="form-group relative">
           <label>E-mail de Suporte</label>
           <input
             v-model="formData.supportEmail"
             @input="emailTouched = true; $emit('update-email-touched')"
             @blur="emailTouched = true; $emit('update-email-touched')"
             @keydown.enter="$event.target.blur()"
             type="email"
             placeholder="Ex: suporte@programa.com"
             :class="['form-input', { 'input-error': emailError }]"
           />
           <span v-if="emailError" class="error-text">Formato de e-mail invÃ¡lido.</span>
         </div>
       </div>

       <div class="form-row two-cols">
         <div class="form-group">
           <label>Site Oficial</label>
           <input v-model="formData.officialWebsite" @keydown.enter="$event.target.blur()" type="url" placeholder="Ex: https://brisa.org/programa" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Observações gerais</label>
           <input v-model="formData.observations" @keydown.enter="$event.target.blur()" type="text" placeholder="Informações complementares do programa" class="form-input"/>
         </div>
       </div>

<div class="form-group" style="margin-bottom: 0;">
         <label>Empresas ou InstituiÃ§Ãµes Parceiras</label>
         <div class="partner-input-row">
           <input
             :value="newPartnerName"
             @input="$emit('update-partner-name', $event.target.value)"
             @keydown.enter.prevent="$emit('add-partner')"
             type="text"
             placeholder="Digite o nome do parceiro e aperte Enter ou clique em Adicionar"
             class="form-input"
           />
           <button class="btn-add-partner" @click="$emit('add-partner')">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              Adicionar
           </button>
         </div>
         
         <div v-if="formData.partners && formData.partners.length > 0" class="partner-tags-container">
            <div v-for="(partner, index) in formData.partners" :key="index" class="partner-tag">
               {{ partner }}
               <button class="btn-remove-tag" @click="$emit('remove-partner', index)" aria-label="Remover">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
               </button>
            </div>
         </div>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Cronograma Geral</h3>
          <p>Defina as datas macro do edital e programa</p>
       </div>
       
       <div class="form-row three-cols-special">
         <div class="form-group relative">
           <label>PublicaÃ§Ã£o do Edital <span class="required">*</span></label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.publishDate" @input="$emit('parse-date-input', 'publishDate')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'publishDate')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           
           <div v-if="activeDatePicker === 'publishDate'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">â€¹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">â€º</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('publishDate', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
         
         <div class="form-group relative">
           <label>InÃ­cio do Programa</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.startDate" @input="$emit('parse-date-input', 'startDate')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'startDate')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           <div v-if="activeDatePicker === 'startDate'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">â€¹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">â€º</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('startDate', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
         
         <div class="form-group relative" style="margin-bottom: 0;">
           <label>Fim do Programa</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.endDate" @input="$emit('parse-date-input', 'endDate')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'endDate')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           <div v-if="activeDatePicker === 'endDate'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">â€¹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">â€º</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('endDate', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
         
       </div>
    </div>
  </div>
</template>

<script>
export default {
  // Nome oficial deste componente Vue
  name: 'DataProgramRegistrationView',
  
  // As propriedades injetadas pelo componente Pai
  props: {
    formData: { type: Object, required: true },
    displayDates: { type: Object, required: true },
    activeDatePicker: { type: String, default: null },
    calendarDate: { type: Date, required: true },
    weekDays: { type: Array, required: true },
    monthNames: { type: Array, required: true },
    calendarDays: { type: Array, required: true },
    isEmailInvalid: { type: Boolean, required: true }, // Mantido para nÃ£o quebrar contrato com o pai
    newPartnerName: { type: String, required: true },
    isSelectedDay: { type: Function, required: true },
    isToday: { type: Function, required: true }
  },

  // Dados locais (Estado deste componente filho)
  data() {
    return {
      // Flag para sabermos se o usuÃ¡rio jÃ¡ focou e interagiu com o campo de e-mail
      emailTouched: false
    }
  },

  // Propriedades computadas (ValidaÃ§Ã£o local)
  computed: {
    emailError() {
      // Se nÃ£o encostou no campo ainda, nÃ£o hÃ¡ erro
      if (!this.emailTouched) return false;
      
      // Se estiver em branco (usuÃ¡rio apagou), tambÃ©m nÃ£o mostra "formato invÃ¡lido"
      if (!this.formData.supportEmail) return false;
      
      // ValidaÃ§Ã£o com ExpressÃ£o Regular para "nome@exemplo.com"
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return !emailRegex.test(this.formData.supportEmail);
    }
  }
}
</script>

<style scoped>
/* O CSS Global jÃ¡ lida com todo o layout. A tag scoped garante proteÃ§Ã£o aqui. */
</style>

