package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class LoreModifier implements SpigotItemModifier {
    private final List<String> lore;

    public LoreModifier(List<String> lore) {
        this.lore = lore;
    }

    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        List<String> lore = this.lore.stream().map(translator).collect(Collectors.toList());
        item.editMeta(itemMeta -> itemMeta.setLore(lore));
    }
}
