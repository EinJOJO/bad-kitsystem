package de.einjojo.kitsystem;

import de.einjojo.kitsystem.commands.KitCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class KitSystem extends JavaPlugin {

    private final HashMap<String, String> messages = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        getConfig().getConfigurationSection("messages").getValues(false).forEach((s, o) -> {
            messages.put(s, (String) o);
        });
        messages.forEach((s, o) -> {
            getLogger().info(s + " - " + o);
        });

        new KitCommand(this);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    public String getPrefix() {
        String prefix = this.getConfig().getString("messages.prefix");
        return prefix != null ? ChatColor.translateAlternateColorCodes('&', prefix) : "§7[§cKaktussucht§7] ";
    }

    public String getKitName(String element) {
        String name = getConfig().getConfigurationSection("kit").getConfigurationSection(element).getString("name");
        if(name == null) {
            return "undefined";
        }
        return name;
    }
    public List<String> getKitLore(String element) {
        ArrayList<String> list = new ArrayList<>();
        getConfig().getConfigurationSection("kit").getConfigurationSection(element).getStringList("lore").forEach((entry) -> {
            list.add(ChatColor.translateAlternateColorCodes('&', entry));
        });
        return list;
    }


    public String getMessage(String key) {
        String res = messages.get(key);
        if(res == null) {
            res = "undefined";
            getLogger().log(Level.WARNING,"Could not get message string: " + key);
        }
        res = res.replace("%prefix%", getPrefix());

        return ChatColor.translateAlternateColorCodes('&', res);

    }



}
