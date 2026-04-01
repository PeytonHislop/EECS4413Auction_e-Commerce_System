import { api } from "../../../shared/api/apiClient";
import { wsClient } from "../../../shared/api/websocketClient";

export const auctionApi = {
  getActiveAuctions: () => api.get("/api/auctions/active"),
  getAuctionById: (auctionId) => api.get(`/api/auctions/${auctionId}`),
  getSellerAuctions: (sellerId) => api.get(`/api/auctions/seller/${sellerId}`),
  createAuction: (payload, token) => api.post("/api/auctions", payload, token),
  placeBid: (auctionId, payload, token) =>
    api.post(`/api/auctions/${auctionId}/bids`, payload, token),
  getBidHistory: (auctionId) => api.get(`/api/auctions/${auctionId}/bids`),
  getHighestBid: (auctionId) => api.get(`/api/auctions/${auctionId}/highest-bid`),
  getBidCount: (auctionId) => api.get(`/api/auctions/${auctionId}/bid-count`),
  getBidsByBidder: (bidderId) => api.get(`/api/auctions/bidders/${bidderId}/bids`),
  closeAuction: (auctionId, token) => api.put(`/api/auctions/${auctionId}/close`, null, token),
  closeExpiredAuctions: (token) => api.post("/api/auctions/close-expired", null, token),

  // WebSocket methods for real-time bid updates
  isWebSocketConnected: () => wsClient.isConnected,
  connectWebSocket: (onConnect, onError) => wsClient.connect(onConnect, onError),
  disconnectWebSocket: () => wsClient.disconnect(),
  subscribeToAuctionBids: (auctionId, callback) =>
    wsClient.subscribe(`/topic/auction/${auctionId}/bids`, callback),
  unsubscribeFromAuctionBids: (auctionId) =>
    wsClient.unsubscribe(`/topic/auction/${auctionId}/bids`)
};
