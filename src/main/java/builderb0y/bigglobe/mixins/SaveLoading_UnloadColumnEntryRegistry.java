package builderb0y.bigglobe.mixins;

import java.util.concurrent.CompletableFuture;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.SaveLoading;

import builderb0y.bigglobe.columns.scripted.ColumnEntryRegistry;

@Mixin(SaveLoading.class)
public class SaveLoading_UnloadColumnEntryRegistry {

	@ModifyReturnValue(method = "load", at = @At("RETURN"))
	private static CompletableFuture<?> bigglobe_finishLoadingColumnEntryRegistry(
		CompletableFuture<?> original
	) {
		return original.whenComplete((Object result, Throwable exception) -> {
			ColumnEntryRegistry.Loading.endLoad(exception == null);
		});
	}
}