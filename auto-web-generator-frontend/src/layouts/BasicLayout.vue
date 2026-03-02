<template>
  <a-layout class="basic-layout">
    <!-- Top navigation bar -->
    <GlobalHeader />

    <!-- Main content area -->
    <a-layout-content class="main-content">
      <router-view />
    </a-layout-content>

    <!-- Bottom copyright -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: none;
}

.main-content {
  max-width: 1200px;
  padding: 24px;
  background: white;
  margin: 16px auto 80px;
}
</style>
