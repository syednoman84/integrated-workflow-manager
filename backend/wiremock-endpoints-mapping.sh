#!/bin/bash

echo "📡 Registering WireMock stubs..."

# 1. authToken
curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "POST",
      "url": "/post"
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "token": "mocked-token-123"
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'

# 2. getUser
curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "url": "/users/1"
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "id": 1,
        "username": "Bret"
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'

# 3. getUserProfile
curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "url": "/get",
      "headers": {
        "Authorization": {
          "equalTo": "Bearer mocked-token-123"
        }
      }
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "profile": "user profile data"
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'

# 4. getInternalData (Basic Auth)
curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "url": "/basic-auth/demo/pass",
      "headers": {
        "Authorization": {
          "equalTo": "Basic ZGVtbzpwYXNz"
        }
      }
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "internal": "secret data"
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'

# 5. loanCheck
curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPathPattern": "/loan-check/.*"
    },
    "response": {
      "status": 200,
      "jsonBody": {
          "status": "approved",
          "limit": 10000
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'

# 6. riskCheck
curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPathPattern": "/risk-check/.*"
    },
    "response": {
      "status": 200,
      "jsonBody": {
          "RiskLevel": "Low",
          "limit": 10000
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'

# 7. validateUser
curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "POST",
      "url": "/validate-user",
      "bodyPatterns": [
        {
          "matchesJsonPath": "$[?(@.userId == '\''12345'\'')]"
        },
        {
          "matchesJsonPath": "$[?(@.loanAmount == 5000)]"
        }
      ]
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "valid": true,
        "message": "User validated successfully"
      }
    }
  }'

echo "✅ WireMock stubs registered successfully!"
