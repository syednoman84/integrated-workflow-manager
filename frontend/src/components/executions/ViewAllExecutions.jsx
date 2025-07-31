import { useEffect, useState, useRef } from "react";
import api from "../../axiosConfig";
import { FaCopy } from "react-icons/fa";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { oneLight } from "react-syntax-highlighter/dist/esm/styles/prism";

export default function ViewAllExecutions() {
  const [executions, setExecutions] = useState([]);
  const [selectedExecution, setSelectedExecution] = useState(null);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);
  const detailsRef = useRef(null);

  useEffect(() => {
    const fetchExecutions = async () => {
      try {
        const res = await api.get("/workflows/executions");
        setExecutions(res.data);
      } catch (err) {
        setError("❌ Failed to fetch executions");
      }
    };
    fetchExecutions();
  }, []);

  const handleView = async (executionId) => {
    setError("");
    try {
      const response = await api.get(`/workflows/executions/${executionId}`);
      setSelectedExecution(response.data);
      setTimeout(() => {
        if (detailsRef.current) {
          detailsRef.current.scrollIntoView({ behavior: "smooth" });
        }
      }, 100);
    } catch (err) {
      setError("❌ Failed to load execution details.");
    }
  };

  const handleCopy = () => {
    if (selectedExecution) {
      const json = JSON.stringify(selectedExecution, null, 2);
      navigator.clipboard.writeText(json);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="container">
      <h2 style={{ marginBottom: "20px" }}>All Executions</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <table className="fancy-table">
        <thead>
          <tr>
            <th>Execution ID</th>
            <th>Workflow Name</th>
            <th>Executed At</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {executions.map((exe) => (
            <tr key={exe.executionId}>
              <td>{exe.executionId}</td>
              <td>{exe.workflowName}</td>
              <td>{exe.executedAt}</td>
              <td>
                <span
                  style={{
                    color: exe.status === "SUCCESS" ? "green" : "red",
                    fontWeight: "bold",
                  }}
                >
                  {exe.status}
                </span>
              </td>
              <td>
                <button className="view-button" onClick={() => handleView(exe.executionId)}>
                  View
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedExecution && (
        <div
          ref={detailsRef}
          style={{
            position: "relative",
            backgroundColor: "#fff",
            padding: "1rem",
            borderRadius: "8px",
            boxShadow: "0 2px 8px rgba(0, 0, 0, 0.1)",
            marginTop: "2rem",
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
              <span
                style={{
                  fontSize: "12px",
                  color: "green",
                  marginTop: "2px",
                  display: "block",
                }}
              >
                Copied!
              </span>
            )}
          </div>

          <h3>Execution Details</h3>
          <SyntaxHighlighter language="json" style={oneLight}>
            {JSON.stringify(selectedExecution, null, 2)}
          </SyntaxHighlighter>
        </div>
      )}
    </div>
  );
}
