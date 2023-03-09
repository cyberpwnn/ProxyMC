package com.volmit.proxy;

import art.arcane.curse.Curse;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class Proxy extends JavaPlugin {
    public static Proxy instance;

    public void onEnable() {
        instance = this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("proxy"))
        {
            if(args.length == 2)
            {
                String c = args[0];
                String p = args[1];

                if(c.equalsIgnoreCase("unload"))
                {
                    Plugin pp = Plugins.getPlugin(p);

                    if(pp == null)
                    {
                        sender.sendMessage("Plugin not found.");
                        return true;
                    }

                    Plugins.unload(pp);
                    sender.sendMessage("Unloaded " + pp.getName());
                    return true;
                }

                else if(c.equalsIgnoreCase("load"))
                {
                    File f = Plugins.pluginFile(p);
                    if(f == null)
                    {
                        sender.sendMessage("Plugin not found.");
                        return true;
                    }

                    try {
                        Plugins.load(f);
                        sender.sendMessage("Loaded " + p);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        sender.sendMessage("Failed to load " + p + " see the console");
                    }
                    return true;
                }

                else if(c.equalsIgnoreCase("reload"))
                {
                    Plugin pp = Plugins.getPlugin(p);

                    if(pp == null)
                    {
                        sender.sendMessage("Plugin not found.");
                        return true;
                    }

                    Plugins.reload(pp);
                    sender.sendMessage("Reloaded " + pp.getName());

                    return true;
                }

                else if(c.equalsIgnoreCase("delete"))
                {
                    Plugin pp = Plugins.getPlugin(p);

                    if(pp == null)
                    {
                        sender.sendMessage("Plugin not found.");
                        return true;
                    }

                    Plugins.delete(pp);
                    sender.sendMessage("Deleted " + pp.getName());

                    return true;
                }
            }

            sender.sendMessage("/proxy unload <plugin>");
            sender.sendMessage("/proxy load <plugin>");
            sender.sendMessage("/proxy reload <plugin>");
            sender.sendMessage("/proxy delete <plugin>");

            return true;
        }

        return false;
    }

    public static void msg(String string) {
        try {
            if (instance == null) {
                System.out.println("[Proxy]: " + string);
                return;
            }

            String msg = ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "Proxy" + ChatColor.GRAY + "]: " + string;
            Bukkit.getConsoleSender().sendMessage(msg);
        } catch (Throwable e) {
            System.out.println("[Proxy]: " + string);
        }
    }

    public static void actionbar(Player p, String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
    }

    public static void warn(String string) {
        msg(ChatColor.YELLOW + string);
    }

    public static void error(String string) {
        msg(ChatColor.RED + string);
    }

    public static void info(String string) {
        msg(ChatColor.WHITE + string);
    }
}
