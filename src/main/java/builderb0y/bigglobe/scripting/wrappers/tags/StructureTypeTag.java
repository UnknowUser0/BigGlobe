package builderb0y.bigglobe.scripting.wrappers.tags;

import java.lang.invoke.MethodHandles;
import java.util.random.RandomGenerator;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.structure.StructureType;

import builderb0y.bigglobe.scripting.wrappers.entries.StructureTypeEntry;
import builderb0y.bigglobe.util.DelayedEntryList;
import builderb0y.scripting.bytecode.MethodInfo;
import builderb0y.scripting.bytecode.TypeInfo;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class StructureTypeTag extends TagWrapper<StructureType<?>, StructureTypeEntry> {

	public static final TypeInfo TYPE = type(StructureTypeTag.class);
	public static final TagParser PARSER = new TagParser("StructureTypeTag", StructureTypeTag.class, "StructureType", MethodInfo.findMethod(StructureTypeEntry.class, "isIn", boolean.class, StructureTypeTag.class));

	public StructureTypeTag(DelayedEntryList<StructureType<?>> list) {
		super(list);
	}

	public static StructureTypeTag of(MethodHandles.Lookup caller, String name, Class<?> type, String... ids) {
		return of(ids);
	}

	public static StructureTypeTag of(String... ids) {
		if (ids == null) return null;
		DelayedEntryList<StructureType<?>> tag = DelayedEntryList.create(RegistryKeys.STRUCTURE_TYPE, ids);
		if (tag == null) return null;
		return new StructureTypeTag(tag);
	}

	@Override
	public StructureTypeEntry wrap(RegistryEntry<StructureType<?>> entry) {
		return new StructureTypeEntry(entry);
	}

	@Override
	public RegistryEntry<StructureType<?>> unwrap(StructureTypeEntry entry) {
		return entry.entry;
	}

	@Override
	public boolean contains(StructureTypeEntry entry) {
		return super.contains(entry);
	}

	@Override
	public StructureTypeEntry random(RandomGenerator random) {
		return super.random(random);
	}

	@Override
	public StructureTypeEntry random(long seed) {
		return super.random(seed);
	}
}