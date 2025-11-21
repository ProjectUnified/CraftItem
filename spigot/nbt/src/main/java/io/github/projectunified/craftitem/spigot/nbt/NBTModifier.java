package io.github.projectunified.craftitem.spigot.nbt;

import io.github.projectunified.craftitem.nbt.NBTMapNormalizer;
import io.github.projectunified.craftitem.nbt.SNBTConverter;
import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.Bukkit;

import java.util.function.UnaryOperator;

/**
 * Spigot modifier for applying NBT data to items.
 *
 * <p>Normalizes and converts map-based NBT data to SNBT format, then applies it to items.
 * Supports both legacy NBT format and newer data component format (1.20.5+).
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * Map<String, Object> nbtData = Map.of(
 *     "display", Map.of(
 *         "Name", "Enchanted Sword"
 *     ),
 *     "Enchantments", List.of(
 *         Map.of("id", "minecraft:sharpness", "lvl", 5)
 *     )
 * );
 * ItemModifier modifier = new NBTModifier(nbtData, false);
 * modifier.modify(spigotItem, s -> s);
 * }</pre>
 */
public class NBTModifier implements SpigotItemModifier {
    private final Object value;
    private final boolean useDataComponent;

    /**
     * Creates a new NBTModifier with the specified NBT data.
     *
     * @param value            the NBT data (typically a Map)
     * @param useDataComponent whether to use data component format (1.20.5+) or legacy NBT
     */
    public NBTModifier(Object value, boolean useDataComponent) {
        this.value = value;
        this.useDataComponent = useDataComponent;
    }

    /**
     * Applies the NBT data to the SpigotItem.
     * The translator is used to resolve variables in the NBT values.
     *
     * @param item       the SpigotItem to modify
     * @param translator the string translator for variable substitution
     */
    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        Object normalized = NBTMapNormalizer.normalize(value, translator);
        String nbtString = SNBTConverter.convert(normalized, useDataComponent);
        if (useDataComponent) {
            String materialName = item.getItemStack().getType().getKey().toString();
            applyNBT(item, materialName + nbtString, true);
        } else {
            applyNBT(item, nbtString, false);
        }
    }

    /**
     * Applies the SNBT string to the item using Bukkit's API.
     * Silently ignores errors if NBT application fails.
     *
     * @param item             the SpigotItem to modify
     * @param nbtString        the SNBT string to apply
     * @param useDataComponent whether to use data component format
     */
    @SuppressWarnings("deprecation")
    private void applyNBT(SpigotItem item, String nbtString, boolean useDataComponent) {
        try {
            if (useDataComponent) {
                item.setItemStack(Bukkit.getItemFactory().createItemStack(nbtString));
            } else {
                item.setItemStack(Bukkit.getUnsafe().modifyItemStack(item.getItemStack(), nbtString));
            }
        } catch (Throwable ignored) {
        }
    }
}
