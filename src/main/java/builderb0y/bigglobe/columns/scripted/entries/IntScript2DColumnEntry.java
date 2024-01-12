package builderb0y.bigglobe.columns.scripted.entries;

import builderb0y.autocodec.annotations.RecordLike;
import builderb0y.scripting.bytecode.TypeInfo;
import builderb0y.scripting.parsing.GenericScriptTemplate.GenericScriptTemplateUsage;
import builderb0y.scripting.parsing.ScriptUsage;
import builderb0y.scripting.util.TypeInfos;

public class IntScript2DColumnEntry extends Script2DColumnEntry {

	public IntScript2DColumnEntry(ScriptUsage<GenericScriptTemplateUsage> value, Valid valid, boolean cache) {
		super(value, valid, cache);
	}

	@Override
	public AccessSchema getAccessSchema() {
		return new Int2DAccessSchema();
	}

	@RecordLike({})
	public static class Int2DAccessSchema extends Basic2DAccessSchema {

		@Override
		public TypeInfo type() {
			return TypeInfos.INT;
		}
	}
}