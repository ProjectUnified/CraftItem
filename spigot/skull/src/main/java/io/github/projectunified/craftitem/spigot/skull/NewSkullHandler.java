package io.github.projectunified.craftitem.spigot.skull;

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
 */
class NewSkullHandler implements SkullHandler {
    private final Map<String, PlayerProfile> profileMap = new ConcurrentHashMap<>();

    @Override
    public void setSkullByPlayer(SkullMeta meta, OfflinePlayer player) {
        meta.setOwningPlayer(player);
    }

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

    @Override
    public void setSkullByBase64(SkullMeta meta, String base64) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            JsonObject json = new Gson().fromJson(decoded, JsonObject.class);
            String url = json.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            setSkullByURL(meta, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
