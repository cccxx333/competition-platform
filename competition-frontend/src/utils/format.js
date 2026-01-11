import moment from 'moment';

// 格式化日期
export const formatDate = (date, format = 'YYYY-MM-DD') => {
  return moment(date).format(format);
};

// 格式化日期时间
export const formatDateTime = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  return moment(date).format(format);
};

// 计算时间差
export const getTimeFromNow = (date) => {
  return moment(date).fromNow();
};

// 格式化分数
export const formatScore = (score, decimals = 2) => {
  return (score * 100).toFixed(decimals) + '%';
};

// 格式化数字
export const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

// 截取文本
export const truncateText = (text, maxLength = 100) => {
  if (!text) return '';
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
};