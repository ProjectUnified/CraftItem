package io.github.projectunified.craftitem.modifier;

import io.github.projectunified.craftitem.core.Item;
import io.github.projectunified.craftitem.core.ItemModifier;

import java.util.function.UnaryOperator;

public class NameModifier implements ItemModifier {
    private final String name;

    public NameModifier(String name) {
        this.name = name;
    }

    @Override
    public void modify(Item item, UnaryOperator<String> translator) {
        String name = translator.apply(this.name);
        item.setName(name);
    }
}
