// 아이소메트릭 마을 맵 (SVG) — 동물의 숲 느낌의 입체 섬 + 장식 + 애니메이션
const TILE_W = 46
const TILE_H = 23
const DEPTH = 14 // 섬 두께(흙 단면)

// 배경 잔디에 흩뿌리는 장식 (비율 좌표, sway=나무 흔들림)
const DECOR = [
  { e: '🌳', fx: 0.1, fy: 0.26, s: 30, sway: true },
  { e: '🌲', fx: 0.88, fy: 0.22, s: 28, sway: true },
  { e: '🌳', fx: 0.2, fy: 0.06, s: 24, sway: true },
  { e: '🌲', fx: 0.8, fy: 0.05, s: 22, sway: true },
  { e: '🌷', fx: 0.06, fy: 0.62, s: 16 },
  { e: '🌸', fx: 0.94, fy: 0.66, s: 16 },
  { e: '🍄', fx: 0.14, fy: 0.86, s: 15 },
  { e: '🌼', fx: 0.86, fy: 0.9, s: 15 },
  { e: '🪨', fx: 0.5, fy: 0.965, s: 18 },
]

export default function VillageMap({
  gridSize = 5,
  placements = [],
  editMode = false,
  selectedCell = null,
  onTileTap,
}) {
  const pad = 40
  const width = gridSize * TILE_W + pad * 2
  const height = gridSize * TILE_H + pad * 2 + DEPTH + 20
  const originX = width / 2
  const originY = pad + 16

  const toScreen = (x, y) => ({
    cx: originX + (x - y) * (TILE_W / 2),
    cy: originY + (x + y) * (TILE_H / 2),
  })

  // 섬 외곽 4개 꼭짓점
  const c00 = toScreen(0, 0)
  const cN0 = toScreen(gridSize - 1, 0)
  const cNN = toScreen(gridSize - 1, gridSize - 1)
  const c0N = toScreen(0, gridSize - 1)
  const top = { x: c00.cx, y: c00.cy - TILE_H / 2 }
  const right = { x: cN0.cx + TILE_W / 2, y: cN0.cy }
  const bottom = { x: cNN.cx, y: cNN.cy + TILE_H / 2 }
  const left = { x: c0N.cx - TILE_W / 2, y: c0N.cy }

  // 개별 타일
  const tiles = []
  for (let y = 0; y < gridSize; y++) {
    for (let x = 0; x < gridSize; x++) {
      const { cx, cy } = toScreen(x, y)
      tiles.push({
        x,
        y,
        cx,
        cy,
        points: `${cx},${cy - TILE_H / 2} ${cx + TILE_W / 2},${cy} ${cx},${cy + TILE_H / 2} ${cx - TILE_W / 2},${cy}`,
      })
    }
  }

  const isSel = (x, y) =>
    selectedCell && selectedCell.x === x && selectedCell.y === y

  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      className="village-map-enter h-full w-full"
      preserveAspectRatio="xMidYMid meet"
    >
      <defs>
        <linearGradient id="grassTop" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#B7E07E" />
          <stop offset="1" stopColor="#9CD05F" />
        </linearGradient>
        <linearGradient id="dirt" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#C79A5E" />
          <stop offset="1" stopColor="#A9773F" />
        </linearGradient>
      </defs>

      {/* 배경 장식 (섬 뒤) */}
      {DECOR.filter((d) => d.fy < 0.5).map((d, i) => (
        <text
          key={`decor-b-${i}`}
          x={d.fx * width}
          y={d.fy * height}
          textAnchor="middle"
          fontSize={d.s}
          className={d.sway ? 'tree-sway' : undefined}
          style={{ pointerEvents: 'none', animationDelay: `${i * 0.3}s` }}
        >
          {d.e}
        </text>
      ))}

      {/* 섬 흙 단면 (좌/우) */}
      <polygon
        points={`${left.x},${left.y} ${bottom.x},${bottom.y} ${bottom.x},${bottom.y + DEPTH} ${left.x},${left.y + DEPTH}`}
        fill="url(#dirt)"
      />
      <polygon
        points={`${right.x},${right.y} ${bottom.x},${bottom.y} ${bottom.x},${bottom.y + DEPTH} ${right.x},${right.y + DEPTH}`}
        fill="url(#dirt)"
        opacity="0.85"
      />

      {/* 섬 윗면(잔디) */}
      <polygon
        points={`${top.x},${top.y} ${right.x},${right.y} ${bottom.x},${bottom.y} ${left.x},${left.y}`}
        fill="url(#grassTop)"
        stroke="#8CC152"
        strokeWidth="1.5"
      />

      {/* 타일 (체커 + 편집 시 그리드) */}
      {tiles.map((t) => (
        <polygon
          key={`${t.x}-${t.y}`}
          points={t.points}
          fill={
            isSel(t.x, t.y)
              ? '#EF9F27'
              : (t.x + t.y) % 2 === 0
                ? 'rgba(255,255,255,0.10)'
                : 'transparent'
          }
          stroke={editMode ? 'rgba(59,109,17,0.35)' : 'transparent'}
          strokeWidth="1"
          onClick={editMode ? () => onTileTap?.(t.x, t.y) : undefined}
          style={editMode ? { cursor: 'pointer' } : undefined}
        />
      ))}

      {/* 주민 (그림자 + 통통 튀는 이모지) */}
      {placements.map((p, i) => {
        const { cx, cy } = toScreen(p.gridX, p.gridY)
        return (
          <g key={p.id} style={{ pointerEvents: 'none' }}>
            <ellipse
              cx={cx}
              cy={cy + 2}
              rx="12"
              ry="4.5"
              fill="rgba(0,0,0,0.14)"
            />
            <text
              x={cx}
              y={cy - TILE_H * 0.5}
              textAnchor="middle"
              dominantBaseline="middle"
              fontSize="26"
              className="villager-bob"
              style={{ animationDelay: `${(i % 5) * 0.28}s` }}
            >
              {p.villagerImageUrl}
            </text>
          </g>
        )
      })}

      {/* 앞쪽 장식 (섬 앞) */}
      {DECOR.filter((d) => d.fy >= 0.5).map((d, i) => (
        <text
          key={`decor-f-${i}`}
          x={d.fx * width}
          y={d.fy * height}
          textAnchor="middle"
          fontSize={d.s}
          className={d.sway ? 'tree-sway' : undefined}
          style={{ pointerEvents: 'none', animationDelay: `${i * 0.3}s` }}
        >
          {d.e}
        </text>
      ))}
    </svg>
  )
}
