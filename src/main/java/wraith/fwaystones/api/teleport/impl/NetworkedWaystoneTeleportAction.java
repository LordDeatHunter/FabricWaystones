package wraith.fwaystones.api.teleport.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.teleport.TeleportAction;
import wraith.fwaystones.api.teleport.TeleportSource;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.item.components.TextUtils;

import java.util.ArrayList;
import java.util.UUID;

public record NetworkedWaystoneTeleportAction(@Nullable UUID uuid, TeleportSource source) implements TeleportAction {
    @Override
    public boolean isValid(PlayerEntity player) {
        if (uuid != null) {
            var position = WaystoneDataStorage.getStorage(player).getPosition(uuid);

            if (position != null) {
                var server = player.getServer();

                if (server != null && server.getWorld(position.worldKey()) == null) {
                    return false;
                }

                return TeleportAction.super.isValid(player);
            }
        }

        player.sendMessage(TextUtils.translation("no_teleport.invalid_waystone"), true);

        return false;
    }

    @Override
    public GlobalPos getPos(World world) {
        var position = WaystoneDataStorage.getStorage(world).getPosition(uuid);

        return position.globalPos();
    }

    @Override
    public TeleportTarget createTarget(ServerPlayerEntity player) {
        var server = player.getServer();

        var storage = WaystoneDataStorage.getStorage(player);

        var position = storage.getPosition(uuid);
        var entity = storage.getEntity(uuid);

        var positionDir = entity.getCachedState().get(WaystoneBlock.FACING);

        float yaw = (positionDir.getAxis().getType().equals(Direction.Type.HORIZONTAL))
            ? positionDir.getOpposite().asRotation()
            : player.getYaw();

        var teleportPos = position.blockPos().toBottomCenterPos()
            .add(positionDir.getOffsetX(), 0, positionDir.getOffsetZ());

        return new TeleportTarget(
            server.getWorld(position.worldKey()),
            teleportPos,
            new Vec3d(0, 0, 0),
            yaw,
            0,
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

            for (ItemStack stack : oldInventory) {
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
