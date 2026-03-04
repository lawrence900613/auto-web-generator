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
  background: linear-gradient(to top, #fbc2eb 0%, #a6c1ee 100%);
  background-attachment: fixed;
  min-height: 100vh;
}

.main-content {
  width: 100%;
  min-height: calc(100vh - 72px - 80px);
  background: transparent;
}
</style>
