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

public class PotionEffectModifier implements SpigotItemModifier {
    private final Function<UnaryOperator<String>, Collection<PotionEffect>> potionEffect;

    public PotionEffectModifier(PotionEffect potionEffect) {
        this.potionEffect = translator -> Collections.singletonList(potionEffect);
    }

    public PotionEffectModifier(String potionEffect) {
        this.potionEffect = translator -> pastePotionEffect(translator.apply(potionEffect)).map(Collections::singletonList).orElse(Collections.emptyList());
    }

    public PotionEffectModifier(Collection<PotionEffect> potionEffects) {
        this.potionEffect = translator -> potionEffects;
    }

    public PotionEffectModifier(List<String> potionEffects) {
        this.potionEffect = translator -> potionEffects.stream()
                .map(translator)
                .map(PotionEffectModifier::pastePotionEffect)
                .flatMap(optional -> optional.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());
    }

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
