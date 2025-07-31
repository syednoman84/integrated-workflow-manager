import { useState } from "react";
import { FaCopy } from "react-icons/fa";
import api from "../../axiosConfig";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { oneLight } from "react-syntax-highlighter/dist/esm/styles/prism"; // Light theme

export default function ViewWorkflow() {
  const [name, setName] = useState("");
  const [workflow, setWorkflow] = useState(null);
  const [message, setMessage] = useState("");
  const [copied, setCopied] = useState(false);

  const handleFetch = async (e) => {
    e.preventDefault();
    setMessage("");
    setCopied(false);
    try {
      const response = await api.get(`/workflows/get/${name}`);
      setWorkflow(response.data);
    } catch (err) {
      setWorkflow(null);
      setMessage("❌ " + (err.response?.data || err.message));
    }
  };

  const handleCopy = () => {
    const json = JSON.stringify(JSON.parse(workflow.workflowJson), null, 2);
    navigator.clipboard.writeText(json);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
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
        <div
          style={{
            position: "relative",
            backgroundColor: "#fff",
            padding: "1rem",
            borderRadius: "8px",
            boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
          }}
        >
          <h4>Name: {workflow.name}</h4>

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
                  display: "block",
                  fontSize: "12px",
                  color: "green",
                  marginTop: "2px",
                }}
              >
                Copied!
              </span>
            )}
          </div>

          <SyntaxHighlighter language="json" style={oneLight}>
            {JSON.stringify(JSON.parse(workflow.workflowJson), null, 2)}
          </SyntaxHighlighter>
        </div>
      )}
    </div>
  );
}
