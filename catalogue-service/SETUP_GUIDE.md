# Setup Guide for Code2Cash Catalogue Service

## Prerequisites Installation

### Option 1: Install Maven (Recommended)

#### Windows Installation:

1. **Download Maven**:
   - Go to https://maven.apache.org/download.cgi
   - Download the Binary zip archive (e.g., `apache-maven-3.9.6-bin.zip`)

2. **Extract Maven**:
   - Extract the zip file to a directory (e.g., `C:\Program Files\Apache\maven`)

3. **Set Environment Variables**:
   - Open System Properties → Advanced → Environment Variables
   - Add a new System Variable:
     - Variable name: `MAVEN_HOME`
     - Variable value: `C:\Program Files\Apache\maven` (or your installation path)
   - Edit the `Path` variable and add: `%MAVEN_HOME%\bin`

4. **Verify Installation**:
   ```powershell
   mvn -version
   ```

### Option 2: Use IDE's Built-in Maven

Most IDEs (IntelliJ IDEA, Eclipse, VS Code with Java Extension Pack) come with Maven bundled.

#### For VS Code:
1. Install the **Extension Pack for Java** from Microsoft
2. Install the **Spring Boot Extension Pack**
3. VS Code will use its embedded Maven

#### For IntelliJ IDEA:
1. Open the project folder
2. IntelliJ will automatically detect `pom.xml` and configure Maven
3. Click "Import Maven Projects" if prompted

#### For Eclipse:
1. Install Eclipse IDE for Enterprise Java and Web Developers
2. Open the project (File → Import → Existing Maven Project)
3. Eclipse has Maven integrated (m2e)

---

## Building the Project

Once Maven is installed or you're using an IDE:

### Using Command Line:
```powershell
cd "c:\Users\16475\Downloads\EECS4413-GroupProject"
mvn clean install
```

### Using VS Code:
1. Open the Command Palette (Ctrl+Shift+P)
2. Type "Java: Clean Java Language Server Workspace"
3. Right-click on `pom.xml` → Maven → Reload Project

### Using IntelliJ IDEA:
1. Right-click on `pom.xml`
2. Maven → Reload Project
3. Wait for dependencies to download

---

## Running the Application

### Option 1: Command Line
```powershell
mvn spring-boot:run
```

### Option 2: IDE
- **VS Code**: Press F5 or run the `CatalogueApplication.java` file
- **IntelliJ**: Right-click `CatalogueApplication.java` → Run
- **Eclipse**: Right-click `CatalogueApplication.java` → Run As → Java Application

---

## Verifying the Setup

Once the application starts, you should see:
```
Tomcat started on port(s): 8081 (http)
Started CatalogueApplication in X.XXX seconds
```

Then open your browser to:
- **Test UI**: http://localhost:8081/index.html
- **API Endpoint**: http://localhost:8081/api/catalogue/items
- **H2 Console**: http://localhost:8081/h2-console

---

## Troubleshooting

### "mvn is not recognized"
- Maven is not installed or not in PATH
- Solution: Follow the Maven installation steps above

### "Cannot resolve symbol" errors in IDE
- Dependencies not downloaded yet
- Solution: Run `mvn clean install` or use IDE's Maven reload

### Port 8081 already in use
- Another application is using port 8081
- Solution: Change the port in `application.properties`:
  ```properties
  server.port=8082
  ```

### Java version issues
- Requires Java 17 or higher
- Check version: `java -version`
- Download from: https://adoptium.net/

---

## Quick Start (No Maven Installation Required)

If you don't want to install Maven, you can use the IDE's embedded tools:

1. **Open in IntelliJ IDEA** (Recommended):
   - File → Open → Select the project folder
   - Wait for indexing to complete
   - Run `CatalogueApplication.java`

2. **Open in VS Code**:
   - Install Extension Pack for Java
   - Open the project folder
   - Run the application using the Run button

The IDE will handle all Maven operations automatically!

---

## Dependencies Downloaded

When you build the project, Maven downloads these dependencies:
- Spring Boot 3.2.0
- Spring Web (REST API)
- Spring Data JPA (Database)
- H2 Database
- Jakarta Validation
- Jakarta Persistence

Total download size: ~100MB (first time only)

---

## Next Steps

After successful setup:
1. Test the API using the web interface
2. View the database in H2 console
3. Try the cURL commands in `API_TESTING.md`
4. Start implementing additional features

Happy coding! 🚀
