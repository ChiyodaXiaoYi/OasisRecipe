package top.oasismc.oasisrecipe.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import top.oasismc.oasisrecipe.OasisRecipe;
import top.oasismc.oasisrecipe.api.ISubCommand;
import top.oasismc.oasisrecipe.cmd.subcmd.ImportCommand;
import top.oasismc.oasisrecipe.cmd.subcmd.ReloadCommand;
import top.oasismc.oasisrecipe.cmd.subcmd.RemoveCommand;
import top.oasismc.oasisrecipe.cmd.subcmd.VersionCommand;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum PluginCommand implements TabExecutor {

    INSTANCE;

    private final Map<String, ISubCommand> subCommandMap;

    PluginCommand() {
        subCommandMap = new ConcurrentHashMap<>();
        regDefaultSubCommands();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() < 1) {
            OasisRecipe.getPlugin().sendMsg(sender, "commands.noArgs");
            return true;
        }
        ISubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onCommand(sender, argList.subList(1, argList.size()));
        else {
            OasisRecipe.getPlugin().sendMsg(sender, "commands.noArgs");
            return true;
        }
    }

    private void regDefaultSubCommands() {
        regSubCommand(ImportCommand.INSTANCE);
        regSubCommand(ReloadCommand.INSTANCE);
        regSubCommand(VersionCommand.INSTANCE);
        regSubCommand(RemoveCommand.INSTANCE);
    }

    public void regSubCommand(ISubCommand executor) {
        subCommandMap.put(executor.getSubCommand(), executor);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() <= 1) {
            List<String> returnList = new ArrayList<>(subCommandMap.keySet());
            returnList.removeIf(str -> !str.startsWith(args[0]));
            return returnList;
        }
        ISubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onTabComplete(sender, argList.subList(1, argList.size()));
        else
            return Collections.singletonList("");
    }

    public Map<String, ISubCommand> getSubCommandMap() {
        return subCommandMap;
    }


}
