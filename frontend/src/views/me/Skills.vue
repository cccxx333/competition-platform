<script lang="ts" setup>
import { ElMessage } from "element-plus"
import { useSkills } from "@/composables/useSkills"

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

const skillIdInput = ref("")
const selectedSkillId = ref<number | null>(null)

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

onMounted(() => {
  reload()
  loadAllSkills()
})
</script>

<template>
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
