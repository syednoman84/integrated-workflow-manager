import { useState } from "react";
import api from "../../axiosConfig";
import { FaCopy } from "react-icons/fa";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { oneLight } from "react-syntax-highlighter/dist/esm/styles/prism"; // Light theme

export default function ViewExecution() {
  const [executionId, setExecutionId] = useState("");
  const [execution, setExecution] = useState(null);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

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

  const handleCopy = () => {
    if (execution) {
      const json = JSON.stringify(execution, null, 2);
      navigator.clipboard.writeText(json);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
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
            position: "relative",
            backgroundColor: "#fff",
            padding: "1rem",
            borderRadius: "8px",
            boxShadow: "0 2px 8px rgba(0, 0, 0, 0.1)",
          }}
        >
          <div
            style={{
              position: "absolute",
              top: "10px",
              right: "10px",
              textAlign: "center",
            }}
          >
            <button
              onClick={handleCopy}
              title="Copy JSON"
              style={{
                background: "transparent",
                border: "none",
                cursor: "pointer",
                fontSize: "20px",
                color: "#555",
              }}
            >
              <FaCopy />
            </button>
            {copied && (
              <span style={{ fontSize: "12px", color: "green", marginTop: "2px", display: "block" }}>
                Copied!
              </span>
            )}
          </div>

          <SyntaxHighlighter language="json" style={oneLight}>
            {JSON.stringify(execution, null, 2)}
          </SyntaxHighlighter>
        </div>
      )}
    </div>
  );
}
