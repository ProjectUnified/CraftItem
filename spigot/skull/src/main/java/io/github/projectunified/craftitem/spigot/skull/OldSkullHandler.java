package io.github.projectunified.craftitem.spigot.skull;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skull handler for older Bukkit versions (before 1.18 with PlayerProfile API).
 * Uses Mojang's GameProfile and reflection for compatibility.
 *
 * <p>Caches GameProfile instances to avoid recreating them for the same texture.
 * Uses reflection to access internal SkullMeta fields due to API limitations.
 *
 * <p><strong>Implementation Details:</strong>
 * <ul>
 *   <li>Attempts both newer setOwningPlayer() and older setOwner() methods</li>
 *   <li>Creates GameProfiles with texture properties for custom skins</li>
 *   <li>Uses reflection to access profile field in SkullMeta</li>
 *   <li>Caches profiles to improve performance with repeated textures</li>
 * </ul>
 */
@SuppressWarnings("deprecation")
class OldSkullHandler implements SkullHandler {
    private final Map<String, GameProfile> cache = new ConcurrentHashMap<>();
    private final Method getProfileMethod;

    /**
     * Initializes the OldSkullHandler by detecting the correct reflection method
     * for extracting property values (differs between Bukkit versions).
     */
    OldSkullHandler() {
        Method method = null;
        try {
            // noinspection JavaReflectionMemberAccess
            method = Property.class.getDeclaredMethod("value");
        } catch (Exception e) {
            try {
                // noinspection JavaReflectionMemberAccess
                method = Property.class.getDeclaredMethod("getValue");
            } catch (NoSuchMethodException ex) {
                // Ignore
            }
        }
        getProfileMethod = method;
    }

    /**
     * Sets skull texture using an OfflinePlayer.
     * Tries the newer setOwningPlayer() method first, falls back to older setOwner().
     *
     * @param meta   the SkullMeta to modify
     * @param player the OfflinePlayer
     */
    @Override
    public void setSkullByPlayer(SkullMeta meta, OfflinePlayer player) {
        try {
            // Try newer API first
            meta.setOwningPlayer(player);
        } catch (Exception e) {
            // Fallback to old API
            meta.setOwner(player.getName());
        }
    }

    /**
     * Sets a GameProfile on the SkullMeta using reflection.
     * Attempts setProfile() method first, then falls back to direct field access.
     *
     * @param meta    the SkullMeta to modify
     * @param profile the GameProfile with texture data
     */
    private void setSkullByGameProfile(SkullMeta meta, GameProfile profile) {
        try {
            Method setProfile = meta.getClass().getMethod("setProfile", GameProfile.class);
            setProfile.setAccessible(true);
            setProfile.invoke(meta, profile);
        } catch (Exception e) {
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (Exception ignored) {
                // Ignore
            }
        }
    }

    /**
     * Sets skull texture from a URL by creating a GameProfile with encoded texture data.
     * Results are cached to avoid recreating profiles for the same URL.
     *
     * @param meta the SkullMeta to modify
     * @param url  the texture URL
     */
    @Override
    public void setSkullByURL(SkullMeta meta, URL url) {
        GameProfile profile = cache.computeIfAbsent(url.toString(), url1 -> {
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
            gameProfile.getProperties().put("textures", new Property("textures",
                    Base64.getEncoder().encodeToString(
                            String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", url1).getBytes()
                    )
            ));
            return gameProfile;
        });
        setSkullByGameProfile(meta, profile);
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
        GameProfile gameProfile = cache.computeIfAbsent(base64, b -> {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", b));
            return profile;
        });
        setSkullByGameProfile(meta, gameProfile);
    }

    /**
     * Extracts the skull texture value (Base64 data) from SkullMeta.
     * Uses reflection to access the profile field.
     *
     * @param meta the SkullMeta to query
     * @return the Base64 texture value, or empty string if not found
     */
    @Override
    public String getSkullValue(SkullMeta meta) {
        GameProfile profile;
        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profile = (GameProfile) profileField.get(meta);
        } catch (Exception e) {
            return "";
        }

        if (profile == null) {
            return "";
        }

        Collection<Property> properties = profile.getProperties().get("textures");
        if (properties == null || properties.isEmpty()) {
            return "";
        }

        for (Property property : properties) {
            if (getProfileMethod == null) {
                continue;
            }

            String value;
            try {
                value = (String) getProfileMethod.invoke(property);
            } catch (Exception e) {
                continue;
            }

            if (!value.isEmpty()) {
                return value;
            }
        }

        return "";
    }
}
