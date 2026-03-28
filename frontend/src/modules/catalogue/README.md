# Catalogue Module

## This folder owns

- browse items
- item details
- create item
- item presentation components

## Important note

The backend catalogue service supports keyword search, but the gateway currently exposes only a plain `GET /api/items`.

Because of that, the React app loads items through the gateway and filters them client-side.
