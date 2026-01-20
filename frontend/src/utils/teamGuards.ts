export type TeamLike = {
  status?: string | null
}

export const isDisbanded = (team?: TeamLike | null): boolean => {
  return team?.status === "DISBANDED"
}

export const canWriteTeam = (team?: TeamLike | null): boolean => {
  return !isDisbanded(team)
}

export const canCloseRecruiting = (team: TeamLike | null | undefined, role?: string, isLeader?: boolean): boolean => {
  if (!team) return false
  if (isDisbanded(team)) return false
  if (team.status !== "RECRUITING") return false
  if (role === "ADMIN") return true
  if (role === "TEACHER" && isLeader) return true
  return false
}
