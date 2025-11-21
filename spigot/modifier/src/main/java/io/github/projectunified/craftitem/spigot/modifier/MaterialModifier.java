package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Spigot modifier for changing item material type.
 *
 * <p>Supports multiple input formats:
 * <ul>
 *   <li>Direct Material enum</li>
 *   <li>Material name (String) with optional data value: "DIAMOND_SWORD:0"</li>
 *   <li>Material ID number</li>
 *   <li>List of materials (tries each until valid)</li>
 * </ul>
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * // Direct material
 * ItemModifier mod1 = new MaterialModifier(Material.DIAMOND_SWORD);
 *
 * // String with data
 * ItemModifier mod2 = new MaterialModifier("DIAMOND_SWORD:0");
 *
 * // Fallback list
 * ItemModifier mod3 = new MaterialModifier(List.of("DIAMOND_SWORD", "IRON_SWORD"));
 * }</pre>
 */
public class MaterialModifier implements SpigotItemModifier {
    private static final Map<Integer, Material> ID_MATERIAL_MAP = new HashMap<>();

    static {
        for (Material material : Material.values()) {
            try {
                ID_MATERIAL_MAP.put(material.getId(), material);
            } catch (Exception ignored) {
                // IGNORED
            }
        }
    }

    private final Function<UnaryOperator<String>, MaterialData> material;

    /**
     * Creates a MaterialModifier with the specified Material enum.
     *
     * @param material the Material to apply
     */
    public MaterialModifier(Material material) {
        this.material = translator -> new MaterialData(material, null);
    }

    /**
     * Creates a MaterialModifier with the specified Material and data value.
     *
     * @param material the Material to apply
     * @param data     the data value (durability)
     */
    public MaterialModifier(Material material, short data) {
        this.material = translator -> new MaterialData(material, data);
    }

    /**
     * Creates a MaterialModifier with a material name that may contain variables.
     * Format: "MATERIAL_NAME" or "MATERIAL_NAME:data_value"
     *
     * @param material the material name (can be translated)
     */
    public MaterialModifier(String material) {
        this.material = translator -> getMaterialData(translator.apply(material));
    }

    /**
     * Creates a MaterialModifier that tries materials in order until one is valid.
     * Useful as a fallback mechanism.
     *
     * @param materials list of material names to try in order
     */
    public MaterialModifier(List<String> materials) {
        this.material = translator -> {
            for (String material : materials) {
                MaterialData materialData = getMaterialData(translator.apply(material));
                if (materialData != null) {
                    return materialData;
                }
            }
            return null;
        };
    }

    /**
     * Attempts to match a material string to a Material enum.
     * Tries direct name matching and ID lookup.
     *
     * @param materialString the material name or ID
     * @return the Material, or null if not found
     */
    private static Material getMaterial(String materialString) {
        materialString = materialString.replace(" ", "_");
        Material material;
        try {
            material = Material.matchMaterial(materialString);
        } catch (Exception ignored) {
            material = null;
        }

        if (material == null) {
            try {
                material = ID_MATERIAL_MAP.get(Integer.parseInt(materialString));
            } catch (NumberFormatException ignored) {
                // IGNORED
            }
        }

        return material;
    }

    /**
     * Parses a material string into MaterialData.
     * Format: "MATERIAL_NAME" or "MATERIAL_NAME:data_value"
     *
     * @param materialString the material string to parse
     * @return the MaterialData, or null if invalid
     */
    private MaterialData getMaterialData(String materialString) {
        String[] split = materialString.split(":", 2);
        Material material = getMaterial(split[0].trim());
        if (material == null) return null;
        Short data = null;
        if (split.length > 1) {
            try {
                data = Short.parseShort(split[1].trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return new MaterialData(material, data);
    }

    /**
     * Applies the material change to the SpigotItem.
     *
     * @param item       the SpigotItem to modify
     * @param translator the string translator for variable substitution
     */
    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        MaterialData materialData = this.material.apply(translator);
        if (materialData == null) return;
        item.edit(itemStack -> {
            itemStack.setType(materialData.material);
            if (materialData.data != null) {
                itemStack.setDurability(materialData.data);
            }
        });
    }

    /**
     * Internal data class for storing material and optional data value.
     */
    private static final class MaterialData {
        private final Material material;
        private final Short data;

        private MaterialData(Material material, Short data) {
            this.material = material;
            this.data = data;
        }
    }
}
