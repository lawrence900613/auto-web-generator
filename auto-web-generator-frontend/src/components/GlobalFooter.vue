<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        Original project by Programmer Lawrence Hung
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// No reactive data needed
</script>

<style scoped>
.footer {
  background: transparent;
  text-align: center;
  padding: 20px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
