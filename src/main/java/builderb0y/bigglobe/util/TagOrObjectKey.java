package builderb0y.bigglobe.util;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

import builderb0y.bigglobe.versions.RegistryVersions;
import builderb0y.bigglobe.versions.TagKeyVersions;

public class TagOrObjectKey<T> {

	public final @Nullable TagKey<T> tagKey;
	public final @Nullable RegistryKey<T> objectKey;
	public @Nullable TagOrObject<T> resolution;

	public TagOrObjectKey(@Nullable TagKey<T> tagKey) {
		this.tagKey = tagKey;
		this.objectKey = null;
	}

	public TagOrObjectKey(@Nullable RegistryKey<T> objectKey) {
		this.tagKey = null;
		this.objectKey = objectKey;
	}

	public <X extends Throwable> @Nullable TagOrObject<T> resolve(DynamicRegistryManager registryManager, MissingResolver<T, X> missingResolver) throws X {
		if (this.resolution == null) {
			if (this.tagKey != null) {
				RegistryKey<Registry<T>> registryKey = TagKeyVersions.registry(this.tagKey);
				RegistryEntryList<T> list = RegistryVersions.getTagNullable(registryManager, this.tagKey);
				this.resolution = list != null ? new TagOrObject<>(list) : missingResolver.resolveMissing(() -> "No such tag " + this.tagKey.id() + " in registry " + registryKey.getValue());
			}
			else {
				RegistryKey<Registry<T>> registryKey = RegistryKey.ofRegistry(this.objectKey.getRegistry());
				RegistryEntry<T> object = RegistryVersions.getEntry(registryManager,  this.objectKey);
				this.resolution = object != null ? new TagOrObject<>(object) : missingResolver.resolveMissing(() -> "No such object " + this.objectKey.getValue() + " in registry " + registryKey.getValue());
			}
		}
		return this.resolution;
	}

	@Override
	public String toString() {
		return this.tagKey != null ? '#' + this.tagKey.id().toString() : this.objectKey.getValue().toString();
	}

	@FunctionalInterface
	public static interface MissingResolver<T, X extends Throwable> {

		public abstract @Nullable TagOrObject<T> resolveMissing(Supplier<String> message) throws X;
	}
}