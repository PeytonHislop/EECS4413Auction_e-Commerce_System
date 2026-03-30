# Catalogue Module

## This folder owns

- browse items
- item details
- create item
- item presentation components

## Important note

The backend catalogue service supports keyword search, but the gateway currently exposes only a plain `GET /api/items`.

Because of that, the React app loads items through the gateway and filters them client-side.

## Use Cases Supported

- **UC-CAT-2: Browse the Catalogue of Auctioned Items**
	- Users can browse all items currently up for auction (active items only).
- **UC-CAT-2.1: Item Search**
	- Users can search for items by keyword (name/description). Search is client-side due to gateway limitation.
	- Only items with status 'ACTIVE' are shown.
- **UC-CAT-7: Auction Item (Static Catalogue)**
	- Sellers can list new items for auction, specifying description, auction type, duration, and shipping price.
	- The end date is calculated by the backend and shown in the UI.

## Flows

- **Browse**: `/catalogue` — Search and view all active items.
- **Create**: `/catalogue/create` — Form for sellers to list a new item.
- **Details**: `/catalogue/items/:itemId` — View all details for a specific item.

## Limitations

- Search is client-side only until the gateway supports keyword queries.
- Only active items are shown in the catalogue browse page.

## Validation

- All required fields are validated in the item creation form.
- Auction type is required and selectable.

## Navigation

- Users can easily navigate between browse, create, and details pages.
