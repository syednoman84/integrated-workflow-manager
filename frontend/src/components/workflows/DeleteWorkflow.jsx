import { useState } from "react";
import api from "../../axiosConfig";

export default function DeleteWorkflow() {
  const [name, setName] = useState("");
  const [message, setMessage] = useState("");

  const handleDelete = async (e) => {
    e.preventDefault();
    try {
      const response = await api.delete(`/workflows/${name}`);
      setMessage("✅ " + response.data);
    } catch (err) {
      const msg = err.response?.status === 409
        ? err.response.data
        : "❌ Error: " + (err.response?.data || err.message);
      setMessage(msg);
    }
  };

  return (
    <div className="container">
      <h2>Delete Workflow</h2>

      <form onSubmit={handleDelete}>
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

        <button type="submit" style={{ backgroundColor: "#e74c3c", color: "white" }}>
          Delete Workflow
        </button>
      </form>

      {message && <p style={{ marginTop: "1rem", color: "darkred" }}>{message}</p>}
    </div>
  );
}
