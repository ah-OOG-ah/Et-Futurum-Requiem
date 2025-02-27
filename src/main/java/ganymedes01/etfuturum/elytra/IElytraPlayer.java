package ganymedes01.etfuturum.elytra;

import ganymedes01.etfuturum.api.elytra.RollEntity;

public interface IElytraPlayer extends ganymedes01.etfuturum.api.elytra.IElytraPlayer, RollEntity {
	void etfu$setElytraFlying(boolean flag);

	float etfu$getTicksElytraFlying();

	boolean etfu$lastElytraFlying();

	void etfu$setLastElytraFlying(boolean flag);

	void tickElytra();
}
