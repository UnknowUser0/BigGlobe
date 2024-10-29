package builderb0y.bigglobe.scripting.wrappers.tags;

import java.lang.invoke.MethodHandles;
import java.util.random.RandomGenerator;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

import builderb0y.bigglobe.util.DelayedEntryList;
import builderb0y.scripting.bytecode.ConstantFactory;
import builderb0y.scripting.bytecode.TypeInfo;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class ItemTag extends TagWrapper<Item, Item> {

	public static final TypeInfo TYPE = type(ItemTag.class);
	public static final ConstantFactory CONSTANT_FACTORY = ConstantFactory.autoOfString();

	public ItemTag(DelayedEntryList<Item> list) {
		super(list);
	}

	public static ItemTag of(MethodHandles.Lookup caller, String name, Class<?> type, String id) {
		return of(id);
	}

	public static ItemTag of(String id) {
		if (id == null) return null;
		return new ItemTag(DelayedEntryList.constant(RegistryKeys.ITEM, id));
	}

	@Override
	public Item wrap(RegistryEntry<Item> entry) {
		return entry.value();
	}

	@Override
	@SuppressWarnings("deprecation")
	public RegistryEntry<Item> unwrap(Item item) {
		return item.getRegistryEntry();
	}

	@Override
	public boolean contains(Item item) {
		return super.contains(item);
	}

	@Override
	public Item random(RandomGenerator random) {
		return super.random(random);
	}

	@Override
	public Item random(long seed) {
		return super.random(seed);
	}
}