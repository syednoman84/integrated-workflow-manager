import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../../axiosConfig";

export default function ViewAllWorkflows() {
  const [workflowNames, setWorkflowNames] = useState([]);

  useEffect(() => {
    const fetchWorkflowNames = async () => {
      try {
        const response = await api.get("/workflows/get/all");
        setWorkflowNames(response.data); // expecting list of names
      } catch (err) {
        console.error("Error fetching workflow names:", err);
      }
    };
    fetchWorkflowNames();
  }, []);

 return (
  <div className="container">
    <h2 style={{ marginBottom: "20px" }}>All Workflows</h2>
    {workflowNames.length === 0 ? (
      <p>No workflows found.</p>
    ) : (
      <table className="fancy-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Workflow Name</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {workflowNames.map((name, index) => (
            <tr key={name}>
              <td>{index + 1}</td>
              <td>{name}</td>
              <td>
                <Link to={`/workflows/view/${name}`}>
                  <button className="view-button">View</button>
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    )}
  </div>
);
}
