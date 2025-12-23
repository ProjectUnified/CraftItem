package io.github.projectunified.craftitem.spigot.skull.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skull handler for newer Bukkit versions (1.18+) that have the PlayerProfile API.
 *
 * <p>Uses the modern Bukkit API for setting player profiles and textures.
 * Caches PlayerProfile instances to avoid recreation for repeated textures.
 *
 * <p><strong>Implementation Details:</strong>
 * <ul>
 *   <li>Uses Bukkit.createPlayerProfile() to create profiles</li>
 *   <li>Uses PlayerTextures.setSkin() for direct URL assignment</li>
 *   <li>Decodes Base64 texture data to extract texture URL</li>
 *   <li>Caches profiles for performance optimization</li>
 * </ul>
 */
class NewSkullHandler implements SkullHandler {
    private final Gson gson = new Gson();
    private final Map<String, PlayerProfile> profileMap = new ConcurrentHashMap<>();

    /**
     * Sets skull texture using an OfflinePlayer.
     * Uses the modern setOwningPlayer() method.
     *
     * @param meta   the SkullMeta to modify
     * @param player the OfflinePlayer
     */
    @Override
    public void setSkullByPlayer(SkullMeta meta, OfflinePlayer player) {
        meta.setOwningPlayer(player);
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
        PlayerProfile profile = profileMap.computeIfAbsent(url.toString(), u -> {
            PlayerProfile newProfile = Bukkit.createPlayerProfile(UUID.randomUUID(), "");
            PlayerTextures textures = newProfile.getTextures();
            try {
                textures.setSkin(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return newProfile;
        });
        meta.setOwnerProfile(profile);
    }

    /**
     * Sets skull texture from Base64-encoded texture data.
     * Decodes the Base64 data to extract the texture URL, then applies it.
     *
     * @param meta   the SkullMeta to modify
     * @param base64 the Base64-encoded texture data (JSON format)
     * @throws RuntimeException if the Base64 data is invalid or malformed
     */
    @Override
    public void setSkullByBase64(SkullMeta meta, String base64) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            JsonObject json = gson.fromJson(decoded, JsonObject.class);
            String url = json.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            setSkullByURL(meta, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts the skull texture URL from SkullMeta.
     *
     * @param meta the SkullMeta to query
     * @return the texture URL as a string, or empty string if not found
     */
    @Override
    public String getSkullValue(SkullMeta meta) {
        PlayerProfile profile = meta.getOwnerProfile();
        if (profile == null) {
            return "";
        }

        PlayerTextures textures = profile.getTextures();
        if (textures == null) {
            return "";
        }

        URL url = textures.getSkin();
        if (url == null) {
            return "";
        }

        return url.toString();
    }
}
