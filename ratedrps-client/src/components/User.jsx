import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { User as UserIcon, Trophy, Clock } from "lucide-react";
import { userService } from "../services/userService";
import { supabase } from "../services/supabaseClient";
import Navigation from "./Navigation";

const User = () => {
  const { username } = useParams();
  const [profile, setProfile] = useState(null);
  const [matches, setMatches] = useState([]);
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [loadingMatches, setLoadingMatches] = useState(true);
  const [profileImageUrl, setProfileImageUrl] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const userProfile = await userService.getUserByUsername(username);
        setProfile(userProfile);

        if (userProfile.avatar_url) {
                  const { data } = supabase.storage
                    .from('avatars')
                    .getPublicUrl(userProfile.avatar_url);
                  setProfileImageUrl(data.publicUrl);
        }
      } catch (error) {
        console.error("Failed to fetch user profile:", error);
      } finally {
        setLoadingProfile(false);
      }
    };

    fetchProfile();
  }, [username]);

  useEffect(() => {
    const fetchUserMatches = async () => {
      if (profile) {
        try {
          const userMatches = await userService.getUserMatches(profile.id);
          setMatches(userMatches);
        } catch (error) {
          console.error("Failed to fetch matches:", error);
        } finally {
          setLoadingMatches(false);
        }
      }
    };

    fetchUserMatches();
  }, [profile]);

  if (loadingProfile) {
    return (
      <div className="text-center py-8">
        <div className="animate-spin w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full mx-auto mb-4"></div>
        <p className="text-gray-600">Loading profile...</p>
      </div>
    );
  }

  if (!profile) {
    return <div>Why is this getting hit</div>;
  }

  const formatDate = (dateString) =>
    new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });

  const getOpponentName = (match) =>
    match.player1_id === profile.id
      ? match.player2_username
      : match.player1_username;

  const getMatchResult = (match) => {
    if (!match.winner_id) return "Draw";
    return match.winner_id === profile.id ? "Won" : "Lost";
  };

  const getMatchMoves = (match) =>
    match.player1_id === profile.id
      ? `${match.player1_move} vs ${match.player2_move}`
      : `${match.player2_move} vs ${match.player1_move}`;

  return (
    <>
    <Navigation />
    <div className="p-6">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
          <div className="flex items-center gap-6 mb-6">
            <div className="w-20 h-20 bg-purple-600 rounded-full flex items-center justify-center">
              {profileImageUrl ? (
                <img
                  src={profileImageUrl}
                  alt={profile.username}
                  className="w-full h-full rounded-full object-cover"
                />
                ) : (
                <UserIcon className="w-10 h-10 text-white" />
                ) 
              }
            </div>
            <div>
              <h2 className="text-3xl font-bold text-gray-800">
                {profile.username}
              </h2>
              <p className="text-gray-600">ELO Rating: {profile.elo}</p>
            </div>
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            <div className="bg-green-50 p-4 rounded-lg text-center">
              <Trophy className="w-8 h-8 text-green-600 mx-auto mb-2" />
              <div className="text-2xl font-bold text-green-600">
                {profile.wins}
              </div>
              <div className="text-sm text-gray-600">Wins</div>
            </div>
            <div className="bg-red-50 p-4 rounded-lg text-center">
              <div className="text-2xl font-bold text-red-600">
                {profile.losses}
              </div>
              <div className="text-sm text-gray-600">Losses</div>
            </div>
            <div className="bg-blue-50 p-4 rounded-lg text-center">
              <div className="text-2xl font-bold text-blue-600">
                {profile.draws}
              </div>
              <div className="text-sm text-gray-600">Draws</div>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-lg p-8">
          <h3 className="text-xl font-bold mb-6 flex items-center gap-2">
            <Clock className="w-5 h-5" />
            Recent Matches
          </h3>

          {loadingMatches ? (
            <div className="text-center py-8">
              <div className="animate-spin w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full mx-auto mb-4"></div>
              <p className="text-gray-600">Loading matches...</p>
            </div>
          ) : matches.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              No matches found. Play some games to see match history!
            </div>
          ) : (
            <div className="space-y-4">
              {matches.map((match) => (
                <div
                  key={match.id}
                  className="flex items-center justify-between p-4 bg-gray-50 rounded-lg"
                >
                  <div>
                    <div className="font-semibold">
                      vs{" "}
                      <Link
                        to={`/user/${getOpponentName(match)}`}
                        className="text-purple-600 hover:underline"
                      >
                        {getOpponentName(match)}
                      </Link>
                    </div>
                    <div className="text-sm text-gray-600">
                      {getMatchMoves(match)}
                    </div>
                  </div>
                  <div className="text-right">
                    <div
                      className={`font-semibold ${
                        match.winner_id === profile.id
                          ? "text-green-600"
                          : match.winner_id === null
                          ? "text-blue-600"
                          : "text-red-600"
                      }`}
                    >
                      {getMatchResult(match)}
                    </div>
                    <div className="text-sm text-gray-600">
                      {formatDate(match.created_at)}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
    </>
  );
};

export default User;
