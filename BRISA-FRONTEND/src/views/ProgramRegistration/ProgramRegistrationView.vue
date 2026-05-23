<template>
  <div class="program-registration-view">
    
    <div v-if="activeDatePicker" class="picker-overlay" @click="closeDatePicker"></div>

    <div v-if="showNewStageModal" class="modal-overlay" @click="closeNewStageModal">
      <div class="modal-content" @click.stop>
        
        <div class="modal-header">
          <div>
            <h3>Adicionar Nova Etapa</h3> 
            <p class="modal-subtitle">Configure as informações da nova etapa do processo seletivo</p> 
          </div>
          <button class="btn-close-modal" @click="closeNewStageModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
          
        <div class="modal-body">
          <div class="form-row two-cols">
            <div class="form-group">
              <label>Nome da Etapa <span class="required">*</span></label> 
              <input v-model="newStageForm.title" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Entrevista Técnica" class="form-input"/>
            </div>
            <div class="form-group">
              <label>Tipo</label> 
              <input v-model="newStageForm.type" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Avaliação" class="form-input"/>
            </div>
          </div>

          <div class="form-group">
            <label>Descrição</label> 
            <textarea v-model="newStageForm.desc" placeholder="Descreva brevemente o objetivo desta etapa..." class="form-textarea" rows="2"></textarea>
          </div>

          <div class="form-row three-cols-special" style="margin-bottom: 0;">
            <div class="form-group" style="margin-bottom: 0;">
              <label>Modalidade</label>
              <select v-model="newStageForm.modality" class="form-input form-select">
                <option value="Online">Online</option>
                <option value="Presencial">Presencial</option>
                <option value="Híbrida">Híbrida</option>
                <option value="Remota Assíncrona">Remota Assíncrona</option>
              </select>
            </div>
            
            <div class="form-group" style="margin-bottom: 0;">
              <label>Duração</label>
              <div class="duration-input-group">
                <input v-model="newStageForm.durationValue" @keydown.enter="$event.target.blur()" type="number" min="1" placeholder="Ex: 15" class="form-input"/>
                <select v-model="newStageForm.durationUnit" class="form-input form-select min-w-select">
                  <option value="dias">dias</option>
                  <option value="meses">meses</option>
                </select>
              </div>
            </div>
            
            <div class="form-group" style="margin-bottom: 0;">
              <label>Vagas</label>
              <input v-model="newStageForm.slots" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: 20 vagas" class="form-input"/>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-footer-back" @click="closeNewStageModal">Cancelar</button>
          <button class="btn-footer-continue" @click="saveNewStage">Adicionar</button>
        </div>
      </div>
    </div>

    <div v-if="showEditStageModal" class="modal-overlay" @click="closeEditStageModal">
      <div class="modal-content" @click.stop>
        
        <div class="modal-header">
          <div>
            <h3>Editar Etapa</h3>
            <p class="modal-subtitle">Configure as informações da etapa do processo seletivo</p>
          </div>
          <button class="btn-close-modal" @click="closeEditStageModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
          
        <div class="modal-body">
          <div class="form-row two-cols">
            <div class="form-group">
              <label>Nome da Etapa <span class="required">*</span></label>
              <input v-model="editStageForm.title" @keydown.enter="$event.target.blur()" type="text" class="form-input"/>
            </div>
            <div class="form-group">
              <label>Tipo</label>
              <input v-model="editStageForm.type" @keydown.enter="$event.target.blur()" type="text" class="form-input"/>
            </div>
          </div>

          <div class="form-group">
            <label>Descrição</label>
            <textarea v-model="editStageForm.desc" class="form-textarea" rows="2"></textarea>
          </div>

          <div class="form-row three-cols-special" style="margin-bottom: 0;">
            <div class="form-group" style="margin-bottom: 0;">
              <label>Modalidade</label>
              <select v-model="editStageForm.modality" class="form-input form-select">
                <option value="Online">Online</option>
                <option value="Presencial">Presencial</option>
                <option value="Híbrida">Híbrida</option>
                <option value="Remota Assíncrona">Remota Assíncrona</option>
              </select>
            </div>
            
            <div class="form-group" style="margin-bottom: 0;">
              <label>Duração</label>
              <div class="duration-input-group">
                <input v-model="editStageForm.durationValue" @keydown.enter="$event.target.blur()" type="number" min="1" class="form-input"/>
                <select v-model="editStageForm.durationUnit" class="form-input form-select min-w-select">
                  <option value="dias">dias</option>
                  <option value="meses">meses</option>
                </select>
              </div>
            </div>
            
            <div class="form-group" style="margin-bottom: 0;">
              <label>Vagas</label>
              <input v-model="editStageForm.slots" @keydown.enter="$event.target.blur()" type="text" class="form-input"/>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-footer-back" @click="closeEditStageModal">Cancelar</button>
          <button class="btn-footer-continue" @click="saveEditStage">Salvar</button>
        </div>
      </div>
    </div>

    <div v-if="showDeleteConfirmModal" class="modal-overlay" @click="cancelDeleteStage">
      <div class="modal-content modal-sm" @click.stop> 
        <div class="modal-header">
          <div>
            <h3>Excluir Etapa</h3>
            <p class="modal-subtitle">Esta ação não poderá ser desfeita.</p>
          </div>
          <button class="btn-close-modal" @click="cancelDeleteStage">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <p class="delete-confirm-text">Deseja excluir a etapa <strong>{{ stageToDelete?.title }}</strong>?</p>
        </div>
        
        <div class="modal-footer">
          <button class="btn-footer-back" @click="cancelDeleteStage">Cancelar</button>
          <button class="btn-footer-danger" @click="executeDeleteStage">Excluir</button>
        </div>
      </div>
    </div>

    <div v-if="showRestartConfirmModal" class="modal-overlay" @click="cancelRestartRegistration">
      <div class="modal-content modal-sm" @click.stop>
        <div class="modal-header">
          <div>
            <h3>Reiniciar Cadastro</h3>
            <p class="modal-subtitle">Esta ação não poderá ser desfeita.</p>
          </div>
          <button class="btn-close-modal" @click="cancelRestartRegistration">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <p class="delete-confirm-text">Deseja realmente apagar todos os dados preenchidos e reiniciar o cadastro?</p>
        </div>
        
        <div class="modal-footer">
          <button class="btn-footer-back" @click="cancelRestartRegistration">Cancelar</button>
          <button class="btn-footer-danger" @click="executeRestartRegistration">Reiniciar</button>
        </div>
      </div>
    </div>

    <div v-if="showPublishSuccessModal" class="modal-overlay" @click="closePublishSuccessModal">
      <div class="modal-content modal-sm" @click.stop>
        <div class="modal-header">
          <div>
            <h3>Cadastro concluído</h3>
            <p class="modal-subtitle">Programa cadastrado com sucesso.</p>
          </div>
          <button class="btn-close-modal" @click="closePublishSuccessModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <p class="delete-confirm-text">Seu programa foi cadastrado com sucesso!</p>
        </div>

        <div class="modal-footer">
          <button class="btn-footer-continue" @click="closePublishSuccessModal">Fechar</button>
        </div>
      </div>
    </div>

    <div v-if="showCustomFieldModal" class="modal-overlay" @click="closeCustomFieldModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div>
            <h3>Adicionar Campo Personalizado</h3>
            <p class="modal-subtitle">Crie um novo campo para o formulário de inscrição</p>
          </div>
          <button class="btn-close-modal" @click="closeCustomFieldModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="form-group relative">
            <label>Nome do Campo <span class="required">*</span></label>
            <input v-model="customFieldForm.name" @input="customFieldFormError = false" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Link do Portfólio, Gênero, etc." :class="['form-input', { 'input-error': customFieldFormError }]"/>
            <span v-if="customFieldFormError" class="error-text">O campo de Nome não pode ficar vazio.</span>
          </div>
             
          <div class="form-row two-cols" style="margin-bottom: 0;">
            <div class="form-group" style="margin-bottom: 0;">
              <label>Tipo de Resposta</label>
              <select v-model="customFieldForm.type" class="form-input form-select">
                <option value="text">Texto Curto</option>
                <option value="textarea">Texto Longo</option>
                <option value="number">Número</option>
                <option value="url">Link / URL</option>
                <option value="select">Múltipla Escolha</option>
              </select>
            </div>
               
            <div class="form-group" style="margin-bottom: 0; display: flex; flex-direction: column; justify-content: center;">
              <label>Campo Obrigatório?</label>
              <label class="toggle-switch" style="margin-top: 4px;">
                <input type="checkbox" v-model="customFieldForm.required" class="toggle-input">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-footer-back" @click="closeCustomFieldModal">Cancelar</button>
          <button class="btn-footer-continue" @click="saveCustomField">Adicionar Campo</button>
        </div>
      </div>
    </div>

    <div v-if="showCustomDocumentModal" class="modal-overlay" @click="closeCustomDocumentModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div>
            <h3>Adicionar Documento</h3>
            <p class="modal-subtitle">Configure o novo documento exigido na inscrição</p>
          </div>
          <button class="btn-close-modal" @click="closeCustomDocumentModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="form-group relative">
            <label>Nome do Documento <span class="required">*</span></label>
            <input v-model="customDocumentForm.name" @input="customDocumentFormError = false" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Histórico Escolar, Carta de Recomendação, etc." :class="['form-input', { 'input-error': customDocumentFormError }]"/>
            <span v-if="customDocumentFormError" class="error-text">O nome do documento não pode ficar vazio.</span>
          </div>
          
          <div class="form-row two-cols" style="margin-bottom: 0; align-items: flex-start;">
            <div class="form-group" style="margin-bottom: 0;">
              <label>Tipos Aceitos <span class="required">*</span></label>
              <div :class="['doc-types-container', { 'error-border': customDocumentTypeError }]">
                <span v-for="docType in availableDocTypes" :key="docType" @click="toggleDocType(docType)" :class="['doc-type-pill', { 'active': customDocumentForm.types.includes(docType) }]">
                  {{ docType }}
                </span>
              </div>
              <span v-if="customDocumentTypeError" class="error-text">Selecione ao menos um tipo de arquivo.</span>
            </div>
            
            <div class="form-group" style="margin-bottom: 0; display: flex; flex-direction: column; justify-content: flex-start;">
              <label>Documento Obrigatório?</label>
              <label class="toggle-switch" style="margin-top: 4px;">
                <input type="checkbox" v-model="customDocumentForm.required" class="toggle-input">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-footer-back" @click="closeCustomDocumentModal">Cancelar</button>
          <button class="btn-footer-continue" @click="saveCustomDocument">Adicionar Documento</button>
        </div>
      </div>
    </div>

    <div v-if="showNewCourseModal" class="modal-overlay" @click="closeNewCourseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div>
            <h3>Adicionar Novo Curso</h3>
            <p class="modal-subtitle">Cadastre um novo curso para a etapa de nivelamento</p>
          </div>
          <button class="btn-close-modal" @click="closeNewCourseModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="form-row" style="display: grid; grid-template-columns: 3fr 1fr; gap: 16px; margin-bottom: 16px;">
            <div class="form-group relative" style="margin-bottom: 0;">
              <label>Nome do Curso <span class="required">*</span></label>
              <input v-model="newCourseForm.name" @input="newCourseFormError = false" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: Lógica de Programação II" :class="['form-input', { 'input-error': newCourseFormError }]"/>
              <span v-if="newCourseFormError" class="error-text">O nome do curso não pode ficar vazio.</span>
            </div>
            <div class="form-group" style="margin-bottom: 0;">
              <label>Carga Horária</label>
              <input v-model="newCourseForm.hours" @keydown.enter="$event.target.blur()" type="text" placeholder="Ex: 40h" class="form-input"/>
            </div>
          </div>
             
          <div class="form-row" style="display: flex; align-items: flex-start; gap: 32px; margin-bottom: 0;">
            <div class="form-group" style="margin-bottom: 0;">
              <label>Obrigatório?</label>
              <label class="toggle-switch" style="margin-top: 4px;">
                <input type="checkbox" v-model="newCourseForm.required" class="toggle-input">
                <span class="toggle-slider"></span>
              </label>
            </div>
            <div class="form-group" style="margin-bottom: 0;">
              <label>Pontua na Nota?</label>
              <label class="toggle-switch" style="margin-top: 4px;">
                <input type="checkbox" v-model="newCourseForm.scores" class="toggle-input">
                <span class="toggle-slider"></span>
              </label>
            </div>
            <div class="form-group" style="margin-bottom: 0; margin-left: auto; width: 140px;">
              <label>Status</label>
              <select v-model="newCourseForm.status" class="form-input form-select">
                <option value="Ativo">Ativo</option>
                <option value="Inativo">Inativo</option>
              </select>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-footer-back" @click="closeNewCourseModal">Cancelar</button>
          <button class="btn-footer-continue" @click="saveNewCourse">Adicionar Curso</button>
        </div>
      </div>
    </div>

    <div class="header">
      <div class="header-left">
        <div style="display: flex; align-items: center; gap: 12px;">
          <h1>{{ isEditMode ? 'Editar Programa' : 'Cadastro de Programa' }}</h1>
          <span v-if="isEditMode" style="background-color: #fef3c7; color: #92400e; padding: 4px 12px; border-radius: 4px; font-size: 12px; font-weight: 500;">Modo Edição</span>
        </div>
        <p class="subtitle">{{ isEditMode ? 'Altere os dados do programa' : 'Configure e publique novos editais' }}</p>
      </div>
    </div>

    <div class="registration-container">
      
      <div class="sidebar">
        <div class="steps">
          <div v-for="(title, index) in stepTitles" :key="index">
            <div :class="['step', { active: currentStep === index + 1, completed: stepStatuses[index] === 'completed', warning: stepStatuses[index] === 'warning' }]" @click="goToStep(index + 1)">
               
               <div :class="['step-icon', { outline: stepStatuses[index] === 'pending' && currentStep !== index + 1 }]">
                  <svg v-if="stepStatuses[index] === 'completed'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  <svg v-else-if="stepStatuses[index] === 'warning'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                    <polyline points="14 2 14 8 20 8"></polyline>
                  </svg>
               </div>
               
               <div class="step-label">
                  <span class="step-title">{{ title }}</span>
                  <span class="step-desc">{{ stepDescs[index] }}</span>
               </div>
            </div>
            
            <div v-if="index < 5" class="step-divider"></div>
          </div>
        </div>
      </div>

      <div class="content">
        
        <DataProgramRegistrationView 
          v-if="currentStep === 1"
          :formData="formData" 
          :displayDates="displayDates"
          :activeDatePicker="activeDatePicker"
          :calendarDate="calendarDate"
          :weekDays="weekDays"
          :monthNames="monthNames"
          :calendarDays="calendarDays"
          :isEmailInvalid="isEmailInvalid"
          @update-email-touched="emailTouched = true"
          :newPartnerName="newPartnerName"
          @update-partner-name="newPartnerName = $event"
          @add-partner="addPartner"
          @remove-partner="removePartner"
          @open-date-picker="openDatePicker"
          @parse-date-input="parseDateInput"
          @select-date="selectDate"
          @prev-month="prevMonth"
          @next-month="nextMonth"
          :isSelectedDay="isSelectedDay"
          :isToday="isToday"
        />

        <EstructureProgramRegistrationView 
          v-else-if="currentStep === 2"
          :stageList="stageList"
          @open-edit-stage-modal="openEditStageModal"
          @duplicate-stage="duplicateStage"
          @confirm-delete-stage="confirmDeleteStage"
          @open-new-stage-modal="openNewStageModal"
          @restore-default-stages="restoreDefaultStages"
        />

        <FirstStageProgramRegistrationView 
          v-else-if="currentStep === 3"
          :inscriptionForm="inscriptionForm"
          :displayDates="displayDates"
          :activeDatePicker="activeDatePicker"
          :calendarDate="calendarDate"
          :weekDays="weekDays"
          :monthNames="monthNames"
          :calendarDays="calendarDays"
          @open-date-picker="openDatePicker"
          @parse-date-input="parseDateInput"
          @select-date="selectDate"
          @prev-month="prevMonth"
          @next-month="nextMonth"
          @open-custom-field-modal="openCustomFieldModal"
          @open-custom-document-modal="openCustomDocumentModal"
          :isSelectedDay="isSelectedDay"
          :isToday="isToday"
        />

        <SecondStageProgramRegistrationView 
          v-else-if="currentStep === 4"
          :nivelamentoForm="nivelamentoForm"
          :displayDates="displayDates"
          :activeDatePicker="activeDatePicker"
          :calendarDate="calendarDate"
          :weekDays="weekDays"
          :monthNames="monthNames"
          :calendarDays="calendarDays"
          :reqCoursesCount="reqCoursesCount"
          :optCoursesCount="optCoursesCount"
          @open-date-picker="openDatePicker"
          @parse-date-input="parseDateInput"
          @select-date="selectDate"
          @prev-month="prevMonth"
          @next-month="nextMonth"
          @parse-time-input="parseTimeInput"
          @open-new-course-modal="openNewCourseModal"
          @weights-invalid="onWeightsInvalid"
          @weights-valid="onWeightsValid"
          :isSelectedDay="isSelectedDay"
          :isToday="isToday"
        />

        <ThirdStageProgramRegistrationView 
          v-else-if="currentStep === 5"
          :currentStep="currentStep"
          :imersaoForm="imersaoForm"
          :displayDates="displayDates"
          :activeDatePicker="activeDatePicker"
          :calendarDate="calendarDate"
          :weekDays="weekDays"
          :monthNames="monthNames"
          :calendarDays="calendarDays"
          @open-date-picker="openDatePicker"
          @parse-date-input="parseDateInput"
          @select-date="selectDate"
          @prev-month="prevMonth"
          @next-month="nextMonth"
          @parse-time-input-imersao="parseTimeInputImersao"
          @evaluation-weights-invalid="onEvaluationWeightsInvalid"
          @evaluation-weights-valid="onEvaluationWeightsValid"
          :isSelectedDay="isSelectedDay"
          :isToday="isToday"
        />

        <RevisionProgramRegistrationView 
          v-else-if="currentStep === 6"
          :currentStep="currentStep"
          :formData="formData"
          :stageList="stageList"
          :inscriptionForm="inscriptionForm"
          :nivelamentoForm="nivelamentoForm"
          :imersaoForm="imersaoForm" 
          :displayDates="displayDates"
          :isEditMode="isEditMode"
          @go-to-step="goToStep"
          @save-draft="saveDraft"
          @publish-program="publishProgram"
          @update-status="handleRevisionStatus" 
        /> <div v-if="currentStep < 6" class="form-actions-footer">
           <button v-if="currentStep > 1" class="btn-footer-back" @click="prevStep">Voltar</button>
           <button class="btn-footer-continue ml-auto" @click="nextStep">Continuar →</button>
        </div>

      </div>

      <div class="summary-sidebar">
         
         <h3 style="margin-top: 0;">Resumo do Edital</h3>
         <p class="summary-desc">Preencha os dados do programa para ver o resumo</p>
         
         <div class="sidebar-actions-container" style="margin-bottom: 32px;">
            <button class="btn-sidebar-draft" @click="saveDraft">
               <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                 <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                 <polyline points="17 21 17 13 7 13 7 21"></polyline>
                 <polyline points="7 3 7 8 15 8"></polyline>
               </svg>
               Salvar Rascunho
            </button>

            <button class="btn-sidebar-restart" @click="restartRegistration">
               <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                 <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
                 <path d="M3 3v5h5"></path>
               </svg>
               Reiniciar Cadastro
            </button>
         </div>

         <div class="summary-block">
            <span class="summary-label">Status</span>
            <span :class="['status-badge', isDraftSaved ? 'badge-saved' : 'badge-unsaved']" style="display: inline-block;">
              {{ isDraftSaved ? 'Rascunho Salvo' : 'Rascunho Não Salvo' }}
            </span>
         </div>
         
         <div class="summary-block">
            <span class="summary-label">Etapas do Processo</span>
            <div class="timeline">
               <template v-for="(stage, index) in stageList" :key="stage.id">
                 <div class="timeline-item">
                    <div class="timeline-number">{{ stage.id }}</div> 
                    <div class="timeline-content">
                       <h4>{{ stage.title }}</h4>
                       <p>{{ stage.modality }}</p>
                       <span class="timeline-tag">{{ stage.slots }}</span> 
                    </div>
                 </div>
                 <div v-if="index < stageList.length - 1" class="timeline-line"></div>
               </template>
            </div>
         </div>
         
         <div class="summary-footer">
            Última atualização: Hoje às {{ lastUpdatedTime }}
         </div>
         
      </div>
    </div>
  </div>
</template>

<script>
// Importação dos componentes filhos que representam o conteúdo de cada etapa (aba) do formulário
import DataProgramRegistrationView from './components/DataProgramRegistrationView.vue';
import EstructureProgramRegistrationView from './components/EstructureProgramRegistrationView.vue';
import FirstStageProgramRegistrationView from './components/FirstStageProgramRegistrationView.vue';
import SecondStageProgramRegistrationView from './components/SecondStageProgramRegistrationView.vue';
import ThirdStageProgramRegistrationView from './components/ThirdStageProgramRegistrationView.vue';
import RevisionProgramRegistrationView from './components/RevisionProgramRegistrationView.vue';
import { programService } from '../../services/programService';

export default {
  // Nome principal do componente pai que engloba toda a tela de cadastro
  name: 'ProgramRegistrationView',
  
  // Registro dos componentes importados para uso no template
  components: {
    DataProgramRegistrationView, EstructureProgramRegistrationView, FirstStageProgramRegistrationView, SecondStageProgramRegistrationView, ThirdStageProgramRegistrationView, RevisionProgramRegistrationView
  },
  
  // Variáveis de estado global da aplicação
  data() {
    return {
      lastUpdatedTime: '', // Armazena a string com o horário da última modificação (ex: "14:30")
      currentStep: 1, // Controla qual das 6 abas está ativa e visível no momento
      isDraftSaved: false, // Flag que controla se a pílula verde de "Rascunho Salvo" deve aparecer
      stepStatuses: ['pending', 'pending', 'pending', 'pending', 'pending', 'pending'], // Status de validação de cada etapa para pintar as bolinhas laterais ('pending', 'warning', 'completed')
       
      isRevisionValid: false, // Flag que controla se a aba 6 está 100% validada sem erros
      isEditMode: false, // Flag que controla se estamos editando um programa existente
      editingProgramId: null, // ID do programa sendo editado (se aplicável)
      isWeightsValid: true, // Flag que controla se a soma dos pesos de prova + opcionais está dentro de 100%
      isEvaluationWeightsValid: true, // Flag que controla se a soma dos pesos de avaliação parcial + final está dentro de 100%

      // Textos exibidos nos botões do menu lateral esquerdo
      stepTitles: ['Dados do programa', 'Estrutura das etapas', 'Inscrição', 'Nivelamento', 'Imersão', 'Revisão final'],
      stepDescs: ['Informações gerais', 'Definição do fluxo', 'Formulário e elegibilidade', 'Cursos e avaliação', 'Projetos e benefícios', 'Validar e publicar'],
       
      programService, // Importa o serviço de programa
      newPartnerName: '', // Armazena temporariamente o texto digitado no input de adicionar parceiro (Aba 1)
      emailTouched: false, // Controla se o usuário já focou no campo de e-mail para ativar a validação de erro (Aba 1)
      
      // OBJETO: Aba 1 - Informações gerais do edital
      formData: { 
        programName: '', batchName: '', executor: '', objective: '', partners: [], location: '', supportEmail: '', officialWebsite: '', publishDate: '', startDate: '', endDate: '', inscStart: '', inscEnd: '', nivStart: '', nivEnd: '', nivExamDate: '', imerStart: '', imerEnd: '', status: 'Rascunho', observations: '' 
      },
      
      // OBJETO: Aba 3 - Configuração do formulário de inscrição, documentos e cotas
      inscriptionForm: {
        title: 'Inscrição', type: 'Inscrição / Triagem', desc: 'Etapa de inscrições e triagem inicial dos candidatos', situation: 'Obrigatória', targetAudience: '', educationReqs: '', requireLinkedin: false, requireCPF: true, singleRegistration: true,
        fields: [
          { name: 'Nome completo', required: true }, { name: 'E-mail', required: true }, { name: 'CPF', required: true }, { name: 'Data de nascimento', required: true }, { name: 'Telefone', required: true }, { name: 'LinkedIn', required: false }, { name: 'Município', required: true }, { name: 'Estado', required: true }, { name: 'Formação', required: true }, { name: 'Instituição de ensino', required: false }, { name: 'Grau de escolaridade', required: true },
        ],
        documents: [
          { name: 'Documento de identidade', types: 'PDF, JPG', required: true }, { name: 'CPF', types: 'PDF, JPG', required: true }, { name: 'Comprovante de escolaridade', types: 'PDF', required: true },
        ],
        quotas: { ampla: 50, pcd: 10, negros: 30, mulheres: 0, age45: 10, singleQuota: true, revertUnfilled: true },
        classification: { active: true, count: 250, criteria: 'Ordem de inscrição', tiebreaker: '', waitlist: true, allowAppeals: true, appealDeadline: '' }
      },
      
      // OBJETO: Aba 4 - Configuração do Nivelamento (cursos, prova final e pesos)
      nivelamentoForm: {
        title: 'Nivelamento', modality: 'Remota Assíncrona', workload: '172h', platform: '',
        courses: [
          { name: 'Introdução à plataforma', hours: '2h', required: true, scores: false, status: 'Ativo' },
          { name: 'Lógica de programação', hours: '40h', required: true, scores: true, status: 'Ativo' },
          { name: 'Programação Python', hours: '60h', required: true, scores: true, status: 'Ativo' },
          { name: 'Organização de computadores', hours: '30h', required: true, scores: true, status: 'Ativo' },
          { name: 'Banco de dados', hours: '40h', required: true, scores: true, status: 'Ativo' },
          { name: 'Empreendedorismo e gerência de projetos', hours: '20h', required: false, scores: true, status: 'Ativo' },
          { name: 'Desenvolvimento mobile', hours: '30h', required: false, scores: true, status: 'Ativo' },
          { name: 'Business Intelligence', hours: '25h', required: false, scores: true, status: 'Ativo' },
          { name: 'Big Data', hours: '30h', required: false, scores: true, status: 'Ativo' },
          { name: 'Inteligência Artificial', hours: '35h', required: false, scores: true, status: 'Ativo' },
          { name: 'IoT', hours: '20h', required: false, scores: true, status: 'Ativo' },
          { name: 'Engenharia de Requisitos', hours: '15h', required: false, scores: true, status: 'Ativo' },
          { name: 'Treinamento de IA', hours: '25h', required: false, scores: true, status: 'Ativo' },
          { name: 'UI/UX', hours: '30h', required: false, scores: true, status: 'Ativo' },
        ],
        completionRules: { requireActivities: true, requireMinScore: true, minScoreValue: '70', requireFinalEval: true },
        finalExam: { active: true, time: '', duration: 120, questionsCount: 50, type: 'Múltipla Escolha', sequential: true, allowBack: false, mandatorySubmit: true },
        grading: { examWeight: 70, optionalWeight: 30, bonusLocation: 5, minPassingScore: 50, cutOffRule: false, approvedCount: 50, generatePrelimList: true, allowAppeals: true, generateFinalList: true },
        certification: { active: true, criteria: 'Aprovação na etapa', defaultText: '' }
      },

      // OBJETO: Aba 5 - Configurações da Etapa de Imersão
      imersaoForm: {
        nome: 'Imersão', modalidade: 'Híbrida', cargaHoraria: 960, duracaoMeses: 6, mediaHoras: 40,
        local: '', nomeProfessor: '',
        hasProjetos: true, hasGrupos: true, minGroup: 3, maxGroup: 5, orientador: 'vinculado',
        presenca: {
          encontrosObrigatorios: true, frequenciaSemanal: true, minFrequencia: 75, 
          diaPadrao: '', horarioPadrao: '', contabilizarFaltas: true,
          regraAdvertencia: true, regraExclusao3: true, regraExclusao6: true,
          responsavelJustificativa: ''
        },
        avaliacoes: { 
          pesoParcial: 40, pesoFinal: 60, minScore: 70, 
          regraAprovacao: 'Baseado em % da maior nota',
          parcialCriterios: { grupo: true, banca: true, apresentacao: true, participacao: true, comportamento: true },
          finalCriterios: { grupo: true, apresentacao: true, participacao: true }
        },
        beneficios: { 
          hasBolsa: true, valorBolsa: '1.500', mesesBolsa: 6, 
          exigirContaBanco: true, exigirAssinaturaRecibo: true,
          hasNotebook: true, modeloNotebook: '', prazoDevolucaoNotebook: '',
          exigirContrato: true, prazoContrato: 5
        }
      },

      // Variáveis de controle para abertura e dados temporários dos Modais (Popups)
      showNewStageModal: false,
      newStageForm: { title: '', type: '', modality: 'Online', durationValue: '', durationUnit: 'dias', slots: '', desc: '' },
      showEditStageModal: false,
      editStageForm: { id: null, title: '', type: '', modality: 'Online', durationValue: '', durationUnit: 'dias', slots: '', desc: '' },
      showDeleteConfirmModal: false,
      stageToDelete: null,
      showRestartConfirmModal: false, 
      showPublishSuccessModal: false,
      showCustomFieldModal: false,
      customFieldFormError: false, 
      customFieldForm: { name: '', type: 'text', required: true },
      showCustomDocumentModal: false,
      customDocumentFormError: false, 
      customDocumentTypeError: false, 
      customDocumentForm: { name: '', types: ['PDF'], required: true },
      availableDocTypes: ['PDF', 'DOCX', 'JPG', 'PNG', 'XLSX', 'ZIP'], // Tipos de arquivos permitidos no modal de documento
      showNewCourseModal: false,
      newCourseFormError: false,
      newCourseForm: { name: '', hours: '', required: false, scores: true, status: 'Ativo' },
      
      stageList: [], // Array principal que guarda todas as etapas da Aba 2 (linha do tempo)
      
      // Controle centralizado de todas as datas no formato visual (DD/MM/AAAA)
      displayDates: { 
        publishDate: '', startDate: '', endDate: '', 
        inscStart: '', inscEnd: '', 
        nivStart: '', nivEnd: '', nivExamDate: '', 
        imerStart: '', imerEnd: '',
        prazoDevolucaoNotebook: '' 
      },     

      // Componentes de inteligência e navegação do Calendário Customizado
      activeDatePicker: null, // Guarda o nome do campo (ex: 'startDate') que está com o calendário aberto
      calendarDate: new Date(), // Mês e ano âncora exibidos no topo do calendário aberto
      weekDays: ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'],
      monthNames: ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro']
    };
  },

  // Lifecycle Hook: Executado assim que a tela termina de ser montada no navegador
  mounted() { 
    this.updateLastModifiedTime(); // Inicializa o relógio
    
    // Verificar se estamos em modo de edição
    const programIdToEdit = this.$route.query.edit;
    if (programIdToEdit) {
      this.isEditMode = true;
      this.editingProgramId = parseInt(programIdToEdit, 10);
      this.loadProgramForEdit();
      return; // Sai do fluxo normal de carregamento de rascunho
    }
    
    // Tentativa de resgatar rascunhos anteriores salvos no cache (localStorage) do navegador
    const savedStep = localStorage.getItem('programDraftStep');
    if (savedStep) {
      this.currentStep = parseInt(savedStep, 10);
      this.isDraftSaved = true;
      
      // Restaura os status visuais (cores) da barra lateral
      const savedStatuses = localStorage.getItem('draftStepStatuses');
      if (savedStatuses) this.stepStatuses = JSON.parse(savedStatuses);
      
      // Restaura todos os objetos de dados de volta ao estado do Vue
      const savedFormData = localStorage.getItem('draftFormData');
      if (savedFormData) this.formData = JSON.parse(savedFormData);
      
      const savedInscription = localStorage.getItem('draftInscriptionForm');
      if (savedInscription) this.inscriptionForm = JSON.parse(savedInscription);
      
      const savedNivelamento = localStorage.getItem('draftNivelamentoForm');
      if (savedNivelamento) this.nivelamentoForm = JSON.parse(savedNivelamento);
      
      // Mescla segura (Safe Merge) para evitar erro no Vue caso o rascunho antigo não tenha os objetos filhos novos da Aba 5
      const savedImersao = localStorage.getItem('draftImersaoForm');
      if (savedImersao) {
        const parsedImersao = JSON.parse(savedImersao);
        this.imersaoForm = {
          ...this.imersaoForm, // Mantém os defaults (incluindo as chaves novas)
          ...parsedImersao, // Sobrescreve com o que já foi salvo
          presenca: { ...this.imersaoForm.presenca, ...(parsedImersao.presenca || {}) },
          avaliacoes: { ...this.imersaoForm.avaliacoes, ...(parsedImersao.avaliacoes || {}) },
          beneficios: { ...this.imersaoForm.beneficios, ...(parsedImersao.beneficios || {}) }
        };
      }
      
      const savedStageList = localStorage.getItem('draftStageList');
      if (savedStageList) {
        this.stageList = JSON.parse(savedStageList);
      } else {
        this.restoreDefaultStages(); // Fallback de segurança se a lista corromper
      }
      
      const savedDisplayDates = localStorage.getItem('draftDisplayDates');
      if (savedDisplayDates) this.displayDates = JSON.parse(savedDisplayDates);
    } else {
      // Caso seja a primeira vez abrindo a página (sem rascunho)
      this.currentStep = 1;
      this.restoreDefaultStages(); 
    }
  },

  // Watchers vigiam propriedades. Se elas mudarem, executam a função interna
  watch: {
    // Bloco de sincronização: Quando o sistema altera a data formato ISO (BD), atualiza a data visual da tela (DD/MM/AAAA)
    'formData.publishDate'(val) { this.displayDates.publishDate = this.formatDateDisplay(val); },
    'formData.startDate'(val) { this.displayDates.startDate = this.formatDateDisplay(val); },
    'formData.endDate'(val) { this.displayDates.endDate = this.formatDateDisplay(val); },
    'formData.inscStart'(val) { this.displayDates.inscStart = this.formatDateDisplay(val); },
    'formData.inscEnd'(val) { this.displayDates.inscEnd = this.formatDateDisplay(val); },
    'formData.nivStart'(val) { this.displayDates.nivStart = this.formatDateDisplay(val); },
    'formData.nivEnd'(val) { this.displayDates.nivEnd = this.formatDateDisplay(val); },
    'formData.nivExamDate'(val) { this.displayDates.nivExamDate = this.formatDateDisplay(val); },
    'formData.imerStart'(val) { this.displayDates.imerStart = this.formatDateDisplay(val); },
    'formData.imerEnd'(val) { this.displayDates.imerEnd = this.formatDateDisplay(val); },
    'imersaoForm.beneficios.prazoDevolucaoNotebook'(val) { this.displayDates.prazoDevolucaoNotebook = this.formatDateDisplay(val); },
    
    // Deep Watchers: Detectam qualquer digitação dentro de objetos complexos para atualizar o relógio e remover o status "Salvo"
    formData: { handler() { this.updateLastModifiedTime(); this.isDraftSaved = false; }, deep: true },
    inscriptionForm: { handler() { this.updateLastModifiedTime(); this.isDraftSaved = false; }, deep: true },
    nivelamentoForm: { handler() { this.updateLastModifiedTime(); this.isDraftSaved = false; }, deep: true },
    imersaoForm: { handler() { this.updateLastModifiedTime(); this.isDraftSaved = false; }, deep: true },
    stageList: { handler() { this.updateLastModifiedTime(); this.isDraftSaved = false; }, deep: true }
  },

  // Propriedades Computadas (variáveis inteligentes recalculadas automaticamente)
  computed: {
    // Filtra e conta os cursos obrigatórios da Aba 4
    reqCoursesCount() { return this.nivelamentoForm.courses.filter(c => c.required).length; },
    
    // Filtra e conta os cursos opcionais da Aba 4
    optCoursesCount() { return this.nivelamentoForm.courses.filter(c => !c.required).length; },
    
    // Verifica usando regex se o e-mail digitado possui formato válido
    isEmailInvalid() {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return this.formData.supportEmail && !emailRegex.test(this.formData.supportEmail);
    },
    
    // Motor geométrico do calendário: gera as casas nulas para deslocar os dias da semana e preenche até o dia 31/30
    calendarDays() {
      const year = this.calendarDate.getFullYear();
      const month = this.calendarDate.getMonth();
      const firstDay = new Date(year, month, 1).getDay(); 
      const daysInMonth = new Date(year, month + 1, 0).getDate(); 
      const days = [];
      for (let i = 0; i < firstDay; i++) { days.push(null); }
      for (let i = 1; i <= daysInMonth; i++) { days.push(i); }
      return days;
    }
  },

  methods: {
    // Escuta a emissão de evento da aba 6 para saber se ela já passou nas verificações automáticas
    handleRevisionStatus(isValid) {
      this.isRevisionValid = isValid;
      // Se estivermos vendo a tela 6 no momento, forçamos a atualização da bolinha visual
      if (this.currentStep === 6) {
        this.stepStatuses[5] = isValid ? 'completed' : 'warning';
      }
    },

    // Pega a hora atual do sistema operacional e formata com dois dígitos
    updateLastModifiedTime() {
      const now = new Date();
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      this.lastUpdatedTime = `${hours}:${minutes}`;
    },
    
    // Avalia a aba sendo exibida no momento para determinar se preencheu o mínimo necessário (Altera bolinha pra amarelo/azul)
    validateCurrentStep() {
      let isValid = true;
      if (this.currentStep === 1) {
        isValid = !!(this.formData.programName && this.formData.batchName && this.formData.executor && this.formData.objective && this.formData.supportEmail && this.displayDates.startDate && this.displayDates.endDate);
      } else if (this.currentStep === 2) {
        isValid = this.stageList.length > 0;
      } else if (this.currentStep === 3) {
        isValid = !!(this.displayDates.inscStart && this.displayDates.inscEnd);
      } else if (this.currentStep === 4) {
        isValid = !!(this.nivelamentoForm.title && this.displayDates.nivStart && this.displayDates.nivEnd && this.isWeightsValid);
      } else if (this.currentStep === 5) {
        isValid = !!(this.imersaoForm.nome && this.displayDates.imerStart && this.displayDates.imerEnd && this.isEvaluationWeightsValid);
      } else if (this.currentStep === 6) {
        // Agora a aba 6 só fica azul de fato se todas as validações forem concluídas!
        isValid = this.isRevisionValid;
      }
      this.stepStatuses[this.currentStep - 1] = isValid ? 'completed' : 'warning';
    },

    // Handler para quando os pesos da prova + opcionais ficam inválidos
    onWeightsInvalid() {
      this.isWeightsValid = false;
      this.validateCurrentStep();
    },

    // Handler para quando os pesos da prova + opcionais ficam válidos
    onWeightsValid() {
      this.isWeightsValid = true;
      this.validateCurrentStep();
    },

    // Handler para quando os pesos de avaliação parcial + final ficam inválidos
    onEvaluationWeightsInvalid() {
      this.isEvaluationWeightsValid = false;
      this.validateCurrentStep();
    },

    // Handler para quando os pesos de avaliação parcial + final ficam válidos
    onEvaluationWeightsValid() {
      this.isEvaluationWeightsValid = true;
      this.validateCurrentStep();
    },

    // Grava todos os Data Objects no LocalStorage do navegador convertido para String(JSON)
    saveDraft() {
      this.isDraftSaved = true;
      localStorage.setItem('programDraftStep', this.currentStep.toString());
      localStorage.setItem('draftStepStatuses', JSON.stringify(this.stepStatuses));
      localStorage.setItem('draftFormData', JSON.stringify(this.formData));
      localStorage.setItem('draftInscriptionForm', JSON.stringify(this.inscriptionForm));
      localStorage.setItem('draftNivelamentoForm', JSON.stringify(this.nivelamentoForm));
      localStorage.setItem('draftImersaoForm', JSON.stringify(this.imersaoForm));
      localStorage.setItem('draftStageList', JSON.stringify(this.stageList));
      localStorage.setItem('draftDisplayDates', JSON.stringify(this.displayDates));
    },

    // Abre o Modal de Confirmação de Limpeza
    restartRegistration() {
      this.showRestartConfirmModal = true;
    },
    
    // Esconde o Modal de Confirmação de Limpeza sem fazer nada
    cancelRestartRegistration() {
      this.showRestartConfirmModal = false;
    },
    closePublishSuccessModal() {
      this.showPublishSuccessModal = false;
    },
    
    // Função "Destrutiva" (Nuke): Limpa o LocalStorage e redefine todas as variáveis do State para os seus valores limpos originais
    executeRestartRegistration() {
      localStorage.removeItem('programDraftStep');
      localStorage.removeItem('draftStepStatuses');
      localStorage.removeItem('draftFormData');
      localStorage.removeItem('draftInscriptionForm');
      localStorage.removeItem('draftNivelamentoForm');
      localStorage.removeItem('draftImersaoForm');
      localStorage.removeItem('draftStageList');
      localStorage.removeItem('draftDisplayDates');

      this.currentStep = 1;
      this.isDraftSaved = false;
      this.stepStatuses = ['pending', 'pending', 'pending', 'pending', 'pending', 'pending'];
      this.isRevisionValid = false;
      
      this.formData = { 
        programName: '', batchName: '', executor: '', objective: '', partners: [], location: '', supportEmail: '', officialWebsite: '', publishDate: '', startDate: '', endDate: '', inscStart: '', inscEnd: '', nivStart: '', nivEnd: '', nivExamDate: '', imerStart: '', imerEnd: '', status: 'Rascunho', observations: '' 
      };

      this.inscriptionForm = {
        title: 'Inscrição', type: 'Inscrição / Triagem', desc: 'Etapa de inscrições e triagem inicial dos candidatos', situation: 'Obrigatória', targetAudience: '', educationReqs: '', requireLinkedin: false, requireCPF: true, singleRegistration: true,
        fields: [
          { name: 'Nome completo', required: true }, { name: 'E-mail', required: true }, { name: 'CPF', required: true }, { name: 'Data de nascimento', required: true }, { name: 'Telefone', required: true }, { name: 'LinkedIn', required: false }, { name: 'Município', required: true }, { name: 'Estado', required: true }, { name: 'Formação', required: true }, { name: 'Instituição de ensino', required: false }, { name: 'Grau de escolaridade', required: true },
        ],
        documents: [
          { name: 'Documento de identidade', types: 'PDF, JPG', required: true }, { name: 'CPF', types: 'PDF, JPG', required: true }, { name: 'Comprovante de escolaridade', types: 'PDF', required: true },
        ],
        quotas: { ampla: 50, pcd: 10, negros: 30, mulheres: 0, age45: 10, singleQuota: true, revertUnfilled: true },
        classification: { active: true, count: 250, criteria: 'Ordem de inscrição', tiebreaker: '', waitlist: true, allowAppeals: true, appealDeadline: '' }
      };

      this.nivelamentoForm = {
        title: 'Nivelamento', modality: 'Remota Assíncrona', workload: '172h', platform: '',
        courses: [
          { name: 'Introdução à plataforma', hours: '2h', required: true, scores: false, status: 'Ativo' },
          { name: 'Lógica de programação', hours: '40h', required: true, scores: true, status: 'Ativo' },
          { name: 'Programação Python', hours: '60h', required: true, scores: true, status: 'Ativo' },
          { name: 'Organização de computadores', hours: '30h', required: true, scores: true, status: 'Ativo' },
          { name: 'Banco de dados', hours: '40h', required: true, scores: true, status: 'Ativo' },
          { name: 'Empreendedorismo e gerência de projetos', hours: '20h', required: false, scores: true, status: 'Ativo' },
          { name: 'Desenvolvimento mobile', hours: '30h', required: false, scores: true, status: 'Ativo' },
          { name: 'Business Intelligence', hours: '25h', required: false, scores: true, status: 'Ativo' },
          { name: 'Big Data', hours: '30h', required: false, scores: true, status: 'Ativo' },
          { name: 'Inteligência Artificial', hours: '35h', required: false, scores: true, status: 'Ativo' },
          { name: 'IoT', hours: '20h', required: false, scores: true, status: 'Ativo' },
          { name: 'Engenharia de Requisitos', hours: '15h', required: false, scores: true, status: 'Ativo' },
          { name: 'Treinamento de IA', hours: '25h', required: false, scores: true, status: 'Ativo' },
          { name: 'UI/UX', hours: '30h', required: false, scores: true, status: 'Ativo' },
        ],
        completionRules: { requireActivities: true, requireMinScore: true, minScoreValue: '70', requireFinalEval: true },
        finalExam: { active: true, time: '', duration: 120, questionsCount: 50, type: 'Múltipla Escolha', sequential: true, allowBack: false, mandatorySubmit: true },
        grading: { examWeight: 70, optionalWeight: 30, bonusLocation: 5, minPassingScore: 50, cutOffRule: false, approvedCount: 50, generatePrelimList: true, allowAppeals: true, generateFinalList: true },
        certification: { active: true, criteria: 'Aprovação na etapa', defaultText: '' }
      };

      this.imersaoForm = {
        nome: 'Imersão', modalidade: 'Híbrida', cargaHoraria: 960, duracaoMeses: 6, mediaHoras: 40,
        local: '', nomeProfessor: '',
        hasProjetos: true, hasGrupos: true, minGroup: 3, maxGroup: 5, orientador: 'vinculado',
        presenca: {
          encontrosObrigatorios: true, frequenciaSemanal: true, minFrequencia: 75, 
          diaPadrao: '', horarioPadrao: '', contabilizarFaltas: true,
          regraAdvertencia: true, regraExclusao3: true, regraExclusao6: true,
          responsavelJustificativa: ''
        },
        avaliacoes: { 
          pesoParcial: 40, pesoFinal: 60, minScore: 70, 
          regraAprovacao: 'Baseado em % da maior nota',
          parcialCriterios: { grupo: true, banca: true, apresentacao: true, participacao: true, comportamento: true },
          finalCriterios: { grupo: true, apresentacao: true, participacao: true }
        },
        beneficios: { 
          hasBolsa: true, valorBolsa: '1.500', mesesBolsa: 6, 
          exigirContaBanco: true, exigirAssinaturaRecibo: true,
          hasNotebook: true, modeloNotebook: '', prazoDevolucaoNotebook: '',
          exigirContrato: true, prazoContrato: 5
        }
      };

      this.displayDates = { publishDate: '', startDate: '', endDate: '', inscStart: '', inscEnd: '', nivStart: '', nivEnd: '', nivExamDate: '', imerStart: '', imerEnd: '', prazoDevolucaoNotebook: '' };
      
      this.restoreDefaultStages(); // Reinicia também os cards da linha do tempo da etapa 2
      this.updateLastModifiedTime();
      
      window.scrollTo({ top: 0, behavior: 'smooth' }); // Joga a tela pro topo suavemente
      this.cancelRestartRegistration(); // Fecha o modal
    },

    // Funções de Navegação e Pulo de Tela que acionam a Validação e o Salvamento Automático
    nextStep() { this.validateCurrentStep(); if (this.currentStep < 6) { this.currentStep++; } this.saveDraft(); window.scrollTo({ top: 0, behavior: 'smooth' }); },
    prevStep() { this.validateCurrentStep(); if (this.currentStep > 1) { this.currentStep--; } this.saveDraft(); window.scrollTo({ top: 0, behavior: 'smooth' }); },
    goToStep(num) { this.validateCurrentStep(); this.currentStep = num; this.saveDraft(); window.scrollTo({ top: 0, behavior: 'smooth' }); },
    
    // Preenchimento padrão automático para o array da Aba 2 (Estruturas)
    restoreDefaultStages() {
      this.stageList = [
        { id: 0, title: 'Inscrição', type: 'Inscrição / Triagem', desc: 'Etapa de inscrições e triagem inicial dos candidatos', modality: 'Online', duration: '30 dias', slots: 'Ilimitado', isDefault: true },
        { id: 1, title: 'Nivelamento', type: 'Capacitação', desc: 'Cursos online e avaliação teórica', modality: 'Remota Assíncrona', duration: '3 meses', slots: '250 vagas', isDefault: true },
        { id: 2, title: 'Imersão', type: 'Projeto Prático', desc: 'Desenvolvimento de projetos em grupos', modality: 'Híbrida', duration: '6 meses', slots: '50 vagas', isDefault: true }
      ];
    },
    
    // Funções de Modais da Etapa 2 (Abertura, Fechamento, Edição, Adição)
    openNewStageModal() { this.showNewStageModal = true; },
    closeNewStageModal() { this.showNewStageModal = false; this.newStageForm = { title: '', type: '', modality: 'Online', durationValue: '', durationUnit: 'dias', slots: '', desc: '' }; },
    saveNewStage() {
       if(!this.newStageForm.title) return; 
       const nextId = this.stageList.length > 0 ? Math.max(...this.stageList.map(s => s.id)) + 1 : 0; // Calcula o proximo ID (Index) autoincrementável
       const finalDuration = this.newStageForm.durationValue ? `${this.newStageForm.durationValue} ${this.newStageForm.durationUnit}` : ''; // Une input com select pra texto contínuo
       this.stageList.push({ id: nextId, title: this.newStageForm.title, type: this.newStageForm.type, desc: this.newStageForm.desc, modality: this.newStageForm.modality, duration: finalDuration, slots: this.newStageForm.slots, isDefault: false });
       this.closeNewStageModal();
    },
    openEditStageModal(stage) {
       this.editStageForm = { ...stage }; 
       if (stage.duration) {
          const parts = stage.duration.split(' '); // Split reversa para jogar para a interface que tem separação Input e Select
          this.editStageForm.durationValue = parts[0] || '';
          this.editStageForm.durationUnit = parts[1] || 'dias';
       }
       this.showEditStageModal = true;
    },
    closeEditStageModal() { this.showEditStageModal = false; },
    saveEditStage() {
       const index = this.stageList.findIndex(s => s.id === this.editStageForm.id);
       if (index !== -1) {
          const finalDuration = this.editStageForm.durationValue ? `${this.editStageForm.durationValue} ${this.editStageForm.durationUnit}` : '';
          this.stageList[index] = { ...this.editStageForm, duration: finalDuration };
       }
       this.closeEditStageModal();
    },
    confirmDeleteStage(stage) { this.stageToDelete = stage; this.showDeleteConfirmModal = true; },
    cancelDeleteStage() { this.showDeleteConfirmModal = false; this.stageToDelete = null; },
    executeDeleteStage() {
      if (this.stageToDelete) {
        this.stageList = this.stageList.filter(s => s.id !== this.stageToDelete.id); // Tira da matriz renderizada pelo filtro
        this.cancelDeleteStage();
      }
    },
    duplicateStage(stage) {
      const nextId = this.stageList.length > 0 ? Math.max(...this.stageList.map(s => s.id)) + 1 : 0;
      const duplicatedStage = { ...stage };
      duplicatedStage.id = nextId;
      duplicatedStage.title = `${stage.title} (Cópia)`; 
      duplicatedStage.isDefault = false; // Tira badge de que é raiz do sistema
      const originalIndex = this.stageList.findIndex(s => s.id === stage.id);
      if (originalIndex !== -1) {
        this.stageList.splice(originalIndex + 1, 0, duplicatedStage); // Insere logo abaixo de quem foi duplicado
      } else {
        this.stageList.push(duplicatedStage);
      }
    },
    
    // Funções do Modal da Etapa 3 (Inscrição: Custom Fields)
    openCustomFieldModal() { this.showCustomFieldModal = true; },
    closeCustomFieldModal() {
      this.showCustomFieldModal = false;
      this.customFieldFormError = false; 
      this.customFieldForm = { name: '', type: 'text', required: true };
    },
    saveCustomField() {
      if (!this.customFieldForm.name.trim()) { this.customFieldFormError = true; return; } 
      this.inscriptionForm.fields.push({ name: this.customFieldForm.name, required: this.customFieldForm.required });
      this.closeCustomFieldModal();
    },
    
    // Funções do Modal da Etapa 3 (Inscrição: Custom Documents)
    toggleDocType(type) {
      const index = this.customDocumentForm.types.indexOf(type);
      if (index > -1) { this.customDocumentForm.types.splice(index, 1); } // Se já tem, apaga do array visual (unselect)
      else { this.customDocumentForm.types.push(type); } // Se n tem, injeta
      this.customDocumentTypeError = false;
    },
    openCustomDocumentModal() { this.showCustomDocumentModal = true; },
    closeCustomDocumentModal() {
      this.showCustomDocumentModal = false;
      this.customDocumentFormError = false; 
      this.customDocumentTypeError = false; 
      this.customDocumentForm = { name: '', types: ['PDF'], required: true };
    },
    saveCustomDocument() {
      if (!this.customDocumentForm.name.trim()) { this.customDocumentFormError = true; }
      if (this.customDocumentForm.types.length === 0) { this.customDocumentTypeError = true; } 
      if (this.customDocumentFormError || this.customDocumentTypeError) return;
      this.inscriptionForm.documents.push({ name: this.customDocumentForm.name, types: this.customDocumentForm.types.join(', '), required: this.customDocumentForm.required });
      this.closeCustomDocumentModal();
    },
    
    // Funções do Modal da Etapa 4 (Nivelamento: Novo Curso)
    openNewCourseModal() { this.showNewCourseModal = true; },
    closeNewCourseModal() {
      this.showNewCourseModal = false;
      this.newCourseFormError = false; 
      this.newCourseForm = { name: '', hours: '', required: false, scores: true, status: 'Ativo' };
    },
    saveNewCourse() {
      if (!this.newCourseForm.name.trim()) { this.newCourseFormError = true; return; } 
      this.nivelamentoForm.courses.push({ name: this.newCourseForm.name, hours: this.newCourseForm.hours || '0h', required: this.newCourseForm.required, scores: this.newCourseForm.scores, status: this.newCourseForm.status });
      this.closeNewCourseModal();
    },
    
    // Parceiros (Pílulas Aba 1)
    addPartner() { 
      if (this.newPartnerName.trim() && !this.formData.partners.includes(this.newPartnerName.trim())) { 
        this.formData.partners.push(this.newPartnerName.trim()); 
        this.newPartnerName = ''; // Limpa Input do usuario dps do push
      } 
    },
    removePartner(idx) { this.formData.partners.splice(idx, 1); },
    
    // CALENDÁRIO MASTER: Lógica de Motor, Popups e Máscaras de String
    openDatePicker(f) { 
        if (this.activeDatePicker === f) { this.closeDatePicker(); return; } // Toggle: Se clicou no que já ta aberto, fecha
        this.activeDatePicker = f; 
        const exist = this.formData[f] || this.displayDates[f]; // Ajuste para buscar de displayDates caso o form pai n possua
        if (exist && exist.includes('-')) { 
          const [y, m, d] = exist.split('-'); 
          this.calendarDate = new Date(y, m - 1, d); // Abre o foco visual focado onde vc preencheu antes
        } else { 
          this.calendarDate = new Date(); // Senão, abre foco visual baseando no relógio atual do seu sistema operacional
        } 
    },
    closeDatePicker() { this.activeDatePicker = null; },
    selectDate(d) { 
      if (!d) return; // Bloco que impede o clique nos quadrados fantasmas nulos gerados pra alinhar a geometria da semana da matriz
      const y = this.calendarDate.getFullYear();
      const m = String(this.calendarDate.getMonth() + 1).padStart(2, '0'); // Garante que 3 vira 03
      const day = String(d).padStart(2, '0'); 
      const formattedIso = `${y}-${m}-${day}`;
      
      // Salva tanto no modelo para backend quanto no modelo de exibição
      if(this.formData[this.activeDatePicker] !== undefined) {
         this.formData[this.activeDatePicker] = formattedIso;
      } else if (this.activeDatePicker === 'prazoDevolucaoNotebook') {
         this.imersaoForm.beneficios.prazoDevolucaoNotebook = formattedIso;
      }
      this.closeDatePicker(); 
    },
    prevMonth() { this.calendarDate = new Date(this.calendarDate.getFullYear(), this.calendarDate.getMonth() - 1, 1); },
    nextMonth() { this.calendarDate = new Date(this.calendarDate.getFullYear(), this.calendarDate.getMonth() + 1, 1); },
    
    // Funções injetadas para processamento de strings (Máscaras de inputs via RegEx)
    formatDateDisplay(s) { 
      if (!s) return ''; 
      if (s.includes('/')) return s; // Já está formatado
      const parts = s.split('-'); 
      if(parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`; // Inverte de ymd pra dmy
      return s;
    },
    parseDateInput(f) {
      let v = this.displayDates[f] ? this.displayDates[f].replace(/\D/g, '') : ''; // RegEx que destrói tudo o que não for número digitado
      if (v.length >= 2) { 
        let d = parseInt(v.substring(0, 2)); 
        if (d > 31) v = '31' + v.substring(2); 
        else if (d === 0) v = '01' + v.substring(2); 
      }
      if (v.length >= 4) { 
        let m = parseInt(v.substring(2, 4)); 
        if (m > 12) v = v.substring(0, 2) + '12' + v.substring(4); 
        else if (m === 0) v = v.substring(0, 2) + '01' + v.substring(4); 
      }
      let fmt = v; 
      if (v.length > 2) fmt = v.substring(0, 2) + '/' + v.substring(2); // Injeta a primeira barra D/M
      if (v.length > 4) fmt = fmt.substring(0, 5) + '/' + v.substring(4, 8); // Injeta a segunda barra M/Y
      this.displayDates[f] = fmt;
      
      if (v.length === 8) { 
        const d = v.substring(0, 2); const m = v.substring(2, 4); const y = v.substring(4, 8);
        const obj = new Date(y, m - 1, d); 
        // Checa se o dia geométrico que vc inseriu existiu de fato no calendário gregoriano
        if (obj.getFullYear() == y && obj.getMonth() == m - 1 && obj.getDate() == d) { 
          if(this.formData[f] !== undefined) {
             this.formData[f] = `${y}-${m}-${d}`; 
          } else if (f === 'prazoDevolucaoNotebook') {
             this.imersaoForm.beneficios.prazoDevolucaoNotebook = `${y}-${m}-${d}`;
          }
          this.calendarDate = new Date(y, m - 1, d); 
        } else { 
          if(this.formData[f] !== undefined) this.formData[f] = ''; 
          if(f === 'prazoDevolucaoNotebook') this.imersaoForm.beneficios.prazoDevolucaoNotebook = '';
        } 
      } else { 
        if(this.formData[f] !== undefined) this.formData[f] = ''; 
        if(f === 'prazoDevolucaoNotebook') this.imersaoForm.beneficios.prazoDevolucaoNotebook = '';
      }
    },
    parseTimeInput() {
      let v = this.nivelamentoForm.finalExam.time.replace(/\D/g, ''); 
      if (v.length >= 2) { 
        let h = parseInt(v.substring(0, 2)); 
        if (h > 23) v = '23' + v.substring(2); 
      }
      if (v.length >= 4) { 
        let m = parseInt(v.substring(2, 4)); 
        if (m > 59) v = v.substring(0, 2) + '59'; 
      }
      let fmt = v; 
      if (v.length > 2) { fmt = v.substring(0, 2) + ':' + v.substring(2, 4); } 
      this.nivelamentoForm.finalExam.time = fmt;
    },
    parseTimeInputImersao() {
      let v = this.imersaoForm.presenca.horarioPadrao.replace(/\D/g, ''); 
      if (v.length >= 2) { 
        let h = parseInt(v.substring(0, 2)); 
        if (h > 23) v = '23' + v.substring(2); 
      }
      if (v.length >= 4) { 
        let m = parseInt(v.substring(2, 4)); 
        if (m > 59) v = v.substring(0, 2) + '59'; 
      }
      let fmt = v; 
      if (v.length > 2) { fmt = v.substring(0, 2) + ':' + v.substring(2, 4); } 
      this.imersaoForm.presenca.horarioPadrao = fmt;
    },
    
    // Funções de injeção CSS do calendário
    isSelectedDay(f, d) { 
      if (!d) return false; 
      let formVal = this.formData[f];
      if (f === 'prazoDevolucaoNotebook') formVal = this.imersaoForm.beneficios.prazoDevolucaoNotebook;
      if (!formVal) return false;

      const [y, m, day] = formVal.split('-'); 
      return Number(y) === this.calendarDate.getFullYear() && Number(m) === this.calendarDate.getMonth() + 1 && Number(day) === d; 
    },
    isToday(day) { 
      const t = new Date(); 
      return day && t.getFullYear() === this.calendarDate.getFullYear() && t.getMonth() === this.calendarDate.getMonth() && t.getDate() === day; 
    },

    // Publica o programa após validação final
    async publishProgram() {
      console.log('Publicando edital...', this.formData);
      
      try {
        // Validação básica dos campos obrigatórios
        if (!this.formData.programName || !this.formData.programName.trim()) {
          alert('Nome do programa é obrigatório.');
          return;
        }
        
        if (!this.formData.startDate || !this.formData.endDate) {
          alert('Datas de início e fim são obrigatórias.');
          return;
        }

        // Dados comuns para ambos os casos (criar ou editar)
        const programData = {
          name: this.formData.programName.trim(),
          contractNumber: this.formData.batchName || this.formData.programName,
          startDate: this.formData.startDate,
          endDate: this.formData.endDate,
          targetAudience: this.formData.objective || '',
          executor: this.formData.executor || '',
          location: this.formData.location || '',
          quotaCriteria: JSON.stringify(this.inscriptionForm.quotas),
          evaluationCriteria: JSON.stringify(this.nivelamentoForm.grading)
        };

        let programId;
        
        if (this.isEditMode) {
          // CASO 2: Editar programa existente
          console.log('Atualizando programa com ID:', this.editingProgramId);
          await this.programService.update(this.editingProgramId, programData);
          programId = this.editingProgramId;
          console.log('Programa atualizado com sucesso.');
        } else {
          // CASO 1: Criar novo programa
          const programResponse = await this.programService.create(programData);
          programId = programResponse.id;
          console.log('Programa criado com ID:', programId);
        }

        // PASSO 2: Criar/atualizar a classe/edital a partir do programa
        const classData = {
          nomeTurma: this.formData.batchName || this.formData.programName,
          localidade: this.imersaoForm.local || this.formData.location || '',
          qtdVagas: this.stageList.reduce((total, stage) => total + parseInt(stage.slots || 0), 0),
          publicationDate: this.formData.publishDate,
          applicationStartDate: this.formData.inscStart,
          applicationEndDate: this.formData.inscEnd,
          levelingSelectionAnnouncementDate: this.formData.publishDate,
          levelingStartDate: this.formData.nivStart,
          levelingEndDate: this.formData.nivEnd,
          levelingFinalExamDate: this.formData.nivExamDate,
          immersionStartDate: this.formData.imerStart,
          immersionEndDate: this.formData.imerEnd,
          partialEvaluationDate: this.formData.imerStart,
          finalEvaluationDate: this.formData.imerEnd,
          certificateIssueDate: this.formData.endDate
        };

        if (!this.isEditMode) {
          // Só criar nova classe se estamos criando um novo programa
          const classResponse = await this.programService.createClassFromProgram(programId, classData);
          console.log('Edital (classe) criado com sucesso:', classResponse);
        } else {
          console.log('Em modo de edição - classe não foi criada novamente');
        }
        
        this.executeRestartRegistration(); // Limpa o formulário após publicação
        this.showPublishSuccessModal = true;
      } catch (error) {
        console.error('Erro ao publicar edital:', error);
        const errorMsg = error.response?.data?.message || error.message || 'Erro ao publicar o edital';
        alert('Erro ao publicar: ' + errorMsg);
      }
    },

    // Carrega os dados do programa para edição
    async loadProgramForEdit() {
      try {
        const program = await this.programService.getById(this.editingProgramId);
        console.log('Programa carregado para edição:', program);
        
        // Mapear dados do programa para as estruturas de formulário
        this.mapProgramToFormData(program);
        
        // Marcar como rascunho não salvo para indicar que está carregado
        this.isDraftSaved = true;
        this.currentStep = 1;
      } catch (error) {
        console.error('Erro ao carregar programa para edição:', error);
        alert('Erro ao carregar programa: ' + (error.response?.data?.message || error.message));
        // Redirecionar para programas em caso de erro
        this.$router.push('/programs');
      }
    },

    // Mapeia dados do programa da API para as estruturas de formulário da tela
    mapProgramToFormData(program) {
      // Aba 1 - Dados gerais do programa
      this.formData.programName = program.name || '';
      this.formData.batchName = program.contractNumber || '';
      this.formData.objective = program.targetAudience || '';
      this.formData.startDate = program.startDate || '';
      this.formData.endDate = program.endDate || '';
      
      // Tentar parsear critérios de quota e avaliação
      if (program.quotaCriteria) {
        try {
          this.inscriptionForm.quotas = JSON.parse(program.quotaCriteria);
        } catch (e) {
          console.warn('Não foi possível parsear quotaCriteria');
        }
      }
      
      if (program.evaluationCriteria) {
        try {
          this.nivelamentoForm.grading = JSON.parse(program.evaluationCriteria);
        } catch (e) {
          console.warn('Não foi possível parsear evaluationCriteria');
        }
      }
      
      // Atualizar datas de display
      this.displayDates.startDate = this.formatDateDisplay(program.startDate);
      this.displayDates.endDate = this.formatDateDisplay(program.endDate);
    }
  }
};
</script>

<style>
* { box-sizing: border-box; } /* Aplica box-sizing para todos os elementos */
.program-registration-view { min-height: 100vh; background-color: #fafbfd; padding: 24px; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; } /* Estilo principal da tela de registro */
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; } /* Cabeçalho da página */
.header-left h1 { font-size: 22px; font-weight: 600; color: #1a233a; margin: 0 0 4px 0; } /* Título principal do cabeçalho */
.subtitle { color: #6b7280; margin: 0; font-size: 13px; } /* Subtítulo do cabeçalho */
.registration-container { display: grid; grid-template-columns: 260px 1fr 280px; gap: 32px; align-items: start; } /* Grid principal dividindo as 3 colunas */
.sidebar { position: sticky; top: 90px; height: fit-content; max-height: calc(100vh - 110px); overflow-y: auto; } /* Barra lateral esquerda fixa */
.steps { display: flex; flex-direction: column; } /* Container dos passos na barra lateral */
.step { display: flex; align-items: center; gap: 14px; padding: 16px; border-radius: 8px; cursor: pointer; transition: all 0.2s; } /* Estilo de cada passo (botão) */
.step:hover { background-color: #f3f4f6; } /* Efeito hover no passo */
.step.active { background-color: #e0f2fe; border: 1px solid #bae6fd; } /* Estilo do passo ativo (selecionado) */
.step-icon { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background-color: #0288d1; color: white; transition: all 0.2s; } /* Ícone circular do passo */
.step-icon.outline { background-color: #f3f4f6; color: #9ca3af; } /* Ícone de passo inativo/futuro */
.step.completed .step-icon { background-color: #0288d1; color: white; } /* Ícone de passo concluído */
.step.warning .step-icon { background-color: #f59e0b; color: white; } /* Ícone de passo com aviso (incompleto) */
.step-label { display: flex; flex-direction: column; } /* Container do texto do passo */
.step-title { font-size: 14px; font-weight: 500; color: #1a233a; transition: color 0.2s; } /* Título do passo */
.step.active .step-title, .step.completed .step-title { color: #000000; } /* Cor do título para ativo/concluído */
.step-desc { font-size: 13px; color: #6b7280; transition: color 0.2s; } /* Descrição do passo */
.step.active .step-desc, .step.completed .step-desc { color: #000000; } /* Cor da descrição para ativo/concluído */
.step-divider { width: 1px; height: 16px; background-color: #e5e7eb; margin-left: 32px; } /* Linha conectora entre os passos */
.sidebar::-webkit-scrollbar, .summary-sidebar::-webkit-scrollbar { width: 4px; } /* Largura da barra de rolagem customizada */
.sidebar::-webkit-scrollbar-thumb, .summary-sidebar::-webkit-scrollbar-thumb { background: #e5e7eb; border-radius: 4px; } /* Cor e formato da barra de rolagem */
.content { display: flex; flex-direction: column; justify-content: space-between; min-height: calc(100vh - 120px); } /* Coluna central de conteúdo */
.step-container { display: flex; flex-direction: column; gap: 12px; } /* Container interno da etapa atual */
.step-header h2 { font-size: 20px; color: #1a233a; margin: 0 0 4px 0; } /* Título da etapa no topo do conteúdo */
.step-header p { margin: 0; color: #6b7280; font-size: 14px; } /* Subtítulo da etapa no topo do conteúdo */
.card-section { background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 18px 24px; } /* Estilo dos cartões brancos de formulário */
.section-title { margin-bottom: 16px; } /* Margem inferior do título da seção */
.section-title h3 { font-size: 17px; font-weight: 600; color: #1a233a; margin: 0 0 4px 0; } /* Título h3 de cada cartão */
.section-title p { font-size: 15px; color: #6b7280; margin: 0; } /* Descrição de cada cartão */
.form-row { display: flex; gap: 16px; width: 100%; } /* Linha do formulário (flexbox) */
.form-row.two-cols > .form-group { flex: 1; } /* Linha com 2 colunas divididas igualmente */
.three-cols-special { display: grid; grid-template-columns: 1fr 1.5fr 1fr; gap: 16px; } /* Linha com 3 colunas proporcionais */
.form-group { margin-bottom: 16px; position: relative; } /* Container para label e input */
.form-group label { display: block; font-size: 14px; font-weight: 500; margin-bottom: 6px; color: #374151; } /* Estilo dos labels dos inputs */
.required { color: #ef4444; } /* Asterisco vermelho para campos obrigatórios */
.form-input, .form-textarea { width: 100%; padding: 10px 12px; border: 1px solid transparent; border-radius: 6px; background-color: #f9fafb; font-family: inherit; font-size: 14px; color: #1a233a; transition: all 0.2s; } /* Estilo global de inputs e textareas */
.form-select { appearance: auto; cursor: pointer; padding-left: 8px; } /* Estilo específico para dropdowns (select) */
.form-input:focus, .form-textarea:focus { outline: none; background-color: white; box-shadow: 0 0 0 1px #3b82f6; } /* Efeito de foco nos inputs */
.form-input::placeholder, .form-textarea::placeholder { color: #9ca3af; } /* Cor do texto de placeholder */
.form-textarea { resize: vertical; } /* Permite redimensionar textarea apenas na vertical */
.form-input.input-error { background-color: #fef2f2; box-shadow: 0 0 0 1px #ef4444; } /* Estilo de erro para inputs */
.error-text { display: block; color: #ef4444; font-size: 11px; margin-top: 6px; font-weight: 500; } /* Mensagem de erro abaixo do input */
.duration-input-group { display: flex; gap: 8px; width: 100%; } /* Grupo flex para input de duração e unidade */
.duration-input-group input { flex: 1; } /* Input numérico expandido no grupo de duração */
.duration-input-group select { flex: 1; min-width: 100px; } /* Dropdown expandido no grupo de duração */
.partner-input-row { display: flex; gap: 12px; align-items: center; } /* Linha para input de parceiros */
.partner-input-row .form-input { flex: 1; } /* Input expandido na linha de parceiros */
.btn-add-partner { display: flex; align-items: center; gap: 6px; background-color: #1e1b4b; color: white; border: none; padding: 10px 16px; border-radius: 6px; font-size: 13px; font-weight: 500; cursor: pointer; } /* Botão de adicionar parceiro */
.partner-tags-container { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; } /* Container para as tags/pílulas de parceiros */
.partner-tag { display: flex; align-items: center; gap: 6px; padding: 6px 12px; border: 1px solid #0288d1; background-color: #e1f5fe; color: #01579b; border-radius: 16px; font-size: 13px; font-weight: 500; } /* Estilo individual da tag de parceiro */
.btn-remove-tag { background: transparent; border: none; padding: 0; color: #0288d1; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 50%; transition: all 0.2s; } /* Botão X para remover tag de parceiro */
.info-alert { display: flex; gap: 12px; background-color: #e1f5fe; border: 1px solid #b3e5fc; border-radius: 10px; padding: 16px; margin-bottom: 8px; color: #01579b; } /* Caixa azul de alerta informativo */
.alert-icon { flex-shrink: 0; margin-top: 2px; } /* Ícone da caixa de alerta */
.alert-content strong { display: block; font-size: 14px; margin-bottom: 4px; } /* Título em negrito do alerta */
.alert-content p { margin: 0; font-size: 13px; line-height: 1.4; opacity: 0.9; } /* Texto descritivo do alerta */
.stages-list { display: flex; flex-direction: column; gap: 16px; } /* Container vertical para lista de etapas (cards) */
.stage-card { display: flex; background: white; border: 1px solid #e5e7eb; border-radius: 10px; overflow: hidden; transition: all 0.2s ease; } /* Card individual de etapa */
.stage-card.is-dragged { opacity: 0.4; border: 2px dashed #9ca3af; } /* Estilo do card de etapa quando está sendo arrastado */
.stage-card.is-dragover { border-top: 4px solid #0288d1; margin-top: 8px; } /* Indicador visual onde o card arrastado será solto */
.stage-drag-handle { padding: 0 12px; display: flex; align-items: center; justify-content: center; color: #d1d5db; cursor: grab; border-right: 1px solid #e5e7eb; background: #f9fafb; } /* Alça lateral esquerda para arrastar o card */
.stage-drag-handle:active { cursor: grabbing; } /* Cursor muda ao segurar a alça de arrastar */
.stage-body { padding: 20px; flex: 1; } /* Corpo de conteúdo do card de etapa */
.stage-header { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; } /* Cabeçalho interno do card de etapa */
.stage-number { width: 32px; height: 32px; border-radius: 50%; background-color: #0288d1; color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 14px; } /* Círculo azul com número da etapa */
.stage-titles h4 { margin: 0 0 4px 0; font-size: 15px; color: #1a233a; display: flex; align-items: center; } /* Título interno do card de etapa */
.badge-padrao { background-color: #e1f5fe; color: #0288d1; font-size: 10px; padding: 2px 8px; border-radius: 12px; margin-left: 8px; font-weight: 500; } /* Etiqueta indicando que a etapa é padrão do sistema */
.stage-type { font-size: 12px; color: #6b7280; } /* Texto do tipo da etapa abaixo do título */
.stage-description { margin: 0 0 20px 0; font-size: 13px; color: #4b5563; } /* Parágrafo de descrição dentro do card de etapa */
.stage-info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #f3f4f6; } /* Grid com ícones e métricas da etapa */
.info-item { display: flex; align-items: flex-start; gap: 8px; color: #6b7280; } /* Item individual no grid de métricas */
.info-item svg { margin-top: 2px; color: #9ca3af; } /* Ícone SVG da métrica na etapa */
.info-item small { display: block; font-size: 11px; margin-bottom: 2px; } /* Título pequeno da métrica na etapa */
.info-item span { display: block; font-size: 13px; color: #1a233a; font-weight: 500; } /* Valor numérico/texto da métrica na etapa */
.stage-actions { display: flex; gap: 12px; } /* Container para os botões Editar/Duplicar/Excluir da etapa */
.btn-stage-action { background: white; border: 1px solid #d1d5db; border-radius: 6px; padding: 6px 12px; font-size: 13px; font-weight: 500; color: #374151; cursor: pointer; display: flex; align-items: center; gap: 6px; } /* Estilo dos botões de ação do card da etapa */
.btn-stage-action.btn-danger { color: #fff; border-color: #ef4444; background: #ef4444; } /* Estilo de erro/vermelho para o botão Excluir etapa */
.stage-list-actions { display: flex; gap: 12px; margin-top: 8px; margin-bottom: 40px; } /* Ações globais no rodapé da lista de etapas */
.btn-action-outline { background: white; border: 1px solid #d1d5db; border-radius: 6px; padding: 10px 16px; font-size: 13px; font-weight: 500; color: #374151; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: all 0.2s; } /* Botão vazado para adicionar nova etapa */
.btn-action-outline:hover { background: #f9fafb; border-color: #9ca3af; } /* Efeito hover no botão vazado */
.modal-overlay { position: fixed; inset: 0; background-color: rgba(15, 23, 42, 0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; } /* Fundo escuro atrás dos popups (modais) */
.modal-content { background: white; border-radius: 12px; padding: 24px; width: 100%; max-width: 600px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); } /* Caixa branca principal do modal */
.modal-sm { max-width: 450px; } /* Classe modificadora para modais menores (ex: exclusão) */
.modal-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; } /* Cabeçalho interno do modal */
.modal-header h3 { margin: 0; font-size: 18px; color: #1a233a; font-weight: 600; } /* Título h3 do modal */
.modal-subtitle { margin: 4px 0 0 0; font-size: 13px; color: #6b7280; } /* Subtítulo descritivo do modal */
.btn-close-modal { background: transparent; border: none; color: #9ca3af; cursor: pointer; padding: 4px; border-radius: 6px; display: flex; } /* Botão de fechar (X) do modal */
.modal-body { margin-bottom: 24px; } /* Corpo interno de conteúdo do modal */
.delete-confirm-text { font-size: 14px; color: #374151; line-height: 1.5; margin: 0; } /* Texto de aviso no modal de exclusão */
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 16px; border-top: 1px solid #e5e7eb; } /* Rodapé do modal com botões */
.btn-footer-danger { background-color: #ef4444; color: white; border: none; padding: 10px 24px; border-radius: 6px; font-size: 13px; font-weight: 500; cursor: pointer; transition: opacity 0.2s; } /* Botão vermelho de perigo no modal */
.btn-footer-danger:hover { opacity: 0.9; } /* Efeito hover no botão de perigo */
.form-actions-footer { display: flex; justify-content: space-between; align-items: center; padding: 24px; border-top: 1px solid #e5e7eb; margin-top: auto; } /* Rodapé fixo de navegação Voltar/Continuar */
.ml-auto { margin-left: auto; } /* Utilitário para empurrar botão Continuar para a direita */
.btn-footer-back { background: white; border: 1px solid #d1d5db; border-radius: 6px; padding: 10px 24px; font-size: 13px; font-weight: 500; color: #1a233a; cursor: pointer; transition: all 0.2s; } /* Botão de Voltar etapa ou Cancelar modal */
.btn-footer-continue { background-color: #1e1b4b; color: white; border: none; padding: 10px 24px; border-radius: 6px; font-size: 13px; font-weight: 500; cursor: pointer; transition: opacity 0.2s; } /* Botão primário para Salvar ou Continuar */
.date-row { display: flex; gap: 16px; width: 100%; } /* Linha flex para agrupar campos de data */
.date-field { flex: 1; min-width: 0; } /* Campo de data ocupando espaço disponível */
.picker-overlay { position: fixed; inset: 0; z-index: 50; background: transparent; } /* Camada invisível para fechar calendário ao clicar fora */
.date-input-wrapper { position: relative; z-index: 51; width: 100%; } /* Container relativo para input de data e ícone SVG */
.date-icon { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); color: #9ca3af; cursor: pointer; } /* Ícone de calendário clicável no input */
.custom-calendar { position: absolute; top: calc(100% + 8px); right: 0; width: 260px; background: white; border: 1px solid #e5e7eb; border-radius: 12px; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1); z-index: 100; padding: 16px; } /* Caixa popup do calendário dinâmico */
.calendar-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; font-weight: 600; color: #1a233a; font-size: 14px; } /* Topo do calendário com Mês, Ano e Setas */
.cal-btn { background: transparent; border: 1px solid transparent; cursor: pointer; color: #6b7280; padding: 4px; border-radius: 6px; } /* Setas de navegação de meses do calendário */
.calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; } /* Matriz 7x7 dos dias do calendário */
.cal-weekday { text-align: center; font-size: 11px; font-weight: 600; color: #9ca3af; margin-bottom: 8px; } /* Rótulos dos dias da semana (Dom, Seg...) */
.cal-day { height: 32px; display: flex; align-items: center; justify-content: center; font-size: 13px; border-radius: 6px; cursor: pointer; color: #374151; } /* Quadrado clicável para cada número de dia */
.cal-day.selected { background-color: #1e1b4b; color: white; font-weight: 600; } /* Destaque azul para a data que está selecionada */
.summary-sidebar { position: sticky; top: 90px; height: fit-content; max-height: calc(100vh - 110px); overflow-y: auto; border-left: 1px solid #e5e7eb; padding-left: 32px; } /* Barra lateral direita com Resumo e Status */
.sidebar-actions-container { display: flex; flex-direction: column; gap: 12px; margin-bottom: 32px; } /* Bloco de ações globais na barra lateral direita */
.sidebar-actions { display: flex; align-items: center; gap: 12px; } /* Container para botões da sidebar direita */
.btn-sidebar-draft { display: flex; align-items: center; justify-content: center; gap: 8px; background-color: #0288d1; border: none; padding: 10px 16px; border-radius: 6px; font-size: 13px; color: white; cursor: pointer; font-weight: 500; width: 100%; transition: opacity 0.2s; } /* Botão azul para Salvar Rascunho lateral */
.btn-sidebar-draft:hover { opacity: 0.9; } /* Efeito hover botão Salvar Rascunho */
.btn-sidebar-restart { display: flex; align-items: center; justify-content: center; gap: 8px; background-color: #ef4444; border: none; padding: 10px 16px; border-radius: 6px; font-size: 13px; color: white; cursor: pointer; font-weight: 500; width: 100%; transition: opacity 0.2s; } /* Botão vermelho para Reiniciar Cadastro lateral */
.btn-sidebar-restart:hover { opacity: 0.9; } /* Efeito hover botão Reiniciar Cadastro */
.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; } /* Pílula de status genérica */
.outline-badge { border: 1px solid #e5e7eb; background: white; color: #4b5563; } /* Badge vazio/estado inicial */
.badge-saved { background-color: #dcfce7; color: #065f46; border: 1px solid #bbf7d0; } /* Badge indicando sucesso/salvo (Verde) */
.badge-unsaved { background-color: #f3f4f6; color: #4b5563; border: 1px solid #e5e7eb; } /* Badge indicando não salvo (Cinza) */
.summary-sidebar h3 { font-size: 16px; margin: 0 0 4px 0; color: #1a233a; } /* Título principal da barra de resumo */
.summary-desc { font-size: 13px; color: #9ca3af; margin: 0 0 24px 0; font-style: italic; } /* Subtítulo da barra de resumo */
.summary-block { margin-bottom: 24px; } /* Bloco de divisão dentro da barra de resumo */
.summary-label { display: block; font-size: 13px; color: #6b7280; margin-bottom: 8px; } /* Label das categorias de resumo */
.timeline { display: flex; flex-direction: column; } /* Contêiner da linha do tempo na lateral */
.timeline-item { display: flex; gap: 12px; } /* Item individual da linha do tempo */
.timeline-number { width: 20px; height: 20px; border-radius: 50%; background-color: #0288d1; color: white; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; flex-shrink: 0; margin-top: 2px; } /* Bolinha numerada azul da timeline */
.timeline-content h4 { margin: 0 0 2px 0; font-size: 14px; color: #1a233a; } /* Título da etapa na timeline */
.timeline-content p { margin: 0 0 4px 0; font-size: 13px; color: #6b7280; } /* Tipo da etapa na timeline */
.timeline-tag { font-size: 12px; color: #01579b; } /* Etiqueta com vagas ou detalhe extra na timeline */
.timeline-line { width: 1.5px; height: 24px; background-color: #e5e7eb; margin-left: 9px; margin-top: 4px; margin-bottom: 4px; } /* Fio vertical que une os pontos da timeline */
.summary-footer { margin-top: 40px; font-size: 12px; color: #9ca3af; } /* Texto de rodapé com a hora da última atualização */
.toggle-switch { position: relative; display: inline-block; width: 36px; height: 20px; } /* Box principal do botão switch (Toggle) */
.toggle-input { opacity: 0; width: 0; height: 0; } /* Esconde o input checkbox nativo do toggle */
.toggle-slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #d1d5db; transition: .3s; border-radius: 20px; } /* Corpo deslizante do switch com animação */
.toggle-slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 2px; bottom: 2px; background-color: white; transition: .3s; border-radius: 50%; } /* Bolinha branca de dentro do switch */
.toggle-input:checked + .toggle-slider { background-color: #1e1b4b; } /* Cor do switch ativado (ligado) */
.toggle-input:checked + .toggle-slider:before { transform: translateX(16px); } /* Animação arrastando a bolinha branca para direita */
.list-item-row { display: flex; justify-content: space-between; align-items: center; padding: 16px; border: 1px solid #e5e7eb; border-radius: 8px; margin-bottom: 8px; background-color: white; } /* Linha visual de tabela usada em requisitos ou opções de toggle */
.toggle-row-simple { border: none; padding: 0 0 16px 0; border-radius: 0; border-bottom: 1px solid #f3f4f6; margin-bottom: 16px; } /* Variação de linha sem bordas fechadas, apenas risco em baixo */
.list-item-info { display: flex; align-items: center; } /* Container para infos em lista */
.list-item-info-stacked { display: flex; flex-direction: column; } /* Container para infos de lista em coluna */
.list-item-title { font-size: 14px; color: #1a233a; font-weight: 500; } /* Título base de iten de lista (toggles, cursos, cotas) */
.list-item-sub { font-size: 12px; color: #6b7280; margin-top: 4px; } /* Subtexto descritivo para itens de lista */
.badge-required { background-color: #fef2f2; color: #ef4444; font-size: 10px; padding: 2px 6px; border-radius: 4px; margin-left: 8px; font-weight: 500; } /* Etiqueta vermelha indicando item obrigatório */
.btn-dashed-add { width: 100%; padding: 12px; background: white; border: 1px dashed #d1d5db; color: #1a233a; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; margin-top: 8px; transition: all 0.2s; } /* Botão transparente tracejado para adicionar algo na lista */
.btn-dashed-add:hover { background-color: #f9fafb; border-color: #9ca3af; } /* Hover do botão tracejado de adicionar */
.quota-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #f3f4f6; } /* Linha de formulário exclusiva para cotas */
.quota-input-wrapper { display: flex; align-items: center; gap: 8px; } /* Container juntando input de cota + símbolo % */
.quota-input { width: 60px; padding: 6px 10px; text-align: right; border: 1px solid transparent; background-color: #f3f4f6; border-radius: 6px; font-size: 14px; color: #1a233a; transition: all 0.2s; } /* Caixa numérica pequena para cotas e pesos */
.quota-input:focus { outline: none; background-color: white; border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1); } /* Focus para a caixa pequena de cotas */
.quota-symbol { font-size: 14px; color: #6b7280; font-weight: 500; } /* Símbolo % ao lado dos inputs menores */
.green-toggle-box { display: flex; justify-content: space-between; align-items: center; border: 1px solid #a7f3d0; background-color: #f0fdf4; border-radius: 8px; padding: 16px; } /* Caixa com fundo verde de sucesso/ativo envolvendo um toggle */
.doc-types-container { display: flex; flex-wrap: wrap; gap: 8px; padding: 8px 0; transition: all 0.2s; } /* Flex para englobar os botões de tipo de arquivos (Pills PDF/DOCX) */
.doc-types-container.error-border { padding: 8px; border: 1px dashed #ef4444; border-radius: 6px; background-color: #fef2f2; } /* Borda de erro para quando nenhum tipo for selecionado */
.doc-type-pill { padding: 6px 12px; border: 1px solid #d1d5db; background-color: white; color: #4b5563; border-radius: 16px; font-size: 12px; font-weight: 500; cursor: pointer; transition: all 0.2s ease; user-select: none; } /* Botão pílula (Pill) selecionável para tipo de documento */
.doc-type-pill:hover { border-color: #9ca3af; background-color: #f9fafb; } /* Hover do botão Pill documento */
.doc-type-pill.active { background-color: #e1f5fe; border-color: #0288d1; color: #01579b; } /* Estado ativo do botão Pill (Fica azul claro com borda azul) */
.course-table { display: flex; flex-direction: column; } /* Tabela vertical construída em flex-direction (Aba 4) */
.course-header { display: grid; grid-template-columns: 2.5fr 1fr 1fr 1fr 1fr; padding: 8px 16px; font-size: 13px; color: #6b7280; border-bottom: 1px solid #e5e7eb; } /* Cabeçalho CSS Grid para a tabela de cursos do Nivelamento */
.course-row { display: grid; grid-template-columns: 2.5fr 1fr 1fr 1fr 1fr; align-items: center; padding: 16px; border-bottom: 1px solid #f3f4f6; font-size: 14px; } /* Corpo das linhas de CSS Grid da tabela de cursos */
.course-row:last-child { border-bottom: none; } /* Remove borda da última linha da tabela */
.course-name { color: #1a233a; font-weight: 500; } /* css do nome do curso */
.course-hours { color: #6b7280; } /* css das horas do curso */
.badge-active { background-color: #dcfce7; color: #065f46; padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; display: inline-block; } /* Pílula verdinha clara de Status Ativo no meio das linhas de dados */
.form-range { -webkit-appearance: none; width: 100%; height: 6px; background: #e5e7eb; border-radius: 4px; outline: none; transition: opacity .2s; } /* Barra horizontal dos Slider de pesos matemáticos */
.form-range::-webkit-slider-thumb { -webkit-appearance: none; appearance: none; width: 16px; height: 16px; border-radius: 50%; background: white; border: 3px solid #1e1b4b; cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,0.1); } /* Bolinha drag & drop (thumb) no slider (Webkit) */
.form-range::-moz-range-thumb { width: 16px; height: 16px; border-radius: 50%; background: white; border: 3px solid #1e1b4b; cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,0.1); } /* Bolinha drag & drop (thumb) no slider (Firefox) */
.formula-box { background-color: #0f172a; border-radius: 8px; padding: 16px; color: white; margin-bottom: 24px; } /* Caixa escura (Night Mode) apresentando equações / regras de aprovação */
.formula-label { font-size: 12px; color: #94a3b8; margin-bottom: 8px; display: block; } /* Título cinza claro acima da fórmula dentro da caixa escura */
.formula-text { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size: 14px; line-height: 1.5; } /* Fonte mono-espaçada tipo console para leitura da fórmula de nota */
</style>