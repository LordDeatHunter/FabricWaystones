package wraith.fwaystones.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import wraith.fwaystones.access.StructurePiecesListAccess;

import java.util.List;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

@Mixin(PiecesContainer.class)
public class StructurePiecesListMixin implements StructurePiecesListAccess {

    @Shadow
    @Final
    @Mutable
    private List<StructurePiece> pieces;

    @Override
    public void fabricWaystones$setPieces(List<StructurePiece> pieces) {
        this.pieces = pieces;
    }

}
