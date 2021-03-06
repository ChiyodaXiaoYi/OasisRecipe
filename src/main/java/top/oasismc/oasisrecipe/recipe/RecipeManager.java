package top.oasismc.oasisrecipe.recipe;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import top.oasismc.oasisrecipe.OasisRecipe;
import top.oasismc.oasisrecipe.api.RecipeRegistrar;
import top.oasismc.oasisrecipe.cmd.subcmd.RemoveCommand;
import top.oasismc.oasisrecipe.config.ConfigFile;
import top.oasismc.oasisrecipe.item.ItemLoader;

import java.util.*;

import static top.oasismc.oasisrecipe.OasisRecipe.color;
import static top.oasismc.oasisrecipe.OasisRecipe.info;
import static top.oasismc.oasisrecipe.item.ItemLoader.getChoiceFromStr;
import static top.oasismc.oasisrecipe.item.ItemLoader.getItemFromConfig;

public enum RecipeManager {

    INSTANCE;

    private final ConfigFile recipeFile;
    private final List<String> keyList;

    RecipeManager() {
        recipeFile = new ConfigFile("recipe.yml");
        keyList = new ArrayList<>();
    }

    public void loadRecipesFromConfig() {
        YamlConfiguration config = (YamlConfiguration) recipeFile.getConfig();
        for (String recipeName : config.getKeys(false)) {
            addRecipe(recipeName, config);
        }
    }

    public void addRecipe(String recipeName, YamlConfiguration config) {
        try {
            String key = Objects.requireNonNull(config.getString(recipeName + ".key")).toLowerCase();
            List<String> choiceStrList = config.getStringList(recipeName + ".items");
            RecipeChoice[] choices = new RecipeChoice[choiceStrList.size()];
            switch (config.getString(recipeName + ".type", "shaped")) {
                case "shaped":
                    choices = getShapedRecipeItems(choiceStrList);
                    break;
                case "shapeless":
                    choices = getShapelessRecipeItems(choiceStrList);
                    break;
                case "furnace":
                case "smoking":
                case "campfire":
                case "blasting":
                case "random_furnace":
                case "random_smoking":
                case "random_blasting":
                case "stoneCutting":
                    choices[0] = getChoiceFromStr(choiceStrList.get(0));
                    break;
                case "smithing":
                    choices[0] = getChoiceFromStr(choiceStrList.get(0));
                    choices[1] = getChoiceFromStr(choiceStrList.get(1));
                    break;
            }//???????????????????????????

            String resultStr = config.getString(recipeName + ".result", "Null");
            if (config.getString(recipeName + ".type", "shaped").startsWith("random_")) {
                resultStr = config.getStringList(recipeName + ".result").get(0);
                resultStr = resultStr.substring(0, resultStr.indexOf(" "));
            }
            ItemStack result = getItemFromConfig(resultStr);//?????????????????????

            switch (config.getString(recipeName + ".type", "shaped")) {
                case "shaped"://??????
                    addShapedRecipe(key, result, choices);
                    break;
                case "shapeless"://??????
                    addShapelessRecipe(key, result, choices);
                    break;
                case "furnace":
                case "smoking":
                case "campfire":
                case "blasting":
                    if (choiceStrList.get(0).startsWith("ItemsAdder:")) {
                        String itemsAdderId = choiceStrList.get(0);
                        itemsAdderId = itemsAdderId.substring(itemsAdderId.indexOf(":") + 1);
                        ItemLoader.INSTANCE.getItemsAdderItemMap().put(itemsAdderId, result);
                    }
                    int exp = config.getInt(recipeName + ".exp", 0);
                    int cookTime = config.getInt(recipeName + ".time", 10) * 20;
                    addCookingRecipe(key, result, choices[0], exp, cookTime, config.getString(recipeName + ".type", "furnace"));
                    break;
                case "random_furnace":
                case "random_smoking":
                case "random_blasting":
                    if (OasisRecipe.getPlugin().getVanillaVersion() < 18)
                        break;
                    exp = config.getInt(recipeName + ".exp", 0);
                    cookTime = config.getInt(recipeName + ".time", 10) * 20;
                    addCookingRecipe(key, result, choices[0], exp, cookTime, config.getString(recipeName + ".type", "furnace"));
                    break;
                case "smithing":
                    if (OasisRecipe.getPlugin().getVanillaVersion() < 14)
                        break;
                    addSmithingRecipe(key, result, choices[0], choices[1]);
                    break;
                case "stoneCutting":
                    if (OasisRecipe.getPlugin().getVanillaVersion() < 14)
                        break;
                    addStoneCuttingRecipe(key, result, choices[0]);
                    break;
            }
            keyList.add(config.getString(recipeName + ".key"));
        } catch (Exception e) {
            info(color("&cSome errors occurred while loading the " + recipeName + " recipe, please check your config file"));
            e.printStackTrace();
        }
    }


    public RecipeChoice[] getShapedRecipeItems(List<String> items) {
        RecipeChoice[] choices = new RecipeChoice[9];
        for(int i = 0; i < 3; i++) {
            int space1 = items.get(i).indexOf(" ");
            String item1 = items.get(i).substring(0, space1);
            int space2 = items.get(i).indexOf(" ", space1 + 1);
            String item2 = items.get(i).substring(space1 + 1, space2);
            String item3 = items.get(i).substring(space2 + 1);
            choices[i * 3] = getChoiceFromStr(item1);
            choices[i * 3 + 1] = getChoiceFromStr(item2);
            choices[i * 3 + 2] = getChoiceFromStr(item3);
        }
        return choices;
    }

    public RecipeChoice[] getShapelessRecipeItems(List<String> items) {
        RecipeChoice[] choices = new RecipeChoice[items.size()];
        for (int i = 0; i < items.size(); i++) {
            choices[i] = getChoiceFromStr(items.get(i));
        }
        return choices;
    }

    public List<String> getKeyList() {
        return keyList;
    }

    public void addStoneCuttingRecipe(String key, ItemStack result, RecipeChoice item) {
        NamespacedKey recipeKey = new NamespacedKey(OasisRecipe.getPlugin(), key);
        StonecuttingRecipe recipe = new StonecuttingRecipe(recipeKey, result, item);
        Bukkit.getServer().addRecipe(recipe);
    }

    public void addSmithingRecipe(String key, ItemStack result, RecipeChoice base, RecipeChoice addition) {
        NamespacedKey recipeKey = new NamespacedKey(OasisRecipe.getPlugin(), key);
        SmithingRecipe recipe = new SmithingRecipe(recipeKey, result, base, addition);
        Bukkit.getServer().addRecipe(recipe);
    }

    public void addShapedRecipe(String key, ItemStack result, RecipeChoice[] itemList) {
        NamespacedKey recipeKey = new NamespacedKey(OasisRecipe.getPlugin(), key);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
        recipe = recipe.shape("abc", "def", "ghi");
        int i = 0;
        String temp = "abcdefghi";
        for (RecipeChoice choice : itemList) {
            recipe.setIngredient(temp.charAt(i), choice);
            i++;
        }
        Bukkit.getServer().addRecipe(recipe);
    }

    public void addShapelessRecipe(String key, ItemStack result, RecipeChoice[] itemList) {
        NamespacedKey recipeKey = new NamespacedKey(OasisRecipe.getPlugin(), key);
        ShapelessRecipe recipe = new ShapelessRecipe(recipeKey, result);
        for (RecipeChoice choice : itemList) {
            recipe.addIngredient(choice);
        }
        Bukkit.getServer().addRecipe(recipe);
    }

    public void addCookingRecipe(String key, ItemStack result, RecipeChoice item, int exp, int cookingTime, String type) {
        NamespacedKey recipeKey = new NamespacedKey(OasisRecipe.getPlugin(), key);
        Recipe recipe;
        switch (type) {
            case "furnace":
            case "random_furnace":
            default:
                recipe = new FurnaceRecipe(recipeKey, result, item, exp, cookingTime);
                break;
            case "smoking":
            case "random_smoking":
                recipe = new SmokingRecipe(recipeKey, result, item, exp, cookingTime);
                break;
            case "blasting":
            case "random_blasting":
                recipe = new BlastingRecipe(recipeKey, result, item, exp, cookingTime);
                break;
            case "campfire":
                recipe = new CampfireRecipe(recipeKey, result, item, exp, cookingTime);
                break;
        }
        Bukkit.getServer().addRecipe(recipe);
    }

    public String getRecipeName(Recipe recipe) {
        Set<String> recipes = getRecipeFile().getConfig().getKeys(false);
        NamespacedKey namespacedKey = null;
        if (recipe instanceof ShapedRecipe) {
            namespacedKey = ((ShapedRecipe) recipe).getKey();
        } else if (recipe instanceof ShapelessRecipe){
            namespacedKey = ((ShapelessRecipe) recipe).getKey();
        } else if (recipe instanceof SmithingRecipe) {
            namespacedKey = ((SmithingRecipe) recipe).getKey();
        } else if (recipe instanceof CookingRecipe) {
            namespacedKey = ((CookingRecipe<?>) recipe).getKey();
        }
        if (namespacedKey == null) {
            return null;
        }
        for (String key : recipes) {
            String keyName = getRecipeFile().getConfig().getString(key + ".key", "");
            if (namespacedKey.getKey().equals(keyName)) {
                return key;
            }
        }
        return null;
    }

    public ConfigFile getRecipeFile() {
        return recipeFile;
    }

    public void reloadRecipes() {
        Bukkit.resetRecipes();
        getKeyList().clear();
        loadRecipesFromConfig();
        loadRecipeFromOtherPlugins();
        removeVanillaRecipes();
        ((RemoveCommand) RemoveCommand.INSTANCE).reloadRecipeMap();
    }

    private void removeVanillaRecipes() {
        List<String> removedRecipes = RemoveCommand.getRemovedRecipeConfig().getConfig().getStringList("recipes");
        ((RemoveCommand) RemoveCommand.INSTANCE).removeRecipes(removedRecipes);
    }

    private void loadRecipeFromOtherPlugins() {
        Map<Plugin, List<Recipe>> pluginRecipeMap = RecipeRegistrar.getPluginRecipeMap();
        for (Plugin plugin : pluginRecipeMap.keySet()) {
            for (Recipe recipe : pluginRecipeMap.get(plugin)) {
                OasisRecipe.getPlugin().getServer().addRecipe(recipe);
            }
        }
    }

}
