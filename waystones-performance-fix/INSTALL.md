# Installation Guide - Waystones Performance Fix

## For Server Owners

### Quick Install (Pre-built JAR)

1. **Download the mod**
   - Get `waystones-performance-fix-1.0.0.jar` from releases
   - Or build from source (see below)

2. **Install on server**
   ```bash
   # Place the JAR in your server's mods folder
   cp waystones-performance-fix-1.0.0.jar /path/to/server/mods/
   ```

3. **Verify dependencies**
   Make sure you have in your `mods/` folder:
   - Fabric API (0.92.2+ for 1.20.1)
   - Fabric Waystones (3.0.0+ for 1.20.1)

4. **Start server**
   ```bash
   java -jar fabric-server-launch.jar
   ```

5. **Check logs**
   Look for this message:
   ```
   [WaystonePerformanceFix] Waystones Performance Fix initialized successfully!
   [WaystonePerformanceFix] Teleport optimization active - MSPT spikes should now be <50ms
   ```

### ⚠️ Important Notes

✅ **ONLY install on the server**
❌ **DO NOT install on client**
✅ **Players don't need to download anything**
✅ **Works with vanilla Fabric Waystones client**

## Building from Source

### Prerequisites
- Java 17 or higher
- Internet connection (for dependencies)

### Build Steps

1. **Clone/download the source**
   ```bash
   cd waystones-performance-fix
   ```

2. **Build the mod**
   ```bash
   # On Linux/Mac
   ./gradlew build

   # On Windows
   gradlew.bat build
   ```

3. **Find the JAR**
   ```bash
   ls build/libs/
   # You'll see: waystones-performance-fix-1.0.0.jar
   ```

4. **Install on server**
   ```bash
   cp build/libs/waystones-performance-fix-1.0.0.jar /path/to/server/mods/
   ```

### Build Troubleshooting

**Error: "Could not find fabric-loom"**
```bash
# Make sure you have internet connection
# Gradle needs to download dependencies
```

**Error: "Java version mismatch"**
```bash
# Install Java 17+
java -version  # Check your Java version
```

**Error: "Could not resolve fwaystones"**
```bash
# This is normal if building offline
# The mod will still compile, but you need Fabric Waystones at runtime
```

## Testing the Installation

### 1. Check Server Logs

After server starts, search logs for:
```
[WaystonePerformanceFix] Waystones Performance Fix initialized successfully!
```

### 2. Test Teleportation

1. Have a player discover 2 waystones in different dimensions
2. Use `/spark profiler start` (if you have Spark installed)
3. Teleport between dimensions multiple times
4. Use `/spark profiler stop`
5. Check for reduced MSPT spikes

### 3. Expected Behavior

**Before mod:**
- MSPT spikes to 100-300ms during teleports
- Server stutters when multiple players teleport
- Lag visible to all players

**After mod:**
- MSPT stays under 50ms during teleports
- Small delay (50-100ms) before player teleports
- No server-wide lag
- Smooth teleportation

## Configuration

### Default Settings (No Config Needed)

The mod works out of the box with these settings:
- Max 2 teleports per tick
- 2-chunk radius pre-loading
- 3-second force-execute timeout

### Advanced Configuration

To change settings, edit `TeleportationOptimizer.java` and rebuild:

```java
// File: src/main/java/com/waystoneperf/util/TeleportationOptimizer.java

// Lines 20-23
private static final int MAX_TELEPORTS_PER_TICK = 2;      // Change this
private static final int CHUNK_PRELOAD_RADIUS = 2;        // Change this
private static final int CHUNK_PRELOAD_TICKS = 300;       // Change this
private static final long CHUNK_LOAD_TIMEOUT_MS = 100;    // Change this
```

Then rebuild:
```bash
./gradlew build
```

### Recommended Settings by Server Type

**High-performance (NVMe, 16+ cores):**
```java
MAX_TELEPORTS_PER_TICK = 3
CHUNK_PRELOAD_RADIUS = 3
CHUNK_LOAD_TIMEOUT_MS = 150
```

**Budget (HDD, 4-8 cores):**
```java
MAX_TELEPORTS_PER_TICK = 1
CHUNK_PRELOAD_RADIUS = 1
CHUNK_LOAD_TIMEOUT_MS = 50
```

**Heavily modded:**
```java
MAX_TELEPORTS_PER_TICK = 1
CHUNK_PRELOAD_RADIUS = 2
CHUNK_LOAD_TIMEOUT_MS = 75
```

## Uninstallation

To remove the mod:

1. **Stop server**
2. **Remove JAR**
   ```bash
   rm /path/to/server/mods/waystones-performance-fix-1.0.0.jar
   ```
3. **Start server**

Waystones will work normally again with default (unoptimized) behavior.

## Compatibility Check

### Required Mods
- ✅ Fabric API 0.92.2+
- ✅ Fabric Waystones 3.0.0+

### Compatible Mods
- ✅ Chunky
- ✅ Distant Horizons
- ✅ ServerCore
- ✅ Lithium
- ✅ Starlight
- ✅ C2ME
- ✅ Spark (for profiling)

### Potential Conflicts
- ❓ Mods that heavily modify teleportation
- ❓ Mods that replace chunk loading system

## Getting Help

**Check logs first:**
```bash
cat logs/latest.log | grep WaystonePerformanceFix
```

**Common issues:**

1. **Mod not loading**
   - Check Fabric Waystones is installed
   - Verify Java 17+
   - Check fabric.mod.json dependencies

2. **Still seeing lag spikes**
   - Confirm mod loaded (check logs)
   - Reduce `CHUNK_PRELOAD_RADIUS`
   - Check other mods aren't causing lag

3. **Players stuck in queue**
   - Check for errors in logs
   - Force-execute triggers after 3 seconds
   - Restart server if needed

## Updates

To update the mod:

1. Stop server
2. Remove old JAR
3. Add new JAR
4. Start server

Settings from `TeleportationOptimizer.java` will be reset if you don't rebuild from source.

---

**Need more help? Check README.md for detailed documentation!**
