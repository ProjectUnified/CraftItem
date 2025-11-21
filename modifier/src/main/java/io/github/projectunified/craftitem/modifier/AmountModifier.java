package io.github.projectunified.craftitem.modifier;

import io.github.projectunified.craftitem.core.Item;
import io.github.projectunified.craftitem.core.ItemModifier;

import java.util.function.UnaryOperator;

public class AmountModifier implements ItemModifier {
    private final String amount;

    public AmountModifier(int amount) {
        this.amount = Integer.toString(amount);
    }

    public AmountModifier(String amount) {
        this.amount = amount;
    }

    public AmountModifier() {
        this.amount = "1";
    }

    @Override
    public void modify(Item item, UnaryOperator<String> translator) {
        String amount = translator.apply(this.amount);
        int a;
        try {
            a = Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            return;
        }
        item.setAmount(a);
    }
}
