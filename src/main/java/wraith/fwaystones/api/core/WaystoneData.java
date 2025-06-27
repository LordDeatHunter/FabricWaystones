package wraith.fwaystones.api.core;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class WaystoneData {

    public static final UUID EMPTY_UUID = UUID.nameUUIDFromBytes("empty".getBytes(StandardCharsets.UTF_8));

    public static final WaystoneData EMPTY = new WaystoneData(EMPTY_UUID, WaystoneTypes.STONE_TYPE, -1);

    public static final StructEndec<WaystoneData> ENDEC = StructEndecBuilder.of(
        BuiltInEndecs.UUID.fieldOf("uuid", WaystoneData::uuid),
        WaystoneTypes.ENDEC.fieldOf("type", WaystoneData::type),
        Endec.INT.fieldOf("color", WaystoneData::color),
        WaystoneData::new
    );

    protected final UUID uuid;

    protected WaystoneType type = WaystoneTypes.STONE_TYPE;
    protected int color;

    public WaystoneData() {
        this(UUID.randomUUID());
    }

    public WaystoneData(UUID uuid) {
        this.uuid = uuid;
    }

    public WaystoneData(UUID uuid, WaystoneType type, int color) {
        this.uuid = uuid;
        this.type = type;
        this.color = color;
    }

    public UUID uuid() {
        return uuid;
    }

    public WaystoneType type() {
        return this.type;
    }

    public void type(WaystoneType type) {
        if (this.type.equals(type)) return;

        if (this.color == this.type.defaultRuneColor()) {
            this.color = type.defaultRuneColor();
        }

        this.type = type;
    }

    public int color() {
        return this.color;
    }

    public void color(int value) {
        this.color = value;
    }


    @Override
    public boolean equals(Object object) {
        return (object instanceof WaystoneData that) && Objects.equals(uuid, that.uuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
