package builderb0y.bigglobe.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import builderb0y.bigglobe.config.BigGlobeConfig;
import builderb0y.bigglobe.hyperspace.HyperspaceConstants;
import builderb0y.bigglobe.hyperspace.PackedWorldPos;
import builderb0y.bigglobe.hyperspace.ServerWaypointData;
import builderb0y.bigglobe.hyperspace.ServerWaypointManager;
import builderb0y.bigglobe.mixins.Entity_CurrentIdGetter;
import builderb0y.bigglobe.versions.ItemStackVersions;

public class WaypointItem extends Item {

	public final boolean isPrivate;

	public WaypointItem(Settings settings, boolean isPrivate) {
		super(settings);
		this.isPrivate = isPrivate;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (!(context.getWorld() instanceof ServerWorld serverWorld)) {
			return ActionResult.SUCCESS;
		}
		if (!BigGlobeConfig.INSTANCE.get().hyperspaceEnabled) {
			if (context.getPlayer() != null) {
				context.getPlayer().sendMessage(Text.translatable("bigglobe.hyperspace.disabled").formatted(Formatting.RED), true);
			}
			return ActionResult.SUCCESS;
		}
		if (context.getWorld().getRegistryKey() == HyperspaceConstants.WORLD_KEY) {
			if (context.getPlayer() != null) {
				context.getPlayer().sendMessage(Text.translatable("bigglobe.hyperspace.cant_place_waypoint_here").formatted(Formatting.RED), true);
			}
			return ActionResult.SUCCESS;
		}
		ServerWaypointManager manager = ServerWaypointManager.get(serverWorld);
		if (
			manager != null &&
			manager.addWaypoint(
				new ServerWaypointData(
					manager.nextID(),
					Entity_CurrentIdGetter.bigglobe_getCurrentID().incrementAndGet(),

					this.isPrivate && context.getPlayer() != null
					? context.getPlayer().getGameProfile().getId()
					: null,

					new PackedWorldPos(
						serverWorld.getRegistryKey(),
						context.getBlockPos().getX() + 0.5D,
						context.getBlockPos().getY() + 2.5D,
						context.getBlockPos().getZ() + 0.5D
					),

					ItemStackVersions.getCustomName(context.getStack())
				),
				true
			)
		) {
			context.getStack().decrement(1);
		}
		return ActionResult.SUCCESS;
	}
}