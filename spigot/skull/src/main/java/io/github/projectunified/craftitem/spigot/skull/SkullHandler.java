package io.github.projectunified.craftitem.spigot.skull;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URL;
import java.util.UUID;

/**
 * Handler interface for setting and getting skull textures.
 * Different implementations are used based on the Bukkit version.
 */
interface SkullHandler {
    /**
     * Set skull by player name
     */
    @SuppressWarnings("deprecation")
    default void setSkullByName(SkullMeta meta, String name) {
        setSkullByPlayer(meta, org.bukkit.Bukkit.getOfflinePlayer(name));
    }

    /**
     * Set skull by player UUID
     */
    default void setSkullByUUID(SkullMeta meta, UUID uuid) {
        setSkullByPlayer(meta, org.bukkit.Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Set skull by OfflinePlayer
     */
    void setSkullByPlayer(SkullMeta meta, OfflinePlayer player);

    /**
     * Set skull by texture URL
     */
    void setSkullByURL(SkullMeta meta, URL url);

    /**
     * Set skull by texture URL (string)
     */
    default void setSkullByURL(SkullMeta meta, String url) {
        try {
            setSkullByURL(meta, new URL(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set skull by Base64 encoded texture data
     */
    void setSkullByBase64(SkullMeta meta, String base64);

    /**
     * Get skull value (texture data) from SkullMeta
     */
    String getSkullValue(SkullMeta meta);
}
