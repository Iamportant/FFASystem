package dev.iamportant.fFASystem;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Event implements Listener {

    private static final Map<UUID, Integer> type = new HashMap<>();
    private static final Map<UUID, UUID> target = new HashMap<>();

    private final FFASystem plugin;

    public Event(FFASystem plugin) {
        this.plugin = plugin;
    }

    // CORE #1 - Start
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        type.put(player.getUniqueId(), 1);
        target.put(player.getUniqueId(), null);
        plugin.playerKit(player);
        plugin.playerSpawn(player);
        double pMaxHealth = player.getMaxHealth();
        player.setHealth(pMaxHealth);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID target5 = target.get(player.getUniqueId());
        if (target5 != null) {
            Player target1 = Bukkit.getPlayer(target5);
            if (target1 != null) {
                type.put(target1.getUniqueId(), 1);
                target.put(target1.getUniqueId(), null);
                target1.playSound(target1.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
                if (type.get(target1.getUniqueId()) == 1) {
                    // inviting
                    target1.sendMessage(ChatColor.translateAlternateColorCodes('&', ("&7&lChallenge canceled! &fPlayer &7{target} &fhas left the server.").replace("{target}", player.getName())));
                }
                if (type.get(target1.getUniqueId()) == 2) {
                    // inviting
                    target1.sendMessage(ChatColor.translateAlternateColorCodes('&', ("&7&lChallenge canceled! &fPlayer &7{target} &fhas left the server.").replace("{target}", player.getName())));
                }
                if (type.get(target1.getUniqueId()) == 3) {
                    // fighting
                    target1.sendMessage(ChatColor.translateAlternateColorCodes('&', ("&7&lMatch aborted! &fOpponent &7{target} &fhas left the server.").replace("{target}", player.getName())));
                }
            }
        }
    }
    // CORE #1 - End

    // CORE #2 - Start
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player gg = event.getEntity();

        UUID targetUUID = target.get(gg.getUniqueId());
        Player killer;
        if (targetUUID != null) {
            killer = Bukkit.getPlayer(targetUUID);
            if (killer != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.playerKit(killer);
                    }
                }.runTaskLater(plugin, 1L);
                type.put(killer.getUniqueId(), 1);
                target.put(killer.getUniqueId(), null);
                killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                gg.playSound(gg.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
                killer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lVictory! &fYou killed player &6{gg} &fand have &6{health} &fhealth remaining.".replace("{gg}", gg.getName())).replace("{health}", String.format("%.2f", killer.getHealth())));
                gg.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lDefeat! &fYou were killed by player &c{won}&f, who has &c{health} &fhealth remaining.".replace("{won}", killer.getName())).replace("{health}", String.format("%.2f", killer.getHealth())));
                sendActionBar(killer, "&r");
                sendActionBar(gg, "&r");
                double killerMaxHealth = killer.getMaxHealth();
                killer.setHealth(killerMaxHealth);
            }
        }

        type.put(gg.getUniqueId(), 1);
        target.put(gg.getUniqueId(), null);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        Player player = event.getEntity();

        new BukkitRunnable() {
            @Override
            public void run() {
                player.spigot().respawn();
            }
        }.runTaskTimer(plugin, 1L, 0L);
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.playerSpawn(player);
                plugin.playerKit(player);
            }
        }.runTaskLater(plugin, 2L);
    }
    // CORE #2 - End

    // CORE #3 - Start
    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING)) {
                        if (type.get(player.getUniqueId()) == 1 && target.get(player.getUniqueId()) != null) {
                            int seconds = Objects.requireNonNull(player.getPotionEffect(PotionEffectType.WATER_BREATHING)).getDuration() / 20;
                            if (seconds < 10) {
                                sendActionBar(player, "&fNew challenge: &e" + Objects.requireNonNull(Bukkit.getPlayer(target.get(player.getUniqueId()))).getName() + " &fTime: &e00:0" + seconds);
                            } else {
                                sendActionBar(player, "&fNew challenge: &e" + Objects.requireNonNull(Bukkit.getPlayer(target.get(player.getUniqueId()))).getName() + " &fTime: &e00:" + seconds);
                            }
                        }
                        if (type.get(player.getUniqueId()) == 2) {
                            int seconds = Objects.requireNonNull(player.getPotionEffect(PotionEffectType.WATER_BREATHING)).getDuration() / 20;
                            if (seconds < 10) {
                                sendActionBar(player, "&fInvited player: &e" + Objects.requireNonNull(Bukkit.getPlayer(target.get(player.getUniqueId()))).getName() + " &fTime: &e00:0" + seconds);
                            } else {
                                sendActionBar(player, "&fInvited player: &e" + Objects.requireNonNull(Bukkit.getPlayer(target.get(player.getUniqueId()))).getName() + " &fTime: &e00:" + seconds);
                            }
                        }
                        if (type.get(player.getUniqueId()) == 3) {
                            int seconds = Objects.requireNonNull(player.getPotionEffect(PotionEffectType.WATER_BREATHING)).getDuration() / 20;
                            if (seconds < 10) {
                                sendActionBar(player, "&fOpponent: &a" + Objects.requireNonNull(Bukkit.getPlayer(target.get(player.getUniqueId()))).getName() + " &fTime: &a00:0" + seconds);
                            } else {
                                sendActionBar(player, "&fOpponent: &a" + Objects.requireNonNull(Bukkit.getPlayer(target.get(player.getUniqueId()))).getName() + " &fTime: &a00:" + seconds);
                            }

                        }
                    }
                    if (!player.hasPotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING) && target.get(player.getUniqueId()) != null) {
                        if (type.get(player.getUniqueId()) == 1 && target.get(player.getUniqueId()) != null) {
                            type.put(player.getUniqueId(), 1);
                            target.put(player.getUniqueId(), null);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ("&cYou did not accept the duel request, so your challenge has been canceled.")));
                            sendActionBar(player, "&r");
                        }
                        if (type.get(player.getUniqueId()) == 2) {
                            type.put(player.getUniqueId(), 1);
                            target.put(player.getUniqueId(), null);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ("&cThe player didn't accept your duel request, so it has been canceled.")));
                            sendActionBar(player, "&r");
                        }
                        if (type.get(player.getUniqueId()) == 3) {
                            type.put(player.getUniqueId(), 1);
                            target.put(player.getUniqueId(), null);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ("&7&lDuel aborted! &fThe battle was terminated early due to a lack of attacks from both sides.")));
                            sendActionBar(player, "&r");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }
    // CORE #3 - End


    // CORE #4 - Start
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player defender = (Player) event.getEntity();

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {

            if (type.get(attacker.getUniqueId()) == 3 && type.get(defender.getUniqueId()) == 3 && target.get(attacker.getUniqueId()) == defender.getUniqueId() && target.get(defender.getUniqueId()) == attacker.getUniqueId()) {
                attacker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING, 319, 0, false, false));
                defender.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING, 319, 0, false, false));
            }

            if (type.get(attacker.getUniqueId()) == 3 && (type.get(defender.getUniqueId()) != 3 || target.get(attacker.getUniqueId()) != defender.getUniqueId())) {
                event.setCancelled(true);
                attacker.sendTitle(ChatColor.RED + "", ChatColor.RED + "In combat—attack only your opponent!", 1, 70, 1);
                return;
            }

            if (target.get(defender.getUniqueId()) == attacker.getUniqueId() && target.get(attacker.getUniqueId()) == defender.getUniqueId() && type.get(attacker.getUniqueId()) == 1 && type.get(defender.getUniqueId()) == 2) {
                type.put(defender.getUniqueId(), 3);
                type.put(attacker.getUniqueId(), 3);
                defender.playSound(defender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0F, 1.0F);
                attacker.playSound(attacker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0F, 1.0F);
                attacker.sendTitle(ChatColor.RED + "", ChatColor.GREEN + "Fight!", 1, 20, 1);
                defender.sendTitle(ChatColor.RED + "", ChatColor.GREEN + "Fight!", 1, 20, 1);
                event.setDamage(0);
                attacker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING, 319, 0, false, false));
                defender.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING, 319, 0, false, false));
                return;
            }

            if (type.get(attacker.getUniqueId()) == 2) {
                event.setCancelled(true);
                attacker.sendTitle(ChatColor.RED + "", ChatColor.YELLOW + "No attacks until your challenge is answered.", 1, 30, 1);
                return;
            }

            if (type.get(attacker.getUniqueId()) == 1 && target.get(attacker.getUniqueId()) != null && target.get(attacker.getUniqueId()) != defender.getUniqueId()) {
                event.setCancelled(true);
                attacker.sendTitle(ChatColor.RED + "", ChatColor.YELLOW + "Do not attack non-target players.", 1, 30, 1);
                return;
            }

            if (target.get(defender.getUniqueId()) != null && target.get(defender.getUniqueId()) != attacker.getUniqueId()) {
                event.setCancelled(true);
                attacker.sendTitle(ChatColor.RED + "", ChatColor.RED + "Do not attack non-idle players!", 1, 30, 1);
                return;
            }

            if (type.get(attacker.getUniqueId()) == 1 && type.get(defender.getUniqueId()) == 1) {
                target.put(defender.getUniqueId(), attacker.getUniqueId());
                target.put(attacker.getUniqueId(), defender.getUniqueId());
                type.put(defender.getUniqueId(), 1);
                type.put(attacker.getUniqueId(), 2);
                attacker.playSound(attacker.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                defender.playSound(defender.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lChallenge! &fYou have sent a duel request to player &a{to}&f.".replace("{to}", defender.getName())));
                defender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lChallenge! &fPlayer &a{by} &fhas sent you a duel request, strike back to start the duel!".replace("{by}", attacker.getName())));
                event.setDamage(0);
                attacker.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING, 319, 0, false, false));
                defender.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING, 319, 0, false, false));
            }
        }
    }
    // CORE #4 - End

    // No Hunger - Start
    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }
    // No Hunger - End

    // Actionbar - Start
    private void sendActionBar(Player player, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
    // Actionbar - End
}
