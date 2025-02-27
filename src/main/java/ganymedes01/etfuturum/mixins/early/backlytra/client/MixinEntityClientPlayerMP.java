package ganymedes01.etfuturum.mixins.early.backlytra.client;

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import ganymedes01.etfuturum.api.elytra.event.RollContext;
import ganymedes01.etfuturum.api.elytra.event.RollEvents;
import ganymedes01.etfuturum.api.elytra.rotation.RotationInstant;
import ganymedes01.etfuturum.elytra.Sensitivity;
import ganymedes01.etfuturum.elytra.flight.RotationModifiers;
import ganymedes01.etfuturum.mixins.early.backlytra.MixinEntityPlayer;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityClientPlayerMP.class)
public abstract class MixinEntityClientPlayerMP extends MixinEntityPlayer {
    @Unique
    private boolean efr$lastSentIsRolling;
    @Unique
    private float efr$lastSentRoll;

    public MixinEntityClientPlayerMP(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Inject(
            method = "sendMotionUpdates",
            at = @At("TAIL")
    )
    private void doABarrelRoll$sendRollPacket(CallbackInfo ci) {
        var isRolling = doABarrelRoll$isRolling();
        var rollDiff = doABarrelRoll$getRoll() - efr$lastSentRoll;
        if (isRolling != efr$lastSentIsRolling || rollDiff != 0.0f) {
            //ClientNetworking.sendRollUpdate(this);

            efr$lastSentIsRolling = isRolling;
            efr$lastSentRoll = doABarrelRoll$getRoll();
        }
    }

    @Override
    @Unique
    protected void doABarrelRoll$baseTickTail2() {
        // Update rolling status
        doABarrelRoll$setRolling(RollEvents.shouldRoll());
    }

    @Override
    public void doABarrelRoll$changeElytraLook(double pitch, double yaw, double roll, Sensitivity sensitivity, double mouseDelta) {
        var rotDelta = RotationInstant.of(pitch, yaw, roll);
        var currentRoll = doABarrelRoll$getRoll();
        var currentRotation = RotationInstant.of(
                rotationPitch,
                rotationYaw,
                currentRoll
        );
        var context = RollContext.of(currentRotation, rotDelta, mouseDelta);

        context.useModifier(RotationModifiers.fixNaN("INPUT"));
        RollEvents.earlyCameraModifiers(context);
        context.useModifier(RotationModifiers.fixNaN("EARLY_CAMERA_MODIFIERS"));
        context.useModifier((rotation, ctx) -> rotation.applySensitivity(sensitivity));
        context.useModifier(RotationModifiers.fixNaN("SENSITIVITY"));
        RollEvents.lateCameraModifiers(context);
        context.useModifier(RotationModifiers.fixNaN("LATE_CAMERA_MODIFIERS"));

        rotDelta = context.getRotationDelta();

        doABarrelRoll$changeElytraLook((float) rotDelta.pitch(), (float) rotDelta.yaw(), (float) rotDelta.roll());
    }

    @Override
    public void doABarrelRoll$changeElytraLook(float pitch, float yaw, float roll) {
        var currentPitch = rotationPitch;
        var currentYaw = rotationYaw;
        var currentRoll = doABarrelRoll$getRoll();

        // Convert pitch, yaw, and roll to a facing and left vector
        var facing = getLookVec();
        var left = Vec3.createVectorHelper(1, 0, 0);
        left.rotateAroundZ((float) toRadians(-currentRoll));
        left.rotateAroundX((float) toRadians(-currentPitch));
        left.rotateAroundY((float) toRadians(-(currentYaw + 180)));

        // Apply pitch
        facing = sfr$rotateAroundAxis(facing, left, (float) toRadians(-0.15 * pitch));

        // Apply yaw
        var up = facing.crossProduct(left);
        facing = sfr$rotateAroundAxis(facing, up, (float) toRadians(0.15 * yaw));
        left = sfr$rotateAroundAxis(left, up, (float) toRadians(0.15 * yaw));

        // Apply roll
        left = sfr$rotateAroundAxis(left, facing, (float) toRadians(0.15 * roll));


        // Extract new pitch, yaw, and roll
        double newPitch = toDegrees(-Math.asin(facing.yCoord));
        double newYaw = toDegrees(-Math.atan2(facing.xCoord, facing.zCoord));

        var normalLeft = Vec3.createVectorHelper(1, 0, 0);
        normalLeft.rotateAroundY((float) toRadians(-(newYaw + 180)));
        double newRoll = toDegrees(-Math.atan2(left.crossProduct(normalLeft).dotProduct(facing), left.dotProduct(normalLeft)));

        // Calculate deltas
        double deltaY = newPitch - currentPitch;
        double deltaX = newYaw - currentYaw;
        double deltaRoll = newRoll - currentRoll;

        // Apply vanilla pitch and yaw
        setAngles((float) (deltaX / 0.15), (float) (deltaY / 0.15));

        // Apply roll
        this.efr$roll += (float) deltaRoll;
        this.efr$prevRoll += (float) deltaRoll;

        // fix hand spasm when wrapping yaw value
        // TODO: renderYaw sus
        if (rotationYaw < -90 && renderYawOffset > 90) {
            renderYawOffset -= 360;
            prevRenderYawOffset -= 360;
        } else if (rotationYaw > 90 && renderYawOffset < -90) {
            renderYawOffset += 360;
            prevRenderYawOffset += 360;
        }
    }

    @Unique
    public Vec3 sfr$rotateAroundAxis(Vec3 og, Vec3 axis, float radians) {
        float x = (float) og.xCoord;
        float y = (float) og.yCoord;
        float z = (float) og.zCoord;

        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        Vec3 cross = axis.crossProduct(og);
        float dot = (float) axis.dotProduct(og);
        float mcos = 1 - cos;

        return Vec3.createVectorHelper(
                x * cos + cross.xCoord * sin + axis.xCoord * dot * mcos,
                y * cos + cross.yCoord * sin + axis.yCoord * dot * mcos,
                z * cos + cross.zCoord * sin + axis.zCoord * dot * mcos);
    }
}
