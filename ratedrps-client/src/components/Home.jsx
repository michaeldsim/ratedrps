import { Zap, Trophy, User } from "lucide-react";
import { useNavigate } from "react-router-dom";

const Home = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-900 via-blue-900 to-indigo-900 flex items-center justify-center">
      <div className="text-center text-white max-w-4xl px-6 md:px-8">
        <h1 className="text-6xl font-bold mb-6 bg-gradient-to-r from-pink-400 to-purple-400 bg-clip-text text-transparent">
          Rated RPS
        </h1>
        <p className="text-xl mb-10 text-gray-300">
          Challenge players worldwide in competitive rock-paper-scissors. Climb
          the ELO rankings and prove your strategic mastery!
        </p>

        <div className="grid md:grid-cols-3 gap-6 md:gap-8 mb-12">
          <FeatureCard
            Icon={Zap}
            title="Real-time Battles"
            description="Face opponents in lightning-fast matches."
            color="text-yellow-400"
          />
          <FeatureCard
            Icon={Trophy}
            title="ELO Rankings"
            description="Compete for the top spot on global leaderboards."
            color="text-amber-400"
          />
          <FeatureCard
            Icon={User}
            title="Player Profiles"
            description="Track your progress and match history."
            color="text-blue-400"
          />
        </div>

        <div className="flex justify-center flex-wrap gap-4">
          <button
            onClick={() => navigate("/login")}
            className="bg-purple-600 hover:bg-purple-700 px-8 py-3 rounded-lg font-semibold transition-colors"
          >
            Sign In
          </button>
          <button
            onClick={() => navigate("/register")}
            className="border-2 border-purple-400 hover:bg-purple-400/20 px-8 py-3 rounded-lg font-semibold transition-colors"
          >
            Sign Up
          </button>
        </div>
      </div>
    </div>
  );
};

const FeatureCard = ({ Icon, title, description, color }) => (
  <div className="bg-white/10 backdrop-blur-md rounded-lg p-6 hover:scale-105 transition-transform duration-300">
    <Icon className={`w-12 h-12 mx-auto mb-4 ${color}`} />
    <h3 className="text-lg font-semibold mb-2">{title}</h3>
    <p className="text-gray-300 text-sm">{description}</p>
  </div>
);

export default Home;
