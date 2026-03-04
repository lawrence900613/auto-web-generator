<template>
  <div id="userManagePage">
    <!-- Search form -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="Account">
        <a-input v-model:value="searchParams.userAccount" placeholder="Enter account" />
      </a-form-item>
      <a-form-item label="Username">
        <a-input v-model:value="searchParams.userName" placeholder="Enter username" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">Search</a-button>
      </a-form-item>
    </a-form>

    <a-divider />

    <!-- User table -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      row-key="id"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <!-- Avatar -->
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-avatar :src="record.userAvatar" :size="48" />
        </template>
        <!-- Role badge -->
        <template v-else-if="column.dataIndex === 'userRole'">
          <a-tag v-if="record.userRole === 'admin'" color="green">Admin</a-tag>
          <a-tag v-else color="blue">User</a-tag>
        </template>
        <!-- Create time -->
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ record.createTime ? new Date(record.createTime).toLocaleString() : '-' }}
        </template>
        <!-- Action -->
        <template v-else-if="column.key === 'action'">
          <a-popconfirm
            title="Are you sure you want to delete this user?"
            ok-text="Yes"
            cancel-text="No"
            @confirm="doDelete(record.id)"
          >
            <a-button danger size="small">Delete</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { deleteUser, listUserVoByPage } from '@/api/userController'

// ---- Table columns ----
const columns = [
  { title: 'ID', dataIndex: 'id' },
  { title: 'Account', dataIndex: 'userAccount' },
  { title: 'Username', dataIndex: 'userName' },
  { title: 'Avatar', dataIndex: 'userAvatar' },
  { title: 'Profile', dataIndex: 'userProfile' },
  { title: 'Role', dataIndex: 'userRole' },
  { title: 'Created', dataIndex: 'createTime' },
  { title: 'Action', key: 'action' },
]

// ---- Data ----
const data = ref<API.UserVO[]>([])
const total = ref(0)

// ---- Search + pagination state ----
const searchParams = reactive<API.UserQueryRequest>({
  userAccount: '',
  userName: '',
  page: 1,
  size: 10,
})

// Ant Design Table pagination config (computed so it reacts to total changes)
const pagination = computed(() => ({
  current: searchParams.page ?? 1,
  pageSize: searchParams.size ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `Total ${t} users`,
}))

// ---- Fetch ----
const fetchData = async () => {
  const res = await listUserVoByPage({ ...searchParams })
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('Failed to fetch users: ' + res.data.message)
  }
}

// ---- Events ----
const doSearch = () => {
  searchParams.page = 1
  fetchData()
}

const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.page = page.current
  searchParams.size = page.pageSize
  fetchData()
}

const doDelete = async (id: string) => {
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('Deleted successfully')
    fetchData()
  } else {
    message.error('Delete failed')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  padding: 24px;
}
</style>
