<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Box, Tickets } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isCollapsed = ref(false)

const pageTitle = computed(() => String(route.meta.title ?? '设备管理'))

const updateSidebar = () => {
  isCollapsed.value = window.innerWidth < 768
}

onMounted(() => {
  updateSidebar()
  window.addEventListener('resize', updateSidebar)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateSidebar)
})
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" :width="isCollapsed ? '72px' : '224px'">
      <div class="brand">
        <div class="brand-mark">
          <el-icon :size="22">
            <Box />
          </el-icon>
        </div>
        <div v-if="!isCollapsed" class="brand-copy">
          <strong>Lab Borrow</strong>
          <span>设备借用管理</span>
        </div>
      </div>

      <el-menu
        class="nav-menu"
        :default-active="route.path"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
      >
        <el-menu-item index="/equipment">
          <el-icon>
            <Box />
          </el-icon>
          <template #title>设备管理</template>
        </el-menu-item>
        <el-menu-item index="/borrow-records">
          <el-icon>
            <Tickets />
          </el-icon>
          <template #title>借用记录</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-shell">
      <el-header class="app-header">
        <div>
          <span class="header-kicker">实验室设备借用管理系统</span>
          <h1>{{ pageTitle }}</h1>
        </div>
      </el-header>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--app-bg);
}

.app-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden;
  background: var(--app-surface);
  border-right: 1px solid var(--app-border);
  transition: width 0.2s ease;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 72px;
  padding: 0 16px;
  border-bottom: 1px solid #edf3ff;
}

.brand-mark {
  display: grid;
  flex: 0 0 40px;
  width: 40px;
  height: 40px;
  color: #ffffff;
  background: var(--app-primary);
  border-radius: 8px;
  place-items: center;
}

.brand-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  line-height: 1.25;
}

.brand-copy strong {
  color: var(--app-text);
  font-size: 16px;
}

.brand-copy span {
  margin-top: 3px;
  color: var(--app-muted);
  font-size: 12px;
}

.nav-menu {
  border-right: none;
  padding: 12px 8px;
}

.nav-menu :deep(.el-menu-item) {
  height: 44px;
  margin-bottom: 6px;
  border-radius: 7px;
  color: #475569;
}

.nav-menu :deep(.el-menu-item:hover) {
  background: #f2f6ff;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: var(--app-primary);
  background: #eaf1ff;
  font-weight: 600;
}

.main-shell {
  min-width: 0;
}

.app-header {
  display: flex;
  align-items: center;
  height: 72px;
  padding: 0 28px;
  background: var(--app-surface);
  border-bottom: 1px solid var(--app-border);
}

.header-kicker {
  display: block;
  margin-bottom: 3px;
  color: var(--app-muted);
  font-size: 12px;
}

.app-header h1 {
  margin: 0;
  color: var(--app-text);
  font-size: 20px;
  font-weight: 650;
  line-height: 1.3;
}

.app-main {
  padding: 24px 28px 32px;
}

@media (max-width: 767px) {
  .app-header {
    padding: 0 18px;
  }

  .app-main {
    padding: 18px;
  }
}
</style>
