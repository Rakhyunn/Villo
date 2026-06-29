import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/auth/LoginPage'
import NicknamePage from './pages/auth/NicknamePage'
import TodoHomePage from './pages/todo/TodoHomePage'
import VillageMainPage from './pages/village/VillageMainPage'
import MyPage from './pages/mypage/MyPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 인증 */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/nickname" element={<NicknamePage />} />

        {/* 메인 (바텀 네비) */}
        <Route path="/" element={<TodoHomePage />} />
        <Route path="/village" element={<VillageMainPage />} />
        <Route path="/my" element={<MyPage />} />

        {/* 미정의 경로 → 루트 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
