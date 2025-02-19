package builderb0y.bigglobe.blocks;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import builderb0y.bigglobe.codecs.BigGlobeAutoCodec;
import builderb0y.bigglobe.versions.BlockStateVersions;

public class OvergrownSandBlock extends FallingBlock implements Fertilizable {

	#if MC_VERSION >= MC_1_20_3
		public static final MapCodec<OvergrownSandBlock> CODEC = BigGlobeAutoCodec.AUTO_CODEC.createDFUMapCodec(OvergrownSandBlock.class);

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public MapCodec getCodec() {
			return CODEC;
		}
	#endif

	public OvergrownSandBlock(Settings settings) {
		super(settings);
	}

	@Override
	public int getColor(BlockState state, BlockView world, BlockPos pos) {
		return 0xDBD3A0;
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
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		((GrassBlock)(Blocks.GRASS_BLOCK)).grow(world, random, pos, state);
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		BlockPos upPos = pos.up();
		if (BlockStateVersions.isOpaqueFullCube(world.getBlockState(upPos), world, upPos)) {
			world.setBlockState(pos, BlockStates.SAND);
		}
	}
}