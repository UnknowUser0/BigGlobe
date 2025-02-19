package builderb0y.bigglobe.columns.scripted.entries;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.DefaultBoolean;
import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseCoder;
import builderb0y.autocodec.annotations.VerifyNullable;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.bigglobe.columns.scripted.*;
import builderb0y.bigglobe.columns.scripted.compile.DataCompileContext;
import builderb0y.bigglobe.columns.scripted.types.DoubleColumnValueType;
import builderb0y.bigglobe.columns.scripted.types.FloatColumnValueType;
import builderb0y.bigglobe.noise.Grid2D;
import builderb0y.bigglobe.noise.Grid3D;
import builderb0y.bigglobe.noise.NumberArray;
import builderb0y.bigglobe.noise.Permuter;
import builderb0y.bigglobe.settings.Seed;
import builderb0y.scripting.bytecode.MethodCompileContext;
import builderb0y.scripting.bytecode.MethodInfo;
import builderb0y.scripting.bytecode.tree.ConstantValue;
import builderb0y.scripting.bytecode.tree.InsnTree;
import builderb0y.scripting.bytecode.tree.instructions.binary.BitwiseXorInsnTree;
import builderb0y.scripting.bytecode.tree.instructions.casting.OpcodeCastInsnTree;
import builderb0y.scripting.environments.MutableScriptEnvironment;
import builderb0y.scripting.environments.MutableScriptEnvironment.CastResult;
import builderb0y.scripting.environments.MutableScriptEnvironment.FunctionHandler;
import builderb0y.scripting.parsing.ExpressionParser;
import builderb0y.scripting.parsing.ScriptParsingException;
import builderb0y.scripting.util.TypeInfos;

import static builderb0y.scripting.bytecode.InsnTrees.*;

@UseCoder(name = "new", in = NoiseColumnEntry.Coder.class, usage = MemberUsage.METHOD_IS_FACTORY)
public class NoiseColumnEntry extends AbstractColumnEntry {

	public static final ColumnEntryMemory.Key<ConstantValue>
		CONSTANT_GRID = new ColumnEntryMemory.Key<>("constantGrid");

	public final Grid2D grid2D;
	public final Grid3D grid3D;
	public final @Nullable Seed seed;

	public NoiseColumnEntry(
		AccessSchema params,
		@VerifyNullable Valid valid,
		@DefaultBoolean(true) boolean cache,
		Grid2D grid2D,
		Grid3D grid3D,
		@Nullable Seed seed,
		DecodeContext<?> decodeContext
	) {
		super(params, valid, cache, decodeContext);
		this.grid2D = grid2D;
		this.grid3D = grid3D;
		this.seed = seed;
		if (!(params.type() instanceof FloatColumnValueType || params.type() instanceof DoubleColumnValueType)) {
			throw new IllegalArgumentException("params for noise should be of type float or double.");
		}
	}

	@Override
	public void emitFieldGetterAndSetter(ColumnEntryMemory memory, DataCompileContext context) {
		memory.putTyped(CONSTANT_GRID, ConstantValue.ofManual(this.is3D() ? this.grid3D : this.grid2D, type(this.is3D() ? Grid3D.class : Grid2D.class)));
		super.emitFieldGetterAndSetter(memory, context);
	}

	public long getSeed(ColumnEntryMemory memory) {
		return this.seed != null ? this.seed.value : Permuter.permute(0L, memory.getTyped(ColumnEntryMemory.ACCESSOR_ID));
	}

	@Override
	public void populateComputeAll(ColumnEntryMemory memory, DataCompileContext context, MethodCompileContext computeAllMethod) {
		ConstantValue constantGrid = memory.getTyped(CONSTANT_GRID);
		computeAllMethod.setCode(
			"""
			grid.getBulkY(
				seed(salt),
				column.x(),
				valueField.minCached,
				column.z(),
				valueField.array.prefix(valueField.maxCached - valueField.minCached)
			)
			""",
			(MutableScriptEnvironment environment) -> {
				environment
				.addVariableConstant("grid", constantGrid)
				.addMethodInvoke(Grid3D.class, "getBulkY")
				.addVariable("column", context.loadColumn())
				.addFunction("seed", new FunctionHandler.Named("seed(long salt)", (ExpressionParser parser, String name, InsnTree... arguments) -> {
					return new CastResult(context.loadSeed(arguments[0]), false);
				}))
				.addVariableConstant("salt", this.getSeed(memory))
				.addMethodInvoke(ScriptedColumn.INFO.x)
				.addMethodInvoke(ScriptedColumn.INFO.z)
				.addVariableRenamedGetField(context.loadSelf(), "valueField", memory.getTyped(ColumnEntryMemory.FIELD).info)
				.addFieldGet(MappedRangeArray.INFO.minCached)
				.addFieldGet(MappedRangeArray.INFO.maxCached)
				.addFieldGet(MappedRangeNumberArray.ARRAY)
				.addMethodInvoke(NumberArray.class, "prefix")
				;
			}
		);
	}

	@Override
	public void populateCompute2D(ColumnEntryMemory memory, DataCompileContext context, MethodCompileContext computeMethod) throws ScriptParsingException {
		InsnTree x = ScriptedColumn.INFO.x(context.loadColumn());
		InsnTree z = ScriptedColumn.INFO.z(context.loadColumn());
		long salt = this.getSeed(memory);
		InsnTree originalSeed = ScriptedColumn.INFO.baseSeed(context.loadColumn());
		InsnTree saltedSeed = new BitwiseXorInsnTree(originalSeed, ldc(salt), LXOR);
		InsnTree getValueInvoker = invokeInstance(ldc(memory.getTyped(CONSTANT_GRID)), MethodInfo.getMethod(this.is3D() ? Grid3D.class : Grid2D.class, "getValue"), saltedSeed, x, z);
		if (this.params.type() instanceof FloatColumnValueType) getValueInvoker = new OpcodeCastInsnTree(getValueInvoker, D2F, TypeInfos.FLOAT);
		return_(getValueInvoker).emitBytecode(computeMethod);
		computeMethod.endCode();
	}

	@Override
	public void populateCompute3D(ColumnEntryMemory memory, DataCompileContext context, MethodCompileContext computeMethod) throws ScriptParsingException {
		computeMethod.setCode(
			this.params.type() instanceof FloatColumnValueType
			? "return(float(grid.getValue(column.seed # salt, column.x, y, column.z)))"
			: "return(grid.getValue(column.seed # salt, column.x, y, column.z))",
			(MutableScriptEnvironment environment) -> {
				environment
				.addVariableConstant("grid", memory.getTyped(CONSTANT_GRID))
				.addMethodInvoke(this.is3D() ? Grid3D.class : Grid2D.class, "getValue")
				.addVariable("column", context.loadColumn())
				.addFieldInvoke("seed", ScriptedColumn.INFO.baseSeed)
				.addVariableConstant("salt", this.getSeed(memory))
				.addFieldInvoke(ScriptedColumn.INFO.x)
				.addVariableLoad("y", TypeInfos.INT)
				.addFieldInvoke(ScriptedColumn.INFO.z)
				;
			}
		);
	}

	/** hacks to make 2 java fields share the same JSON field. */
	public static class Coder extends NamedCoder<NoiseColumnEntry> {

		public final AutoCoder<AccessSchema> params;
		public final AutoCoder<Valid> valid;
		public final AutoCoder<Boolean> cache;
		public final AutoCoder<Grid2D> grid2D;
		public final AutoCoder<Grid3D> grid3D;
		public final AutoCoder<Seed> seed;

		public Coder(FactoryContext<NoiseColumnEntry> context) {
			super(context.type);
			this.params = context.type(ReifiedType.from(AccessSchema.class)).forceCreateCoder();
			this.valid  = context.type(ReifiedType.from(Valid.class).addAnnotation(VerifyNullable.INSTANCE)).forceCreateCoder();
			this.cache  = context.type(new ReifiedType<@DefaultBoolean(true) Boolean>() {}).forceCreateCoder();
			this.grid2D = context.type(ReifiedType.from(Grid2D.class)).forceCreateCoder();
			this.grid3D = context.type(ReifiedType.from(Grid3D.class)).forceCreateCoder();
			this.seed   = context.type(new ReifiedType<Seed>() {}).forceCreateCoder();
		}

		@Override
		public <T_Encoded> @Nullable NoiseColumnEntry decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			AccessSchema params = context.getMember("params").decodeWith(this.params);
			Valid valid = context.getMember("valid").decodeWith(this.valid);
			boolean cache = context.getMember("cache").decodeWith(this.cache);
			DecodeContext<T_Encoded> encodedSeed = context.getMember("seed");
			Seed seed = encodedSeed.isEmpty() ? null : encodedSeed.decodeWith(this.seed);
			if (params.is_3d()) {
				Grid3D grid = context.getMember("grid").decodeWith(this.grid3D);
				return new NoiseColumnEntry(params, valid, cache, null, grid, seed, context);
			}
			else {
				Grid2D grid = context.getMember("grid").decodeWith(this.grid2D);
				return new NoiseColumnEntry(params, valid, cache, grid, null, seed, context);
			}
		}

		@Override
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, NoiseColumnEntry> context) throws EncodeException {
			NoiseColumnEntry entry = context.object;
			if (entry == null) return context.empty();
			Map<String, T_Encoded> map = new HashMap<>(8);
			map.put("params", context.object(entry.params).encodeWith(this.params));
			if (entry.valid != null) map.put("valid", context.object(entry.valid).encodeWith(this.valid));
			if (!entry.cache) map.put("cache", context.createBoolean(false));
			map.put(
				"grid",
				entry.params.is_3d()
				? context.object(entry.grid3D).encodeWith(this.grid3D)
				: context.object(entry.grid2D).encodeWith(this.grid2D)
			);
			if (entry.seed != null) map.put("seed", context.object(entry.seed).encodeWith(this.seed));
			return context.createStringMap(map);
		}
	}
}