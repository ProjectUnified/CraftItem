package io.github.projectunified.craftitem.core;

import java.util.function.UnaryOperator;

public interface ItemModifier {
    void modify(Item item, UnaryOperator<String> translator);
}
