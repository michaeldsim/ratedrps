import { useState, useEffect } from "react";
import { BarChart3, Trophy } from "lucide-react";
import { useAuth } from "../auth/AuthContext";
import { userService } from "../services/userService";
import { Link } from "react-router-dom";
import { supabase } from "../services/supabaseClient";

const Leaderboard = () => {
  const { session } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const currentUser = session?.user;

  useEffect(() => {
    const fetchLeaderboard = async () => {
      try {
        const leaderboard = await userService.getLeaderboard();
        setUsers(leaderboard);
      } catch (error) {
        console.error("Failed to fetch leaderboard:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchLeaderboard();
  }, [session]);

  const calculateWinRate = (wins, losses) => {
    const totalGames = wins + losses;
    if (totalGames === 0) return 0;
    return ((wins / totalGames) * 100).toFixed(1);
  };

  const getTrophyColor = (index) => {
    switch (index) {
      case 0:
        return "text-yellow-500"; // Gold
      case 1:
        return "text-gray-400"; // Silver
      case 2:
        return "text-yellow-600"; // Bronze
      default:
        return "text-transparent";
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="max-w-4xl mx-auto text-center">
          <div className="animate-spin w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full mx-auto mb-4"></div>
          <p className="text-gray-600">Loading leaderboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="max-w-4xl mx-auto">
        <h2 className="text-3xl font-bold text-center mb-8 text-gray-800 flex items-center justify-center gap-2">
          <BarChart3 className="w-8 h-8" />
          Global Leaderboard
        </h2>

        <div className="bg-white rounded-xl shadow-lg overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-purple-600 text-white">
                <tr>
                  <th className="px-6 py-4 text-left">Rank</th>
                  <th className="px-6 py-4 text-left">Player</th>
                  <th className="px-6 py-4 text-left">ELO</th>
                  <th className="px-6 py-4 text-left">Wins</th>
                  <th className="px-6 py-4 text-left">Losses</th>
                  <th className="px-6 py-4 text-left">Win Rate</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {users.map((user, index) => (
                  <tr
                    key={user.id}
                    className={
                      currentUser?.id === user.id
                        ? "bg-purple-50"
                        : "hover:bg-gray-50"
                    }
                  >
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        {index < 3 && (
                          <Trophy
                            className={`w-5 h-5 ${getTrophyColor(index)}`}
                          />
                        )}
                        <span className="font-semibold">#{index + 1}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full flex items-center justify-center bg-purple-600 overflow-hidden">
                          {user.avatar_url ? (
                            <img
                              src={
                                user.avatar_url.startsWith("http")
                                  ? user.avatar_url
                                  : supabase.storage
                                      .from("avatars")
                                      .getPublicUrl(user.avatar_url).data.publicUrl
                              }
                              alt={user.username}
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <span className="text-white text-sm font-bold">
                              {user.username.charAt(0).toUpperCase()}
                            </span>
                          )}
                        </div>
                        <span className="font-semibold">
                          {currentUser?.id === user.id ? (
                            <>
                              {user.username}
                              <span className="ml-2 text-sm text-purple-600">(You)</span>
                            </>
                          ) : (
                            <Link
                              to={`/user/${user.username}`}
                              className="text-blue-600 hover:underline"
                            >
                              {user.username}
                            </Link>
                          )}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="font-bold text-purple-600 text-lg">
                        {user.elo}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-green-600 font-semibold">
                        {user.wins}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-red-600 font-semibold">
                        {user.losses}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <span className="font-semibold">
                          {calculateWinRate(user.wins, user.losses)}%
                        </span>
                        <div className="w-12 bg-gray-200 rounded-full h-2">
                          <div
                            className="bg-purple-600 h-2 rounded-full"
                            style={{
                              width: `${calculateWinRate(
                                user.wins,
                                user.losses
                              )}%`,
                            }}
                          ></div>
                        </div>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {currentUser && (
          <div className="mt-6 bg-gradient-to-r from-purple-100 to-blue-100 rounded-lg p-4">
            <h3 className="text-lg font-semibold text-gray-800 mb-2">
              Your Ranking
            </h3>
            <p className="text-gray-600">
              You are currently ranked{" "}
              <span className="font-bold text-purple-600">
                #{users.findIndex((user) => user.id === currentUser.id) + 1}
              </span>{" "}
              out of {users.length} players
              <span className="font-bold text-purple-600">
                {currentUser.elo}
              </span>
              .
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Leaderboard;
