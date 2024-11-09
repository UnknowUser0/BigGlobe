package builderb0y.bigglobe.entities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import builderb0y.bigglobe.BigGlobeMod;
import builderb0y.bigglobe.entities.WaypointEntity.Orbit;
import builderb0y.bigglobe.math.BigGlobeMath;

@Environment(EnvType.CLIENT)
public class WaypointEntityRenderer extends BigGlobeEntityRenderer<WaypointEntity, WaypointEntityRenderer.State> {

	public static final Identifier TEXTURE = BigGlobeMod.mcID("textures/particle/flash.png");

	public WaypointEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	public void doRender(WaypointEntityRenderer.State state, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light) {
		VertexConsumer buffer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucentEmissive(TEXTURE));
		int fullbright = LightmapTextureManager.pack(15, LightmapTextureManager.getSkyLightCoordinates(light));
		Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().subtract(state.x, state.y, state.z);
		Vector3f normal = new Vector3f().set(camera.x, camera.y, camera.z).normalize();
		Vector3f unit1 = new Vector3f(normal).cross(0.0F, 1.0F, 0.0F).normalize();
		Vector3f unit2 = new Vector3f(unit1).cross(normal).normalize();
		Vector3f scratch = new Vector3f();
		int maxOrbits = BigGlobeMath.roundI(state.health / WaypointEntity.MAX_HEALTH * state.orbits.length);
		for (int i = 0; i < maxOrbits; i++) {
			Orbit orbit = state.orbits[i];
			for (int history = 0; history < 16; history++) {
				orbit.getPosition(scratch, history);
				float size = history * (-1.0F / 16.0F / 16.0F) + 0.0625F;

				buffer
				.vertex(
					matrices.peek().getPositionMatrix(),
					scratch.x + (unit1.x + unit2.x) * size,
					scratch.y + (unit1.y + unit2.y) * size + 1.0F,
					scratch.z + (unit1.z + unit2.z) * size
				)
				.color(orbit.color)
				.texture(0.0F, 0.0F)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(fullbright)
				.normal(matrices.peek() #if MC_VERSION < MC_1_20_5 .getNormalMatrix() #endif, 0.0F, 1.0F, 0.0F)
				#if MC_VERSION < MC_1_21_0 .next() #endif
				;

				buffer
				.vertex(
					matrices.peek().getPositionMatrix(),
					scratch.x + (unit1.x - unit2.x) * size,
					scratch.y + (unit1.y - unit2.y) * size + 1.0F,
					scratch.z + (unit1.z - unit2.z) * size
				)
				.color(orbit.color)
				.texture(0.0F, 1.0F)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(fullbright)
				.normal(matrices.peek() #if MC_VERSION < MC_1_20_5 .getNormalMatrix() #endif, 0.0F, 1.0F, 0.0F)
				#if MC_VERSION < MC_1_21_0 .next() #endif
				;

				buffer
				.vertex(
					matrices.peek().getPositionMatrix(),
					scratch.x + (-unit1.x - unit2.x) * size,
					scratch.y + (-unit1.y - unit2.y) * size + 1.0F,
					scratch.z + (-unit1.z - unit2.z) * size
				)
				.color(orbit.color)
				.texture(1.0F, 1.0F)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(fullbright)
				.normal(matrices.peek() #if MC_VERSION < MC_1_20_5 .getNormalMatrix() #endif, 0.0F, 1.0F, 0.0F)
				#if MC_VERSION < MC_1_21_0 .next() #endif
				;

				buffer
				.vertex(
					matrices.peek().getPositionMatrix(),
					scratch.x + (-unit1.x + unit2.x) * size,
					scratch.y + (-unit1.y + unit2.y) * size + 1.0F,
					scratch.z + (-unit1.z + unit2.z) * size
				)
				.color(orbit.color)
				.texture(1.0F, 0.0F)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(fullbright)
				.normal(matrices.peek() #if MC_VERSION < MC_1_20_5 .getNormalMatrix() #endif, 0.0F, 1.0F, 0.0F)
				#if MC_VERSION < MC_1_21_0 .next() #endif
				;
			}
		}
	}

	#if MC_VERSION < MC_1_20_4
		//1.20.4 has this built in, so this override is only necessary in older versions.
		@Override
		public boolean hasLabel(WaypointEntity entity) {
			return (
				entity.shouldRenderName() || (
					entity.hasCustomName() &&
					MinecraftClient.getInstance().crosshairTarget instanceof EntityHitResult hit &&
					hit.getEntity() == entity
				)
			);
		}
	#endif

	#if MC_VERSION < MC_1_21_2

		@Override
		public Identifier getTexture(WaypointEntity entity) {
			return TEXTURE;
		}

	#endif

	@Override
	public WaypointEntityRenderer.State createState() {
		return new State();
	}

	@Override
	public void updateState(WaypointEntity entity, WaypointEntityRenderer.State state, float partialTicks) {
		state.health = entity.health;
		state.orbits = entity.orbits;
	}

	public static class State extends BigGlobeEntityRenderer.State {

		public float age, health;
		public WaypointEntity.Orbit[] orbits;
	}
}