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

# 8. User api for testing custom eval function max

curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "url": "/api/user"
    },
    "response": {
      "status": 200,
      "headers": {
        "Content-Type": "application/json"
      },
      "jsonBody": {
        "name": "John Doe",
        "age": 21
      }
    }
  }'

# 9. Eligibility api for testing custom eval function max

curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPattern": "/eligibility/.*"
    },
    "response": {
      "status": 200,
      "headers": {
        "Content-Type": "application/json"
      },
      "jsonBody": {
        "eligible": true,
        "message": "Age is eligible."
      }
    }
  }'

# 10. Substring api for testing custom StringUtils of mvel

curl -s -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "POST",
      "url": "/api/check-substring",
      "bodyPatterns": [
        {
          "matchesJsonPath": "$[?(@.prefix == \"John\")]"
        }
      ]
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "result": "Substring matched successfully!"
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'

  # 11. /api/user/details

  curl -s -X POST http://localhost:8089/__admin/mappings \
    -H "Content-Type: application/json" \
    -d '{
            "request": {
              "method": "POST",
              "url": "/api/user/details"
            },
            "response": {
              "status": 200,
              "body": "{\"name\":\"John Doe\", \"age\":28, \"id\":\"U123\"}",
              "headers": { "Content-Type": "application/json" }
            }
          }'

  # 12. /api/credit/score\\?userId=.+

  curl -s -X POST http://localhost:8089/__admin/mappings \
    -H "Content-Type: application/json" \
    -d '{
            "request": {
              "method": "GET",
              "urlPattern": "/api/credit/score\\?userId=.+"
            },
            "response": {
              "status": 200,
              "body": "{\"score1\": 720, \"score2\": 690}",
              "headers": { "Content-Type": "application/json" }
            }
          }'

  # 13. /api/loan/decision

  curl -s -X POST http://localhost:8089/__admin/mappings \
    -H "Content-Type: application/json" \
    -d '{
            "request": {
              "method": "POST",
              "url": "/api/loan/decision"
            },
            "response": {
              "status": 200,
              "body": "{\"decision\": \"APPROVED\"}",
              "headers": { "Content-Type": "application/json" }
            }
          }'

# 14. /internal/score/compute

  curl -s -X POST http://localhost:8089/__admin/mappings \
    -H "Content-Type: application/json" \
    -d '{
          "request": {
            "method": "POST",
            "url": "/internal/score/compute"
          },
          "response": {
            "status": 200,
            "jsonBody": {
              "average": 705,
              "minAllowed": 690
            },
            "headers": {
              "Content-Type": "application/json"
            }
          }
        }'

# 15. /internal/log/userId

  curl -s -X POST http://localhost:8089/__admin/mappings \
    -H "Content-Type: application/json" \
    -d '{
          "request": {
            "method": "POST",
            "url": "/internal/log/userId"
          },
          "response": {
            "status": 200,
            "jsonBody": {
              "logStatus": "SUCCESS"
            },
            "headers": {
              "Content-Type": "application/json"
            }
          }
        }'

echo "✅ WireMock stubs registered successfully!"
