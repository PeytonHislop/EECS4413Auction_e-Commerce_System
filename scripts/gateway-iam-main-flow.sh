\
#!/usr/bin/env bash
set -euo pipefail

# Gateway -> IAM main flow script (happy path) — Windows Git Bash friendly
# Requires:
#   - gateway-service running on http://localhost:8080
#   - iam-service running and reachable by gateway (default http://localhost:8081)
#
# Usage:
#   bash gateway-iam-main-flow.sh
#   GATEWAY_URL=http://localhost:8080 bash gateway-iam-main-flow.sh

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"

echo "=== Gateway->IAM Main Flow (GATEWAY_URL=$GATEWAY_URL) ==="

ts="$(date +%s)"
USERNAME="ravi_${ts}"
EMAIL="ravi_${ts}@example.com"
PASSWORD="P@ssword123"
NEW_PASSWORD="NewP@ssword123"

signup_payload=$(cat <<JSON
{
  "username": "$USERNAME",
  "password": "$PASSWORD",
  "firstName": "Ravi",
  "lastName": "Deol",
  "email": "$EMAIL",
  "role": "BUYER",
  "shippingAddress": {
    "streetNumber": "123",
    "streetName": "Main St",
    "city": "Brampton",
    "country": "Canada",
    "postalCode": "L6X1X1"
  }
}
JSON
)

echo
echo "1) SIGNUP via Gateway"
signup_resp="$(curl -sS -X POST "$GATEWAY_URL/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d "$signup_payload")"
echo "$signup_resp"

# Windows-safe JSON extraction (export env var so Python can read it)
export SIGNUP_RESP="$signup_resp"
USER_ID="$(python -c "import os, json; s=os.environ.get('SIGNUP_RESP','{}'); print(json.loads(s).get('userId','') if s.strip() else '')")"

echo
echo "2) LOGIN via Gateway"
login_resp="$(curl -sS -X POST "$GATEWAY_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")"
echo "$login_resp"

export LOGIN_RESP="$login_resp"
TOKEN="$(python -c "import os, json; s=os.environ.get('LOGIN_RESP','{}'); print(json.loads(s).get('token','') if s.strip() else '')")"
LOGIN_USER_ID="$(python -c "import os, json; s=os.environ.get('LOGIN_RESP','{}'); print(json.loads(s).get('userId','') if s.strip() else '')")"

if [[ -z "${USER_ID:-}" ]]; then
  USER_ID="$LOGIN_USER_ID"
fi

if [[ -z "$TOKEN" ]]; then
  echo "ERROR: token not found in login response."
  exit 1
fi

echo
echo "3) VALIDATE TOKEN via Gateway"
validate_resp="$(curl -sS -X POST "$GATEWAY_URL/api/auth/validate" \
  -H "Authorization: Bearer $TOKEN")"
echo "$validate_resp"

echo
echo "4) AUTHORIZE ROLE (BUYER) via Gateway"
authorize_resp="$(curl -sS -X GET "$GATEWAY_URL/api/auth/authorize?requiredRole=BUYER" \
  -H "Authorization: Bearer $TOKEN")"
echo "$authorize_resp"

if [[ -n "${USER_ID:-}" ]]; then
  echo
  echo "5) GET USER PROFILE via Gateway (userId=$USER_ID)"
  profile_resp="$(curl -sS -X GET "$GATEWAY_URL/api/users/$USER_ID")"
  echo "$profile_resp"
else
  echo
  echo "5) GET USER PROFILE skipped (userId not found in responses)"
fi

echo
echo "6) FORGOT PASSWORD via Gateway"
forgot_resp="$(curl -sS -X POST "$GATEWAY_URL/api/auth/password/forgot" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\"}")"
echo "$forgot_resp"

export FORGOT_RESP="$forgot_resp"
RESET_TOKEN="$(python -c "import os, json; s=os.environ.get('FORGOT_RESP','{}'); print(json.loads(s).get('resetToken','') if s.strip() else '')")"

if [[ -z "$RESET_TOKEN" ]]; then
  echo "NOTE: resetToken not found in forgot response. If IAM doesn't return it, this is expected."
  echo "      If IAM logs/console prints the token, copy it and run reset manually."
  exit 0
fi

echo
echo "7) RESET PASSWORD via Gateway"
reset_resp="$(curl -sS -X POST "$GATEWAY_URL/api/auth/password/reset" \
  -H "Content-Type: application/json" \
  -d "{\"resetToken\":\"$RESET_TOKEN\",\"newPassword\":\"$NEW_PASSWORD\"}")"
echo "$reset_resp"

echo
echo "8) LOGIN WITH NEW PASSWORD via Gateway"
login2_resp="$(curl -sS -X POST "$GATEWAY_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$NEW_PASSWORD\"}")"
echo "$login2_resp"

echo
echo "=== DONE ==="
