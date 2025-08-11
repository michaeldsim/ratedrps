import { NavLink } from "react-router-dom";
import { Home, User, Trophy, Circle } from "lucide-react";
import { useAuth } from "../auth/AuthContext";
import { useEffect, useState } from "react";

const Navigation = () => {
  const { signOut } = useAuth();
  const [isUp, setIsUp] = useState(null);


  useEffect(() => {
    const checkStatus = async () => {
      const apiUrl = import.meta.env.VITE_API_URL || "/api";
      
      try {
        const res = await fetch(`${apiUrl}/health_check`, { cache: "no-store" });
        if (res.ok) {
          setIsUp(true);
        } else {
          setIsUp(false);
        }
      } catch {
        setIsUp(false);
      }
    };

    checkStatus();
    const interval = setInterval(checkStatus, 10000);
    return () => clearInterval(interval);
  }, []);

  const navItems = [
    { to: "/dashboard/play", label: "Play", icon: Home },
    { to: "/dashboard/profile", label: "Profile", icon: User },
    { to: "/dashboard/leaderboard", label: "Leaderboard", icon: Trophy },
  ];

  return (
    <nav className="bg-white shadow-sm border-b">
      <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
        <h1
          className="text-2xl font-bold text-purple-600 cursor-pointer"
          onClick={() => (window.location.href = "/dashboard")}
        >
          Rated RPS
        </h1>

        <div className="flex items-center gap-6">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-2 px-4 py-2 rounded-lg transition-colors ${
                  isActive
                    ? "bg-purple-100 text-purple-600"
                    : "hover:bg-gray-100 text-gray-800"
                }`
              }
            >
              <Icon className="w-5 h-5" />
              {label}
            </NavLink>
          ))}

          <div
            className="flex items-center gap-2 px-4 py-2 rounded-lg text-gray-800"
            title={
              isUp === null
                ? "Checking server..."
                : isUp
                ? "Server is running"
                : "Server is down"
            }
          >
            <Circle
              className={`w-3 h-3 ${
                isUp === null
                  ? "text-gray-400"
                  : isUp
                  ? "text-green-500"
                  : "text-red-500"
              }`}
              fill="currentColor"
            />
            <span className="text-sm">
              {isUp === null ? "Checking..." : isUp ? "Online" : "Offline"}
            </span>
          </div>

          <button
            onClick={signOut}
            className="gap-2 px-4 py-2 rounded-lg transition-colors text-gray-600 hover:bg-gray-100 hover:text-gray-800"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navigation;
