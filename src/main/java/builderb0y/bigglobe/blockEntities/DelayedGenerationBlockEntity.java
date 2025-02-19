package builderb0y.bigglobe.blockEntities;

import java.util.Objects;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.blocks.BlockStates;
import builderb0y.bigglobe.features.SerializableBlockQueue;
import builderb0y.bigglobe.versions.BlockArgumentParserVersions;
import builderb0y.bigglobe.versions.BlockEntityVersions;

public class DelayedGenerationBlockEntity extends BlockEntity {

	public SerializableBlockQueue blockQueue;
	public @Nullable BlockState oldState;
	public @Nullable NbtCompound oldBlockData;

	public DelayedGenerationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public DelayedGenerationBlockEntity(BlockPos pos, BlockState state) {
		this(BigGlobeBlockEntityTypes.DELAYED_GENERATION, pos, state);
	}

	@SuppressWarnings("deprecation")
	public void tick() {
		BlockPos pos = this.pos;
		World world = Objects.requireNonNull(this.world, "world");
		if (this.blockQueue == null) {
			BigGlobeMod.LOGGER.warn("Missing block queue at " + pos);
			world.setBlockState(pos, BlockStates.AIR);
			return;
		}
		if (world.isRegionLoaded(this.blockQueue.minX, this.blockQueue.minY, this.blockQueue.minZ, this.blockQueue.maxX, this.blockQueue.maxY, this.blockQueue.maxZ)) {
			world.setBlockState(pos, this.oldState != null ? this.oldState : BlockStates.AIR);
			if (this.oldBlockData != null) {
				BlockEntity blockEntity = world.getBlockEntity(pos);
				if (blockEntity != null) BlockEntityVersions.readFromNbt(blockEntity, this.oldBlockData);
			}
			if (!this.blockQueue.hasSpace(world)) {
				return;
			}
			this.blockQueue.actuallyPlaceQueuedBlocks(world);
		}
	}

	@Override
	public void readNbt(NbtCompound nbt #if MC_VERSION >= MC_1_20_5 , RegistryWrapper.WrapperLookup wrapperLookup #endif) {
		super.readNbt(nbt #if MC_VERSION >= MC_1_20_5 , wrapperLookup #endif);
		try {
			this.blockQueue = SerializableBlockQueue.read(nbt.getCompound("queue"));
		}
		catch (RuntimeException exception) {
			BigGlobeMod.LOGGER.error("Error reading NBT data for delayed generation at " + this.pos, exception);
		}
		String oldState = nbt.getString("old_state");
		if (!oldState.isEmpty()) try {
			this.oldState = BlockArgumentParserVersions.block(oldState, false).blockState();
		}
		catch (CommandSyntaxException exception) {
			BigGlobeMod.LOGGER.error("", exception);
		}
		if (nbt.get("old_data") instanceof NbtCompound compound) {
			this.oldBlockData = compound;
		}
	}

	@Override
	public void writeNbt(NbtCompound nbt #if MC_VERSION >= MC_1_20_5 , RegistryWrapper.WrapperLookup wrapperLookup #endif) {
		super.writeNbt(nbt #if MC_VERSION >= MC_1_20_5 , wrapperLookup #endif);
		nbt.put("queue", this.blockQueue.toNBT());
		if (this.oldState != null) nbt.putString("old_state", BlockArgumentParser.stringifyBlockState(this.oldState));
		if (this.oldBlockData != null) nbt.put("old_data", this.oldBlockData);
	}
}