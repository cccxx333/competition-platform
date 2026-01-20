<script lang="ts" setup>
import { ElMessage } from "element-plus"

const router = useRouter()
const teamId = ref<number | null>(null)

const goToTeam = () => {
  if (!teamId.value) {
    ElMessage.warning("请输入 Team ID")
    return
  }
  router.push(`/teams/${teamId.value}`)
}
</script>

<template>
  <el-card shadow="never">
    <div class="page-header">
      <h2>Team Lookup</h2>
    </div>

    <el-form label-width="120px" class="lookup-form">
      <el-form-item label="Team ID">
        <el-input-number v-model="teamId" :min="1" :controls="false" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="goToTeam">进入队伍详情</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.page-header {
  margin-bottom: 12px;
}

.lookup-form {
  max-width: 520px;
}
</style>
