import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomNav from '../../components/common/BottomNav'
import BottomSheet from '../../components/common/BottomSheet'
import { getShopVillagers, buyVillager } from '../../api/village'
import { getMyProfile } from '../../api/mypage'

// 주민 등급 스타일 (DESIGN.md gradeStyles)
const gradeStyles = {
  COMMON: { bg: '#F1EFE8', text: '#5F5E5A', label: '일반' },
  RARE: { bg: '#E6F1FB', text: '#185FA5', label: '희귀' },
  EPIC: { bg: '#EEEDFE', text: '#534AB7', label: '에픽' },
  LEGENDARY: { bg: '#FAEEDA', text: '#854F0B', label: '전설' },
}

const FILTERS = [
  { key: 'ALL', label: '전체' },
  { key: 'COMMON', label: '일반' },
  { key: 'RARE', label: '희귀' },
  { key: 'EPIC', label: '에픽' },
  { key: 'LEGENDARY', label: '전설' },
]

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

function GoldChip({ amount }) {
  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-accent px-2.5 py-1 text-[12px] font-bold text-amber-dark">
      <span>🪙</span>
      {amount == null ? '—' : amount.toLocaleString()} G
    </span>
  )
}

function GradeTag({ grade }) {
  const g = gradeStyles[grade] ?? gradeStyles.COMMON
  return (
    <span
      className="inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-bold"
      style={{ backgroundColor: g.bg, color: g.text }}
    >
      {g.label}
    </span>
  )
}

export default function VillageShopPage() {
  const navigate = useNavigate()

  const [filter, setFilter] = useState('ALL')
  const [villagers, setVillagers] = useState([])
  const [totalGold, setTotalGold] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  const [selected, setSelected] = useState(null) // 구매 확인 대상
  const [buying, setBuying] = useState(false)
  const [toast, setToast] = useState('')

  // 골드 조회
  useEffect(() => {
    getMyProfile()
      .then((p) => setTotalGold(p.totalGold))
      .catch(() => {})
  }, [])

  // 등급 필터별 목록 조회
  useEffect(() => {
    setLoading(true)
    setError(false)
    getShopVillagers(filter === 'ALL' ? undefined : filter)
      .then((data) => setVillagers(data ?? []))
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [filter])

  const showToast = (msg) => {
    setToast(msg)
    setTimeout(() => setToast(''), 1800)
  }

  const canAfford = (price) => totalGold != null && totalGold >= price

  const handleBuy = async () => {
    if (!selected || buying) return
    setBuying(true)
    try {
      await buyVillager(selected.id)
      const profile = await getMyProfile()
      setTotalGold(profile.totalGold)
      showToast(`${selected.name}를 영입했어요!`)
      setSelected(null)
    } catch (err) {
      showToast(err.response?.data?.msg ?? '영입에 실패했어요')
    } finally {
      setBuying(false)
    }
  }

  return (
    <div className="flex min-h-screen justify-center bg-background">
      <div className="relative flex min-h-screen w-full max-w-[375px] flex-col bg-background">
        {/* 헤더 */}
        <header className="flex h-16 shrink-0 items-center justify-between bg-primary px-4">
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => navigate(-1)}
              aria-label="뒤로"
              className="text-white"
            >
              <BackIcon />
            </button>
            <h1 className="text-[16px] font-bold text-white">주민 상점</h1>
          </div>
          <GoldChip amount={totalGold} />
        </header>

        {/* 등급 필터 */}
        <div className="shrink-0 border-b border-border-base bg-white px-4 py-2.5">
          <div className="flex gap-2 overflow-x-auto">
            {FILTERS.map((f) => {
              const active = filter === f.key
              return (
                <button
                  key={f.key}
                  type="button"
                  onClick={() => setFilter(f.key)}
                  className={`shrink-0 rounded-full border px-3.5 py-1.5 text-[13px] font-bold transition ${
                    active
                      ? 'border-primary bg-primary text-white'
                      : 'border-border-base bg-white text-text-sub'
                  }`}
                >
                  {f.label}
                </button>
              )
            })}
          </div>
        </div>

        {/* 목록 */}
        <main className="flex-grow overflow-y-auto px-4 py-4 pb-24">
          {loading ? (
            <p className="py-20 text-center text-[13px] text-text-muted">
              불러오는 중...
            </p>
          ) : error ? (
            <p className="py-20 text-center text-[13px] text-error">
              목록을 불러오지 못했어요
            </p>
          ) : villagers.length === 0 ? (
            <p className="py-20 text-center text-[13px] text-text-muted">
              해당 등급의 주민이 없어요
            </p>
          ) : (
            <div className="grid grid-cols-2 gap-3">
              {villagers.map((v) => {
                const afford = canAfford(v.price)
                return (
                  <div
                    key={v.id}
                    className="flex flex-col items-center rounded-2xl border border-border-base bg-white p-4"
                  >
                    <span className="text-[40px]">{v.imageUrl}</span>
                    <span className="mt-1 text-[14px] font-bold text-text">
                      {v.name}
                    </span>
                    <div className="mt-1">
                      <GradeTag grade={v.grade} />
                    </div>
                    <span className="mt-2 text-[14px] font-bold text-green-dark">
                      🪙 {v.price.toLocaleString()} G
                    </span>
                    <button
                      type="button"
                      onClick={() => afford && setSelected(v)}
                      disabled={!afford}
                      className={`mt-3 w-full rounded-xl border py-2 text-[13px] font-bold transition ${
                        afford
                          ? 'border-primary bg-white text-primary active:scale-[0.98]'
                          : 'cursor-not-allowed border-border-base bg-background text-text-muted'
                      }`}
                    >
                      {afford ? '영입하기' : '골드 부족'}
                    </button>
                  </div>
                )
              })}
            </div>
          )}
        </main>

        {/* 토스트 */}
        {toast && (
          <div className="pointer-events-none absolute bottom-24 left-1/2 z-40 -translate-x-1/2 whitespace-nowrap rounded-full bg-text px-4 py-2 text-[13px] font-semibold text-white shadow-lg">
            {toast}
          </div>
        )}

        {/* 구매 확인 바텀시트 */}
        <BottomSheet
          open={!!selected}
          onClose={() => !buying && setSelected(null)}
          title=""
        >
          {selected && (
            <div className="flex flex-col items-center">
              <p className="mb-3 text-[16px] font-bold text-text">
                주민을 영입할까요?
              </p>
              <span className="text-[44px]">{selected.imageUrl}</span>
              <span className="mt-1 text-[15px] font-bold text-text">
                {selected.name}
              </span>
              <div className="mt-1">
                <GradeTag grade={selected.grade} />
              </div>

              <div className="mt-4 flex w-full items-center justify-between rounded-xl bg-background px-4 py-3">
                <span className="text-[13px] text-text-sub">보유 골드</span>
                <span className="text-[14px] font-bold text-text">
                  {totalGold?.toLocaleString() ?? '—'} G →{' '}
                  <span className="text-primary">
                    {totalGold != null
                      ? (totalGold - selected.price).toLocaleString()
                      : '—'}{' '}
                    G
                  </span>
                </span>
              </div>

              <div className="mt-4 flex w-full gap-2.5">
                <button
                  type="button"
                  onClick={() => setSelected(null)}
                  disabled={buying}
                  className="h-12 flex-1 rounded-xl border border-border-base bg-white text-[14px] font-bold text-text-sub"
                >
                  취소
                </button>
                <button
                  type="button"
                  onClick={handleBuy}
                  disabled={buying}
                  className="h-12 flex-[1.6] rounded-xl bg-primary text-[14px] font-bold text-white shadow-lg transition active:scale-[0.98] disabled:opacity-60"
                >
                  {buying
                    ? '영입 중...'
                    : `영입하기 (-${selected.price.toLocaleString()} G)`}
                </button>
              </div>
            </div>
          )}
        </BottomSheet>

        <BottomNav />
      </div>
    </div>
  )
}
