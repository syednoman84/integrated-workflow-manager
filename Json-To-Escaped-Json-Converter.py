import json

# Read the original workflow JSON
with open('workflow-unescaped-input.json', 'r') as f:
    original = json.load(f)

# Extract name and inner workflowJson
name = original["name"]
workflow_json = original["workflowJson"]

# Prepare payload with escaped JSON string
payload = {
    "name": name,
    "workflowJson": json.dumps(workflow_json)  # This escapes it
}

# Write to output file
with open('workflow_escaped_output.json', 'w') as f:
    json.dump(payload, f, indent=2)

print("✅ Postman-compatible JSON written to 'workflow_postman_ready.json'")
