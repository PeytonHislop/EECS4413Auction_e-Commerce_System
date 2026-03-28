export const moduleRegistry = [
  {
    key: "gateway",
    owner: "Gateway owner",
    purpose: "Own shared integration rules, app shell, and route orchestration.",
    routes: ["/"]
  },
  {
    key: "iam",
    owner: "IAM owner",
    purpose: "Own authentication, password flows, validation, and profile screens.",
    routes: ["/iam/login", "/iam/signup", "/iam/profile", "/iam/tools"]
  },
  {
    key: "catalogue",
    owner: "Catalogue owner",
    purpose: "Own item browsing, item details, and seller item creation.",
    routes: ["/catalogue", "/catalogue/create"]
  },
  {
    key: "auction",
    owner: "Auction owner",
    purpose: "Own auction browsing, bidding, seller auctions, and admin close flows.",
    routes: ["/auctions", "/auctions/create", "/auctions/seller", "/auctions/my-bids", "/auctions/admin"]
  },
  {
    key: "payment",
    owner: "Payment owner",
    purpose: "Own checkout and receipt UX.",
    routes: ["/payments/checkout"]
  }
];
