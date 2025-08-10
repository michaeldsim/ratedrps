import { NavLink } from "react-router-dom";
import { Home, User, Trophy } from "lucide-react";
import { useAuth } from "../auth/AuthContext";

const Navigation = () => {
  const { signOut } = useAuth();

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
