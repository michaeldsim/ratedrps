import Navigation from "./Navigation";
import { Outlet } from "react-router-dom";

const Dashboard = () => {
  return (
    <div>
      <Navigation />
      <main>
        <Outlet />
      </main>
    </div>
  );
};

export default Dashboard;
