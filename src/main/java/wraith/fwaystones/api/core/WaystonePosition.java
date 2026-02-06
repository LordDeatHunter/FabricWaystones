package wraith.fwaystones.api.core;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.util.Utils;

import java.math.BigInteger;
import java.util.Objects;

//TODO: TRANSITION TO USING GLOBAL POS WITHIN THE FUTURE AFTER SOME TIME
public final class WaystonePosition {

    public static final WaystonePosition EMPTY = new WaystonePosition(RegistryKey.of(RegistryKeys.WORLD, FabricWaystones.id("empty")), BlockPos.ORIGIN);

    public static final StructEndec<GlobalPos> GLOBAL_POS_ENDEC = StructEndecBuilder.of(
        MinecraftEndecs.IDENTIFIER.xmap(id -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue).fieldOf("dimension", GlobalPos::dimension),
        MinecraftEndecs.BLOCK_POS.fieldOf("pos", GlobalPos::pos),
        GlobalPos::new
    );

    public static final StructEndec<WaystonePosition> ENDEC = StructEndecBuilder.of(
        GLOBAL_POS_ENDEC.flatFieldOf(WaystonePosition::globalPos),
        WaystonePosition::new
    );

    // TODO: MAKE THIS STRUCT WITHIN THE FUTURE with structCatchErrors or whatever and deal with future version where this is removed!
    @Deprecated
    @ApiStatus.Internal
    public static final Endec<WaystonePosition> DEPRECATED_ENDEC = ENDEC.catchErrors((ctx, serializer, exception) -> {
        var stringHash = Endec.STRING.decode(ctx, serializer);

        return WaystonePosition.unsafePositonFromHash(stringHash, WaystoneDataStorage.getServer().getScoreboard());
    });

    private final GlobalPos globalPos;
    private final String hash;

    private final boolean isUnsafe;

    public WaystonePosition(GlobalPos pos) {
        this(pos, createHashString(pos.dimension(), pos.pos()), false);
    }

    public WaystonePosition(RegistryKey<World> worldKey, BlockPos position) {
        this(worldKey, position, createHashString(worldKey, position), false);
    }

    private WaystonePosition(RegistryKey<World> worldKey, BlockPos position, String hash, boolean isUnsafe) {
        this(new GlobalPos(worldKey, position), hash, isUnsafe);
    }

    private WaystonePosition(GlobalPos pos, String hash, boolean isUnsafe) {
        this.globalPos = pos;
        this.hash = hash;
        this.isUnsafe = isUnsafe;
    }

    /**
     * Very unsafe if this fails meaning it should be avoided for when creating new WaystoneHash
     */
    @Deprecated
    @ApiStatus.Internal
    public static WaystonePosition unsafePositonFromHash(String hashString, @Nullable Scoreboard scoreboard) {
        var hash = new WaystonePosition(null, null, hashString, true);

        if (scoreboard != null) hash = hash.attemptToFix(scoreboard);

        return hash;
    }

    @Deprecated
    public static String createHashString(RegistryKey<World> worldKey, BlockPos position) {
        return Utils.getSHA256(
                "<POS X:" + position.getX() +
                        ", Y:" + position.getY() +
                        ", Z:" + position.getZ() +
                        ", WORLD: \">" + worldKey.getValue().toString() + "\">"
        );
    }

    public GlobalPos globalPos(){
        return this.globalPos;
    }

    public String worldName() {
        if (this.isUnsafe) {
            throw new IllegalArgumentException("Unable to get the world name from the WaystoneHash as it is unsafe variant!");
        }

        return globalPos.dimension().getValue().toString();
    }

    public RegistryKey<World> worldKey(){
        return globalPos.dimension();
    }

    public BlockPos blockPos() {
        if (this.isUnsafe) {
            throw new IllegalArgumentException("Unable to get the position from the WaystoneHash as it is unsafe variant!");
        }

        return globalPos.pos();
    }

    public WaystonePosition attemptToFix(Scoreboard scoreboard) {
        if (!this.isUnsafe) return this;

        return WaystoneDataStorage.getStorage(scoreboard)
                .getHashFromUnsafeHash(this);
    }

    public boolean isUnsafe() {
        return isUnsafe;
    }

    public String hashString() {
        return hash;
    }

    public byte[] getHashByteArray() {
        var hash = hashString();
        var values = hash.substring(1, hash.length() - 1).split(", ");
        var bytes = new byte[values.length];
        for (int i = 0; i < values.length; ++i) {
            bytes[i] = Byte.parseByte(values[i]);
        }
        return bytes;
    }

    public String getHexHash() {
        BigInteger number = new BigInteger(1, getHashByteArray());
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WaystonePosition) obj;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "WaystoneHash[" +
                "worldName=" + globalPos.dimension().getValue().toString() + ", " +
                "position=" + globalPos.pos() + ", " +
                "hash=" + hash + ']';
    }


}
