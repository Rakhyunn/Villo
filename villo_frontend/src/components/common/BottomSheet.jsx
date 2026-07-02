// 공용 바텀시트 (DESIGN.md 3번 규칙)
export default function BottomSheet({ open, onClose, title, children }) {
  if (!open) return null
  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center"
      onClick={onClose}
    >
      {/* 배경 오버레이 */}
      <div className="absolute inset-0 bg-black/30" />

      {/* 시트 */}
      <div
        onClick={(e) => e.stopPropagation()}
        className="relative w-full max-w-[375px] rounded-t-[20px] bg-white px-5 pb-8 pt-3 shadow-2xl"
      >
        {/* 핸들바 */}
        <div className="mx-auto mb-4 h-1 w-9 rounded-full bg-border-base" />
        {title && (
          <h2 className="mb-4 text-[16px] font-bold text-text">{title}</h2>
        )}
        {children}
      </div>
    </div>
  )
}
