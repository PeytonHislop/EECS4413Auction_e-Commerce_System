# ── Stage 1: Build the React app ──────────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies first (cached unless package.json changes)
COPY package*.json ./
RUN npm ci

# Copy all source files and build for production
COPY . .
RUN npm run build
# Output goes to /app/dist

# ── Stage 2: Serve with nginx ─────────────────────────────────────────────────
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove nginx default page and copy our built React app
RUN rm -rf ./*
COPY --from=build /app/dist .

# Write the nginx config that:
#  1. Serves the React SPA (any unknown URL → index.html for React Router)
#  2. Proxies /api/* to the gateway service
#  3. Proxies /leaderboard-api/* to the leaderboard service directly
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
