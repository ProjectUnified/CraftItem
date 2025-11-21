package io.github.projectunified.craftitem.spigot.skull;

import io.github.projectunified.craftitem.spigot.core.SpigotItem;
import io.github.projectunified.craftitem.spigot.core.SpigotItemModifier;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Skull modifier that handles setting skull textures from various sources:
 * - Player names
 * - Player UUIDs
 * - Texture URLs
 * - Mojang SHA256 hashes
 * - Base64 encoded texture data
 */
public class SkullModifier implements SpigotItemModifier {
    /**
     * Pattern for Mojang SHA256 texture hashes
     * https://github.com/CryptoMorin/XSeries/blob/b633d00608435701f1045a566b98a81edd5f923c/src/main/java/com/cryptomorin/xseries/profiles/objects/ProfileInputType.java
     */
    private static final Pattern MOJANG_SHA256_APPROX_PATTERN = Pattern.compile("[0-9a-z]{55,70}");

    /**
     * Pattern for Base64 encoded texture data
     * https://github.com/CryptoMorin/XSeries/blob/b11b176deca55da6d465e67a3d4be548c3ef06c6/src/main/java/com/cryptomorin/xseries/profiles/objects/ProfileInputType.java
     */
    private static final Pattern BASE64_PATTERN = Pattern.compile("[-A-Za-z0-9+/]{100,}={0,3}");

    private static final SkullHandler skullHandler = getSkullHandler();

    private final String skullString;

    /**
     * Creates a skull modifier.
     *
     * @param skullString the skull string
     */
    public SkullModifier(String skullString) {
        this.skullString = skullString;
    }

    private static SkullHandler getSkullHandler() {
        try {
            Class.forName("org.bukkit.profile.PlayerProfile");
            return new NewSkullHandler();
        } catch (ClassNotFoundException e) {
            return new OldSkullHandler();
        }
    }

    private static void setSkull(SkullMeta meta, String skull) {
        // Try URL
        Optional<URL> url = getURL(skull);
        if (url.isPresent()) {
            skullHandler.setSkullByURL(meta, url.get());
            return;
        }

        // Try Mojang SHA256
        if (MOJANG_SHA256_APPROX_PATTERN.matcher(skull).matches()) {
            skullHandler.setSkullByURL(meta, "https://textures.minecraft.net/texture/" + skull);
            return;
        }

        // Try Base64
        if (BASE64_PATTERN.matcher(skull).matches()) {
            skullHandler.setSkullByBase64(meta, skull);
            return;
        }

        // Try UUID
        Optional<UUID> uuid = getUUID(skull);
        if (uuid.isPresent()) {
            skullHandler.setSkullByUUID(meta, uuid.get());
            return;
        }

        // Default to player name
        skullHandler.setSkullByName(meta, skull);
    }

    private static Optional<URL> getURL(String str) {
        try {
            return Optional.of(new URL(str));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<UUID> getUUID(String str) {
        try {
            return Optional.of(UUID.fromString(str));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public void modify(SpigotItem item, UnaryOperator<String> translator) {
        item.editMeta(SkullMeta.class, skullMeta -> setSkull(skullMeta, translator.apply(skullString)));
    }
}
