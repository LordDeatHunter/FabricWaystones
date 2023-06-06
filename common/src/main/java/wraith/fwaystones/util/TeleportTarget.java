package wraith.fwaystones.util;

import net.minecraft.world.phys.Vec3;

public class TeleportTarget {
	public final Vec3 position;
	public final Vec3 velocity;
	public final float yaw;
	public final float pitch;

	public TeleportTarget(Vec3 position, Vec3 velocity, float yaw, float pitch) {
		this.position = position;
		this.velocity = velocity;
		this.yaw = yaw;
		this.pitch = pitch;
	}
}
