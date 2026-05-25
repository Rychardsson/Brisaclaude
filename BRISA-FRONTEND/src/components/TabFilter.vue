<template>
  <div class="tab-filter-container">
    <div class="tab-filter">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        :class="['tab-item', { active: activeTabId === tab.id }]"
        @click="$emit('tab-change', tab.id)"
      >
        <span class="tab-label">{{ tab.label }}</span>
        <span class="tab-count">{{ tab.count }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

defineProps({
  tabs: {
    type: Array,
    required: true,
    validator: (tabs) => tabs.every((tab) => tab.id && tab.label && typeof tab.count === 'number'),
  },
  activeTabId: {
    type: String,
    required: true,
  },
});

defineEmits(['tab-change']);
</script>

<style scoped>
.tab-filter-container {
  background: #ffffff;
  border: 1px solid #D1D6E8;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 20px;
}

.tab-filter {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #E1E6F0;
  overflow-x: auto;
}

.tab-item {
   display: flex;
   align-items: center;
   gap: 8px;
   padding: 12px 16px;
   background: transparent;
   border: none;
   border-bottom: 2px solid transparent;
   cursor: pointer;
   font-size: 14px;
   font-weight: 600;
   color: var(--slate-600);
   transition: all 0.2s ease;
   white-space: nowrap;
}

.tab-item:hover {
   color: var(--color-text);
}

.tab-item.active {
   color: var(--teal-600);
   border-bottom-color: var(--teal-600);
}

.tab-label {
  font-weight: 500;
}

.tab-count {
   background: #eef2f7;
   color: #8a98ab;
   border-radius: 999px;
   padding: 2px 7px;
   font-size: 11px;
   font-weight: 600;
}

.tab-item.active .tab-count {
   color: var(--teal-600);
}

@media (max-width: 860px) {
  .tab-item {
    padding: 12px 16px;
    font-size: 13px;
  }

  .tab-count {
    font-size: 11px;
  }
}
</style>
