package ganymedes01.etfuturum.mixins.early.backlytra;

import ganymedes01.etfuturum.elytra.IElytraPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
	@Shadow
	public abstract boolean isClientWorld();

	private static final DamageSource flyIntoWall = (new DamageSource("flyIntoWall")).setDamageBypassesArmor();

	public MixinEntityLivingBase(World worldIn) {
		super(worldIn);
	}

	@Inject(method = "moveEntityWithHeading", at = @At("HEAD"), cancellable = true)
	private void moveElytra(float p_70612_1_, float p_70612_2_, CallbackInfo ci) {
		/* method is named incorrectly in these older mappings, it's really isServerWorld */
		if (this.isClientWorld() && !this.isInWater() && !this.handleLavaMovement()) {
			if (this instanceof IElytraPlayer && ((IElytraPlayer) this).etfu$isElytraFlying()) {
				if (this.motionY > -0.5) {
					this.fallDistance = 1;
				}

				Vec3 vec3d = this.getLookVec();
				double f = Math.toRadians(this.rotationPitch);
				double d6 = Math.sqrt(vec3d.xCoord * vec3d.xCoord + vec3d.zCoord * vec3d.zCoord);
				double d8 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
				double d1 = vec3d.lengthVector();
				double f4 = Math.cos(f);
				f4 = f4 * f4 * Math.min(1, d1 / 0.4);
				this.motionY += -0.08 + f4 * 0.06;

				if (this.motionY < 0 && d6 > 0) {
					double d2 = this.motionY * -0.1 * f4;
					this.motionY += d2;
					this.motionX += vec3d.xCoord * d2 / d6;
					this.motionZ += vec3d.zCoord * d2 / d6;
				}

				if (f < 0) {
					double d9 = d8 * (-Math.sin(f)) * 0.04;
					this.motionY += d9 * 3.2;
					this.motionX -= vec3d.xCoord * d9 / d6;
					this.motionZ -= vec3d.zCoord * d9 / d6;
				}

				if (d6 > 0) {
					this.motionX += (vec3d.xCoord / d6 * d8 - this.motionX) * 0.1;
					this.motionZ += (vec3d.zCoord / d6 * d8 - this.motionZ) * 0.1;
				}

				this.motionX *= 0.99;
				this.motionY *= 0.98;
				this.motionZ *= 0.99;
				this.moveEntity(this.motionX, this.motionY, this.motionZ);

				if (this.isCollidedHorizontally && !this.worldObj.isRemote) {
					double d10 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
					double d3 = d8 - d10;
					double f5 = d3 * 10 - 3;

					if (f5 > 0) {
						this.playSound((int) f5 > 4 ? "game.player.hurt.fall.big" : "game.player.hurt.fall.small", 1, 1);
						this.attackEntityFrom(flyIntoWall, (float) f5);
					}
				}

				if (this.onGround && !this.worldObj.isRemote) {
					((IElytraPlayer) this).etfu$setElytraFlying(false);
				}
				ci.cancel();
			}
		}
	}
}
