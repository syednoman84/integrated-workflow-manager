import { useState } from "react";
import api from "../../axiosConfig";

export default function RunWorkflow() {
  const [workflowName, setWorkflowName] = useState("");
  const [applicationId, setApplicationId] = useState("");
  const [response, setResponse] = useState(null);
  const [error, setError] = useState("");

  const handleRun = async (e) => {
    e.preventDefault();
    setError("");
    setResponse(null);
    try {
      const payload = { applicationId };
      const res = await api.post(`/workflows/run/${workflowName}`, payload);
      setResponse(res.data);
    } catch (err) {
      setError(err.response?.data || err.message);
    }
  };

  return (
    <div className="container">
      <h2>Run Workflow</h2>
      <form onSubmit={handleRun}>
        <div>
          <label>Workflow Name:</label>
          <input
            type="text"
            value={workflowName}
            onChange={(e) => setWorkflowName(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Application ID:</label>
          <input
            type="text"
            value={applicationId}
            onChange={(e) => setApplicationId(e.target.value)}
            required
          />
        </div>
        <button type="submit">Run</button>
      </form>

      {error && <p style={{ color: "red", marginTop: "1rem" }}>❌ {error}</p>}
      {response && (
        <div
          style={{
            backgroundColor: "#f4f4f4",
            padding: "1rem",
            marginTop: "1rem",
            borderRadius: "8px",
            whiteSpace: "pre-wrap",
          }}
        >
          <pre>{JSON.stringify(response, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}
