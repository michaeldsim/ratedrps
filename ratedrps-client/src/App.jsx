import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./auth/AuthContext";

import Home from "./components/Home";
import Login from "./components/Login";
import Register from "./components/Register";
import Dashboard from "./components/Dashboard";
import GameInterface from "./components/GameInterface";
import Profile from "./components/Profile";
import Leaderboard from "./components/Leaderboard";
import User from "./components/User";

function App() {
  const { session } = useAuth();

  return (
    <Routes>
      <Route
        path="/"
        element={session ? <Navigate to="/dashboard" replace /> : <Home />}
      />

      {!session && (
        <>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </>
      )}

      {session && (
        <Route path="/dashboard" element={<Dashboard />}>
          <Route index element={<Navigate to="play" replace />} />
          <Route path="play" element={<GameInterface />} />
          <Route path="profile" element={<Profile />} />
          <Route path="leaderboard" element={<Leaderboard />} />
        </Route>
      )}
      <Route path="/user/:username" element={<User />} />
      <Route
        path="*"
        element={<Navigate to={session ? "/dashboard" : "/"} replace />}
      />
    </Routes>
  );
}

export default App;
