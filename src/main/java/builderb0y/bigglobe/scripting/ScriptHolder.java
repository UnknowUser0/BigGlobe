package builderb0y.bigglobe.scripting;

import builderb0y.autocodec.annotations.MultiLine;
import builderb0y.autocodec.annotations.UseName;
import builderb0y.scripting.environments.BuiltinScriptEnvironment;
import builderb0y.scripting.parsing.GenericScriptTemplate.GenericScriptTemplateUsage;
import builderb0y.scripting.parsing.Script;
import builderb0y.scripting.parsing.ScriptUsage;

public class ScriptHolder<S extends Script> implements Script {

	public final @UseName("script") ScriptUsage<GenericScriptTemplateUsage> usage;
	public final transient S script;
	public transient long nextWarning = Long.MIN_VALUE;

	public ScriptHolder(ScriptUsage<GenericScriptTemplateUsage> usage, S script) {
		this.usage = usage;
		this.script = script;
	}

	public void onError(Throwable throwable) {
		long time = System.currentTimeMillis();
		if (time >= this.nextWarning) {
			this.nextWarning = time + 5000L;
			BuiltinScriptEnvironment.PRINTER.println("Caught exception from " + this.getClass().getName() + ": " + throwable + "; Check your logs for more info.");
			ScriptLogger.LOGGER.error("Script source was:\n" + ScriptLogger.addLineNumbers(this.getSource()));
			ScriptLogger.LOGGER.error("Exception was: ", throwable);
		}
	}

	@Override
	public @MultiLine String getSource() {
		return this.script.getSource();
	}
}