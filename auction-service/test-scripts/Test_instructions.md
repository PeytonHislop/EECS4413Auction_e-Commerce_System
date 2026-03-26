Demo Preparation - Auction Service
Your Use Cases to Demonstrate:

UC4-AS: Browse Active Auctions (Public, no auth)
UC1-AS: Create Auction Listing (SELLER role)
UC2-AS: Place Bid on Auction (BUYER role)
UC3-AS: Auction Closure & Winner Determination (Automatic/Manual)


Demo Flow (Recommended Order)
1. Introduction (30 seconds)
"I'll demonstrate the Auction Service, which handles the complete auction lifecycle - from creation to bidding to closure with winner determination."
Key points to mention:

- Runs on port 8082
- SQLite database with auto-initialization
- Integrates with IAM for authentication and role-based authorization
- 11 REST endpoints (7 public, 4 protected)

One one terminal:
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run

------------------------------------------------------------------------------------------------------------------

Standalone (not with gateway)------------------------------------------

# 1. Browse Active Auctions
	"First, let me verify the service is running..."
(curl http://localhost:8082/api/auctions/active).Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
	"Great! Service is up. We have 2 active auctions with sample data."

# 2. Get Specific Auction Details
	"Use Case 4 - Browse Active Auctions. This is public - no authentication needed."
(curl http://localhost:8082/api/auctions/AUC001).Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
	"AUC001 has current highest bid of $50"

# 3. Create Auction (copy all 3 lines together)
	"Use Case 1 - Create Auction. Requires SELLER role."
$body = '{"itemId":"ITEM999","sellerId":"SELLER001","durationHours":48,"reservePrice":100.00}'
$response = Invoke-WebRequest -Uri "http://localhost:8082/api/auctions" -Method POST -Headers @{"Content-Type"="application/json";"Authorization"="Bearer demo-seller-token"} -Body $body
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 4. Place Valid Bid (copy all 3 lines together)
	"Use Case 2 - Place Bid. First, a valid bid higher than current."
$body = '{"bidderId":"BUYER001","bidAmount":75.00}'
$response = Invoke-WebRequest -Uri "http://localhost:8082/api/auctions/AUC001/bids" -Method POST -Headers @{"Content-Type"="application/json";"Authorization"="Bearer demo-buyer-token"} -Body $body
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 5. Place Invalid Bid - Too Low (copy all 4 lines together)
	"Now the validation - what if someone bids lower..."
$body = '{"bidderId":"BUYER001","bidAmount":30.00}'
try { $response = Invoke-WebRequest -Uri "http://localhost:8082/api/auctions/AUC001/bids" -Method POST -Headers @{"Content-Type"="application/json";"Authorization"="Bearer demo-buyer-token"} -Body $body; $response.Content } 
catch { $_.ErrorDetails.Message } | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 6. Close Auction (copy all 2 lines together)
	"Use Case 3 - Close Auction and Determine Winner."
$response = Invoke-WebRequest -Uri "http://localhost:8082/api/auctions/AUC003/close" -Method PUT -Headers @{"Authorization"="Bearer demo-admin-token"}
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 7. Verify Closed
(curl http://localhost:8082/api/auctions/AUC003).Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# BONUS: Bid History
(curl http://localhost:8082/api/auctions/AUC001/bids).Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

All 4 use cases complete:
	✓ Browse Auctions
	✓ Create Auction
	✓ Place Bid with validation
	✓ Close & Determine Winner

With Gateway------------------------------------------

# 1. Browse Active Auctions
(curl http://localhost:8080/api/auctions/active).Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 2. Get Specific Auction Details
(curl http://localhost:8080/api/auctions/AUC001).Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 3. Create Auction (copy all 3 lines together)
$body = '{"itemId":"ITEM999","sellerId":"SELLER001","durationHours":48,"reservePrice":100.00}'
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auctions" -Method POST -Headers @{"Content-Type"="application/json";"Authorization"="Bearer demo-seller-token"} -Body $body
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 4. Place Valid Bid (copy all 3 lines together)
$body = '{"bidderId":"BUYER001","bidAmount":75.00}'
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auctions/AUC001/bids" -Method POST -Headers @{"Content-Type"="application/json";"Authorization"="Bearer demo-buyer-token"} -Body $body
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 5. Place Invalid Bid - Too Low (copy all 4 lines together)
$body = '{"bidderId":"BUYER001","bidAmount":30.00}'
try { $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auctions/AUC001/bids" -Method POST -Headers @{"Content-Type"="application/json";"Authorization"="Bearer demo-buyer-token"} -Body $body; $response.Content } 
catch { $_.ErrorDetails.Message } | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 6. Close Auction (copy all 2 lines together)
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auctions/AUC002/close" -Method PUT -Headers @{"Authorization"="Bearer demo-admin-token"}
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# 7. Verify Closed
(curl http://localhost:8080/api/auctions/AUC003).Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# BONUS: Bid History
(curl http://localhost:8080/api/auctions/AUC001/bids).Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
