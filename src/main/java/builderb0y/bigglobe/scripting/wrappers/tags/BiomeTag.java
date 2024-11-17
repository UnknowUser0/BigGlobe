package builderb0y.bigglobe.scripting.wrappers.tags;

import java.lang.invoke.MethodHandles;
import java.util.random.RandomGenerator;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import builderb0y.bigglobe.scripting.wrappers.entries.BiomeEntry;
import builderb0y.bigglobe.util.DelayedEntryList;
import builderb0y.scripting.bytecode.MethodInfo;
import builderb0y.scripting.bytecode.TypeInfo;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class BiomeTag extends TagWrapper<Biome, BiomeEntry> {

	public static final TypeInfo TYPE = type(BiomeTag.class);
	public static final TagParser PARSER = new TagParser("BiomeTag", BiomeTag.class, "Biome", MethodInfo.findMethod(BiomeEntry.class, "isIn", boolean.class, BiomeTag.class));

	public BiomeTag(DelayedEntryList<Biome> list) {
		super(list);
	}

	public static BiomeTag of(MethodHandles.Lookup caller, String name, Class<?> type, String... ids) {
		return of(ids);
	}

	public static BiomeTag of(String... ids) {
		if (ids == null) return null;
		DelayedEntryList<Biome> tag = DelayedEntryList.create(RegistryKeys.BIOME, ids);
		if (tag == null) return null;
		return new BiomeTag(tag);
	}

	@Override
	public BiomeEntry wrap(RegistryEntry<Biome> entry) {
		return new BiomeEntry(entry);
	}

	@Override
	public RegistryEntry<Biome> unwrap(BiomeEntry entry) {
		return entry.entry;
	}

	@Override
	public boolean contains(BiomeEntry entry) {
		return super.contains(entry);
	}

	@Override
	public BiomeEntry random(RandomGenerator random) {
		return super.random(random);
	}

	@Override
	public BiomeEntry random(long seed) {
		return super.random(seed);
	}
}