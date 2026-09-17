<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
</script>

<template>
  <section class="page-band">
    <header class="section-header">
      <h2>设备列表</h2>
      <el-button type="primary" :icon="Plus" disabled>新增设备</el-button>
    </header>

    <el-table :data="[]" empty-text="暂无设备数据" stripe>
      <el-table-column prop="name" label="设备名称" min-width="180" />
      <el-table-column prop="category" label="分类" min-width="120" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column prop="description" label="备注" min-width="220" />
      <el-table-column prop="createdAt" label="录入时间" width="180" />
    </el-table>
  </section>
</template>

<style scoped>
.page-band {
  min-height: calc(100vh - 128px);
  padding: 20px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 8px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-header h2 {
  margin: 0;
  color: var(--app-text);
  font-size: 17px;
  font-weight: 650;
}

@media (max-width: 767px) {
  .page-band {
    min-height: calc(100vh - 108px);
    padding: 16px;
  }
}
</style>
