package io.github.projectunified.craftitem.core;

import java.util.function.UnaryOperator;

/**
 * Interface for applying modifications to items.
 *
 * <p>Implementations modify item properties using a string translator for dynamic value substitution.
 * The translator allows for variable interpolation and custom value transformations.
 *
 * <p><strong>Example Implementation:</strong>
 * <pre>{@code
 * public class CustomModifier implements ItemModifier {
 *     @Override
 *     public void modify(Item item, UnaryOperator<String> translator) {
 *         String translatedValue = translator.apply("${player_name}");
 *         item.setName(translatedValue);
 *     }
 * }
 * }</pre>
 */
public interface ItemModifier {
    /**
     * Applies modifications to the given item.
     *
     * @param item       the item to modify
     * @param translator a function to translate string values (e.g., for variable substitution)
     */
    void modify(Item item, UnaryOperator<String> translator);

    /**
     * Applies modifications to the given item.
     *
     * @param item the item to modify
     */
    default void modify(Item item) {
        modify(item, UnaryOperator.identity());
    }
}
