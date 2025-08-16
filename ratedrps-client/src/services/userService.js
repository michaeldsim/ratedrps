import { supabase } from "../services/supabaseClient";

export const userService = {
  async getUserById(userId) {
    try {
      const { data, error } = await supabase
        .from("users")
        .select("*")
        .eq("id", userId)
        .single();

      if (error) throw error;
      return data;
    } catch (error) {
      console.error("Error fetching user:", error);
      throw error;
    }
  },

  async getUserByUsername(username) {
    try {
      const { data, error } = await supabase
        .from("users")
        .select("*")
        .eq("username", username)
        .single();

      if (error) throw error;
      return data;
    } catch (error) {
      console.error("Error fetching user:", error);
      throw error;
    }
  },

  async getLeaderboard() {
    try {
      const { data, error } = await supabase
        .from("users")
        .select("id, username, elo, wins, losses, draws, avatar_url")
        .order("elo", { ascending: false })
        .limit(10);

      if (error) throw error;
      return data;
    } catch (error) {
      console.error("Error fetching leaderboard:", error);
      throw error;
    }
  },

  async getUserMatches(userId) {
    try {
      const { data, error } = await supabase
        .from("game_matches")
        .select("*")
        .or(`player1_id.eq.${userId},player2_id.eq.${userId}`)
        .order("created_at", { ascending: false });

      if (error) throw error;
      return data;
    } catch (error) {
      console.error("Error fetching user matches:", error);
      throw error;
    }
  },

  async getRecentMatches() {
    try {
      const { data, error } = await supabase
        .from("game_matches")
        .select("*")
        .order("created_at", { ascending: false })
        .limit(10);

      if (error) throw error;
      return data;
    } catch (error) {
      console.error("Error fetching recent matches:", error);
      throw error;
    }
  },
};
