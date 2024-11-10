package builderb0y.bigglobe.hyperspace;

import java.util.TreeSet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import builderb0y.bigglobe.compat.satin.SatinCompat;
import builderb0y.bigglobe.math.BigGlobeMath;

@Environment(EnvType.CLIENT)
public class HyperspaceRendering {

	public static Vec3d cameraPosition = Vec3d.ZERO;
	public static Matrix4f
		modelView         = new Matrix4f(),
		modelViewInverse  = new Matrix4f(),
		projection        = new Matrix4f(),
		projectionInverse = new Matrix4f();

	public static final TreeSet<VisibleWaypointData> visibleWaypoints = (
		#if MC_VERSION >= MC_1_21_2
			new TreeSet<>()
		#else
			SatinCompat.ENABLED ? new TreeSet() : null
		#endif
	);

	static {
		if (visibleWaypoints != null) {
			WorldRenderEvents.START.register((WorldRenderContext context) -> beginFrame(
				context.camera().getPos(),
				#if MC_VERSION >= MC_1_20_5
					context.positionMatrix(),
				#else
					context.matrixStack().peek().getPositionMatrix(),
				#endif
				context.projectionMatrix()
			));
			WorldRenderEvents.END.register((WorldRenderContext context) -> endFrame());
		}
	}

	public static void markWaypointVisible(double x, double y, double z, float age, float health) {
		if (visibleWaypoints != null) {
			visibleWaypoints.add(new VisibleWaypointData(x, y, z, age, health));
			if (visibleWaypoints.size() > 16) {
				visibleWaypoints.pollLast();
			}
		}
	}

	public static void beginFrame(Vec3d cameraPos, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
		cameraPosition = cameraPos;
		modelView.set(modelViewMatrix).invert(modelViewInverse);
		projection.set(projectionMatrix).invert(projectionInverse);
	}

	public static void endFrame() {
		visibleWaypoints.clear();
	}

	@Environment(EnvType.CLIENT)
	public static record VisibleWaypointData(double x, double y, double z, float age, float health) implements Comparable<VisibleWaypointData> {

		public double squareDistanceToCamera() {
			return BigGlobeMath.squareD(
				this.x - cameraPosition.x,
				this.y - cameraPosition.y + 1.0D,
				this.z - cameraPosition.z
			);
		}

		@Override
		public int compareTo(@NotNull VisibleWaypointData that) {
			return Double.compare(this.squareDistanceToCamera(), that.squareDistanceToCamera());
		}
	}

	public static float time(float tickDelta) {
		return (
			(
				(
					(float)(
						BigGlobeMath.modulus_BP(
							MinecraftClient.getInstance().world.getTime(),
							24000L
						)
					)
				)
				+ tickDelta
			)
			/ 20.0F
		);
	}
}