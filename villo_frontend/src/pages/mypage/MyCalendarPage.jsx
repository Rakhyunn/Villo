import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getMyCalendar, getMyDailyTodos } from '../../api/mypage'
import { categoryStyle } from '../../components/todo/TodoTag'

function BackIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="m15 18-6-6 6-6"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

const WEEK = ['일', '월', '화', '수', '목', '금', '토']
const pad = (n) => String(n).padStart(2, '0')
const toDateStr = (y, m, d) => `${y}-${pad(m)}-${pad(d)}`
const today = new Date()

export default function MyCalendarPage() {
  const navigate = useNavigate()

  const [year, setYear] = useState(today.getFullYear())
  const [month, setMonth] = useState(today.getMonth() + 1) // 1~12
  const [completedSet, setCompletedSet] = useState(new Set())

  const [selected, setSelected] = useState(null) // "YYYY-MM-DD"
  const [dailyTodos, setDailyTodos] = useState([])
  const [dailyLoading, setDailyLoading] = useState(false)

  const [lightbox, setLightbox] = useState(null) // 확대 이미지 URL

  // 월별 완료 날짜 조회
  useEffect(() => {
    getMyCalendar(year, month)
      .then((data) => setCompletedSet(new Set(data?.completedDates ?? [])))
      .catch(() => setCompletedSet(new Set()))
    setSelected(null)
    setDailyTodos([])
  }, [year, month])

  // 날짜 선택 → 완료 투두 조회
  const handleSelect = (dateStr) => {
    setSelected(dateStr)
    setDailyLoading(true)
    getMyDailyTodos(dateStr)
      .then((data) => setDailyTodos(data ?? []))
      .catch(() => setDailyTodos([]))
      .finally(() => setDailyLoading(false))
  }

  const goMonth = (delta) => {
    let m = month + delta
    let y = year
    if (m < 1) {
      m = 12
      y -= 1
    } else if (m > 12) {
      m = 1
      y += 1
    }
    setYear(y)
    setMonth(m)
  }

  // 달력 셀 구성
  const firstWeekday = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()
  const cells = [
    ...Array(firstWeekday).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ]

  const isToday = (d) =>
    year === today.getFullYear() &&
    month === today.getMonth() + 1 &&
    d === today.getDate()

  return (
    <div className="flex min-h-screen justify-center bg-background">
      <div className="relative flex min-h-screen w-full max-w-[375px] flex-col bg-background">
        {/* 헤더 */}
        <header className="flex h-16 shrink-0 items-center gap-2 bg-primary px-4">
          <button
            type="button"
            onClick={() => navigate(-1)}
            aria-label="뒤로"
            className="text-white"
          >
            <BackIcon />
          </button>
          <h1 className="text-[16px] font-bold text-white">완료 기록</h1>
        </header>

        <main className="flex-grow overflow-y-auto px-5 py-5">
          {/* 월 이동 */}
          <div className="flex items-center justify-between px-1">
            <button
              type="button"
              onClick={() => goMonth(-1)}
              aria-label="이전 달"
              className="flex h-8 w-8 items-center justify-center rounded-full text-text-sub"
            >
              ‹
            </button>
            <span className="text-[15px] font-bold text-text">
              {year}년 {month}월
            </span>
            <button
              type="button"
              onClick={() => goMonth(1)}
              aria-label="다음 달"
              className="flex h-8 w-8 items-center justify-center rounded-full text-text-sub"
            >
              ›
            </button>
          </div>

          {/* 달력 */}
          <div className="mt-3 rounded-2xl border border-border-base bg-white p-3">
            <div className="grid grid-cols-7">
              {WEEK.map((w, i) => (
                <div
                  key={w}
                  className={`pb-2 text-center text-[11px] font-bold ${
                    i === 0 ? 'text-error' : 'text-text-muted'
                  }`}
                >
                  {w}
                </div>
              ))}
              {cells.map((d, idx) => {
                if (d == null) return <div key={`e${idx}`} />
                const dateStr = toDateStr(year, month, d)
                const done = completedSet.has(dateStr)
                const isSel = selected === dateStr
                return (
                  <button
                    key={dateStr}
                    type="button"
                    onClick={() => handleSelect(dateStr)}
                    className="flex flex-col items-center py-1.5"
                  >
                    <span
                      className={`flex h-8 w-8 items-center justify-center rounded-full text-[13px] font-semibold ${
                        isSel
                          ? 'bg-primary text-white'
                          : isToday(d)
                            ? 'bg-green-light text-green-dark'
                            : 'text-text'
                      }`}
                    >
                      {d}
                    </span>
                    <span
                      className={`mt-0.5 h-1.5 w-1.5 rounded-full ${
                        done && !isSel ? 'bg-accent' : 'bg-transparent'
                      }`}
                    />
                  </button>
                )
              })}
            </div>
          </div>

          {/* 선택 날짜 완료 목록 */}
          <section className="mt-5">
            {!selected ? (
              <p className="py-10 text-center text-[13px] text-text-muted">
                날짜를 선택하면 완료한 퀘스트가 보여요
              </p>
            ) : dailyLoading ? (
              <p className="py-10 text-center text-[13px] text-text-muted">
                불러오는 중...
              </p>
            ) : dailyTodos.length === 0 ? (
              <p className="py-10 text-center text-[13px] text-text-muted">
                이 날 완료한 퀘스트가 없어요
              </p>
            ) : (
              <div className="space-y-3">
                <p className="text-[13px] font-bold text-text-sub">
                  {selected} · {dailyTodos.length}개 완료
                </p>
                {dailyTodos.map((todo) => (
                  <div
                    key={todo.todoId}
                    className="rounded-2xl border border-border-base bg-white p-4"
                  >
                    <div className="flex items-start justify-between gap-2">
                      <p className="text-[14px] font-semibold text-text">
                        {todo.title}
                      </p>
                      {todo.isCertified && (
                        <span className="shrink-0 rounded-full bg-green-light px-2 py-0.5 text-[10px] font-bold text-green-dark">
                          인증
                        </span>
                      )}
                    </div>
                    <div className="mt-2 flex items-center gap-1.5">
                      <span
                        className="rounded-full px-2 py-0.5 text-[11px] font-bold"
                        style={{
                          backgroundColor: categoryStyle.bg,
                          color: categoryStyle.text,
                        }}
                      >
                        {todo.category}
                      </span>
                      <span className="rounded-full bg-green-light px-2 py-0.5 text-[11px] font-bold text-green-dark">
                        +{todo.earnedGold} G
                      </span>
                    </div>

                    {/* 인증 사진 갤러리 */}
                    {todo.imageUrls?.length > 0 && (
                      <div className="mt-3 flex flex-wrap gap-2">
                        {todo.imageUrls.map((url, i) => (
                          <button
                            key={url + i}
                            type="button"
                            onClick={() => setLightbox(url)}
                            className="h-16 w-16 overflow-hidden rounded-lg border border-border-base"
                          >
                            <img
                              src={url}
                              alt={`인증 사진 ${i + 1}`}
                              loading="lazy"
                              className="h-full w-full object-cover"
                            />
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>
        </main>

        {/* 라이트박스 — 이미 로드된 이미지 확대만 (추가 R2 조회 없음) */}
        {lightbox && (
          <div
            onClick={() => setLightbox(null)}
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-6"
          >
            <img
              src={lightbox}
              alt="인증 사진 크게 보기"
              className="max-h-[80vh] max-w-full rounded-lg object-contain"
            />
          </div>
        )}
      </div>
    </div>
  )
}
