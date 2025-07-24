import { useState } from "react";
import api from "../../axiosConfig";

export default function UpdateWorkflow() {
    const [name, setName] = useState("");
    const [jsonText, setJsonText] = useState("");
    const [message, setMessage] = useState("");

    const handleUpdateSubmit = async (e) => {
        e.preventDefault();
        try {
            const parsed = JSON.parse(jsonText);

            if (!Array.isArray(parsed.workflowJson?.nodes)) {
                throw new Error("❌ Invalid workflow JSON: Missing 'nodes' array.");
            }

            const payload = {
                name: parsed.name,
                workflowJson: JSON.stringify(parsed.workflowJson)
            };

            await api.put(`/workflows/${name}`, payload);
            setMessage("✅ Workflow updated successfully!");
        } catch (err) {
            setMessage("❌ Error: " + (err.response?.data || err.message));
        }
    };

    return (
        <div className="container">
            <h2>Update Existing Workflow</h2>

            <form onSubmit={handleUpdateSubmit}>
                <div style={{ marginBottom: "1rem" }}>
                    <label>Workflow Name:</label>
                    <input
                        type="text"
                        value={name}
                        required
                        onChange={(e) => setName(e.target.value)}
                        style={{ width: "300px", marginLeft: "1rem" }}
                    />
                </div>

                <div style={{ marginBottom: "1rem" }}>
                    <label>Workflow JSON:</label>
                    <br />
                    <textarea
                        value={jsonText}
                        onChange={(e) => setJsonText(e.target.value)}
                        rows="12"
                        cols="80"
                        placeholder='{
  "nodes": [
    {
      "id": 1,
      "name": "exampleNode",
      "request_url": "http://localhost/test"
    }
  ]
}'
                        required
                    />
                </div>

                <button type="submit">Update Workflow</button>
            </form>

            {message && <p style={{ marginTop: "1rem", color: "darkblue" }}>{message}</p>}
        </div>
    );
}
