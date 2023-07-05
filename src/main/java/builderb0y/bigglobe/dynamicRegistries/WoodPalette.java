package builderb0y.bigglobe.dynamicRegistries;

import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Range;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.*;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.tag.TagKey;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import builderb0y.autocodec.annotations.*;
import builderb0y.autocodec.verifiers.VerifyContext;
import builderb0y.autocodec.verifiers.VerifyException;
import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.randomLists.IRandomList;
import builderb0y.bigglobe.util.ServerValue;
import builderb0y.bigglobe.util.UnregisteredObjectException;
import builderb0y.bigglobe.versions.RegistryKeyVersions;

@SuppressWarnings("unused")
@UseVerifier(name = "verify", usage = MemberUsage.METHOD_IS_HANDLER)
public class WoodPalette {

	public static final ServerValue<Map<RegistryKey<Biome>, List<RegistryEntry<WoodPalette>>>>
		BIOME_CACHE = new ServerValue<>(WoodPalette::computeBiomeCache);

	public final EnumMap<WoodPaletteType, @SingletonArray IRandomList<@UseName("block") Block>> blocks;
	public final @VerifyNullable RegistryEntry<ConfiguredFeature<?, ?>> sapling_grow_feature;
	/** a tag containing biomes whose trees are made of this wood palette. */
	public final @VerifyNullable TagKey<Biome> biomes;
	public transient Set<RegistryKey<Biome>> biomeSet;

	public WoodPalette(
		EnumMap<WoodPaletteType, IRandomList<Block>> blocks,
		@VerifyNullable RegistryEntry<ConfiguredFeature<?, ?>> sapling_grow_feature,
		@VerifyNullable TagKey<Biome> biomes
	) {
		this.blocks = blocks;
		this.sapling_grow_feature = sapling_grow_feature;
		this.biomes = biomes;
	}

	public static <T_Encoded> void verify(VerifyContext<T_Encoded, WoodPalette> context) throws VerifyException {
		//fast check.
		WoodPalette palette = context.object;
		if (palette != null && palette.blocks.size() != WoodPaletteType.VALUES.length) {
			//slow print.
			context.logger().logErrorLazy(() -> {
				StringBuilder builder = new StringBuilder("WoodPalette is missing blocks: ");
				for (WoodPaletteType type : WoodPaletteType.VALUES) {
					if (!palette.blocks.containsKey(type)) {
						builder.append(type.lowerCaseName).append(", ");
					}
				}
				builder.setLength(builder.length() - 2);
				return builder.toString();
			});
		}
	}

	public Set<RegistryKey<Biome>> getBiomeSet() {
		if (this.biomeSet == null) {
			if (this.biomes != null) {
				Optional<RegistryEntryList.Named<Biome>> list = BigGlobeMod.getCurrentServer().getRegistryManager().get(RegistryKeyVersions.biome()).getEntryList(this.biomes);
				if (list.isPresent()) {
					this.biomeSet = list.get().stream().map(UnregisteredObjectException::getKey).collect(Collectors.toSet());
				}
				else {
					this.biomeSet = Collections.emptySet();
				}
			}
			else {
				this.biomeSet = Collections.emptySet();
			}
		}
		return this.biomeSet;
	}

	public static Map<RegistryKey<Biome>, List<RegistryEntry<WoodPalette>>> computeBiomeCache() {
		Map<RegistryKey<Biome>, List<RegistryEntry<WoodPalette>>> map = new HashMap<>();
		BigGlobeMod
		.getCurrentServer()
		.getRegistryManager()
		.get(BigGlobeDynamicRegistries.WOOD_PALETTE_REGISTRY_KEY)
		.streamEntries()
		.sequential()
		.forEach((RegistryEntry<WoodPalette> entry) -> {
			entry.value().getBiomeSet().forEach((RegistryKey<Biome> key) -> {
				map.computeIfAbsent(key, $ -> new ArrayList<>(8)).add(entry);
			});
		});
		return map;
	}

	//////////////////////////////// block ////////////////////////////////

	public Block getBlock(RandomGenerator random, WoodPaletteType type) {
		Block block = this.getBlocks(type).getRandomElement(random);
		if (block != null) return block;
		else throw new IllegalStateException("WoodPaletteType not present: " + type);
	}

	public Block logBlock          (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.LOG           ); }
	public Block woodBlock         (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.WOOD          ); }
	public Block strippedLogBlock  (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.STRIPPED_LOG  ); }
	public Block strippedWoodBlock (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.STRIPPED_WOOD ); }
	public Block planksBlock       (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.PLANKS        ); }
	public Block stairsBlock       (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.STAIRS        ); }
	public Block slabBlock         (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.SLAB          ); }
	public Block fenceBlock        (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.FENCE         ); }
	public Block fenceGateBlock    (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.FENCE_GATE    ); }
	public Block doorBlock         (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.DOOR          ); }
	public Block trapdoorBlock     (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.TRAPDOOR      ); }
	public Block pressurePlateBlock(RandomGenerator random) { return this.getBlock(random, WoodPaletteType.PRESSURE_PLATE); }
	public Block buttonBlock       (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.BUTTON        ); }
	public Block leavesBlock       (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.LEAVES        ); }
	public Block saplingBlock      (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.SAPLING       ); }
	public Block pottedSaplingBlock(RandomGenerator random) { return this.getBlock(random, WoodPaletteType.POTTED_SAPLING); }
	public Block standingSignBlock (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.STANDING_SIGN ); }
	public Block wallSignBlock     (RandomGenerator random) { return this.getBlock(random, WoodPaletteType.WALL_SIGN     ); }

	//////////////////////////////// blocks ////////////////////////////////

	public IRandomList<Block> getBlocks(WoodPaletteType type) {
		IRandomList<Block> block = this.blocks.get(type);
		if (block != null) return block;
		else throw new IllegalStateException("WoodPaletteType not present: " + type);
	}

	public IRandomList<Block> logBlocks          () { return this.getBlocks(WoodPaletteType.LOG           ); }
	public IRandomList<Block> woodBlocks         () { return this.getBlocks(WoodPaletteType.WOOD          ); }
	public IRandomList<Block> strippedLogBlocks  () { return this.getBlocks(WoodPaletteType.STRIPPED_LOG  ); }
	public IRandomList<Block> strippedWoodBlocks () { return this.getBlocks(WoodPaletteType.STRIPPED_WOOD ); }
	public IRandomList<Block> planksBlocks       () { return this.getBlocks(WoodPaletteType.PLANKS        ); }
	public IRandomList<Block> stairsBlocks       () { return this.getBlocks(WoodPaletteType.STAIRS        ); }
	public IRandomList<Block> slabBlocks         () { return this.getBlocks(WoodPaletteType.SLAB          ); }
	public IRandomList<Block> fenceBlocks        () { return this.getBlocks(WoodPaletteType.FENCE         ); }
	public IRandomList<Block> fenceGateBlocks    () { return this.getBlocks(WoodPaletteType.FENCE_GATE    ); }
	public IRandomList<Block> doorBlocks         () { return this.getBlocks(WoodPaletteType.DOOR          ); }
	public IRandomList<Block> trapdoorBlocks     () { return this.getBlocks(WoodPaletteType.TRAPDOOR      ); }
	public IRandomList<Block> pressurePlateBlocks() { return this.getBlocks(WoodPaletteType.PRESSURE_PLATE); }
	public IRandomList<Block> buttonBlocks       () { return this.getBlocks(WoodPaletteType.BUTTON        ); }
	public IRandomList<Block> leavesBlocks       () { return this.getBlocks(WoodPaletteType.LEAVES        ); }
	public IRandomList<Block> saplingBlocks      () { return this.getBlocks(WoodPaletteType.SAPLING       ); }
	public IRandomList<Block> pottedSaplingBlocks() { return this.getBlocks(WoodPaletteType.POTTED_SAPLING); }
	public IRandomList<Block> standingSignBlocks () { return this.getBlocks(WoodPaletteType.STANDING_SIGN ); }
	public IRandomList<Block> wallSignBlocks     () { return this.getBlocks(WoodPaletteType.WALL_SIGN     ); }

	//////////////////////////////// states ////////////////////////////////

	public static <V extends Comparable<V>> BlockState withIfExists(BlockState state, Property<V> property, V value) {
		return state.contains(property) ? state.with(property, value) : state;
	}

	public BlockState getState(RandomGenerator random, WoodPaletteType type) {
		return this.getBlock(random, type).getDefaultState();
	}

	public BlockState logState(RandomGenerator random, Axis axis) {
		BlockState state = this.getState(random, WoodPaletteType.LOG);
		state = withIfExists(state, Properties.AXIS, axis);
		return state;
	}

	public BlockState woodState(RandomGenerator random, Axis axis) {
		BlockState state = this.getState(random, WoodPaletteType.WOOD);
		state = withIfExists(state, Properties.AXIS, axis);
		return state;
	}

	public BlockState strippedLogState(RandomGenerator random, Axis axis) {
		BlockState state = this.getState(random, WoodPaletteType.STRIPPED_LOG);
		state = withIfExists(state, Properties.AXIS, axis);
		return state;
	}

	public BlockState strippedWoodState(RandomGenerator random, Axis axis) {
		BlockState state = this.getState(random, WoodPaletteType.STRIPPED_WOOD);
		state = withIfExists(state, Properties.AXIS, axis);
		return state;
	}

	public BlockState planksState(RandomGenerator random) {
		BlockState state = this.getState(random, WoodPaletteType.PLANKS);
		return state;
	}

	public BlockState stairsState(RandomGenerator random, Direction facing, BlockHalf half, StairShape shape, boolean waterlogged) {
		BlockState state = this.getState(random, WoodPaletteType.STAIRS);
		state = withIfExists(state, Properties.HORIZONTAL_FACING, facing);
		state = withIfExists(state, Properties.BLOCK_HALF, half);
		state = withIfExists(state, Properties.STAIR_SHAPE, shape);
		state = withIfExists(state, Properties.WATERLOGGED, waterlogged);
		return state;
	}

	public BlockState slabState(RandomGenerator random, BlockHalf half, boolean waterlogged) {
		BlockState state = this.getState(random, WoodPaletteType.SLAB);
		state = withIfExists(state, Properties.BLOCK_HALF, half);
		state = withIfExists(state, Properties.WATERLOGGED, waterlogged);
		return state;
	}

	public BlockState fenceState(RandomGenerator random, boolean north, boolean east, boolean south, boolean west, boolean waterlogged) {
		BlockState state = this.getState(random, WoodPaletteType.FENCE);
		state = withIfExists(state, Properties.NORTH, north);
		state = withIfExists(state, Properties.EAST, east);
		state = withIfExists(state, Properties.SOUTH, south);
		state = withIfExists(state, Properties.WEST, west);
		state = withIfExists(state, Properties.WATERLOGGED, waterlogged);
		return state;
	}

	public BlockState fenceGateState(RandomGenerator random, Direction facing, boolean open, boolean in_wall, boolean powered) {
		BlockState state = this.getState(random, WoodPaletteType.FENCE_GATE);
		state = withIfExists(state, Properties.HORIZONTAL_FACING, facing);
		state = withIfExists(state, Properties.OPEN, open);
		state = withIfExists(state, Properties.IN_WALL, in_wall);
		state = withIfExists(state, Properties.POWERED, powered);
		return state;
	}

	public BlockState doorState(RandomGenerator random, Direction facing, DoubleBlockHalf half, DoorHinge hinge, boolean open, boolean powered) {
		BlockState state = this.getState(random, WoodPaletteType.DOOR);
		state = withIfExists(state, Properties.HORIZONTAL_FACING, facing);
		state = withIfExists(state, Properties.DOUBLE_BLOCK_HALF, half);
		state = withIfExists(state, Properties.DOOR_HINGE, hinge);
		state = withIfExists(state, Properties.OPEN, open);
		state = withIfExists(state, Properties.POWERED, powered);
		return state;
	}

	public BlockState trapdoorState(RandomGenerator random, Direction facing, BlockHalf half, boolean open, boolean powered, boolean waterlogged) {
		BlockState state = this.getState(random, WoodPaletteType.TRAPDOOR);
		state = withIfExists(state, Properties.HORIZONTAL_FACING, facing);
		state = withIfExists(state, Properties.BLOCK_HALF, half);
		state = withIfExists(state, Properties.OPEN, open);
		state = withIfExists(state, Properties.POWERED, powered);
		state = withIfExists(state, Properties.WATERLOGGED, waterlogged);
		return state;
	}

	public BlockState pressurePlateState(RandomGenerator random, boolean powered) {
		BlockState state = this.getState(random, WoodPaletteType.PRESSURE_PLATE);
		state = withIfExists(state, Properties.POWERED, powered);
		return state;
	}

	public BlockState buttonState(RandomGenerator random, WallMountLocation face, Direction facing, boolean powered) {
		BlockState state = this.getState(random, WoodPaletteType.BUTTON);
		state = withIfExists(state, Properties.WALL_MOUNT_LOCATION, face);
		state = withIfExists(state, Properties.HORIZONTAL_FACING, facing);
		state = withIfExists(state, Properties.POWERED, powered);
		return state;
	}

	public BlockState leavesState(RandomGenerator random, @Range(from = 1, to = 7) int distance, boolean persistent, boolean waterlogged) {
		BlockState state = this.getState(random, WoodPaletteType.LEAVES);
		state = withIfExists(state, Properties.DISTANCE_1_7, distance);
		state = withIfExists(state, Properties.PERSISTENT, persistent);
		state = withIfExists(state, Properties.WATERLOGGED, waterlogged);
		return state;
	}

	public BlockState saplingState(RandomGenerator random, @Range(from = 0, to = 1) int stage) {
		BlockState state = this.getState(random, WoodPaletteType.SAPLING);
		state = withIfExists(state, Properties.STAGE, stage);
		return state;
	}

	public BlockState pottedSaplingState(RandomGenerator random) {
		return this.getState(random, WoodPaletteType.POTTED_SAPLING);
	}

	public BlockState standingSignState(RandomGenerator random, int rotation, boolean waterlogged) {
		BlockState state = this.getState(random, WoodPaletteType.STANDING_SIGN);
		state = withIfExists(state, Properties.ROTATION, rotation);
		state = withIfExists(state, Properties.WATERLOGGED, waterlogged);
		return state;
	}

	public BlockState wallSignState(RandomGenerator random, Direction facing, boolean waterlogged) {
		BlockState state = this.getState(random, WoodPaletteType.WALL_SIGN);
		state = withIfExists(state, Properties.HORIZONTAL_FACING, facing);
		state = withIfExists(state, Properties.WATERLOGGED, waterlogged);
		return state;
	}

	//////////////////////////////// types ////////////////////////////////

	public static enum WoodPaletteType implements StringIdentifiable {
		LOG,
		WOOD,
		STRIPPED_LOG,
		STRIPPED_WOOD,
		PLANKS,
		STAIRS,
		SLAB,
		FENCE,
		FENCE_GATE,
		DOOR,
		TRAPDOOR,
		PRESSURE_PLATE,
		BUTTON,
		LEAVES,
		SAPLING,
		POTTED_SAPLING,
		STANDING_SIGN,
		WALL_SIGN;

		public static final WoodPaletteType[] VALUES = values();
		public static final Map<String, WoodPaletteType> LOWER_CASE_LOOKUP = (
			Arrays
			.stream(VALUES)
			.collect(
				Collectors.toMap(
					(WoodPaletteType type) -> type.lowerCaseName,
					Function.identity()
				)
			)
		);

		public final String lowerCaseName = this.name().toLowerCase(Locale.ROOT);

		@Override
		public String asString() {
			return this.lowerCaseName;
		}
	}
}