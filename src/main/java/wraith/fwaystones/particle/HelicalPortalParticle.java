package wraith.fwaystones.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class HelicalPortalParticle extends SpriteBillboardParticle {
	private final double startX;
	private final double startY;
	private final double startZ;

	private final float rotationOffset = (float)(Math.random() * Math.PI * 2.0);

	protected HelicalPortalParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
		super(clientWorld, d, e, f);
		this.velocityX = g;
		this.velocityY = h;
		this.velocityZ = i;
		this.x = d;
		this.y = e;
		this.z = f;
		this.startX = this.x;
		this.startY = this.y;
		this.startZ = this.z;
		this.scale = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
		float j = this.random.nextFloat() * 0.6F + 0.4F;
		this.red = j * 0.9F;
		this.green = j * 0.3F;
		this.blue = j;
		this.maxAge = (int)(Math.random() * 10.0) + 40;
		offsetToHelix();
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void move(double dx, double dy, double dz) {
		this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
		this.repositionFromBoundingBox();
	}

	@Override
	public float getSize(float tickDelta) {
		float f = 1.0F - (this.age + tickDelta) / (this.maxAge * 1.5F);
		return this.scale * f;
	}

	@Override
	public int getBrightness(float tint) {
		int i = super.getBrightness(tint);
		float f = (float)this.age / this.maxAge;
		f *= f;
		f *= f;
		int j = i & 0xFF;
		int k = i >> 16 & 0xFF;
		k += (int)(f * 15.0F * 16.0F);
		if (k > 240) {
			k = 240;
		}

		return j | k << 16;
	}

	@Override
	public void tick() {
		this.prevPosX = this.x;
		this.prevPosY = this.y;
		this.prevPosZ = this.z;
		if (this.age++ >= this.maxAge) {
			this.markDead();
		} else {
			float f = (float)this.age / this.maxAge;
			this.x = this.startX + this.velocityX * f;
			this.y = this.startY + this.velocityY * f;
			this.z = this.startZ + this.velocityZ * f;
			this.offsetToHelix();
		}
	}

	private void offsetToHelix() {
		float progress = (float)this.age / this.maxAge;
		float helixRadius = 0.2F;
		float helixTurns = 0.5F;
		float angle = helixTurns * 2.0F * (float)Math.PI * progress + rotationOffset;

		var dir = new Vec3d(velocityX, velocityY, velocityZ).normalize();
		var up = Math.abs(dir.y) < 0.99 ? new Vec3d(0, 1, 0) : new Vec3d(1, 0, 0);
		var right = dir.crossProduct(up).normalize();
		var forward = dir.crossProduct(right).normalize();

		this.x += helixRadius * Math.cos(angle) * right.x + helixRadius * Math.sin(angle) * forward.x;
		this.y += helixRadius * Math.cos(angle) * right.y + helixRadius * Math.sin(angle) * forward.y;
		this.z += helixRadius * Math.cos(angle) * right.z + helixRadius * Math.sin(angle) * forward.z;
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider spriteProvider;

		public Factory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
			HelicalPortalParticle helicalPortalParticle = new HelicalPortalParticle(clientWorld, d, e, f, g, h, i);
			helicalPortalParticle.setSprite(this.spriteProvider);
			return helicalPortalParticle;
		}
	}
}
