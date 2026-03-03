<template>
  <a-result
    status="403"
    title="403"
    sub-title="You do not have permission to access this page."
  >
    <template #extra>
      <a-button type="primary" @click="router.push('/')">Back Home</a-button>
    </template>
  </a-result>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>
