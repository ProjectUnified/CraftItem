package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class EnchantmentModifier implements SpigotItemModifier {
    private static final Map<String, Enchantment> ENCHANTMENT_MAP = new HashMap<>();

    static {
        for (Enchantment enchantment : Enchantment.values()) {
            ENCHANTMENT_MAP.put(normalizeEnchantmentName(enchantment.getName()), enchantment);
        }
    }

    private final Function<UnaryOperator<String>, Map<Enchantment, Integer>> enchantments;

    public EnchantmentModifier(List<String> enchantments) {
        this.enchantments = translator -> getParsed(enchantments, translator);
    }

    public EnchantmentModifier(Map<Enchantment, Integer> enchantments) {
        this.enchantments = translator -> enchantments;
    }

    private static String normalizeEnchantmentName(String name) {
        return name.toUpperCase(Locale.ROOT).replace(" ", "_");
    }

    private static Map<Enchantment, Integer> getParsed(List<String> enchantments, UnaryOperator<String> translator) {
        Map<Enchantment, Integer> enchantmentMap = new LinkedHashMap<>();
        for (String string : enchantments) {
            String replaced = translator.apply(string);
            String[] split;
            if (replaced.indexOf(',') != -1) {
                split = replaced.split(",", 2);
            } else {
                split = replaced.split(" ", 2);
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
