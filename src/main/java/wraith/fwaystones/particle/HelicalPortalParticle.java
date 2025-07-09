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

	private final Vec3d rotationAxis;
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
		this.rotationAxis = new Vec3d(g, h, i).subtract(d, e, f).normalize();
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
			Vec3d rotatedPosition = getRotatedPosition();
			this.x = rotatedPosition.x;
			this.y = rotatedPosition.y;
			this.z = rotatedPosition.z;
		}
	}

	private Vec3d getRotatedPosition() {
		var angle = this.rotationOffset + (this.age / (float)this.maxAge) * Math.PI * 2.0F;
		double radius = 0.1 * (this.maxAge - this.age) / this.maxAge;
		double x = this.startX + radius * (Math.cos(angle) * this.rotationAxis.getX() - Math.sin(angle) * this.rotationAxis.getY());
		double y = this.startY + radius * (Math.sin(angle) * this.rotationAxis.getX() + Math.cos(angle) * this.rotationAxis.getY());
		double z = this.startZ + radius * (Math.cos(angle) * this.rotationAxis.getZ() - Math.sin(angle) * this.rotationAxis.getY());
		return new Vec3d(x, y, z).add(this.rotationAxis.multiply(radius));
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
