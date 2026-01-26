<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { useSkills } from "@/composables/useSkills"
import { bindMySkill, createSkill, listSkills, updateMySkillLevel, type Skill, type SkillPayload, updateSkill } from "@/api/skills"
import { useAuthStore } from "@/stores/auth"
import { getApiErrorMessage } from "@/utils/errorMessage"

const {
  skills,
  availableSkills,
  loadingList,
  loadingOptions,
  actionLoading,
  boundSkillIds,
  selectableSkills,
  reload,
  loadAllSkills,
  bindSkill,
  unbindSkill
} = useSkills()

const authStore = useAuthStore()
const isAdmin = computed(() => String(authStore.user?.role ?? "").toUpperCase() === "ADMIN")

const defaultProficiency = 3
const selectedSkillId = ref<number | null>(null)
const selectedProficiency = ref(defaultProficiency)

const adminSkills = ref<Skill[]>([])
const adminLoading = ref(false)
const adminDialogVisible = ref(false)
const adminDialogLoading = ref(false)
const adminDialogMode = ref<"create" | "edit">("create")
const adminDialogForm = ref({
  id: null as number | null,
  name: "",
  category: "",
  description: ""
})
const adminDialogError = ref("")

const loadAdminSkills = async () => {
  adminLoading.value = true
  try {
    adminSkills.value = (await listSkills()).slice().sort((a, b) => {
      const aId = a.id ?? Number.MAX_SAFE_INTEGER
      const bId = b.id ?? Number.MAX_SAFE_INTEGER
      return aId - bId
    })
  } catch (error: any) {
    ElMessage.error(getApiErrorMessage(error, "加载技能列表失败"))
    adminSkills.value = []
  } finally {
    adminLoading.value = false
  }
}

const resetAdminDialog = () => {
  adminDialogForm.value = {
    id: null,
    name: "",
    category: "",
    description: ""
  }
  adminDialogError.value = ""
}

const openCreateSkillDialog = () => {
  adminDialogMode.value = "create"
  resetAdminDialog()
  adminDialogVisible.value = true
}

const openEditSkillDialog = (skill: Skill) => {
  adminDialogMode.value = "edit"
  adminDialogForm.value = {
    id: skill.id ?? null,
    name: skill.name ?? "",
    category: skill.category ?? "",
    description: skill.description ?? ""
  }
  adminDialogError.value = ""
  adminDialogVisible.value = true
}

const closeAdminDialog = () => {
  adminDialogVisible.value = false
  resetAdminDialog()
}

const submitAdminSkill = async () => {
  const nameTrimmed = adminDialogForm.value.name.trim()
  if (!nameTrimmed) {
    adminDialogError.value = "技能名称不能为空"
    return
  }
  adminDialogLoading.value = true
  adminDialogError.value = ""
  const payload: SkillPayload = {
    name: nameTrimmed,
    category: adminDialogForm.value.category.trim() || undefined,
    description: adminDialogForm.value.description.trim() || undefined
  }
  try {
    if (adminDialogMode.value === "create") {
      await createSkill(payload)
      ElMessage.success("技能已创建")
    } else if (adminDialogForm.value.id) {
      await updateSkill(adminDialogForm.value.id, payload)
      ElMessage.success("技能已更新")
    }
    await loadAdminSkills()
    closeAdminDialog()
  } catch (error: any) {
    adminDialogError.value = getApiErrorMessage(error, "操作失败")
  } finally {
    adminDialogLoading.value = false
  }
}

const loadSelectableSkills = async (keyword?: string) => {
  loadingOptions.value = true
  try {
    availableSkills.value = await listSkills(keyword?.trim() || undefined)
  } catch (error: any) {
    ElMessage.error(getApiErrorMessage(error, "加载技能列表失败"))
    availableSkills.value = []
  } finally {
    loadingOptions.value = false
  }
}

const handleSkillSearch = (keyword: string) => {
  loadSelectableSkills(keyword)
}

const handleSelectVisible = (visible: boolean) => {
  if (!visible) return
  loadSelectableSkills()
}

const handleBindSelected = async () => {
  if (selectedSkillId.value == null) {
    ElMessage.error("请选择技能")
    return
  }
  actionLoading.value = true
  try {
    await bindMySkill(selectedSkillId.value, selectedProficiency.value)
    await reload()
    selectedSkillId.value = null
    selectedProficiency.value = defaultProficiency
  } catch (error: any) {
    ElMessage.error(getApiErrorMessage(error, "绑定失败"))
  } finally {
    actionLoading.value = false
  }
}

const handleUnbind = async (skillId?: number) => {
  await unbindSkill(skillId)
}

const handleUpdateProficiency = async (row: { skillId?: number }, value: number) => {
  if (!row.skillId) return
  actionLoading.value = true
  try {
    await updateMySkillLevel(row.skillId, value)
    ElMessage.success("已更新熟练度")
    await reload()
  } catch (error: any) {
    ElMessage.error(getApiErrorMessage(error, "更新熟练度失败"))
  } finally {
    actionLoading.value = false
  }
}

const loadViewData = () => {
  if (isAdmin.value) {
    loadAdminSkills()
  } else {
    reload()
    loadAllSkills()
  }
}

onMounted(loadViewData)
watch(isAdmin, () => {
  loadViewData()
})
</script>

<template>
  <div class="page-container">
    <template v-if="isAdmin">
      <div class="admin-header-wrap">
        <div class="page-header admin-header">
          <h2>技能管理</h2>
          <el-button size="default" type="primary" @click="openCreateSkillDialog">新建技能</el-button>
        </div>
      </div>

      <el-card shadow="never" v-loading="adminLoading" class="admin-skill-card">
        <el-table
          :data="adminSkills"
          v-loading="adminLoading"
          style="width: 100%"
          empty-text="暂无技能，请先新增"
          @row-click="openEditSkillDialog"
        >
          <el-table-column prop="id" label="技能 ID" width="120" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="category" label="分类" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button type="text" size="small" @click.stop="openEditSkillDialog(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-dialog
        v-model="adminDialogVisible"
        :title="adminDialogMode === 'create' ? '新建技能' : '编辑技能'"
        width="520px"
        append-to-body
        top="12vh"
        :close-on-click-modal="false"
      >
        <el-form label-position="top">
          <el-form-item label="名称">
            <el-input v-model="adminDialogForm.name" placeholder="请输入技能名称" />
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="adminDialogForm.category" placeholder="请输入技能分类（可选）" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input
              v-model="adminDialogForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入技能描述（可选）"
            />
          </el-form-item>
        </el-form>
        <el-alert
          v-if="adminDialogError"
          type="error"
          :closable="false"
          :title="adminDialogError"
          style="margin-bottom: 12px"
        />
        <template #footer>
          <el-button @click="closeAdminDialog">取消</el-button>
          <el-button type="primary" :loading="adminDialogLoading" @click="submitAdminSkill">确认</el-button>
        </template>
      </el-dialog>
    </template>

    <template v-else>
      <div class="page-header">
        <h2>我的技能</h2>
      </div>
      <el-card shadow="never">
        <el-alert
          class="skills-hint"
          type="info"
          :closable="false"
          title="当前技能画像将作为后续竞赛推荐与组队匹配的重要依据。"
          style="margin-bottom: 12px"
        />
        <el-table :data="skills" v-loading="loadingList" style="width: 100%">
          <el-table-column prop="skillId" label="技能 ID" width="120" />
          <el-table-column prop="skillName" label="名称" />
          <el-table-column prop="skillCategory" label="分类" />
          <el-table-column label="熟练度" width="160">
            <template #default="{ row }">
              <el-select
                v-model="row.proficiency"
                size="small"
                :style="{ width: '80px' }"
                :disabled="actionLoading"
                @change="(value) => handleUpdateProficiency(row, value)"
              >
                <el-option v-for="level in [1, 2, 3, 4, 5]" :key="level" :label="level" :value="level" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="left" header-align="left" class-name="action-column">
            <template #default="scope">
              <el-button
                type="text"
                size="small"
                :loading="actionLoading"
                :disabled="loadingList"
                @click="handleUnbind(scope.row.skillId)"
              >
                解绑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!loadingList && skills.length === 0">暂无技能，请先绑定</div>
      </el-card>

      <div class="page-header" style="margin-top: 16px">
        <h2>绑定技能</h2>
      </div>
      <el-card shadow="never">
        <el-form label-position="top">
          <div style="display: flex; gap: 16px; align-items: flex-end; flex-wrap: wrap;">
            <el-form-item label="选择技能" style="margin-bottom: 0;">
              <el-select
                v-model="selectedSkillId"
                filterable
                remote
                :remote-method="handleSkillSearch"
                placeholder="请输入技能名称搜索"
                :loading="loadingOptions"
                clearable
                :style="{ width: '240px' }"
                @visible-change="handleSelectVisible"
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
            <el-form-item label="熟练度" style="margin-bottom: 0;">
              <el-select v-model="selectedProficiency" placeholder="请选择熟练度" :style="{ width: '90px' }">
                <el-option v-for="level in [1, 2, 3, 4, 5]" :key="level" :label="level" :value="level" />
              </el-select>
            </el-form-item>
            <el-button
              type="primary"
              :loading="actionLoading"
              :disabled="selectedSkillId == null || boundSkillIds.has(selectedSkillId)"
              @click="handleBindSelected"
            >
              绑定
            </el-button>
          </div>
        </el-form>
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.action-column :deep(.cell) {
  padding-left: 0;
}

.action-column :deep(.el-button) {
  padding-left: 0;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-header-wrap {
  max-width: 920px;
  margin: 0 auto 12px;
}

.admin-skill-card {
  max-width: 920px;
  margin: 0 auto;
}
</style>

