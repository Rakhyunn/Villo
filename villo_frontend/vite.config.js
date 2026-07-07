import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // 백엔드 CORS / OAuth 리다이렉트 포트와 일치 (5173)
    port: 5173,
    // 5173 점유 시 다른 포트로 폴백 금지 (CORS/OAuth 깨짐 방지)
    strictPort: true,
  },
});
