package io.github.projectunified.craftitem.spigot.core;

import io.github.projectunified.craftitem.core.Item;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class SpigotItem implements Item {
    private ItemStack itemStack;

    public SpigotItem(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public SpigotItem(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public SpigotItem() {
        this.itemStack = new ItemStack(Material.STONE);
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
}
