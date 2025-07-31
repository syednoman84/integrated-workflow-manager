import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../axiosConfig";
import { FaCopy } from "react-icons/fa";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { oneLight } from "react-syntax-highlighter/dist/esm/styles/prism";

export default function ViewWorkflowByName() {
  const { name } = useParams();
  const [workflow, setWorkflow] = useState(null);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    const fetchWorkflow = async () => {
      try {
        const response = await api.get(`/workflows/get/${name}`);
        setWorkflow(response.data);
      } catch (err) {
        setError("❌ Failed to fetch workflow: " + (err.response?.data || err.message));
      }
    };

    fetchWorkflow();
  }, [name]);

  const handleCopy = () => {
    if (workflow?.workflowJson) {
      const json = JSON.stringify(JSON.parse(workflow.workflowJson), null, 2);
      navigator.clipboard.writeText(json);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="container">
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "1rem",
        }}
      >
        <h2 style={{ margin: 0 }}>Workflow: {name}</h2>
        <div style={{ textAlign: "right" }}>
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
            <span style={{ fontSize: "12px", color: "green", display: "block" }}>
              Copied!
            </span>
          )}
        </div>
      </div>

      {error && <p style={{ color: "red" }}>{error}</p>}

      {workflow ? (
        <div
          style={{
            backgroundColor: "#fff",
            padding: "1rem",
            borderRadius: "8px",
            boxShadow: "0 2px 8px rgba(0, 0, 0, 0.1)",
          }}
        >
          <SyntaxHighlighter language="json" style={oneLight}>
            {JSON.stringify(JSON.parse(workflow.workflowJson), null, 2)}
          </SyntaxHighlighter>
        </div>
      ) : (
        !error && <p>Loading...</p>
      )}
    </div>
  );
}
