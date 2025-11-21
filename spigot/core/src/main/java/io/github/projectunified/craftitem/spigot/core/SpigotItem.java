package io.github.projectunified.craftitem.spigot.core;

import io.github.projectunified.craftitem.core.Item;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;
import java.util.function.Consumer;

public class SpigotItem implements Item {
    private final UUID owner;
    private ItemStack itemStack;

    public SpigotItem(ItemStack itemStack, UUID owner) {
        this.owner = owner;
        this.itemStack = itemStack.clone();
    }

    public SpigotItem(ItemStack itemStack) {
        this(itemStack, null);
    }

    public SpigotItem(Material material, UUID owner) {
        this.owner = owner;
        this.itemStack = new ItemStack(material);
    }

    public SpigotItem(Material material) {
        this(material, null);
    }

    public SpigotItem(UUID owner) {
        this.owner = owner;
        this.itemStack = new ItemStack(Material.STONE);
    }

    public SpigotItem() {
        this((UUID) null);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public void edit(Consumer<ItemStack> consumer) {
        consumer.accept(this.itemStack);
    }

    public void editMeta(Consumer<ItemMeta> consumer) {
        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta == null) return;
        consumer.accept(meta);
        this.itemStack.setItemMeta(meta);
    }

    public <T extends ItemMeta> void editMeta(Class<T> metaClass, Consumer<T> consumer) {
        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta == null) return;
        if (!metaClass.isInstance(meta)) return;
        consumer.accept(metaClass.cast(meta));
        this.itemStack.setItemMeta(meta);
    }

    @Override
    public void setName(String name) {
        editMeta(itemMeta -> itemMeta.setDisplayName(name));
    }

    @Override
    public void setAmount(int amount) {
        this.itemStack.setAmount(amount);
    }

    @Override
    public UUID getOwner() {
        return owner;
    }
}
