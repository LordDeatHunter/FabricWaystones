//package wraith.waystones.mixin;
//
//import net.minecraft.structure.PoolStructurePiece;
//import net.minecraft.structure.pool.StructurePool;
//import net.minecraft.structure.pool.StructurePoolBasedGenerator;
//import net.minecraft.structure.pool.StructurePoolElement;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//import wraith.waystones.util.Config;
//import wraith.waystones.util.Utils;
//
//import java.util.List;
//import java.util.Random;
//import java.util.function.Function;
//
//@Mixin(StructurePoolBasedGenerator.StructurePoolGenerator.class)
//public class StructurePoolBasedGeneratorMixin {
//
//    @Shadow
//    @Final
//    private List<? super PoolStructurePiece> children;
//    TODO: Fix this
//    @Redirect(method = "generatePiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/pool/StructurePool;getElementIndicesInRandomOrder(Ljava/util/Random;)Ljava/util/List;", ordinal = 0))
//    private List<StructurePoolElement> test(StructurePool originalPool, Random random) {
//        var config = Config.getInstance();
//        var poolAccess = (StructurePoolAccessor) originalPool;
//        Function<String, Boolean> isWaystonePool = s -> s.contains("waystone") && s.contains("village_waystone");
//        var ret = originalPool.getElementIndicesInRandomOrder(random);
//        if (config.generateInVillages() && poolAccess.getElements().stream().anyMatch(element -> isWaystonePool.apply(element.toString()))) {
//            var addedWaystones = children.stream().filter(element -> isWaystonePool.apply(element.toString())).count();
//            if (addedWaystones < config.getMaxPerVillage() || (addedWaystones < config.getMaxPerVillage() && Utils.getRandomIntInRange(0, poolAccess.getElements().size()) < config.getMaxPerVillage())) {
//                ret = ret.stream().filter(element -> isWaystonePool.apply(element.toString())).toList();
//            }
//        } else {
//            ret =ret.stream().filter(element -> !isWaystonePool.apply(element.toString())).toList();
//        }
//        return ret;
//    }
//
//}
//