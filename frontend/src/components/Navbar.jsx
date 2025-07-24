import { Link } from "react-router-dom";
import "./Navbar.css";

export default function Navbar() {
  return (
    <nav className="navbar">
      <Link to="/" className="logo">Workflow Manager</Link>

      <div className="dropdown">
        <button className="dropbtn">Workflows</button>
        <div className="dropdown-content">
          <Link to="/workflows/add">Add Workflow</Link>
          <Link to="/workflows/update">Update Workflow</Link>
          <Link to="/workflows/delete">Delete Workflow</Link>
          <Link to="/workflows/view">View Workflow</Link>
          <Link to="/workflows/view-all">View All Workflows</Link>
        </div>
      </div>

      <div className="dropdown">
        <button className="dropbtn">Executions</button>
        <div className="dropdown-content">
          <Link to="/executions/view">View Execution</Link>
          <Link to="/executions/view-all">View All Executions</Link>
          <Link to="/executions/run">Run Workflow</Link>
        </div>
      </div>
    </nav>
  );
}
