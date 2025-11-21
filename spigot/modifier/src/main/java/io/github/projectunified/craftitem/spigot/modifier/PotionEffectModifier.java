package io.github.projectunified.craftitem.spigot.modifier;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spigot modifier that adds potion effects to potions and compatible items.
 *
 * <p>Supports multiple input formats:
 * <ul>
 *   <li>Direct PotionEffect object</li>
 *   <li>String format: "EFFECT_NAME duration amplifier" or "EFFECT_NAME,duration,amplifier"</li>
 *   <li>Collection of PotionEffect objects</li>
 *   <li>List of effect strings</li>
 * </ul>
 * Duration is specified in seconds and internally converted to ticks (1 second = 20 ticks).
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * // Direct PotionEffect
 * ItemModifier mod1 = new PotionEffectModifier(
 *     new PotionEffect(PotionEffectType.SPEED, 2400, 2)
 * );
 *
 * // String list format (duration in seconds)
 * ItemModifier mod2 = new PotionEffectModifier(List.of(
 *     "Speed 120 2",
 *     "Strength, 60, 1"
 * ));
 *
 * // Direct collection
 * ItemModifier mod3 = new PotionEffectModifier(List.of(
 *     new PotionEffect(PotionEffectType.SPEED, 2400, 2)
 * ));
 * }</pre>
 */
public class PotionEffectModifier implements SpigotItemModifier {
    private final Function<UnaryOperator<String>, Collection<PotionEffect>> potionEffect;

    /**
     * Creates a PotionEffectModifier with a single PotionEffect.
     *
     * @param potionEffect the potion effect to apply
     */
    public PotionEffectModifier(PotionEffect potionEffect) {
        this.potionEffect = translator -> Collections.singletonList(potionEffect);
    }

    /**
     * Creates a PotionEffectModifier with a potion effect string.
     * Format: "EFFECT_NAME duration amplifier" or "EFFECT_NAME,duration,amplifier"
     * Duration is in seconds.
     *
     * @param potionEffect the potion effect string
     */
    public PotionEffectModifier(String potionEffect) {
        this.potionEffect = translator -> pastePotionEffect(translator.apply(potionEffect)).map(Collections::singletonList).orElse(Collections.emptyList());
    }

    /**
     * Creates a PotionEffectModifier with a collection of PotionEffect objects.
     *
     * @param potionEffects the collection of potion effects
     */
    public PotionEffectModifier(Collection<PotionEffect> potionEffects) {
        this.potionEffect = translator -> potionEffects;
    }

    /**
     * Creates a PotionEffectModifier with a list of potion effect strings.
     * Format: "EFFECT_NAME duration amplifier" or "EFFECT_NAME,duration,amplifier"
     * Duration is in seconds.
     *
     * @param potionEffects the list of potion effect strings
     */
    public PotionEffectModifier(List<String> potionEffects) {
        this.potionEffect = translator -> potionEffects.stream()
                .map(translator)
                .map(PotionEffectModifier::pastePotionEffect)
                .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());
    }

    /**
     * Parses a potion effect string into a PotionEffect object.
     * Format: "EFFECT_NAME duration amplifier" or "EFFECT_NAME,duration,amplifier"
     * Duration is in seconds and converted to ticks internally.
     *
     * @param string the potion effect string
     * @return an Optional containing the parsed PotionEffect, or empty if invalid
     */
    private static Optional<PotionEffect> pastePotionEffect(String string) {
        String[] split;
        if (string.indexOf(',') != -1) {
            split = string.split(",", 3);
        } else {
            split = string.split(" ", 3);
        }
        PotionEffectType potionEffectType = PotionEffectType.getByName(split[0].replace(" ", "_").trim());
        if (potionEffectType == null) {
            return Optional.empty();
        }
        int duration = 2400;
        int amplifier = 0;
        if (split.length > 1) {
            try {
                duration = Integer.parseInt(split[1]) * 20;
            } catch (NumberFormatException ignored) {
                // IGNORED
            }
        }
        if (split.length > 2) {
            try {
                amplifier = Integer.parseInt(split[2]);
            } catch (NumberFormatException ignored) {
                // IGNORED
            }
        }
        return Optional.of(new PotionEffect(potionEffectType, duration, amplifier));
    }

    /**
     * Applies the potion effects to the item.
     * Only works on items with PotionMeta (potions, splash potions, lingering potions, etc.).
     *
     * @param item       the SpigotItem to modify
     * @param translator the string translator for variable substitution
     */
    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        Collection<PotionEffect> potionEffects = this.potionEffect.apply(translator);
        item.editMeta(PotionMeta.class, potionMeta -> {
            for (PotionEffect potionEffect : potionEffects) {
                potionMeta.addCustomEffect(potionEffect, true);
            }
        });
    }
}
