package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.inventory.ItemFlag;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ItemFlagModifier implements SpigotItemModifier {
    private final List<String> flags;

    public ItemFlagModifier(List<String> flags) {
        this.flags = flags;
    }

    public ItemFlagModifier(Collection<ItemFlag> flags) {
        this.flags = flags.stream().map(Enum::name).collect(Collectors.toList());
    }

    private Set<ItemFlag> getParsed(UnaryOperator<String> translator) {
        Set<ItemFlag> flags = new HashSet<>();
        for (String string : this.flags) {
            string = translator.apply(string);
            if (string.equalsIgnoreCase("all")) {
                Collections.addAll(flags, ItemFlag.values());
                continue;
            }
            try {
                flags.add(ItemFlag.valueOf(string.toUpperCase().replace(" ", "_")));
            } catch (IllegalArgumentException e) {
                // IGNORED
            }
        }
        return flags;
    }

    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        Set<ItemFlag> parsed = getParsed(translator);
        if (parsed.isEmpty()) return;
        item.editMeta(itemMeta -> {
            for (ItemFlag flag : parsed) {
                itemMeta.addItemFlags(flag);
            }
        });
    }
}
