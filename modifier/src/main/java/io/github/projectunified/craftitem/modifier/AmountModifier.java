package io.github.projectunified.craftitem.modifier;

import io.github.projectunified.craftitem.core.Item;
import io.github.projectunified.craftitem.core.ItemModifier;

import java.util.function.UnaryOperator;

/**
 * Item modifier that sets the item's stack amount.
 *
 * <p>The amount can be provided as an integer, string, or defaults to 1.
 * The string value is translated before parsing to support dynamic amounts.
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * // Direct integer amount
 * ItemModifier modifier1 = new AmountModifier(64);
 *
 * // String amount with variables
 * ItemModifier modifier2 = new AmountModifier("${stack_size}");
 * modifier2.modify(item, s -> s.replace("${stack_size}", "32"));
 *
 * // Default amount of 1
 * ItemModifier modifier3 = new AmountModifier();
 * }</pre>
 */
public class AmountModifier implements ItemModifier {
    private final String amount;

    /**
     * Creates a new AmountModifier with the specified integer amount.
     *
     * @param amount the stack size
     */
    public AmountModifier(int amount) {
        this.amount = Integer.toString(amount);
    }

    /**
     * Creates a new AmountModifier with the specified string amount.
     * The string can contain variables for translation.
     *
     * @param amount the stack size as a string
     */
    public AmountModifier(String amount) {
        this.amount = amount;
    }

    /**
     * Creates a new AmountModifier with a default amount of 1.
     */
    public AmountModifier() {
        this.amount = "1";
    }

    /**
     * Applies the translated amount to the item.
     * Invalid amounts are silently ignored.
     *
     * @param item       the item to modify
     * @param translator the string translator for variable substitution
     */
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
