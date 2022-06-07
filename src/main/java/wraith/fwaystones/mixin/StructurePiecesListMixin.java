package wraith.fwaystones.mixin;

import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import wraith.fwaystones.access.StructurePiecesListAccess;

import java.util.List;

@Mixin(StructurePiecesList.class)
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
