import { useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import BottomNav from '../../components/common/BottomNav'
import RepeatEditor, {
  isRepeatValid,
  buildRepeatConfig,
} from '../../components/todo/RepeatEditor'
import { createRepeat, updateRepeat, deleteRepeat } from '../../api/todo'

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

// 반복 설정 GET이 없어 기존 값 프리로드 불가 → 기본값에서 시작 (매일)
const initialRepeat = {
  type: 'DAILY',
  weekDays: [],
  monthDays: [],
  endDate: null,
}

export default function RepeatSettingPage() {
  const navigate = useNavigate()
  const { todoId } = useParams()
  const { state } = useLocation()

  // 편집 진입 시 isRepeat 여부를 넘겨받으면 신규/수정 구분에 사용
  const alreadyRepeat = state?.isRepeat ?? false

  const [repeat, setRepeat] = useState(initialRepeat)
  const [saving, setSaving] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  const canSave = isRepeatValid(repeat)

  const handleSave = async () => {
    if (!canSave || saving) return
    setSaving(true)
    setErrorMsg('')
    const config = buildRepeatConfig(repeat)
    try {
      // 이미 반복이면 수정, 아니면 등록. GET이 없어 확실치 않으므로 실패 시 상호 폴백
      if (alreadyRepeat) {
        await updateRepeat(todoId, config).catch(async (err) => {
          if (err.response?.data?.resultCode?.startsWith('404'))
            return createRepeat(todoId, config)
          throw err
        })
      } else {
        await createRepeat(todoId, config).catch(async (err) => {
          if (err.response?.data?.resultCode === '409-2')
            return updateRepeat(todoId, config)
          throw err
        })
      }
      navigate(-1)
    } catch (err) {
      setErrorMsg(err.response?.data?.msg ?? '반복 설정에 실패했어요')
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    setSaving(true)
    setErrorMsg('')
    try {
      await deleteRepeat(todoId)
      navigate(-1)
    } catch (err) {
      setErrorMsg(err.response?.data?.msg ?? '반복 해제에 실패했어요')
      setSaving(false)
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
            <h1 className="text-[16px] font-bold text-white">반복 설정</h1>
          </div>
        </header>

        {/* 본문 */}
        <main className="flex-grow overflow-y-auto px-5 py-6 pb-40">
          <RepeatEditor repeat={repeat} onChange={setRepeat} />

          {alreadyRepeat && (
            <button
              type="button"
              onClick={handleDelete}
              disabled={saving}
              className="mt-6 w-full text-center text-[13px] font-semibold text-error"
            >
              반복 해제
            </button>
          )}

          {errorMsg && (
            <p className="mt-4 text-center text-[12px] font-semibold text-error">
              {errorMsg}
            </p>
          )}
        </main>

        {/* 하단 CTA */}
        <footer className="absolute bottom-14 w-full bg-background/80 px-5 py-4 backdrop-blur-sm">
          <button
            type="button"
            onClick={handleSave}
            disabled={!canSave || saving}
            className={`flex h-14 w-full items-center justify-center rounded-xl text-[15px] font-bold transition active:scale-[0.98] ${
              canSave && !saving
                ? 'bg-primary text-white shadow-lg'
                : 'cursor-not-allowed bg-border-base text-text-sub'
            }`}
          >
            {saving ? '저장 중...' : '반복 설정 완료'}
          </button>
        </footer>

        <BottomNav />
      </div>
    </div>
  )
}
