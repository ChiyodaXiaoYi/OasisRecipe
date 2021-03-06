package top.oasismc.oasisrecipe.item.nbt.impl;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisrecipe.item.nbt.api.NBTTag;

public enum DamageableTag implements NBTTag {

    INSTANCE;

    private final String key;

    DamageableTag() {this.key = "durability"; }

    @Override
    public void importTag(String itemName, ItemStack item, YamlConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable)
            config.set(itemName + "." + key, ((Damageable) meta).getDamage());
    }

    @Override
    public void loadTag(String itemName, ItemStack item, YamlConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(config.getInt(itemName + "." + key, 0));
        }
        item.setItemMeta(meta);
    }

    @Override
    public String getKey() {
        return key;
    }
}
