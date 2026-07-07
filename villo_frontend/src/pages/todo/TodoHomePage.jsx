import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomNav from '../../components/common/BottomNav'
import TodoCard from '../../components/todo/TodoCard'
import { getTodayTodos, completeTodo } from '../../api/todo'
import { getMyProfile } from '../../api/mypage'

// 골드 칩
function GoldChip({ amount }) {
  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-accent px-2.5 py-1 text-[12px] font-bold text-amber-dark">
      <span>🪙</span>
      {amount == null ? '—' : amount.toLocaleString()} G
    </span>
  )
}

export default function TodoHomePage() {
  const navigate = useNavigate()

  const [todos, setTodos] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [busyId, setBusyId] = useState(null)

  // 헤더 골드 — 초기 로드는 /my/profile, 완료 시 completion 응답의 totalGold로 갱신
  const [totalGold, setTotalGold] = useState(null)
  const [toast, setToast] = useState('')

  // 소프트딜리트(CANCELLED)는 숨김
  const visibleTodos = todos.filter((t) => t.status !== 'CANCELLED')
  const doneCount = visibleTodos.filter((t) => t.status === 'COMPLETED').length
  const totalCount = visibleTodos.length
  const percent = totalCount > 0 ? Math.round((doneCount / totalCount) * 100) : 0

  useEffect(() => {
    getTodayTodos()
      .then((data) => setTodos(data ?? []))
      .catch(() => setError(true))
      .finally(() => setLoading(false))

    // 헤더 누적 골드 초기값
    getMyProfile()
      .then((p) => setTotalGold(p.totalGold))
      .catch(() => {})
  }, [])

  // 토스트 자동 사라짐
  const showToast = (msg) => {
    setToast(msg)
    setTimeout(() => setToast(''), 2000)
  }

  // 일반 완료
  const handleComplete = async (todo) => {
    setBusyId(todo.id)
    try {
      const result = await completeTodo(todo.id)
      setTodos((prev) =>
        prev.map((t) =>
          t.id === todo.id ? { ...t, status: 'COMPLETED' } : t,
        ),
      )
      setTotalGold(result.totalGold)
      showToast(
        result.earnedGold > 0
          ? `+${result.earnedGold} G 획득!`
          : '오늘 골드 한도를 모두 채웠어요',
      )
    } catch (err) {
      showToast(err.response?.data?.msg ?? '완료 처리에 실패했어요')
    } finally {
      setBusyId(null)
    }
  }

  // 사진 인증 화면으로 이동 (투두 정보 전달)
  const handleCertify = (todo) =>
    navigate(`/todos/${todo.id}/certify`, { state: { todo } })

  // 편집 화면으로 이동 (투두 정보 전달)
  const handleEdit = (todo) =>
    navigate(`/todos/${todo.id}/edit`, { state: { todo } })

  return (
    <div className="flex min-h-screen justify-center bg-background">
      <div className="relative flex min-h-screen w-full max-w-[375px] flex-col bg-background">
        {/* 헤더 */}
        <header className="shrink-0 bg-primary px-5 pb-5 pt-6">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-[12px] text-green-medium">안녕하세요</p>
              <h1 className="mt-0.5 text-[20px] font-bold text-white">
                오늘의 퀘스트
              </h1>
            </div>
            <GoldChip amount={totalGold} />
          </div>

          {/* 진행률 바 */}
          <div className="mt-4">
            <div className="mb-1 flex items-center justify-between text-[11px] text-green-light">
              <span>오늘 진행률</span>
              <span>
                {doneCount}/{totalCount} 완료
              </span>
            </div>
            <div className="h-2 w-full overflow-hidden rounded-full bg-white/25">
              <div
                className="h-full rounded-full bg-accent transition-all"
                style={{ width: `${percent}%` }}
              />
            </div>
          </div>
        </header>

        {/* 본문 */}
        <main className="flex-grow space-y-3 overflow-y-auto px-5 py-5 pb-24">
          {loading ? (
            <p className="py-20 text-center text-[13px] text-text-muted">
              불러오는 중...
            </p>
          ) : error ? (
            <p className="py-20 text-center text-[13px] text-error">
              목록을 불러오지 못했어요
            </p>
          ) : totalCount === 0 ? (
            <div className="flex flex-col items-center justify-center gap-2 py-20 text-center">
              <span className="text-[40px]">🌱</span>
              <p className="text-[14px] font-semibold text-text-sub">
                아직 오늘의 퀘스트가 없어요
              </p>
              <p className="text-[12px] text-text-muted">
                아래 + 버튼으로 첫 퀘스트를 추가해보세요
              </p>
            </div>
          ) : (
            visibleTodos.map((todo) => (
              <TodoCard
                key={todo.id}
                todo={todo}
                busy={busyId === todo.id}
                onComplete={handleComplete}
                onCertify={handleCertify}
                onEdit={handleEdit}
              />
            ))
          )}
        </main>

        {/* 토스트 */}
        {toast && (
          <div className="pointer-events-none absolute bottom-32 left-1/2 z-30 -translate-x-1/2 whitespace-nowrap rounded-full bg-text px-4 py-2 text-[13px] font-semibold text-white shadow-lg">
            {toast}
          </div>
        )}

        {/* FAB — 퀘스트 추가 */}
        <button
          type="button"
          onClick={() => navigate('/todos/new')}
          aria-label="퀘스트 추가"
          className="absolute bottom-20 right-5 z-20 flex h-14 w-14 items-center justify-center rounded-full bg-primary text-[28px] leading-none text-white shadow-lg transition active:scale-95"
        >
          +
        </button>

        <BottomNav />
      </div>
    </div>
  )
}
