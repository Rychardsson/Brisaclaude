?<template>
  <div class="step-container">
    
    <div class="step-header">
      <h2>Revisão Final</h2>
      <p>Revise todas as configurações antes de publicar o edital</p>
    </div>

    <div class="card-section validation-card" :class="{ 'success-card': progressPercentage === 100 }">
       <div class="validation-content">
          <div class="validation-info" style="flex: 1; padding-right: 20px;">
             
             <div class="validation-header-row">
                <span class="icon-circle-outline" :class="{ 'success-icon': progressPercentage === 100 }">
                   <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                     <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                     <path d="M9 12l2 2 4-4"></path>
                   </svg>
                </span>
                <div>
                            <h3 class="validation-title" :class="{ 'success-title': progressPercentage === 100 }">
                                 Validações Automáticas
                            </h3>
                   <p class="validation-subtitle" v-if="progressPercentage < 100">
                               Você possui {{ validationData.totalErrors }} pendência(s) antes de publicar.
                   </p>
                   <p class="validation-subtitle success-text" v-else>
                               Tudo certo! Edital validado e pronto para publicação.
                   </p>
                </div>
             </div>

             <div class="pending-groups" v-if="progressPercentage < 100">
                <div v-for="group in validationData.pendingSteps" :key="group.stepNumber" class="pending-group">
                   
                   <div class="pending-group-header">
                      <strong>[ {{ group.stepName }} ]</strong>
                      
                      <div class="header-actions">
                         <button class="btn-fix-step" @click="$emit('go-to-step', group.stepNumber)">
                            Corrigir Etapa
                         </button>
                         <button class="btn-toggle-errors" @click="toggleGroup(group.stepNumber)">
                            <svg v-if="isExpanded(group.stepNumber)" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                               <polyline points="18 15 12 9 6 15"></polyline>
                            </svg>
                            <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                               <polyline points="6 9 12 15 18 9"></polyline>
                            </svg>
                         </button>
                      </div>
                   </div>
                   
                   <div :class="['pending-list-wrapper', { 'is-open': isExpanded(group.stepNumber) }]">
                      <div class="pending-list-inner">
                         <ul class="pending-list">
                            <li v-for="(error, i) in group.errors" :key="i">
                               ? {{ error }}
                            </li>
                         </ul>
                      </div>
                   </div>
                </div>
             </div>

          </div>

          <div class="validation-progress-box">
             <div class="progress-circle" :class="{ 'success-circle': progressPercentage === 100 }">
                <span class="progress-value">{{ progressPercentage }}%</span>
                <span class="progress-label">{{ progressPercentage === 100 ? 'Completo' : 'Concluído' }}</span>
             </div>
          </div>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title-row">
          <div class="section-title-review">
             <i class="fa-solid fa-file-invoice"></i>
             <h3>Dados do Programa</h3>
          </div>
          <button class="btn-edit-review" @click="$emit('go-to-step', 1)">
             <i class="fa-solid fa-pen"></i> Editar
          </button>
       </div>
       
       <div class="review-grid">
          <div class="review-item">
             <small>Nome do Programa</small>
             <span>{{ formData.programName || 'Não informado' }}</span>
          </div>
          <div class="review-item">
             <small>Turma/Edição</small>
             <span>{{ formData.batchName || 'Não informado' }}</span>
          </div>
          <div class="review-item">
             <small>Executora</small>
             <span>{{ formData.executor || 'Não informado' }}</span>
          </div>
          <div class="review-item">
             <small>Entidade de Fomento</small>
             <span>{{ formData.fundingEntity || 'Não informado' }}</span>
          </div>
          <div class="review-item">
             <small>Coordenador Geral</small>
             <span>{{ formData.generalCoordinator || 'Não informado' }}</span>
          </div>
          <div class="review-item">
             <small>Valor do Programa</small>
             <span>{{ formData.programValue || 'Não informado' }}</span>
          </div>
          <div class="review-item">
             <small>Status</small>
             <span v-if="isProgramDataComplete" :class="['status-badge', formData.status === 'Publicado' ? 'badge-saved' : 'outline-badge']">
               {{ formData.status }}
             </span>
             <span v-else style="color: #9ca3af; font-weight: 500;">-</span>
          </div>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title-review">
          <i class="fa-solid fa-calendar-days"></i>
          <h3>Timeline do Processo Seletivo</h3>
       </div>
       
       <div class="review-timeline">
          <div class="timeline-step">
             <div class="timeline-point">0</div>
             <div class="timeline-info">
                <strong>Período de Inscrições</strong>
                <p>Data de início: {{ displayDates.startDate || 'Não definido' }} ? Data de encerramento: {{ displayDates.endDate || 'Não definido' }}</p>
             </div>
          </div>
          <div v-for="stage in stageList" :key="stage.id" class="timeline-step">
             <div class="timeline-point">{{ stage.id + 1 }}</div>
             <div class="timeline-info">
                <strong>{{ stage.title }}</strong>
                <p>Modalidade: {{ stage.modality }} ? Duração prevista: {{ stage.duration || 'Não definida' }}</p>
             </div>
          </div>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title-row">
          <div class="section-title-review">
             <i class="fa-solid fa-users-gear"></i>
             <h3>Etapas e Capacidade</h3>
          </div>
          <button class="btn-edit-review" @click="$emit('go-to-step', 2)">
             <i class="fa-solid fa-pen"></i> Editar
          </button>
       </div>
       
       <div class="review-table">
          <div v-for="stage in stageList" :key="stage.id" class="review-table-row">
             <div class="row-main">
                <span class="row-id">{{ stage.id + 1 }}</span>
                <div class="row-text">
                   <strong>{{ stage.title }}</strong>
                   <p>{{ stage.type }}</p>
                </div>
             </div>
             <div class="row-side">
                <span class="row-value">{{ stage.slots }}</span>
                <p>{{ stage.modality }}</p>
             </div>
          </div>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title-review">
          <i class="fa-solid fa-percent"></i>
          <h3>Cotas e Elegibilidade</h3>
       </div>
       <div class="quota-review-list">
          <div class="quota-review-item"><span>Ampla concorrência</span><strong>{{ inscriptionForm.quotas.ampla }}%</strong></div>
          <div class="quota-review-item"><span>PCD / Neurodivergentes</span><strong>{{ inscriptionForm.quotas.pcd }}%</strong></div>
          <div class="quota-review-item"><span>Negros e pardos</span><strong>{{ inscriptionForm.quotas.negros }}%</strong></div>
          <div class="quota-review-item"><span>Mulheres</span><strong>{{ inscriptionForm.quotas.mulheres }}%</strong></div>
          <div class="quota-review-item"><span>45+</span><strong>{{ inscriptionForm.quotas.age45 }}%</strong></div>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title-review">
          <i class="fa-solid fa-gavel"></i>
          <h3>Regras de Classificação e Aprovação</h3>
       </div>
       <div class="rules-review-box">
          <div class="rule-review-item">
             <strong>Etapa Inscrição → Nivelamento</strong>
             <p>{{ inscriptionForm.classification.count }} candidatos classificados por {{ inscriptionForm.classification.criteria.toLowerCase() }}</p>
          </div>
          <div class="rule-review-item">
             <strong>Etapa Nivelamento → Imersão</strong>
             <p>{{ nivelamentoForm.grading.approvedCount }} aprovados com base em: Nota de prova ({{ nivelamentoForm.grading.examWeight }}%) + Cursos opcionais ({{ nivelamentoForm.grading.optionalWeight }}%)</p>
          </div>
          <div class="rule-review-item">
             <strong>Aprovação Final da Imersão</strong>
             <p>Avaliação Parcial ({{ imersaoForm.avaliacoes.pesoParcial }}%) + Avaliação Final ({{ imersaoForm.avaliacoes.pesoFinal }}%) com critérios múltiplos</p>
          </div>
       </div>
    </div>

    <div class="card-section">
       <div class="section-title-review">
          <i class="fa-solid fa-hand-holding-dollar"></i>
          <h3>Benefícios da Imersão</h3>
       </div>
       <div class="benefits-grid">
          <div class="benefit-item" v-if="imersaoForm.beneficios.hasBolsa"><i class="fa-solid fa-circle-check"></i> Bolsa de R$ {{ imersaoForm.beneficios.valorBolsa }} durante {{ imersaoForm.beneficios.mesesBolsa }} meses</div>
          <div class="benefit-item" v-if="imersaoForm.beneficios.hasNotebook"><i class="fa-solid fa-circle-check"></i> Notebook em comodato</div>
          <div class="benefit-item" v-if="nivelamentoForm.certification.active"><i class="fa-solid fa-circle-check"></i> Certificado de conclusão</div>
          
          <div class="benefit-item" v-if="!imersaoForm.beneficios.hasBolsa && !imersaoForm.beneficios.hasNotebook && !nivelamentoForm.certification.active" style="color: #6b7280; font-style: italic;">
             Nenhum benefício extra configurado.
          </div>
       </div>
    </div>

    <div class="review-footer">
       <button class="btn-footer-back" @click="$emit('go-to-step', 5)">
          <i class="fa-solid fa-arrow-left"></i> Voltar e Editar
       </button>
       <div class="footer-right">
          <button class="btn-footer-back" @click="$emit('save-draft')">
             <i class="fa-solid fa-floppy-disk"></i> Salvar Rascunho
          </button>
          <button class="btn-publish" @click="$emit('publish-program')" :disabled="progressPercentage !== 100" :style="progressPercentage !== 100 ? 'opacity: 0.5; cursor: not-allowed;' : ''">
             <i class="fa-solid fa-paper-plane"></i> {{ isEditMode ? 'Atualizar programa' : 'Cadastrar programa' }}
          </button>
       </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'RevisionProgramRegistrationView',
  props: {
    formData: { type: Object, required: true },
    stageList: { type: Array, required: true },
    inscriptionForm: { type: Object, required: true },
    nivelamentoForm: { type: Object, required: true },
    imersaoForm: { type: Object, required: true },
    displayDates: { type: Object, required: true },
    currentStep: { type: Number, required: true },
    isEditMode: { type: Boolean, default: false }
  },
  data() {
    return {
      expandedGroups: [] 
    };
  },
  watch: {
    progressPercentage: {
      immediate: true,
      handler(val) {
        this.$emit('update-status', val === 100);
      }
    }
  },
  methods: {
    toggleGroup(stepNumber) {
      const index = this.expandedGroups.indexOf(stepNumber);
      if (index > -1) {
        this.expandedGroups.splice(index, 1); 
      } else {
        this.expandedGroups.push(stepNumber); 
      }
    },
    isExpanded(stepNumber) {
      return this.expandedGroups.includes(stepNumber);
    }
  },
  computed: {
    isProgramDataComplete() {
      return !!(this.formData.programName && this.formData.executor && this.formData.fundingEntity && this.formData.generalCoordinator && this.formData.programValue && this.formData.supportEmail);
    },

    // LÓGICA MESTRA: Varre absolutamente todos os campos de todas as abas
    validationData() {
      const steps = [];
      let totalChecks = 0;
      let totalErrors = 0;

      // Função ajudante para encurtar a verificação
      const checkField = (condition, errorMessage, stepArray) => {
        totalChecks++;
        if (!condition) {
          stepArray.push(errorMessage);
          totalErrors++;
        }
      };

      // ETAPA 1 - Dados do Programa
      const step1Errors = [];
      checkField(this.formData.programName, 'Nome do programa não preenchido.', step1Errors);
      checkField(this.formData.batchName, 'Turma/Edição não preenchida.', step1Errors);
      checkField(this.formData.executor, 'Executora do programa não definida.', step1Errors);
      checkField(this.formData.fundingEntity, 'Entidade de fomento não definida.', step1Errors);
      checkField(this.formData.generalCoordinator, 'Coordenador geral não definido.', step1Errors);
      checkField(this.formData.programValue, 'Valor do programa não informado.', step1Errors);
      checkField(this.formData.objective, 'Objetivo do programa não preenchido.', step1Errors);
      checkField(this.formData.location, 'Local de realização não definido.', step1Errors);
      checkField(this.formData.supportEmail, 'E-mail de suporte não configurado.', step1Errors);
      checkField(this.displayDates.publishDate, 'Data de publicação não definida.', step1Errors);
      checkField(this.displayDates.startDate, 'Data de início do programa não definida.', step1Errors);
      checkField(this.displayDates.endDate, 'Data de fim do programa não definida.', step1Errors);
      
      if (step1Errors.length > 0) {
         steps.push({ stepNumber: 1, stepName: 'Dados do programa', errors: step1Errors });
      }

      // ETAPA 2 - Estrutura de Etapas
      const step2Errors = [];
      checkField(this.stageList.length > 0, 'Nenhuma etapa foi criada no fluxo.', step2Errors);
      this.stageList.forEach((stage, idx) => {
         checkField(stage.title, `Etapa ${idx + 1}: Nome não preenchido.`, step2Errors);
         checkField(stage.duration, `Etapa ${idx + 1}: Duração não definida.`, step2Errors);
         checkField(stage.slots, `Etapa ${idx + 1}: Quantidade de vagas não informada.`, step2Errors);
      });

      if (step2Errors.length > 0) {
         steps.push({ stepNumber: 2, stepName: 'Estrutura das etapas', errors: step2Errors });
      }

      // ETAPA 3 - Inscrição
      const step3Errors = [];
      checkField(this.displayDates.inscStart, 'Data de início das inscrições não definida.', step3Errors);
      checkField(this.displayDates.inscEnd, 'Data de fim das inscrições não definida.', step3Errors);
      
      const q = this.inscriptionForm.quotas;
      const totalQuotas = Number(q.ampla) + Number(q.pcd) + Number(q.negros) + Number(q.mulheres) + Number(q.age45);
      checkField(totalQuotas === 100, `Distribuição de cotas inválida (${totalQuotas}% em vez de 100%).`, step3Errors);
      
      if (step3Errors.length > 0) {
         steps.push({ stepNumber: 3, stepName: 'Inscrição', errors: step3Errors });
      }

      // ETAPA 4 - Nivelamento
      const step4Errors = [];
      checkField(this.displayDates.nivStart, 'Data de início do nivelamento não definida.', step4Errors);
      checkField(this.displayDates.nivEnd, 'Data de fim do nivelamento não definida.', step4Errors);
      checkField(this.nivelamentoForm.workload, 'Carga horária do nivelamento não informada.', step4Errors);
      checkField(this.nivelamentoForm.platform, 'Plataforma do curso não definida.', step4Errors);
      
      if (this.nivelamentoForm.finalExam.active) {
         checkField(this.displayDates.nivExamDate, 'Data da prova final não definida.', step4Errors);
         checkField(this.nivelamentoForm.finalExam.time, 'Horário da prova final não definido.', step4Errors);
         checkField(this.nivelamentoForm.finalExam.duration, 'Duração da prova final não informada.', step4Errors);
      }

      if (step4Errors.length > 0) {
         steps.push({ stepNumber: 4, stepName: 'Nivelamento', errors: step4Errors });
      }

      // ETAPA 5 - Imersão
      const step5Errors = [];
      checkField(this.displayDates.imerStart, 'Data de início da imersão não definida.', step5Errors);
      checkField(this.displayDates.imerEnd, 'Data de fim da imersão não definida.', step5Errors);
      checkField(this.imersaoForm.cargaHoraria, 'Carga horária da imersão não definida.', step5Errors);
      checkField(this.imersaoForm.duracaoMeses, 'Duração em meses da imersão não informada.', step5Errors);
      checkField(this.imersaoForm.mediaHoras, 'Média de horas semanais não informada.', step5Errors);
      
      // Validar local apenas se a modalidade não for Online
      if (this.imersaoForm.modalidade !== 'Online') {
         checkField(this.imersaoForm.local, 'Local da imersão não informado.', step5Errors);
      }
      
      checkField(this.imersaoForm.presenca.responsavelJustificativa, 'Responsável pela análise de justificativas não informado.', step5Errors);

      if (this.imersaoForm.beneficios.hasBolsa) {
        checkField(this.imersaoForm.beneficios.valorBolsa, 'Valor financeiro da bolsa não informado.', step5Errors);
        checkField(this.imersaoForm.beneficios.mesesBolsa, 'Quantidade de meses da bolsa não definida.', step5Errors);
      }
      
      if (this.imersaoForm.beneficios.hasNotebook) {
        checkField(this.imersaoForm.beneficios.modeloNotebook, 'Modelo/especificação do notebook não informado.', step5Errors);
        checkField(this.displayDates.prazoDevolucaoNotebook, 'Prazo de devolução do equipamento não definido.', step5Errors);
      }
      
      if (this.imersaoForm.beneficios.exigirContrato) {
        checkField(this.imersaoForm.beneficios.prazoContrato, 'Prazo para assinatura do contrato não estipulado.', step5Errors);
      }
      
      if (step5Errors.length > 0) {
         steps.push({ stepNumber: 5, stepName: 'Imersão', errors: step5Errors });
      }

      return {
         pendingSteps: steps,
         totalChecks,
         totalErrors
      };
    },

    pendingSteps() {
      return this.validationData.pendingSteps;
    },
    
    totalErrors() {
      return this.validationData.totalErrors;
    },
    
    progressPercentage() {
      const data = this.validationData;
      if (data.totalChecks === 0) return 0;
      const approved = data.totalChecks - data.totalErrors;
      return Math.round((approved / data.totalChecks) * 100);
    }
  }
}
</script>

<style scoped>
.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title-review {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1e1b4b;
}

.section-title-review i {
  color: #0d9488;
  font-size: 18px;
}

.section-title-review h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.btn-edit-review {
  background: transparent;
  border: 1px solid #d1d5db;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  color: #4b5563;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background 0.2s;
}

.btn-edit-review:hover {
  background-color: #f3f4f6;
}

.validation-card {
  background-color: #fffbeb;
  border: 1px solid #fde68a;
  transition: all 0.3s ease;
}

.validation-card.success-card { background-color: #f0fdf4; border-color: #a7f3d0; }
.icon-circle-outline.success-icon { border-color: #10b981; color: #10b981; }
.validation-title.success-title { color: #065f46; }
.success-text { color: #059669 !important; font-weight: 500; font-size: 14px !important; }
.progress-circle.success-circle { border-color: #d1fae5; border-top-color: #10b981; }

.validation-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start; 
}

.validation-header-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.icon-circle-outline {
  width: 40px;
  height: 40px;
  border: 2px solid #f59e0b;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f59e0b;
  transition: all 0.3s ease;
}

.validation-title { color: #92400e; font-size: 18px; margin: 0; transition: color 0.3s ease; }
.validation-subtitle { color: #b45309; font-size: 13px; margin: 0; }

.pending-groups {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
  padding-left: 56px;
}

.pending-group {
  background: white;
  border-radius: 6px;
  padding: 12px 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.pending-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pending-group-header strong {
  font-size: 13px;
  color: #b45309;
}

.btn-fix-step {
  background: transparent;
  border: none;
  color: #0288d1;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-fix-step:hover {
  text-decoration: underline;
}

.btn-toggle-errors {
  background: #f3f4f6;
  border: none;
  border-radius: 4px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-toggle-errors:hover {
  background: #e5e7eb;
}

.pending-list-wrapper {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.3s ease-out;
}

.pending-list-wrapper.is-open {
  grid-template-rows: 1fr;
}

.pending-list-inner {
  overflow: hidden;
}

.pending-list {
  list-style: none;
  padding: 0;
  margin: 0;
  border-top: 1px dashed #e5e7eb;
  padding-top: 12px;
  margin-top: 12px;
}

.pending-list li {
  font-size: 13px;
  color: #374151;
  margin-bottom: 4px;
}

.validation-progress-box {
  padding-left: 40px;
  text-align: center;
  margin-top: 4px;
}

.progress-circle {
  width: 100px;
  height: 100px;
  border: 8px solid #fde68a;
  border-top-color: #f59e0b;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.progress-value { font-size: 24px; font-weight: 700; color: #1e1b4b; }
.progress-label { font-size: 10px; color: #6b7280; text-transform: uppercase; }

.review-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.review-item small { display: block; color: #6b7280; font-size: 11px; margin-bottom: 4px; }
.review-item span { color: #1a233a; font-weight: 500; font-size: 14px; }

.review-timeline {
  margin-top: 10px;
}

.timeline-step {
  display: flex;
  gap: 20px;
  padding-bottom: 20px;
  position: relative;
}

.timeline-step:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 16px;
  top: 32px;
  bottom: 0;
  width: 2px;
  background-color: #f3f4f6;
}

.timeline-point {
  width: 32px;
  height: 32px;
  background-color: #0d9488;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  z-index: 1;
}

.timeline-info strong { display: block; font-size: 14px; color: #1a233a; }
.timeline-info p { font-size: 13px; color: #6b7280; margin: 2px 0 0; }

.review-table-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
}

.row-main { display: flex; align-items: center; gap: 16px; }
.row-id { color: #0d9488; font-weight: 600; }
.row-text strong { font-size: 14px; color: #1a233a; }
.row-text p { font-size: 12px; color: #9ca3af; margin: 0; }
.row-side { text-align: right; }
.row-value { font-weight: 600; color: #0d9488; font-size: 14px; }
.row-side p { font-size: 11px; color: #9ca3af; margin: 0; }

.quota-review-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
}

.quota-review-item { text-align: center; }
.quota-review-item span { display: block; font-size: 11px; color: #6b7280; margin-bottom: 4px; }
.quota-review-item strong { font-size: 16px; color: #1e1b4b; }

.rules-review-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-review-item {
  padding: 12px 16px;
  background-color: #f0fdfa;
  border-radius: 8px;
}

.rule-review-item strong { font-size: 13px; color: #0d9488; }
.rule-review-item p { font-size: 13px; color: #374151; margin: 4px 0 0; }

.benefits-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #1a233a;
}

.benefit-item i { color: #10b981; }

.review-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.footer-right {
  display: flex;
  gap: 12px;
}

.btn-publish {
  background-color: #10b981;
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: opacity 0.2s;
}

.btn-publish:hover { opacity: 0.9; }
</style>

