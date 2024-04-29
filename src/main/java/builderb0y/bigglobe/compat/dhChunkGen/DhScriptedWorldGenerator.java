package builderb0y.bigglobe.compat.dhChunkGen;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.worldGeneration.EDhApiDistantGeneratorMode;
import com.seibel.distanthorizons.api.enums.worldGeneration.EDhApiWorldGeneratorReturnType;
import com.seibel.distanthorizons.api.interfaces.override.worldGenerator.IDhApiWorldGenerator;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;
import com.seibel.distanthorizons.api.objects.data.DhApiChunk;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.BiomeKeys;

import builderb0y.autocodec.util.AutoCodecUtil;
import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.chunkgen.BigGlobeScriptedChunkGenerator;
import builderb0y.bigglobe.chunkgen.scripted.BlockSegmentList;
import builderb0y.bigglobe.chunkgen.scripted.BlockSegmentList.LitSegment;
import builderb0y.bigglobe.chunkgen.scripted.RootLayer;
import builderb0y.bigglobe.columns.scripted.ScriptedColumn;
import builderb0y.bigglobe.columns.scripted.ScriptedColumn.Purpose;
import builderb0y.bigglobe.util.AsyncRunner;
import builderb0y.bigglobe.util.BigGlobeThreadPool;
import builderb0y.bigglobe.versions.RegistryKeyVersions;

public class DhScriptedWorldGenerator implements IDhApiWorldGenerator {

	public final IDhApiLevelWrapper level;
	public final ServerWorld serverWorld;
	public final BigGlobeScriptedChunkGenerator chunkGenerator;
	public final AtomicInteger runningCount, maxRunningCount;

	public DhScriptedWorldGenerator(
		IDhApiLevelWrapper level,
		ServerWorld serverWorld,
		BigGlobeScriptedChunkGenerator chunkGenerator
	) {
		this.level = level;
		this.serverWorld = serverWorld;
		this.chunkGenerator = chunkGenerator;
		this.runningCount = new AtomicInteger();
		this.maxRunningCount = new AtomicInteger();
	}

	@Override
	public boolean isBusy() {
		int runningCount = this.runningCount.get();
		int maxRunningCount = runningCount == 0 ? this.maxRunningCount.incrementAndGet() : this.maxRunningCount.get();
		return runningCount >= maxRunningCount;
	}

	@Override
	public CompletableFuture<Void> generateApiChunks(int chunkPosMinX, int chunkPosMinZ, byte granularity, byte targetDataDetail, EDhApiDistantGeneratorMode generatorMode, ExecutorService worldGeneratorThreadPool, Consumer<DhApiChunk> resultConsumer) {
		this.runningCount.getAndIncrement();
		return CompletableFuture.runAsync(
			() -> {
				try {
					int chunkPosMaxX = chunkPosMinX + (1 << (granularity - 4));
					int chunkPosMaxZ = chunkPosMinZ + (1 << (granularity - 4));
					for (int chunkZ = chunkPosMinZ; chunkZ < chunkPosMaxZ; chunkZ++) {
						for (int chunkX = chunkPosMinX; chunkX < chunkPosMaxX; chunkX++) {
							try {
								resultConsumer.accept(this.generateChunkOfDataPoints(chunkX, chunkZ));
							}
							catch (Throwable throwable) {
								BigGlobeMod.LOGGER.error("An error occurred in a hyperspeed DH world generator: ", throwable);
								throw AutoCodecUtil.rethrow(throwable);
							}
						}
					}
				}
				finally {
					this.runningCount.getAndDecrement();
				}
			},
			worldGeneratorThreadPool
		);
	}

	public DhApiChunk generateChunkOfDataPoints(int chunkX, int chunkZ) {
		int chunkBottomY = this.chunkGenerator.height.min_y();
		int chunkTopY    = this.chunkGenerator.height.max_y();
		DhApiChunk results = new DhApiChunk(chunkX, chunkZ, chunkBottomY, chunkTopY);
		for (int index = 0; index < 256; index++) {
			//populate early to make sanity checking happen earlier.
			//we will mutate this array later.
			results.setDataPoints(index & 15, index >>> 4, new DataPointListBuilder(this.level, (byte)(0)));
		}
		int startX = chunkX << 4;
		int startZ = chunkZ << 4;
		ScriptedColumn.Params params = new ScriptedColumn.Params(this.chunkGenerator, 0, 0, Purpose.RAW_DH);
		try (AsyncRunner async = BigGlobeThreadPool.lodRunner()) {
			for (int offsetZ = 0; offsetZ < 16; offsetZ += 2) {
				final int offsetZ_ = offsetZ;
				for (int offsetX = 0; offsetX < 16; offsetX += 2) {
					final int offsetX_ = offsetX;
					async.submit(() -> {
						ScriptedColumn.Factory factory = this.chunkGenerator.columnEntryRegistry.columnFactory;
						int minY = this.chunkGenerator.height.min_y();
						int maxY = this.chunkGenerator.height.max_y();
						RootLayer layer = this.chunkGenerator.layer;
						int quadX = startX | offsetX_;
						int quadZ = startZ | offsetZ_;
						ScriptedColumn
							column00 = factory.create(params.at(quadX,     quadZ    )),
							column01 = factory.create(params.at(quadX | 1, quadZ    )),
							column10 = factory.create(params.at(quadX,     quadZ | 1)),
							column11 = factory.create(params.at(quadX | 1, quadZ | 1));
						BlockSegmentList
							list00 = new BlockSegmentList(minY, maxY),
							list01 = new BlockSegmentList(minY, maxY),
							list10 = new BlockSegmentList(minY, maxY),
							list11 = new BlockSegmentList(minY, maxY);
						layer.emitSegments(column00, column01, column10, column11, list00);
						layer.emitSegments(column01, column00, column11, column10, list01);
						layer.emitSegments(column10, column11, column00, column01, list10);
						layer.emitSegments(column11, column10, column01, column00, list11);
						this.convertToDataPoints((DataPointListBuilder)(results.getDataPoints(offsetX_,     offsetZ_    )), list00);
						this.convertToDataPoints((DataPointListBuilder)(results.getDataPoints(offsetX_ ^ 1, offsetZ_    )), list01);
						this.convertToDataPoints((DataPointListBuilder)(results.getDataPoints(offsetX_,     offsetZ_ ^ 1)), list10);
						this.convertToDataPoints((DataPointListBuilder)(results.getDataPoints(offsetX_ ^ 1, offsetZ_ ^ 1)), list11);
					});
				}
			}
		}
		return results;
	}

	public void convertToDataPoints(DataPointListBuilder builder, BlockSegmentList segments) {
		builder.query[0] = this.serverWorld.getRegistryManager().get(RegistryKeyVersions.biome()).entryOf(BiomeKeys.PLAINS);
		builder.biome = DhApi.Delayed.wrapperFactory.getBiomeWrapper(builder.query, this.level);
		segments.computeLightLevels();
		for (int index = segments.size(); --index >= 0;) {
			LitSegment segment = segments.getLit(index);
			if (segment.value.isAir()) continue;
			builder.skyLightLevel = segment.lightLevel;
			builder.add(segment.value, segment.minY, segment.maxY + 1);
		}
	}

	@Override
	public EDhApiWorldGeneratorReturnType getReturnType() {
		return EDhApiWorldGeneratorReturnType.API_CHUNKS;
	}

	@Override
	public void preGeneratorTaskStart() {

	}

	@Override
	public void close() {

	}
}