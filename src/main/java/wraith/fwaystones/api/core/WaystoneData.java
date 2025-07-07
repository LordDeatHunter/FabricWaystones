package wraith.fwaystones.api.core;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import wraith.fwaystones.block.AbstractWaystoneBlock;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.registry.WaystoneBlocks;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class WaystoneData {

    public static final Endec<RegistryEntry<AbstractWaystoneBlock>> BLOCK_ENDEC = CodecUtils.toEndec(Registries.BLOCK.getEntryCodec()).xmap(entry -> (RegistryEntry<AbstractWaystoneBlock>) (Object) entry, entry -> (RegistryEntry<Block>) (Object) entry);

    public static final UUID EMPTY_UUID = UUID.nameUUIDFromBytes("empty".getBytes(StandardCharsets.UTF_8));

    public static final WaystoneData EMPTY = new WaystoneData(EMPTY_UUID, WaystoneBlocks.DEFAULT_ENTRY, WaystoneTypes.STONE_TYPE, -1);

    private static final StructEndec<WaystoneData> SIMPLE_ENDEC = StructEndecBuilder.of(
        BuiltInEndecs.UUID.fieldOf("uuid", WaystoneData::uuid),
        BLOCK_ENDEC.fieldOf("block", WaystoneData::waystoneBlock),
        WaystoneTypes.ENDEC.fieldOf("type", WaystoneData::type),
        Endec.INT.fieldOf("color", WaystoneData::color),
        WaystoneData::new
    );

    public static final StructEndec<WaystoneData> ENDEC = (StructEndec<WaystoneData>) Endec.dispatchedStruct(
        type -> {
            return switch (type){
                case SIMPLE -> SIMPLE_ENDEC;
                case NETWORKED -> NetworkedWaystoneData.ENDEC;
            };
        },
        WaystoneData::dataType,
        Endec.forEnum(Type.class),
        "variant"
    );

    protected final UUID uuid;

    protected RegistryEntry<AbstractWaystoneBlock> waystoneBlock;
    protected WaystoneType type;
    protected int color;

    public WaystoneData(UUID uuid) {
        this(uuid, WaystoneBlocks.DEFAULT_ENTRY, WaystoneTypes.STONE_TYPE, WaystoneTypes.STONE_TYPE.defaultRuneColor());
    }

    public WaystoneData(UUID uuid, RegistryEntry<AbstractWaystoneBlock> waystoneBlock, WaystoneType type, int color) {
        this.uuid = uuid;
        this.waystoneBlock = waystoneBlock;
        this.type = type;
        this.color = color;
    }

    public UUID uuid() {
        return uuid;
    }

    public WaystoneType type() {
        return this.type;
    }

    public void type(WaystoneType type, AbstractWaystoneBlock block) {
        if (!this.type.equals(type)) {
            if (this.color == this.type.defaultRuneColor()) {
                this.color = type.defaultRuneColor();
            }

            this.type = type;
        }

        if (!this.waystoneBlock.value().equals(block)) {
            this.waystoneBlock = (RegistryEntry<AbstractWaystoneBlock>) (Object) block.getRegistryEntry();
        }
    }

    public RegistryEntry<AbstractWaystoneBlock> waystoneBlock() {
        return this.waystoneBlock;
    }

    public int color() {
        return this.color;
    }

    public void color(int value) {
        this.color = value;
    }

    public WaystoneData cloneWithUUID(UUID uuid) {
        return new WaystoneData(uuid, waystoneBlock, type, color);
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof WaystoneData that) && Objects.equals(uuid, that.uuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    public Type dataType() {
        return Type.SIMPLE;
    }

    public enum Type {
        SIMPLE,
        NETWORKED
    }
}
