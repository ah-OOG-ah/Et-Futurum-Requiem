package ganymedes01.etfuturum.api.elytra;

import ganymedes01.etfuturum.elytra.Sensitivity;

public interface RollEntity {
    default void doABarrelRoll$changeElytraLook(
            double pitch, double yaw, double roll, Sensitivity sensitivity, double mouseDelta) {}

    default void doABarrelRoll$changeElytraLook(float pitch, float yaw, float roll) {}

    default boolean doABarrelRoll$isRolling() {
        return false;
    }

    default void doABarrelRoll$setRolling(boolean rolling) {}

    default float doABarrelRoll$getRoll() { return 0; }

    default float doABarrelRoll$getRoll(float tickDelta) { return 0; }

    default void doABarrelRoll$setRoll(float roll) {}
}