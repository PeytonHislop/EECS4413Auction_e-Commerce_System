\
#!/usr/bin/env bash
set -euo pipefail

# Gateway -> IAM robustness test cases — Windows Git Bash friendly
# Requires:
#   - gateway-service running on http://localhost:8080
#   - iam-service running and reachable by gateway
#
# Usage:
#   bash gateway-iam-test-cases.sh
#   GATEWAY_URL=http://localhost:8080 bash gateway-iam-test-cases.sh

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"

echo "=== Gateway->IAM Robustness Tests (GATEWAY_URL=$GATEWAY_URL) ==="

ts="$(date +%s)"
USERNAME="test_${ts}"
EMAIL="test_${ts}@example.com"
PASSWORD="P@ssword123"

echo
echo "TC1: Signup success via Gateway"
signup_payload=$(cat <<JSON
{
  "username": "$USERNAME",
  "password": "$PASSWORD",
  "firstName": "Test",
  "lastName": "User",
  "email": "$EMAIL",
  "role": "BUYER",
  "shippingAddress": {
    "streetNumber": "1",
    "streetName": "Test St",
    "city": "Toronto",
    "country": "Canada",
    "postalCode": "M1M1M1"
  }
}
JSON
)

status1="$(curl -sS -o /tmp/gw_tc1 -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/signup" -H "Content-Type: application/json" -d "$signup_payload")"
body1="$(cat /tmp/gw_tc1)"; rm -f /tmp/gw_tc1
echo "Status: $status1"
echo "$body1"

echo
echo "TC2: Duplicate signup should fail (same username/email)"
status2="$(curl -sS -o /tmp/gw_tc2 -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/signup" -H "Content-Type: application/json" -d "$signup_payload" || true)"
body2="$(cat /tmp/gw_tc2)"; rm -f /tmp/gw_tc2
echo "Status: $status2"
echo "$body2"

echo
echo "TC3: Login success via Gateway"
status3="$(curl -sS -o /tmp/gw_tc3 -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")"
body3="$(cat /tmp/gw_tc3)"; rm -f /tmp/gw_tc3
echo "Status: $status3"
echo "$body3"

# Windows-safe token extraction (export env var so Python can read it)
export LOGIN_RESP="$body3"
TOKEN="$(python -c "import os, json; s=os.environ.get('LOGIN_RESP','{}'); print(json.loads(s).get('token','') if s.strip() else '')")"

echo
echo "TC4: Login wrong password should fail"
status4="$(curl -sS -o /tmp/gw_tc4 -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"$USERNAME\",\"password\":\"WRONG\"}" || true)"
body4="$(cat /tmp/gw_tc4)"; rm -f /tmp/gw_tc4
echo "Status: $status4"
echo "$body4"

echo
echo "TC5: Validate token (valid) via Gateway"
if [[ -n "$TOKEN" ]]; then
  status5="$(curl -sS -o /tmp/gw_tc5 -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/validate" -H "Authorization: Bearer $TOKEN")"
  body5="$(cat /tmp/gw_tc5)"; rm -f /tmp/gw_tc5
  echo "Status: $status5"
  echo "$body5"
else
  echo "SKIP: token missing from login response"
fi

echo
echo "TC6: Validate token (invalid token) via Gateway"
status6="$(curl -sS -o /tmp/gw_tc6 -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/validate" -H "Authorization: Bearer not.a.real.token" || true)"
body6="$(cat /tmp/gw_tc6)"; rm -f /tmp/gw_tc6
echo "Status: $status6"
echo "$body6"

echo
echo "TC7: Authorize role (BUYER) with valid token via Gateway"
if [[ -n "$TOKEN" ]]; then
  status7="$(curl -sS -o /tmp/gw_tc7 -w "%{http_code}" -X GET "$GATEWAY_URL/api/auth/authorize?requiredRole=BUYER" -H "Authorization: Bearer $TOKEN")"
  body7="$(cat /tmp/gw_tc7)"; rm -f /tmp/gw_tc7
  echo "Status: $status7"
  echo "$body7"
else
  echo "SKIP: token missing"
fi

echo
echo "TC8: Authorize role (SELLER) should be false for BUYER token via Gateway"
if [[ -n "$TOKEN" ]]; then
  status8="$(curl -sS -o /tmp/gw_tc8 -w "%{http_code}" -X GET "$GATEWAY_URL/api/auth/authorize?requiredRole=SELLER" -H "Authorization: Bearer $TOKEN")"
  body8="$(cat /tmp/gw_tc8)"; rm -f /tmp/gw_tc8
  echo "Status: $status8"
  echo "$body8"
else
  echo "SKIP: token missing"
fi

echo
echo "TC9: Signup missing fields should fail (no password)"
bad_payload='{"username":"missing_pw","firstName":"A","lastName":"B","email":"missing_pw@example.com","role":"BUYER"}'
status9="$(curl -sS -o /tmp/gw_tc9 -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/signup" -H "Content-Type: application/json" -d "$bad_payload" || true)"
body9="$(cat /tmp/gw_tc9)"; rm -f /tmp/gw_tc9
echo "Status: $status9"
echo "$body9"

echo
echo "TC10: Authorize role missing param should fail (no requiredRole)"
if [[ -n "$TOKEN" ]]; then
  status10="$(curl -sS -o /tmp/gw_tc10 -w "%{http_code}" -X GET "$GATEWAY_URL/api/auth/authorize" -H "Authorization: Bearer $TOKEN" || true)"
  body10="$(cat /tmp/gw_tc10)"; rm -f /tmp/gw_tc10
  echo "Status: $status10"
  echo "$body10"
else
  echo "SKIP: token missing"
fi

echo
echo "=== DONE (review responses above) ==="
