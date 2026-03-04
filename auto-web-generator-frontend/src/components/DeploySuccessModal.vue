<template>
  <a-modal v-model:open="visible" :footer="null" :closable="true" centered width="420px">
    <div class="deploy-success-body">
      <CheckCircleFilled class="deploy-success-icon" />
      <h3 class="deploy-success-title">Website Deployed!</h3>
      <p class="deploy-success-sub">Your website is live and accessible at:</p>
      <div class="deploy-url-row">
        <a-input :value="deployedUrl" readonly class="deploy-url-input" />
        <a-button type="text" @click="doCopy">
          <template #icon><CopyOutlined /></template>
        </a-button>
      </div>
      <div class="deploy-success-actions">
        <a-button type="primary" @click="doOpen">Visit Website</a-button>
        <a-button @click="visible = false">Close</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { message } from 'ant-design-vue'
import { CheckCircleFilled, CopyOutlined } from '@ant-design/icons-vue'

const props = defineProps<{
  open: boolean
  deployedUrl: string
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const visible = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v),
})

const doCopy = () => {
  navigator.clipboard.writeText(props.deployedUrl)
  message.success('Copied!')
}

const doOpen = () => window.open(props.deployedUrl, '_blank')
</script>

<style scoped>
.deploy-success-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0 4px;
  text-align: center;
}

.deploy-success-icon {
  font-size: 56px;
  color: #52c41a;
  margin-bottom: 16px;
}

.deploy-success-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 6px;
  color: #1a1a2e;
}

.deploy-success-sub {
  font-size: 13px;
  color: #888;
  margin: 0 0 16px;
}

.deploy-url-row {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
  margin-bottom: 20px;
  background: #f7f8fc;
  border: 1px solid #e4e6ee;
  border-radius: 8px;
  padding: 4px 4px 4px 12px;
}

.deploy-url-input :deep(.ant-input) {
  border: none !important;
  box-shadow: none !important;
  background: transparent;
  font-size: 13px;
  padding: 0;
  color: #333;
}

.deploy-success-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>
