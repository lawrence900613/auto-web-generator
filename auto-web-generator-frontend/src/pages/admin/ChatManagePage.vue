<template>
  <div id="chatManagePage">
    <!-- Search -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="App ID">
        <a-input v-model:value="searchParams.appId" placeholder="App ID" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">Search</a-button>
      </a-form-item>
    </a-form>

    <a-divider />

    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      row-key="id"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'messageType'">
          <a-tag :color="record.messageType === 'user' ? 'blue' : 'purple'">
            {{ record.messageType === 'user' ? 'User' : 'AI' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'message'">
          <a-typography-paragraph :ellipsis="{ rows: 2 }" :content="record.message" />
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ record.createTime ? new Date(record.createTime).toLocaleString() : '—' }}
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { listAllChatHistoryForAdmin } from '@/api/chatHistoryController'

const columns = [
  { title: 'ID', dataIndex: 'id' },
  { title: 'App ID', dataIndex: 'appId' },
  { title: 'User ID', dataIndex: 'userId' },
  { title: 'Type', dataIndex: 'messageType' },
  { title: 'Message', dataIndex: 'message' },
  { title: 'Created', dataIndex: 'createTime' },
]

const data = ref<API.ChatHistory[]>([])
const total = ref(0)

const searchParams = reactive<{ appId: string; page: number; size: number }>({
  appId: '',
  page: 1,
  size: 20,
})

const pagination = computed(() => ({
  current: searchParams.page,
  pageSize: searchParams.size,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `Total ${t} records`,
}))

const fetchData = async () => {
  const res = await listAllChatHistoryForAdmin({
    page: searchParams.page,
    pageSize: searchParams.size,
    appId: searchParams.appId || undefined,
  })
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('Failed to load: ' + res.data.message)
  }
}

const doSearch = () => {
  searchParams.page = 1
  fetchData()
}

const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.page = page.current
  searchParams.size = page.pageSize
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
#chatManagePage {
  padding: 24px;
}
</style>
