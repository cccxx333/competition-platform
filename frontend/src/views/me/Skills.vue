<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { bindMySkill, listMySkills, listSkills, unbindMySkill } from "@/api/skills"

const skills = ref<Awaited<ReturnType<typeof listMySkills>>>([])
const availableSkills = ref<Awaited<ReturnType<typeof listSkills>>>([])
const loadingList = ref(false)
const loadingOptions = ref(false)
const actionLoading = ref(false)
const skillIdInput = ref("")
const selectedSkillId = ref<number | null>(null)

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  if (message && message !== fallback) {
    ElMessage.error(message)
    return
  }
  if (status === 400) {
    ElMessage.error("参数错误/skillId 不合法")
    return
  }
  if (status === 403) {
    ElMessage.error("无权限")
    return
  }
  if (status === 404) {
    ElMessage.error("资源不存在/skillId 不存在")
    return
  }
  if (status === 409) {
    ElMessage.error("重复绑定/业务冲突")
    return
  }
  ElMessage.error(fallback)
}

const boundSkillIds = computed(() => new Set(skills.value.map((skill) => skill.skillId).filter(Boolean)))

const selectableSkills = computed(() => {
  return availableSkills.value
    .filter((skill) => Boolean(skill.id))
    .map((skill) => ({
      id: skill.id as number,
      label: [skill.name, skill.category].filter(Boolean).join(" / "),
      disabled: boundSkillIds.value.has(skill.id as number)
    }))
})

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

const loadAllSkills = async () => {
  loadingOptions.value = true
  try {
    availableSkills.value = await listSkills()
  } catch (error: any) {
    showRequestError(error, "Failed to load skills")
  } finally {
    loadingOptions.value = false
  }
}

const handleBindSelected = async () => {
  if (selectedSkillId.value == null) {
    ElMessage.error("请选择技能")
    return
  }
  if (boundSkillIds.value.has(selectedSkillId.value)) {
    ElMessage.error("已绑定")
    return
  }
  actionLoading.value = true
  try {
    await bindMySkill(selectedSkillId.value)
    selectedSkillId.value = null
    await reload()
  } catch (error: any) {
    showRequestError(error, "Failed to bind skill")
  } finally {
    actionLoading.value = false
  }
}

const handleBindManual = async () => {
  const skillId = Number(skillIdInput.value)
  if (!Number.isFinite(skillId) || skillId <= 0) {
    ElMessage.error("参数错误/skillId 不合法")
    return
  }
  if (boundSkillIds.value.has(skillId)) {
    ElMessage.error("已绑定")
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

onMounted(() => {
  reload()
  loadAllSkills()
})
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
          <el-button
            size="small"
            :loading="actionLoading"
            :disabled="loadingList"
            @click="handleUnbind(scope.row.skillId)"
          >
            Unbind
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="!loadingList && skills.length === 0">暂无技能，请先绑定</div>
  </el-card>

  <el-card shadow="never" style="margin-top: 16px">
    <h2>Bind Skill</h2>
    <el-form label-position="top">
      <el-form-item label="Select Skill">
        <el-select
          v-model="selectedSkillId"
          placeholder="Select a skill"
          :loading="loadingOptions"
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="item in selectableSkills"
            :key="item.id"
            :label="item.label"
            :value="item.id"
            :disabled="item.disabled"
          />
        </el-select>
      </el-form-item>
      <el-button
        type="primary"
        :loading="actionLoading"
        :disabled="selectedSkillId == null || boundSkillIds.has(selectedSkillId)"
        @click="handleBindSelected"
      >
        Bind
      </el-button>
    </el-form>

    <el-collapse style="margin-top: 16px">
      <el-collapse-item title="Manual skillId" name="manual">
        <el-form label-position="top">
          <el-form-item label="Skill ID">
            <el-input v-model="skillIdInput" placeholder="Enter skillId" />
          </el-form-item>
          <el-button type="primary" :loading="actionLoading" @click="handleBindManual">Bind</el-button>
        </el-form>
      </el-collapse-item>
    </el-collapse>
  </el-card>
</template>
