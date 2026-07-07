// 동물의 숲 느낌의 치비(2등신) 주민 캐릭터 — 이모지(imageUrl)를 키로 종을 매핑
// 맵/상점/보유목록에서 공용으로 사용. 큰 머리 + 반짝이는 눈 + 볼터치.
//
// 좌표계: viewBox "-4 -8 72 84" (여백 포함), 지면 y=64 기준으로 서 있음.
const VB = { x: -4, y: -8, w: 72, h: 84 }
const RATIO = VB.h / VB.w
const GROUND_VY = 64 // 캐릭터가 서 있는 지면 y
const GROUND_FRAC = (GROUND_VY - VB.y) / VB.h

// 종 설정 (이모지 → 색/귀/특징)
const SPECIES = {
  '🐰': { body: '#F2EBE4', shade: '#E2D6CB', belly: '#FCF8F4', earType: 'long', earInner: '#F5C4D0', nose: '#EC8AA1', noseType: 'dot', feat: 'bunny' },
  '🐿️': { body: '#C98A47', shade: '#B0763A', belly: '#EAD2AD', earType: 'tuft', earInner: '#8A5A2E', nose: '#4C3A2C', noseType: 'dot', muzzle: '#EAD2AD', feat: 'squirrel' },
  '🐶': { body: '#E7C593', shade: '#D2AC74', belly: '#F5E7CC', earType: 'flop', earInner: '#C89A63', nose: '#4C3A2C', noseType: 'tri', muzzle: '#F5E7CC', feat: 'dog' },
  '🐱': { body: '#CDD3D9', shade: '#B6BDC4', belly: '#EBEEF1', earType: 'point', earInner: '#EDB8C5', nose: '#EC8AA1', noseType: 'tri', feat: 'cat' },
  '🦊': { body: '#EF9A54', shade: '#DF7E3B', belly: '#FAE6D4', earType: 'point', earInner: '#4A3A2E', nose: '#3E3128', noseType: 'tri', muzzle: '#FAE6D4', feat: 'fox' },
  '🦉': { body: '#A98862', shade: '#8E6E4E', belly: '#E7D4B6', earType: 'owltuft', earInner: '#8E6E4E', nose: '#E7A23A', noseType: 'beak', feat: 'owl' },
  '🐻': { body: '#D6AC7C', shade: '#C29868', belly: '#EBD5B2', earType: 'round', earInner: '#C29868', nose: '#4A3F37', noseType: 'tri', muzzle: '#EBD5B2', feat: 'bear' },
  '🦁': { body: '#EBB65C', shade: '#D7A047', belly: '#F6DFAB', earType: 'round', earInner: '#D7A047', nose: '#4A3F37', noseType: 'tri', muzzle: '#F6DFAB', mane: '#C97C2E', feat: 'lion' },
  '🐯': { body: '#F2A94B', shade: '#E08F35', belly: '#FBE7CB', earType: 'point', earInner: '#3E3128', nose: '#5A4433', noseType: 'tri', muzzle: '#FBE7CB', feat: 'tiger' },
  '🦄': { body: '#F0E6F6', shade: '#DECCE8', belly: '#FBF6FD', earType: 'point', earInner: '#F5C4D8', nose: '#C98AB0', noseType: 'dot', feat: 'unicorn' },
}

const DEFAULT = { body: '#9CD05F', shade: '#84BA48', belly: '#EAF3DE', earType: 'round', earInner: '#84BA48', nose: '#3E5220', noseType: 'dot', feat: null }

function Ears({ cfg }) {
  switch (cfg.earType) {
    case 'round':
      return (
        <>
          <circle cx="19" cy="14" r="7" fill={cfg.body} />
          <circle cx="45" cy="14" r="7" fill={cfg.body} />
          <circle cx="19" cy="15" r="3.6" fill={cfg.earInner} />
          <circle cx="45" cy="15" r="3.6" fill={cfg.earInner} />
        </>
      )
    case 'point':
      return (
        <>
          <path d="M12 18 L20 3 L28 16 Z" fill={cfg.body} />
          <path d="M52 18 L44 3 L36 16 Z" fill={cfg.body} />
          <path d="M16 15 L20.5 6 L24.5 14 Z" fill={cfg.earInner} />
          <path d="M48 15 L43.5 6 L39.5 14 Z" fill={cfg.earInner} />
        </>
      )
    case 'long':
      return (
        <>
          <ellipse cx="25" cy="8" rx="4.2" ry="12.5" fill={cfg.body} transform="rotate(-8 25 8)" />
          <ellipse cx="39" cy="8" rx="4.2" ry="12.5" fill={cfg.body} transform="rotate(8 39 8)" />
          <ellipse cx="25" cy="8" rx="2" ry="8.5" fill={cfg.earInner} transform="rotate(-8 25 8)" />
          <ellipse cx="39" cy="8" rx="2" ry="8.5" fill={cfg.earInner} transform="rotate(8 39 8)" />
        </>
      )
    case 'tuft':
      return (
        <>
          <path d="M14 16 Q17 3 23 15 Z" fill={cfg.body} />
          <path d="M50 16 Q47 3 41 15 Z" fill={cfg.body} />
        </>
      )
    case 'owltuft':
      return (
        <>
          <path d="M18 12 L23 3 L27 13 Z" fill={cfg.body} />
          <path d="M46 12 L41 3 L37 13 Z" fill={cfg.body} />
        </>
      )
    default: // flop 등은 머리 앞에서 렌더
      return null
  }
}

function Nose({ cfg }) {
  const n =
    cfg.noseType === 'dot' ? (
      <ellipse cx="32" cy="32.3" rx="2.1" ry="1.7" fill={cfg.nose} />
    ) : (
      <path d="M29.5 31 H34.5 L32 34.2 Z" fill={cfg.nose} />
    )
  return (
    <>
      {n}
      <path d="M32 34.2 Q28.5 37.5 26 35.2" fill="none" stroke={cfg.nose} strokeWidth="1.1" strokeLinecap="round" />
      <path d="M32 34.2 Q35.5 37.5 38 35.2" fill="none" stroke={cfg.nose} strokeWidth="1.1" strokeLinecap="round" />
    </>
  )
}

function Face({ cfg }) {
  if (cfg.feat === 'owl') {
    return (
      <>
        <circle cx="24" cy="26.5" r="8.2" fill="#F3EAD8" />
        <circle cx="40" cy="26.5" r="8.2" fill="#F3EAD8" />
        <circle cx="24" cy="27" r="4.3" fill="#3A2E28" />
        <circle cx="40" cy="27" r="4.3" fill="#3A2E28" />
        <circle cx="22.6" cy="25.6" r="1.5" fill="#fff" />
        <circle cx="38.6" cy="25.6" r="1.5" fill="#fff" />
        <path d="M30 30 H34 L32 35 Z" fill={cfg.nose} />
      </>
    )
  }
  return (
    <>
      <ellipse cx="25" cy="27.5" rx="3" ry="4" fill="#3A2E28" />
      <ellipse cx="39" cy="27.5" rx="3" ry="4" fill="#3A2E28" />
      <circle cx="23.9" cy="25.8" r="1.3" fill="#fff" />
      <circle cx="37.9" cy="25.8" r="1.3" fill="#fff" />
      <ellipse cx="19.5" cy="33" rx="3.3" ry="2.3" fill="#F79FB6" opacity="0.6" />
      <ellipse cx="44.5" cy="33" rx="3.3" ry="2.3" fill="#F79FB6" opacity="0.6" />
      <Nose cfg={cfg} />
    </>
  )
}

function ManeBack({ cfg }) {
  const circles = []
  for (let k = 0; k < 12; k++) {
    const a = (k / 12) * Math.PI * 2
    circles.push(
      <circle key={k} cx={32 + Math.cos(a) * 17} cy={26 + Math.sin(a) * 17} r="6.5" fill={cfg.mane} />
    )
  }
  return <g>{circles}</g>
}

function Character({ cfg }) {
  return (
    <>
      {/* 뒤쪽 특수 */}
      {cfg.feat === 'lion' && <ManeBack cfg={cfg} />}
      {cfg.feat === 'squirrel' && (
        <g>
          <ellipse cx="47" cy="45" rx="7.5" ry="13" fill={cfg.body} transform="rotate(18 47 45)" />
          <ellipse cx="47" cy="45" rx="4" ry="9" fill={cfg.belly} transform="rotate(18 47 45)" />
        </g>
      )}
      {cfg.feat === 'unicorn' && (
        <ellipse cx="47" cy="52" rx="5" ry="9" fill="#F5B8D0" transform="rotate(20 47 52)" />
      )}

      {/* 다리 */}
      <ellipse cx="25" cy="62" rx="5.5" ry="4" fill={cfg.shade} />
      <ellipse cx="39" cy="62" rx="5.5" ry="4" fill={cfg.shade} />
      {/* 팔 */}
      <ellipse cx="17.5" cy="49" rx="4.6" ry="6.6" fill={cfg.shade} />
      <ellipse cx="46.5" cy="49" rx="4.6" ry="6.6" fill={cfg.shade} />
      {/* 몸 */}
      <ellipse cx="32" cy="52" rx="13" ry="12" fill={cfg.body} />
      <ellipse cx="32" cy="54" rx="8" ry="8.5" fill={cfg.belly} />

      {/* 귀 (머리 뒤) */}
      <Ears cfg={cfg} />

      {/* 머리 */}
      <circle cx="32" cy="26" r="18.5" fill={cfg.body} />

      {/* 주둥이 */}
      {cfg.muzzle && cfg.feat !== 'owl' && (
        <ellipse cx="32" cy="34" rx="8" ry="6" fill={cfg.muzzle} />
      )}

      {/* 얼굴 */}
      <Face cfg={cfg} />

      {/* 앞쪽 특수 */}
      {cfg.feat === 'tiger' && (
        <g stroke="#4A3320" strokeWidth="1.5" strokeLinecap="round" fill="none">
          <path d="M22 12 q2 4 0.5 8" />
          <path d="M32 8 v6" />
          <path d="M42 12 q-2 4 -0.5 8" />
        </g>
      )}
      {cfg.feat === 'dog' && (
        <>
          <ellipse cx="15" cy="27" rx="5" ry="9.5" fill={cfg.shade} transform="rotate(-14 15 27)" />
          <ellipse cx="49" cy="27" rx="5" ry="9.5" fill={cfg.shade} transform="rotate(14 49 27)" />
        </>
      )}
      {cfg.feat === 'unicorn' && (
        <>
          <path d="M25 12 Q28 4 32 11 Q36 4 39 12 Q34 9 32 13 Q30 9 25 12 Z" fill="#F5B8D0" />
          <path d="M32 -3 L28 12 L36 12 Z" fill="#F4CE5E" stroke="#E0B23F" strokeWidth="0.8" />
          <path d="M29.5 6 L34.5 4.2" stroke="#E0B23F" strokeWidth="0.7" />
          <path d="M29 10 L35 8.2" stroke="#E0B23F" strokeWidth="0.7" />
        </>
      )}
      {cfg.feat === 'bunny' && (
        <>
          <rect x="30" y="34.8" width="4" height="3.6" rx="1" fill="#fff" stroke="#E2D6CB" strokeWidth="0.5" />
          <line x1="32" y1="34.8" x2="32" y2="38.4" stroke="#E2D6CB" strokeWidth="0.5" />
        </>
      )}
    </>
  )
}

export default function VillagerSprite({
  emoji,
  size = 40,
  bob = false,
  delay = 0,
  cx,
  cy,
  className,
}) {
  const cfg = SPECIES[emoji] || DEFAULT
  const width = size
  const height = size * RATIO
  const positioned = cx != null && cy != null
  const pos = positioned
    ? { x: cx - width / 2, y: cy - GROUND_FRAC * height, width, height }
    : { width, height }

  return (
    <svg
      viewBox={`${VB.x} ${VB.y} ${VB.w} ${VB.h}`}
      {...pos}
      className={className}
      style={{ overflow: 'visible', pointerEvents: 'none' }}
      aria-hidden="true"
    >
      <ellipse cx="32" cy="67" rx="15" ry="3.6" fill="rgba(0,0,0,0.13)" />
      <g
        className={bob ? 'villager-bob' : undefined}
        style={bob ? { animationDelay: `${delay}s` } : undefined}
      >
        <Character cfg={cfg} />
      </g>
    </svg>
  )
}
