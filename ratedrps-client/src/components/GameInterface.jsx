import { useEffect, useState, useRef } from "react";
import { Play } from "lucide-react";
import { useAuth } from "../auth/AuthContext";
import GameService from "../services/gameService";

const GameInterface = () => {
  const { session } = useAuth();
  const userId = session?.user?.id;
  const gameServiceRef = useRef();

  const [gameState, setGameState] = useState("waiting"); // waiting, playing, finished
  const [playerMove, setPlayerMove] = useState(null);
  const [opponentUsername, setOpponentUsername] = useState(null);
  const [opponentMove, setOpponentMove] = useState(null);
  const [gameResult, setGameResult] = useState(null);
  const [isConnecting, setIsConnecting] = useState(false);
  const [gameId, setGameId] = useState(null);

  useEffect(() => {
    gameServiceRef.current = new GameService(session?.access_token);
    gameServiceRef.current.setCallbacks({
      onLobbyUpdate: (data) => {
        // Handle lobby updates if needed - leaving this empty for now
      },

      onMatchFound: (data) => {
        setGameId(data.gameId);
        setGameState("playing");
        setIsConnecting(false);
        setPlayerMove(null);
        setOpponentUsername(data.opponentUsername);
        setOpponentMove(null);
        setGameResult(null);
      },

      onGameUpdate: (data) => {
        if (data.result) {
          const isPlayer1 = userId === data.player1Id;
          const elo_delta = isPlayer1 ? data.player1EloDelta : data.player2EloDelta;

          let result;
          if (data.result === userId) {
            result = "win";
          } else if (data.result === "draw") {
            result = "draw";
          } else {
            result = "lose";
          }

          setGameResult({ result, elo_delta });
          setGameState("finished");
        }
      },

      onError: (error) => {
        console.error("Game error", error);
        setIsConnecting(false);
      },

      onDisconnect: () => {
        setIsConnecting(false);
        setGameState("waiting");
      },
    });

    return () => {
    gameServiceRef.current?.disconnect();
  };
}, [session?.access_token, userId]);

  const startGame = async () => {
    if (!userId) return;
    setIsConnecting(true);
    await gameServiceRef.current.connect();
    gameServiceRef.current.joinLobby(userId, session.user.user_metadata.username);
  };

  const makeMove = (move) => {
    if (!gameId || !userId) return;
    setPlayerMove(move);
    gameServiceRef.current.sendMove(gameId, userId, move);
  };

  const resetGame = () => {
    setGameState("waiting");
    setPlayerMove(null);
    setOpponentMove(null);
    setGameResult(null);
    setGameId(null);
    gameServiceRef.current.leaveLobby(userId);
  };

  const renderMoveIcon = (move) => {
    const icons = {
      rock: "🪨",
      paper: "📄",
      scissors: "✂️",
    };
    return <span className="text-4xl">{icons[move] || "?"}</span>;
  };

  const renderResultMessage = () => {
    switch (gameResult.result) {
      case "win":
        return (
          <div className="flex flex-col items-center text-center">
            <span className="text-green-600">Victory! 🎉</span>
            <span>+{gameResult.elo_delta}</span>
          </div>
        );
      case "lose":
        return (
          <div className="flex flex-col items-center text-center">
            <span className="text-red-600">Defeat 😔</span>
            <span>{gameResult.elo_delta}</span>
          </div>
        );
      case "draw":
        return (
          <div className="flex flex-col items-center text-center">
            <span className="text-blue-600">Draw! 🤝</span>
            <span>{gameResult.elo_delta}</span>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h2 className="text-3xl font-bold text-center mb-8 text-gray-800">
        Battle Arena
      </h2>

      {gameState === "waiting" && (
        <div className="text-center">
          <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
            <h3 className="text-xl font-semibold mb-4">Ready to Battle?</h3>
            <p className="text-gray-600 mb-6">
              Find an opponent and test your skills!
            </p>
            <p className="text-sm text-gray-500 mb-4">
              The server for this application is hosted using the Render free tier, so it may take up to a minute to connect based on how recently the server was last hit. The status indictor above will turn green when the server is ready.
            </p>
            <button
              onClick={startGame}
              disabled={isConnecting}
              className="bg-purple-600 hover:bg-purple-700 disabled:bg-gray-400 text-white px-8 py-3 rounded-lg font-semibold transition-colors flex items-center gap-2 mx-auto"
            >
              <Play className="w-5 h-5" />
              {isConnecting ? "Finding Opponent..." : "Find Match"}
            </button>
          </div>
        </div>
      )}

      {gameState === "playing" && (
        <div className="bg-white rounded-xl shadow-lg p-8">
          <div className="grid md:grid-cols-2 gap-8 mb-8">
            <div className="text-center">
              <h3 className="text-lg font-semibold mb-4">You</h3>
              <div className="w-32 h-32 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                {playerMove ? (
                  renderMoveIcon(playerMove)
                ) : (
                  <span className="text-gray-400">?</span>
                )}
              </div>
              <p className="text-sm text-gray-600">Your Move</p>
            </div>
            <div className="text-center">
              <h3 className="text-lg font-semibold mb-4">{opponentUsername}</h3>
              <div className="w-32 h-32 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                {opponentMove ? (
                  renderMoveIcon(opponentMove)
                ) : (
                  <span className="text-gray-400">?</span>
                )}
              </div>
              <p className="text-sm text-gray-600">Opponent's Move</p>
            </div>
          </div>

          {!playerMove && (
            <div className="text-center">
              <p className="mb-6 text-lg">Choose your move:</p>
              <div className="flex justify-center gap-4">
                {["rock", "paper", "scissors"].map((move) => (
                  <button
                    key={move}
                    onClick={() => makeMove(move)}
                    className="bg-gray-100 hover:bg-gray-200 p-6 rounded-xl transition-colors"
                  >
                    {renderMoveIcon(move)}
                    <div className="mt-2 font-semibold capitalize">{move}</div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {playerMove && !opponentMove && (
            <div className="text-center">
              <div className="animate-spin w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full mx-auto mb-4"></div>
              <p className="text-gray-600">Waiting for opponent...</p>
            </div>
          )}
        </div>
      )}

      {gameState === "finished" && (
        <div className="bg-white rounded-xl shadow-lg p-8 text-center">
          <div className="mb-6">
            <h3 className="text-2xl font-bold mb-4">{renderResultMessage()}</h3>
            <p className="text-gray-600 mb-4">
              {playerMove && opponentMove && `${playerMove} vs ${opponentMove}`}
            </p>
          </div>
          <button
            onClick={resetGame}
            className="bg-purple-600 hover:bg-purple-700 text-white px-8 py-3 rounded-lg font-semibold transition-colors"
          >
            Play Again
          </button>
        </div>
      )}
    </div>
  );
};

export default GameInterface;
