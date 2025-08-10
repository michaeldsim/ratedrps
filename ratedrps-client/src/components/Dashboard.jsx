import Navigation from "./Navigation";
import { Outlet } from "react-router-dom";

const Dashboard = () => {
  return (
    <div className="min-h-screen bg-gray-100">
      <Navigation />
      <main>
        <Outlet />
      </main>
    </div>
  );
};

export default Dashboard;
