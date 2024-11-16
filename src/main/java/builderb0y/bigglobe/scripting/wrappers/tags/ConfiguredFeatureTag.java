package builderb0y.bigglobe.scripting.wrappers.tags;

import java.lang.invoke.MethodHandles;
import java.util.random.RandomGenerator;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import builderb0y.bigglobe.scripting.wrappers.entries.ConfiguredFeatureEntry;
import builderb0y.bigglobe.util.DelayedEntryList;
import builderb0y.scripting.bytecode.MethodInfo;
import builderb0y.scripting.bytecode.TypeInfo;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class ConfiguredFeatureTag extends TagWrapper<ConfiguredFeature<?, ?>, ConfiguredFeatureEntry> {

	public static final TypeInfo TYPE = type(ConfiguredFeatureTag.class);
	public static final TagParser PARSER = new TagParser("ConfiguredFeatureTag", ConfiguredFeatureTag.class, "ConfiguredFeature", MethodInfo.findMethod(ConfiguredFeatureEntry.class, "isIn", boolean.class, ConfiguredFeatureTag.class));

	public ConfiguredFeatureTag(DelayedEntryList<ConfiguredFeature<?, ?>> list) {
		super(list);
	}

	public static ConfiguredFeatureTag of(MethodHandles.Lookup caller, String name, Class<?> type, String... id) {
		return of(id);
	}

	public static ConfiguredFeatureTag of(String... id) {
		if (id == null) return null;
		return new ConfiguredFeatureTag(DelayedEntryList.create(RegistryKeys.CONFIGURED_FEATURE, id));
	}

	@Override
	public ConfiguredFeatureEntry wrap(RegistryEntry<ConfiguredFeature<?, ?>> entry) {
		return new ConfiguredFeatureEntry(entry);
	}

	@Override
	public RegistryEntry<ConfiguredFeature<?, ?>> unwrap(ConfiguredFeatureEntry entry) {
		return entry.entry;
	}

	@Override
	public boolean contains(ConfiguredFeatureEntry entry) {
		return super.contains(entry);
	}

	@Override
	public ConfiguredFeatureEntry random(RandomGenerator random) {
		return super.random(random);
	}

	@Override
	public ConfiguredFeatureEntry random(long seed) {
		return super.random(seed);
	}
}