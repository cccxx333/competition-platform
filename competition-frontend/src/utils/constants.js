// API 基础配置
export const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

// 页面大小配置
export const PAGE_SIZES = {
  SMALL: 5,
  MEDIUM: 10,
  LARGE: 20
};

// 竞赛状态
export const COMPETITION_STATUS = {
  UPCOMING: { text: '即将开始', color: 'blue' },
  ONGOING: { text: '进行中', color: 'green' },
  FINISHED: { text: '已结束', color: 'gray' }
};

// 队伍状态
export const TEAM_STATUS = {
  RECRUITING: { text: '招募中', color: 'green' },
  FULL: { text: '已满员', color: 'orange' },
  DISBANDED: { text: '已解散', color: 'red' }
};

// 技能熟练度
export const SKILL_PROFICIENCY = {
  1: '初学者',
  2: '入门',
  3: '熟练',
  4: '精通',
  5: '专家'
};

// 竞赛分类
export const COMPETITION_CATEGORIES = [
  '编程',
  '数学建模',
  '创新创业',
  '电子设计',
  '机械设计',
  '人工智能',
  '数据科学',
  '网络安全',
  '其他'
];

// 竞赛级别
export const COMPETITION_LEVELS = [
  '国际级',
  '国家级',
  '省级',
  '市级',
  '校级'
];