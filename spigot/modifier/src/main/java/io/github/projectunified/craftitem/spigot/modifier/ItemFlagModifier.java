package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.inventory.ItemFlag;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Spigot modifier that adds item flags to hide or show specific item attributes.
 *
 * <p>Flags control what information is displayed in the item tooltip.
 * Special value "all" applies all available flags.
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * // Hide enchantments and attributes
 * ItemModifier mod1 = new ItemFlagModifier(List.of(
 *     "HIDE_ENCHANTS",
 *     "HIDE_ATTRIBUTES"
 * ));
 *
 * // Hide everything
 * ItemModifier mod2 = new ItemFlagModifier(List.of("all"));
 *
 * // From ItemFlag enum
 * ItemModifier mod3 = new ItemFlagModifier(
 *     List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
 * );
 * }</pre>
 */
public class ItemFlagModifier implements SpigotItemModifier {
    private final List<String> flags;

    /**
     * Creates a new ItemFlagModifier with the specified flag names.
     * Flag names are case-insensitive and spaces are converted to underscores.
     *
     * @param flags the list of item flag names (or "all")
     */
    public ItemFlagModifier(List<String> flags) {
        this.flags = flags;
    }

    /**
     * Creates a new ItemFlagModifier from ItemFlag enum values.
     *
     * @param flags the collection of ItemFlag enums
     */
    public ItemFlagModifier(Collection<ItemFlag> flags) {
        this.flags = flags.stream().map(Enum::name).collect(Collectors.toList());
    }

    /**
     * Parses flag strings into ItemFlag enum values.
     * Handles the special "all" value to apply all available flags.
     *
     * @param translator the string translator for variable substitution
     * @return set of parsed ItemFlags
     */
    private Set<ItemFlag> getParsed(UnaryOperator<String> translator) {
        Set<ItemFlag> flags = new HashSet<>();
        for (String string : this.flags) {
            string = translator.apply(string);
            if (string.equalsIgnoreCase("all")) {
                Collections.addAll(flags, ItemFlag.values());
                continue;
            }
            try {
                flags.add(ItemFlag.valueOf(string.toUpperCase().replace(" ", "_")));
            } catch (IllegalArgumentException e) {
                // IGNORED
            }
        }
        return flags;
    }

    /**
     * Applies the parsed item flags to the item.
     *
     * @param item       the SpigotItem to modify
     * @param translator the string translator for variable substitution
     */
    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        Set<ItemFlag> parsed = getParsed(translator);
        if (parsed.isEmpty()) return;
        item.editMeta(itemMeta -> {
            for (ItemFlag flag : parsed) {
                itemMeta.addItemFlags(flag);
            }
        });
    }
}
