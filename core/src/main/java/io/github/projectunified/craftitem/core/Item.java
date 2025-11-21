package io.github.projectunified.craftitem.core;

import java.util.UUID;

/**
 * Represents a generic item with name, amount, and ownership information.
 *
 * <p>This interface defines the core API for modifying item properties.
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * Item item = new SpigotItem(Material.DIAMOND_SWORD);
 * item.setName("Legendary Sword");
 * item.setAmount(1);
 * UUID owner = item.getOwner();
 * }</pre>
 */
public interface Item {
    /**
     * Sets the display name of the item.
     *
     * @param name the new display name for the item
     */
    void setName(String name);

    /**
     * Sets the stack amount of the item.
     *
     * @param amount the stack size (typically 1-64)
     */
    void setAmount(int amount);

    /**
     * Gets the UUID of the item's owner.
     *
     * @return the owner's UUID, or null if no owner is set
     */
    UUID getOwner();
}
