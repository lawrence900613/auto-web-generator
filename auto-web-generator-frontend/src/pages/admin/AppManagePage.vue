<template>
  <div id="appManagePage">
    <!-- Search -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="App Name">
        <a-input v-model:value="searchParams.appName" placeholder="App name" />
      </a-form-item>
      <a-form-item label="Code Gen Type">
        <a-select v-model:value="searchParams.codeGenType" style="width: 140px" allow-clear>
          <a-select-option value="html">HTML</a-select-option>
          <a-select-option value="multi_file">Multi File</a-select-option>
        </a-select>
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
        <template v-if="column.dataIndex === 'cover'">
          <img v-if="record.cover" :src="record.cover" style="height: 48px; object-fit: cover; border-radius: 4px" />
          <span v-else>—</span>
        </template>
        <template v-else-if="column.dataIndex === 'initPrompt'">
          <a-typography-paragraph :ellipsis="{ rows: 2 }" :content="record.initPrompt" />
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag v-if="record.priority === 99" color="gold">Featured</a-tag>
          <span v-else>{{ record.priority }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'deployedTime'">
          {{ record.deployedTime ? new Date(record.deployedTime).toLocaleString() : '—' }}
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ record.createTime ? new Date(record.createTime).toLocaleString() : '—' }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="router.push(`/app/edit/${record.id}`)">Edit</a-button>
            <a-button
              size="small"
              :type="record.priority === 99 ? 'default' : 'primary'"
              @click="toggleFeatured(record)"
            >
              {{ record.priority === 99 ? 'Unfeature' : 'Feature' }}
            </a-button>
            <a-popconfirm
              title="Delete this app?"
              ok-text="Yes"
              cancel-text="No"
              @confirm="doDelete(record.id)"
            >
              <a-button danger size="small">Delete</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { deleteAppByAdmin, listAppVoByPageByAdmin, updateAppByAdmin } from '@/api/appController'

const router = useRouter()

const columns = [
  { title: 'ID', dataIndex: 'id' },
  { title: 'Name', dataIndex: 'appName' },
  { title: 'Cover', dataIndex: 'cover' },
  { title: 'Prompt', dataIndex: 'initPrompt' },
  { title: 'Type', dataIndex: 'codeGenType' },
  { title: 'Priority', dataIndex: 'priority' },
  { title: 'Deployed', dataIndex: 'deployedTime' },
  { title: 'Created', dataIndex: 'createTime' },
  { title: 'Action', key: 'action' },
]

const data = ref<API.AppVO[]>([])
const total = ref(0)

const searchParams = reactive<API.AppQueryRequest>({
  appName: '',
  codeGenType: '',
  page: 1,
  size: 10,
})

const pagination = computed(() => ({
  current: searchParams.page ?? 1,
  pageSize: searchParams.size ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `Total ${t} apps`,
}))

const fetchData = async () => {
  const res = await listAppVoByPageByAdmin({ ...searchParams })
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

const toggleFeatured = async (record: API.AppVO) => {
  const newPriority = record.priority === 99 ? 0 : 99
  const res = await updateAppByAdmin({ id: record.id, priority: newPriority })
  if (res.data.code === 0) {
    message.success(newPriority === 99 ? 'Marked as featured' : 'Removed from featured')
    fetchData()
  } else {
    message.error(res.data.message)
  }
}

const doDelete = async (id: string) => {
  const res = await deleteAppByAdmin({ id })
  if (res.data.code === 0) {
    message.success('Deleted')
    fetchData()
  } else {
    message.error(res.data.message)
  }
}

onMounted(fetchData)
</script>

<style scoped>
#appManagePage {
  padding: 24px;
}
</style>
