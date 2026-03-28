import axios from 'axios';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';
const LEADERBOARD_URL = process.env.REACT_APP_LEADERBOARD_URL || 'http://localhost:8085';

const apiClient = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth endpoints
export const login = (username, password) =>
  apiClient.post('/auth/login', { username, password });

export const signup = (username, password, role) =>
  apiClient.post('/auth/signup', { username, password, role });

// Auction endpoints
export const getAuctions = (page = 0, size = 10) =>
  apiClient.get(`/api/auctions/active?page=${page}&size=${size}`);

export const getAuctionById = (auctionId) =>
  apiClient.get(`/api/auctions/${auctionId}`);

export const getAuctionsByStatus = (status) =>
  apiClient.get(`/api/auctions`, { params: { status } });

export const getSellerAuctions = (sellerId) =>
  apiClient.get(`/api/auctions/seller/${sellerId}`);

export const createAuction = (data) =>
  apiClient.post('/api/auctions', data);

export const closeAuction = (auctionId) =>
  apiClient.put(`/api/auctions/${auctionId}/close`);

// Bid endpoints
export const placeBid = (auctionId, bidAmount) =>
  apiClient.post(`/api/auctions/${auctionId}/bids`, { bidAmount });

export const getBidHistory = (auctionId) =>
  apiClient.get(`/api/auctions/${auctionId}/bids`);

export const getHighestBid = (auctionId) =>
  apiClient.get(`/api/auctions/${auctionId}/highest-bid`);

export const getBidderBids = (bidderId) =>
  apiClient.get(`/api/auctions/bidders/${bidderId}/bids`);

export const getBidCount = (auctionId) =>
  apiClient.get(`/api/auctions/${auctionId}/bid-count`);

// Leaderboard endpoints (separate service)
export const getWeeklyLeaderboard = () => {
  const url = `${LEADERBOARD_URL}/api/leaderboard`;
  return axios.get(url);
};

export const getLeaderboardStats = () => {
  const url = `${LEADERBOARD_URL}/api/leaderboard/stats`;
  return axios.get(url);
};

export const getBidderStats = (bidderId) => {
  const url = `${LEADERBOARD_URL}/api/leaderboard/bidder/${bidderId}`;
  return axios.get(url);
};

export const getHistoricalLeaderboard = (year, week) => {
  const url = `${LEADERBOARD_URL}/api/leaderboard/week/${year}/${week}`;
  return axios.get(url);
};

// Error handler
export const handleApiError = (error) => {
  if (error.response) {
    // Server responded with error status
    return error.response.data?.message || `Error ${error.response.status}`;
  } else if (error.request) {
    // Request made but no response
    return 'Network error - no response from server';
  } else {
    // Error in request setup
    return error.message || 'Unknown error occurred';
  }
};

export default apiClient;
