package wraith.fwaystones.registry;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;

public class AttachmentsRegistry {
    public static final AttachmentType<Integer> PERSISTENT_MANA = AttachmentRegistry.<Integer>builder() // Builder for finer control
            .persistent(Codec.INT) // persistent
            .initializer(() -> 0) // default value
            .buildAndRegister(Identifier.of(FabricWaystones.MOD_ID + ":persistent_mana"));

}