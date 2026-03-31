# Team Ownership Guide

## Shared frontend rules

Everyone should assume:
- browser requests go to the **gateway**
- shared auth state lives in `src/shared/auth/AuthContext.jsx`
- shared fetch logic lives in `src/shared/api/apiClient.js`
- shared navigation lives in `src/shared/components/Layout.jsx`

## Folder map

### Gateway owner
**Folder:** `src/modules/gateway`

Owns:
- app overview
- integration notes visible in UI
- endpoint map shown to teammates
- shared gateway assumptions

### IAM owner
**Folder:** `src/modules/iam`

Owns:
- signup
- login
- forgot password
- reset password
- validate token
- role authorization checks
- user profile display

### Catalogue owner
**Folder:** `src/modules/catalogue`

Owns:
- browse items
- item details
- create item
- client-side search/filter UX

### Auction owner
**Folder:** `src/modules/auction`

Owns:
- active auctions
- auction detail page
- create auction
- place bid
- seller auctions
- bidder bid history
- admin close actions

### Payment owner
**Folder:** `src/modules/payment`

Owns:
- payment form
- checkout page
- receipt display

## Shared rule for new code

When a teammate adds a feature, they should try to keep it inside:
- their module folder for service-specific code
- `src/shared` only if it is useful to more than one module
