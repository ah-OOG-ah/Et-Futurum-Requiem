package ganymedes01.etfuturum.mixins.early.backlytra;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import ganymedes01.etfuturum.elytra.IElytraPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
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

				Vec3 lookVec = this.getLookVec();
				double pitchRadians = toRadians(this.rotationPitch);

				double horizontalPos = sqrt(lookVec.xCoord * lookVec.xCoord + lookVec.zCoord * lookVec.zCoord);
				double horizontalSpeed = sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

                double cos2Pitch = cos(pitchRadians);
				cos2Pitch = cos2Pitch * cos2Pitch;
				this.motionY += -0.08 + cos2Pitch * 0.06;

				if (this.motionY < 0 && horizontalPos > 0) {
					double d2 = this.motionY * -0.1 * cos2Pitch;
					this.motionY += d2;
					this.motionX += lookVec.xCoord * d2 / horizontalPos;
					this.motionZ += lookVec.zCoord * d2 / horizontalPos;
				}

				if (pitchRadians < 0) {
					double d9 = horizontalSpeed * (-Math.sin(pitchRadians)) * 0.04;
					this.motionY += d9 * 3.2;
					this.motionX -= lookVec.xCoord * d9 / horizontalPos;
					this.motionZ -= lookVec.zCoord * d9 / horizontalPos;
				}

				if (horizontalPos > 0) {
					this.motionX += (lookVec.xCoord / horizontalPos * horizontalSpeed - this.motionX) * 0.1;
					this.motionZ += (lookVec.zCoord / horizontalPos * horizontalSpeed - this.motionZ) * 0.1;
				}

				this.motionX *= 0.99;
				this.motionY *= 0.98;
				this.motionZ *= 0.99;

				// DaBR hooks here! //

				this.moveEntity(this.motionX, this.motionY, this.motionZ);

				if (this.isCollidedHorizontally && !this.worldObj.isRemote) {
					double newHorizontalSpeed = sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
					double speedChange = horizontalSpeed - newHorizontalSpeed;
					double damage = speedChange * 10 - 3;

					if (damage > 0) {
						this.playSound((int) damage > 4 ? "game.player.hurt.fall.big" : "game.player.hurt.fall.small", 1, 1);
						this.attackEntityFrom(flyIntoWall, (float) damage);
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
