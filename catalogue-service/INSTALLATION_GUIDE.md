# Complete Installation Guide

## 🚨 Required Software

To run this Spring Boot application, you need:
1. **Java 17 or higher** (JDK)
2. **Maven** (or use an IDE with built-in Maven)
3. **An IDE** (IntelliJ IDEA Community Edition recommended)

---

## Step 1: Install Java (JDK 17 or higher)

### Windows Installation:

#### Option A: Using Installer (Recommended)

1. **Download AdoptOpenJDK (Temurin)**:
   - Go to: https://adoptium.net/
   - Select: **Temurin 17 (LTS)**
   - Architecture: **x64**
   - Package Type: **JDK**
   - Click **Download**

2. **Run the Installer**:
   - Double-click the downloaded `.msi` file
   - ✅ Check "Set JAVA_HOME variable"
   - ✅ Check "Add to PATH"
   - Click "Install"

3. **Verify Installation**:
   ```powershell
   java -version
   ```
   You should see: `openjdk version "17.0.x"`

#### Option B: Manual Installation

1. **Download**: https://adoptium.net/
2. **Extract** to: `C:\Program Files\Java\jdk-17`
3. **Set Environment Variables**:
   - Open: System Properties → Advanced → Environment Variables
   - Add **System Variable**:
     - Name: `JAVA_HOME`
     - Value: `C:\Program Files\Java\jdk-17`
   - Edit **Path** variable, add: `%JAVA_HOME%\bin`

4. **Restart** your terminal and verify:
   ```powershell
   java -version
   ```

---

## Step 2: Choose Your Development Environment

### 🌟 Option A: IntelliJ IDEA Community (Best for Beginners)

**Why IntelliJ?**
- Maven is built-in (no separate installation needed)
- Automatic dependency download
- Easy to run Spring Boot applications
- Free Community Edition

**Installation:**

1. **Download**:
   - Go to: https://www.jetbrains.com/idea/download/
   - Download **Community Edition** (FREE)

2. **Install**:
   - Run the installer
   - Accept defaults
   - ✅ Check "Add 'Open Folder as Project'"
   - ✅ Check "Add launchers dir to PATH"

3. **Open the Project**:
   - Launch IntelliJ IDEA
   - Click "Open"
   - Select: `c:\Users\16475\Downloads\EECS4413-GroupProject`
   - Wait for indexing and dependency download (bottom right corner)

4. **Run the Application**:
   - Open: `CatalogueApplication.java`
   - Click the green ▶️ button next to the class name
   - Or right-click → Run 'CatalogueApplication'

5. **Open Browser**:
   - Go to: http://localhost:8083/index.html

✅ **Done!** No Maven installation needed!

---

### Option B: VS Code (Lightweight)

**Installation:**

1. **Install VS Code**:
   - Download from: https://code.visualstudio.com/

2. **Install Required Extensions**:
   - Open VS Code
   - Go to Extensions (Ctrl+Shift+X)
   - Search and install:
     1. **Extension Pack for Java** (by Microsoft)
     2. **Spring Boot Extension Pack** (by VMware)
     3. **Maven for Java** (by Microsoft)

3. **Configure Java**:
   - Press Ctrl+Shift+P
   - Type: "Java: Configure Java Runtime"
   - Set to Java 17 or higher

4. **Open the Project**:
   - File → Open Folder
   - Select: `c:\Users\16475\Downloads\EECS4413-GroupProject`
   - VS Code will detect `pom.xml` and download dependencies

5. **Run the Application**:
   - Open `CatalogueApplication.java`
   - Click "Run" above the `main` method
   - Or press F5

6. **Open Browser**:
   - Go to: http://localhost:8083/index.html

---

### Option C: Eclipse (Alternative)

1. **Download**:
   - Go to: https://www.eclipse.org/downloads/
   - Download: **Eclipse IDE for Enterprise Java and Web Developers**

2. **Import Project**:
   - File → Import → Existing Maven Projects
   - Browse to: `c:\Users\16475\Downloads\EECS4413-GroupProject`
   - Click Finish

3. **Run**:
   - Right-click `CatalogueApplication.java`
   - Run As → Java Application

---

## Step 3 (Optional): Install Maven Separately

**Only needed if**:
- You want to run Maven commands from the terminal
- Your IDE doesn't have Maven built-in

**Windows Installation:**

1. **Download**:
   - Go to: https://maven.apache.org/download.cgi
   - Download: `apache-maven-3.9.6-bin.zip`

2. **Extract**:
   - Extract to: `C:\Program Files\Apache\maven`

3. **Set Environment Variables**:
   - System Properties → Advanced → Environment Variables
   - Add **System Variable**:
     - Name: `MAVEN_HOME`
     - Value: `C:\Program Files\Apache\maven`
   - Edit **Path** variable, add: `%MAVEN_HOME%\bin`

4. **Verify**:
   ```powershell
   mvn -version
   ```

5. **Build the Project**:
   ```powershell
   cd "c:\Users\16475\Downloads\EECS4413-GroupProject"
   mvn clean install
   mvn spring-boot:run
   ```

---

## 📋 Quick Reference

### Check Installations:
```powershell
# Check Java
java -version

# Check Maven (if installed separately)
mvn -version
```

### Build and Run:
```powershell
# Navigate to project
cd "c:\Users\16475\Downloads\EECS4413-GroupProject"

# Build (downloads dependencies)
mvn clean install

# Run the application
mvn spring-boot:run
```

### Access the Application:
- **Web Interface**: http://localhost:8083/index.html
- **API**: http://localhost:8083/api/catalogue/items
- **H2 Console**: http://localhost:8083/h2-console

---

## ❓ Troubleshooting

### "java is not recognized"
- Java not installed or not in PATH
- Solution: Follow Step 1 above

### "mvn is not recognized"
- Maven not installed or not in PATH
- Solution: Use an IDE (IntelliJ/VS Code) or follow Step 3

### "Cannot find symbol" errors in code editor
- Dependencies not downloaded yet
- Solution: 
  - IntelliJ: File → Invalidate Caches → Restart
  - VS Code: Ctrl+Shift+P → "Java: Clean Language Server Workspace"
  - Or run: `mvn clean install`

### Port 8083 already in use
- Another app is using that port
- Solution: Change in `application.properties`:
  ```properties
  server.port=8082
  ```

---

## 🎯 Recommended Setup (Easiest Path)

For the smoothest experience:

1. ✅ **Install Java 17** from https://adoptium.net/
2. ✅ **Install IntelliJ IDEA Community** from https://www.jetbrains.com/idea/
3. ✅ **Open the project** in IntelliJ
4. ✅ **Wait for dependencies** to download (automatic)
5. ✅ **Run** `CatalogueApplication.java`
6. ✅ **Open** http://localhost:8083/index.html

**Total time**: ~15-20 minutes (including downloads)

---

## 📞 Need Help?

If you encounter issues:
1. Check that Java 17+ is installed: `java -version`
2. Ensure your IDE has Java and Maven support
3. Try reloading/reimporting the Maven project
4. Check that port 8083 is available

---

## ✅ Ready to Go!

Once you see this message in your console:
```
Started CatalogueApplication in X.XXX seconds
```

Your application is running! Open http://localhost:8083/index.html to start testing.

Happy coding! 🚀
