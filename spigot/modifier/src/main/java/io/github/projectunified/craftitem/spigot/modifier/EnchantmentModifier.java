package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Spigot modifier that applies enchantments to items.
 *
 * <p>Supports enchantment definition as either a list of strings or a map of Enchantment objects.
 * String format: "ENCHANTMENT_NAME level" or "ENCHANTMENT_NAME,level"
 * Handles both regular items and enchanted books.
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * // String list format
 * ItemModifier mod1 = new EnchantmentModifier(List.of(
 *     "Sharpness 5",
 *     "Knockback, 2"
 * ));
 *
 * // Direct Enchantment map
 * Map<Enchantment, Integer> enchants = Map.of(
 *     Enchantment.DAMAGE_ALL, 5,
 *     Enchantment.KNOCKBACK, 2
 * );
 * ItemModifier mod2 = new EnchantmentModifier(enchants);
 * }</pre>
 */
public class EnchantmentModifier implements SpigotItemModifier {
    private static final Map<String, Enchantment> ENCHANTMENT_MAP = new HashMap<>();

    static {
        for (Enchantment enchantment : Enchantment.values()) {
            ENCHANTMENT_MAP.put(normalizeEnchantmentName(enchantment.getName()), enchantment);
        }
    }

    private final Function<UnaryOperator<String>, Map<Enchantment, Integer>> enchantments;

    /**
     * Creates an EnchantmentModifier from a list of enchantment strings
     *
     * @param enchantments the list of enchantment strings
     * @param delimiters   the character to separate the enchantment name and level
     */
    public EnchantmentModifier(List<String> enchantments, char... delimiters) {
        this.enchantments = translator -> getParsed(enchantments, delimiters, translator);
    }

    /**
     * Creates an EnchantmentModifier from a list of enchantment strings.
     * Format: "ENCHANTMENT_NAME level" or "ENCHANTMENT_NAME,level"
     *
     * @param enchantments the list of enchantment strings
     */
    public EnchantmentModifier(List<String> enchantments) {
        this(enchantments, ',', ' ');
    }

    /**
     * Creates an EnchantmentModifier from a map of Enchantment objects.
     *
     * @param enchantments map of enchantments to their levels
     */
    public EnchantmentModifier(Map<Enchantment, Integer> enchantments) {
        this.enchantments = translator -> enchantments;
    }

    /**
     * Normalizes enchantment names for lookup (uppercase, replace spaces with underscores).
     *
     * @param name the enchantment name
     * @return the normalized name
     */
    private static String normalizeEnchantmentName(String name) {
        return name.toUpperCase(Locale.ROOT).replace(" ", "_");
    }

    /**
     * Parses enchantment strings into a map of Enchantment objects.
     *
     * @param enchantments the list of enchantment strings
     * @param delimiters   the character to separate the enchantment name and level
     * @param translator   the string translator for variable substitution
     * @return map of valid enchantments to their levels
     */
    private static Map<Enchantment, Integer> getParsed(List<String> enchantments, char[] delimiters, UnaryOperator<String> translator) {
        Map<Enchantment, Integer> enchantmentMap = new LinkedHashMap<>();
        for (String string : enchantments) {
            String replaced = translator.apply(string);
            String[] split = null;
            for (char delimiter : delimiters) {
                if (replaced.indexOf(delimiter) >= 0) {
                    split = replaced.split(Pattern.quote(String.valueOf(delimiter)), 2);
                    break;
                }
            }
            if (split == null) {
                split = new String[]{replaced};
            }
            Optional<Enchantment> enchantment = Optional.of(split[0].trim()).map(EnchantmentModifier::normalizeEnchantmentName).map(ENCHANTMENT_MAP::get);
            int level = 1;
            if (split.length > 1) {
                String rawLevel = split[1].trim();
                try {
                    level = Integer.parseInt(rawLevel);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            if (enchantment.isPresent()) {
                enchantmentMap.put(enchantment.get(), level);
            }
        }
        return enchantmentMap;
    }

    /**
     * Applies enchantments to the item.
     * For enchanted books (EnchantmentStorageMeta), uses stored enchantments.
     * For other items, applies enchantments directly.
     *
     * @param item       the SpigotItem to modify
     * @param translator the string translator for variable substitution
     */
    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        Map<Enchantment, Integer> map = enchantments.apply(translator);
        item.editMeta(meta -> {
            if (meta instanceof EnchantmentStorageMeta) {
                map.forEach((enchant, level) -> ((EnchantmentStorageMeta) meta).addStoredEnchant(enchant, level, true));
            } else {
                map.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
            }
        });
    }
}
