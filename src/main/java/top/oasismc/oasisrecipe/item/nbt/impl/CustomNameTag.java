package top.oasismc.oasisrecipe.item.nbt.impl;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisrecipe.item.nbt.api.NBTTag;

import static top.oasismc.oasisrecipe.OasisRecipe.color;

public enum CustomNameTag implements NBTTag {

    INSTANCE;

    private final String key;

    CustomNameTag() {this.key = "name";}

    @Override
    public void importTag(String itemName, ItemStack item, YamlConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName())
            config.set(itemName + "." + key, meta.getDisplayName());
    }

    @Override
    public void loadTag(String itemName, ItemStack item, YamlConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        String customName = config.getString(itemName + "." + key, "");
        if (!customName.equals("")) {
            meta.setDisplayName(color(customName));
        }//设置合成物品的名字
        item.setItemMeta(meta);
    }

    @Override
    public String getKey() {
        return key;
    }

}
