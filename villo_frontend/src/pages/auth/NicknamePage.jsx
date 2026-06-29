import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { checkNickname, setNickname } from "../../api/auth";

// 백엔드 NicknameRequest 검증 규칙과 동일
const NICKNAME_REGEX = /^[가-힣a-zA-Z0-9]+$/;
const validateNickname = (value) => {
  if (value.length < 2 || value.length > 10) return "닉네임은 2~10자여야 해요";
  if (!NICKNAME_REGEX.test(value)) return "한글, 영문, 숫자만 사용할 수 있어요";
  return null;
};

// 체크 아이콘
function CheckIcon() {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M20 6 9 17l-5-5"
        stroke="currentColor"
        strokeWidth="3"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export default function NicknamePage() {
  const navigate = useNavigate();

  const [nickname, setNicknameValue] = useState("");
  const [villageName, setVillageName] = useState("");

  // 닉네임 상태: idle | checking | available | error
  const [nickStatus, setNickStatus] = useState("idle");
  const [nickMessage, setNickMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const isAvailable = nickStatus === "available";

  // 닉네임 입력 변경 → 확인 상태 초기화 (재확인 필요)
  const handleNicknameChange = (e) => {
    setNicknameValue(e.target.value);
    setNickStatus("idle");
    setNickMessage("");
  };

  // 중복 확인
  const handleCheck = async () => {
    const error = validateNickname(nickname);
    if (error) {
      setNickStatus("error");
      setNickMessage(error);
      return;
    }

    setNickStatus("checking");
    try {
      await checkNickname(nickname);
      setNickStatus("available");
      setNickMessage("사용 가능한 닉네임이에요");
    } catch (err) {
      setNickStatus("error");
      setNickMessage(err.response?.data?.msg ?? "이미 사용 중인 닉네임이에요");
    }
  };

  // 시작하기
  const handleStart = async () => {
    if (!isAvailable || submitting) return;
    setSubmitting(true);
    try {
      await setNickname(nickname, villageName.trim() || null);
      navigate("/");
    } catch (err) {
      setNickStatus("error");
      setNickMessage(
        err.response?.data?.msg ?? "설정에 실패했어요. 다시 시도해주세요",
      );
      setSubmitting(false);
    }
  };

  // 인풋 테두리 색상
  const nickBorder =
    nickStatus === "available"
      ? "border-primary"
      : nickStatus === "error"
        ? "border-error"
        : "border-border-base focus-within:border-primary";

  const previewName = villageName.trim() || "나의 마을";
  const canCheck =
    nickname.length > 0 && nickStatus !== "checking" && !isAvailable;

  return (
    <div className="flex min-h-screen justify-center bg-background">
      <div className="relative flex min-h-screen w-full max-w-[375px] flex-col bg-background">
        {/* 헤더 */}
        <header className="sticky top-0 z-10 flex h-16 shrink-0 flex-col justify-center bg-primary px-5">
          <h1 className="text-[16px] font-bold text-white">닉네임 설정</h1>
        </header>

        {/* 본문 */}
        <main className="flex-grow space-y-8 overflow-y-auto px-5 py-8 pb-28">
          {/* 환영 문구 */}
          <section className="space-y-1 text-center">
            <div className="mb-1 inline-block origin-bottom-right animate-wave text-[40px]">
              👋
            </div>
            <h2 className="text-[20px] font-bold text-text">처음 오셨군요!</h2>
            <p className="text-[14px] text-text-sub">
              마을에서 사용할 닉네임을 설정해주세요
            </p>
          </section>

          <div className="space-y-6">
            {/* 닉네임 입력 */}
            <div className="space-y-1.5">
              <label className="block text-[12px] font-bold text-text-sub">
                닉네임
              </label>
              <div
                className={`relative flex items-center rounded-xl border-2 bg-white transition-colors ${nickBorder}`}
              >
                <input
                  type="text"
                  value={nickname}
                  onChange={handleNicknameChange}
                  placeholder="닉네임을 입력해주세요"
                  maxLength={10}
                  className="h-12 w-full rounded-xl bg-transparent px-4 text-[16px] text-text outline-none placeholder:text-text-muted"
                />
                <button
                  type="button"
                  onClick={handleCheck}
                  disabled={!canCheck}
                  className={`absolute right-2 rounded-lg px-3 py-1.5 text-[11px] font-bold text-white transition active:scale-95 ${
                    isAvailable
                      ? "bg-green-dark"
                      : canCheck
                        ? "bg-green-dark"
                        : "cursor-not-allowed bg-border-base"
                  }`}
                >
                  {nickStatus === "checking"
                    ? "확인 중..."
                    : isAvailable
                      ? "확인 완료"
                      : "중복 확인"}
                </button>
              </div>
              {nickMessage && (
                <div
                  className={`flex items-center gap-1 text-[11px] font-semibold ${
                    isAvailable ? "text-primary" : "text-error"
                  }`}
                >
                  {isAvailable && <CheckIcon />}
                  <span>{nickMessage}</span>
                </div>
              )}
            </div>

            {/* 마을 이름 입력 (선택) */}
            <div className="space-y-1.5">
              <label className="block text-[12px] font-bold text-text-sub">
                마을 이름 (선택)
              </label>
              <input
                type="text"
                value={villageName}
                onChange={(e) => setVillageName(e.target.value)}
                placeholder="나의 마을"
                maxLength={20}
                className="h-12 w-full rounded-xl border-2 border-border-base bg-white px-4 text-[16px] text-text outline-none transition-colors focus:border-primary placeholder:text-text-muted"
              />
              {villageName.trim() && (
                <div className="flex items-center gap-1 text-[11px] font-semibold text-primary">
                  <CheckIcon />
                  <span>멋진 마을 이름이에요!</span>
                </div>
              )}
            </div>

            {/* 마을 미리보기 카드 */}
            <div className="flex items-center gap-4 rounded-3xl border border-primary/10 bg-green-light p-4 shadow-sm">
              <div className="flex h-12 w-12 items-center justify-center rounded-full bg-white text-[22px] shadow-sm">
                🏘️
              </div>
              <div className="flex flex-col">
                <span className="text-[14px] font-bold text-primary">
                  {previewName}
                </span>
                <span className="text-[11px] font-medium text-text-sub">
                  주민 0명 · Lv.1로 시작해요
                </span>
              </div>
            </div>
          </div>
        </main>

        {/* 하단 시작하기 버튼 */}
        <footer className="absolute bottom-0 w-full bg-background/80 px-5 py-4 backdrop-blur-sm">
          <button
            type="button"
            onClick={handleStart}
            disabled={!isAvailable || submitting}
            className={`flex h-14 w-full items-center justify-center gap-2 rounded-xl text-[15px] font-bold transition active:scale-[0.98] ${
              isAvailable && !submitting
                ? "bg-primary text-white shadow-lg"
                : "cursor-not-allowed bg-border-base text-text-sub"
            }`}
          >
            <span>{submitting ? "입장하는 중..." : "시작하기"}</span>
            <span className="text-[20px]">🏘️</span>
          </button>
        </footer>
      </div>
    </div>
  );
}
