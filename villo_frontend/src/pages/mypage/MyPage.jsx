import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomNav from '../../components/common/BottomNav'
import BottomSheet from '../../components/common/BottomSheet'
import { getMyProfile, getMyStats, updateMyNickname } from '../../api/mypage'
import { logout, checkNickname } from '../../api/auth'

// 소셜 provider 표시
const PROVIDER_LABEL = { KAKAO: '카카오', NAVER: '네이버', GOOGLE: '구글' }

// 백엔드 NicknameUpdateRequest 검증 규칙과 동일
const NICKNAME_REGEX = /^[가-힣a-zA-Z0-9]+$/
const validateNickname = (value) => {
  if (value.length < 2 || value.length > 10) return '닉네임은 2~10자여야 해요'
  if (!NICKNAME_REGEX.test(value)) return '한글, 영문, 숫자만 사용할 수 있어요'
  return null
}

function ChevronRight() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="m9 6 6 6-6 6"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

// 체크 아이콘
function CheckIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M20 6 9 17l-5-5"
        stroke="currentColor"
        strokeWidth="3"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

// 설정 메뉴 한 줄
function SettingRow({ icon, label, onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex w-full items-center justify-between px-4 py-3.5"
    >
      <span className="flex items-center gap-3">
        <span className="text-[18px]">{icon}</span>
        <span className="text-[14px] font-semibold text-text">{label}</span>
      </span>
      <span className="text-text-muted">
        <ChevronRight />
      </span>
    </button>
  )
}

export default function MyPage() {
  const navigate = useNavigate()

  const [profile, setProfile] = useState(null)
  const [stats, setStats] = useState(null)
  const [loggingOut, setLoggingOut] = useState(false)

  // 닉네임 변경 시트
  const [nickSheet, setNickSheet] = useState(false)
  const [newNick, setNewNick] = useState('')
  const [nickStatus, setNickStatus] = useState('idle') // idle | checking | available | error
  const [nickMessage, setNickMessage] = useState('')
  const [nickSaving, setNickSaving] = useState(false)

  useEffect(() => {
    // 프로필/통계 독립 호출 — 통계가 실패해도 프로필/골드는 노출
    getMyProfile()
      .then(setProfile)
      .catch(() => {})
    getMyStats()
      .then(setStats)
      .catch(() => {})
  }, [])

  const handleLogout = async () => {
    if (loggingOut) return
    setLoggingOut(true)
    try {
      await logout()
    } catch {
      // 실패해도 로그인 화면으로 (베스트 에포트)
    }
    navigate('/login')
  }

  // 닉네임 변경 시트 열기
  const openNickSheet = () => {
    if (!profile) return
    setNewNick(profile.nickname)
    setNickStatus('idle')
    setNickMessage('')
    setNickSheet(true)
  }

  // 입력 변경 → 확인 상태 초기화 (재확인 필요)
  const handleNickChange = (e) => {
    setNewNick(e.target.value)
    setNickStatus('idle')
    setNickMessage('')
  }

  const trimmedNick = newNick.trim()
  const nickIsAvailable = nickStatus === 'available'
  const canCheckNick =
    trimmedNick.length > 0 &&
    trimmedNick !== profile?.nickname &&
    nickStatus !== 'checking' &&
    !nickIsAvailable

  // 중복 확인 (기존 GET /auth/nickname/check 재사용)
  const handleCheckNick = async () => {
    const error = validateNickname(trimmedNick)
    if (error) {
      setNickStatus('error')
      setNickMessage(error)
      return
    }
    setNickStatus('checking')
    try {
      await checkNickname(trimmedNick)
      setNickStatus('available')
      setNickMessage('사용 가능한 닉네임이에요')
    } catch (err) {
      setNickStatus('error')
      setNickMessage(err.response?.data?.msg ?? '이미 사용 중인 닉네임이에요')
    }
  }

  // 닉네임 변경 저장 (중복 확인 통과 후)
  const handleSaveNickname = async () => {
    if (!nickIsAvailable || nickSaving) return
    setNickSaving(true)
    try {
      await updateMyNickname(trimmedNick)
      setProfile((p) => ({ ...p, nickname: trimmedNick }))
      setNickSheet(false)
    } catch (err) {
      setNickStatus('error')
      setNickMessage(err.response?.data?.msg ?? '변경에 실패했어요')
    } finally {
      setNickSaving(false)
    }
  }

  const providerLabel = profile ? (PROVIDER_LABEL[profile.provider] ?? '소셜') : ''
  const statVal = (v) => (v == null ? '—' : v.toLocaleString())

  return (
    <div className="flex min-h-screen justify-center bg-background">
      <div className="relative flex min-h-screen w-full max-w-[375px] flex-col bg-background">
        <main className="flex-grow overflow-y-auto pb-24">
          {/* 헤더 (프로필) */}
          <header className="bg-primary px-5 pb-12 pt-6">
            <h1 className="text-[16px] font-bold text-white">마이페이지</h1>

            <div className="mt-4 flex items-center gap-3">
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-white text-[28px] shadow-sm">
                🐻
              </div>
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <span className="text-[16px] font-bold text-white">
                    {profile?.nickname ?? '...'}
                  </span>
                  {profile && (
                    <span className="rounded-full bg-white px-2 py-0.5 text-[10px] font-bold text-text-sub">
                      {providerLabel} 로그인
                    </span>
                  )}
                </div>
                <p className="mt-0.5 truncate text-[12px] text-green-light">
                  {profile?.email ?? ''}
                </p>
              </div>
            </div>
          </header>

          {/* 통계 3카드 (헤더에 걸침) */}
          <section className="-mt-8 grid grid-cols-3 gap-2 px-5">
            <StatCard value={statVal(stats?.completedTodoCount)} label="완료 퀘스트" />
            <StatCard value={statVal(stats?.consecutiveDays)} label="연속 달성일" />
            <StatCard value={statVal(stats?.villagerCount)} label="보유 주민" />
          </section>

          {/* 보유 골드 */}
          <section className="mt-4 px-5">
            <div className="flex items-center justify-between rounded-2xl border border-amber-light bg-amber-light px-4 py-4">
              <div>
                <p className="text-[12px] font-semibold text-amber-dark/70">
                  보유 골드
                </p>
                <p className="mt-0.5 text-[22px] font-extrabold text-amber-dark">
                  {profile ? profile.totalGold.toLocaleString() : '—'} G
                </p>
                <p className="mt-0.5 text-[11px] font-medium text-amber-dark/70">
                  오늘 획득 +{profile ? profile.dailyGold.toLocaleString() : '0'} G
                </p>
              </div>
              <div className="flex h-11 w-11 items-center justify-center rounded-full bg-accent text-[20px] text-white shadow-sm">
                🪙
              </div>
            </div>
          </section>

          {/* 내 마을 (마을 조회 API 부재 → 보유 주민 수만) */}
          <section className="mt-5 px-5">
            <p className="mb-2 text-[13px] font-bold text-text-sub">내 마을</p>
            <button
              type="button"
              onClick={() => navigate('/village')}
              className="flex w-full items-center justify-between rounded-2xl border border-border-base bg-white p-4"
            >
              <span className="flex items-center gap-3">
                <span className="flex h-11 w-11 items-center justify-center rounded-full bg-green-light text-[20px]">
                  🏘️
                </span>
                <span className="flex flex-col items-start">
                  <span className="text-[14px] font-bold text-text">나의 마을</span>
                  <span className="text-[11px] text-text-sub">
                    보유 주민 {statVal(stats?.villagerCount)}명
                  </span>
                </span>
              </span>
              <ChevronRight />
            </button>
          </section>

          {/* 설정 */}
          <section className="mt-5 px-5">
            <p className="mb-2 text-[13px] font-bold text-text-sub">설정</p>
            <div className="divide-y divide-border-base/60 overflow-hidden rounded-2xl border border-border-base bg-white">
              <SettingRow
                icon="👤"
                label="닉네임 변경"
                onClick={openNickSheet}
              />
              <SettingRow
                icon="📊"
                label="퀘스트 통계"
                onClick={() => navigate('/my/calendar')}
              />
            </div>
          </section>

          {/* 로그아웃 */}
          <section className="mt-6 px-5">
            <button
              type="button"
              onClick={handleLogout}
              disabled={loggingOut}
              className="h-14 w-full rounded-xl bg-primary text-[15px] font-bold text-white shadow-lg transition active:scale-[0.98] disabled:opacity-60"
            >
              {loggingOut ? '로그아웃 중...' : '로그아웃'}
            </button>
          </section>
        </main>

        {/* 닉네임 변경 바텀시트 */}
        <BottomSheet
          open={nickSheet}
          onClose={() => !nickSaving && setNickSheet(false)}
          title="닉네임 변경"
        >
          <div
            className={`relative flex items-center rounded-xl border-2 bg-white transition-colors ${
              nickIsAvailable
                ? 'border-primary'
                : nickStatus === 'error'
                  ? 'border-error'
                  : 'border-border-base focus-within:border-primary'
            }`}
          >
            <input
              type="text"
              value={newNick}
              onChange={handleNickChange}
              placeholder="새 닉네임을 입력해주세요"
              maxLength={10}
              autoFocus
              className="h-12 w-full rounded-xl bg-transparent px-4 text-[15px] text-text outline-none placeholder:text-text-muted"
            />
            <button
              type="button"
              onClick={handleCheckNick}
              disabled={!canCheckNick}
              className={`absolute right-2 rounded-lg px-3 py-1.5 text-[11px] font-bold text-white transition active:scale-95 ${
                canCheckNick || nickIsAvailable
                  ? 'bg-green-dark'
                  : 'cursor-not-allowed bg-border-base'
              }`}
            >
              {nickStatus === 'checking'
                ? '확인 중...'
                : nickIsAvailable
                  ? '확인 완료'
                  : '중복 확인'}
            </button>
          </div>
          {nickMessage && (
            <div
              className={`mt-1.5 flex items-center gap-1 text-[12px] font-semibold ${
                nickIsAvailable ? 'text-primary' : 'text-error'
              }`}
            >
              {nickIsAvailable && <CheckIcon />}
              <span>{nickMessage}</span>
            </div>
          )}
          <button
            type="button"
            onClick={handleSaveNickname}
            disabled={!nickIsAvailable || nickSaving}
            className={`mt-4 h-12 w-full rounded-xl text-[15px] font-bold transition active:scale-[0.98] ${
              nickIsAvailable && !nickSaving
                ? 'bg-primary text-white shadow-lg'
                : 'cursor-not-allowed bg-border-base text-text-sub'
            }`}
          >
            {nickSaving ? '변경 중...' : '변경하기'}
          </button>
        </BottomSheet>

        <BottomNav />
      </div>
    </div>
  )
}

function StatCard({ value, label }) {
  return (
    <div className="flex flex-col items-center rounded-2xl border border-border-base bg-white py-3 shadow-sm">
      <span className="text-[20px] font-extrabold text-primary">{value}</span>
      <span className="mt-0.5 text-[11px] font-medium text-text-sub">{label}</span>
    </div>
  )
}
