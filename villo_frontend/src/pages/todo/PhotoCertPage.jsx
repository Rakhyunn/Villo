import { useRef, useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import TodoTag, { difficultyStyles } from '../../components/todo/TodoTag'
import { completeTodo, certifyTodo, uploadImage } from '../../api/todo'

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

const MAX_PHOTOS = 3

export default function PhotoCertPage() {
  const navigate = useNavigate()
  const { todoId } = useParams()
  const { state } = useLocation()
  const fileRef = useRef(null)

  const todo = state?.todo // 홈 카드에서 넘겨받은 투두 정보
  const [photos, setPhotos] = useState([]) // { file, preview }[]
  const [submitting, setSubmitting] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  const diff = todo
    ? (difficultyStyles[todo.difficulty] ?? difficultyStyles.NORMAL)
    : null
  const boostedGold = todo ? Math.floor(todo.gold * 1.3) : 0

  // 사진 추가
  const handleAddFiles = (e) => {
    const files = Array.from(e.target.files ?? [])
    e.target.value = '' // 같은 파일 재선택 허용
    const room = MAX_PHOTOS - photos.length
    const next = files.slice(0, room).map((file) => ({
      file,
      preview: URL.createObjectURL(file),
    }))
    setPhotos((prev) => [...prev, ...next])
  }

  const removePhoto = (idx) => {
    setPhotos((prev) => {
      URL.revokeObjectURL(prev[idx].preview)
      return prev.filter((_, i) => i !== idx)
    })
  }

  // 일반 완료 (사진 없이)
  const handleNormalComplete = async () => {
    if (submitting) return
    setSubmitting(true)
    setErrorMsg('')
    try {
      await completeTodo(todoId)
      navigate('/')
    } catch (err) {
      setErrorMsg(err.response?.data?.msg ?? '완료 처리에 실패했어요')
      setSubmitting(false)
    }
  }

  // 인증 완료 (사진 업로드 → certify)
  const handleCertify = async () => {
    if (submitting) return
    if (photos.length === 0) {
      setErrorMsg('인증 사진을 1장 이상 첨부해주세요')
      return
    }
    setSubmitting(true)
    setErrorMsg('')
    try {
      const imageUrls = await Promise.all(photos.map((p) => uploadImage(p.file)))
      await certifyTodo(todoId, imageUrls)
      navigate('/')
    } catch (err) {
      setErrorMsg(
        err.response?.data?.msg ?? err.message ?? '인증에 실패했어요',
      )
      setSubmitting(false)
    }
  }

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
          <h1 className="text-[16px] font-bold text-white">사진 인증</h1>
        </header>

        <main className="flex-grow overflow-y-auto px-5 py-6 pb-40">
          {/* 투두 정보 카드 */}
          <div className="rounded-2xl border border-border-base bg-white p-4">
            <p className="text-[15px] font-bold text-text">
              {todo?.title ?? '퀘스트'}
            </p>
            <div className="mt-2 flex flex-wrap items-center gap-1.5">
              {todo && (
                <>
                  <TodoTag bg="#E6F1FB" text="#185FA5">
                    {todo.category}
                  </TodoTag>
                  <TodoTag bg={diff.bg} text={diff.text}>
                    {diff.label}
                  </TodoTag>
                  <span className="text-[13px] font-bold text-text-sub">
                    +{todo.gold} G{' '}
                    <span className="text-primary">→ +{boostedGold} G</span>
                  </span>
                </>
              )}
            </div>
          </div>

          {/* 사진 슬롯 */}
          <div className="mt-6">
            <div className="mb-2 flex items-center justify-between">
              <p className="text-[13px] font-bold text-text-sub">
                인증 사진 (최대 3장)
              </p>
              <span className="text-[11px] text-text-muted">
                {photos.length}/3장
              </span>
            </div>
            <div className="grid grid-cols-3 gap-2.5">
              {photos.map((p, idx) => (
                <div
                  key={p.preview}
                  className="relative aspect-square overflow-hidden rounded-xl border border-border-base"
                >
                  <img
                    src={p.preview}
                    alt={`인증 사진 ${idx + 1}`}
                    className="h-full w-full object-cover"
                  />
                  <button
                    type="button"
                    onClick={() => removePhoto(idx)}
                    aria-label="삭제"
                    className="absolute right-1 top-1 flex h-5 w-5 items-center justify-center rounded-full bg-black/50 text-[12px] leading-none text-white"
                  >
                    ×
                  </button>
                </div>
              ))}
              {photos.length < MAX_PHOTOS && (
                <button
                  type="button"
                  onClick={() => fileRef.current?.click()}
                  className="flex aspect-square flex-col items-center justify-center gap-1 rounded-xl border-2 border-dashed border-border-base text-text-muted"
                >
                  <span className="text-[22px] leading-none">+</span>
                  <span className="text-[11px] font-semibold">추가</span>
                </button>
              )}
            </div>
            <input
              ref={fileRef}
              type="file"
              accept="image/*"
              multiple
              onChange={handleAddFiles}
              className="hidden"
            />
          </div>

          {/* 보너스 안내 */}
          <div className="mt-5 flex items-center gap-2 rounded-xl bg-amber-light px-4 py-3">
            <span className="text-[16px]">⭐</span>
            <p className="text-[12px] font-semibold text-amber-dark">
              인증 완료 시 골드 +30% 보너스가 적용돼요
            </p>
          </div>

          {errorMsg && (
            <p className="mt-4 text-center text-[12px] font-semibold text-error">
              {errorMsg}
            </p>
          )}
        </main>

        {/* 하단 버튼 (일반 완료 : 인증 완료 = 1 : 2) */}
        <footer className="absolute bottom-0 w-full bg-background/80 px-5 py-4 backdrop-blur-sm">
          <div className="flex gap-2.5">
            <button
              type="button"
              onClick={handleNormalComplete}
              disabled={submitting}
              className="h-12 flex-1 rounded-xl border border-border-base bg-white text-[13px] font-bold text-text-sub transition active:scale-[0.98] disabled:opacity-60"
            >
              일반 완료
            </button>
            <button
              type="button"
              onClick={handleCertify}
              disabled={submitting}
              className="flex h-12 flex-[2] items-center justify-center gap-1 rounded-xl bg-primary text-[14px] font-bold text-white shadow-lg transition active:scale-[0.98] disabled:opacity-60"
            >
              {submitting ? '처리 중...' : '📷 인증 완료'}
            </button>
          </div>
        </footer>
      </div>
    </div>
  )
}
