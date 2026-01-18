<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { bindMySkill, listMySkills, unbindMySkill } from "@/api/skills"

const skills = ref<Awaited<ReturnType<typeof listMySkills>>>([])
const loadingList = ref(false)
const actionLoading = ref(false)
const skillIdInput = ref("")

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message ?? error?.response?.data?.message ?? fallback
  if (status === 400) {
    ElMessage.error("参数错误/skillId 不合法")
    return
  }
  if (status === 403 || status === 404 || status === 409) {
    ElMessage.error(message || fallback)
    return
  }
  ElMessage.error(message || fallback)
}

const reload = async () => {
  loadingList.value = true
  try {
    skills.value = await listMySkills()
  } catch (error: any) {
    showRequestError(error, "Failed to load skills")
  } finally {
    loadingList.value = false
  }
}

const handleBind = async () => {
  const skillId = Number(skillIdInput.value)
  if (!Number.isFinite(skillId) || skillId <= 0) {
    ElMessage.error("参数错误/skillId 不合法")
    return
  }
  actionLoading.value = true
  try {
    await bindMySkill(skillId)
    skillIdInput.value = ""
    await reload()
  } catch (error: any) {
    showRequestError(error, "Failed to bind skill")
  } finally {
    actionLoading.value = false
  }
}

const handleUnbind = async (skillId?: number) => {
  if (!skillId) return
  actionLoading.value = true
  try {
    await unbindMySkill(skillId)
    await reload()
  } catch (error: any) {
    showRequestError(error, "Failed to unbind skill")
  } finally {
    actionLoading.value = false
  }
}

onMounted(reload)
</script>

<template>
  <el-card shadow="never">
    <h2>My Skills</h2>
    <el-table :data="skills" v-loading="loadingList" style="width: 100%">
      <el-table-column prop="skillId" label="Skill ID" width="120" />
      <el-table-column prop="skillName" label="Name" />
      <el-table-column prop="skillCategory" label="Category" />
      <el-table-column prop="proficiency" label="Proficiency" width="140" />
      <el-table-column label="Action" width="140">
        <template #default="scope">
          <el-button size="small" :loading="actionLoading" @click="handleUnbind(scope.row.skillId)">
            Unbind
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="!loadingList && skills.length === 0">Empty</div>
  </el-card>

  <el-card shadow="never" style="margin-top: 16px">
    <h2>Bind Skill</h2>
    <el-form label-position="top">
      <el-form-item label="Skill ID">
        <el-input v-model="skillIdInput" placeholder="Enter skillId" />
      </el-form-item>
      <el-button type="primary" :loading="actionLoading" @click="handleBind">Bind</el-button>
    </el-form>
  </el-card>
</template>
