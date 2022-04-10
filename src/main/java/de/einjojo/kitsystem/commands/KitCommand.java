package de.einjojo.kitsystem.commands;

import de.einjojo.kitsystem.KitSystem;
import de.einjojo.kitsystem.util.ItemBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class KitCommand implements CommandExecutor {


    private final KitSystem plugin;
    private final Map<UUID, Long> COOLDOWN;

    private final ItemStack helmet;
    private final ItemStack chestplate;
    private final ItemStack legggins;
    private final ItemStack boots;
    private final List<ItemStack> itemsStackList;

    public KitCommand(KitSystem plugin) {
        plugin.getCommand("kit").setExecutor(this);
        this.COOLDOWN = new HashMap<>();
        this.plugin = plugin;

        this.helmet = loadArmorPiece("helmet");
        this.chestplate = loadArmorPiece("chest");
        this.legggins = loadArmorPiece("legs");
        this.boots = loadArmorPiece("boots");

        itemsStackList = loadItems();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if(!(player.hasPermission("kitsystem.start"))) {
            player.sendMessage(plugin.getMessage("time_limit"));
            return true;
        }

        if(COOLDOWN.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("time_limit"));

            if (COOLDOWN.get(player.getUniqueId()) < System.currentTimeMillis()) {
                COOLDOWN.remove(player.getUniqueId());
            } else {
                return true;
            }
        }

        if(player.getInventory().contains(helmet) || player.getInventory().contains(chestplate)
                || player.getInventory().contains(legggins) || player.getInventory().contains(boots)) {
            player.sendMessage(plugin.getMessage("kit_limit"));
            return true;
        }


        player.getInventory().addItem(helmet);
        player.getInventory().addItem(chestplate);
        player.getInventory().addItem(legggins);
        player.getInventory().addItem(boots);

        itemsStackList.forEach((item)-> {
            player.getInventory().addItem(item);
        });





        player.sendMessage(plugin.getMessage("kit_receive"));
        COOLDOWN.put(player.getUniqueId(), System.currentTimeMillis() + 1800000L); //30 Minuten warten
        return true;
    }

    private List<ItemStack> loadItems() {
        List<ItemStack> list = new ArrayList<>();
        plugin.getConfig().getStringList("kit.items").forEach((string) -> {
            String[] split = string.split(":");
            Material material = Material.AIR;
            String name = null;
            ArrayList<String> loreList = new ArrayList<>();
            int amount = 1;
            try {
                material = Material.valueOf(split[0]);
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                plugin.getLogger().info("Invalid Item: " + split[0] + " in " + string);
            }


            try {
                amount = Integer.parseInt(split[1]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                plugin.getLogger().info("Invalid Amount: " + split[1] + " in " + string);
            }

            try {
                name = ChatColor.translateAlternateColorCodes('&', split[2]);
            } catch (ArrayIndexOutOfBoundsException e) {}

            try {
                for (String s : split[3].split(";")) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&',s));
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                loreList = null;
            }



            ItemStack itemStack = new ItemStack(material, amount);
            ItemMeta meta = itemStack.getItemMeta();
            Validate.notNull(meta, "Could not create ItemMeta");
            String displayName = name != null ? name : meta.getDisplayName();
            List<String> lore = loreList != null ? loreList : meta.getLore();
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            itemStack.setItemMeta(meta);

            list.add(itemStack);
        });

        plugin.getLogger().info(String.format("Loaded %d Items", list.size()));
        return list;
    }

    private ItemStack loadArmorPiece(String key) {
        Material material;
        switch (key) {
            case "helmet":
                material = Material.LEATHER_HELMET;
                break;
            case "chest":
                material = Material.LEATHER_CHESTPLATE;
                break;
            case "legs":
                material = Material.LEATHER_LEGGINGS;
                break;
            case "boots":
                material = Material.LEATHER_BOOTS;
                break;
            default:
                material = Material.AIR;
        }
        return (plugin.getConfig().getBoolean(String.format("kit.%s.enabled", key))) ? new ItemBuilder(material).displayname(plugin.getKitName(key))
                .lore(plugin.getKitLore(key)).build() : new ItemStack(Material.AIR);
    }


}
