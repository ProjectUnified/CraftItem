package io.github.projectunified.craftitem.spigot.core;

import io.github.projectunified.craftitem.core.Item;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Spigot-specific implementation of the Item interface.
 * Wraps Bukkit's ItemStack and provides convenient modification methods.
 *
 * <p>Provides multiple constructors for flexibility and specialized methods
 * for editing item metadata and the underlying ItemStack.
 *
 * <p><strong>Example Usage:</strong>
 * <pre>{@code
 * SpigotItem item = new SpigotItem(Material.DIAMOND_SWORD, playerUUID);
 * item.setName("Legendary Sword");
 * item.setAmount(1);
 * item.editMeta(meta -> meta.setCustomModelData(1));
 * ItemStack resultStack = item.getItemStack();
 * }</pre>
 */
public class SpigotItem implements Item {
    private final UUID owner;
    private ItemStack itemStack;

    /**
     * Creates a new SpigotItem with the specified ItemStack and owner.
     *
     * @param itemStack the ItemStack to wrap (will be cloned)
     * @param owner     the UUID of the item's owner, or null
     */
    public SpigotItem(ItemStack itemStack, UUID owner) {
        this.owner = owner;
        this.itemStack = itemStack.clone();
    }

    /**
     * Creates a new SpigotItem with the specified ItemStack and no owner.
     *
     * @param itemStack the ItemStack to wrap (will be cloned)
     */
    public SpigotItem(ItemStack itemStack) {
        this(itemStack, null);
    }

    /**
     * Creates a new SpigotItem with the specified Material type and owner.
     *
     * @param material the Material type for the new ItemStack
     * @param owner    the UUID of the item's owner, or null
     */
    public SpigotItem(Material material, UUID owner) {
        this.owner = owner;
        this.itemStack = new ItemStack(material);
    }

    /**
     * Creates a new SpigotItem with the specified Material type and no owner.
     *
     * @param material the Material type for the new ItemStack
     */
    public SpigotItem(Material material) {
        this(material, null);
    }

    /**
     * Creates a new SpigotItem of STONE material with the specified owner.
     *
     * @param owner the UUID of the item's owner, or null
     */
    public SpigotItem(UUID owner) {
        this.owner = owner;
        this.itemStack = new ItemStack(Material.STONE);
    }

    /**
     * Creates a new SpigotItem of STONE material with no owner.
     */
    public SpigotItem() {
        this((UUID) null);
    }

    /**
     * Gets the underlying ItemStack.
     *
     * @return the ItemStack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Sets the underlying ItemStack (cloned to prevent external modifications).
     *
     * @param itemStack the new ItemStack
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    /**
     * Allows direct modification of the ItemStack.
     *
     * @param consumer the consumer to modify the ItemStack
     */
    public void edit(Consumer<ItemStack> consumer) {
        consumer.accept(this.itemStack);
    }

    /**
     * Allows modification of the item's metadata.
     *
     * @param consumer the consumer to modify the ItemMeta
     */
    public void editMeta(Consumer<ItemMeta> consumer) {
        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta == null) return;
        consumer.accept(meta);
        this.itemStack.setItemMeta(meta);
    }

    /**
     * Allows type-safe modification of specific ItemMeta subclasses.
     *
     * @param <T>       the ItemMeta subclass type
     * @param metaClass the class of the ItemMeta to modify
     * @param consumer  the consumer to modify the metadata
     */
    public <T extends ItemMeta> void editMeta(Class<T> metaClass, Consumer<T> consumer) {
        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta == null) return;
        if (!metaClass.isInstance(meta)) return;
        consumer.accept(metaClass.cast(meta));
        this.itemStack.setItemMeta(meta);
    }

    /**
     * Sets the display name of the item.
     *
     * @param name the new display name
     */
    @Override
    public void setName(String name) {
        editMeta(itemMeta -> itemMeta.setDisplayName(name));
    }

    /**
     * Sets the stack amount of the item.
     *
     * @param amount the stack size
     */
    @Override
    public void setAmount(int amount) {
        this.itemStack.setAmount(amount);
    }

    /**
     * Gets the UUID of the item's owner.
     *
     * @return the owner's UUID, or null if no owner
     */
    @Override
    public UUID getOwner() {
        return owner;
    }
}
