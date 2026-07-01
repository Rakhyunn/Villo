import { useRef } from 'react'

// 반복 설정 편집기 — 투두 등록/편집에서 공용 사용
// 백엔드 repeatValue 포맷: WEEKLY "MON,WED" / MONTHLY "1,15,LAST" / DAILY null

// 요일 (표시 일~토, 토큰은 배치 스케줄러 parseDayOfWeek 기준)
const WEEK_DAYS = [
  { token: 'SUN', label: '일' },
  { token: 'MON', label: '월' },
  { token: 'TUE', label: '화' },
  { token: 'WED', label: '수' },
  { token: 'THU', label: '목' },
  { token: 'FRI', label: '금' },
  { token: 'SAT', label: '토' },
]

const MONTH_DAYS = Array.from({ length: 28 }, (_, i) => String(i + 1))

export const defaultRepeat = {
  type: 'NONE', // NONE | DAILY | WEEKLY | MONTHLY
  weekDays: [],
  monthDays: [],
  endDate: null,
}

// 유효성 — WEEKLY는 요일, MONTHLY는 날짜 1개 이상 필수
export const isRepeatValid = (repeat) => {
  if (repeat.type === 'WEEKLY') return repeat.weekDays.length > 0
  if (repeat.type === 'MONTHLY') return repeat.monthDays.length > 0
  return true
}

// 백엔드 RepeatConfigRequest로 변환 (NONE이면 null)
export const buildRepeatConfig = (repeat) => {
  if (repeat.type === 'NONE') return null
  let repeatValue = null
  if (repeat.type === 'WEEKLY')
    repeatValue = WEEK_DAYS.filter((d) => repeat.weekDays.includes(d.token))
      .map((d) => d.token)
      .join(',')
  if (repeat.type === 'MONTHLY')
    repeatValue = repeat.monthDays.join(',')
  return {
    repeatType: repeat.type,
    repeatValue,
    endDate: repeat.endDate,
  }
}

// 미리보기 문구
export const repeatSummaryText = (repeat) => {
  if (repeat.type === 'DAILY') return '매일 마을 회관에 새로운 퀘스트가 도착해요'
  if (repeat.type === 'WEEKLY') {
    if (repeat.weekDays.length === 0) return '반복할 요일을 선택해주세요'
    const days = WEEK_DAYS.filter((d) => repeat.weekDays.includes(d.token))
      .map((d) => d.label)
      .join(', ')
    return `매주 ${days}요일마다 새로운 퀘스트가 도착해요`
  }
  if (repeat.type === 'MONTHLY') {
    if (repeat.monthDays.length === 0) return '반복할 날짜를 선택해주세요'
    const labels = repeat.monthDays.map((d) => (d === 'LAST' ? '말일' : `${d}일`))
    return `매월 ${labels.join(', ')}마다 새로운 퀘스트가 도착해요`
  }
  return ''
}

const TYPE_CHIPS = [
  { type: 'DAILY', label: '매일' },
  { type: 'WEEKLY', label: '매주' },
  { type: 'MONTHLY', label: '매월' },
]

function Chip({ selected, onClick, children, className = '' }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-full border px-3 py-1.5 text-[13px] font-semibold transition ${
        selected
          ? 'border-primary bg-green-light text-green-dark'
          : 'border-border-base bg-background text-text-sub'
      } ${className}`}
    >
      {children}
    </button>
  )
}

export default function RepeatEditor({ repeat, onChange, allowNone = false }) {
  const set = (patch) => onChange({ ...repeat, ...patch })
  const dateRef = useRef(null)

  // 네이티브 달력 열기 (데스크톱 크롬은 입력영역 클릭만으론 안 열림 → showPicker 직접 호출)
  const openDatePicker = () => {
    const el = dateRef.current
    if (!el) return
    if (typeof el.showPicker === 'function') {
      try {
        el.showPicker()
        return
      } catch {
        // showPicker 미지원/실패 시 아래 폴백
      }
    }
    el.focus()
    el.click()
  }

  const toggleIn = (key, token) => {
    const list = repeat[key]
    set({
      [key]: list.includes(token)
        ? list.filter((t) => t !== token)
        : [...list, token],
    })
  }

  return (
    <div className="space-y-6">
      {/* 반복 주기 */}
      <div className="space-y-2">
        <p className="text-[13px] font-bold text-text-sub">반복 주기</p>
        <div className="flex gap-2">
          {allowNone && (
            <Chip
              selected={repeat.type === 'NONE'}
              onClick={() => set({ type: 'NONE' })}
            >
              없음
            </Chip>
          )}
          {TYPE_CHIPS.map(({ type, label }) => (
            <Chip
              key={type}
              selected={repeat.type === type}
              onClick={() => set({ type })}
            >
              {label}
            </Chip>
          ))}
        </div>
      </div>

      {/* 반복 요일 (매주) */}
      {repeat.type === 'WEEKLY' && (
        <div className="space-y-2">
          <p className="text-[13px] font-bold text-text-sub">반복 요일</p>
          <div className="flex justify-between">
            {WEEK_DAYS.map(({ token, label }) => {
              const selected = repeat.weekDays.includes(token)
              return (
                <button
                  key={token}
                  type="button"
                  onClick={() => toggleIn('weekDays', token)}
                  className={`flex h-9 w-9 items-center justify-center rounded-full text-[13px] font-bold transition ${
                    selected
                      ? 'bg-primary text-white'
                      : 'bg-background text-text-sub'
                  }`}
                >
                  {label}
                </button>
              )
            })}
          </div>
        </div>
      )}

      {/* 반복 날짜 (매월) */}
      {repeat.type === 'MONTHLY' && (
        <div className="space-y-2">
          <p className="text-[13px] font-bold text-text-sub">반복 날짜</p>
          <div className="grid grid-cols-7 gap-1.5">
            {MONTH_DAYS.map((d) => {
              const selected = repeat.monthDays.includes(d)
              return (
                <button
                  key={d}
                  type="button"
                  onClick={() => toggleIn('monthDays', d)}
                  className={`flex h-8 items-center justify-center rounded-lg text-[12px] font-bold transition ${
                    selected
                      ? 'bg-primary text-white'
                      : 'bg-background text-text-sub'
                  }`}
                >
                  {d}
                </button>
              )
            })}
            <button
              type="button"
              onClick={() => toggleIn('monthDays', 'LAST')}
              className={`col-span-2 flex h-8 items-center justify-center rounded-lg text-[12px] font-bold transition ${
                repeat.monthDays.includes('LAST')
                  ? 'bg-primary text-white'
                  : 'bg-background text-text-sub'
              }`}
            >
              말일
            </button>
          </div>
        </div>
      )}

      {/* 종료일 (매일/매주/매월 공통) */}
      {repeat.type !== 'NONE' && (
        <div className="space-y-2">
          <p className="text-[13px] font-bold text-text-sub">종료일</p>
          <div className="flex items-center justify-between rounded-xl border border-border-base bg-white px-4 py-3">
            <span className="text-[13px] text-text">
              {repeat.endDate ?? '종료일 없음 (무기한)'}
            </span>
            <div className="flex items-center gap-2">
              {repeat.endDate && (
                <button
                  type="button"
                  onClick={() => set({ endDate: null })}
                  className="text-[12px] font-semibold text-text-muted"
                >
                  해제
                </button>
              )}
              <button
                type="button"
                onClick={openDatePicker}
                className="rounded-lg bg-background px-2.5 py-1 text-[12px] font-bold text-text-sub"
              >
                변경하기
              </button>
              <input
                ref={dateRef}
                type="date"
                min={new Date().toISOString().slice(0, 10)}
                value={repeat.endDate ?? ''}
                onChange={(e) => set({ endDate: e.target.value || null })}
                aria-label="종료일 선택"
                className="sr-only"
                tabIndex={-1}
              />
            </div>
          </div>
        </div>
      )}

      {/* 미리보기 */}
      {repeat.type !== 'NONE' && (
        <div className="flex items-start gap-3 rounded-2xl border border-primary/10 bg-green-light p-4">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary text-[16px]">
            🔁
          </div>
          <div>
            <p className="text-[13px] font-bold text-green-dark">
              반복 퀘스트 생성됨
            </p>
            <p className="mt-0.5 text-[12px] text-text-sub">
              {repeatSummaryText(repeat)}
            </p>
          </div>
        </div>
      )}
    </div>
  )
}
