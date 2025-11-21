package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;

import java.util.function.UnaryOperator;

public class DurabilityModifier implements SpigotItemModifier {
    private final String durability;

    public DurabilityModifier(short durability) {
        this.durability = Short.toString(durability);
    }

    public DurabilityModifier(String durability) {
        this.durability = durability;
    }

    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        String durability = translator.apply(this.durability);
        short d;
        try {
            d = Short.parseShort(durability);
        } catch (NumberFormatException e) {
            return;
        }
        item.edit(itemStack -> itemStack.setDurability(d));
    }
}
