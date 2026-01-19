import { computed, ref } from "vue"
import { ElMessage } from "element-plus"
import { bindMySkill, listMySkills, listSkills, unbindMySkill, type Skill, type UserSkill } from "@/api/skills"

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

export const useSkills = () => {
  // skills are input for the recommendation module
  const skills = ref<UserSkill[]>([])
  const availableSkills = ref<Skill[]>([])
  const loadingList = ref(false)
  const loadingOptions = ref(false)
  const actionLoading = ref(false)

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

  const bindSkill = async (skillId: number) => {
    if (!Number.isFinite(skillId) || skillId <= 0) {
      ElMessage.error("参数错误/skillId 不合法")
      return false
    }
    if (boundSkillIds.value.has(skillId)) {
      ElMessage.error("已绑定")
      return false
    }
    actionLoading.value = true
    try {
      await bindMySkill(skillId)
      await reload()
      return true
    } catch (error: any) {
      showRequestError(error, "Failed to bind skill")
      return false
    } finally {
      actionLoading.value = false
    }
  }

  const unbindSkill = async (skillId?: number) => {
    if (!skillId) return false
    actionLoading.value = true
    try {
      await unbindMySkill(skillId)
      await reload()
      return true
    } catch (error: any) {
      showRequestError(error, "Failed to unbind skill")
      return false
    } finally {
      actionLoading.value = false
    }
  }

  return {
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
  }
}
