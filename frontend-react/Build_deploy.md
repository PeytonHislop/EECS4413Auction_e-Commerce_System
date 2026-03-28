# Build and Deploy Guide - React Frontend

Complete instructions for building the React frontend and integrating it with the Spring Boot frontend service.

## Quick Start (Development)

### 1. Start Development Server

```bash
cd frontend-react
npm install
npm start
```

Access at: `http://localhost:3000`

API calls go to: `http://localhost:8080/api` (Gateway Service)

## Production Build

### 1. Build React Application

```bash
cd frontend-react

# Install/update dependencies
npm install

# Build optimized production bundle
npm run build
```

This creates a `build/` directory with:
- Minified React app (~280KB uncompressed, ~50KB gzipped)
- Optimized CSS and JavaScript
- Source maps for debugging
- Static assets ready to serve

### 2. Verify Build Contents

```bash
# Check build size
du -sh build/

# List build files
ls -la build/

# Expected structure:
# build/
# ├── index.html          (Main HTML file)
# ├── static/
# │   ├── css/            (Minified stylesheets)
# │   ├── js/             (JavaScript bundles)
# │   └── media/          (Images, fonts)
# ├── favicon.ico
# └── robots.txt
```

## Integration with Spring Boot Frontend Service

### Step 1: Copy Build to Spring Static Directory

```bash
# From repository root
cp -r frontend-react/build/* frontend-service/src/main/resources/static/

# Verify files copied
ls -la frontend-service/src/main/resources/static/
```

### Step 2: Update Spring Configuration (if needed)

Edit `frontend-service/src/main/resources/application.properties`:

```properties
# Server port
server.port=8081

# Context path (optional)
server.servlet.context-path=/

# If using different API endpoint
# spring.frontend.api-url=http://localhost:8080/api
```

### Step 3: Build Spring Frontend Service

```bash
cd frontend-service
mvn clean package
```

This creates: `frontend-service/target/frontend-service-0.0.1-SNAPSHOT.jar`

### Step 4: Run Spring Frontend Service

```bash
# Option 1: Run JAR directly
java -jar frontend-service/target/frontend-service-0.0.1-SNAPSHOT.jar

# Option 2: Run with Maven
mvn spring-boot:run

# Option 3: Run from IDE
# - Open project in IDE
# - Run FrontendServiceApplication.java as Spring Boot Application
```

### Step 5: Access React App

Open browser: `http://localhost:8081`

The React app is now served by Spring Boot frontend service.

## Docker Deployment

### Build Docker Image

Create `frontend-react/Dockerfile`:

```dockerfile
# Build stage
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Create `frontend-react/nginx.conf`:

```nginx
server {
    listen 80;
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://gateway:8080;
    }
}
```

### Build and Run Container

```bash
cd frontend-react

# Build image
docker build -t auction-frontend:latest .

# Run container
docker run -d \
  --name auction-frontend \
  -p 3000:80 \
  -e API_URL=http://gateway:8080/api \
  auction-frontend:latest

# Test
curl http://localhost:3000
```

## CI/CD Pipeline

### GitHub Actions Example

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy React Frontend

on:
  push:
    branches: [ main ]
    paths:
      - 'frontend-react/**'

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
        cache-dependency-path: 'frontend-react/package-lock.json'
    
    - name: Install dependencies
      run: cd frontend-react && npm ci
    
    - name: Run tests
      run: cd frontend-react && npm test -- --coverage
    
    - name: Build production
      run: cd frontend-react && npm run build
    
    - name: Copy to Spring
      run: cp -r frontend-react/build/* frontend-service/src/main/resources/static/
    
    - name: Build Spring JAR
      run: cd frontend-service && mvn clean package -DskipTests
    
    - name: Push Docker image
      run: |
        docker build -t auction-frontend:${{ github.sha }} .
        docker push auction-frontend:${{ github.sha }}
    
    - name: Deploy to server
      run: |
        # Your deployment script here
        ssh deploy@server "docker pull auction-frontend:${{ github.sha }}"
        ssh deploy@server "docker-compose up -d"
```

## Environment-Specific Builds

### Development Build

```bash
cd frontend-react
REACT_APP_DEBUG=true REACT_APP_API_URL=http://localhost:8080/api npm run build
```

### Staging Build

```bash
cd frontend-react
REACT_APP_ENV=staging REACT_APP_API_URL=https://staging-api.example.com npm run build
```

### Production Build

```bash
cd frontend-react
REACT_APP_ENV=production REACT_APP_API_URL=https://api.example.com npm run build
```

## Testing Before Deployment

### 1. Run Unit Tests

```bash
cd frontend-react
npm test -- --coverage
```

### 2. Manual Testing

```bash
# Start dev server
npm start

# Test in browser:
# - http://localhost:3000/
# - Login with test credentials
# - Browse auctions
# - Place bid
# - View leaderboard
# - Create auction (if seller)
```

### 3. Integration Testing

```bash
# Ensure all backend services running:
curl http://localhost:8080/health
curl http://localhost:8090/health  # IAM Service
curl http://localhost:8093/health  # Auction Service

# Run integration tests
npm test -- --testPathPattern="integration"
```

### 4. Performance Testing

```bash
# Analyze bundle size
npm install --save-dev cra-bundle-analyzer
npm run analyze

# Check Lighthouse score
# - Open http://localhost:3000 in Chrome
# - F12 > Lighthouse tab
# - Run audit
```

## Rollback Procedure

If deployment fails:

```bash
# Stop current service
docker stop auction-frontend
docker rm auction-frontend

# Check backup
ls -la /backups/frontend-builds/

# Restore previous build
cp -r /backups/frontend-builds/build-2024-01-15/* frontend-service/src/main/resources/static/

# Rebuild and restart
cd frontend-service
mvn clean package
java -jar target/frontend-service-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### Build Fails with "npm ERR!"

```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
npm run build
```

### API Calls Timeout

```bash
# Increase timeout in .env
REACT_APP_API_TIMEOUT=60000

# Rebuild
npm run build
```

### React App Shows "404 Not Found" When Accessed

**Issue:** Spring Boot not configured to serve React as SPA

**Solution:** Update Spring controller to redirect all routes to index.html

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location) 
                    throws IOException {
                    Resource resource = location.createRelative(resourcePath);
                    if (resource.exists() && resource.isReadable()) {
                        return resource;
                    }
                    return location.createRelative("index.html");
                }
            });
    }
}
```

### CORS Errors in Browser Console

**Issue:** Gateway Service doesn't have CORS configured for localhost:3000

**Solution:** Add to Gateway Service `application.properties`:

```properties
spring.cors.allowed-origins=http://localhost:3000,http://localhost:8081
spring.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.cors.allowed-headers=*
spring.cors.allow-credentials=true
```

### Black/Empty React App Screen

**Issue:** React app loaded but not rendering

**Debug steps:**
1. Open browser console (F12 > Console tab)
2. Check for JavaScript errors
3. Verify `REACT_APP_API_URL` is correct
4. Check network tab for failed API requests
5. Restart dev server: `npm start`

## Performance Optimization

### 1. Code Splitting

```javascript
// lazy load pages
const Home = lazy(() => import('./pages/Home'));
const AuctionsBrowse = lazy(() => import('./pages/AuctionsBrowse'));

// In JSX:
<Suspense fallback={<Loading />}>
  <Home />
</Suspense>
```

### 2. Image Optimization

```bash
# Install image optimizer
npm install --save-dev image-webpack-loader

# Images will be automatically optimized in build
```

### 3. Caching Strategy

Update `frontend-service/src/main/resources/application.properties`:

```properties
# Cache static files for 1 year
spring.resources.chain.cache=true
spring.resources.cache.period=31536000s
```
