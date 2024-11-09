package builderb0y.bigglobe.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import builderb0y.bigglobe.hyperspace.HyperspaceConstants;
import builderb0y.bigglobe.hyperspace.PlayerWaypointManager;
import builderb0y.bigglobe.mixinInterfaces.WaypointTracker;
import builderb0y.bigglobe.versions.EntityVersions;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntity_StopFlyingWhenExitingHyperspace extends PlayerEntity {

	@Shadow @Final public ServerPlayerInteractionManager interactionManager;

	public ServerPlayerEntity_StopFlyingWhenExitingHyperspace() {
		super(null, null, 0.0F, null);
	}

	@Inject(method = "setServerWorld", at = @At("HEAD"))
	private void bigglobe_makePlayersStopFlyingWhenExitingHyperspace(ServerWorld newWorld, CallbackInfo callback) {
		World oldWorld = EntityVersions.getWorld(this);
		//these null checks are 100% unnecessary,
		//but they won't hurt anything either.
		if (oldWorld != null && newWorld != null && newWorld.getRegistryKey() != HyperspaceConstants.WORLD_KEY) {
			if (oldWorld.getRegistryKey() == HyperspaceConstants.WORLD_KEY) {
				this.interactionManager.getGameMode().setAbilities(this.getAbilities());
				//only place where this is called from will send
				//a PlayerAbilitiesS2CPacket shortly afterward.
			}
			PlayerWaypointManager manager = ((WaypointTracker)(this)).bigglobe_getWaypointManager();
			if (manager != null) manager.entrance = null;
		}
	}
}