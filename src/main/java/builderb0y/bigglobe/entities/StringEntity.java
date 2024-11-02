package builderb0y.bigglobe.entities;

import java.util.*;
import java.util.function.ToDoubleFunction;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.World;

import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.items.BallOfStringItem;
import builderb0y.bigglobe.items.BigGlobeItems;
import builderb0y.bigglobe.math.BigGlobeMath;
import builderb0y.bigglobe.util.Directions;
import builderb0y.bigglobe.versions.EntityVersions;

public class StringEntity extends Entity {

	public static final RegistryKey<LootTable> LOOT_TABLE_KEY = RegistryKey.of(RegistryKeys.LOOT_TABLE, BigGlobeMod.modID("entities/string"));
	public static final TrackedData<Integer>
		PREVIOUS_ID = DataTracker.registerData(StringEntity.class, TrackedDataHandlerRegistry.INTEGER),
		NEXT_ID     = DataTracker.registerData(StringEntity.class, TrackedDataHandlerRegistry.INTEGER);

	//need to tick string entities in a very specific order.
	//minecraft doesn't guarantee that order, so I'm guaranteeing it manually.
	public static final WeakHashMap<World, ArrayList<StringEntity>> TO_TICK = new WeakHashMap<>();
	static {
		ServerTickEvents.END_WORLD_TICK.register(StringEntity::onWorldTickEnd);
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			initClient();
		}
	}

	@Environment(EnvType.CLIENT)
	public static void initClient() {
		ClientTickEvents.END_CLIENT_TICK.register((MinecraftClient client) -> {
			if (client.world != null) {
				onWorldTickEnd(client.world);
			}
		});
	}

	public static void onWorldTickEnd(World world) {
		ArrayList<StringEntity> entities = TO_TICK.get(world);
		if (entities != null && !entities.isEmpty()) {
			entities.forEach(StringEntity::tickAll);
			entities.clear();
		}
	}

	public CachedEntity
		prevEntity = this.new CachedEntity(PREVIOUS_ID),
		nextEntity = this.new CachedEntity(NEXT_ID);

	public StringEntity(EntityType<?> type, World world) {
		super(type, world);
		this.setSilent(true);
	}

	public StringEntity(EntityType<?> type, World world, double x, double y, double z) {
		super(type, world);
		this.setPosition(x, y, z);
		this.setSilent(true);
	}

	#if MC_VERSION >= MC_1_20_5

		@Override
		public void initDataTracker(DataTracker.Builder builder) {
			builder.add(PREVIOUS_ID, 0).add(NEXT_ID, 0);
		}

	#else

		@Override
		public void initDataTracker() {
			this.dataTracker.startTracking(PREVIOUS_ID, 0);
			this.dataTracker.startTracking(NEXT_ID, 0);
		}

	#endif

	public Box getVisibilityBoundingBox() {
		Entity next = this.getNextEntity();
		if (next != null) {
			return this.getBoundingBox().union(next.getBoundingBox());
		}
		else {
			return this.getBoundingBox();
		}
	}

	@Override
	public boolean canHit() {
		return true;
	}

	#if MC_VERSION >= MC_1_21_2

		@Override
		public boolean damage(ServerWorld world, DamageSource source, float amount) {
			if (this.isAlwaysInvulnerableTo(source)) {
				return false;
			}
			this.dropString(world, source);
			this.remove(RemovalReason.KILLED);
			return true;
		}

	#else

		@Override
		public boolean damage(DamageSource source, float amount) {
			if (this.isInvulnerableTo(source)) {
				return false;
			}
			if (this.getWorld() instanceof ServerWorld serverWorld) {
				this.dropString(serverWorld, source);
				this.remove(RemovalReason.KILLED);
			}
			return true;
		}

	#endif

	public void dropString(ServerWorld world, DamageSource damageSource) {
		world
		.getServer()
		.getReloadableRegistries()
		.getLootTable(LOOT_TABLE_KEY)
		.generateLoot(
			new LootWorldContext.Builder(world)
			.add(LootContextParameters.THIS_ENTITY, this)
			.add(LootContextParameters.ORIGIN, this.getPos())
			.add(LootContextParameters.DAMAGE_SOURCE, damageSource)
			.addOptional(LootContextParameters.ATTACKING_ENTITY, damageSource.getAttacker())
			.addOptional(LootContextParameters.DIRECT_ATTACKING_ENTITY, damageSource.getSource())
			.build(LootContextTypes.ENTITY),
			(ItemStack stack) -> this.dropStack(world, stack)
		);
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (player.getStackInHand(hand).getItem() == BigGlobeItems.BALL_OF_STRING && this.getNextEntity() == null) {
			this.setNextEntity(player);
			return ActionResult.SUCCESS;
		}
		else {
			return ActionResult.PASS;
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.getNextEntity() instanceof StringEntity)) {
			//last entity in the line.
			TO_TICK.computeIfAbsent(this.getWorld(), $ -> new ArrayList<>(4)).add(this);
		}
	}

	public void tickAll() {
		class Iter {

			public StringEntity current;

			public Iter(StringEntity current) {
				this.current = current;
			}

			public boolean next() {
				if (this.current.getNextEntity() instanceof StringEntity string) {
					this.current = string;
					return true;
				}
				return false;
			}

			public boolean prev() {
				if (this.current.getPrevEntity() instanceof StringEntity string && string.getPrevEntity() != null) {
					this.current = string;
					return true;
				}
				return false;
			}
		}

		if (this.getPrevEntity() instanceof StringEntity) {
			Iter iter = new Iter(this);
			//tug entities towards player first.
			do iter.current.moveTowards(iter.current.getNextEntity(), true);
			while (iter.prev());
			//then tug towards the start of the line.
			do iter.current.moveTowards(iter.current.getPrevEntity(), false);
			while (iter.next());
		}

		if (!this.getWorld().isClient) {
			this.maybeSplit();
		}
	}

	@Override
	public void baseTick() {
		this.prevEntity.update();
		this.nextEntity.update();
		super.baseTick();
		this.applyGravitationalVelocity();
	}

	public void applyGravitationalVelocity() {
		this.addVelocity(0.0D, -0.015625D, 0.0D);
		this.move(MovementType.SELF, this.getVelocity());
		if (EntityVersions.isOnGround(this)) {
			this.setVelocity(Vec3d.ZERO);
		}
	}

	public void moveTowards(Entity other, boolean slow) {
		if (other == null) return;
		Vec3d projection = project(this.getPos(), other.getPos(), slow);
		if (projection != null) {
			if (other instanceof StringEntity string) {
				//make 2 adjacent strings "pull" on each other.
				Vec3d shared = new Vec3d(0.0D, (this.getVelocity().y + string.getVelocity().y) * 0.5D, 0.0D);
				this.setVelocity(shared);
				string.setVelocity(shared);
			}
			else {
				this.setVelocity(Vec3d.ZERO);
			}
			this.moveLeniently(projection);
		}
	}

	public static @Nullable Vec3d project(Vec3d from, Vec3d onto, boolean slow) {
		double
			dx = onto.getX() - from.getX(),
			dy = onto.getY() - from.getY(),
			dz = onto.getZ() - from.getZ(),
			scalar = dx * dx + dy * dy + dz * dz;
		if (scalar > 1.0D) {
			scalar = Math.sqrt(scalar);
			//0.125 blocks per move operation seems to be the
			//max safe value for slabs, stairs, and corners.
			scalar = (slow ? Math.min(scalar - 1.0D, 0.125D) : (scalar - 1.0D)) / scalar;
			dx *= scalar;
			dy *= scalar;
			dz *= scalar;
			return new Vec3d(dx + from.x, dy + from.y, dz + from.z);
		}
		else {
			return null;
		}
	}

	public void moveLeniently(Vec3d to) {
		double scalar = this.getPos().squaredDistanceTo(to);
		if (!(scalar > 1.0E-7D)) return;
		this.setPosition(to);
		this.moveOutOfBlocks();
	}

	public void moveOutOfBlocks() {
		record PositionedVoxelShape(BlockPos pos, VoxelShape shape) {

			public PositionedVoxelShape {
				pos = pos.toImmutable();
			}
		}

		for (
			BlockCollisionSpliterator<PositionedVoxelShape> iterator = (
				new BlockCollisionSpliterator<>(
					this.getWorld(),
					this,
					this.getBoundingBox(),
					false,
					PositionedVoxelShape::new
				)
			);
			iterator.hasNext();
		) {
			PositionedVoxelShape next = iterator.next();
			BlockPos pos = next.pos();
			VoxelShape collision = next.shape();
			collision.forEachBox(
				(
					double collisionMinX,
					double collisionMinY,
					double collisionMinZ,
					double collisionMaxX,
					double collisionMaxY,
					double collisionMaxZ
				)
				-> {
					Box bounds = this.getBoundingBox();
					if (!bounds.intersects(collisionMinX, collisionMinY, collisionMinZ, collisionMaxX, collisionMaxY, collisionMaxZ)) return;

					double[] overlaps = new double[6];
					overlaps[Directions.POSITIVE_X.ordinal()] = collisionMaxX - bounds.minX;
					overlaps[Directions.POSITIVE_Y.ordinal()] = collisionMaxY - bounds.minY;
					overlaps[Directions.POSITIVE_Z.ordinal()] = collisionMaxZ - bounds.minZ;
					overlaps[Directions.NEGATIVE_X.ordinal()] = bounds.maxX - collisionMinX;
					overlaps[Directions.NEGATIVE_Y.ordinal()] = bounds.maxY - collisionMinY;
					overlaps[Directions.NEGATIVE_Z.ordinal()] = bounds.maxZ - collisionMinZ;

					Direction[] directions = Directions.ALL.clone();
					//sort by distance we need to move to escape the block in this direction.
					Arrays.sort(directions, Comparator.comparing((Direction direction) -> overlaps[direction.ordinal()]));

					Vec3d oldPos = this.getPos();
					//try to escape the block by moving the least distance first.
					for (Direction direction : directions) {
						double amount = overlaps[direction.ordinal()];
						Vec3d newPos = oldPos.add(direction.getOffsetX() * amount, direction.getOffsetY() * amount, direction.getOffsetZ() * amount);
						this.setPosition(newPos);
						Box newBounds = this.getBoundingBox();
						BlockPos offsetPos = pos.offset(direction);
						//check if moving on this axis would put us inside another block or not.
						if (
							VoxelShapes.matchesAnywhere(
								this
								.getWorld()
								.getBlockState(offsetPos)
								.getCollisionShape(this.getWorld(), offsetPos, ShapeContext.of(this)),

								VoxelShapes.cuboidUnchecked(
									newBounds.minX - offsetPos.getX(),
									newBounds.minY - offsetPos.getY(),
									newBounds.minZ - offsetPos.getZ(),
									newBounds.maxX - offsetPos.getX(),
									newBounds.maxY - offsetPos.getY(),
									newBounds.maxZ - offsetPos.getZ()
								),

								BooleanBiFunction.AND
							)
						) {
							//inside another block. revert to previous position
							//and try a different axis if one is available.
							this.setPosition(oldPos);
						}
						else {
							//successfully found a place where we can exist without colliding.
							//no need to try any more axes.
							break;
						}
					}
				}
			);
		}
	}

	public void maybeSplit() {
		Entity prevEntity = this.getPrevEntity();
		Entity nextEntity = this.getNextEntity();
		if (nextEntity instanceof PlayerEntity player) {
			double distanceSquared = this.getPos().squaredDistanceTo(
				player.getX(),
				MathHelper.clamp(
					this.getY(),
					player.getBoundingBox().minY - 1.0D,
					player.getBoundingBox().maxY
				),
				player.getZ()
			);
			if (distanceSquared > 4.0D) {
				if (tryTakeString(player, false)) {
					Vec3d target = project(
						player.getBoundingBox().getCenter(),
						this.getPos(),
						false
					);
					//should always be the case since distance was > 4 to begin with,
					//but just in case something weird happens,
					//it's better to handle this sanely.
					if (target != null) {
						StringEntity newEntity = new StringEntity(
							BigGlobeEntityTypes.STRING,
							EntityVersions.getWorld(this),
							target.x,
							target.y,
							target.z
						);
						newEntity.moveOutOfBlocks();
						this.setNextEntity(newEntity);
						newEntity.setPrevEntity(this);
						newEntity.setNextEntity(player);
						EntityVersions.getWorld(this).spawnEntity(newEntity);
					}
				}
				else {
					this.setNextEntity(null);
				}
			}
			else if (distanceSquared < 0.99D) {
				if (tryTakeString(player, true)) {
					if (prevEntity instanceof StringEntity string) {
						string.setNextEntity(player);
					}
					this.setPrevEntity(null);
					this.setNextEntity(null);
					this.discard();
				}
			}
		}
	}

	public static boolean tryTakeString(PlayerEntity player, boolean add) {
		PlayerInventory inventory = player.getInventory();
		for (int slot = 0, size = inventory.size(); slot < size; slot++) {
			ItemStack stack = inventory.getStack(slot);
			if (stack.getItem() == BigGlobeItems.BALL_OF_STRING) {
				if (add) {
					BallOfStringItem.addString(stack, 1);
					return true;
				}
				else {
					if (stack.getDamage() < stack.getMaxDamage()) {
						stack.setDamage(stack.getDamage() + 1);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		UUID prev = getUUID(nbt, "prev");
		if (prev != null) this.prevEntity.uuid = prev;
		UUID next = getUUID(nbt, "next");
		if (next != null) this.nextEntity.uuid = next;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt = super.writeNbt(nbt);
		putUUID(nbt, "prev", this.prevEntity.uuid);
		putUUID(nbt, "next", this.nextEntity.uuid);
		return nbt;
	}

	public static void putUUID(NbtCompound compound, String key, UUID uuid) {
		if (uuid != null) {
			compound.putLongArray(key, new long[] { uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() });
		}
	}

	public static @Nullable UUID getUUID(NbtCompound compound, String key) {
		long[] element = compound.getLongArray(key);
		if (element.length == 2) {
			return new UUID(element[0], element[1]);
		}
		else {
			return null;
		}
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {}

	public @Nullable Entity getPrevEntity() {
		return this.prevEntity.entity;
	}

	public @Nullable Entity getNextEntity() {
		return this.nextEntity.entity;
	}

	public void setPrevEntity(@Nullable Entity entity) {
		this.prevEntity.setEntity(entity);
	}

	public void setNextEntity(@Nullable Entity entity) {
		this.nextEntity.setEntity(entity);
	}

	public class CachedEntity {

		public final TrackedData<Integer> trackedID;
		public @Nullable Entity entity;
		public UUID uuid;

		public CachedEntity(TrackedData<Integer> trackedID) {
			this.trackedID = trackedID;
		}

		public Entity update() {
			World world = EntityVersions.getWorld(StringEntity.this);
			Entity entity;
			if (world.isClient) {
				Integer id = StringEntity.this.dataTracker.get(this.trackedID);
				entity = id == 0 ? null : world.getEntityById(id);
			}
			else {
				UUID uuid = this.uuid;
				entity = uuid == null ? null : ((ServerWorld)(world)).getEntity(uuid);
			}
			if (entity != null) {
				if (entity.squaredDistanceTo(StringEntity.this) > 256.0D) {
					this.uuid = null;
					entity = null;
				}
				else if (entity.isRemoved()) {
					entity = null;
				}
			}
			if (!world.isClient) {
				StringEntity.this.dataTracker.set(this.trackedID, entity != null ? entity.getId() : 0);
			}
			return this.entity = entity;
		}

		public void setEntity(Entity entity) {
			if (!EntityVersions.getWorld(StringEntity.this).isClient) {
				if (entity != null) {
					this.uuid = entity.getUuid();
					StringEntity.this.dataTracker.set(this.trackedID, entity.getId());
				}
				else {
					this.uuid = null;
					StringEntity.this.dataTracker.set(this.trackedID, 0);
				}
			}
		}
	}
}