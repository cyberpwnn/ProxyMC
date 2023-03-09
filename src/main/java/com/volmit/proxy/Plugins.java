package com.volmit.proxy;

import art.arcane.curse.Curse;
import com.google.common.graph.MutableGraph;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Plugins {
    public static Plugin getPlugin(String query)
    {
        for(Plugin i : Bukkit.getPluginManager().getPlugins()) {
            if(i.getName().equalsIgnoreCase(query)) {
                return i;
            }
        }

        for(Plugin i : Bukkit.getPluginManager().getPlugins()) {
            if(i.getName().toLowerCase().startsWith(query.toLowerCase())) {
                return i;
            }
        }

        for(Plugin i : Bukkit.getPluginManager().getPlugins()) {
            if(i.getName().toLowerCase().contains(query.toLowerCase())) {
                return i;
            }
        }

        return null;
    }

    public static File pluginFile(String query)
    {
        File f = new File("plugins/" + query + ".jar");

        if(f.exists()) {
            return f;
        }

        Map<File, PluginDescriptionFile> pdfs = new HashMap<>();

        for(File i : new File("plugins").listFiles()) {
            try {
                PluginDescriptionFile pdf = pdfs.computeIfAbsent(i, (k) -> {
                    try {
                        return Proxy.instance.getPluginLoader().getPluginDescription(k);
                    } catch(InvalidDescriptionException e) {
                        throw new RuntimeException(e);
                    }
                });

                if(pdf.getName().equalsIgnoreCase(query)) {
                    return i;
                }
            } catch(Throwable e) {

            }
        }

        for(File i : new File("plugins").listFiles()) {
            try {
                PluginDescriptionFile pdf = pdfs.computeIfAbsent(i, (k) -> {
                    try {
                        return Proxy.instance.getPluginLoader().getPluginDescription(k);
                    } catch(InvalidDescriptionException e) {
                        throw new RuntimeException(e);
                    }
                });

                if(pdf.getName().toLowerCase().startsWith(query.toLowerCase())) {
                    return i;
                }
            } catch(Throwable e) {

            }
        }

        for(File i : new File("plugins").listFiles()) {
            try {
                PluginDescriptionFile pdf = pdfs.computeIfAbsent(i, (k) -> {
                    try {
                        return Proxy.instance.getPluginLoader().getPluginDescription(k);
                    } catch(InvalidDescriptionException e) {
                        throw new RuntimeException(e);
                    }
                });

                if(pdf.getName().toLowerCase().contains(query.toLowerCase())) {
                    return i;
                }
            } catch(Throwable e) {

            }
        }

        return null;
    }

    public static void delete(Plugin pp)
    {
        String n = pp.getName();
        File f = pluginFile(n);
        unload(pp);
        f.delete();
    }

    public static void reload(Plugin pp) {
        String n = pp.getName();
        File f = pluginFile(n);
        unload(pp);
        load(f);
    }

    public static Plugin load(File file) {
        try {
            Plugin p = Bukkit.getPluginManager().loadPlugin(file);
            p.onLoad();
            Bukkit.getPluginManager().enablePlugin(p);
            return p;
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void unload(Plugin plugin)
    {
        Proxy.info("Unloading " + plugin.getName() + " v" + plugin.getDescription().getVersion());
        JavaPluginLoader loader = (JavaPluginLoader) plugin.getPluginLoader();
        SimplePluginManager manager = (SimplePluginManager) Bukkit.getServer().getPluginManager();
        Proxy.info("Disabling " + plugin.getName() + " v" + plugin.getDescription().getVersion());
        manager.disablePlugin(plugin);
        Proxy.info("Disabled " + plugin.getName() + " v" + plugin.getDescription().getVersion());

        synchronized(manager) {
            Proxy.info("Unregistering Listeners for " + plugin.getName() + " v" + plugin.getDescription().getVersion());
            HandlerList.unregisterAll(plugin);
            Proxy.info("Cancelling Tasks for " + plugin.getName() + " v" + plugin.getDescription().getVersion());
            Bukkit.getScheduler().cancelTasks(plugin);
            List<Plugin> plugins = Curse.on(manager).get("plugins");
            Map<String, Plugin> lookupNames = Curse.on(manager).get("lookupNames");
            MutableGraph<String> dependencyGraph = Curse.on(manager).get("dependencyGraph");
            Map<String, Permission> permissions = Curse.on(manager).get("permissions");
            Map<Boolean, Set<Permission>> defaultPerms = Curse.on(manager).get("defaultPerms");

            if(plugins.remove(plugin)) {
                Proxy.info("Removed " + plugin.getName() + " from plugin list");
            }

            else {
                Proxy.warn("Couldn't find " + plugin.getName() + " in plugin list");
            }

            if(plugins.removeIf(i -> i.getClass().equals(plugin.getClass()))) {
                Proxy.info("Removed refs by class " + plugin.getName() + " from plugin list");
            }

            else {
                Proxy.warn("Couldn't find refs by class " + plugin.getName() + " in plugin list");
            }

            if(lookupNames.remove(plugin.getDescription().getName()) != null) {
                Proxy.info("Removed " + plugin.getName() + " from lookup names");
            }

            else {
                Proxy.warn("Couldn't find " + plugin.getName() + " in lookup names");
            }

            for(String i : new HashMap<>(lookupNames).keySet()) {
                if(lookupNames.get(i).getClass().equals(plugin.getClass())) {
                    lookupNames.remove(i);
                    Proxy.info("Removed '" + i + "' from lookup names");
                }
            }

            if(dependencyGraph.removeNode(plugin.getDescription().getName()))
            {
                Proxy.info("Removed " + plugin.getName() + " from dependency graph");
            }

            else {
                Proxy.warn("Couldn't find " + plugin.getName() + " in dependency graph");
            }
            dependencyGraph.edges().stream().filter(i ->
                i.nodeU().equals(plugin.getDescription().getName())
                || i.nodeV().equals(plugin.getDescription().getName()))
                .forEach(i -> {
                    if(dependencyGraph.removeEdge(i)) {
                        Proxy.info("Removed " + plugin.getName() + " from dependency graph edge " + i);
                    }

                    else {
                        Proxy.warn("Couldn't find " + plugin.getName() + " in dependency graph edge " + i);
                    }
                });
            Set<Permission> p = new HashSet<>(plugin.getDescription().getPermissions());

            for(String i : new HashSet<>(permissions.keySet())) {
                if(p.contains(permissions.get(i))) {
                    permissions.remove(i);
                    Proxy.info("Removed " + plugin.getName() + ":" + i + " from permissions");
                }
            }

            if(defaultPerms.get(true).removeAll(p))
            {
                Proxy.info("Removed " + plugin.getName() + " from default perms TRUE");
            }

            else {
                Proxy.warn("Couldn't find " + plugin.getName() + " in default perms TRUE");
            }
            if(defaultPerms.get(false).removeAll(p))
            {
                Proxy.info("Removed " + plugin.getName() + " from default perms FALSE");
            }

            else {
                Proxy.warn("Couldn't find " + plugin.getName() + " in default perms FALSE");
            }
        }

        synchronized(loader) {
            List<?> loaders = Curse.on(loader).get("loaders");

            for(Object i : new ArrayList<>(loaders)) {
                JavaPlugin p = Curse.on(i).get("plugin");
                if(p != null)
                {
                    if(p.getClass().equals(plugin.getClass())) {
                        if(loaders.remove(i))
                        {
                            Proxy.info("Removed " + plugin.getName() + " from loaders");
                        }

                        else{
                            Proxy.warn("Couldn't remove " + plugin.getName() + " in loaders?");
                        }
                    }
                }
            }
        }

        Proxy.info("Calling GC to unlock dangling file locks");
        System.gc();
        Proxy.info("Unloaded " + plugin.getName() + "!");
    }
}
