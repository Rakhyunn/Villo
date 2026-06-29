import axios from 'axios'

// 백엔드 베이스 URL (기본: 로컬 Spring Boot 8080)
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const REFRESH_URL = '/api/v1/auth/token/refresh'

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // HttpOnly 쿠키(JWT) 전송
  headers: { 'Content-Type': 'application/json' },
})

// 401 → refresh 후 원요청 재시도 인터셉터
// 동시 401 → refresh 1회만 호출 (큐로 관리)
let isRefreshing = false
let pendingQueue = []

const resolveQueue = (error) => {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve()
  })
  pendingQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config: originalRequest } = error

    // 응답 없는 네트워크 에러 등은 그대로 전달
    if (!response || !originalRequest) return Promise.reject(error)

    const isUnauthorized = response.status === 401
    const isRefreshCall = originalRequest.url?.includes(REFRESH_URL)

    // 401이 아니거나, refresh 요청 자체가 실패했거나, 이미 재시도한 요청이면 중단
    if (!isUnauthorized || isRefreshCall || originalRequest._retry) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    // 이미 다른 요청이 refresh 진행 중이면 큐에 대기
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingQueue.push({ resolve, reject })
      }).then(() => api(originalRequest))
    }

    isRefreshing = true
    try {
      await api.post(REFRESH_URL)
      resolveQueue(null)
      return api(originalRequest) // 새 토큰 쿠키로 원요청 재시도
    } catch (refreshError) {
      resolveQueue(refreshError)
      // refresh 실패 → 로그인 페이지로
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  },
)

export default api
