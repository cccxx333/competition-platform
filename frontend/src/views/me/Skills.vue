<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { useSkills } from "@/composables/useSkills"
import { createSkill, listSkills, type Skill, type SkillPayload, updateSkill } from "@/api/skills"
import { useAuthStore } from "@/stores/auth"
import { getApiErrorMessage } from "@/utils/errorMessage"

const {
  skills,
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

const skillIdInput = ref("")
const selectedSkillId = ref<number | null>(null)

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
    adminSkills.value = await listSkills()
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

const formatDateValue = (value?: string) => {
  if (!value) return "-"
  if (value.includes("T")) {
    return value.split("T")[0]
  }
  return value
}

const handleBindSelected = async () => {
  if (selectedSkillId.value == null) {
    ElMessage.error("请选择技能")
    return
  }
  const success = await bindSkill(selectedSkillId.value)
  if (success) {
    selectedSkillId.value = null
  }
}

const handleBindManual = async () => {
  const skillId = Number(skillIdInput.value)
  const success = await bindSkill(skillId)
  if (success) {
    skillIdInput.value = ""
  }
}

const handleUnbind = async (skillId?: number) => {
  await unbindSkill(skillId)
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
  <template v-if="isAdmin">
    <el-card shadow="never" v-loading="adminLoading">
      <div class="page-header">
        <h2>技能管理</h2>
        <el-button size="small" type="primary" @click="openCreateSkillDialog">新建技能</el-button>
      </div>
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
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateValue(row.createdAt) }}
          </template>
        </el-table-column>
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
    <el-card shadow="never">
      <h2>我的技能</h2>
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
        <el-table-column prop="proficiency" label="熟练度" width="140" />
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button
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

    <el-card shadow="never" style="margin-top: 16px">
      <h2>绑定技能</h2>
      <el-form label-position="top">
        <el-form-item label="选择技能">
          <el-select
            v-model="selectedSkillId"
            placeholder="请选择技能"
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
          绑定
        </el-button>
      </el-form>

      <el-collapse style="margin-top: 16px">
        <el-collapse-item title="手动填写技能 ID" name="manual">
          <el-form label-position="top">
            <el-form-item label="技能 ID">
              <el-input v-model="skillIdInput" placeholder="请输入技能 ID" />
            </el-form-item>
            <el-button type="primary" :loading="actionLoading" @click="handleBindManual">绑定</el-button>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </template>
</template>
