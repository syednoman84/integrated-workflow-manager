import { useEffect, useState } from "react";
import api from "../../axiosConfig";

export default function ViewAllExecutions() {
    const [executions, setExecutions] = useState([]);
    const [selectedExecution, setSelectedExecution] = useState(null);
    const [error, setError] = useState("");

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
        } catch (err) {
            setError("❌ Failed to load execution details.");
        }
    };


    return (
        <div className="container">
            <h2>All Executions</h2>
            {error && <p style={{ color: "red" }}>{error}</p>}

            <table border="1" cellPadding="8" cellSpacing="0" style={{ width: "100%", marginBottom: "2rem" }}>
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
                            <td>{exe.status}</td>
                            <td>
                                <button onClick={() => handleView(exe.executionId)}>View</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {selectedExecution && (
                <div style={{ backgroundColor: "#f4f4f4", padding: "1rem", borderRadius: "8px" }}>
                    <h3>Execution Details</h3>
                    <pre style={{ whiteSpace: "pre-wrap" }}>
                        {JSON.stringify(selectedExecution, null, 2)}
                    </pre>
                </div>
            )}
        </div>
    );
}
