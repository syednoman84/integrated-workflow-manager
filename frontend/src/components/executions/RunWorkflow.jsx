import { useState } from "react";
import api from "../../axiosConfig";

export default function RunWorkflow() {
  const [workflowName, setWorkflowName] = useState("");
  const [payload, setPayload] = useState("{}");
  const [response, setResponse] = useState(null);
  const [error, setError] = useState("");

  const handleRun = async (e) => {
  e.preventDefault();
  setError("");
  setResponse(null);

  try {
    const parsedPayload = JSON.parse(payload); 
    const res = await api.post(`/workflows/run/${workflowName}`, parsedPayload, {
      headers: {
        "Content-Type": "application/json",
      },
    });
    setResponse(res.data);
  } catch (err) {
    if (err instanceof SyntaxError) {
      setError("❌ Invalid JSON format in payload.");
    } else {
      setError(err.response?.data || err.message);
    }
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
          <label>Payload:</label>
          <textarea
            value={payload}
            onChange={(e) => setPayload(e.target.value)}
            rows="10"
            cols="80"
            
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
