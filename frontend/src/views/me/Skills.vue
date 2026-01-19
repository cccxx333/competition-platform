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
    <h2>My Skills</h2>
    <el-alert
      class="skills-hint"
      type="info"
      :closable="false"
      title="当前技能画像将作为后续竞赛推荐与组队匹配的重要依据。"
      style="margin-bottom: 12px"
    />
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
