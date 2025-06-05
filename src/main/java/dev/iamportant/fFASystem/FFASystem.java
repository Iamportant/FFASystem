package dev.iamportant.fFASystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FFASystem extends JavaPlugin {

    private ArenaDataManager arenaDataManager;

    // Enable - Start
    @Override
    public void onEnable() {
        arenaDataManager = new ArenaDataManager();
        getServer().getPluginManager().registerEvents(new Event(this), this);
        PluginCommand command = getCommand("ffasystem");
        if (command != null) {
            command.setExecutor(this);
        }
        if (command != null) {
            command.setTabCompleter(this);
        }
    }
    // Enable - End

    // Commands - Start
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("ffasystem")) {
            if (args.length == 1) {
                completions.add("help");

                if (sender.hasPermission("ffasystem.reload")) {
                    completions.add("reload");
                }
                if (sender.hasPermission("ffasystem.setspawn")) {
                    completions.add("setspawn");
                }
                if (sender.hasPermission("ffasystem.setkit")) {
                    completions.add("setkit");
                }
            }
        }
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("ffasystem")) {
            return false;
        }

        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(sender);
                break;

            case "reload":
                if (!sender.hasPermission("neoffa.reload")) return true;
                arenaDataManager.reloadArenaConfig();
                sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
                break;

            case "setspawn":
                if (!sender.hasPermission("neoffa.setspawn")) return true;
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Console can't use this command!");
                    return true;
                }
                setSpawnLocation((Player) sender);
                break;

            case "setkit":
                if (!sender.hasPermission("neoffa.setkit")) return true;
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Console can't use this command!");
                    return true;
                }
                setKit((Player) sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown command! Use /ffasystem help for help.");
        }

        return true;
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "FFASystem" + " " + ChatColor.WHITE + "(" + "1.0" + ")");
        sender.sendMessage(ChatColor.WHITE + "Author: " + ChatColor.LIGHT_PURPLE + "Iamportant");
        sender.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.LIGHT_PURPLE + "/neoffa help");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + " ");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/neoffa reload" + ChatColor.WHITE + " Reloaded plugin config");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/neoffa setspawn" + ChatColor.WHITE + " Set the spawn point");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/neoffa setkit" + ChatColor.WHITE + " Set the kit");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + " ");

    }
    // Commands - End

    // Setup - Start
    private void setSpawnLocation(Player player) {
        Location location = player.getLocation();
        FileConfiguration arenaConfig = arenaDataManager.getArenaConfig();

        ConfigurationSection spawnSection = arenaConfig.createSection("arena.spawn");
        spawnSection.set("world", Objects.requireNonNull(location.getWorld()).getName());
        spawnSection.set("x", location.getX());
        spawnSection.set("y", location.getY());
        spawnSection.set("z", location.getZ());
        spawnSection.set("yaw", location.getYaw());
        spawnSection.set("pitch", location.getPitch());

        arenaDataManager.saveArenaConfig();
        player.sendMessage(ChatColor.GREEN + "Spawn set successfully!");
    }

    public Location arenaSpawn() {
        FileConfiguration arenaConfig = arenaDataManager.getArenaConfig();

        if (!arenaConfig.isConfigurationSection("arena.spawn")) {
            return null;
        }

        ConfigurationSection spawnSection = arenaConfig.getConfigurationSection("arena.spawn");

        String worldName = Objects.requireNonNull(spawnSection).getString("world");
        if (worldName == null || worldName.isEmpty()) {
            return null;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }

        return new Location(
                world,
                spawnSection.getDouble("x", 0),
                spawnSection.getDouble("y", 64),
                spawnSection.getDouble("z", 0),
                (float) spawnSection.getDouble("yaw", 0),
                (float) spawnSection.getDouble("pitch", 0)
        );
    }

    public void playerSpawn (Player player) {
        Location spawnLocation = arenaSpawn();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
        }
    }


    private void setKit(Player player) {
        FileConfiguration arenaConfig = arenaDataManager.getArenaConfig();
        ConfigurationSection kitSection = arenaConfig.createSection("arena.kit");

        ItemStack[] inventory = player.getInventory().getContents();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                kitSection.set("inventory." + i, inventory[i]);
            }
        }

        ItemStack[] armor = player.getInventory().getArmorContents();
        kitSection.set("armor.helmet", armor[3]);
        kitSection.set("armor.chestplate", armor[2]);
        kitSection.set("armor.leggings", armor[1]);
        kitSection.set("armor.boots", armor[0]);

        try {
            kitSection.set("offhand", player.getInventory().getItemInOffHand());
        } catch (NoSuchMethodError ignored) {
        }

        arenaDataManager.saveArenaConfig();
    }
    public boolean playerKit(Player player) {
        FileConfiguration arenaConfig = arenaDataManager.getArenaConfig();

        if (!arenaConfig.isConfigurationSection("arena.kit")) {
            return false;
        }

        ConfigurationSection kitSection = arenaConfig.getConfigurationSection("arena.kit");
        if (kitSection == null) {
            return false;
        }

        PlayerInventory inv = player.getInventory();

        try {
            inv.clear();

            if (kitSection.isConfigurationSection("inventory")) {
                ConfigurationSection invSection = kitSection.getConfigurationSection("inventory");
                if (invSection != null) {
                    for (String slotStr : invSection.getKeys(false)) {
                        try {
                            int slot = Integer.parseInt(slotStr);
                            ItemStack item = invSection.getItemStack(slotStr);
                            if (item != null) {
                                inv.setItem(slot, item.clone());
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            if (kitSection.isConfigurationSection("armor")) {
                ConfigurationSection armorSection = kitSection.getConfigurationSection("armor");
                if (armorSection != null) {
                    ItemStack[] armor = new ItemStack[4];
                    armor[3] = GetItemStack(armorSection, "helmet");
                    armor[2] = GetItemStack(armorSection, "chestplate");
                    armor[1] = GetItemStack(armorSection, "leggings");
                    armor[0] = GetItemStack(armorSection, "boots");
                    inv.setArmorContents(armor);
                }
            }

            if (kitSection.isItemStack("offhand")) {
                try {
                    ItemStack offhand = kitSection.getItemStack("offhand");
                    if (offhand != null) {
                        inv.setItemInOffHand(offhand.clone());
                    }
                } catch (NoSuchMethodError ignored) {
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private ItemStack GetItemStack(ConfigurationSection section, String path) {
        return section.isItemStack(path) ? Objects.requireNonNull(section.getItemStack(path)).clone() : null;
    }
    // Setup - End

    // Config - Start
    private class ArenaDataManager {
        private File arenaFile;
        private FileConfiguration arenaConfig;


        public ArenaDataManager() {
            setupArenaFile();
        }

        private void setupArenaFile() {
            arenaFile = new File(getDataFolder(), "arena.yml");
            if (!arenaFile.exists()) {
                saveResource("arena.yml", false);
            }
            reloadArenaConfig();
        }

        public FileConfiguration getArenaConfig() {
            return arenaConfig;
        }

        public void saveArenaConfig() {
            try {
                arenaConfig.save(arenaFile);
            } catch (IOException ignored) {
            }
        }

        public void reloadArenaConfig() {
            arenaFile = new File(getDataFolder(), "arena.yml");
            if (!arenaFile.exists()) {
                saveResource("arena.yml", false);
            }
            arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
        }
    }
    // Config - End
}
