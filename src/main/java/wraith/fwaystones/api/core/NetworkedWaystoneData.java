package wraith.fwaystones.api.core;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class NetworkedWaystoneData extends WaystoneData implements Ownable, Named {

    public static final StructEndec<NetworkedWaystoneData> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.fieldOf("uuid", WaystoneData::uuid),
            WaystoneTypes.ENDEC.fieldOf("type", WaystoneData::type),
            Endec.INT.fieldOf("color", WaystoneData::color),
            Endec.STRING.fieldOf("name", NetworkedWaystoneData::name),
            Endec.BOOLEAN.fieldOf("global", NetworkedWaystoneData::global),
            BuiltInEndecs.UUID.optionalFieldOf("owner", NetworkedWaystoneData::ownerID, () -> null),
            Endec.STRING.optionalFieldOf("owner_name", NetworkedWaystoneData::ownerName, () -> null),
            NetworkedWaystoneData::new
    );

    private String name;
    private boolean global;

    private UUID owner = null;
    private String ownerName = null;

    public NetworkedWaystoneData(UUID uuid, String name) {
        this(uuid, Utils.generateWaystoneName(name), false);
    }

    public NetworkedWaystoneData(UUID uuid, WaystoneType type, int color, String name, boolean global, UUID owner, String ownerName) {
        super(uuid, type, color);

        this.name = name;
        this.global = global;

        this.setOwnerData(owner, ownerName);
    }

    public NetworkedWaystoneData(UUID uuid, String name, boolean global) {
        super(uuid);

        this.name = name;
        this.global = global;
    }

    @Override
    public WaystoneType type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    public boolean global() {
        return global;
    }

    @Nullable
    @Override
    public UUID ownerID() {
        return owner;
    }

    @Nullable
    @Override
    public String ownerName() {
        return ownerName;
    }

    public boolean hasOwner() {
        return owner != null;
    }

    //--

    @ApiStatus.Internal
    @Override
    public void owner(@Nullable PlayerEntity player) {
        if (player == null) {
            setOwnerData(null, null);
        } else {
            setOwnerData(player.getUuid(), player.getName().getString());
        }
    }

    @ApiStatus.Internal
    private NetworkedWaystoneData setOwnerData(UUID owner, String ownerName){
        this.owner = owner;
        this.ownerName = ownerName;

        return this;
    }

    @ApiStatus.Internal
    @Override
    public void name(String value) {
        this.name = value;
    }

    @ApiStatus.Internal
    public NetworkedWaystoneData global(boolean value) {
        this.global = value;

        return this;
    }

    @Override
    public String toString() {
        return "NetworkedWaystoneData[" +
                "name=" + name + ", " +
                "color=" + color + ", " +
                "global=" + global + ", " +
                "ownerUUID=" + owner + ", " +
                "ownerName=" + ownerName + ']';
    }

    public Type dataType() {
        return Type.NETWORKED;
    }

    public NetworkedWaystoneData cloneWithUUID(UUID uuid) {
        return new NetworkedWaystoneData(uuid, type, color, name, global, owner, ownerName);
    }
}
