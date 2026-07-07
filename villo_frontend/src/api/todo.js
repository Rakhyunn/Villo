import api from './axios'

// 오늘의 투두 목록 조회
export const getTodayTodos = () =>
  api.get('/api/v1/todos').then((r) => r.data.data)

// AI 분석 (DB 저장 안 함) → { category, difficulty, gold }
export const analyzeTodo = (title) =>
  api.post('/api/v1/todos/analyze', { title }).then((r) => r.data.data)

// 투두 등록 → TodoResponse
// payload: { title, category, difficulty, gold, isRepeat, repeatConfig }
export const createTodo = (payload) =>
  api.post('/api/v1/todos', payload).then((r) => r.data.data)

// 투두 제목 수정 (제목 변경 시 서버가 AI 재분석) → TodoResponse
export const updateTodoTitle = (todoId, title) =>
  api.put(`/api/v1/todos/${todoId}`, { title }).then((r) => r.data.data)

// 반복 설정 등록 → RepeatConfigResponse
export const createRepeat = (todoId, config) =>
  api.post(`/api/v1/todos/${todoId}/repeat`, config).then((r) => r.data.data)

// 반복 설정 수정 → RepeatConfigResponse
export const updateRepeat = (todoId, config) =>
  api.put(`/api/v1/todos/${todoId}/repeat`, config).then((r) => r.data.data)

// 반복 설정 삭제
export const deleteRepeat = (todoId) =>
  api.delete(`/api/v1/todos/${todoId}/repeat`).then((r) => r.data.data)

// 투두 일반 완료 처리 → { earnedGold, totalGold, remainingDaily }
export const completeTodo = (todoId) =>
  api.post(`/api/v1/todos/${todoId}/complete`).then((r) => r.data.data)

// 사진 인증 완료 처리 → { earnedGold, totalGold, remainingDaily }
export const certifyTodo = (todoId, imageUrls) =>
  api
    .post(`/api/v1/todos/${todoId}/certify`, { imageUrls })
    .then((r) => r.data.data)

// 업로드용 Presigned URL 발급 → { uploadUrl, imageUrl }
export const getPresignedUrl = (fileName) =>
  api
    .post('/api/v1/todos/images/presigned-url', { fileName })
    .then((r) => r.data.data)

// 이미지 1장 업로드 (presigned URL 발급 → S3 직접 PUT) → imageUrl
// ※ S3 PUT은 우리 API 인스턴스(쿠키/인터셉터)를 타면 안 되므로 순수 fetch 사용
export const uploadImage = async (file) => {
  // 확장자 없으면 백엔드가 500 → 안전하게 보장
  const fileName = file.name.includes('.') ? file.name : `${file.name}.jpg`
  const { uploadUrl, imageUrl } = await getPresignedUrl(fileName)
  const res = await fetch(uploadUrl, { method: 'PUT', body: file })
  if (!res.ok) throw new Error('이미지 업로드에 실패했어요')
  return imageUrl
}

// 투두 삭제 (소프트 딜리트)
export const deleteTodo = (todoId) =>
  api.delete(`/api/v1/todos/${todoId}`).then((r) => r.data.data)
