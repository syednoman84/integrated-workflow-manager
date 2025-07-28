# Workflow Manager

A lightweight, pluggable workflow orchestration engine built with **Spring Boot** and **React**, designed to execute complex workflows defined entirely in **JSON**. Featuring built-in support for **dynamic expression evaluation via MVEL**, **idempotency**, and **retries**, Workflow Manager enables traceable, testable, and flexible automation.

---

## ✨ Features

- ✅ Define and manage JSON-based workflows
- ✅ API-calling nodes with dynamic parameters
- ✅ Conditional execution with MVEL expressions
- ✅ Retry support for individual nodes
- ✅ Idempotency to skip previously executed steps
- ✅ Full execution history tracking and debugging
- ✅ Swagger UI for API exploration
- ✅ React frontend with fancy UI for managing workflows and executions

---

## 🚀 Getting Started

### Backend

- Java 17+, Spring Boot
- MySQL database
- Run: `./mvnw spring-boot:run`
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Frontend

- React + Axios
- Run: `npm install && npm start`
- Base URL: [http://localhost:3000](http://localhost:3000)

---

## 🔹 API Endpoints

### ✏️ Workflow CRUD

| Method | Endpoint                    | Description                     |
| ------ | --------------------------- | ------------------------------- |
| POST   | `/api/workflows`            | Add a new workflow (via JSON)   |
| PUT    | `/api/workflows/{name}`     | Update an existing workflow     |
| DELETE | `/api/workflows/{name}`     | Delete a workflow by name       |
| POST   | `/api/workflows/fileupload` | Upload workflow using JSON file |
| GET    | `/api/workflows/get/{name}` | Fetch a specific workflow       |
| GET    | `/api/workflows/get/all`    | Fetch all workflow names        |

### ⏱️ Workflow Execution

| Method | Endpoint                                  | Description                                          |
| ------ | ----------------------------------------- | ---------------------------------------------------- |
| POST   | `/api/workflows/run/{name}`               | Execute a workflow with input parameters             |
| GET    | `/api/workflows/executions`               | View all workflow executions                         |
| GET    | `/api/workflows/executions/{executionId}` | View a specific execution with full step-level trace |

---

## ⚖️ MVEL Expression Support

We use [MVEL](https://github.com/mvel/mvel) to dynamically evaluate conditions and expressions inside workflow JSON.

### Example Usage

```json
{
  "condition": "user.age >= 18"
}
```

### Available Utilities

You can use built-in functions like:

- `math.max(a, b)`
- `math.min(a, b)`
- `stringUtils.substring(str, start, end)`
- `base64.encode(value)`
- `base64.decode(value)`

These are injected into the MVEL context automatically via:

```java
context.put("math", Math.class);
context.put("stringUtils", StringUtils.class);
context.put("base64", Base64Utils.class);
```

---

## ♻️ Retry & Idempotency

### Retry

Each node in a workflow can specify a `retry` field indicating max retry attempts:

```json
{
  "name": "fetchUser",
  "retry": 3
}
```

If the node fails (e.g. due to network issue), it will be retried up to 3 times. Each attempt is stored in DB with `attemptCount`.

### Idempotency

Use `idempotency_key` field to skip re-execution of previously successful nodes for same `applicationId`:

```json
{
  "name": "fetchUser",
  "idempotency_key": "{{applicationId}}-fetchUser"
}
```

If the key has already been executed for that application, the node is skipped (but still logged as `skipped = true`).

---

## 📂 JSON Workflow Format (Sample)

```json
{
  "nodes": [
    {
      "id": 1,
      "name": "getUser",
      "request_url": "http://localhost:8089/api/user",
      "method": "GET"
    },
    {
      "id": 2,
      "name": "checkAge",
      "condition": "user.age >= 18",
      "request_url": "http://localhost:8089/api/validate",
      "method": "POST",
      "request_body": {
        "age": "{{user.age}}"
      },
      "retry": 2
    }
  ]
}
```

---

## 📊 Architecture

### Backend

- **Spring Boot** app exposing REST APIs
- Uses **MVEL** for dynamic logic
- Stores workflows & executions in **MySQL**
- Swagger integrated for testing
- Utility classes (`Math`, `StringUtils`, `Base64Utils`) injected for use in expressions

### Frontend

- Built with **React**
- Axios-based API calls
- Fancy UI with clean layout using shared `App.css`
- Supports:
  - Add/Update/Delete/View Workflows
  - Upload via file
  - View All Executions & Single Execution
  - Run workflows with input

---

## 📄 Sample Use Cases

- API chaining
- Conditional logic pipelines
- Testing external API integrations
- Step-based automation with audit trail

---

## 🚧 Future Improvements

- JWT Authentication & Role-based access
- Step rollback or compensation actions
- WebSocket support for live execution tracking
- Reusable subflows

---

## ✉️ Contributions

Open to feedback, enhancements, and pull requests!

---

## 🚩 License

MIT License

---

Happy Workflowing! ✨

