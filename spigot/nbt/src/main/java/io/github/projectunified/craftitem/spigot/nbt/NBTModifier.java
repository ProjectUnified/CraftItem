package io.github.projectunified.craftitem.spigot.nbt;

import io.github.projectunified.craftitem.nbt.NBTMapNormalizer;
import io.github.projectunified.craftitem.nbt.SNBTConverter;
import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.Bukkit;

import java.util.function.UnaryOperator;

public class NBTModifier implements SpigotItemModifier {
    private final Object value;
    private final boolean useDataComponent;

    public NBTModifier(Object value, boolean useDataComponent) {
        this.value = value;
        this.useDataComponent = useDataComponent;
    }

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
