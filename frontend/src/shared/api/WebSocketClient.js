import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketClient {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.isConnected = false;
  }

  connect(onConnect, onError) {
    if (this.client && this.isConnected) {
      return;
    }

    // Connect directly to auction service (assuming gateway forwards or direct connection)
    const socket = new SockJS('http://localhost:8082/ws');
    this.client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log(str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = (frame) => {
      console.log('Connected to WebSocket');
      this.isConnected = true;
      if (onConnect) onConnect();
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      console.error('Details:', frame.body);
      this.isConnected = false;
      if (onError) onError(frame);
    };

    this.client.onWebSocketClose = () => {
      console.log('WebSocket connection closed');
      this.isConnected = false;
    };

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.isConnected = false;
    }
  }

  subscribe(destination, callback) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot subscribe');
      return null;
    }

    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const data = JSON.parse(message.body);
        callback(data);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
        callback(message.body);
      }
    });

    this.subscriptions.set(destination, subscription);
    return subscription;
  }

  unsubscribe(destination) {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
    }
  }

  send(destination, body) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot send');
      return;
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body)
    });
  }
}

export const wsClient = new WebSocketClient();
