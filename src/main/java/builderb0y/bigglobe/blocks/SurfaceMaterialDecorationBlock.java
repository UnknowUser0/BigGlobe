package builderb0y.bigglobe.blocks;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class SurfaceMaterialDecorationBlock extends Block implements Waterloggable {

	#if MC_VERSION >= MC_1_20_3

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public abstract MapCodec getCodec();
	#endif

	public SurfaceMaterialDecorationBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(Properties.WATERLOGGED, Boolean.FALSE));
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public abstract VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context);

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos downPos = pos.down();
		return world.getBlockState(downPos).isSideSolidFullSquare(world, downPos, Direction.UP);
	}

	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return this.getDefaultState().with(Properties.WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).isEqualAndStill(Fluids.WATER));
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public BlockState getStateForNeighborUpdate(
		#if MC_VERSION >= MC_1_21_2
			BlockState state,
			WorldView world,
			net.minecraft.world.tick.ScheduledTickView tickView,
			BlockPos pos,
			Direction direction,
			BlockPos neighborPos,
			BlockState neighborState,
			Random random
		#else
			BlockState state,
			Direction direction,
			BlockState neighborState,
			WorldAccess world,
			BlockPos pos,
			BlockPos neighborPos
		#endif
	) {
		if (state.get(Properties.WATERLOGGED)) {
			#if MC_VERSION >= MC_1_21_2 tickView #else world #endif .scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		if (direction == Direction.DOWN && !this.canPlaceAt(state, world, pos)) {
			return BlockStates.AIR;
		}
		return state;
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return (
			state.get(Properties.WATERLOGGED)
			? Fluids.WATER.getStill(false)
			: Fluids.EMPTY.getDefaultState()
		);
	}

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.WATERLOGGED);
	}
}