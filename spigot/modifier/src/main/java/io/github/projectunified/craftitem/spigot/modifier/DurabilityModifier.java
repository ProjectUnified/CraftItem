package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;

import java.util.function.UnaryOperator;

/**
 * Spigot modifier that sets item durability (damage value).
 *
 * <p>Durability can be provided as a short value or a string.
 * The string value is translated before parsing to support dynamic durability.
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * // Direct durability value
 * ItemModifier mod1 = new DurabilityModifier((short) 50);
 *
 * // String with variable
 * ItemModifier mod2 = new DurabilityModifier("${damage}");
 * mod2.modify(item, s -> s.replace("${damage}", "100"));
 * }</pre>
 */
public class DurabilityModifier implements SpigotItemModifier {
    private final String durability;

    /**
     * Creates a new DurabilityModifier with the specified durability value.
     *
     * @param durability the durability value (0 = full durability)
     */
    public DurabilityModifier(short durability) {
        this.durability = Short.toString(durability);
    }

    /**
     * Creates a new DurabilityModifier with the specified durability as a string.
     * The string can contain variables for translation.
     *
     * @param durability the durability value as a string
     */
    public DurabilityModifier(String durability) {
        this.durability = durability;
    }

    /**
     * Applies the translated durability to the item.
     * Invalid durability values are silently ignored.
     *
     * @param item       the SpigotItem to modify
     * @param translator the string translator for variable substitution
     */
    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        String durability = translator.apply(this.durability);
        short d;
        try {
            d = Short.parseShort(durability);
        } catch (NumberFormatException e) {
            return;
        }
        item.edit(itemStack -> itemStack.setDurability(d));
    }
}
