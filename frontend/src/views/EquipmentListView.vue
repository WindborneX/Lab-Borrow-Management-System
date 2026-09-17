<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  createEquipment,
  listEquipment,
  type Equipment,
} from '../api/equipment'

type TagType = 'success' | 'warning' | 'info' | 'danger'

interface StatusOption {
  label: string
  type: TagType
}

const statusOptions = [
  { label: '在库', value: 0 },
  { label: '借出', value: 1 },
  { label: '维修', value: 2 },
  { label: '停用', value: 3 },
]

const statusConfig: Record<number, StatusOption> = {
  0: { label: '在库', type: 'success' },
  1: { label: '借出', type: 'warning' },
  2: { label: '维修', type: 'info' },
  3: { label: '停用', type: 'danger' },
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const equipmentList = ref<Equipment[]>([])
const formRef = ref<FormInstance>()

const filters = reactive({
  name: '',
  category: '',
  status: undefined as number | undefined,
})

const form = reactive({
  name: '',
  category: '',
  description: '',
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { max: 100, message: '设备名称不能超过100个字符', trigger: 'blur' },
  ],
  category: [
    { max: 50, message: '设备分类不能超过50个字符', trigger: 'blur' },
  ],
}

const loadEquipment = async () => {
  loading.value = true

  try {
    equipmentList.value = await listEquipment({
      name: filters.name.trim() || undefined,
      category: filters.category.trim() || undefined,
      status: filters.status,
    })
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}

const resetFilters = async () => {
  filters.name = ''
  filters.category = ''
  filters.status = undefined
  await loadEquipment()
}

const openCreateDialog = () => {
  Object.assign(form, {
    name: '',
    category: '',
    description: '',
  })
  dialogVisible.value = true

  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const submitCreate = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true

  try {
    await createEquipment({
      name: form.name.trim(),
      category: form.category.trim() || undefined,
      description: form.description.trim() || undefined,
    })
    dialogVisible.value = false
    ElMessage.success('设备已新增')
    await loadEquipment()
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    submitting.value = false
  }
}

const getStatus = (status: number) =>
  statusConfig[status] ?? { label: '未知', type: 'info' as const }

const getErrorMessage = (error: unknown) =>
  error instanceof Error ? error.message : '请求失败，请稍后重试'

onMounted(loadEquipment)
</script>

<template>
  <section class="page-band">
    <header class="section-header">
      <h2>设备列表</h2>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">
        新增设备
      </el-button>
    </header>

    <div class="toolbar">
      <el-input
        v-model="filters.name"
        class="filter-field"
        placeholder="设备名称"
        clearable
        @keyup.enter="loadEquipment"
      />
      <el-input
        v-model="filters.category"
        class="filter-field"
        placeholder="设备分类"
        clearable
        @keyup.enter="loadEquipment"
      />
      <el-select
        v-model="filters.status"
        class="status-field"
        placeholder="全部状态"
        clearable
      >
        <el-option
          v-for="option in statusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>

      <el-button type="primary" :icon="Search" @click="loadEquipment">
        查询
      </el-button>
      <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
    </div>

    <div class="table-wrap">
      <el-table
        v-loading="loading"
        :data="equipmentList"
        empty-text="暂无设备数据"
        stripe
      >
        <el-table-column prop="name" label="设备名称" min-width="180" />
        <el-table-column label="分类" min-width="120">
          <template #default="{ row }">
            {{ row.category || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatus(row.status).type" effect="light">
              {{ getStatus(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="220">
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="录入时间" width="180" />
      </el-table>
    </div>
  </section>

  <el-dialog
    v-model="dialogVisible"
    title="新增设备"
    width="min(520px, calc(100vw - 32px))"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
    >
      <el-form-item label="设备名称" prop="name">
        <el-input
          v-model="form.name"
          maxlength="100"
          placeholder="请输入设备名称"
        />
      </el-form-item>
      <el-form-item label="设备分类" prop="category">
        <el-input
          v-model="form.category"
          maxlength="50"
          placeholder="例如：开发板、传感器"
        />
      </el-form-item>
      <el-form-item label="备注" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入设备备注"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitCreate">
        确认新增
      </el-button>
    </template>
  </el-dialog>
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

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.filter-field {
  width: 190px;
}

.status-field {
  width: 140px;
}

.table-wrap {
  width: 100%;
  overflow-x: auto;
}

@media (max-width: 767px) {
  .page-band {
    min-height: calc(100vh - 108px);
    padding: 16px;
  }

  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-field,
  .status-field {
    width: 100%;
  }
}
</style>
