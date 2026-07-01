// 투두 태그 스타일 (DESIGN.md 6번 기준)
export const difficultyStyles = {
  EASY: { bg: '#EAF3DE', text: '#3B6D11', label: '쉬움' },
  NORMAL: { bg: '#FAEEDA', text: '#854F0B', label: '보통' },
  HARD: { bg: '#FFE0E0', text: '#D85A30', label: '어려움' },
}

// 카테고리(파란 칩) / 골드(초록 칩) / 반복(회색 칩)
export const categoryStyle = { bg: '#E6F1FB', text: '#185FA5' }
export const goldStyle = { bg: '#EAF3DE', text: '#3B6D11' }
export const repeatStyle = { bg: '#F1EFE8', text: '#5F5E5A' }

// 공용 pill 태그
export default function TodoTag({ bg, text, children }) {
  return (
    <span
      className="inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-bold"
      style={{ backgroundColor: bg, color: text }}
    >
      {children}
    </span>
  )
}
