package builderb0y.bigglobe.scripting.wrappers.entries;

import java.lang.invoke.MethodHandles;

import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.structure.Structure;

import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.scripting.wrappers.tags.BiomeTag;
import builderb0y.bigglobe.scripting.wrappers.tags.StructureTag;
import builderb0y.bigglobe.util.DelayedEntryList;
import builderb0y.bigglobe.versions.RegistryVersions;
import builderb0y.scripting.bytecode.ConstantFactory;
import builderb0y.scripting.bytecode.TypeInfo;

public class StructureEntry extends EntryWrapper<Structure, StructureTag> {

	public static final TypeInfo TYPE = TypeInfo.of(StructureEntry.class);
	public static final ConstantFactory CONSTANT_FACTORY = ConstantFactory.autoOfString();

	public final BiomeTag validBiomes;
	public final GenerationStep.Feature step;
	public final StructureTypeEntry type;

	public StructureEntry(RegistryEntry<Structure> entry) {
		super(entry);
		this.validBiomes = new BiomeTag(new DelayedEntryList<>(BigGlobeMod.getRegistry(RegistryKeys.BIOME), entry.value().getValidBiomes()));
		this.step = entry.value().getFeatureGenerationStep();
		this.type = new StructureTypeEntry(
			RegistryVersions.getEntry(
				Registries.STRUCTURE_TYPE,
				entry.value().getType()
			)
		);
	}

	public StructureEntry(RegistryEntry<Structure> entry, BiomeTag validBiomes, GenerationStep.Feature step) {
		super(entry);
		this.step = step;
		this.validBiomes = validBiomes;
		this.type = new StructureTypeEntry(
			RegistryVersions.getEntry(
				Registries.STRUCTURE_TYPE,
				entry.value().getType()
			)
		);
	}

	public static StructureEntry of(MethodHandles.Lookup caller, String name, Class<?> type, String id) {
		return of(id);
	}

	public static StructureEntry of(String id) {
		if (id == null) return null;
		return new StructureEntry(BigGlobeMod.getRegistry(RegistryKeys.STRUCTURE).getByName(id));
	}

	public StructureTypeEntry type() {
		return this.type;
	}

	public String generationStep() {
		return this.step.asString();
	}

	public BiomeTag validBiomes() {
		return this.validBiomes;
	}

	@Override
	public boolean isIn(StructureTag entries) {
		return super.isIn(entries);
	}
}