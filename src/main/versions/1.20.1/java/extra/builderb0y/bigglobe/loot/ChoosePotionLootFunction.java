package builderb0y.bigglobe.loot;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.JsonHelper;

import builderb0y.autocodec.annotations.AddPseudoField;
import builderb0y.autocodec.annotations.DefaultEmpty;
import builderb0y.autocodec.annotations.UseName;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.bigglobe.codecs.BigGlobeAutoCodec;
import builderb0y.bigglobe.randomLists.IRandomList;

@AddPseudoField("conditions")
public class ChoosePotionLootFunction extends ConditionalLootFunction {

	public static final Serializer SERIALIZER = new Serializer();

	public final IRandomList<@UseName("potion") RegistryEntry<Potion>> potions;

	public ChoosePotionLootFunction(LootCondition[] conditions, IRandomList<RegistryEntry<Potion>> potions) {
		super(conditions);
		this.potions = potions;
	}

	public LootCondition @DefaultEmpty [] conditions() {
		return this.conditions;
	}

	@Override
	public ItemStack process(ItemStack stack, LootContext context) {
		PotionUtil.setPotion(stack, this.potions.getRandomElement(context.getRandom().nextLong()).value());
		return stack;
	}

	@Override
	public LootFunctionType getType() {
		return BigGlobeLoot.CHOOSE_POTION_TYPE;
	}

	public static class Serializer extends ConditionalLootFunction.Serializer<ChoosePotionLootFunction> {

		public static final AutoCoder<IRandomList<RegistryEntry<Potion>>> POTION_LIST = (
			BigGlobeAutoCodec.AUTO_CODEC.createCoder(
				new ReifiedType<IRandomList<@UseName("potion") RegistryEntry<Potion>>>() {}
			)
		);

		@Override
		public void toJson(JsonObject json, ChoosePotionLootFunction function, JsonSerializationContext context) {
			super.toJson(json, function, context);
			json.add("potions", BigGlobeAutoCodec.AUTO_CODEC.encode(POTION_LIST, function.potions, JsonOps.INSTANCE));
		}

		@Override
		public ChoosePotionLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
			JsonArray potionsJson = JsonHelper.getArray(json, "potions");
			IRandomList<RegistryEntry<Potion>> potions;
			try {
				potions = BigGlobeAutoCodec.AUTO_CODEC.decode(POTION_LIST, potionsJson, JsonOps.INSTANCE);
			}
			catch (DecodeException exception) {
				throw new JsonSyntaxException(exception);
			}
			return new ChoosePotionLootFunction(conditions, potions);
		}
	}
}