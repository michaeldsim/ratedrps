class GameService {
  constructor(token) {
    this.ws = null;
    this.isConnected = false;
    this.token = token;
    this.callbacks = {};
  }

  connect() {
    return new Promise((resolve, reject) => {
      if (this.isConnected) return resolve();

      this.ws = new WebSocket(`ws://localhost:8080/ws/game?token=${this.token}`);

      this.ws.onopen = () => {
        console.log("WebSocket connected");
        this.isConnected = true;
        resolve();
      };

      this.ws.onmessage = (event) => {
        console.log(event.data);
        const msg = JSON.parse(event.data);
        this.handleMessage(msg);
      };

      this.ws.onerror = (err) => {
        console.error("WebSocket error", err);
        reject(err);
      };

      this.ws.onclose = () => {
        console.log("WebSocket disconnected");
        this.isConnected = false;
        if (this.callbacks.onDisconnect) this.callbacks.onDisconnect();
      };
    });
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
      this.isConnected = false;
    }
  }

  sendMessage(message) {
    if (this.ws && this.isConnected) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.error("Cannot send message, WebSocket not connected");
    }
  }

  handleMessage(message) {
    const { type, data } = message;

    switch (type) {
      case "LOBBY_UPDATE":
        this.callbacks.onLobbyUpdate && this.callbacks.onLobbyUpdate(data);
        break;
      case "MATCH_FOUND":
        this.callbacks.onMatchFound && this.callbacks.onMatchFound(data);
        break;
      case "GAME_UPDATE":
        this.callbacks.onGameUpdate && this.callbacks.onGameUpdate(data);
        break;
      case "ERROR":
        this.callbacks.onError && this.callbacks.onError(data);
        break;
      default:
        console.warn("Unknown message type", type);
    }
  }

  setCallbacks(callbacks) {
    this.callbacks = callbacks;
  }

  joinLobby(userId, username) {
    this.connect().then(() => {
      this.sendMessage({ type: "JOIN_LOBBY", userId, username });
    });
  }

  leaveLobby(userId) {
    this.sendMessage({ type: "LEAVE_LOBBY", userId });
  }

  sendMove(gameId, userId, move) {
    this.sendMessage({ type: "MAKE_MOVE", gameId, userId, move });
  }
}

export default GameService;
