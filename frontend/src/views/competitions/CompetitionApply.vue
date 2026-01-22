<script lang="ts" setup>
import {
  getRecommendedTeams,
  listApplyableCompetitions,
  type ApplyableCompetitionItem,
  type TeamRecommendation,
  type TeamRecommendReason
} from "@/api/competitions"
import { createApplication } from "@/api/teamApplications"
import { getApiErrorMessage } from "@/utils/errorMessage"

const competitionOptions = ref<ApplyableCompetitionItem[]>([])
const selectedCompetitionId = ref<number | null>(null)
const competitionLoading = ref(false)
const teamLoading = ref(false)
const teamRows = ref<TeamRecommendation[]>([])
const fallbackSorted = ref(false)
const errorDialogVisible = ref(false)
const errorDialogMessage = ref("")
const successDialogVisible = ref(false)
const successDialogMessage = ref("")
const applyDialogVisible = ref(false)
const applyDialogLoading = ref(false)
const pendingApplyTeam = ref<{ teamId: number; teamName?: string } | null>(null)
const applyRemark = ref("")
const applyDialogError = ref("")

let searchTimer: number | undefined

const statusLabelMap: Record<string, string> = {
  RECRUITING: "招募中",
  CLOSED: "已结束",
  DISBANDED: "已解散"
}

const fetchCompetitions = async (keyword: string) => {
  competitionLoading.value = true
  try {
    const items = await listApplyableCompetitions(keyword, 0, 20)
    competitionOptions.value = items
  } catch (error: any) {
    competitionOptions.value = []
    showErrorDialog(getApiErrorMessage(error, "加载可报名竞赛失败"))
  } finally {
    competitionLoading.value = false
  }
}

const handleSearch = (keyword: string) => {
  if (searchTimer) {
    window.clearTimeout(searchTimer)
  }
  searchTimer = window.setTimeout(() => {
    fetchCompetitions(keyword)
  }, 300)
}

const handleDropdownVisible = (visible: boolean) => {
  if (!visible) return
  fetchCompetitions("")
}

const fetchTeams = async (competitionId: number) => {
  teamLoading.value = true
  try {
    const teams = await getRecommendedTeams(competitionId, 10)
    teamRows.value = teams
    fallbackSorted.value = Boolean(teams[0]?.fallbackSorted)
  } catch (error: any) {
    teamRows.value = []
    fallbackSorted.value = false
    showErrorDialog(getApiErrorMessage(error, "加载推荐队伍失败"))
  } finally {
    teamLoading.value = false
  }
}

const handleCompetitionChange = (value: number | null) => {
  teamRows.value = []
  fallbackSorted.value = false
  if (!value) return
  fetchTeams(value)
}

const buildReasonText = (reasons?: TeamRecommendReason[]) => {
  if (!reasons || reasons.length === 0) {
    return "原因：暂无匹配技能"
  }
  const items = reasons.map((reason) => {
    const name = reason.skillName ?? "技能"
    const skillId = reason.skillId ?? "-"
    const weight = reason.weight ?? 1
    return `${name}（${skillId}×${weight}）`
  })
  return `原因：Matched：${items.join("，")}`
}

const showErrorDialog = (message: string) => {
  errorDialogMessage.value = message
  errorDialogVisible.value = true
}

const showSuccessDialog = (message: string) => {
  successDialogMessage.value = message
  successDialogVisible.value = true
}

const closeErrorDialog = () => {
  errorDialogVisible.value = false
}

const closeSuccessDialog = () => {
  successDialogVisible.value = false
}

const buildApplyErrorMessage = (error: any) => {
  const status = error?.status ?? error?.response?.status
  if (status === 400) return getApiErrorMessage(error, "参数错误，请检查后重试。")
  if (status === 403) return getApiErrorMessage(error, "无权限。")
  if (status === 404) return getApiErrorMessage(error, "资源不存在（竞赛或队伍不存在）。")
  if (status === 409) return getApiErrorMessage(error, "报名失败：存在冲突（可能已报名、队伍不可报名或人数已满）。")
  return getApiErrorMessage(error, "服务异常，请稍后重试。")
}

const handleApplyClick = (row: TeamRecommendation) => {
  if (!row?.teamId) {
    showErrorDialog("队伍信息异常，请稍后重试。")
    return
  }
  pendingApplyTeam.value = { teamId: row.teamId, teamName: row.teamName }
  applyRemark.value = ""
  applyDialogError.value = ""
  applyDialogVisible.value = true
}

const cancelApply = () => {
  applyDialogVisible.value = false
  pendingApplyTeam.value = null
  applyRemark.value = ""
  applyDialogError.value = ""
}

const confirmApply = async () => {
  if (!selectedCompetitionId.value) {
    applyDialogError.value = "请先选择竞赛。"
    return
  }
  if (!pendingApplyTeam.value?.teamId) {
    applyDialogError.value = "请选择队伍后再提交申请。"
    return
  }
  applyDialogLoading.value = true
  applyDialogError.value = ""
  try {
    await createApplication({
      competitionId: selectedCompetitionId.value,
      teamId: pendingApplyTeam.value.teamId,
      remark: applyRemark.value ? applyRemark.value : undefined
    })
    applyDialogVisible.value = false
    pendingApplyTeam.value = null
    applyRemark.value = ""
    showSuccessDialog("报名申请已提交，可在“我的申请”中查看进度。")
    if (selectedCompetitionId.value) {
      fetchTeams(selectedCompetitionId.value)
    }
  } catch (error: any) {
    applyDialogError.value = buildApplyErrorMessage(error)
  } finally {
    applyDialogLoading.value = false
  }
}
</script>

<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>竞赛报名</h2>
    </div>

    <el-form label-width="0" class="apply-form">
      <el-form-item>
        <el-select
          v-model="selectedCompetitionId"
          filterable
          remote
          clearable
          :remote-method="handleSearch"
          :loading="competitionLoading"
          no-data-text="暂无可报名竞赛"
          placeholder="请选择竞赛（可输入关键字搜索）"
          style="width: 360px"
          @visible-change="handleDropdownVisible"
          @change="handleCompetitionChange"
        >
          <el-option
            v-for="item in competitionOptions"
            :key="item.id"
            :label="item.name ?? ''"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <div v-if="selectedCompetitionId" class="team-panel">
      <el-alert
        v-if="fallbackSorted"
        type="warning"
        :closable="false"
        title="当前竞赛下队伍与您的技能匹配度整体较低，已按默认顺序展示。"
        style="margin-bottom: 12px"
      />

      <el-table :data="teamRows" v-loading="teamLoading" style="width: 100%" empty-text="暂无可申请队伍">
        <el-table-column prop="teamName" label="队伍名称" min-width="180" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="row.teamStatus === 'RECRUITING' ? 'success' : row.teamStatus === 'CLOSED' ? 'info' : 'danger'">
              {{ statusLabelMap[row.teamStatus ?? ""] ?? "未知" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="匹配分" width="120">
          <template #default="{ row }">
            <span>{{ typeof row.matchScore === "number" ? row.matchScore.toFixed(3) : "-" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="推荐信息" min-width="260">
          <template #default="{ row }">
            {{ buildReasonText(row.reasons) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleApplyClick(row)">申请</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-card>

  <el-dialog v-model="errorDialogVisible" title="提示" width="420px" :close-on-click-modal="true">
    <div>{{ errorDialogMessage }}</div>
    <template #footer>
      <el-button type="primary" @click="closeErrorDialog">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="successDialogVisible" title="提示" width="420px" :close-on-click-modal="true">
    <div>{{ successDialogMessage }}</div>
    <template #footer>
      <el-button type="primary" @click="closeSuccessDialog">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="applyDialogVisible"
    title="提示"
    width="520px"
    :align-center="true"
    top="12vh"
    append-to-body="true"
    :close-on-click-modal="false"
  >
    <div>确认提交报名申请吗？</div>
    <div v-if="pendingApplyTeam" class="apply-dialog__team">
      <span>队伍：{{ pendingApplyTeam.teamName ?? "未知队伍" }}（ID：{{ pendingApplyTeam.teamId }}）</span>
    </div>
    <el-form label-width="80px" class="apply-dialog__form">
      <el-form-item label="申请备注">
        <el-input
          type="textarea"
          v-model="applyRemark"
          placeholder="可填写申请备注（可选）"
          :rows="3"
        />
      </el-form-item>
    </el-form>
    <div v-if="applyDialogError" class="apply-dialog__error">
      {{ applyDialogError }}
    </div>
    <template #footer>
      <el-button @click="cancelApply">取消</el-button>
      <el-button type="primary" :loading="applyDialogLoading" @click="confirmApply">确认</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.page-header {
  margin-bottom: 12px;
}

.apply-form {
  margin-bottom: 12px;
}

.team-panel {
  margin-top: 8px;
}

.apply-dialog__team {
  margin: 12px 0 8px;
  color: #1f2937;
}

.apply-dialog__form {
  margin-top: 4px;
}

.apply-dialog__error {
  margin-top: 8px;
  color: #f56c6c;
}
</style>
