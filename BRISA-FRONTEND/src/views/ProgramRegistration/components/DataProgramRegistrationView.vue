<template>
  <div class="step-container">
    
    <div class="step-header">
       <h2>Dados do Programa</h2>
      <p>Preencha as informações gerais do edital e do programa</p>
    </div>

    <div class="card-section">
       <div class="section-title">
          <h3>Informações Básicas</h3>
          <p>Dados principais do programa e edital</p>
       </div>
       
       <div class="form-row two-cols">
         <div class="form-group">
           <label>Nome do Programa <span class="required">*</span></label>
           <input v-model="formData.programName" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Programa de Formação em Tecnologia" class="form-input"/>
         </div>
         <div class="form-group">
           <label>Turma/Edição <span class="required">*</span></label>
           <input v-model="formData.batchName" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: 2026.1" class="form-input"/>
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
         <div class="form-group currency-group">
           <label>Valor do Programa <span class="required">*</span></label>
           <div class="currency-row">
             <input
               v-model="programValueDisplay"
               @input="onProgramValueInput"
               @keydown.enter="$event.target.blur()"
               type="text"
               placeholder="Ex: R$ 150.000,00"
               class="form-input currency-input"
             />
             <div class="value-words" aria-live="polite">{{ valuePorExtenso }}</div>
           </div>
         </div>
       </div>

<div class="form-group" style="margin-bottom: 0;">
         <label>Objetivo do Programa <span class="required">*</span></label>
         <textarea v-model="formData.objective" placeholder="Descreva o propósito e os resultados esperados deste programa..." class="form-textarea" rows="3"></textarea>
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
           <input v-model="formData.location" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Maceió/AL" class="form-input"/>
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
           <span v-if="emailError" class="error-text">Formato de e-mail inválido.</span>
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
         <label>Empresas ou Instituições Parceiras</label>
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
           <label>Publicação do Edital <span class="required">*</span></label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.publishDate" @input="$emit('parse-date-input', 'publishDate')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'publishDate')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           
           <div v-if="activeDatePicker === 'publishDate'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
              </div>
              <div class="calendar-grid">
                 <span v-for="d in weekDays" :key="d" class="cal-weekday">{{ d }}</span>
                 <span v-for="(day, idx) in calendarDays" :key="idx" :class="['cal-day', { 'empty': !day, 'selected': isSelectedDay('publishDate', day), 'today': isToday(day) }]" @click.stop="$emit('select-date', day)">{{ day }}</span>
              </div>
           </div>
         </div>
         
         <div class="form-group relative">
           <label>Início do Programa</label>
           <div class="date-input-wrapper">
             <input v-model="displayDates.startDate" @input="$emit('parse-date-input', 'startDate')" type="text" placeholder="dd/mm/aaaa" class="form-input" maxlength="10"/>
             <svg @click="$emit('open-date-picker', 'startDate')" class="date-icon cursor-pointer" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
           </div>
           <div v-if="activeDatePicker === 'startDate'" class="custom-calendar">
              <div class="calendar-header">
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
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
                 <button type="button" class="cal-btn" @click.stop="$emit('prev-month')">‹</button>
                 <span>{{ monthNames[calendarDate.getMonth()] }} {{ calendarDate.getFullYear() }}</span>
                 <button type="button" class="cal-btn" @click.stop="$emit('next-month')">›</button>
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
  name: 'DataProgramRegistrationView',
  props: {
    formData: { type: Object, required: true },
    displayDates: { type: Object, required: true },
    activeDatePicker: { type: String, default: null },
    calendarDate: { type: Date, required: true },
    weekDays: { type: Array, required: true },
    monthNames: { type: Array, required: true },
    calendarDays: { type: Array, required: true },
    isEmailInvalid: { type: Boolean, required: true }, // Mantido para não quebrar contrato com o pai
    newPartnerName: { type: String, required: true },
    isSelectedDay: { type: Function, required: true },
    isToday: { type: Function, required: true }
  },
  data() {
    return {
      emailTouched: false,
      programValueDisplay: ''
    }
  },
  computed: {
    emailError() {
      if (!this.emailTouched) return false;
      if (!this.formData.supportEmail) return false;
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return !emailRegex.test(this.formData.supportEmail);
    },
    valuePorExtenso() {
      const value = Number(this.formData.programValue) || 0;
      return numberToWordsBR(value);
    }
  },
  watch: {
    'formData.programValue': {
      immediate: true,
      handler(val) {
        const v = Number(val) || 0;
        this.programValueDisplay = new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(v);
      }
    }
  },
  methods: {
    onProgramValueInput(e) {
      const digits = String(e.target.value).replace(/\D/g,'');
      const value = digits ? parseInt(digits,10)/100 : 0;
      // Mutating formData is consistent with existing v-model usage in this component
      this.formData.programValue = value;
      this.programValueDisplay = new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(value);
    }
  }
}

function numberToWordsBR(value) {
  const v = Math.abs(Number(value) || 0);
  const inteiro = Math.floor(v);
  const cents = Math.round((v - inteiro) * 100);
  if (inteiro === 0 && cents === 0) return 'zero reais';

  // compute groups to decide whether to insert 'de' before 'reais'
  const groups = [];
  let rem = inteiro;
  while (rem > 0) { groups.push(rem % 1000); rem = Math.floor(rem / 1000); }
  let lowestNonZeroIndex = groups.findIndex(g => g !== 0);
  if (lowestNonZeroIndex === -1) lowestNonZeroIndex = 0;

  const inteiroWords = integerToWordsBR(inteiro);
  const centsWords = cents > 0 ? `${integerToWordsBR(cents)} centavo${cents > 1 ? 's' : ''}` : '';

  // singular real
  if (inteiro === 1) {
    const reais = `${inteiroWords} real`;
    return centsWords ? `${reais} e ${centsWords}` : reais;
  }

  // decide whether to use 'de reais' (when the lowest non-zero group is million or higher)
  let reais;
  if (inteiro >= 1000000 && lowestNonZeroIndex >= 2) {
    reais = `${inteiroWords} de reais`;
  } else {
    reais = `${inteiroWords} reais`;
  }

  return centsWords ? `${reais} e ${centsWords}` : reais;
}

function integerToWordsBR(n) {
  n = Math.floor(Math.abs(n));
  if (n === 0) return 'zero';

  const units = ['','um','dois','três','quatro','cinco','seis','sete','oito','nove'];
  const teens = ['dez','onze','doze','treze','quatorze','quinze','dezesseis','dezessete','dezoito','dezenove'];
  const tens = ['','','vinte','trinta','quarenta','cinquenta','sessenta','setenta','oitenta','noventa'];
  const hundreds = ['','cento','duzentos','trezentos','quatrocentos','quinhentos','seiscentos','setecentos','oitocentos','novecentos'];

  function belowThousand(num) {
    num = Number(num);
    if (num === 0) return '';
    const h = Math.floor(num / 100);
    const rem = num % 100;
    const parts = [];
    if (h > 0) {
      if (h === 1 && rem === 0) parts.push('cem');
      else parts.push(hundreds[h]);
    }
    if (rem >= 20) {
      const t = Math.floor(rem / 10);
      const u = rem % 10;
      parts.push(tens[t]);
      if (u > 0) parts.push(units[u]);
    } else if (rem >= 10) {
      parts.push(teens[rem - 10]);
    } else if (rem > 0) {
      parts.push(units[rem]);
    }
    return parts.join(' e ');
  }

  const scales = [
    {singular: '', plural: ''},
    {singular: 'mil', plural: 'mil'},
    {singular: 'milhão', plural: 'milhões'},
    {singular: 'bilhão', plural: 'bilhões'},
    {singular: 'trilhão', plural: 'trilhões'},
    {singular: 'quadrilhão', plural: 'quadrilhões'}
  ];

  const groups = [];
  let remaining = n;
  while (remaining > 0) {
    groups.push(remaining % 1000);
    remaining = Math.floor(remaining / 1000);
  }

  const parts = [];
  for (let i = groups.length - 1; i >= 0; i--) {
    const g = groups[i];
    if (g === 0) continue;
    const gWords = belowThousand(g);
    if (i === 1) {
      // thousands
      if (g === 1) parts.push('mil');
      else parts.push(`${gWords} mil`);
    } else if (i === 0) {
      parts.push(gWords);
    } else {
      const scale = scales[i];
      const scaleName = g === 1 ? scale.singular : scale.plural;
      parts.push(`${gWords} ${scaleName}`);
    }
  }

  if (parts.length === 1) return parts[0];
  const joined = parts.join(', ');
  const lastComma = joined.lastIndexOf(', ');
  if (lastComma !== -1) {
    return joined.substring(0, lastComma) + ' e ' + joined.substring(lastComma + 2);
  }
  return joined;
}
</script>

<style scoped>
.currency-row { display:flex; align-items:flex-start; gap:12px; }
.currency-input { flex: 0 0 160px; width:160px; box-sizing:border-box; }
.value-words { flex:1; font-size:0.95rem; color:#333; white-space:normal; overflow-wrap:break-word; word-break:break-word; line-height:1.3; font-family: inherit; font-weight: 400; }
.currency-group .form-input { box-sizing:border-box; }
@media (max-width:600px) {
  .currency-row { flex-direction:column; align-items:flex-start; gap:6px; }
  .currency-input { width:100%; max-width:100%; }
  .value-words { white-space:normal; }
}
</style>

