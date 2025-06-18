package wraith.fwaystones.api.core;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class WaystoneData {

    public static final UUID EMPTY_UUID = UUID.nameUUIDFromBytes("empty".getBytes(StandardCharsets.UTF_8));

    public static final WaystoneData EMPTY = new WaystoneData(EMPTY_UUID, "", -1, false);

    public static final StructEndec<WaystoneData> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.fieldOf("uuid", WaystoneData::uuid),
            Endec.STRING.fieldOf("name", WaystoneData::name),
            Endec.INT.fieldOf("color", WaystoneData::color),
            Endec.BOOLEAN.fieldOf("global", WaystoneData::global),
            BuiltInEndecs.UUID.optionalFieldOf("owner", WaystoneData::owner, () -> null),
            Endec.STRING.optionalFieldOf("owner_name", WaystoneData::ownerName, () -> null),
            WaystoneTypes.ENDEC.fieldOf("type", WaystoneData::type),
            WaystoneData::new
    );

    private WaystoneType type = WaystoneTypes.STONE_TYPE;

    private final UUID uuid;

    private String name;
    private int color;
    private boolean global;

    private UUID owner = null;
    private String ownerName = null;

    public WaystoneData(String name, WaystoneType type) {
        this(UUID.randomUUID(), Utils.generateWaystoneName(name), type.defaultRuneColor(), false);

        this.type = type;
    }

    public WaystoneData(UUID uuid, String name, int color, boolean global, UUID owner, String ownerName, WaystoneType type) {
        this.uuid = uuid;

        this.name = name;
        this.color = color;
        this.global = global;

        this.setOwnerData(owner, ownerName);

        this.type = type;
    }

    public WaystoneData(UUID uuid, String name, int color, boolean global) {
        this.uuid = uuid;

        this.name = name;
        this.color = color;
        this.global = global;
    }

    public WaystoneType type() {
        return type;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public Text parsedName() {
        return Utils.formatWaystoneName(name);
    }

    public String sortingName() {
        return parsedName().getString().toLowerCase(Locale.ROOT);
    }

    public int color() {
        return color;
    }

    public boolean global() {
        return global;
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    @Nullable
    public String ownerName() {
        return ownerName;
    }

    public boolean hasOwner() {
        return owner != null;
    }

    //--

    @ApiStatus.Internal
    public void setOwner(@Nullable PlayerEntity player) {
        if (player == null) {
            setOwnerData(null, null);
        } else {
            setOwnerData(player.getUuid(), player.getName().getString());
        }
    }

    @ApiStatus.Internal
    public WaystoneData setOwnerData(UUID owner, String ownerName){
        this.owner = owner;
        this.ownerName = ownerName;

        return this;
    }

    @ApiStatus.Internal
    public WaystoneData setName(String value) {
        this.name = value;

        return this;
    }

    @ApiStatus.Internal
    public WaystoneData setColor(int value) {
        this.color = value;

        return this;
    }

    @ApiStatus.Internal
    public WaystoneData setGlobal(boolean value) {
        this.global = value;

        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WaystoneData that)) return false;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return "WaystoneData[" +
                "name=" + name + ", " +
                "color=" + color + ", " +
                "global=" + global + ", " +
                "ownerUUID=" + owner + ", " +
                "ownerName=" + ownerName + ']';
    }

    public WaystoneData cloneWithUUID(UUID uuid) {
        return new WaystoneData(uuid, name, color, global, owner, ownerName, type);
    }
}
