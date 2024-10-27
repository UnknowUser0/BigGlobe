package builderb0y.bigglobe.blocks;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.codecs.BigGlobeAutoCodec;
import builderb0y.bigglobe.versions.RegistryVersions;

public class AshenNetherrackBlock extends Block implements Fertilizable {

	public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_CHARRED_GRASS = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, BigGlobeMod.modID("patch_charred_grass"));

	#if MC_VERSION >= MC_1_20_3
		public static final MapCodec<AshenNetherrackBlock> CODEC = BigGlobeAutoCodec.AUTO_CODEC.createDFUMapCodec(AshenNetherrackBlock.class);

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public MapCodec getCodec() {
			return CODEC;
		}
	#endif

	public AshenNetherrackBlock(Settings settings) {
		super(settings);
	}

	@Override
	public boolean isFertilizable(
		WorldView world,
		BlockPos pos,
		BlockState state
		#if MC_VERSION < MC_1_20_2 , boolean isClient #endif
	) {
		return true;
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return world.getBlockState(pos.up()).isAir();
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		ConfiguredFeature<?, ?> feature = RegistryVersions.getObject(world.getRegistryManager(), PATCH_CHARRED_GRASS);
		if (feature != null) feature.generate(world, world.getChunkManager().getChunkGenerator(), random, pos.up());
	}
}