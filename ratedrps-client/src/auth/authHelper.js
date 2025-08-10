import { supabase } from "../services/supabaseClient";

export async function checkUsernameAvailable(username) {
  const { data, error } = await supabase
    .from("users")
    .select("id")
    .eq("username", username)
    .maybeSingle();

  if (error) {
    console.error("Error checking username:", error.message);
    return false;
  }

  return !data;
}
