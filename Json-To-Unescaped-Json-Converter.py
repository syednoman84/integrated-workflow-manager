import json

# Read the escaped input file (Postman-style JSON)
with open('workflow_escaped_input.json', 'r') as f:
    escaped_payload = json.load(f)

# Extract and parse the escaped workflowJson string
name = escaped_payload["name"]
workflow_json_unescaped = json.loads(escaped_payload["workflowJson"])

# Prepare a clean JSON with workflowJson as an object
unescaped_payload = {
    "name": name,
    "workflowJson": workflow_json_unescaped
}

# Write to a clean output file
with open('workflow_unescaped_output.json', 'w') as f:
    json.dump(unescaped_payload, f, indent=2)

print("✅ Unescaped JSON written to 'workflow_unescaped_output.json'")
