import { useState } from "react";
import api from "../../axiosConfig";

export default function AddWorkflow() {
    const [jsonText, setJsonText] = useState("");
    const [file, setFile] = useState(null);
    const [message, setMessage] = useState("");

    const handleTextSubmit = async (e) => {
        e.preventDefault();
        try {
            const parsed = JSON.parse(jsonText);

            if (!Array.isArray(parsed.workflowJson?.nodes)) {
                throw new Error("❌ Invalid workflow JSON: Missing 'nodes' array.");
            }

            const payload = {
                name: parsed.name,
                workflowJson: JSON.stringify(parsed.workflowJson), // 🔁 match Postman format
            };

            await api.post("/workflows", payload);
            setMessage("✅ Workflow added successfully!");
        } catch (err) {
            setMessage("❌ Error: " + (err.response?.data || err.message));
        }
    };


    const handleFileSubmit = async (e) => {
        e.preventDefault();
        try {
            const formData = new FormData();
            formData.append("file", file);

            await api.post("/workflows/fileupload", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
            setMessage("✅ Workflow uploaded from file successfully!");
        } catch (err) {
            setMessage("❌ Error: " + err.response?.data || err.message);
        }
    };

    return (
        <div className="container">
            <h2>Add New Workflow</h2>

            <form onSubmit={handleTextSubmit} style={{ marginBottom: "2rem" }}>
                <div>
                    <label>Workflow JSON:</label>
                    <textarea
                        value={jsonText}
                        onChange={(e) => setJsonText(e.target.value)}
                        rows="10"
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
                <button type="submit">Add Workflow</button>
            </form>

            <form onSubmit={handleFileSubmit}>
                <div>
                    <label>Upload Workflow JSON File:</label>
                    <input
                        type="file"
                        accept=".json"
                        onChange={(e) => setFile(e.target.files[0])}
                        required
                    />
                </div>
                <button type="submit">Upload Workflow</button>
            </form>

            {message && <p style={{ marginTop: "1rem", color: "darkblue" }}>{message}</p>}
        </div>
    );
}
