import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getMyProfile } from '../../api/mypage'

// 인증/온보딩 상태에 따른 라우트 접근 제어
// mode:
//   'app'        — 로그인 + 닉네임 설정 완료 필요 (메인/투두/마을/마이)
//   'onboarding' — 로그인 필요 + 닉네임 미설정 상태만 (닉네임 설정)
//   'guest'      — 비로그인 전용 (로그인 페이지)
export default function AuthGate({ mode, children }) {
  const navigate = useNavigate()
  const [ready, setReady] = useState(false)

  useEffect(() => {
    let alive = true

    getMyProfile()
      .then((profile) => {
        if (!alive) return
        const hasNickname = !!profile.nickname

        if (mode === 'guest') {
          // 이미 로그인됨 → 온보딩 여부에 따라 이동
          navigate(hasNickname ? '/' : '/nickname', { replace: true })
        } else if (mode === 'onboarding') {
          // 닉네임 이미 설정됨 → 메인으로 (온보딩 재진입 방지)
          if (hasNickname) navigate('/', { replace: true })
          else setReady(true)
        } else {
          // app — 닉네임 미설정이면 온보딩으로
          if (!hasNickname) navigate('/nickname', { replace: true })
          else setReady(true)
        }
      })
      .catch(() => {
        if (!alive) return
        // 비로그인(401 등)
        if (mode === 'guest') setReady(true)
        else navigate('/login', { replace: true })
      })

    return () => {
      alive = false
    }
  }, [mode, navigate])

  // 판별 중에는 빈 배경 (깜빡임 방지)
  if (!ready) return <div className="min-h-screen bg-background" />
  return children
}
