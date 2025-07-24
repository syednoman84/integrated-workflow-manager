export default function Home() {
  return (
    <div className="home-container">
      <h1>🚀 Welcome to Workflow Manager</h1>
      <p>
        <strong>Workflow Manager</strong> is a lightweight, pluggable orchestration engine built to run
        complex workflows defined entirely in JSON. With just a few clicks, you can:
      </p>
      <ul>
        <li>📌 Define dynamic workflows using API-calling nodes</li>
        <li>🧠 Embed conditions and idempotency logic</li>
        <li>📂 Upload workflows via JSON or file</li>
        <li>⚙️ Execute workflows with real-time tracking</li>
        <li>📊 View execution histories and debug easily</li>
      </ul>
      <p>
        Designed for developers and product teams who need <strong>flexible, traceable, and testable</strong> workflow orchestration — without vendor lock-in or heavyweight tools.
      </p>
      <p>
        Use the navigation bar above to get started. Whether you're adding a workflow, viewing execution history, or running a test — <strong>you're in control</strong>. 💡
      </p>
    </div>
  );
}
