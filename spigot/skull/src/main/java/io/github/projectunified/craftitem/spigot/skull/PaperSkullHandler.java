package io.github.projectunified.craftitem.spigot.skull;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skull handler for Paper servers using the Paper-specific PlayerProfile API.
 *
 * <p>Uses Paper's faster PlayerProfile implementation compared to Bukkit's standard API.
 * Caches PlayerProfile instances to avoid recreation for repeated textures.
 *
 * <p><strong>Implementation Details:</strong>
 * <ul>
 *   <li>Uses Bukkit.createProfile() to create profiles (Paper-optimized)</li>
 *   <li>Uses ProfileProperty for direct property assignment</li>
 *   <li>Caches profiles for performance optimization</li>
 * </ul>
 */
public class PaperSkullHandler implements SkullHandler {
    private final Map<String, PlayerProfile> profileMap = new ConcurrentHashMap<>();

    /**
     * Sets a PlayerProfile on the SkullMeta.
     *
     * @param meta    the SkullMeta to modify
     * @param profile the PlayerProfile with texture data
     */
    private void setSkull(SkullMeta meta, PlayerProfile profile) {
        meta.setPlayerProfile(profile);
    }

    /**
     * Sets skull texture using an OfflinePlayer.
     * Results are cached to avoid recreating profiles for the same player.
     *
     * @param meta   the SkullMeta to modify
     * @param player the OfflinePlayer
     */
    @Override
    public void setSkullByPlayer(SkullMeta meta, OfflinePlayer player) {
        PlayerProfile profile = profileMap.computeIfAbsent(player.getUniqueId().toString(), s -> Bukkit.createProfile(player.getUniqueId()));
        setSkull(meta, profile);
    }

    /**
     * Sets skull texture from a URL using the PlayerProfile API.
     * Results are cached to avoid recreating profiles for the same URL.
     *
     * @param meta the SkullMeta to modify
     * @param url  the texture URL
     */
    @Override
    public void setSkullByURL(SkullMeta meta, URL url) {
        PlayerProfile profile = profileMap.computeIfAbsent(url.toString(), url1 -> {
            PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
            playerProfile.setProperty(new ProfileProperty("textures",
                    Base64.getEncoder().encodeToString(
                            String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", url1).getBytes()
                    )
            ));
            return playerProfile;
        });
        setSkull(meta, profile);
    }

    /**
     * Sets skull texture from Base64-encoded texture data.
     * Results are cached to avoid recreating profiles for the same Base64 string.
     *
     * @param meta   the SkullMeta to modify
     * @param base64 the Base64-encoded texture data
     */
    @Override
    public void setSkullByBase64(SkullMeta meta, String base64) {
        PlayerProfile profile = profileMap.computeIfAbsent(base64, b -> {
            PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
            playerProfile.setProperty(new ProfileProperty("textures", base64));
            return playerProfile;
        });
        setSkull(meta, profile);
    }

    /**
     * Extracts the skull texture value (Base64 data) from SkullMeta.
     * Searches for the textures property in the PlayerProfile.
     *
     * @param meta the SkullMeta to query
     * @return the Base64 texture value, or empty string if not found
     */
    @Override
    public String getSkullValue(SkullMeta meta) {
        PlayerProfile profile = meta.getPlayerProfile();
        if (profile == null) {
            return "";
        }

        ProfileProperty texturesProperty = null;
        for (ProfileProperty property : profile.getProperties()) {
            if (property.getName().equalsIgnoreCase("textures")) {
                texturesProperty = property;
                break;
            }
        }
        if (texturesProperty == null) {
            return "";
        }

        return texturesProperty.getValue();
    }
}
