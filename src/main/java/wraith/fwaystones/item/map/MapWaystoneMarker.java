package wraith.fwaystones.item.map;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.map.MapIcon;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.block.WaystoneBlockEntity;

import java.util.Objects;

/**
 * Represents a waystone marker in world.
 * <p>
 * Used to track waystones in a map state.
 */
public class MapWaystoneMarker {
    private final BlockPos pos;
    @Nullable
    private final Text name;

    public MapWaystoneMarker(BlockPos pos, @Nullable Text name) {
        this.pos = pos;
        this.name = name;
    }

    public static MapWaystoneMarker fromNbt(NbtCompound nbt) {
        BlockPos blockPos = NbtHelper.toBlockPos(nbt.getCompound("Pos"));
        MutableText text = nbt.contains("Name") ? Text.Serializer.fromJson(nbt.getString("Name")) : null;
        return new MapWaystoneMarker(blockPos, text);
    }

    @Nullable
    public static MapWaystoneMarker fromWorldBlock(BlockView blockView, BlockPos blockPos) {
        BlockEntity blockEntity = blockView.getBlockEntity(blockPos);
        if (blockEntity instanceof WaystoneBlockEntity waystoneBlockEntity) {
            Text text = Text.literal(waystoneBlockEntity.getWaystoneName());
            return new MapWaystoneMarker(blockPos, text);
        }
        return null;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public MapIcon.Type getIconType() {
        return MapIcon.Type.byId((byte) 27);
    }

    @Nullable
    public Text getName() {
        return this.name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MapWaystoneMarker mapWaystoneMarker = (MapWaystoneMarker)o;
        return Objects.equals(this.pos, mapWaystoneMarker.pos) && Objects.equals(this.name, mapWaystoneMarker.name);
    }

    public int hashCode() {
        return Objects.hash(this.pos, this.name);
    }

    public NbtCompound getNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("Pos", NbtHelper.fromBlockPos(this.pos));
        if (this.name != null) {
            nbtCompound.putString("Name", Text.Serializer.toJson(this.name));
        }
        return nbtCompound;
    }

    public String getKey() {
        return "waystone-" + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ();
    }
}
