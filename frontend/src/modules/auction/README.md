# Auction Module

## This folder owns

- active auction listing
- auction details
- create auction
- place bid
- seller auction view
- bidder bid history
- admin close controls

## Backend note

The backend currently expects some IDs in request bodies even though service logic also reads the user from the token.
The frontend supplies those IDs so validation passes cleanly.
