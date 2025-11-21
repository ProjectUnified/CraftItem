package io.github.projectunified.craftitem.spigot.core;

import io.github.projectunified.craftitem.core.Item;
import io.github.projectunified.craftitem.core.ItemModifier;

import java.util.function.UnaryOperator;

public interface SpigotItemModifier extends ItemModifier {
    void modify(SpigotItem item, UnaryOperator<String> translator);

    @Override
    default void modify(Item item, UnaryOperator<String> translator) {
        if (item instanceof SpigotItem) {
            SpigotItem spigotItem = (SpigotItem) item;
            this.modify(spigotItem, translator);
        }
    }
}
