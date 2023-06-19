package wraith.fwaystones.mixin;

import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import wraith.fwaystones.access.StructurePiecesListAccess;

import java.util.List;

@Mixin(PiecesContainer.class)
public class StructurePiecesListMixin implements StructurePiecesListAccess {

    @Shadow
    @Final
    @Mutable
    private List<StructurePiece> pieces;

    @Override
    public void setPieces(List<StructurePiece> pieces) {
        this.pieces = pieces;
    }

}