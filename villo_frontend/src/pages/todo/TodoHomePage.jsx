import { useNavigate } from 'react-router-dom'
import BottomNav from '../../components/common/BottomNav'

// TODO: GET /api/v1/todos 연동 후 실제 데이터로 교체 (현재는 틀만)
const MOCK = {
  nickname: '',
  totalGold: 0,
  done: 0,
  total: 0,
}

// 골드 칩
function GoldChip({ amount }) {
  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-accent px-2.5 py-1 text-[12px] font-bold text-amber-dark">
      <span>🪙</span>
      {amount.toLocaleString()} G
    </span>
  )
}

export default function TodoHomePage() {
  const navigate = useNavigate()
  const { nickname, totalGold, done, total } = MOCK
  const percent = total > 0 ? Math.round((done / total) * 100) : 0

  return (
    <div className="flex min-h-screen justify-center bg-background">
      <div className="relative flex min-h-screen w-full max-w-[375px] flex-col bg-background">
        {/* 헤더 */}
        <header className="shrink-0 bg-primary px-5 pb-5 pt-6">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-[12px] text-green-medium">
                {nickname ? `${nickname}님, 안녕하세요` : '안녕하세요'}
              </p>
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
                {done}/{total} 완료
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
          {total === 0 ? (
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
            // TODO: 투두 카드 목록 렌더링
            null
          )}
        </main>

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
