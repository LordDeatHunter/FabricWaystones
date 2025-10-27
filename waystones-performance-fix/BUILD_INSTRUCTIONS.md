# Build Instructions - Waystones Performance Fix

## Prerequisites

- **Java 17 or higher** installed
- **Internet connection** (to download dependencies)
- **Git** (optional, for cloning)

---

## Method 1: Build on Your Computer

### Step 1: Download the mod source

You can either:

**Option A: Clone the repository**
```bash
git clone <your-repo-url>
cd FabricWaystones/waystones-performance-fix
```

**Option B: Download as ZIP**
- Download the repository as ZIP
- Extract it
- Navigate to `waystones-performance-fix/` folder

### Step 2: Verify Java version

```bash
java -version
```

You should see Java 17 or higher. If not, download from: https://adoptium.net/

### Step 3: Build the mod

**On Linux/Mac:**
```bash
./gradlew build
```

**On Windows:**
```cmd
gradlew.bat build
```

**Note:** The first build will take 5-10 minutes as it downloads dependencies (~150MB).

### Step 4: Find your JAR

After successful build:
```bash
cd build/libs/
ls
```

You'll see: **`waystones-performance-fix-1.0.0.jar`**

---

## Method 2: Use Pre-built JAR (If Available)

If someone has already built the mod, just use the pre-built JAR:

```bash
# Check if JAR already exists
ls build/libs/waystones-performance-fix-1.0.0.jar
```

---

## Method 3: Build on Server (If It Has Internet)

If your Minecraft server has internet access:

```bash
# Upload the source code to your server
scp -r waystones-performance-fix/ user@server:/tmp/

# SSH into server
ssh user@server

# Build on server
cd /tmp/waystones-performance-fix
./gradlew build

# Copy JAR to mods folder
cp build/libs/waystones-performance-fix-1.0.0.jar /path/to/minecraft/server/mods/
```

---

## Troubleshooting

### "gradlew: command not found"

Make it executable:
```bash
chmod +x gradlew
./gradlew build
```

### "Java version mismatch"

Install Java 17+:
```bash
# On Ubuntu/Debian
sudo apt install openjdk-17-jdk

# On Windows
# Download from https://adoptium.net/
```

### "Could not resolve dependencies"

Check your internet connection. Gradle needs to download:
- Fabric Loom
- Minecraft 1.20.1
- Fabric API
- Fabric Loader

### Build succeeds but no JAR

Check the build output:
```bash
./gradlew build
# Look for: BUILD SUCCESSFUL

ls -la build/libs/
```

---

## What Gets Downloaded

During the first build, Gradle will download:

- **Fabric Loom** (~20MB) - Build tool for Fabric mods
- **Minecraft 1.20.1** (~30MB) - Game libraries
- **Fabric API** (~5MB) - Fabric API libraries
- **Fabric Loader** (~2MB) - Mod loader
- **Yarn Mappings** (~10MB) - Deobfuscation mappings
- **Dependencies** (~50MB) - Various libraries

Total: ~150MB

This only happens once. Future builds are much faster!

---

## Expected Build Output

Successful build looks like:
```
> Task :compileJava
> Task :processResources
> Task :classes
> Task :jar
> Task :sourcesJar
> Task :build

BUILD SUCCESSFUL in 2m 15s
```

Failed build shows:
```
FAILURE: Build failed with an exception.
```

---

## After Building

### Verify the JAR

```bash
ls -lh build/libs/
# Should show:
# waystones-performance-fix-1.0.0.jar (about 50-100 KB)
```

### Test the JAR

```bash
unzip -l build/libs/waystones-performance-fix-1.0.0.jar
# Should show:
# - com/waystoneperf/*.class files
# - fabric.mod.json
# - mixins JSON
```

### Install on Server

```bash
# Copy to server
scp build/libs/waystones-performance-fix-1.0.0.jar user@server:/path/to/mods/

# Or if local server
cp build/libs/waystones-performance-fix-1.0.0.jar /path/to/server/mods/
```

---

## Alternative: GitHub Actions Auto-Build

You can set up GitHub Actions to auto-build on every commit.

Create `.github/workflows/build.yml`:

```yaml
name: Build Mod

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Gradle
        run: |
          cd waystones-performance-fix
          chmod +x gradlew
          ./gradlew build
      - uses: actions/upload-artifact@v3
        with:
          name: mod-jar
          path: waystones-performance-fix/build/libs/*.jar
```

Then download the JAR from GitHub Actions artifacts.

---

## Quick Reference

```bash
# Clone repo
git clone <repo-url>
cd FabricWaystones/waystones-performance-fix

# Build
./gradlew build

# Find JAR
ls build/libs/waystones-performance-fix-1.0.0.jar

# Install
cp build/libs/waystones-performance-fix-1.0.0.jar /path/to/server/mods/

# Restart server
# Done!
```

---

## Need Help?

- Check logs: `./gradlew build --stacktrace`
- Clean build: `./gradlew clean build`
- Refresh dependencies: `./gradlew build --refresh-dependencies`
