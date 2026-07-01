import TodoTag, {
  difficultyStyles,
  categoryStyle,
  goldStyle,
  repeatStyle,
} from './TodoTag'

// 카메라 아이콘 (사진 인증)
function CameraIcon() {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M4 8a2 2 0 0 1 2-2h1.5l1-1.5h5l1 1.5H18a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V8Z"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinejoin="round"
      />
      <circle cx="12" cy="12.5" r="3" stroke="currentColor" strokeWidth="1.8" />
    </svg>
  )
}

export default function TodoCard({ todo, onComplete, onCertify, onEdit, busy }) {
  const isCompleted = todo.status === 'COMPLETED'
  const diff = difficultyStyles[todo.difficulty] ?? difficultyStyles.NORMAL

  return (
    <div
      className={`rounded-2xl border border-border-base bg-white p-3.5 transition ${
        isCompleted ? 'opacity-50' : ''
      }`}
    >
      <div className="flex items-start gap-3">
        {/* 완료 체크박스 (일반 완료) */}
        <button
          type="button"
          onClick={() => !isCompleted && !busy && onComplete(todo)}
          disabled={isCompleted || busy}
          aria-label={isCompleted ? '완료됨' : '일반 완료'}
          className={`mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-full border-2 transition ${
            isCompleted
              ? 'border-primary bg-primary text-white'
              : 'border-border-base text-transparent active:scale-90'
          }`}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path
              d="M20 6 9 17l-5-5"
              stroke="currentColor"
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>

        <div
          className={`min-w-0 flex-grow ${isCompleted ? '' : 'cursor-pointer'}`}
          onClick={() => !isCompleted && onEdit?.(todo)}
        >
          <p
            className={`text-[15px] font-semibold text-text ${
              isCompleted ? 'line-through' : ''
            }`}
          >
            {todo.title}
          </p>

          {/* 태그 */}
          <div className="mt-2 flex flex-wrap items-center gap-1.5">
            <TodoTag bg={categoryStyle.bg} text={categoryStyle.text}>
              {todo.category}
            </TodoTag>
            <TodoTag bg={diff.bg} text={diff.text}>
              {diff.label}
            </TodoTag>
            <TodoTag bg={goldStyle.bg} text={goldStyle.text}>
              +{todo.gold} G
            </TodoTag>
            {todo.isRepeat && (
              <TodoTag bg={repeatStyle.bg} text={repeatStyle.text}>
                반복
              </TodoTag>
            )}
          </div>
        </div>

        {/* 사진 인증 진입 (미완료만) */}
        {!isCompleted && (
          <button
            type="button"
            onClick={() => onCertify(todo)}
            className="flex shrink-0 items-center gap-1 rounded-lg border border-border-base px-2 py-1.5 text-[11px] font-bold text-text-sub transition active:scale-95"
          >
            <CameraIcon />
            인증
          </button>
        )}
      </div>
    </div>
  )
}
