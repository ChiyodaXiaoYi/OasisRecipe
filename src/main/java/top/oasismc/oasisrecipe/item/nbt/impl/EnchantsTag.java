package top.oasismc.oasisrecipe.item.nbt.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisrecipe.OasisRecipe;
import top.oasismc.oasisrecipe.item.nbt.api.NBTTag;

import java.util.ArrayList;
import java.util.List;

public enum EnchantsTag implements NBTTag {

    INSTANCE;

    private final String key;

    EnchantsTag() { key = "enchants"; }

    @Override
    public void importTag(String itemName, ItemStack item, YamlConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        List<String> enchants = new ArrayList<>();
        if (meta.hasEnchants()) {
            meta.getEnchants().forEach((enchant, lvl) -> {
                String type = enchant.getKey().getKey();
                enchants.add(type + " " + lvl);
            });
        }
        if (enchants.size() > 0)
            config.set(itemName + "." + key, enchants);
    }

    @Override
    public void loadTag(String itemName, ItemStack item, YamlConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        addEnchants2Meta(meta, itemName, config);
        item.setItemMeta(meta);
    }

    @Override
    public String getKey() {
        return key;
    }

    private void addEnchants2Meta(ItemMeta meta, String itemName, YamlConfiguration config) {
        List<String> enchantStrList = config.getStringList(itemName + "." + key);
        for (String enchantStr : enchantStrList) {
            String enchantTypeStr;
            int enchantLevel;
            enchantStr = enchantStr.toLowerCase();
            int spaceIndex = enchantStr.indexOf(" ");
            if (spaceIndex == -1) {
                enchantTypeStr = enchantStr;
                enchantLevel = 1;
            } else {
                enchantTypeStr = enchantStr.substring(0, spaceIndex);
                enchantLevel = Integer.parseInt(enchantStr.substring(spaceIndex + 1));
            }
            Enchantment enchantType = Enchantment.getByKey(NamespacedKey.minecraft(enchantTypeStr));
            if (enchantType == null)
                enchantType = Enchantment.getByName(enchantStr);
            if (enchantType == null) {
                OasisRecipe.info("&c" + enchantTypeStr + " is not a valid enchant type");
            }
            if (enchantType != null) {
                meta.addEnchant(enchantType, enchantLevel, true);
            }
        }
    }

}
