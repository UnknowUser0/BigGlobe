package builderb0y.bigglobe.features;

import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import builderb0y.autocodec.annotations.DefaultBoolean;
import builderb0y.bigglobe.codecs.BigGlobeAutoCodec;
import builderb0y.bigglobe.features.ScriptedFeature.FeatureScript;
import builderb0y.bigglobe.scripting.ScriptLogger;
import builderb0y.scripting.parsing.ScriptParsingException;

public class UseScriptTemplateFeature extends Feature<UseScriptTemplateFeature.Config> {

	public UseScriptTemplateFeature(Codec<Config> configCodec) {
		super(configCodec);
	}

	public UseScriptTemplateFeature() {
		this(BigGlobeAutoCodec.AUTO_CODEC.createDFUCodec(Config.class));
	}

	@Override
	public boolean generate(FeatureContext<Config> context) {
		Config config = context.getConfig();
		FeatureScript.Holder script;
		try {
			script = config.getCompiledScript();
		}
		catch (IllegalStateException exception) {
			long time = System.currentTimeMillis();
			if (time >= config.nextWarning) {
				ScriptLogger.LOGGER.error(exception.getMessage());
				config.nextWarning = time + 5000L;
			}
			return false;
		}
		catch (ScriptParsingException exception) {
			long time = System.currentTimeMillis();
			if (time >= config.nextWarning) {
				ScriptLogger.LOGGER.error(
					"Script template failed to compile:"
					+ "\nScript source was:\n" + (
						ScriptLogger.addLineNumbers(
							config.getScriptSource()
						)
					)
					+ "\nInputs were:\n" + (
						config
						.inputs
						.entrySet()
						.stream()
						.map(entry -> '\t' + entry.getKey() + ": " + entry.getValue())
						.collect(Collectors.joining("\n"))
					),
					exception
				);
				config.nextWarning = time + 5000L;
			}
			return false;
		}
		return ScriptedFeature.generate(context, script, config.rotate_randomly);
	}

	public static class Config implements FeatureConfig {

		public final RegistryEntry<ConfiguredFeature<?, ?>> script;
		public final Map<String, String> inputs;
		public final @DefaultBoolean(value = false, alwaysEncode = true) boolean rotate_randomly;
		public transient FeatureScript.Holder compiledScript;
		public transient long nextWarning = Long.MIN_VALUE;
		public transient ScriptParsingException compileError;

		public Config(RegistryEntry<ConfiguredFeature<?, ?>> script, Map<String, String> inputs, boolean rotate_randomly) {
			this.script = script;
			this.inputs = inputs;
			this.rotate_randomly = rotate_randomly;
		}

		public DefineScriptTemplateFeature.Config acquireScript() {
			ConfiguredFeature<?, ?> actualScript = this.script.value();
			if (actualScript.feature() == BigGlobeFeatures.DEFINE_SCRIPT_TEMPLATE) {
				return ((DefineScriptTemplateFeature.Config)(actualScript.config()));
			}
			else {
				throw new IllegalStateException("script should point to feature of type bigglobe:define_script_template, but was " + Registries.FEATURE.getId(actualScript.feature()));
			}
		}

		public String getScriptSource() {
			return this.acquireScript().script();
		}

		public String[] getScriptInputs() {
			return this.acquireScript().inputs();
		}

		public FeatureScript.Holder getCompiledScript() throws ScriptParsingException {
			if (this.compiledScript == null) {
				if (this.compileError != null) {
					throw new ScriptParsingException(this.compileError);
				}
				try {
					this.compiledScript = new ScriptedFeature.FeatureScript.Holder(this.getScriptSource(), this.inputs, this.getScriptInputs());
				}
				catch (ScriptParsingException exception) {
					this.compileError = exception;
					throw exception;
				}
			}
			return this.compiledScript;
		}
	}
}