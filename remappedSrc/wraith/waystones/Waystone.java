package wraith.waystones;

import net.minecraft.util.math.BlockPos;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;

import java.util.HashSet;

public class Waystone {

    public BlockPos pos;
    public String world;
    public String name;
    public String facing;
    public HashSet<String> discoveredBy = new HashSet<>();

    public Waystone(String name, WaystoneBlockEntity block) {
        this.pos = block.getPos();
        this.world = block.getWorld().getRegistryKey().getValue().getNamespace() + ":" + block.getWorld().getRegistryKey().getValue().getPath();;
        this.name = name;
        this.facing = block.getCachedState().get(WaystoneBlock.FACING).asString();
    }

    public Waystone(String name, BlockPos pos, String world, String facing) {
        this.pos = pos;
        this.world = world;
        this.name = name;
        this.facing = facing;
    }

    public Waystone(String name, BlockPos pos, String world, String facing, HashSet<String> discoveredBy) {
        this.pos = pos;
        this.world = world;
        this.name = name;
        this.facing = facing;
        this.discoveredBy = discoveredBy;
    }

    public Waystone rename(String newName) {
        this.name = newName;
        return this;
    }

    @Override
    public String toString() {
        return "[" + this.name + "]: " + this.world + " -> " + this.pos.getX() + " " + this.pos.getY() + " " + this.pos.getZ();
    }
}
