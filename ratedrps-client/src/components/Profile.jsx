import { useState, useEffect, useRef } from "react";
import { Trophy, Clock, Camera } from "lucide-react";
import { useAuth } from "../auth/AuthContext";
import { userService } from "../services/userService";
import { supabase } from "../services/supabaseClient"; 
import { Link } from "react-router-dom";

const Profile = () => {
  const { session } = useAuth();
  const [profile, setProfile] = useState(null);
  const [matches, setMatches] = useState([]);
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [loadingMatches, setLoadingMatches] = useState(true);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [profileImageUrl, setProfileImageUrl] = useState(null);
  const fileInputRef = useRef(null);
  const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

  useEffect(() => {
    const fetchProfile = async () => {
      if (!session?.user) {
        setProfile(null);
        setLoadingProfile(false);
        return;
      }

      try {
        const userProfile = await userService.getUserById(session.user.id);
        console.log("Fetched user profile:", userProfile);
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
  }, [session]);

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

  const handleImageUpload = async (event) => {
    const file = event.target.files[0];
    if (!file || !session?.user) return;

    if (!file.type.startsWith('image/')) {
      alert('Please select an image file');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert('File size must be less than 5MB');
      return;
    }

    setUploadingImage(true);

    try {
      const formData = new FormData();
      formData.append('avatar', file);
      formData.append('userId', session.user.id);

      const response = await fetch(`${apiUrl}/users/upload-avatar`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${session.access_token}`,
        },
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to upload image');
      }

      const result = await response.json();
      
      setProfileImageUrl(result.avatarUrl);
      setProfile(prev => ({ ...prev, avatar_url: result.avatarPath }));

      console.log('Profile image updated successfully');
    } catch (error) {
      console.error('Error uploading image:', error);
      alert('Failed to upload image. Please try again.');
    } finally {
      setUploadingImage(false);
    }
  };

  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  if (!session) {
    return <div>Please log in to view your profile.</div>;
  }

  if (loadingProfile) {
    return (
      <div className="text-center py-8">
        <div className="animate-spin w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full mx-auto mb-4"></div>
        <p className="text-gray-600">Loading profile...</p>
      </div>
    );
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getOpponentName = (match) => {
    return match.player1_id === session.user.id
      ? match.player2_username
      : match.player1_username;
  };

  const didUserWin = (match) => {
    return match.winner_id === session.user.id;
  };

  const getMatchResult = (match) => {
    if (!match.winner_id) return "Draw";
    return didUserWin(match) ? "Won" : "Lost";
  };

  const getMatchMoves = (match) => {
    if (match.player1_id === session.user.id) {
      return `${match.player1_move} vs ${match.player2_move}`;
    }
    return `${match.player2_move} vs ${match.player1_move}`;
  };

  return (
    <div className="p-6">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
          <div className="flex items-center gap-6 mb-6">
            <div className="relative">
              <div className="w-20 h-20 bg-purple-600 rounded-full flex items-center justify-center overflow-hidden">
                {profileImageUrl ? (
                  <img
                    src={profileImageUrl}
                    alt={`${profile.username}'s avatar`}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="text-white text-2xl font-bold">
                    {profile.username?.charAt(0).toUpperCase()}
                  </div>
                )}
              </div>
              
              {/* Upload button overlay */}
              <button
                onClick={triggerFileInput}
                disabled={uploadingImage}
                className="absolute inset-0 w-20 h-20 bg-black bg-opacity-50 rounded-full flex items-center justify-center opacity-0 hover:opacity-100 transition-opacity duration-200 text-white"
              >
                {uploadingImage ? (
                  <div className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" />
                ) : (
                  <Camera className="w-5 h-5" />
                )}
              </button>
              
              {/* Hidden file input */}
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleImageUpload}
                className="hidden"
              />
            </div>
            
            <div>
              <h2 className="text-3xl font-bold text-gray-800">
                {profile.username}
              </h2>
              <p className="text-gray-600">ELO Rating: {profile.elo}</p>
              <p className="text-sm text-gray-500 mt-1">
                Click avatar to change picture
              </p>
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
              No matches found. Play some games to see your history!
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
                        match.winner_id === session.user.id
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
  );
};

export default Profile;