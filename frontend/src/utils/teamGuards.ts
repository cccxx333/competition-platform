export type TeamLike = {
  status?: string | null
}

export const isDisbanded = (team?: TeamLike | null): boolean => {
  return team?.status === "DISBANDED"
}

export const canWriteTeam = (team?: TeamLike | null): boolean => {
  return !isDisbanded(team)
}

export const getTeamWriteBlockReason = (team?: TeamLike | null): string | null => {
  if (isDisbanded(team)) return "队伍已解散，操作已禁止"
  return null
}

export const canToggleRecruiting = (team: TeamLike | null | undefined, role?: string, isLeader?: boolean): boolean => {
  if (!team) return false
  if (isDisbanded(team)) return false
  const status = team.status
  if (status !== "RECRUITING" && status !== "CLOSED") return false
  return role === "TEACHER" && Boolean(isLeader)
}
