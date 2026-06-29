import api, { API_BASE_URL } from './axios'

// 소셜 로그인 진입 — 백엔드 OAuth2 엔드포인트로 브라우저 전체 리다이렉트
// provider: 'kakao' | 'naver' | 'google'
export const socialLogin = (provider) => {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/${provider}`
}

// 닉네임 중복 확인
export const checkNickname = (nickname) =>
  api.get('/api/v1/auth/nickname/check', { params: { nickname } })

// 닉네임 + 마을 이름 최초 설정
export const setNickname = (nickname, villageName) =>
  api.post('/api/v1/auth/nickname', { nickname, villageName })

// Access Token 재발급
export const refreshToken = () => api.post('/api/v1/auth/token/refresh')

// 로그아웃
export const logout = () => api.post('/api/v1/auth/logout')
