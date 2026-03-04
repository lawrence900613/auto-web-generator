<template>
  <a-modal v-model:open="visible" title="App Details" :footer="null">
    <div class="detail-creator">
      <a-avatar :src="app?.user?.userAvatar" :size="36">
        {{ (app?.user?.userName ?? 'U')[0].toUpperCase() }}
      </a-avatar>
      <div class="detail-creator-info">
        <div class="detail-label">Creator</div>
        <div class="detail-value">{{ app?.user?.userName ?? 'Unknown' }}</div>
      </div>
    </div>
    <div class="detail-row">
      <span class="detail-label">Create Time</span>
      <span class="detail-value">{{ app?.createTime }}</span>
    </div>
    <template v-if="isOwner || isAdmin">
      <a-divider style="margin: 16px 0" />
      <div class="detail-actions">
        <a-button type="primary" @click="doEdit">
          <template #icon><EditOutlined /></template>
          Edit
        </a-button>
        <a-popconfirm
          title="Are you sure you want to delete this app?"
          ok-text="Yes"
          cancel-text="No"
          @confirm="doDelete"
        >
          <a-button danger>
            <template #icon><DeleteOutlined /></template>
            Delete
          </a-button>
        </a-popconfirm>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { deleteApp, deleteAppByAdmin } from '@/api/appController'

const props = defineProps<{
  open: boolean
  app?: API.AppVO
  isOwner: boolean
  isAdmin: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'deleted'): void
}>()

const router = useRouter()

const visible = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v),
})

const doEdit = () => {
  emit('update:open', false)
  router.push(`/app/edit/${props.app?.id}`)
}

const doDelete = async () => {
  const id = String(props.app?.id)
  const res = props.isAdmin
    ? await deleteAppByAdmin({ id })
    : await deleteApp({ id })
  if (res.data.code === 0) {
    message.success('Deleted successfully')
    emit('update:open', false)
    emit('deleted')
  } else {
    message.error(res.data.message ?? 'Delete failed')
  }
}
</script>

<style scoped>
.detail-creator {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-creator-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-row {
  display: flex;
  gap: 12px;
  align-items: baseline;
}

.detail-label {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.detail-value {
  font-size: 14px;
  color: #333;
}

.detail-actions {
  display: flex;
  gap: 8px;
}
</style>
