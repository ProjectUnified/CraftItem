package io.github.projectunified.craftitem.spigot.core;

import io.github.projectunified.craftitem.core.Item;
import io.github.projectunified.craftitem.core.ItemModifier;

import java.util.function.UnaryOperator;

/**
 * Interface for Spigot-specific item modifications.
 *
 * <p>Extends ItemModifier to work specifically with SpigotItem instances.
 * Provides type-safe modification methods while maintaining compatibility with the generic ItemModifier interface.
 *
 * <p><strong>Example Implementation:</strong>
 * <pre>{@code
 * public class CustomSpigotModifier implements SpigotItemModifier {
 *     @Override
 *     public void modify(SpigotItem item, UnaryOperator<String> translator) {
 *         item.editMeta(meta -> {
 *             meta.setCustomModelData(42);
 *         });
 *     }
 * }
 * }</pre>
 */
public interface SpigotItemModifier extends ItemModifier {
    /**
     * Applies Spigot-specific modifications to a SpigotItem.
     *
     * @param item       the SpigotItem to modify
     * @param translator the string translator for variable substitution
     */
    void modify(SpigotItem item, UnaryOperator<String> translator);

    @Override
    default void modify(Item item, UnaryOperator<String> translator) {
        if (item instanceof SpigotItem) {
            SpigotItem spigotItem = (SpigotItem) item;
            this.modify(spigotItem, translator);
        }
    }
}
