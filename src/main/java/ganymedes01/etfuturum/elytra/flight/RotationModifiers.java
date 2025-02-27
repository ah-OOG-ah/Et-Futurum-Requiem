package ganymedes01.etfuturum.elytra.flight;

import static ganymedes01.etfuturum.core.utils.Utils.lerp;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import ganymedes01.etfuturum.api.elytra.event.RollContext;
import ganymedes01.etfuturum.api.elytra.rotation.RotationInstant;
import ganymedes01.etfuturum.core.utils.Logger;
import ganymedes01.etfuturum.elytra.ModConfig;
import ganymedes01.etfuturum.elytra.ModKeybindings;
import ganymedes01.etfuturum.elytra.Sensitivity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;

public class RotationModifiers {
    public static final double ROLL_REORIENT_CUTOFF = sqrt(10.0 / 3.0);

    public static RollContext.ConfiguresRotation buttonControls(double power) {
        return (rotationInstant, context) -> {
            var delta = power * context.getRenderDelta();
            var pitch = 0.0;
            var yaw = 0.0;
            var roll = 0.0;

            if (ModKeybindings.PITCH_UP.isPressed()) {
                pitch -= delta;
            }
            if (ModKeybindings.PITCH_DOWN.isPressed()) {
                pitch += delta;
            }
            if (ModKeybindings.YAW_LEFT.isPressed()) {
                yaw -= delta;
            }
            if (ModKeybindings.YAW_RIGHT.isPressed()) {
                yaw += delta;
            }
            if (ModKeybindings.ROLL_LEFT.isPressed()) {
                roll -= delta;
            }
            if (ModKeybindings.ROLL_RIGHT.isPressed()) {
                roll += delta;
            }

            // Putting this in the roll value, since it'll be swapped later
            return rotationInstant.add(pitch, yaw, roll);
        };
    }

    public static RollContext.ConfiguresRotation smoothing(Smoother pitchSmoother, Smoother yawSmoother, Smoother rollSmoother, Sensitivity smoothness) {
        return (rotationInstant, context) -> RotationInstant.of(
                smoothness.pitch == 0 ? rotationInstant.pitch() : pitchSmoother.smooth(rotationInstant.pitch(), 1 / smoothness.pitch * context.getRenderDelta()),
                smoothness.yaw == 0 ? rotationInstant.yaw() : yawSmoother.smooth(rotationInstant.yaw(), 1 / smoothness.yaw * context.getRenderDelta()),
                smoothness.roll == 0 ? rotationInstant.roll() : rollSmoother.smooth(rotationInstant.roll(), 1 / smoothness.roll * context.getRenderDelta())
        );
    }

    public static RotationInstant banking(RotationInstant rotationInstant, RollContext context) {
        var delta = context.getRenderDelta();
        var currentRotation = context.getCurrentRotation();
        var currentRoll = toRadians(currentRotation.roll());

        var xExpression = ModConfig.INSTANCE.getBankingXFormula().getCompiledOrDefaulting(0);
        var yExpression = ModConfig.INSTANCE.getBankingYFormula().getCompiledOrDefaulting(0);

        var vars = getVars(context);
        vars.put("banking_strength", ModConfig.INSTANCE.getBankingStrength());

        var dX = xExpression.eval(vars);
        var dY = yExpression.eval(vars);

        // check if we accidentally got NaN, for some reason this happens sometimes
        if (Double.isNaN(dX)) dX = 0;
        if (Double.isNaN(dY)) dY = 0;

        return rotationInstant.addAbsolute(dX * delta, dY * delta, currentRoll);
    }

    public static RotationInstant reorient(RotationInstant rotationInstant, RollContext context) {
        var delta = context.getRenderDelta();
        var currentRoll = toRadians(context.getCurrentRotation().roll());
        var strength = 10 * ModConfig.INSTANCE.getRightingStrength();

        var cutoff = ROLL_REORIENT_CUTOFF;
        double rollDelta = 0;
        if (-cutoff < currentRoll && currentRoll < cutoff) {
            rollDelta = -Math.pow(currentRoll, 3) / 3.0 + currentRoll; //0.1 * Math.pow(currentRoll, 5);
        }

        return rotationInstant.add(0, 0, -rollDelta * strength * delta);
    }

    public static RollContext.ConfiguresRotation fixNaN(String name) {
        return (rotationInstant, context) -> {
            if (Double.isNaN(rotationInstant.pitch())) {
                rotationInstant = RotationInstant.of(0, rotationInstant.yaw(), rotationInstant.roll());
                Logger.warn("NaN found in pitch for " + name + ", setting to 0 as fallback");
            }
            if (Double.isNaN(rotationInstant.yaw())) {
                rotationInstant = RotationInstant.of(rotationInstant.pitch(), 0, rotationInstant.roll());
                Logger.warn("NaN found in yaw for " + name + ", setting to 0 as fallback");
            }
            if (Double.isNaN(rotationInstant.roll())) {
                rotationInstant = RotationInstant.of(rotationInstant.pitch(), rotationInstant.yaw(), 0);
                Logger.warn("NaN found in roll for " + name + ", setting to 0 as fallback");
            }
            return rotationInstant;
        };
    }

    public static RotationInstant applyControlSurfaceEfficacy(RotationInstant rotationInstant, RollContext context) {
        var elevatorExpression = ModConfig.INSTANCE.getElevatorEfficacyFormula().getCompiledOrDefaulting(1);
        var aileronExpression = ModConfig.INSTANCE.getAileronEfficacyFormula().getCompiledOrDefaulting(1);
        var rudderExpression = ModConfig.INSTANCE.getRudderEfficacyFormula().getCompiledOrDefaulting(1);

        var vars = getVars(context);
        return rotationInstant.multiply(elevatorExpression.eval(vars), rudderExpression.eval(vars), aileronExpression.eval(vars));
    }

    private static Map<String, Double> getVars(RollContext context) {
        var player = Minecraft.getMinecraft().thePlayer;
        assert player != null;

        var currentRotation = context.getCurrentRotation();
        var rotationVector = player.getLookVec();
        return new HashMap<>() {{
            put("pitch", currentRotation.pitch());
            put("yaw", currentRotation.yaw());
            put("roll", currentRotation.roll());
            put("velocity_length", sqrt(player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ));
            put("velocity_x", player.motionX);
            put("velocity_y", player.motionY);
            put("velocity_z", player.motionZ);
            put("look_x", rotationVector.xCoord);
            put("look_y", rotationVector.yCoord);
            put("look_z", rotationVector.zCoord);
        }};
    }

    // WARN: this is mojang code!
    public class Smoother {
        private double actualSum;
        private double smoothedSum;
        private double movementLatency;

        public double smooth(double original, double smoother) {
            this.actualSum += original;
            double d = this.actualSum - this.smoothedSum;
            double e = lerp(0.5, this.movementLatency, d);
            double f = Math.signum(d);
            if (f * d > f * this.movementLatency) {
                d = e;
            }

            this.movementLatency = e;
            this.smoothedSum += d * smoother;
            return d * smoother;
        }

        public void clear() {
            this.actualSum = 0.0;
            this.smoothedSum = 0.0;
            this.movementLatency = 0.0;
        }
    }
}
