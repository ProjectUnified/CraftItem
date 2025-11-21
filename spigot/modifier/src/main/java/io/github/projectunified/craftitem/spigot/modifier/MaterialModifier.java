package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

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

    public MaterialModifier(Material material) {
        this.material = translator -> new MaterialData(material, null);
    }

    public MaterialModifier(Material material, short data) {
        this.material = translator -> new MaterialData(material, data);
    }

    public MaterialModifier(String material) {
        this.material = translator -> getMaterialData(translator.apply(material));
    }

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

    private static final class MaterialData {
        private final Material material;
        private final Short data;

        private MaterialData(Material material, Short data) {
            this.material = material;
            this.data = data;
        }
    }
}
