import { useState } from "react";
import api from "../../axiosConfig";

export default function ViewExecution() {
  const [executionId, setExecutionId] = useState("");
  const [execution, setExecution] = useState(null);
  const [error, setError] = useState("");

  const fetchExecution = async () => {
    setError("");
    setExecution(null);
    try {
      const response = await api.get(`/workflows/executions/${executionId}`);
      setExecution(response.data);
    } catch (err) {
      setError("❌ Execution not found or error occurred.");
    }
  };

  return (
    <div className="container">
      <h2>View Execution by ID</h2>
      <div style={{ marginBottom: "1rem" }}>
        <label>Execution ID: </label>
        <input
          type="text"
          value={executionId}
          onChange={(e) => setExecutionId(e.target.value)}
          placeholder="Enter Execution ID"
          style={{ marginRight: "1rem", width: "400px" }}
        />
        <button onClick={fetchExecution}>Fetch Execution</button>
      </div>

      {error && <p style={{ color: "red" }}>{error}</p>}

      {execution && (
        <div
          style={{
            backgroundColor: "#f4f4f4",
            padding: "1rem",
            borderRadius: "8px",
            whiteSpace: "pre-wrap",
            fontFamily: "monospace"
          }}
        >
          <pre>{JSON.stringify(execution, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}
