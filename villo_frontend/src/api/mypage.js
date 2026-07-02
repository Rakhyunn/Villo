import api from './axios'

// 프로필 조회 → { id, nickname, email, provider, totalGold, dailyGold }
export const getMyProfile = () =>
  api.get('/api/v1/my/profile').then((r) => r.data.data)

// 통계 조회 → { completedTodoCount, consecutiveDays, villagerCount }
export const getMyStats = () =>
  api.get('/api/v1/my/stats').then((r) => r.data.data)

// 닉네임 변경
export const updateMyNickname = (nickname) =>
  api.put('/api/v1/my/nickname', { nickname }).then((r) => r.data.data)

// 월별 완료 날짜 목록 (달력 점 표시) → { completedDates: ["YYYY-MM-DD", ...] }
export const getMyCalendar = (year, month) =>
  api
    .get('/api/v1/my/calendar', { params: { year, month } })
    .then((r) => r.data.data)

// 날짜별 완료 투두 목록 → [{ todoId, title, category, earnedGold, isCertified, imageUrls }]
export const getMyDailyTodos = (date) =>
  api.get('/api/v1/my/todos', { params: { date } }).then((r) => r.data.data)
