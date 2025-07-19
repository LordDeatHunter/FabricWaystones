package wraith.fwaystones.mixin.scoreboard;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentEntrypoint;
import net.fabricmc.fabric.impl.attachment.AttachmentSerializingImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.IdentityHashMap;
import java.util.Map;

// Copy of Fabric API implementation of AttachmentTargetImpl
@SuppressWarnings("UnstableApiUsage")
@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin implements AttachmentTargetImpl {
    @Nullable
    private IdentityHashMap<AttachmentType<?>, Object> fabric_dataAttachments = null;

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getAttached(AttachmentType<T> type) {
        return fabric_dataAttachments == null ? null : (T) fabric_dataAttachments.get(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T setAttached(AttachmentType<T> type, @Nullable T value) {
        // Extremely inelegant, but the only alternative is separating out these two mixins and duplicating code
        Object thisObject = this;

        if (thisObject instanceof BlockEntity) {
            ((BlockEntity) thisObject).markDirty();
        } else if (thisObject instanceof Chunk) {
            ((Chunk) thisObject).setNeedsSaving(true);

            if (type.isPersistent() && ((Chunk) thisObject).getStatus().equals(ChunkStatus.EMPTY)) {
                AttachmentEntrypoint.LOGGER.warn("Attaching persistent attachment {} to chunk with chunk status EMPTY. Attachment might be discarded.", type.identifier());
            }
        }

        if (value == null) {
            if (fabric_dataAttachments == null) {
                return null;
            }

            T removed = (T) fabric_dataAttachments.remove(type);

            if (fabric_dataAttachments.isEmpty()) {
                fabric_dataAttachments = null;
            }

            return removed;
        } else {
            if (fabric_dataAttachments == null) {
                fabric_dataAttachments = new IdentityHashMap<>();
            }

            return (T) fabric_dataAttachments.put(type, value);
        }
    }

    @Override
    public boolean hasAttached(AttachmentType<?> type) {
        return fabric_dataAttachments != null && fabric_dataAttachments.containsKey(type);
    }

    @Override
    public void fabric_writeAttachmentsToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        AttachmentSerializingImpl.serializeAttachmentData(nbt, wrapperLookup, fabric_dataAttachments);
    }

    @Override
    public void fabric_readAttachmentsFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        fabric_dataAttachments = AttachmentSerializingImpl.deserializeAttachmentData(nbt, wrapperLookup);
    }

    @Override
    public boolean fabric_hasPersistentAttachments() {
        return AttachmentSerializingImpl.hasPersistentAttachments(fabric_dataAttachments);
    }

    @Override
    public Map<AttachmentType<?>, ?> fabric_getAttachments() {
        return fabric_dataAttachments;
    }
}
