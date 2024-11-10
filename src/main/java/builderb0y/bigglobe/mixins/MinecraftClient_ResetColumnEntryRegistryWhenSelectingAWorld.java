package builderb0y.bigglobe.mixins;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

import builderb0y.bigglobe.columns.scripted.ColumnEntryRegistry;

/**
normally I'd put this hook where the actual data pack loading code is,
but every time I do that, some obscure edge case breaks it.
what I need is a hook that fires after data packs are *unloaded*.
{@link ServerLifecycleEvents#SERVER_STOPPED} is not sufficient because there's
no guarantee that a server will actually start if the user cancels world creation.
so, this is what I'm doing now: reset every time the "select world" screen appears.
in singleplayer, this will fire when canceling world creation.
in multiplayer, none of this is a problem because you
can't start multiple worlds on a dedicated server.
*/
@Mixin(MinecraftClient.class)
public class MinecraftClient_ResetColumnEntryRegistryWhenSelectingAWorld {

	@Inject(method = "setScreen", at = @At("RETURN"))
	private void bigglobe_resetColumnEntryRegistry(Screen screen, CallbackInfo callback) {
		if (screen instanceof SelectWorldScreen) {
			ColumnEntryRegistry.Loading.reset();
		}
	}
}