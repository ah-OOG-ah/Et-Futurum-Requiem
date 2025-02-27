package ganymedes01.etfuturum.mixins.early.backlytra.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import ganymedes01.etfuturum.api.elytra.RollEntity;
import ganymedes01.etfuturum.api.elytra.RollMouse;
import ganymedes01.etfuturum.elytra.IElytraPlayer;
import ganymedes01.etfuturum.elytra.Sensitivity;
import javax.vecmath.Vector2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer implements RollMouse {
	@Shadow
	private Minecraft mc;

	@Unique
	private final Vector2d mouseTurnVec = new Vector2d();
	@Unique
	private final Vector2d scratch = new Vector2d();
	@Unique
	private static final Sensitivity desktop = new Sensitivity();

	@Inject(method = "orientCamera", at = @At("TAIL"))
	private void adjustThirdPersonForElytra(float partialTicks, CallbackInfo ci) {
        if (this.mc.gameSettings.thirdPersonView <= 0 || !(this.mc.renderViewEntity instanceof IElytraPlayer elytraPlayer)) return;
        if (!elytraPlayer.etfu$isElytraFlying()) return;

        /* Move the camera down 1.62 blocks to sit at the player's feet and then up by 0.4 blocks, like 1.12 does */
        GL11.glTranslatef(0, 1.22f, 0f);

		// Rotate camera for DaBR
		if (!elytraPlayer.etfu$isElytraFlying()) return;
		float roll = elytraPlayer.doABarrelRoll$getRoll(partialTicks);
		GL11.glRotatef(roll, 0.0F, 1.0F, 0.0F);
    }

	@Inject(
			method = "updateCameraAndRender",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
					ordinal = 1,
					shift = At.Shift.AFTER
			)
	)
	private void doABarrelRoll$maintainMouseMomentum(CallbackInfo ci, @Local(argsOnly = true) float partialTicks) {
		if (Minecraft.getMinecraft().thePlayer != null && !Minecraft.getMinecraft().isGamePaused()) {
			doABarrelRoll$updateMouse(Minecraft.getMinecraft().thePlayer, 0, 0, partialTicks);
		}
	}

	@WrapWithCondition(
			method = "updateCameraAndRender",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;setAngles(FF)V"
			)
	)
	private boolean doABarrelRoll$changeLookDirection(
			EntityClientPlayerMP player,
			float yaw,
			float pitch,
			@Local(argsOnly = true) float timeDelta) {
		return !doABarrelRoll$updateMouse(player, this.mc.mouseHelper.deltaX, this.mc.mouseHelper.deltaY, timeDelta);
	}

	@Override
	public boolean doABarrelRoll$updateMouse(EntityClientPlayerMP player, double cursorDeltaX, double cursorDeltaY, double mouseDelta) {
		var rollPlayer = (RollEntity) player;

		if (rollPlayer.doABarrelRoll$isRolling()) {

			if (false/**ModConfig.INSTANCE.getMomentumBasedMouse()**/) {

				// add the mouse movement to the current vector and normalize if needed
				scratch.set(cursorDeltaX, cursorDeltaY);
				scratch.scale(1f / 300);
				mouseTurnVec.add(scratch);
				if (mouseTurnVec.lengthSquared() > 1.0) {
					mouseTurnVec.normalize();
				}
				var readyTurnVec = new Vector2d(mouseTurnVec);

				// check if the vector is within the deadzone
				double deadzone = 0.2/**ModConfig.INSTANCE.getMomentumMouseDeadzone()**/;
				if (readyTurnVec.lengthSquared() < deadzone * deadzone) readyTurnVec.scale(0);

				// enlarge the vector and apply it to the camera
				readyTurnVec.scale(1200 * (float) mouseDelta);
				rollPlayer.doABarrelRoll$changeElytraLook(readyTurnVec.y, readyTurnVec.x, 0, desktop/**ModConfig.INSTANCE.getDesktopSensitivity()**/, mouseDelta);

			} else {

				// if we are not using a momentum based mouse, we can reset it and apply the values directly
				mouseTurnVec.scale(0);
				rollPlayer.doABarrelRoll$changeElytraLook(cursorDeltaY, cursorDeltaX, 0, desktop/**ModConfig.INSTANCE.getDesktopSensitivity()**/, mouseDelta);
			}

			return true;
		}

		mouseTurnVec.scale(0);
		return false;
	}

	@Override
	public Vector2d doABarrelRoll$getMouseTurnVec() {
		return mouseTurnVec;
	}
}
