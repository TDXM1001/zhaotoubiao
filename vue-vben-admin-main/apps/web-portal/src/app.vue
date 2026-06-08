<script lang="ts" setup>
import { RouterLink, RouterView } from 'vue-router';
</script>

<template>
  <div class="portal-shell">
    <header class="portal-shell__header">
      <RouterLink class="portal-shell__brand" to="/bid-portal/project/list">
        <span class="portal-shell__mark">招</span>
        <span>供应商门户</span>
      </RouterLink>
      <nav class="portal-shell__nav" aria-label="供应商门户导航">
        <RouterLink to="/bid-portal/project/list">项目大厅</RouterLink>
        <RouterLink to="/bid-portal/lots/result">标段结果</RouterLink>
      </nav>
    </header>
    <main class="portal-shell__main">
      <RouterView />
    </main>
  </div>
</template>
