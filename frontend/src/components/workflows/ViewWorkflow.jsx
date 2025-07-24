import { useState } from "react";
import api from "../../axiosConfig";

export default function ViewWorkflow() {
  const [name, setName] = useState("");
  const [workflow, setWorkflow] = useState(null);
  const [message, setMessage] = useState("");

  const handleFetch = async (e) => {
    e.preventDefault();
    try {
      const response = await api.get(`/workflows/get/${name}`);
      setWorkflow(response.data);
      setMessage("");
    } catch (err) {
      setWorkflow(null);
      setMessage("❌ " + (err.response?.data || err.message));
    }
  };

  return (
    <div className="container">
      <h2>View Workflow</h2>

      <form onSubmit={handleFetch} style={{ marginBottom: "1rem" }}>
        <label>Workflow Name:</label>
        <input
          type="text"
          value={name}
          required
          onChange={(e) => setName(e.target.value)}
          style={{ width: "300px", marginLeft: "1rem" }}
        />
        <button type="submit" style={{ marginLeft: "1rem" }}>
          Fetch Workflow
        </button>
      </form>

      {message && <p style={{ color: "darkred" }}>{message}</p>}

      {workflow && (
        <div style={{ backgroundColor: "#f4f4f4", padding: "1rem", borderRadius: "8px" }}>
          <h4>Name: {workflow.name}</h4>
          <pre style={{ whiteSpace: "pre-wrap" }}>
            {JSON.stringify(JSON.parse(workflow.workflowJson), null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
}
