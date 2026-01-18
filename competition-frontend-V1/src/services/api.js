import axios from 'axios';

// 创建 axios 实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 用户相关 API
export const userAPI = {
  register: (userData) => api.post('/users/register', userData),
  login: (credentials) => api.post('/users/login', credentials),
  getProfile: () => api.get('/users/profile'),
  updateProfile: (userData) => api.put('/users/profile', userData),
  updateSkills: (skills) => api.put('/users/skills', skills),
  // 获取用户技能
  getUserSkills: (userId) => api.get(`/users/${userId}/skills`),
  
  // 添加用户技能
  addUserSkill: (skillData) => api.post('/users/skills', skillData),
  
  // 更新用户技能
  updateUserSkill: (userSkillId, skillData) => api.put(`/users/skills/${userSkillId}`, skillData),
  
  // 删除用户技能
  deleteUserSkill: (userSkillId) => api.delete(`/users/skills/${userSkillId}`),
  // 获取用户技能统计
    getUserSkillStats: (userId) => api.get(`/users/${userId}/skills/stats`),

    // 根据分类获取用户技能
    getUserSkillsByCategory: (userId, category) => api.get(`/users/${userId}/skills/category/${category}`),

    // 批量添加用户技能
    addUserSkillsBatch: (skillsData) => api.post('/users/skills/batch', skillsData),
};

// 竞赛相关 API
export const competitionAPI = {
  getCompetitions: (params) => api.get('/competitions', { params }),
  getCompetitionById: (id) => api.get(`/competitions/${id}`),
  searchCompetitions: (keyword) => api.get(`/competitions/search?keyword=${keyword}`),
  getCompetitionsByCategory: (category) => api.get(`/competitions/category/${category}`),
};

// 队伍相关 API
export const teamAPI = {
  getTeams: (params) => api.get('/teams', { params }),
  getTeamById: (id) => api.get(`/teams/${id}`),
  createTeam: (teamData) => api.post('/teams', teamData),
  joinTeam: (teamId) => api.post(`/teams/${teamId}/join`),
  leaveTeam: (teamId) => api.post(`/teams/${teamId}/leave`),
};

// 推荐相关 API
export const recommendationAPI = {
  getCompetitionRecommendations: (limit = 10) =>
    api.get(`/recommendations/competitions?limit=${limit}`),
  getTeamRecommendations: (limit = 10) =>
    api.get(`/recommendations/teams?limit=${limit}`),
  getMemberRecommendations: (teamId, limit = 10) =>
    api.get(`/recommendations/members/${teamId}?limit=${limit}`),
};

// 技能相关 API
export const skillAPI = {
  // 获取所有技能
  getAllSkills: () => api.get('/skills'),

  // 根据分类获取技能
  getSkillsByCategory: (category) => api.get(`/skills/category/${category}`),

  // 获取所有分类
  getAllCategories: () => api.get('/skills/categories'),

  // 根据ID获取技能
  getSkillById: (id) => api.get(`/skills/${id}`),

  // 搜索技能
  searchSkills: (keyword) => api.get(`/skills/search?keyword=${keyword}`)
};

export default api;