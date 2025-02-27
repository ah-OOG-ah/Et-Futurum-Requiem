package ganymedes01.etfuturum.elytra.event;

import ganymedes01.etfuturum.api.elytra.event.RollContext;
import ganymedes01.etfuturum.api.elytra.rotation.RotationInstant;
import java.util.function.BooleanSupplier;

public final class RollContextImpl implements RollContext {
    private final RotationInstant currentRotation;
    private RotationInstant rotationDelta;
    private final double renderDelta;

    public RollContextImpl(RotationInstant currentRotation, RotationInstant rotationDelta, double renderDelta) {
        this.currentRotation = currentRotation;
        this.rotationDelta = rotationDelta;
        this.renderDelta = renderDelta;
    }

    @Override
    public RollContext useModifier(ConfiguresRotation modifier, BooleanSupplier condition) {
        rotationDelta = rotationDelta.useModifier(rotationInstant -> modifier.apply(rotationInstant, this), condition);
        return this;
    }

    @Override
    public RollContext useModifier(ConfiguresRotation modifier) {
        rotationDelta = rotationDelta.useModifier(rotationInstant -> modifier.apply(rotationInstant, this));
        return this;
    }

    @Override
    public RotationInstant getCurrentRotation() {
        return currentRotation;
    }

    @Override
    public RotationInstant getRotationDelta() {
        return rotationDelta;
    }

    @Override
    public double getRenderDelta() {
        return renderDelta;
    }
}
