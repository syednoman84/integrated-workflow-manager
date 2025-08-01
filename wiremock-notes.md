🐳 Step-by-Step: Install Docker and Run WireMock on MacBook
✅ Step 1: Install Docker Desktop for Mac

    Go to: https://www.docker.com/products/docker-desktop/

    Click "Download for Mac (Apple chip)" or "Intel chip", depending on your machine.

        To check your chip:
        Go to Apple menu > About This Mac.

    Run the installer (.dmg file) and follow the installation steps.

    After install, open Docker Desktop and ensure it starts without errors.

        It may ask for system permissions.

        Wait for the Docker whale icon 🐳 in your top menu bar to become active (solid white).

✅ Step 2: Verify Docker Installation

Open your terminal and run:

docker --version

You should see something like:

Docker version 24.x.x, build xxxx

✅ Step 3: Run WireMock Docker Container

In terminal:

docker run -d --name wiremock -p 8089:8080 wiremock/wiremock

    -d: detached mode

    --name wiremock: easy to stop later

    -p 8089:8080: maps local port 8089 to internal 8080

You now have WireMock running at:
👉 http://localhost:8089
✅ Step 4: Test with Default Page

Open your browser and go to:

http://localhost:8089/__admin

You should see a JSON page like:

{
  "mappings": [],
  "meta": {
    "total": 0
  }
}

✅ You’re ready to create mock endpoints!
🧼 Optional: Stop/Remove Container

docker stop wiremock
docker rm wiremock

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------


✅ Step-by-Step: Create Mock Endpoints in WireMock

You can run these curls one by one to add them individually OR you can also run the wiremock-endpoints-mappings.sh to add them all together.

🟡 1. authToken — POST /post

curl -X POST http://localhost:8089/__admin/mappings \
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

🟡 2. getUser — GET /users/1

curl -X POST http://localhost:8089/__admin/mappings \
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

🟡 3. getUserProfile — GET /get with Bearer token

curl -X POST http://localhost:8089/__admin/mappings \
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

🟡 4. getInternalData — GET /basic-auth/demo/pass with Basic Auth

    Basic demo:pass → base64 → ZGVtbzpwYXNz

curl -X POST http://localhost:8089/__admin/mappings \
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

🟡 5. loanCheck — GET /loan-check/.*

curl -X POST http://localhost:8089/__admin/mappings \
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

🟡 6. riskCheck — GET /risk-check/.*

curl -X POST http://localhost:8089/__admin/mappings \
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

🟡 7. validateUser — POST /validate-user

curl -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
  "request": {
    "method": "POST",
    "url": "/validate-user",
    "bodyPatterns": [
      {
        "matchesJsonPath": "$[?(@.userId == '12345')]"
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

🟡 8. User api for testing custom eval function max

curl -X POST http://localhost:8089/__admin/mappings \
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

🟡 9. Eligibility api for testing custom eval function max

curl -X POST http://localhost:8089/__admin/mappings \
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

🟡 10. Substring api for testing custom StringUtils of mvel

curl -X POST http://localhost:8089/__admin/mappings \
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


🔄 Verify

Open http://localhost:8089/__admin/mappings to confirm that all endpoints are registered.

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

Endpoints for the apis mappings created in the wiremock:
✅ 1. authToken → POST /post

curl -X POST http://localhost:8089/post \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "demo123"}'


  Expected response:
{
  "token": "mocked-token-123"
}


✅ 2. getUser → GET /users/1

curl http://localhost:8089/users/1

Expected response:

{
  "id": 1,
  "username": "Bret"
}

✅ 3. getUserProfile → GET /get with Bearer token

curl http://localhost:8089/get \
  -H "Authorization: Bearer mocked-token-123"

Expected response:

{
  "profile": "user profile data"
}

✅ 4. getInternalData → GET /basic-auth/demo/pass with Basic Auth

curl http://localhost:8089/basic-auth/demo/pass \
  -H "Authorization: Basic ZGVtbzpwYXNz"

Expected response:

{
  "internal": "secret data"
}

✅ 5. loanCheck → GET /loanCheck/.*

curl http://localhost:8089/loan-check/1

Expected response:

{
  "status": "approved",
  "limit": 10000
}

✅ 6. riskCheck → GET /riskCheck/.*

curl http://localhost:8089/risk-check/1

Expected response:

{
  "status": "approved",
  "limit": 10000
}

✅ 7. validateUser → POST /validate-user

curl -X POST http://localhost:8089/validate-user \
  -H "Content-Type: application/json" \
  -d '{"userId": "12345", "loanAmount": 5000}'


curl -X POST http://localhost:8089/api/user/details \
  -H "Content-Type: application/json" \
  -d '{"applicationId": "BOO123"}'




DELETE AN ENDPOINT FROM WIREMOCK:
curl -X DELETE http://localhost:8089/__admin/mappings/42c6da3b-54d0-4bbd-ba1d-287f4c1d6121
