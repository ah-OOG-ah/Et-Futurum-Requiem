package ganymedes01.etfuturum.api.elytra;

import javax.vecmath.Vector2d;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;

public interface RollMouse {
    boolean doABarrelRoll$updateMouse(EntityClientPlayerMP player, double cursorDeltaX, double cursorDeltaY, double mouseDelta);

    Vector2d doABarrelRoll$getMouseTurnVec();
}