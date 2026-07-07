import { socialLogin } from '../../api/auth'

// 카카오 말풍선 아이콘
function KakaoIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
      <path
        fill="#191919"
        d="M12 3C6.48 3 2 6.54 2 10.9c0 2.82 1.87 5.29 4.68 6.68-.16.57-.6 2.18-.69 2.52-.11.42.16.42.33.31.13-.09 2.12-1.44 2.98-2.03.55.08 1.12.12 1.7.12 5.52 0 10-3.54 10-7.92S17.52 3 12 3z"
      />
    </svg>
  )
}

// 네이버 N 아이콘
function NaverIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true">
      <path
        fill="#ffffff"
        d="M16.27 12.84 7.55 0H0v24h7.73V11.16L16.45 24H24V0h-7.73v12.84z"
      />
    </svg>
  )
}

// 구글 컬러 G 아이콘
function GoogleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true">
      <path
        fill="#FFC107"
        d="M43.6 20.5H42V20H24v8h11.3c-1.6 4.7-6.1 8-11.3 8-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.9 1.2 8 3.1l5.7-5.7C34 4.1 29.3 2 24 2 11.8 2 2 11.8 2 24s9.8 22 22 22 22-9.8 22-22c0-1.5-.2-2.6-.4-3.5z"
      />
      <path
        fill="#FF3D00"
        d="m6.3 14.7 6.6 4.8C14.7 15.1 19 12 24 12c3.1 0 5.9 1.2 8 3.1l5.7-5.7C34 4.1 29.3 2 24 2 15.5 2 8.1 6.8 6.3 14.7z"
      />
      <path
        fill="#4CAF50"
        d="M24 46c5.2 0 9.9-2 13.4-5.2l-6.2-5.2C29.2 36.9 26.7 38 24 38c-5.2 0-9.6-3.3-11.3-7.9l-6.5 5C8 41.1 15.4 46 24 46z"
      />
      <path
        fill="#1976D2"
        d="M43.6 20.5H42V20H24v8h11.3c-.8 2.2-2.2 4.2-4.1 5.6l6.2 5.2C42.9 35 46 30 46 24c0-1.5-.2-2.6-.4-3.5z"
      />
    </svg>
  )
}

function SocialButton({ icon, label, onClick, className }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex h-[52px] w-full items-center justify-center gap-2 rounded-xl text-[15px] font-semibold transition active:scale-[0.98] ${className}`}
    >
      <span className="flex w-5 items-center justify-center">{icon}</span>
      <span>{label}</span>
    </button>
  )
}

export default function LoginPage() {
  return (
    <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden bg-gradient-to-b from-[#EAF3DE] via-[#F1F7E8] to-[#DCEBC4] px-6">
      {/* 하단 잔디 장식 */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-x-0 bottom-0 h-24 bg-gradient-to-t from-[#C0DD97]/70 to-transparent"
      />

      <main className="z-10 flex w-full max-w-[375px] flex-col items-center">
        {/* 로고 영역 */}
        <div className="mb-12 flex flex-col items-center">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-white text-3xl shadow-sm">
            🏠
          </div>
          <h1 className="text-[22px] font-extrabold tracking-tight text-green-dark">
            Villo
          </h1>
          <p className="mt-1.5 text-[13px] text-text-sub">
            투두를 완료하고 나만의 마을을 키워요
          </p>
        </div>

        {/* 소셜 로그인 버튼 */}
        <div className="flex w-full flex-col gap-3">
          <SocialButton
            icon={<KakaoIcon />}
            label="카카오로 시작하기"
            onClick={() => socialLogin('kakao')}
            className="bg-[#FEE500] text-[#191919]"
          />
          <SocialButton
            icon={<NaverIcon />}
            label="네이버로 시작하기"
            onClick={() => socialLogin('naver')}
            className="bg-[#03C75A] text-white"
          />
          <SocialButton
            icon={<GoogleIcon />}
            label="Google로 시작하기"
            onClick={() => socialLogin('google')}
            className="border border-border-base bg-white text-text"
          />
        </div>

        {/* 약관 안내 */}
        <p className="mt-7 px-2 text-center text-[11px] leading-relaxed text-text-muted">
          시작하면 <span className="underline">이용약관</span> 및{' '}
          <span className="underline">개인정보처리방침</span>에 동의하는 것으로
          간주됩니다.
        </p>
      </main>
    </div>
  )
}
