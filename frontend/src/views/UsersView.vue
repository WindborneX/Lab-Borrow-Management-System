<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  createUser,
  getUserByStudentId,
  type User,
} from '../api/users'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const studentIdQuery = ref('')
const result = ref<User | null>(null)

const form = reactive({
  studentId: '',
  username: '',
  role: 0,
})

const rules: FormRules = {
  studentId: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' },
    { max: 20, message: '学号/工号不能超过20个字符', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '姓名不能超过50个字符', trigger: 'blur' },
  ],
}

const searchUser = async () => {
  const studentId = studentIdQuery.value.trim()
  if (!studentId) {
    ElMessage.warning('请输入学号/工号')
    return
  }

  loading.value = true

  try {
    result.value = await getUserByStudentId(studentId)
  } catch (error) {
    result.value = null
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  Object.assign(form, {
    studentId: '',
    username: '',
    role: 0,
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
    const user = await createUser({
      studentId: form.studentId.trim(),
      username: form.username.trim(),
      role: form.role,
    })
    result.value = user
    studentIdQuery.value = user.studentId
    dialogVisible.value = false
    ElMessage.success('用户已新增')
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    submitting.value = false
  }
}

const getErrorMessage = (error: unknown) =>
  error instanceof Error ? error.message : '请求失败，请稍后重试'
</script>

<template>
  <section class="page-band">
    <header class="section-header">
      <h2>用户查询</h2>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">
        新增用户
      </el-button>
    </header>

    <div class="toolbar">
      <el-input
        v-model="studentIdQuery"
        class="student-field"
        placeholder="学号/工号"
        clearable
        @keyup.enter="searchUser"
      />
      <el-button type="primary" :icon="Search" @click="searchUser">
        查询
      </el-button>
    </div>

    <div class="table-wrap">
      <el-table
        v-loading="loading"
        :data="result ? [result] : []"
        empty-text="暂无用户数据"
        stripe
      >
        <el-table-column prop="id" label="用户 ID" width="110" />
        <el-table-column prop="studentId" label="学号/工号" min-width="160" />
        <el-table-column prop="username" label="姓名" min-width="140" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'primary'" effect="light">
              {{ row.role === 1 ? '管理员' : '学生' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
      </el-table>
    </div>
  </section>

  <el-dialog
    v-model="dialogVisible"
    title="新增用户"
    width="min(520px, calc(100vw - 32px))"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
    >
      <el-form-item label="学号/工号" prop="studentId">
        <el-input
          v-model="form.studentId"
          maxlength="20"
          placeholder="请输入学号/工号"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="username">
        <el-input
          v-model="form.username"
          maxlength="50"
          placeholder="请输入姓名"
        />
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-radio-group v-model="form.role">
          <el-radio-button :value="0">学生</el-radio-button>
          <el-radio-button :value="1">管理员</el-radio-button>
        </el-radio-group>
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

.student-field {
  width: 220px;
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

  .student-field {
    width: 100%;
  }
}
</style>
