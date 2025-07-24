import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../axiosConfig";

export default function ViewWorkflowByName() {
  const { name } = useParams();
  const [workflow, setWorkflow] = useState(null);
  const [error, setError] = useState("");

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

  return (
    <div className="container">
      <h2>Workflow: {name}</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      {workflow ? (
        <div style={{ backgroundColor: "#f4f4f4", padding: "1rem", borderRadius: "8px" }}>
        <pre style={{ whiteSpace: "pre-wrap" }}>
            {JSON.stringify(JSON.parse(workflow.workflowJson), null, 2)}</pre></div>
      ) : (
        !error && <p>Loading...</p>
      )}
    </div>
  );
}
