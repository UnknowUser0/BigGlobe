package builderb0y.bigglobe.scripting;

import java.lang.invoke.MethodHandles;

import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.util.Directions;
import builderb0y.bigglobe.versions.RegistryKeyVersions;
import builderb0y.scripting.bytecode.ConstantFactory;
import builderb0y.scripting.bytecode.TypeInfo;
import builderb0y.scripting.bytecode.tree.InsnTree;
import builderb0y.scripting.environments.Handlers;
import builderb0y.scripting.environments.MutableScriptEnvironment;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class StructureTemplateScriptEnvironment {

	public static final TypeInfo
		STRUCTURE_TEMPLATE_TYPE = type(StructureTemplate.class),
		STRUCTURE_PLACEMENT_DATA_TYPE = type(StructurePlacementData.class);
	public static final ConstantFactory
		TEMPLATE_FACTORY = new ConstantFactory(StructureTemplateScriptEnvironment.class, "getTemplate", String.class, StructureTemplate.class),
		PROCESSOR_FACTORY = new ConstantFactory(StructureTemplateScriptEnvironment.class, "getProcessorList", String.class, StructureProcessorList.class);

	public static final MutableScriptEnvironment INSTANCE = (
		new MutableScriptEnvironment()
		.addType("StructureTemplate", StructureTemplate.class)
		.addType("StructurePlacementData", StructurePlacementData.class)

		.addFieldGetterSetterStatic(StructurePlacementData.class, StructureTemplateScriptEnvironment.class, "mirror", String.class)
		.addFieldGetterSetterStatic(StructurePlacementData.class, StructureTemplateScriptEnvironment.class, "rotation", int.class)
		.addFieldGetterSetterStatic(StructurePlacementData.class, StructureTemplateScriptEnvironment.class, "x", int.class)
		.addFieldGetterSetterStatic(StructurePlacementData.class, StructureTemplateScriptEnvironment.class, "y", int.class)
		.addFieldGetterSetterStatic(StructurePlacementData.class, StructureTemplateScriptEnvironment.class, "z", int.class)
		.addMethodInvokeStatic(StructureTemplateScriptEnvironment.class, "pos")
		.addFieldGetterSetterStatic(StructurePlacementData.class, StructureTemplateScriptEnvironment.class, "spawnEntities", boolean.class)
		.addFieldGetterSetterStatic(StructurePlacementData.class, StructureTemplateScriptEnvironment.class, "placeFluids", boolean.class)
		.addMethodInvokeStatic(StructureTemplateScriptEnvironment.class, "addProcessors")

		.addCastConstant(TEMPLATE_FACTORY, true)
		.addCastConstant(PROCESSOR_FACTORY, true)
	);

	public static MutableScriptEnvironment create(InsnTree loadChunkBox) {
		return (
			new MutableScriptEnvironment()
			.addAll(INSTANCE)
			.addQualifiedFunction(
				type(StructurePlacementData.class),
				"new",
				Handlers.builder(
					StructureTemplateScriptEnvironment.class,
					"newStructurePlacementData"
				)
				.addImplicitArgument(loadChunkBox)
				.buildFunction()
			)
		);
	}

	public static StructureTemplate getTemplate(MethodHandles.Lookup caller, String name, Class<?> type, String id) {
		return getTemplate(id);
	}

	public static StructureTemplate getTemplate(String id) {
		if (id == null) return null;
		Identifier identifier = new Identifier(id);
		StructureTemplate template = BigGlobeMod.getCurrentServer().getStructureTemplateManager().getTemplate(identifier).orElse(null);
		if (template != null) return template;
		else throw new IllegalArgumentException("Template not found: " + identifier);
	}

	public static StructureProcessorList getProcessorList(MethodHandles.Lookup caller, String name, Class<?> type, String id) {
		return getProcessorList(id);
	}

	public static StructureProcessorList getProcessorList(String id) {
		if (id == null) return null;
		Identifier identifier = new Identifier(id);
		StructureProcessorList template = BigGlobeMod.getCurrentServer().getRegistryManager().get(RegistryKeyVersions.processorList()).get(identifier);
		if (template != null) return template;
		else throw new IllegalArgumentException("Template not found: " + identifier);
	}

	public static StructurePlacementData newStructurePlacementDate(BlockBox chunkBox) {
		return new StructurePlacementData().setBoundingBox(chunkBox);
	}

	public static String mirror(StructurePlacementData data) {
		return Directions.reverseScriptMirror(data.getMirror());
	}

	public static void mirror(StructurePlacementData data, String axis) {
		data.setMirror(Directions.scriptMirror(axis));
	}

	public static int rotation(StructurePlacementData data) {
		return Directions.reverseScriptRotation(data.getRotation());
	}

	public static void rotation(StructurePlacementData data, int rotation) {
		data.setRotation(Directions.scriptRotation(rotation));
	}

	public static int x(StructurePlacementData data) {
		return data.getPosition().getX();
	}

	public static int y(StructurePlacementData data) {
		return data.getPosition().getY();
	}

	public static int z(StructurePlacementData data) {
		return data.getPosition().getZ();
	}

	public static void x(StructurePlacementData data, int x) {
		data.setPosition(new BlockPos(x, data.getPosition().getY(), data.getPosition().getZ()));
	}

	public static void y(StructurePlacementData data, int y) {
		data.setPosition(new BlockPos(data.getPosition().getX(), y, data.getPosition().getZ()));
	}

	public static void z(StructurePlacementData data, int z) {
		data.setPosition(new BlockPos(data.getPosition().getX(), data.getPosition().getY(), z));
	}

	public static void pos(StructurePlacementData data, int x, int y, int z) {
		data.setPosition(new BlockPos(x, y, z));
	}

	public static boolean spawnEntities(StructurePlacementData data) {
		return !data.shouldIgnoreEntities();
	}

	public static void spawnEntities(StructurePlacementData data, boolean spawnEntities) {
		data.setIgnoreEntities(!spawnEntities);
	}

	public static boolean placeFluids(StructurePlacementData data) {
		return data.shouldPlaceFluids();
	}

	public static void placeFluids(StructurePlacementData data, boolean placeFluids) {
		data.setPlaceFluids(placeFluids);
	}

	public static void addProcessors(StructurePlacementData data, StructureProcessorList processor) {
		data.getProcessors().addAll(processor.getList());
	}
}