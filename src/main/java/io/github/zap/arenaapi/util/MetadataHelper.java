package io.github.zap.arenaapi.util;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Utility class which provides several static methods for accessing metadata from Bukkit {@link Metadatable} objects.
 */
public final class MetadataHelper {
    /**
     * Obtains the {@link MetadataValue} object associated with the given plugin and name, on the given
     * {@link Metadatable} instance.
     * @param target The Metadatable object to search for metadata on
     * @param plugin The plugin to which the metadata must belong
     * @param metadataName The name of the metadata
     * @return An {@link Optional} which may contain a MetadataValue. If no metadata could be found under the specified
     * name, that also belongs to the given plugin, the Optional will be empty
     */
    public static @NotNull Optional<MetadataValue> getMetadataValue(@NotNull Metadatable target, @Nullable Plugin plugin,
                                                                       @NotNull String metadataName) {
        for(MetadataValue value : target.getMetadata(metadataName)) {
            if(value.getOwningPlugin() == plugin) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    /**
     * Obtains the metadata instance (i.e. the object MetadataValue returns when its value() method is called) after
     * obtaining a MetadataValue instance as per {@link MetadataHelper#getMetadataValue(Metadatable, Plugin, String)}.
     * Assuming the MetadataValue is present, an unchecked cast will be performed on its underlying object. If the
     * MetadataValue object is <i>not</i> present, a {@link NoSuchElementException} will be thrown. This is necessary
     * to distinguish between null values for present metadata and metadata that is not present.
     * @param target The Metadatable object to search for metadata on
     * @param plugin The plugin to which the metadata must belong
     * @param metadataName The name of the metadata
     * @param <T> The instance type, to which the underlying value will be cast
     * @return The MetadataValue's value() object, after casting to T, which may be null if a null value was stored
     * @throws NoSuchElementException if no MetadataValue exists; see
     * {@link MetadataHelper#getMetadataValue(Metadatable, Plugin, String)}
     */
    public static <T> T getMetadataInstance(@NotNull Metadatable target, @Nullable Plugin plugin,
                                            @NotNull String metadataName) {
        Optional<MetadataValue> valueOptional = getMetadataValue(target, plugin, metadataName);

        if(valueOptional.isPresent()) {
            MetadataValue value = valueOptional.get();
            //noinspection unchecked
            return (T) value.value();
        }

        throw new NoSuchElementException("no metadata found named '" + metadataName + "' under plugin '" +
                (plugin == null ? "null" : plugin.getName()) + "' for Metadatable '" + target + "'");
    }
}
