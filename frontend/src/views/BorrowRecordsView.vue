<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listBorrowRecords,
  returnEquipment,
  type BorrowRecord,
} from '../api/borrow'
import { getEquipment } from '../api/equipment'
import { getUser } from '../api/users'

type TagType = 'success' | 'warning' | 'info' | 'danger'

interface BorrowRecordView extends BorrowRecord {
  equipmentName: string
  username: string
  studentId: string
}

const statusOptions = [
  { label: '借用中', value: 0 },
  { label: '已归还', value: 1 },
  { label: '超期未还', value: 2 },
]

const statusConfig: Record<number, { label: string; type: TagType }> = {
  0: { label: '借用中', type: 'warning' },
  1: { label: '已归还', type: 'success' },
  2: { label: '超期未还', type: 'danger' },
}

const loading = ref(false)
const returningId = ref<number>()
const records = ref<BorrowRecordView[]>([])

const filters = reactive({
  userId: '',
  status: undefined as number | undefined,
})

const loadRecords = async () => {
  loading.value = true

  try {
    const userId = filters.userId.trim()
      ? Number(filters.userId.trim())
      : undefined

    if (userId !== undefined && (!Number.isInteger(userId) || userId <= 0)) {
      ElMessage.warning('用户 ID 必须为正整数')
      return
    }

    const rawRecords = await listBorrowRecords({
      userId,
      status: filters.status,
    })
    const equipmentIds = [...new Set(rawRecords.map((record) => record.equipmentId))]
    const userIds = [...new Set(rawRecords.map((record) => record.userId))]

    const [equipmentResults, userResults] = await Promise.all([
      Promise.all(equipmentIds.map((id) => getEquipment(id))),
      Promise.all(userIds.map((id) => getUser(id))),
    ])

    const equipmentMap = new Map(
      equipmentResults.map((equipment) => [equipment.id, equipment]),
    )
    const userMap = new Map(userResults.map((user) => [user.id, user]))

    records.value = rawRecords.map((record) => {
      const equipment = equipmentMap.get(record.equipmentId)
      const user = userMap.get(record.userId)

      return {
        ...record,
        equipmentName: equipment?.name ?? `设备 #${record.equipmentId}`,
        username: user?.username ?? `用户 #${record.userId}`,
        studentId: user?.studentId ?? '-',
      }
    })
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}

const resetFilters = async () => {
  filters.userId = ''
  filters.status = undefined
  await loadRecords()
}

const confirmReturn = async (record: BorrowRecordView) => {
  try {
    await ElMessageBox.confirm(
      `确认归还“${record.equipmentName}”吗？`,
      '归还设备',
      {
        confirmButtonText: '确认归还',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  returningId.value = record.id

  try {
    await returnEquipment(record.id, record.userId)
    ElMessage.success('设备已归还')
    await loadRecords()
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    returningId.value = undefined
  }
}

const getStatus = (status: number) =>
  statusConfig[status] ?? { label: '未知', type: 'info' as const }

const getErrorMessage = (error: unknown) =>
  error instanceof Error ? error.message : '请求失败，请稍后重试'

onMounted(loadRecords)
</script>

<template>
  <section class="page-band">
    <header class="section-header">
      <h2>借用记录</h2>
      <el-button :icon="Refresh" :loading="loading" @click="loadRecords">
        刷新
      </el-button>
    </header>

    <div class="toolbar">
      <el-input
        v-model="filters.userId"
        class="user-field"
        placeholder="用户 ID"
        clearable
        @keyup.enter="loadRecords"
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

      <el-button type="primary" :icon="Search" @click="loadRecords">
        查询
      </el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="table-wrap">
      <el-table
        v-loading="loading"
        :data="records"
        empty-text="暂无借用记录"
        stripe
      >
        <el-table-column prop="id" label="记录 ID" width="100" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="180" />
        <el-table-column label="借用人" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <strong>{{ row.username }}</strong>
              <span>{{ row.studentId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="borrowTime" label="借用时间" width="180" />
        <el-table-column label="预计归还" width="180">
          <template #default="{ row }">
            {{ row.expectReturnTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="实际归还" width="180">
          <template #default="{ row }">
            {{ row.actualReturnTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatus(row.status).type" effect="light">
              {{ getStatus(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              link
              :loading="returningId === row.id"
              @click="confirmReturn(row)"
            >
              归还
            </el-button>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
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

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.user-field {
  width: 180px;
}

.status-field {
  width: 150px;
}

.table-wrap {
  width: 100%;
  overflow-x: auto;
}

.user-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.35;
}

.user-cell strong {
  color: var(--app-text);
  font-weight: 600;
}

.user-cell span,
.muted {
  color: #94a3b8;
  font-size: 12px;
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

  .user-field,
  .status-field {
    width: 100%;
  }
}
</style>
