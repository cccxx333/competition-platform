<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { createTeacherApplication, type TeacherApplicationCreatePayload } from "@/api/teacherApplications"
import { listSkills, type Skill } from "@/api/skills"

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const errorMessage = ref("")
const skillsLoading = ref(false)
const allSkills = ref<Skill[]>([])
const skillOptions = computed(() => allSkills.value.filter((s): s is Skill & { id: number } => typeof s.id === "number"))
const preferredSkillRows = ref<Array<{ skillId: number | null; weight: number }>>([{ skillId: null, weight: 3 }])
const form = reactive<TeacherApplicationCreatePayload>({
  teamName: "",
  description: ""
})
const selectedSkillIds = computed(
  () => new Set(preferredSkillRows.value.map((row) => row.skillId).filter((id): id is number => typeof id === "number"))
)

const competitionId = computed(() => Number(route.params.competitionId))

const showRequestError = (error: any, fallback: string) => {
  const status = error?.status ?? error?.response?.status
  const message = error?.message
  if (message && message !== fallback) {
    ElMessage.error(message)
    return message
  }
  if (status === 403) {
    ElMessage.error("无权限")
    return "无权限"
  }
  if (status === 404) {
    ElMessage.error("竞赛不存在")
    return "竞赛不存在"
  }
  if (status === 409) {
    ElMessage.error("申请已存在")
    return "申请已存在"
  }
  ElMessage.error("请求失败，请稍后重试")
  return "请求失败，请稍后重试"
}

const handleSubmit = async () => {
  errorMessage.value = ""
  if (!Number.isFinite(competitionId.value) || competitionId.value <= 0) {
    errorMessage.value = "竞赛不存在"
    return
  }
  const skills = preferredSkillRows.value
    .filter((row) => typeof row.skillId === "number")
    .map((row) => ({
      skillId: row.skillId as number,
      weight: Number.isFinite(row.weight) && row.weight > 0 ? row.weight : 1
    }))
  if (skills.length < 1) {
    errorMessage.value = "请至少选择 1 项队伍青睐技能"
    return
  }
  if (skills.length > 8) {
    errorMessage.value = "队伍青睐技能最多 8 项"
    return
  }

  loading.value = true
  try {
    const payload: TeacherApplicationCreatePayload = {}
    if (form.teamName && form.teamName.trim()) {
      payload.teamName = form.teamName.trim()
    }
    if (form.description && form.description.trim()) {
      payload.description = form.description.trim()
    }
    payload.skills = skills
    await createTeacherApplication(competitionId.value, payload)
    ElMessage.success("申请已提交")
    router.push("/teacher/applications")
  } catch (error: any) {
    errorMessage.value = showRequestError(error, "提交建队申请失败")
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  router.back()
}

const loadSkills = async () => {
  if (skillsLoading.value || allSkills.value.length > 0) return
  skillsLoading.value = true
  try {
    allSkills.value = await listSkills()
  } catch (error: any) {
    ElMessage.error(error?.message || "加载技能列表失败")
  } finally {
    skillsLoading.value = false
  }
}

const addPreferredSkillRow = () => {
  if (preferredSkillRows.value.length >= 8) return
  preferredSkillRows.value.push({ skillId: null, weight: 3 })
}

const removePreferredSkillRow = (index: number) => {
  if (preferredSkillRows.value.length <= 1) return
  preferredSkillRows.value.splice(index, 1)
}

onMounted(loadSkills)
</script>

<template>
  <div class="page-container">

    <div class="page-header">
        <h2>建队申请</h2>
      </div>

  <el-card shadow="never" v-loading="loading" class="create-apply-card">
    

      <el-alert
        v-if="errorMessage"
        type="error"
        :closable="false"
        :title="errorMessage"
        style="margin-bottom: 12px"
      />

      <el-form label-width="120px">
        <el-form-item label="队伍名称">
          <el-input
            v-model="form.teamName"
            maxlength="100"
            show-word-limit
            placeholder="可选，不填则自动生成"
          />
        </el-form-item>
        <el-form-item label="说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="可选说明"
          />
        </el-form-item>
        <el-form-item label="队伍青睐技能">
          <div class="preferred-skills-editor">
            <div v-for="(row, index) in preferredSkillRows" :key="`preferred-skill-${index}`" class="preferred-skill-row">
              <el-select
                v-model="row.skillId"
                filterable
                clearable
                placeholder="请选择技能"
                :loading="skillsLoading"
                :disabled="skillsLoading"
                class="preferred-skill-select"
              >
                <el-option
                  v-for="s in skillOptions"
                  :key="`skill-${s.id}`"
                  :label="s.name || `ID:${s.id}`"
                  :value="s.id"
                  :disabled="typeof s.id === 'number' && row.skillId !== s.id && selectedSkillIds.has(s.id)"
                />
              </el-select>
              <el-input-number v-model="row.weight" :min="1" :max="10" class="preferred-skill-weight" />
              <el-button :disabled="preferredSkillRows.length <= 1" @click="removePreferredSkillRow(index)">删除</el-button>
            </div>
            <div class="preferred-skill-actions">
              <el-button type="primary" plain :disabled="preferredSkillRows.length >= 8" @click="addPreferredSkillRow">
                添加技能
              </el-button>
              <span class="skills-inline-hint">至少 1 项，最多 8 项</span>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <div class="page-actions">
        <el-button :disabled="loading" @click="handleCancel">取消</el-button>
        <el-button :loading="loading" type="primary" @click="handleSubmit">提交</el-button>
      </div>
    </el-card>
  </div></template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.page-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 16px;
}

.create-apply-card {
  max-width: 820px;
  margin: 0 auto;
}

.preferred-skills-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preferred-skill-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 130px 88px;
  gap: 8px;
  align-items: center;
}

.preferred-skill-select,
.preferred-skill-weight {
  width: 100%;
}

.preferred-skill-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.skills-inline-hint {
  color: #909399;
  font-size: 12px;
}

@media (max-width: 900px) {
  .preferred-skill-row {
    grid-template-columns: 1fr;
  }
}
</style>
