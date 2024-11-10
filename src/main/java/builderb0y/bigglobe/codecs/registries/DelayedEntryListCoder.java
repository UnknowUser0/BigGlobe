package builderb0y.bigglobe.codecs.registries;

import java.util.List;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;

import builderb0y.autocodec.annotations.SingletonArray;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.codecs.BigGlobeAutoCodec;
import builderb0y.bigglobe.dynamicRegistries.BetterRegistry;
import builderb0y.bigglobe.util.DelayedEntry;
import builderb0y.bigglobe.util.DelayedEntryList;

public class DelayedEntryListCoder<T> extends NamedCoder<DelayedEntryList<T>> {

	public final RegistryKey<Registry<T>> registryKey;

	public DelayedEntryListCoder(@NotNull ReifiedType<DelayedEntryList<T>> handledType, RegistryKey<Registry<T>> registryKey) {
		super(handledType);
		this.registryKey = registryKey;
	}

	@Override
	@OverrideOnly
	public @Nullable <T_Encoded> DelayedEntryList<T> decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		return new DelayedEntryList<>(
			AbstractRegistryCoder.registry(this.registryKey, context),
			context.decodeWith(DelayedCoders.LIST_CODER)
		);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, DelayedEntryList<T>> context) throws EncodeException {
		DelayedEntryList<T> list = context.object;
		if (list == null) return null;
		return context.object(list.delayedEntries).encodeWith(DelayedCoders.LIST_CODER);
	}

	/**
	DelayedEntryListCoder is instantiated before the AUTO_CODEC instance
	is fully constructed, so we need to defer the creation of this coder
	until first use.
	*/
	public static class DelayedCoders {

		public static final AutoCoder<List<DelayedEntry>> LIST_CODER = BigGlobeAutoCodec.AUTO_CODEC.createCoder(new ReifiedType<@SingletonArray List<DelayedEntry>>() {});
	}
}