package com.waystoneperf;

import com.waystoneperf.util.TeleportationOptimizer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Waystones Performance Fix
 *
 * A server-side optimization mod that prevents MSPT spikes during waystone teleportation.
 * Works by injecting async chunk pre-loading and teleport queuing into Fabric Waystones.
 *
 * Features:
 * - Async chunk pre-loading before teleportation
 * - Teleport queue limiting to 2 teleports per tick
 * - Dimension world caching to reduce world iteration
 * - Smart scheduling with force-execute timeout
 *
 * Target MSPT: <50ms per teleport (distributed across 2-3 ticks)
 */
public class WaystonePerformanceFix implements ModInitializer {

    public static final String MOD_ID = "waystones-performance-fix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Waystones Performance Fix initializing...");

        // Register server tick event to process teleport queue
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TeleportationOptimizer.getInstance().tick();
        });

        // Register server stopped event to cleanup resources
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            LOGGER.info("Shutting down teleportation optimizer...");
            TeleportationOptimizer.getInstance().shutdown();
        });

        // Clear dimension cache on world load/reload
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TeleportationOptimizer.getInstance().clearDimensionCache();
        });

        LOGGER.info("Waystones Performance Fix initialized successfully!");
        LOGGER.info("Teleport optimization active - MSPT spikes should now be <50ms");
    }
}
