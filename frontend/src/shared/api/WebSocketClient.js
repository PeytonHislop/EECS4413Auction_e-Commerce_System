class WebSocketClient {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.isConnected = false;
    this.stompClientClass = null;
    this.sockJSFactory = null;
    this.loadingPromise = null;
  }

  async ensureLibrariesLoaded() {
    if (this.stompClientClass && this.sockJSFactory) return;
    if (this.loadingPromise) {
      await this.loadingPromise;
      return;
    }

    this.loadingPromise = (async () => {
      const sockJsPkg = "sockjs-client";
      const stompPkg = "@stomp/stompjs";

      const [sockModule, stompModule] = await Promise.all([
        import(/* @vite-ignore */ sockJsPkg),
        import(/* @vite-ignore */ stompPkg)
      ]);

      this.sockJSFactory = sockModule.default || sockModule.SockJS;
      this.stompClientClass = stompModule.Client;
    })();

    await this.loadingPromise;
  }

  connect(onConnect, onError) {
    if (this.client && this.isConnected) {
      return;
    }

    this.ensureLibrariesLoaded()
      .then(() => {
        if (!this.stompClientClass || !this.sockJSFactory) {
          throw new Error("WebSocket libraries failed to initialize.");
        }

        const wsUrl = import.meta.env.VITE_AUCTION_WS_URL || "http://localhost:8082/ws";
        this.client = new this.stompClientClass({
          webSocketFactory: () => new this.sockJSFactory(wsUrl),
          debug: (str) => console.log(str),
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        });

        this.client.onConnect = () => {
          console.log("Connected to WebSocket");
          this.isConnected = true;
          if (onConnect) onConnect();
        };

        this.client.onStompError = (frame) => {
          console.error("STOMP error:", frame.headers["message"]);
          console.error("Details:", frame.body);
          this.isConnected = false;
          if (onError) onError(frame);
        };

        this.client.onWebSocketClose = () => {
          console.log("WebSocket connection closed");
          this.isConnected = false;
        };

        this.client.activate();
      })
      .catch((error) => {
        console.warn("WebSocket disabled:", error?.message || error);
        this.isConnected = false;
        if (onError) onError(error);
      });
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
