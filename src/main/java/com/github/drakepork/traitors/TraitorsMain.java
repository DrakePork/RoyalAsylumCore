package com.github.drakepork.traitors;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.github.drakepork.traitors.Commands.*;
import me.realized.tokenmanager.TokenManagerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class TraitorsMain extends JavaPlugin implements Listener {
    public HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static TraitorsMain instance;

    public static TraitorsMain getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        File f = new File(String.valueOf(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
                .getDataFolder()));
        if(!f.exists()) {
            f.mkdir();
        }

        f = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
                .getDataFolder() + "/config.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
                FileConfiguration config = YamlConfiguration.loadConfiguration(f);
                config.set("cooldowns.traitortrack-cooldown", 30);
                config.set("cooldowns.traitor-grace-period", 10);
                config.set("cooldowns.traitor-time-required", 1200);
                config.set("cooldowns.login-badlands-delay", 5);
                config.set("login-badlands-tpout", true);
                config.set("tracker-token-cost", 150);
                config.set("default-lives", 1);
                try {
                    config.save(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        f = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
                .getDataFolder() + "/traitors.yml");
        if(!f.exists()) {
            try {
                f.createNewFile();
                getLogger().info("File traitors.yml successfully created!");
            } catch (IOException e) {
                getLogger().info("File traitors.yml failed to create!");
            }
        }

        getCommand("traitor").setExecutor(new TraitorRun());
        getCommand("traitortrack").setExecutor(new TraitorTrack());
        getCommand("traitorcheck").setExecutor(new TraitorCheck());
        getLogger().info("Enabled Traitors - version " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("Disabled Traitors - version " + getDescription().getVersion());
    }

    @EventHandler
    public void traitorAdditionalLivesRespawn(PlayerRespawnEvent event) {
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
                .getDataFolder() + "/traitors.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
        Set<String> traitorList = traitors.getKeys(false);
        if(traitorList.contains(event.getPlayer().getUniqueId().toString())) {
            Player player = event.getPlayer();
            int lives = traitors.getInt(player.getUniqueId().toString() + ".life");
            if(lives > 0) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi warp HuntedSpawn " + player.getName());
            }
        }
    }

    @EventHandler
    public void badlandsJoin(PlayerJoinEvent event) {
        File conf = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
                .getDataFolder() + "/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(conf);
        if(config.getBoolean("login-badlands-tpout") == true) {
            Player player = event.getPlayer();
            File f = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
                    .getDataFolder() + "/traitors.yml");
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
            Set<String> traitorList = traitors.getKeys(false);
            if (player.getWorld().getName().equalsIgnoreCase("world") && !traitorList.contains(player.getUniqueId().toString())) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
                Long lastLogOff = System.currentTimeMillis() - user.getLastLogoff();
                if (lastLogOff >= TimeUnit.MINUTES.toMillis(config.getInt("cooldowns.login-badlands-delay"))) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi warp kingdom " + user.getName());
                }
            }
        }
    }

    @EventHandler
    public void traitorDeath(PlayerDeathEvent event) {
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
                .getDataFolder() + "/traitors.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
        Set<String> traitorList = traitors.getKeys(false);
        if (event.getEntity().getKiller() instanceof Player) {
            if (traitorList != null && !traitorList.isEmpty()) {
                Player player = event.getEntity();
                String tUUID = player.getUniqueId().toString();
                if (traitorList.contains(tUUID)) {
                    int lives = traitors.getInt(tUUID + ".life");
                    int newLives = lives-1;
                    if(newLives < 1) {
                        traitors.set(tUUID, null);
                        try {
                            traitors.save(f);
                            CMIUser killer = CMI.getInstance().getPlayerManager().getUser(event.getEntity().getKiller());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money give " + killer.getName() + " 100000");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + killer.getName() + " permission set deluxetags.tag.loyalist");
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                        + ChatColor.AQUA + killer.getName() + ChatColor.GRAY + " has " + ChatColor.RED + ChatColor.BOLD + "CAUGHT"
                                        + ChatColor.GRAY + " the traitor " + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + " and has collected the "
                                        + ChatColor.GREEN + "100k Bounty and tag" + ChatColor.GRAY + "!");
                            }
                            traitors = YamlConfiguration.loadConfiguration(f);
                            traitorList = traitors.getKeys(false);
                            if(traitorList.isEmpty()) {
                                if(CMI.getInstance().getPortalManager().getByName("bhportal") != null || CMI.getInstance().getPortalManager().getByName("nhportal") != null) {
                                    for (Player online : Bukkit.getOnlinePlayers()) {
                                        online.sendMessage("");
                                        online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                                + ChatColor.AQUA + "The Noble and Bandit Badlands Portals have " + ChatColor.RED + "CLOSED");
                                    }
                                    CMI.getInstance().getPortalManager().getByName("bhportal").setEnabled(false);
                                    CMI.getInstance().getPortalManager().getByName("nhportal").setEnabled(false);
                                } else {
                                    for (Player online : Bukkit.getOnlinePlayers()) {
                                        online.sendMessage(ChatColor.RED + "Looks like ImuRgency has cucked something up with the badlands portals! Report to an admin.");
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        traitors.set(tUUID + ".life", newLives);
                        try {
                            traitors.save(f);
                            CMIUser killer = CMI.getInstance().getPlayerManager().getUser(event.getEntity().getKiller());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money give " + killer.getName() + " 50000");
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                        + ChatColor.AQUA + killer.getName() + ChatColor.GRAY + " nearly caught the traitor " + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + ", but they managed to slip away. "
                                        + ChatColor.AQUA + killer.getName() + ChatColor.GRAY + " receives " + ChatColor.GREEN + "50k" + ChatColor.GRAY + " for nearly capturing the traitor!");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            Player player = event.getEntity();
            String tUUID = player.getUniqueId().toString();
            if (traitorList.contains(tUUID)) {
                int lives = traitors.getInt(tUUID + ".life");
                int newLives = lives-1;
                if(newLives < 1) {
                    traitors.set(tUUID, null);
                    try {
                        traitors.save(f);
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                    + ChatColor.GRAY + " the traitor " + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + " has died and is now back in the kingdom!");
                        }
                        traitors = YamlConfiguration.loadConfiguration(f);
                        traitorList = traitors.getKeys(false);
                        if(traitorList.isEmpty()) {
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.sendMessage("");
                                online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                        + ChatColor.AQUA + "The Noble and Bandit Badlands Portals have " + ChatColor.RED + "CLOSED");
                            }
                            CMI.getInstance().getPortalManager().getByName("bhportal").setEnabled(false);
                            CMI.getInstance().getPortalManager().getByName("nhportal").setEnabled(false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    traitors.set(tUUID + ".life", newLives);
                    try {
                        traitors.save(f);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi warp HuntedSpawn " + player.getName());
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                    + ChatColor.GRAY + " The traitor " + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + " was nearly caught, but they managed to slip away!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private final TraitorTrack TraitorTrackCommand = new TraitorTrack();

    @EventHandler
    public void invClick(InventoryClickEvent event) {
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
                .getDataFolder() + "/traitors.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
        File conf = new File(Bukkit.getServer().getPluginManager().getPlugin("Traitors")
                .getDataFolder() + "/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(conf);
        String[] traitorTitle = ChatColor.stripColor(event.getView().getTitle()).split(" ");
        if(traitorTitle[0].equalsIgnoreCase("Traitors")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                if(event.getCurrentItem().getType() == Material.PAPER) {
                    if(event.getSlot() == 46) {
                        int page = Integer.parseInt(traitorTitle[4])-1;
                        TraitorTrackCommand.openGUI((Player) event.getWhoClicked(), page);
                    } else if(event.getSlot() == 52) {
                        int page = Integer.parseInt(traitorTitle[4])+1;
                        TraitorTrackCommand.openGUI((Player) event.getWhoClicked(), page);
                    }
                } else if(event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                    Player player = (Player) event.getWhoClicked();
                    String iName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                    Player pTraitor = Bukkit.getPlayer(iName);
                    CMIUser traitor = CMI.getInstance().getPlayerManager().getUser(iName);
                    Long pTokens = TokenManagerPlugin.getInstance().getTokens(player).getAsLong();
                    if(pTokens >= config.getInt("tracker-token-cost")) {
                        if(traitor.isOnline()) {
                            long traitorStarted = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - traitors.getLong(traitor.getUniqueId().toString() + ".playtime"));
                            if(traitorStarted >= config.getLong("cooldowns.traitor-grace-period")) {
                                if (!cooldowns.containsKey(player.getUniqueId())) {
                                    int x = (int) traitor.getLocation().getX();
                                    int y = (int) traitor.getLocation().getY();
                                    int z = (int) traitor.getLocation().getZ();
                                    for (Player online : Bukkit.getOnlinePlayers()) {
                                        if (!online.equals(pTraitor)) {
                                            online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                    + ChatColor.GRAY + "A traitors coords has been revealed "
                                                    + ChatColor.YELLOW + "(X: " + x + " Y: " + y + " Z: " + z + ")");
                                        } else {
                                            traitor.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                    + ChatColor.RED + "" + ChatColor.BOLD + "Your location has been revealed!");
                                        }
                                    }
                                    TokenManagerPlugin.getInstance().removeTokens(player, config.getInt("tracker-token-cost"));
                                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                                } else {
                                    long defaultCooldown = config.getLong("cooldowns.traitortrack-cooldown");
                                    long timeLeft = System.currentTimeMillis() - cooldowns.get(player.getUniqueId());
                                    if (TimeUnit.MILLISECONDS.toMinutes(timeLeft) >= defaultCooldown) {
                                        int x = (int) traitor.getLocation().getX();
                                        int y = (int) traitor.getLocation().getY();
                                        int z = (int) traitor.getLocation().getZ();
                                        for (Player online : Bukkit.getOnlinePlayers()) {
                                            if (!online.equals(pTraitor)) {
                                                online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                        + ChatColor.GRAY + "A traitors coords has been revealed "
                                                        + ChatColor.YELLOW + "(X: " + x + " Y: " + y + " Z: " + z + ")");
                                            } else {
                                                traitor.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                        + ChatColor.RED + "" + ChatColor.BOLD + "Your location has been revealed!");
                                            }
                                        }
                                        TokenManagerPlugin.getInstance().removeTokens(player, config.getInt("tracker-token-cost"));
                                        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                                    } else {
                                        long timeRem = defaultCooldown - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                        int hours = (int) timeRem / 3600;
                                        int remainder = (int) timeRem - hours * 3600;
                                        int mins = remainder / 60;
                                        remainder = remainder - mins * 60;
                                        int secs = remainder;
                                        player.sendMessage(ChatColor.RED + "You have to wait " + mins + "m " + secs + "s before you can use this again.");
                                    }
                                }
                            } else {
                                long defaultCooldown = TimeUnit.MINUTES.toSeconds(config.getLong("cooldowns.traitortrack-cooldown"));
                                long timeLeft = System.currentTimeMillis() - traitors.getLong(traitor.getUniqueId().toString() + ".playtime");
                                long timeRem = defaultCooldown - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                int hours = (int) timeRem / 3600;
                                int remainder = (int) timeRem - hours * 3600;
                                int mins = remainder / 60;
                                remainder = remainder - mins * 60;
                                int secs = remainder;
                                player.sendMessage(ChatColor.RED + "You have to wait " + mins + "m " + secs + "s before you can use this.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You can't track " + traitor.getName() + " when he's offline!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have enough tokens!");
                    }
                }
            }
        }
    }
}