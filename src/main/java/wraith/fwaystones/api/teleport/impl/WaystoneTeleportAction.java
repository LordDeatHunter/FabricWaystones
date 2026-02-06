package wraith.fwaystones.api.teleport.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.teleport.TeleportAction;
import wraith.fwaystones.api.teleport.TeleportSource;
import wraith.fwaystones.block.AbstractWaystoneBlock;
import wraith.fwaystones.item.components.TextUtils;

import java.util.ArrayList;
import java.util.UUID;

public record WaystoneTeleportAction(@Nullable UUID uuid, TeleportSource source) implements TeleportAction {
    @Override
    public boolean isValid(Entity entity) {
        if (uuid != null) {
            var position = WaystoneDataStorage.getStorage(entity).getPosition(uuid);

            if (position != null) {
                var server = entity.getServer();

                if (server != null && server.getWorld(position.worldKey()) == null) {
                    return false;
                }

                return TeleportAction.super.isValid(entity);
            }
        }

        if (entity instanceof PlayerEntity player) {
            player.sendMessage(TextUtils.translation("no_teleport.invalid_waystone"), true);
        }

        return false;
    }

    @Override
    public GlobalPos getPos(World world) {
        return WaystoneDataStorage.getStorage(world)
            .getPosition(uuid)
            .globalPos();
    }

    @Override
    public TeleportTarget createTarget(Entity entity) {
        var server = entity.getServer();
        if (server == null) return null;

        var storage = WaystoneDataStorage.getStorage(entity);

        var position = storage.getPosition(uuid);
        if (position == null) return null;

        var world = server.getWorld(position.worldKey());
        if (world == null) return null;

        var waystone = storage.getEntity(uuid);
        if (waystone == null) return null;

        var state = waystone.getCachedState();
        if (!(state.getBlock() instanceof AbstractWaystoneBlock waystoneBlock)) return null;

        var teleportPos = waystoneBlock.findTeleportPosition(entity, world, position.blockPos(), state.get(AbstractWaystoneBlock.FACING));
        if (teleportPos == null) return null;

        float yaw = waystoneBlock.getTeleportationYaw(teleportPos, position.blockPos(), state);

        return new TeleportTarget(
            server.getWorld(position.worldKey()),
            teleportPos,
            entity.getVelocity(),
            yaw,
            entity.getPitch(),
            TeleportTarget.ADD_PORTAL_CHUNK_TICKET
        );
    }

    @Override
    public void addConsumedItems(World world, Item item, int amount) {
        if (world.isClient()) return;

        var waystoneBE = WaystoneDataStorage.getStorage(world).getEntity(uuid);

        if (waystoneBE != null) {
            var oldInventory = new ArrayList<>(waystoneBE.getInventory());

            var found = false;

            for (var stack : oldInventory) {
                if (stack.getItem() == item) {
                    stack.increment(amount);
                    found = true;
                    break;
                }
            }

            if (!found) oldInventory.add(new ItemStack(item, amount));

            waystoneBE.setInventory(oldInventory);
        }
    }
}
