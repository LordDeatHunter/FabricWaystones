//package wraith.fwaystones.item.components;
//
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import io.netty.buffer.ByteBuf;
//import io.wispforest.endec.Endec;
//import io.wispforest.endec.impl.BuiltInEndecs;
//import io.wispforest.endec.impl.StructEndecBuilder;
//import net.minecraft.network.codec.PacketCodec;
//import net.minecraft.network.codec.PacketCodecs;
//import net.minecraft.server.world.ServerWorld;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.GlobalPos;
//import net.minecraft.world.poi.PointOfInterestTypes;
//
//import java.util.Optional;
//import java.util.UUID;
//
//public record WaystoneTrackerComponent(Optional<GlobalPos> target, boolean tracked) {
//	public static final Endec<WaystoneTrackerComponent> ENDEC = BuiltInEndecs.UUID;
//
//	public static final Codec<WaystoneTrackerComponent> CODEC = RecordCodecBuilder.create(
//		instance -> instance.group(
//				GlobalPos.CODEC.optionalFieldOf("target").forGetter(WaystoneTrackerComponent::target),
//				Codec.BOOL.optionalFieldOf("tracked", Boolean.valueOf(true)).forGetter(WaystoneTrackerComponent::tracked)
//				)
//				.apply(instance, WaystoneTrackerComponent::new)
//	);
//	public static final PacketCodec<ByteBuf, WaystoneTrackerComponent> PACKET_CODEC = PacketCodec.tuple(
//		GlobalPos.PACKET_CODEC.collect(PacketCodecs::optional),
//		WaystoneTrackerComponent::target,
//		PacketCodecs.BOOL,
//		WaystoneTrackerComponent::tracked,
//		WaystoneTrackerComponent::new
//	);
//
//	public WaystoneTrackerComponent forWorld(ServerWorld world) {
//		if (this.tracked && !this.target.isEmpty()) {
//			if (((GlobalPos)this.target.get()).dimension() != world.getRegistryKey()) {
//				return this;
//			} else {
//				BlockPos blockPos = ((GlobalPos)this.target.get()).pos();
//				return world.isInBuildLimit(blockPos) && world.getPointOfInterestStorage().hasTypeAt(PointOfInterestTypes.LODESTONE, blockPos)
//					? this
//					: new WaystoneTrackerComponent(Optional.empty(), true);
//			}
//		} else {
//			return this;
//		}
//	}
//}
