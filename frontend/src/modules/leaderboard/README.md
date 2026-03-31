# Leaderboard Module

## This folder owns

- show weekly top leaderboard entries
- show summarized weekly stats
- query bidder-specific weekly performance

## Expected backend behavior

- /api/leaderboard returns current week top entries
- /api/leaderboard/stats returns aggregated weekly numbers
- /api/leaderboard/bidder/{bidderId} returns bidder weekly records

## Usage

1. Start `leaderboard-service` on port `8085`
2. Start `gateway-service` on port `8080` (with new downstream config)
3. Start React app on `5173`
4. Visit `/leaderboard` to validate UI workflows.
