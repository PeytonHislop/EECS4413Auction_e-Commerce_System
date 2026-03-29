import { api } from "../../../shared/api/apiClient";

export const leaderboardApi = {
  getWeeklyLeaderboard: () => api.get("/api/leaderboard"),
  getWeeklyStats: () => api.get("/api/leaderboard/stats"),
  getBidderStats: (bidderId) => api.get(`/api/leaderboard/bidder/${bidderId}`),
  getWeeklyLeaderboardByWeek: (year, week) => api.get(`/api/leaderboard/week/${year}/${week}`)
};
