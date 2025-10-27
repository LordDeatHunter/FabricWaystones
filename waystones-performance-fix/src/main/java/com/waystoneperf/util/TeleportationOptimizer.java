package com.waystoneperf.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Performance optimizer for waystone teleportation that prevents MSPT spikes
 * by pre-loading chunks asynchronously and queuing teleportations across multiple ticks.
 */
public class TeleportationOptimizer {

    private static final TeleportationOptimizer INSTANCE = new TeleportationOptimizer();
    private static final Logger LOGGER = LoggerFactory.getLogger("WaystonePerformanceFix");

    // Configuration
    private static final int MAX_TELEPORTS_PER_TICK = 2;
    private static final int CHUNK_PRELOAD_RADIUS = 2;
    private static final int CHUNK_PRELOAD_TICKS = 300; // 15 seconds
    private static final long CHUNK_LOAD_TIMEOUT_MS = 100;

    // Teleport queue
    private final Queue<QueuedTeleport> teleportQueue = new ConcurrentLinkedQueue<>();

    // Dimension world cache
    private final Map<String, ServerWorld> dimensionCache = new ConcurrentHashMap<>();

    // Chunk pre-loading tracking
    private final Map<UUID, ChunkPreloadTask> preloadTasks = new ConcurrentHashMap<>();

    // Thread pool for async operations
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 4),
        r -> {
            Thread thread = new Thread(r, "Waystone-ChunkLoader");
            thread.setDaemon(true);
            return thread;
        }
    );

    // Custom chunk ticket type
    private static final ChunkTicketType<ChunkPos> WAYSTONE_TICKET =
        ChunkTicketType.create("waystone_teleport", Comparator.comparingLong(ChunkPos::toLong), CHUNK_PRELOAD_TICKS);

    private TeleportationOptimizer() {}

    public static TeleportationOptimizer getInstance() {
        return INSTANCE;
    }

    /**
     * Queues a teleportation with async chunk pre-loading.
     */
    public void queueTeleport(ServerPlayerEntity player, ServerWorld targetWorld,
                             Vec3d targetPos, float yaw, float pitch, Runnable onSuccess) {
        UUID playerId = player.getUuid();

        // Cancel any existing preload task for this player
        cancelPreload(playerId);

        // Create preload task
        BlockPos blockPos = new BlockPos((int) targetPos.x, (int) targetPos.y, (int) targetPos.z);
        ChunkPreloadTask preloadTask = new ChunkPreloadTask(targetWorld, blockPos);
        preloadTasks.put(playerId, preloadTask);

        // Start async chunk preloading
        asyncExecutor.submit(() -> {
            try {
                preloadChunksAsync(preloadTask, targetWorld, blockPos);
            } catch (Exception e) {
                LOGGER.warn("Error preloading chunks for waystone teleport", e);
            }
        });

        // Queue the actual teleport
        QueuedTeleport queuedTeleport = new QueuedTeleport(
            player, targetWorld, targetPos, yaw, pitch, onSuccess, preloadTask
        );
        teleportQueue.offer(queuedTeleport);
    }

    /**
     * Processes queued teleportations. Should be called every server tick.
     */
    public void tick() {
        int teleportsProcessed = 0;
        long tickStartTime = System.currentTimeMillis();

        while (teleportsProcessed < MAX_TELEPORTS_PER_TICK && !teleportQueue.isEmpty()) {
            QueuedTeleport teleport = teleportQueue.peek();

            if (teleport == null) {
                break;
            }

            // Check if player is still valid
            if (!teleport.player.isRemoved() && teleport.player.isAlive()) {
                // Check if chunks are ready or if we've waited long enough
                if (teleport.preloadTask.isReady() || teleport.shouldForceExecute()) {
                    // Remove from queue before executing
                    teleportQueue.poll();

                    // Execute the teleport
                    executeTeleport(teleport);
                    teleportsProcessed++;

                    // Check if we're taking too long this tick
                    long elapsed = System.currentTimeMillis() - tickStartTime;
                    if (elapsed > CHUNK_LOAD_TIMEOUT_MS) {
                        break;
                    }
                } else {
                    // Not ready yet, leave in queue
                    break;
                }
            } else {
                // Player invalid, remove from queue
                teleportQueue.poll();
                preloadTasks.remove(teleport.player.getUuid());
            }
        }

        // Clean up old preload tasks
        preloadTasks.entrySet().removeIf(entry ->
            System.currentTimeMillis() - entry.getValue().creationTime > 10000
        );
    }

    /**
     * Asynchronously preloads chunks around the destination.
     */
    private void preloadChunksAsync(ChunkPreloadTask task, ServerWorld world, BlockPos pos) {
        ChunkPos centerChunk = new ChunkPos(pos);
        Set<ChunkPos> chunksToLoad = new HashSet<>();

        // Calculate chunks in radius
        for (int x = -CHUNK_PRELOAD_RADIUS; x <= CHUNK_PRELOAD_RADIUS; x++) {
            for (int z = -CHUNK_PRELOAD_RADIUS; z <= CHUNK_PRELOAD_RADIUS; z++) {
                chunksToLoad.add(new ChunkPos(centerChunk.x + x, centerChunk.z + z));
            }
        }

        task.totalChunks = chunksToLoad.size();

        // Schedule chunk loading on server thread
        for (ChunkPos chunkPos : chunksToLoad) {
            world.getServer().execute(() -> {
                try {
                    // Add chunk ticket to keep it loaded
                    world.getChunkManager().addTicket(WAYSTONE_TICKET, chunkPos, 3, chunkPos);

                    // Check if chunk is loaded
                    if (world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
                        task.chunksLoaded++;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error loading chunk {} for waystone teleport", chunkPos, e);
                    task.chunksLoaded++;
                }
            });
        }

        task.submitted = true;
    }

    /**
     * Executes the actual teleportation.
     */
    private void executeTeleport(QueuedTeleport teleport) {
        try {
            ServerPlayerEntity player = teleport.player;

            // Detach from vehicles
            player.stopRiding();

            // Teleport using 1.20.1 API
            if (player.getWorld().getRegistryKey() == teleport.targetWorld.getRegistryKey()) {
                // Same dimension - simple teleport
                player.teleport(
                    teleport.targetPos.x,
                    teleport.targetPos.y,
                    teleport.targetPos.z
                );
                player.setYaw(teleport.yaw);
                player.setPitch(teleport.pitch);
            } else {
                // Cross-dimension teleport
                player.teleport(
                    teleport.targetWorld,
                    teleport.targetPos.x,
                    teleport.targetPos.y,
                    teleport.targetPos.z,
                    teleport.yaw,
                    teleport.pitch
                );
            }

            // Clean up preload task
            preloadTasks.remove(player.getUuid());

            // Execute success callback
            if (teleport.onSuccess != null) {
                teleport.onSuccess.run();
            }

        } catch (Exception e) {
            LOGGER.error("Error executing waystone teleport", e);
        }
    }

    /**
     * Cancels any pending teleport for a player.
     */
    public void cancelTeleport(UUID playerId) {
        teleportQueue.removeIf(teleport -> teleport.player.getUuid().equals(playerId));
        cancelPreload(playerId);
    }

    private void cancelPreload(UUID playerId) {
        preloadTasks.remove(playerId);
    }

    /**
     * Gets a cached ServerWorld by dimension name.
     */
    public ServerWorld getWorldByDimension(String dimension, ServerWorld fallback) {
        ServerWorld cached = dimensionCache.get(dimension);
        if (cached != null && !cached.isClient) {
            return cached;
        }

        if (fallback != null && fallback.getServer() != null) {
            for (ServerWorld world : fallback.getServer().getWorlds()) {
                String worldDimension = world.getRegistryKey().getValue().toString();
                dimensionCache.put(worldDimension, world);
                if (worldDimension.equals(dimension)) {
                    return world;
                }
            }
        }

        return fallback;
    }

    public void clearDimensionCache() {
        dimensionCache.clear();
    }

    public int getQueueSize() {
        return teleportQueue.size();
    }

    public void shutdown() {
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        dimensionCache.clear();
        teleportQueue.clear();
        preloadTasks.clear();
    }

    // Inner classes
    private static class QueuedTeleport {
        final ServerPlayerEntity player;
        final ServerWorld targetWorld;
        final Vec3d targetPos;
        final float yaw;
        final float pitch;
        final Runnable onSuccess;
        final ChunkPreloadTask preloadTask;
        final long queuedTime;

        QueuedTeleport(ServerPlayerEntity player, ServerWorld targetWorld,
                      Vec3d targetPos, float yaw, float pitch, Runnable onSuccess,
                      ChunkPreloadTask preloadTask) {
            this.player = player;
            this.targetWorld = targetWorld;
            this.targetPos = targetPos;
            this.yaw = yaw;
            this.pitch = pitch;
            this.onSuccess = onSuccess;
            this.preloadTask = preloadTask;
            this.queuedTime = System.currentTimeMillis();
        }

        boolean shouldForceExecute() {
            return System.currentTimeMillis() - queuedTime > 3000;
        }
    }

    private static class ChunkPreloadTask {
        final ServerWorld world;
        final BlockPos targetPos;
        final long creationTime;
        volatile int totalChunks;
        volatile int chunksLoaded;
        volatile boolean submitted;

        ChunkPreloadTask(ServerWorld world, BlockPos targetPos) {
            this.world = world;
            this.targetPos = targetPos;
            this.creationTime = System.currentTimeMillis();
            this.totalChunks = 0;
            this.chunksLoaded = 0;
            this.submitted = false;
        }

        boolean isReady() {
            return submitted && chunksLoaded >= totalChunks;
        }
    }
}
