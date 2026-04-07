package io.github.projectunified.craftitem.spigot.nbt;

import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

class PaperNBTApplier {
    static final boolean SUPPORTED;

    static {
        boolean supported;
        try {
            Class.forName("io.papermc.paper.datacomponent.DataComponentType");
            Class.forName("io.papermc.paper.datacomponent.DataComponentType$Valued");
            Class.forName("io.papermc.paper.datacomponent.DataComponentType$NonValued");

            Class<?> dataComponentHolderClass = Class.forName("io.papermc.paper.datacomponent.DataComponentHolder");
            supported = dataComponentHolderClass.isAssignableFrom(ItemStack.class);
        } catch (ClassNotFoundException e) {
            supported = false;
        }
        SUPPORTED = supported;
    }

    @SuppressWarnings({"UnstableApiUsage", "unchecked"})
    static void mergeComponent(ItemStack currentItem, ItemStack referenceItem) {
        Material material = currentItem.getType();
        ItemStack defaultItem = new ItemStack(material);

        for (DataComponentType type : referenceItem.getDataTypes()) {
            if (type instanceof DataComponentType.Valued) {
                DataComponentType.Valued<Object> valued = (DataComponentType.Valued<Object>) type;
                Object parsedValue = referenceItem.getData(valued);
                Object defaultValue = defaultItem.getData(valued);
                if (!Objects.equals(parsedValue, defaultValue)) {
                    if (parsedValue == null) {
                        currentItem.unsetData(valued);
                    } else {
                        currentItem.setData(valued, parsedValue);
                    }
                }
            } else if (type instanceof DataComponentType.NonValued) {
                DataComponentType.NonValued nonValued = (DataComponentType.NonValued) type;
                boolean hasTypeInParsed = referenceItem.hasData(nonValued);
                boolean hasTypeInDefault = defaultItem.hasData(nonValued);
                if (hasTypeInParsed == hasTypeInDefault) {
                    continue;
                }
                if (hasTypeInParsed) {
                    currentItem.setData(nonValued);
                } else {
                    currentItem.unsetData(nonValued);
                }
            }
        }
    }
}
