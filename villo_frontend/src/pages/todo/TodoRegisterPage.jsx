import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomNav from '../../components/common/BottomNav'
import TodoTag, {
  difficultyStyles,
  categoryStyle,
  goldStyle,
} from '../../components/todo/TodoTag'
import RepeatEditor, {
  defaultRepeat,
  isRepeatValid,
  buildRepeatConfig,
} from '../../components/todo/RepeatEditor'
import { analyzeTodo, createTodo } from '../../api/todo'

// 뒤로가기 화살표
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

const REPEAT_LABEL = { NONE: '없음', DAILY: '매일', WEEKLY: '매주', MONTHLY: '매월' }

export default function TodoRegisterPage() {
  const navigate = useNavigate()

  const [step, setStep] = useState('input') // input | analyzing | result
  const [title, setTitle] = useState('')
  const [repeat, setRepeat] = useState(defaultRepeat)
  const [ai, setAi] = useState(null) // { category, difficulty, gold }
  const [submitting, setSubmitting] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  const canAnalyze = title.trim().length > 0 && isRepeatValid(repeat)

  // AI 분석
  const runAnalyze = async () => {
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

  // 등록
  const handleCreate = async () => {
    if (!ai || submitting) return
    setSubmitting(true)
    setErrorMsg('')
    try {
      await createTodo({
        title: title.trim(),
        category: ai.category,
        difficulty: ai.difficulty,
        gold: ai.gold,
        isRepeat: repeat.type !== 'NONE',
        repeatConfig: buildRepeatConfig(repeat),
      })
      navigate('/')
    } catch (err) {
      setErrorMsg(err.response?.data?.msg ?? '등록에 실패했어요')
      setSubmitting(false)
    }
  }

  const diff = ai ? (difficultyStyles[ai.difficulty] ?? difficultyStyles.NORMAL) : null

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
          <h1 className="text-[16px] font-bold text-white">퀘스트 추가</h1>
        </header>

        {/* 본문 */}
        <main className="flex-grow overflow-y-auto px-5 py-6 pb-24">
          {/* 입력 단계 */}
          {step === 'input' && (
            <div className="space-y-6">
              <div className="space-y-2">
                <label className="block text-[13px] font-bold text-text-sub">
                  퀘스트 이름
                </label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="예: 알고리즘 문제 3개 풀기"
                  maxLength={200}
                  className="h-12 w-full rounded-xl border-2 border-border-base bg-white px-4 text-[15px] text-text outline-none transition-colors focus:border-primary placeholder:text-text-muted"
                />
                <p className="text-[12px] text-text-muted">
                  퀘스트 이름을 입력하면 AI가 카테고리·난이도·골드를 자동으로
                  분석해요
                </p>
              </div>

              {/* 반복 설정 */}
              <RepeatEditor repeat={repeat} onChange={setRepeat} allowNone />

              {errorMsg && (
                <p className="text-[12px] font-semibold text-error">{errorMsg}</p>
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
                <p className="mt-1 text-[12px] text-text-sub">
                  잠시만 기다려주세요
                </p>
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
                    <TodoTag bg={diff.bg} text={diff.text}>
                      {diff.label}
                    </TodoTag>
                  </ResultRow>
                  <ResultRow label="획득 골드">
                    <TodoTag bg={goldStyle.bg} text={goldStyle.text}>
                      +{ai.gold} G
                    </TodoTag>
                  </ResultRow>
                  <ResultRow label="반복">
                    <span className="text-[13px] font-semibold text-text-sub">
                      {REPEAT_LABEL[repeat.type]}
                    </span>
                  </ResultRow>
                </div>
              </div>

              <p className="text-center text-[12px] text-text-muted">
                결과가 마음에 안 들면 다시 분석하거나 수정할 수 있어요
              </p>

              {errorMsg && (
                <p className="text-center text-[12px] font-semibold text-error">
                  {errorMsg}
                </p>
              )}

              <div className="space-y-2.5">
                <button
                  type="button"
                  onClick={handleCreate}
                  disabled={submitting}
                  className="flex h-14 w-full items-center justify-center gap-2 rounded-xl bg-primary text-[15px] font-bold text-white shadow-lg transition active:scale-[0.98] disabled:opacity-60"
                >
                  {submitting ? '등록 중...' : '✓ 퀘스트 등록'}
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

        {/* 입력 단계 하단 CTA */}
        {step === 'input' && (
          <footer className="absolute bottom-14 w-full bg-background/80 px-5 py-4 backdrop-blur-sm">
            <button
              type="button"
              onClick={runAnalyze}
              disabled={!canAnalyze}
              className={`flex h-14 w-full items-center justify-center gap-2 rounded-xl text-[15px] font-bold transition active:scale-[0.98] ${
                canAnalyze
                  ? 'bg-primary text-white shadow-lg'
                  : 'cursor-not-allowed bg-border-base text-text-sub'
              }`}
            >
              ✦ AI 분석 시작
            </button>
          </footer>
        )}

        <BottomNav />
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
