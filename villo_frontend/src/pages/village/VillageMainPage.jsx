import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomNav from '../../components/common/BottomNav'
import BottomSheet from '../../components/common/BottomSheet'
import VillageMap from '../../components/village/VillageMap'
import VillagerSprite from '../../components/village/VillagerSprite'
import {
  getMyVillage,
  getPlacements,
  getMyVillagers,
  createPlacement,
  updatePlacement,
  deletePlacement,
  updateVillageName,
} from '../../api/village'
import { getMyProfile } from '../../api/mypage'
import { pickDialogue } from '../../data/villagerDialogues'

// 골드 칩
function GoldChip({ amount }) {
  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-accent px-2.5 py-1 text-[12px] font-bold text-amber-dark">
      <span>🪙</span>
      {amount == null ? '—' : amount.toLocaleString()} G
    </span>
  )
}

export default function VillageMainPage() {
  const navigate = useNavigate()
  const [village, setVillage] = useState(null)
  const [placements, setPlacements] = useState([])
  const [myVillagers, setMyVillagers] = useState([])
  const [totalGold, setTotalGold] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  const [editMode, setEditMode] = useState(false)
  const [selectedVillagerId, setSelectedVillagerId] = useState(null) // 패널에서 고른 미배치 주민
  const [selectedPlacementId, setSelectedPlacementId] = useState(null) // 지도에서 고른 배치(이동/해제용)
  const [busy, setBusy] = useState(false)
  const [toast, setToast] = useState('')
  const [celebrateLevel, setCelebrateLevel] = useState(null) // 레벨업 축하 연출
  const [bubble, setBubble] = useState(null) // 배치 주민 탭 시 대화 말풍선
  const bubbleTimerRef = useRef(null)

  // 마을 이름 변경 시트
  const [nameSheet, setNameSheet] = useState(false)
  const [newName, setNewName] = useState('')
  const [nameSaving, setNameSaving] = useState(false)
  const [nameError, setNameError] = useState('')

  // 레벨업 감지 — 직전 방문 레벨과 비교 (상점에서 영입 후 돌아오면 축하)
  const detectLevelUp = (level) => {
    const key = 'villo_village_level'
    const prev = Number(localStorage.getItem(key))
    if (prev && level > prev) {
      setCelebrateLevel(level)
      setTimeout(() => setCelebrateLevel(null), 2800)
    }
    localStorage.setItem(key, String(level))
  }

  useEffect(() => {
    Promise.all([getMyVillage(), getPlacements(), getMyVillagers()])
      .then(([v, p, mv]) => {
        setVillage(v)
        setPlacements(p ?? [])
        setMyVillagers(mv ?? [])
        detectLevelUp(v.villageLevel)
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false))

    getMyProfile()
      .then((prof) => setTotalGold(prof.totalGold))
      .catch(() => {})
  }, [])

  // 타이머 정리
  useEffect(
    () => () => {
      if (bubbleTimerRef.current) clearTimeout(bubbleTimerRef.current)
    },
    [],
  )

  // 편집 모드 진입 시 말풍선 닫기
  useEffect(() => {
    if (editMode) {
      setBubble(null)
      if (bubbleTimerRef.current) clearTimeout(bubbleTimerRef.current)
    }
  }, [editMode])

  // 배치된 주민 탭 → 대화 말풍선 3초 (다른/같은 주민 탭 시 갱신·리셋)
  const handleVillagerTap = (p) => {
    const text = pickDialogue(p.villagerImageUrl)
    setBubble({
      key: Date.now(),
      id: p.id,
      gridX: p.gridX,
      gridY: p.gridY,
      name: p.villagerName,
      text,
    })
    if (bubbleTimerRef.current) clearTimeout(bubbleTimerRef.current)
    bubbleTimerRef.current = setTimeout(() => setBubble(null), 3000)
  }

  const showToast = (msg) => {
    setToast(msg)
    setTimeout(() => setToast(''), 1800)
  }

  const refresh = async () => {
    const [p, mv] = await Promise.all([getPlacements(), getMyVillagers()])
    setPlacements(p ?? [])
    setMyVillagers(mv ?? [])
  }

  const clearSelection = () => {
    setSelectedVillagerId(null)
    setSelectedPlacementId(null)
  }

  const exitEdit = () => {
    setEditMode(false)
    clearSelection()
  }

  // 마을 이름 변경 시트 열기
  const openNameSheet = () => {
    if (!village) return
    setNewName(village.villageName)
    setNameError('')
    setNameSheet(true)
  }

  const trimmedName = newName.trim()
  const canSaveName =
    !nameSaving &&
    trimmedName.length > 0 &&
    trimmedName.length <= 100 &&
    trimmedName !== village?.villageName

  const handleSaveName = async () => {
    if (!canSaveName) return
    setNameSaving(true)
    setNameError('')
    try {
      const updated = await updateVillageName(trimmedName)
      setVillage(updated)
      setNameSheet(false)
      showToast('마을 이름을 바꿨어요')
    } catch (err) {
      setNameError(err.response?.data?.msg ?? '변경에 실패했어요')
    } finally {
      setNameSaving(false)
    }
  }

  // 레벨 진행률 — API의 villagerCount/nextLevelThreshold만 사용 (하드코딩 금지)
  const isMaxLevel = village && village.nextLevelThreshold == null
  const levelPercent =
    village && village.nextLevelThreshold
      ? Math.min(100, Math.round((village.villagerCount / village.nextLevelThreshold) * 100))
      : 100

  // 패널 주민 선택 (미배치만)
  const handleSelectVillager = (uv) => {
    if (uv.isPlaced || busy) return
    setSelectedPlacementId(null)
    setSelectedVillagerId((prev) =>
      prev === uv.userVillagePeopleId ? null : uv.userVillagePeopleId,
    )
  }

  // 칸 탭
  const handleTileTap = async (x, y) => {
    if (busy) return
    const occupied = placements.find((p) => p.gridX === x && p.gridY === y)

    // 1) 패널 주민 배치
    if (selectedVillagerId != null) {
      if (occupied) return showToast('이미 주민이 있는 칸이에요')
      const uv = myVillagers.find((m) => m.userVillagePeopleId === selectedVillagerId)
      setBusy(true)
      try {
        await createPlacement({ userVillagePeopleId: selectedVillagerId, gridX: x, gridY: y })
        await refresh()
        showToast(`${uv?.name ?? '주민'}를 배치했어요!`)
        clearSelection()
      } catch (err) {
        showToast(err.response?.data?.msg ?? '배치에 실패했어요')
      } finally {
        setBusy(false)
      }
      return
    }

    // 2) 지도 배치 이동/해제
    if (selectedPlacementId != null) {
      if (occupied) {
        if (occupied.id === selectedPlacementId) {
          // 같은 칸 다시 탭 → 해제
          setBusy(true)
          try {
            await deletePlacement(selectedPlacementId)
            await refresh()
            showToast('배치를 해제했어요')
            clearSelection()
          } catch (err) {
            showToast(err.response?.data?.msg ?? '해제에 실패했어요')
          } finally {
            setBusy(false)
          }
        } else {
          showToast('이미 주민이 있는 칸이에요')
        }
        return
      }
      const pl = placements.find((p) => p.id === selectedPlacementId)
      setBusy(true)
      try {
        await updatePlacement(selectedPlacementId, {
          userVillagePeopleId: pl.userVillagePeopleId,
          gridX: x,
          gridY: y,
        })
        await refresh()
        showToast('위치를 옮겼어요')
        clearSelection()
      } catch (err) {
        showToast(err.response?.data?.msg ?? '이동에 실패했어요')
      } finally {
        setBusy(false)
      }
      return
    }

    // 3) 아무 것도 선택 안 함 → 배치된 주민 선택(이동 준비)
    if (occupied) setSelectedPlacementId(occupied.id)
  }

  const selectedPlacement = placements.find((p) => p.id === selectedPlacementId)
  const selectedCell = selectedPlacement
    ? { x: selectedPlacement.gridX, y: selectedPlacement.gridY }
    : null

  return (
    <div className="flex min-h-screen justify-center bg-background">
      <div className="relative flex min-h-screen w-full max-w-[375px] flex-col bg-background">
        {/* 헤더 */}
        <header className="shrink-0 bg-primary px-5 pb-4 pt-6">
          <div className="flex items-start justify-between">
            <div className="min-w-0">
              {editMode ? (
                <h1 className="truncate text-[18px] font-bold text-white">
                  {village?.villageName ?? '나의 마을'}
                </h1>
              ) : (
                <button
                  type="button"
                  onClick={openNameSheet}
                  disabled={!village}
                  className="flex max-w-full items-center gap-1"
                >
                  <span className="truncate text-[18px] font-bold text-white">
                    {village?.villageName ?? '나의 마을'}
                  </span>
                  <span className="text-[12px] opacity-90">✏️</span>
                </button>
              )}
              <p className="mt-0.5 text-[12px] text-green-medium">
                {editMode
                  ? '배치 편집 중'
                  : `Lv.${village?.villageLevel ?? 1} · 주민 ${village?.villagerCount ?? 0}명`}
              </p>
            </div>
            {editMode ? (
              <button
                type="button"
                onClick={exitEdit}
                className="rounded-full bg-white px-3.5 py-1.5 text-[12px] font-bold text-primary shadow-sm"
              >
                완료
              </button>
            ) : (
              <GoldChip amount={totalGold} />
            )}
          </div>

          {/* 레벨 진행바 */}
          <div className="mt-3">
            <div className="mb-1 flex items-center justify-between text-[11px] text-green-light">
              <span>마을 레벨</span>
              <span>
                {isMaxLevel
                  ? '최고 레벨'
                  : `${village?.villagerCount ?? 0}/${village?.nextLevelThreshold ?? '—'}명`}
              </span>
            </div>
            <div className="h-2 w-full overflow-hidden rounded-full bg-white/25">
              <div
                className="h-full rounded-full bg-accent transition-all"
                style={{ width: `${levelPercent}%` }}
              />
            </div>
          </div>
        </header>

        {/* 모드바 */}
        <div className="flex shrink-0 items-center justify-between bg-[#E4E2DC] px-5 py-2.5">
          <span className="text-[12px] text-text-sub">
            {editMode
              ? selectedVillagerId != null
                ? '배치할 칸을 탭하세요'
                : selectedPlacementId != null
                  ? '옮길 칸을 탭 (같은 칸 다시 탭 = 해제)'
                  : '주민을 선택하거나 배치된 주민을 탭하세요'
              : '주민을 눌러 말을 걸어보세요'}
          </span>
          {!editMode && (
            <button
              type="button"
              onClick={() => setEditMode(true)}
              className="rounded-lg border border-border-base bg-white px-3 py-1.5 text-[12px] font-bold text-text"
            >
              🛠️ 배치 편집
            </button>
          )}
        </div>

        {/* 마을 뷰 */}
        <main className="relative flex-grow overflow-hidden bg-[#7CB342]">
          {loading ? (
            <p className="py-24 text-center text-[13px] text-white/90">
              마을을 불러오는 중...
            </p>
          ) : error ? (
            <p className="py-24 text-center text-[13px] text-white">
              마을을 불러오지 못했어요
            </p>
          ) : (
            <div
              className={`flex h-full items-center justify-center px-3 py-4 ${
                celebrateLevel ? 'village-levelup' : ''
              }`}
            >
              <VillageMap
                gridSize={village?.gridSize ?? 5}
                placements={placements}
                editMode={editMode}
                selectedCell={selectedCell}
                onTileTap={handleTileTap}
                onVillagerTap={editMode ? undefined : handleVillagerTap}
                bubble={editMode ? null : bubble}
              />
            </div>
          )}

          {/* 레벨업 축하 연출 */}
          {celebrateLevel && (
            <div className="pointer-events-none absolute inset-0 z-30 flex items-center justify-center">
              {['✨', '🎉', '⭐', '✨', '🎊', '⭐'].map((s, i) => (
                <span
                  key={i}
                  className="village-sparkle absolute text-[26px]"
                  style={{
                    left: `${15 + i * 13}%`,
                    top: `${30 + (i % 3) * 16}%`,
                    animationDelay: `${i * 0.15}s`,
                  }}
                >
                  {s}
                </span>
              ))}
              <div className="rounded-2xl bg-primary/90 px-5 py-3 text-center shadow-xl">
                <p className="text-[15px] font-extrabold text-white">
                  🎉 마을이 커졌어요!
                </p>
                <p className="mt-0.5 text-[12px] font-semibold text-green-light">
                  Lv.{celebrateLevel} 달성 · 더 넓어진 마을
                </p>
              </div>
            </div>
          )}

          {/* 토스트 */}
          {toast && (
            <div className="pointer-events-none absolute left-1/2 top-4 z-20 -translate-x-1/2 whitespace-nowrap rounded-full bg-text px-4 py-2 text-[13px] font-semibold text-white shadow-lg">
              {toast}
            </div>
          )}

          {/* 일반 모드 안내 pill */}
          {!editMode && !loading && !error && (
            <div className="pointer-events-none absolute inset-x-0 bottom-[68px] flex justify-center">
              <span className="rounded-full bg-primary/80 px-3 py-1.5 text-[11px] font-semibold text-white">
                🐾 주민을 눌러 인사해보세요
              </span>
            </div>
          )}

          {/* 주민 상점 진입 (일반 모드) */}
          {!editMode && (
            <button
              type="button"
              onClick={() => navigate('/village/shop')}
              aria-label="주민 상점"
              className="absolute bottom-[68px] right-4 flex h-12 w-12 items-center justify-center rounded-full bg-white text-[22px] shadow-lg transition active:scale-95"
            >
              🛒
            </button>
          )}
        </main>

        {/* 편집 모드 — 보유 주민 패널 */}
        {editMode && (
          <>
            <section className="shrink-0 border-t border-border-base bg-white px-4 py-3">
              <p className="mb-2 text-[13px] font-bold text-text-sub">
                보유 주민 {myVillagers.length}명
              </p>
              {myVillagers.length === 0 ? (
                <p className="py-3 text-center text-[12px] text-text-muted">
                  아직 주민이 없어요 — 주민 상점에서 영입해보세요
                </p>
              ) : (
                <div className="flex gap-2 overflow-x-auto pb-1">
                  {myVillagers.map((uv) => {
                    const selected = selectedVillagerId === uv.userVillagePeopleId
                    return (
                      <button
                        key={uv.userVillagePeopleId}
                        type="button"
                        onClick={() => handleSelectVillager(uv)}
                        disabled={uv.isPlaced}
                        className={`flex min-w-[68px] shrink-0 flex-col items-center gap-0.5 rounded-xl border px-3 py-2 transition ${
                          selected
                            ? 'border-primary bg-green-light'
                            : uv.isPlaced
                              ? 'border-border-base bg-background opacity-50'
                              : 'border-border-base bg-white'
                        }`}
                      >
                        <VillagerSprite emoji={uv.imageUrl} size={40} />
                        <span className="text-[12px] font-bold text-text">
                          {uv.name}
                        </span>
                        <span
                          className={`text-[10px] font-semibold ${
                            selected ? 'text-primary' : 'text-text-muted'
                          }`}
                        >
                          {uv.isPlaced ? '배치됨' : selected ? '선택됨' : '미배치'}
                        </span>
                      </button>
                    )
                  })}
                </div>
              )}
            </section>
            {/* 바텀 네비 공간 확보 */}
            <div className="h-14 shrink-0" />
          </>
        )}

        {/* 마을 이름 변경 바텀시트 */}
        <BottomSheet
          open={nameSheet}
          onClose={() => !nameSaving && setNameSheet(false)}
          title="마을 이름 변경"
        >
          <input
            type="text"
            value={newName}
            onChange={(e) => {
              setNewName(e.target.value)
              setNameError('')
            }}
            placeholder="마을 이름을 입력해주세요"
            maxLength={100}
            autoFocus
            className={`h-12 w-full rounded-xl border-2 bg-white px-4 text-[15px] text-text outline-none transition-colors placeholder:text-text-muted ${
              nameError ? 'border-error' : 'border-border-base focus:border-primary'
            }`}
          />
          {nameError && (
            <p className="mt-1.5 text-[12px] font-semibold text-error">
              {nameError}
            </p>
          )}
          <button
            type="button"
            onClick={handleSaveName}
            disabled={!canSaveName}
            className={`mt-4 h-12 w-full rounded-xl text-[15px] font-bold transition active:scale-[0.98] ${
              canSaveName
                ? 'bg-primary text-white shadow-lg'
                : 'cursor-not-allowed bg-border-base text-text-sub'
            }`}
          >
            {nameSaving ? '변경 중...' : '변경하기'}
          </button>
        </BottomSheet>

        <BottomNav />
      </div>
    </div>
  )
}
