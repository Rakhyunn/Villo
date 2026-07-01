import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/auth/LoginPage'
import NicknamePage from './pages/auth/NicknamePage'
import TodoHomePage from './pages/todo/TodoHomePage'
import TodoRegisterPage from './pages/todo/TodoRegisterPage'
import TodoEditPage from './pages/todo/TodoEditPage'
import RepeatSettingPage from './pages/todo/RepeatSettingPage'
import PhotoCertPage from './pages/todo/PhotoCertPage'
import VillageMainPage from './pages/village/VillageMainPage'
import MyPage from './pages/mypage/MyPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 인증 */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/nickname" element={<NicknamePage />} />

        {/* 투두 */}
        <Route path="/" element={<TodoHomePage />} />
        <Route path="/todos/new" element={<TodoRegisterPage />} />
        <Route path="/todos/:todoId/edit" element={<TodoEditPage />} />
        <Route path="/todos/:todoId/repeat" element={<RepeatSettingPage />} />
        <Route path="/todos/:todoId/certify" element={<PhotoCertPage />} />

        {/* 메인 (바텀 네비) */}
        <Route path="/village" element={<VillageMainPage />} />
        <Route path="/my" element={<MyPage />} />

        {/* 미정의 경로 → 루트 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
