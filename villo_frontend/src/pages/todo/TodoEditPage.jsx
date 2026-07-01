import { useEffect, useState } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import TodoTag, {
  difficultyStyles,
  categoryStyle,
  goldStyle,
} from '../../components/todo/TodoTag'
import {
  getTodayTodos,
  analyzeTodo,
  updateTodoTitle,
  deleteTodo,
} from '../../api/todo'

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

export default function TodoEditPage() {
  const navigate = useNavigate()
  const { todoId } = useParams()
  const { state } = useLocation()

  // 홈 카드에서 넘겨받은 투두. 없으면(직접 URL 진입) 목록에서 조회
  const [todo, setTodo] = useState(state?.todo ?? null)
  const [title, setTitle] = useState(state?.todo?.title ?? '')

  const [step, setStep] = useState('input') // input | analyzing | result
  const [ai, setAi] = useState(null) // 새 제목 분석 결과 { category, difficulty, gold }
  const [saving, setSaving] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  useEffect(() => {
    if (todo) return
    getTodayTodos()
      .then((list) => {
        const found = (list ?? []).find((t) => String(t.id) === String(todoId))
        if (found) {
          setTodo(found)
          setTitle(found.title)
        }
      })
      .catch(() => {})
  }, [todo, todoId])

  const titleChanged = todo && title.trim() && title.trim() !== todo.title
  const currentDiff = todo
    ? (difficultyStyles[todo.difficulty] ?? difficultyStyles.NORMAL)
    : null
  const aiDiff = ai ? (difficultyStyles[ai.difficulty] ?? difficultyStyles.NORMAL) : null

  // 제목 입력 변경 → 결과 초기화 (재분석 필요)
  const handleTitleChange = (e) => {
    setTitle(e.target.value)
    setStep('input')
    setAi(null)
  }

  // AI 분석 (미리보기, 저장 안 함)
  const runAnalyze = async () => {
    if (!titleChanged) return
    setStep('analyzing')
    setErrorMsg('')
    try {
      const result = await analyzeTodo(title.trim())
      setAi(result)
      setStep('result')
    } catch (err) {
      setErrorMsg(err.response?.data?.msg ?? 'AI 분석에 실패했어요')
      setStep('input')
    }
  }

  // 수정 완료 (PUT — 서버가 제목 변경분 재분석해 저장)
  const handleSave = async () => {
    if (saving) return
    setSaving(true)
    setErrorMsg('')
    try {
      await updateTodoTitle(todoId, title.trim())
      navigate('/')
    } catch (err) {
      setErrorMsg(err.response?.data?.msg ?? '수정에 실패했어요')
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    if (saving) return
    setSaving(true)
    setErrorMsg('')
    try {
      await deleteTodo(todoId)
      navigate('/')
    } catch (err) {
      setErrorMsg(err.response?.data?.msg ?? '삭제에 실패했어요')
      setSaving(false)
    }
  }

  const goRepeat = () =>
    navigate(`/todos/${todoId}/repeat`, {
      state: { isRepeat: todo?.isRepeat ?? false },
    })

  return (
    <div className="flex min-h-screen justify-center bg-background">
      <div className="relative flex min-h-screen w-full max-w-[375px] flex-col bg-background">
        {/* 헤더 */}
        <header className="flex h-16 shrink-0 items-center gap-2 bg-primary px-4">
          <button
            type="button"
            onClick={() => (step === 'result' ? setStep('input') : navigate(-1))}
            aria-label="뒤로"
            className="text-white"
          >
            <BackIcon />
          </button>
          <h1 className="text-[16px] font-bold text-white">퀘스트 편집</h1>
        </header>

        <main className="flex-grow overflow-y-auto px-5 py-6 pb-40">
          {/* 입력 단계 */}
          {step === 'input' && (
            <div className="space-y-6">
              {/* 제목 수정 */}
              <div className="space-y-2">
                <label className="block text-[13px] font-bold text-text-sub">
                  퀘스트 이름
                </label>
                <input
                  type="text"
                  value={title}
                  onChange={handleTitleChange}
                  maxLength={200}
                  className="h-12 w-full rounded-xl border-2 border-border-base bg-white px-4 text-[15px] text-text outline-none transition-colors focus:border-primary"
                />
                {titleChanged && (
                  <p className="text-[12px] text-text-muted">
                    제목을 바꿨어요. 아래 AI 분석으로 카테고리·난이도·골드를 다시
                    받아보세요
                  </p>
                )}
              </div>

              {/* 현재 AI 분석 정보 (읽기 전용) */}
              {todo && (
                <div className="rounded-2xl border border-border-base bg-white p-4">
                  <p className="mb-3 text-[12px] font-bold text-text-muted">
                    현재 분석 정보
                  </p>
                  <div className="flex flex-wrap items-center gap-1.5">
                    <TodoTag bg={categoryStyle.bg} text={categoryStyle.text}>
                      {todo.category}
                    </TodoTag>
                    <TodoTag bg={currentDiff.bg} text={currentDiff.text}>
                      {currentDiff.label}
                    </TodoTag>
                    <TodoTag bg={goldStyle.bg} text={goldStyle.text}>
                      +{todo.gold} G
                    </TodoTag>
                  </div>
                </div>
              )}

              {/* 반복 설정 진입 */}
              <button
                type="button"
                onClick={goRepeat}
                className="flex w-full items-center justify-between rounded-2xl border border-border-base bg-white px-4 py-4"
              >
                <span className="text-[14px] font-semibold text-text">
                  반복 설정
                </span>
                <span className="flex items-center gap-1 text-text-sub">
                  <span className="text-[13px]">
                    {todo?.isRepeat ? '설정됨' : '없음'}
                  </span>
                  <ChevronRight />
                </span>
              </button>

              {/* 삭제 */}
              <button
                type="button"
                onClick={handleDelete}
                disabled={saving}
                className="w-full text-center text-[13px] font-semibold text-error"
              >
                퀘스트 삭제
              </button>

              {errorMsg && (
                <p className="text-center text-[12px] font-semibold text-error">
                  {errorMsg}
                </p>
              )}
            </div>
          )}

          {/* 분석 중 단계 */}
          {step === 'analyzing' && (
            <div className="flex flex-col items-center justify-center gap-4 py-28 text-center">
              <div className="h-10 w-10 animate-spin rounded-full border-4 border-green-light border-t-primary" />
              <div>
                <p className="text-[15px] font-bold text-text">
                  AI가 분석하고 있어요
                </p>
                <p className="mt-1 text-[12px] text-text-sub">잠시만 기다려주세요</p>
              </div>
            </div>
          )}

          {/* 결과 확인 단계 */}
          {step === 'result' && ai && (
            <div className="space-y-5">
              <div className="inline-flex items-center gap-1 rounded-full bg-green-light px-3 py-1 text-[12px] font-bold text-green-dark">
                ✦ AI 분석 완료
              </div>

              <div className="rounded-2xl border border-border-base bg-white p-5 shadow-sm">
                <p className="text-[18px] font-bold text-text">{title.trim()}</p>

                <div className="mt-4 space-y-3">
                  <ResultRow label="카테고리">
                    <TodoTag bg={categoryStyle.bg} text={categoryStyle.text}>
                      {ai.category}
                    </TodoTag>
                  </ResultRow>
                  <ResultRow label="난이도">
                    <TodoTag bg={aiDiff.bg} text={aiDiff.text}>
                      {aiDiff.label}
                    </TodoTag>
                  </ResultRow>
                  <ResultRow label="획득 골드">
                    <TodoTag bg={goldStyle.bg} text={goldStyle.text}>
                      +{ai.gold} G
                    </TodoTag>
                  </ResultRow>
                </div>
              </div>

              <p className="text-center text-[12px] text-text-muted">
                이 내용으로 수정하거나 다시 분석할 수 있어요
              </p>

              {errorMsg && (
                <p className="text-center text-[12px] font-semibold text-error">
                  {errorMsg}
                </p>
              )}

              <div className="space-y-2.5">
                <button
                  type="button"
                  onClick={handleSave}
                  disabled={saving}
                  className="flex h-14 w-full items-center justify-center gap-2 rounded-xl bg-primary text-[15px] font-bold text-white shadow-lg transition active:scale-[0.98] disabled:opacity-60"
                >
                  {saving ? '수정 중...' : '✓ 수정 완료'}
                </button>
                <button
                  type="button"
                  onClick={runAnalyze}
                  className="flex h-12 w-full items-center justify-center gap-2 rounded-xl border border-border-base bg-white text-[13px] font-bold text-text-sub transition active:scale-[0.98]"
                >
                  ↻ 다시 분석하기
                </button>
              </div>
            </div>
          )}
        </main>

        {/* 입력 단계 하단 CTA — AI 분석 */}
        {step === 'input' && (
          <footer className="absolute bottom-0 w-full bg-background/80 px-5 py-4 backdrop-blur-sm">
            <button
              type="button"
              onClick={runAnalyze}
              disabled={!titleChanged}
              className={`flex h-14 w-full items-center justify-center gap-2 rounded-xl text-[15px] font-bold transition active:scale-[0.98] ${
                titleChanged
                  ? 'bg-primary text-white shadow-lg'
                  : 'cursor-not-allowed bg-border-base text-text-sub'
              }`}
            >
              ✦ AI 분석
            </button>
          </footer>
        )}
      </div>
    </div>
  )
}

function ResultRow({ label, children }) {
  return (
    <div className="flex items-center justify-between border-t border-border-base/60 pt-3 first:border-t-0 first:pt-0">
      <span className="text-[13px] text-text-sub">{label}</span>
      {children}
    </div>
  )
}
