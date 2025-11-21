package io.github.projectunified.craftitem.modifier;

import io.github.projectunified.craftitem.core.Item;
import io.github.projectunified.craftitem.core.ItemModifier;

import java.util.function.UnaryOperator;

/**
 * Item modifier that sets the item's display name.
 *
 * <p>The provided name is passed through the translator before being applied to the item,
 * allowing for variable substitution and dynamic name generation.
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * ItemModifier modifier = new NameModifier("Sword of ${owner}");
 * modifier.modify(item, s -> s.replace("${owner}", "Player123"));
 * // Item name becomes: "Sword of Player123"
 * }</pre>
 */
public class NameModifier implements ItemModifier {
    private final String name;

    /**
     * Creates a new NameModifier with the specified name.
     *
     * @param name the display name (can contain variables for translation)
     */
    public NameModifier(String name) {
        this.name = name;
    }

    /**
     * Applies the translated name to the item.
     *
     * @param item       the item to modify
     * @param translator the string translator for variable substitution
     */
    @Override
    public void modify(Item item, UnaryOperator<String> translator) {
        String name = translator.apply(this.name);
        item.setName(name);
    }
}
