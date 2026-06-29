import BottomNav from '../../components/common/BottomNav'

// 틀만 — 마을 메인 (추후 GET /api/v1/village 연동)
export default function VillageMainPage() {
  return (
    <div className="flex min-h-screen justify-center bg-background">
      <div className="relative flex min-h-screen w-full max-w-[375px] flex-col bg-background">
        <header className="flex h-16 shrink-0 items-center bg-primary px-5">
          <h1 className="text-[16px] font-bold text-white">나의 마을</h1>
        </header>

        <main className="flex flex-grow flex-col items-center justify-center gap-2 pb-24 text-center">
          <span className="text-[40px]">🏘️</span>
          <p className="text-[14px] font-semibold text-text-sub">마을 화면 준비 중</p>
        </main>

        <BottomNav />
      </div>
    </div>
  )
}
