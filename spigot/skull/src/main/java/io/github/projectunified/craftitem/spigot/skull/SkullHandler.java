package io.github.projectunified.craftitem.spigot.skull;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URL;
import java.util.UUID;

/**
 * Handler interface for setting and getting skull textures.
 * Different implementations are used based on the Bukkit version:
 * - NewSkullHandler for 1.18+ with PlayerProfile API
 * - OldSkullHandler for older versions with GameProfile/reflection
 *
 * <p>Provides methods to set skull textures via various sources:
 * player names, UUIDs, texture URLs, Mojang hashes, or Base64 data.
 */
interface SkullHandler {
    /**
     * Sets skull texture using a player name.
     * Uses Bukkit's OfflinePlayer lookup mechanism.
     *
     * @param meta the SkullMeta to modify
     * @param name the player name
     */
    @SuppressWarnings("deprecation")
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
     * Retrieves the skull texture value (URL or Base64 data) from SkullMeta.
     *
     * @param meta the SkullMeta to query
     * @return the skull texture value as a string, or empty if not found
     */
    String getSkullValue(SkullMeta meta);
}
