# Payment Module

## This folder owns

- checkout form
- payment submission
- receipt rendering

## Gateway flow

The payment page submits to:

- `POST /api/payments/process`

The gateway validates the current user, checks the auction winner, looks up shipping address and shipping cost, then forwards the request to the payment service.
