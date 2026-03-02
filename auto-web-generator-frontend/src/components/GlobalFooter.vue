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
  background: #efefef;
  text-align: center;
  padding: 20px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
