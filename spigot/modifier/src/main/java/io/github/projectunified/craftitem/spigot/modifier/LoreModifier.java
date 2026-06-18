package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spigot modifier that sets item lore (description lines).
 *
 * <p>Each line in the lore list is translated before being applied,
 * allowing for dynamic lore generation with variable substitution.
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * ItemModifier modifier = new LoreModifier(List.of(
 *     "Made by ${creator}",
 *     "Durability: ${durability}",
 *     "Level: ${level}"
 * ));
 * modifier.modify(item, s -> s
 *     .replace("${creator}", "PlayerName")
 *     .replace("${durability}", "1000")
 *     .replace("${level}", "10")
 * );
 * }</pre>
 */
public class LoreModifier implements SpigotItemModifier {
    private final List<String> lore;
    private UnaryOperator<String> transformer;

    /**
     * Creates a new LoreModifier with the specified lore lines.
     * Lines can contain variables for translation.
     *
     * @param lore the list of lore lines
     */
    public LoreModifier(List<String> lore) {
        this.lore = lore;
    }

    /**
     * Applies the translated lore to the item.
     *
     * @param item       the SpigotItem to modify
     * @param translator the string translator for variable substitution
     */
    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        Stream<String> loreStream = this.lore.stream().map(translator);
        if (transformer != null) {
            loreStream = loreStream.map(transformer);
        }
        List<String> lore = loreStream.collect(Collectors.toList());
        item.editMeta(itemMeta -> itemMeta.setLore(lore));
    }

    /**
     * Set the function to transform each line of the lore
     *
     * @param transformer the transformer
     */
    public void setTransformer(UnaryOperator<String> transformer) {
        this.transformer = transformer;
    }
}
