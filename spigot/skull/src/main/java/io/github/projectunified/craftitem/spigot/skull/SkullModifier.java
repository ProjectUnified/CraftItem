package io.github.projectunified.craftitem.spigot.skull;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import io.github.projectunified.craftitem.spigot.skull.handler.SkullHandler;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.function.UnaryOperator;

/**
 * Skull modifier that handles setting skull textures from various sources:
 * - Player names
 * - Player UUIDs
 * - Texture URLs
 * - Mojang SHA256 hashes
 * - Base64 encoded texture data
 */
public class SkullModifier implements SpigotItemModifier {
    private static final SkullHandler skullHandler = SkullHandler.getInstance();

    private final String skullString;

    /**
     * Creates a skull modifier.
     *
     * @param skullString the skull string
     */
    public SkullModifier(String skullString) {
        this.skullString = skullString;
    }

    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        item.editMeta(SkullMeta.class, skullMeta -> skullHandler.setSkull(skullMeta, translator.apply(skullString)));
    }
}
