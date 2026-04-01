import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  server: {
    port: 5173,
    proxy: {
      // Catalogue service (Anton)
      "/api/catalogue": {
        target: "http://localhost:8083",
        changeOrigin: true
      },

      // Auction service (Mustafa) — REST + WebSocket
      // ws: true tells Vite to also proxy WebSocket upgrade requests
      // This is what makes SockJS work through the dev proxy
      "/api/auctions": {
        target: "http://localhost:8082",
        changeOrigin: true
      },
      "/ws": {
        target: "http://localhost:8082",
        changeOrigin: true,
        ws: true          // ← critical for SockJS/WebSocket connections
      },

      // Leaderboard service (Mustafa)
      "/leaderboard-api": {
        target: "http://localhost:8085",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/leaderboard-api/, "")
      },

      // Everything else → gateway (IAM, payment, etc.)
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true
      }
    }
  }
});
