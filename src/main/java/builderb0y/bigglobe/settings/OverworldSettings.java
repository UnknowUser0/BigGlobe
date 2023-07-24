package builderb0y.bigglobe.settings;

import builderb0y.autocodec.annotations.VerifyNullable;
import builderb0y.bigglobe.noise.Grid2D;
import builderb0y.bigglobe.scripting.HeightAdjustmentScript;
import builderb0y.bigglobe.scripting.SurfaceDepthWithSlopeScript;
import builderb0y.bigglobe.settings.BiomeLayout.OverworldBiomeLayout;

public class OverworldSettings {

	public final OverworldHeightSettings height;
	public final OverworldTemperatureSettings temperature;
	public final OverworldFoliageSettings foliage;
	public final OverworldSurfaceSettings surface;
	public final OverworldUndergroundSettings underground;
	public final @VerifyNullable OverworldSkylandSettings skylands;
	public final OverworldMiscellaneousSettings miscellaneous;

	public final BiomeLayout.Holder<OverworldBiomeLayout> biomes;

	public OverworldSettings(
		OverworldHeightSettings height,
		OverworldTemperatureSettings temperature,
		OverworldFoliageSettings foliage,
		OverworldSurfaceSettings surface,
		OverworldUndergroundSettings underground,
		@VerifyNullable OverworldSkylandSettings skylands,
		OverworldMiscellaneousSettings miscellaneous,
		BiomeLayout.Holder<OverworldBiomeLayout> biomes
	) {
		this.height = height;
		this.temperature = temperature;
		this.foliage = foliage;
		this.surface = surface;
		this.underground = underground;
		this.skylands = skylands;
		this.miscellaneous = miscellaneous;
		this.biomes = biomes;
	}

	public boolean hasSkylands() {
		return this.skylands != null;
	}

	public static record OverworldTemperatureSettings(
		Grid2D noise,
		HeightAdjustmentScript.TemperatureHolder height_adjustment
	) {}

	public static record OverworldFoliageSettings(
		Grid2D noise,
		HeightAdjustmentScript.FoliageHolder height_adjustment
	) {}

	public static record OverworldSurfaceSettings(
		SurfaceDepthWithSlopeScript.Holder primary_surface_depth
	) {}

	public static record OverworldMiscellaneousSettings(
		double snow_temperature_multiplier
	) {}
}