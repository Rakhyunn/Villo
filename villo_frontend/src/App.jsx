import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import AuthGate from './components/common/AuthGate'
import LoginPage from './pages/auth/LoginPage'
import NicknamePage from './pages/auth/NicknamePage'
import TodoHomePage from './pages/todo/TodoHomePage'
import TodoRegisterPage from './pages/todo/TodoRegisterPage'
import TodoEditPage from './pages/todo/TodoEditPage'
import RepeatSettingPage from './pages/todo/RepeatSettingPage'
import PhotoCertPage from './pages/todo/PhotoCertPage'
import VillageMainPage from './pages/village/VillageMainPage'
import MyPage from './pages/mypage/MyPage'
import MyCalendarPage from './pages/mypage/MyCalendarPage'

// 로그인 + 닉네임 완료 필요
const app = (el) => <AuthGate mode="app">{el}</AuthGate>

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 인증 */}
        <Route
          path="/login"
          element={
            <AuthGate mode="guest">
              <LoginPage />
            </AuthGate>
          }
        />
        <Route
          path="/nickname"
          element={
            <AuthGate mode="onboarding">
              <NicknamePage />
            </AuthGate>
          }
        />

        {/* 투두 */}
        <Route path="/" element={app(<TodoHomePage />)} />
        <Route path="/todos/new" element={app(<TodoRegisterPage />)} />
        <Route path="/todos/:todoId/edit" element={app(<TodoEditPage />)} />
        <Route path="/todos/:todoId/repeat" element={app(<RepeatSettingPage />)} />
        <Route path="/todos/:todoId/certify" element={app(<PhotoCertPage />)} />

        {/* 메인 (바텀 네비) */}
        <Route path="/village" element={app(<VillageMainPage />)} />
        <Route path="/my" element={app(<MyPage />)} />
        <Route path="/my/calendar" element={app(<MyCalendarPage />)} />

        {/* 미정의 경로 → 루트 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
