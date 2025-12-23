package io.github.projectunified.craftitem.spigot.skull.handler;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URL;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Handler interface for setting and getting skull textures.
 * Different implementations are used based on the Bukkit version:
 * - NewSkullHandler for 1.18+ with PlayerProfile API
 * - OldSkullHandler for older versions with GameProfile/reflection
 * - PaperSkullHandler for servers with PaperMC's PlayerProfile API
 *
 * <p>Provides methods to set skull textures via various sources:
 * player names, UUIDs, texture URLs, Mojang hashes, or Base64 data.
 */
public interface SkullHandler {
    /**
     * Pattern for Mojang SHA256 texture hashes
     * https://github.com/CryptoMorin/XSeries/blob/b633d00608435701f1045a566b98a81edd5f923c/src/main/java/com/cryptomorin/xseries/profiles/objects/ProfileInputType.java
     */
    Pattern MOJANG_SHA256_APPROX_PATTERN = Pattern.compile("[0-9a-z]{55,70}");

    /**
     * Pattern for Base64 encoded texture data
     * https://github.com/CryptoMorin/XSeries/blob/b11b176deca55da6d465e67a3d4be548c3ef06c6/src/main/java/com/cryptomorin/xseries/profiles/objects/ProfileInputType.java
     */
    Pattern BASE64_PATTERN = Pattern.compile("[-A-Za-z0-9+/]{100,}={0,3}");

    /**
     * Gets the instance of {@link SkullHandler}.
     *
     * @return the instance
     */
    static SkullHandler getInstance() {
        try {
            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            return new PaperSkullHandler();
        } catch (Exception e1) {
            try {
                Class.forName("org.bukkit.profile.PlayerProfile");
                return new NewSkullHandler();
            } catch (Exception e2) {
                return new OldSkullHandler();
            }
        }
    }

    /**
     * Sets skull texture using a player name.
     * Uses Bukkit's OfflinePlayer lookup mechanism.
     *
     * @param meta the SkullMeta to modify
     * @param name the player name
     */
    default void setSkullByName(SkullMeta meta, String name) {
        setSkullByPlayer(meta, org.bukkit.Bukkit.getOfflinePlayer(name));
    }

    /**
     * Sets skull texture using a player UUID.
     * Uses Bukkit's OfflinePlayer lookup mechanism.
     *
     * @param meta the SkullMeta to modify
     * @param uuid the player UUID
     */
    default void setSkullByUUID(SkullMeta meta, UUID uuid) {
        setSkullByPlayer(meta, org.bukkit.Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Sets skull texture using an OfflinePlayer.
     * This is the most direct method.
     *
     * @param meta   the SkullMeta to modify
     * @param player the OfflinePlayer
     */
    void setSkullByPlayer(SkullMeta meta, OfflinePlayer player);

    /**
     * Sets skull texture using a URL.
     * The URL should point to a texture PNG image.
     *
     * @param meta the SkullMeta to modify
     * @param url  the texture URL
     */
    void setSkullByURL(SkullMeta meta, URL url);

    /**
     * Sets skull texture using a URL string.
     * The URL string is converted to a URL object.
     *
     * @param meta the SkullMeta to modify
     * @param url  the texture URL as a string
     * @throws RuntimeException if the URL string is invalid
     */
    default void setSkullByURL(SkullMeta meta, String url) {
        try {
            setSkullByURL(meta, new URL(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets skull texture using Base64 encoded texture data.
     * The data should be a Base64-encoded JSON string containing texture information.
     *
     * @param meta   the SkullMeta to modify
     * @param base64 the Base64-encoded texture data
     */
    void setSkullByBase64(SkullMeta meta, String base64);

    /**
     * Sets skull texture using a string value that can be in multiple formats.
     * Attempts to parse the value in the following order:
     * <ol>
     *   <li>As a texture URL</li>
     *   <li>As a Mojang SHA256 texture hash</li>
     *   <li>As Base64-encoded texture data</li>
     *   <li>As a player UUID</li>
     *   <li>As a player name (default fallback)</li>
     * </ol>
     *
     * @param meta  the SkullMeta to modify
     * @param value the skull texture value in any of the supported formats
     */
    default void setSkull(SkullMeta meta, String value) {
        // Try URL
        try {
            URL url = new URL(value);
            setSkullByURL(meta, url);
            return;
        } catch (Throwable ignored) {
            // IGNORED
        }

        // Try Mojang SHA256
        if (MOJANG_SHA256_APPROX_PATTERN.matcher(value).matches()) {
            setSkullByURL(meta, "https://textures.minecraft.net/texture/" + value);
            return;
        }

        // Try Base64
        if (BASE64_PATTERN.matcher(value).matches()) {
            setSkullByBase64(meta, value);
            return;
        }

        // Try UUID
        try {
            UUID uuid = UUID.fromString(value);
            setSkullByUUID(meta, uuid);
            return;
        } catch (Throwable ignored) {
            // IGNORED
        }

        // Default to player name
        setSkullByName(meta, value);
    }

    /**
     * Retrieves the skull texture value (URL or Base64 data) from SkullMeta.
     *
     * @param meta the SkullMeta to query
     * @return the skull texture value as a string, or empty if not found
     */
    String getSkullValue(SkullMeta meta);
}
