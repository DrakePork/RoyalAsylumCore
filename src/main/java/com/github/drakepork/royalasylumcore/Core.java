package com.github.drakepork.royalasylumcore;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.github.drakepork.royalasylumcore.Commands.Traitor.*;
import com.github.drakepork.royalasylumcore.Commands.Chats.*;
import com.github.drakepork.royalasylumcore.Listeners.Discord;
import com.github.drakepork.royalasylumcore.Utils.ConfigCreator;
import com.github.drakepork.royalasylumcore.Utils.LangCreator;
import com.github.drakepork.royalasylumcore.Utils.PluginReceiver;
import com.google.inject.Inject;
import com.google.inject.Injector;
import github.scarsz.discordsrv.DiscordSRV;
import me.realized.tokenmanager.TokenManagerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Core extends JavaPlugin implements Listener {
    public HashMap<UUID, Long> gCooldowns = new HashMap<>();
    public HashMap<UUID, Long> pCooldowns = new HashMap<>();
    public HashMap<UUID, Long> killCooldowns = new HashMap<>();


    @Inject private LangCreator lang;
    @Inject private ConfigCreator ConfigCreator;
    @Inject private TraitorRun TraitorRun;
    @Inject private TraitorTrack TraitorTrack;
    @Inject private TraitorCheck TraitorCheck;

    @Inject private AdminChat AdminChat;
    @Inject private BuildChat BuildChat;
    @Inject private DiscordChat DiscordChat;
    @Inject private GuardChat GuardChat;
    @Inject private HunterChat HunterChat;
    @Inject private ReportChat ReportChat;
    @Inject private RoundTableChat RoundTableChat;
    @Inject private TraitorChat TraitorChat;


    private Discord Discord = new Discord(this);



    @Override
    public void onEnable() {
        DiscordSRV.api.subscribe(Discord);
        PluginReceiver module = new PluginReceiver(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);

        this.ConfigCreator.init();
        this.lang.init();

        Bukkit.getServer().getPluginManager().registerEvents(this, this);


        File traitorFile = new File(this.getDataFolder() + File.separator
                + "traitors.yml");
        if(!traitorFile.exists()) {
            try {
                traitorFile.createNewFile();
                getLogger().info("File traitors.yml successfully created!");
            } catch (IOException e) {
                getLogger().info("File traitors.yml failed to create!");
            }
        }

        getCommand("traitor").setExecutor(this.TraitorRun);
        getCommand("traitortrack").setExecutor(this.TraitorTrack);
        getCommand("traitorcheck").setExecutor(this.TraitorCheck);

        getCommand("a").setExecutor(this.AdminChat);
        getCommand("b").setExecutor(this.BuildChat);
        getCommand("d").setExecutor(this.DiscordChat);
        getCommand("k").setExecutor(this.GuardChat);
        getCommand("report").setExecutor(this.ReportChat);
        getCommand("rtc").setExecutor(this.RoundTableChat);
        getCommand("tc").setExecutor(this.TraitorChat);
        getCommand("hunt").setExecutor(this.HunterChat);

        getLogger().info("Enabled RoyalAsylumCore - v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled RoyalAsylumCore - v" + getDescription().getVersion());
        DiscordSRV.api.unsubscribe(Discord);
    }


    public String translateHexColorCodes(String message) {
        final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "\\}");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    @EventHandler
    public void commands(PlayerCommandPreprocessEvent event) {
        switch(event.getMessage().toLowerCase()) {
            case "/pl":
            case "/plugins":
            case "/ver":
            case "/version":
            case "/about":
            case "/icanhasbukkit":
            case "/bukkit:?":
            case "/bukkit:pl":
            case "/bukkit:plugins":
            case "/bukkit:about":
            case "/bukkit:help":
            case "/bukkit:ver":
            case "/bukkit:version":
            case "/minecraft:help":
            case "/minecraft:me":
            case "/?":
            case "/help":
                if(!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.WHITE + "Unknown command. Type " + '"' + "/help" + '"' + " for help.");
                }
                break;
        }
    }

    @EventHandler
    public void villagerTrade(InventoryOpenEvent event) {
        if (event.getInventory().getType() != InventoryType.MERCHANT) {
        } else if (event.getInventory().getType().equals(InventoryType.MERCHANT)) {
            Player player = (Player) event.getPlayer();
            player.sendMessage(ChatColor.RED + "Villager trading has been disabled");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void islarroCraft(CraftItemEvent event) {
        if(event.getWhoClicked() instanceof HumanEntity) {
            Player player = (Player) event.getWhoClicked();
            if(player.getUniqueId().toString().equalsIgnoreCase("2b8cf1ca-1c7f-4529-a5a7-62433fe16460")) {
                if(event.getResult().equals(Material.CAKE)) {
                    event.setCancelled(true);
                    ItemStack cake = new ItemStack(Material.CAKE, 1);
                    ItemMeta cakeMeta = cake.getItemMeta();
                    cakeMeta.setDisplayName(ChatColor.WHITE + "Bread Cake");
                    player.getInventory().addItem(cake);
                } else if(event.getResult().equals(Material.BREAD)) {
                    event.setCancelled(true);
                    ItemStack bread = new ItemStack(Material.BREAD, 1);
                    ItemMeta breadMeta = bread.getItemMeta();
                    breadMeta.setDisplayName(ChatColor.WHITE + "Breadest Bread");
                    player.getInventory().addItem(bread);
                }
            }
        }
    }

    @EventHandler
    public void traitorAdditionalLivesRespawn(PlayerRespawnEvent event) {
        File f = new File(this.getDataFolder() + File.separator
                + "traitors.yml");
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
    public void traitorDrinkEvent(PlayerItemConsumeEvent event) {
        if(event.getItem().equals(Material.MILK_BUCKET)) {
            File f = new File(this.getDataFolder() + File.separator
                    + "traitors.yml");
            FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
            Set<String> traitorList = traitors.getKeys(false);
            Player player = event.getPlayer();
            if (traitorList.contains(player.getUniqueId().toString())) {
                player.sendMessage(ChatColor.RED + "You can't drink milk as a traitor!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void badlandsJoin(PlayerJoinEvent event) {
        FileConfiguration config = this.getConfig();
        Player player = event.getPlayer();
        if(config.getBoolean("login-badlands-tpout") == true) {
            File f = new File(this.getDataFolder() + File.separator
                    + "traitors.yml");
            FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
            Set<String> traitorList = traitors.getKeys(false);
            if (player.getWorld().getName().equalsIgnoreCase("world") && !traitorList.contains(player.getUniqueId().toString())) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
                Long lastLogOff = System.currentTimeMillis() - user.getLastLogoff();
                if (lastLogOff >= TimeUnit.MINUTES.toMillis(config.getInt("cooldowns.login-badlands-delay"))) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi warp spawn " + user.getName());
                }
            }
        }
    }

    @EventHandler
    public void traitorDeath(PlayerDeathEvent event) {
        File f = new File(this.getDataFolder() + File.separator
                + "traitors.yml");
        FileConfiguration config = this.getConfig();
        FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
        Set<String> traitorList = traitors.getKeys(false);
        if (event.getEntity().getKiller() instanceof Player) {
            ArrayList stabList = (ArrayList) config.getStringList("token-group-kill-rewards");
            if(!stabList.isEmpty()) {
                Player killer = event.getEntity().getKiller();
                Player killed = event.getEntity();
                CMIUser userK = CMI.getInstance().getPlayerManager().getUser(killer);
                CMIUser userD = CMI.getInstance().getPlayerManager().getUser(killed);
                if(!userD.getLastIp().equalsIgnoreCase(userK.getLastIp())) {
                    if (!killCooldowns.containsKey(killer.getUniqueId())) {
                        ArrayList tokens = new ArrayList();
                        for (Object stabStuff : stabList) {
                            String stabs = stabStuff.toString();
                            String[] wham = stabs.split(":");
                            if (killed.hasPermission(wham[0])) {
                                tokens.add(Integer.valueOf(wham[1]));
                                killCooldowns.put(killer.getUniqueId(), System.currentTimeMillis());
                            }
                        }
                        if(!tokens.isEmpty() && tokens != null) {
                            int tokenReward = (int) Collections.max(tokens);
                            TokenManagerPlugin.getInstance().addTokens(killer, tokenReward);
                            killer.sendMessage(ChatColor.DARK_PURPLE + "Tokens" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + tokenReward + " tokens "
                                    + ChatColor.GRAY + "has been added to your balance.");
                        }
                    } else {
                        long defaultCooldown = config.getLong("token-kill-delay");
                        long timeLeft = System.currentTimeMillis() - killCooldowns.get(killer.getUniqueId());
                        if (TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= defaultCooldown) {
                            ArrayList tokens = new ArrayList();
                            for (Object stabStuff : stabList) {
                                String stabs = stabStuff.toString();
                                String[] wham = stabs.split(":");
                                if (killed.hasPermission(wham[0])) {
                                    tokens.add(Integer.valueOf(wham[1]));
                                    killCooldowns.put(killer.getUniqueId(), System.currentTimeMillis());
                                }
                            }
                            if(!tokens.isEmpty() && tokens != null) {
                                int tokenReward = (int) Collections.max(tokens);
                                TokenManagerPlugin.getInstance().addTokens(killer, tokenReward);
                                killer.sendMessage(ChatColor.DARK_PURPLE + "Tokens" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + tokenReward + " tokens "
                                        + ChatColor.GRAY + "has been added to your balance.");
                            }
                        }
                    }
                }
            }
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
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money give " + killer.getName() + " " + config.getInt("hunter-kill-prize.money"));
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + killer.getName() + " permission set deluxetags.tag.loyalist");
                            TokenManagerPlugin.getInstance().addTokens(event.getEntity().getKiller(), config.getInt("hunter-kill-prize.tokens"));
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                        + ChatColor.AQUA + killer.getName() + ChatColor.GRAY + " has " + ChatColor.RED + ChatColor.BOLD + "CAUGHT"
                                        + ChatColor.GRAY + " the traitor " + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + " and has collected the "
                                        + ChatColor.GREEN + config.getInt("hunter-kill-prize-money") + " Bounty and tag" + ChatColor.GRAY + "!");
                            }
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi effect " + player.getName() + " clear");
                            traitors = YamlConfiguration.loadConfiguration(f);
                            traitorList = traitors.getKeys(false);
                            if(traitorList.isEmpty()) {
                                if(CMI.getInstance().getPortalManager().getByName("BHPortal") != null || CMI.getInstance().getPortalManager().getByName("NHPortal") != null) {
                                    for (Player online : Bukkit.getOnlinePlayers()) {
                                        online.sendMessage("");
                                        online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                                + ChatColor.AQUA + "The Noble and Bandit Badlands Portals have " + ChatColor.RED + "CLOSED");
                                    }
                                    CMI.getInstance().getPortalManager().getByName("BHPortal").setEnabled(false);
                                    CMI.getInstance().getPortalManager().getByName("NHPortal").setEnabled(false);
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
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money give " + killer.getName() +  " " + config.getInt("hunter-kill-prize.money-extra-life"));
                            TokenManagerPlugin.getInstance().addTokens(event.getEntity().getKiller(), config.getInt("hunter-kill-prize.tokens-extra-life"));
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                        + ChatColor.AQUA + killer.getName() + ChatColor.GRAY + " nearly caught the traitor " + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + ", but they managed to slip away. "
                                        + ChatColor.AQUA + killer.getName() + ChatColor.GRAY + " receives " + ChatColor.GREEN + config.getInt("hunter-kill-prize-money-extra-life") + ChatColor.GRAY + " for nearly capturing the traitor!");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if(traitorList.contains(event.getEntity().getKiller().getUniqueId().toString())) {
                    Player traitor = event.getEntity().getKiller();
                    TokenManagerPlugin.getInstance().addTokens(traitor, config.getInt("traitor-kill-tokens"));
                }
            }
        } else {
            if (traitorList != null && !traitorList.isEmpty()) {
                Player player = event.getEntity();
                String tUUID = player.getUniqueId().toString();
                if (traitorList.contains(tUUID)) {
                    int lives = traitors.getInt(tUUID + ".life");
                    int newLives = lives - 1;
                    if (newLives < 1) {
                        traitors.set(tUUID, null);
                        try {
                            traitors.save(f);
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                        + ChatColor.GRAY + "The traitor " + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + " has died and is now back in the kingdom!");
                            }
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi effect " + player.getName() + " clear");
                            traitors = YamlConfiguration.loadConfiguration(f);
                            traitorList = traitors.getKeys(false);
                            if (traitorList.isEmpty()) {
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    online.sendMessage("");
                                    online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Royal Decree" + ChatColor.DARK_GRAY + "] "
                                            + ChatColor.AQUA + "The Noble and Bandit Badlands Portals have " + ChatColor.RED + "CLOSED");
                                }
                                CMI.getInstance().getPortalManager().getByName("BHPortal").setEnabled(false);
                                CMI.getInstance().getPortalManager().getByName("NHPortal").setEnabled(false);
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
                                        + ChatColor.GRAY + "The traitor " + ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + " was nearly caught, but they managed to slip away!");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }



    @EventHandler
    public void invClick(InventoryClickEvent event) {
        File f = new File(this.getDataFolder() + File.separator
                + "traitors.yml");
        FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
        FileConfiguration config = this.getConfig();
        String[] traitorTitle = ChatColor.stripColor(event.getView().getTitle()).split(" ");
        if(traitorTitle[0].equalsIgnoreCase("Traitors")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                if(event.getCurrentItem().getType() == Material.PAPER) {
                    if(event.getSlot() == 46) {
                        int page = Integer.parseInt(traitorTitle[4])-1;
                        this.TraitorTrack.openGUI(player, page);
                    } else if(event.getSlot() == 52) {
                        int page = Integer.parseInt(traitorTitle[4])+1;
                        this.TraitorTrack.openGUI(player, page);
                    }
                } else if(event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                    String iName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                    Player pTraitor = Bukkit.getPlayer(iName);
                    CMIUser traitor = CMI.getInstance().getPlayerManager().getUser(iName);
                    if(traitor.isOnline()) {
                        long traitorStarted = System.currentTimeMillis() - traitors.getLong(traitor.getUniqueId().toString() + ".startdate");
                        if(TimeUnit.MILLISECONDS.toMinutes(traitorStarted) >= config.getLong("cooldowns.traitor-grace-period")) {
                            this.TraitorTrack.chooseType(player, pTraitor);
                        } else {
                            long defaultCooldown = TimeUnit.MINUTES.toSeconds(config.getLong("cooldowns.traitor-grace-period"));
                            long timeLeft = System.currentTimeMillis() - traitors.getLong(traitor.getUniqueId().toString() + ".startdate");
                            long timeRem = defaultCooldown - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                            int hours = (int) timeRem / 3600;
                            int remainder = (int) timeRem - hours * 3600;
                            int mins = remainder / 60;
                            remainder = remainder - mins * 60;
                            int secs = remainder;
                            player.sendMessage(ChatColor.RED + "You have to wait " + mins + "m " + secs + "s before you can use this.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You can't track " + traitor.getName() + " when they're offline!");
                    }
                }
            }
        } else if(traitorTitle[0].equalsIgnoreCase("Tracking:")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                Player traitor = Bukkit.getPlayer(ChatColor.stripColor(traitorTitle[1]));
                if(event.getSlot() == 11) {
                    if(event.getClick().isLeftClick() == true) {
                        CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
                        Double pMoney = user.getBalance();
                        if(pMoney >= config.getDouble("track-cost.global-money-cost")) {
                            if (!gCooldowns.containsKey(player.getUniqueId())) {
                                int x = (int) traitor.getLocation().getX();
                                int y = (int) traitor.getLocation().getY();
                                int z = (int) traitor.getLocation().getZ();
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    if (!online.equals(traitor)) {
                                        online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                + ChatColor.GRAY + "A traitors coords has been revealed "
                                                + ChatColor.YELLOW + "(X: " + x + " Y: " + y + " Z: " + z + ")");
                                    } else {
                                        traitor.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                + ChatColor.RED + "" + ChatColor.BOLD + "Your location has been revealed!");
                                    }
                                }
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + config.getInt("track-cost.global-money-cost"));
                                gCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                                player.closeInventory();
                            } else {
                                long defaultCooldown = config.getLong("cooldowns.traitortrack-global-cooldown");
                                long timeLeft = System.currentTimeMillis() - gCooldowns.get(player.getUniqueId());
                                if (TimeUnit.MILLISECONDS.toMinutes(timeLeft) >= defaultCooldown) {
                                    int x = (int) traitor.getLocation().getX();
                                    int y = (int) traitor.getLocation().getY();
                                    int z = (int) traitor.getLocation().getZ();
                                    for (Player online : Bukkit.getOnlinePlayers()) {
                                        if (!online.equals(traitor)) {
                                            online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                    + ChatColor.GRAY + "A traitors coords has been revealed "
                                                    + ChatColor.YELLOW + "(X: " + x + " Y: " + y + " Z: " + z + ")");
                                        } else {
                                            traitor.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                    + ChatColor.RED + "" + ChatColor.BOLD + "Your location has been revealed!");
                                        }
                                    }
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + config.getInt("track-cost.global-money-cost"));
                                    gCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                                    player.closeInventory();
                                } else {
                                    long timeRem = TimeUnit.MINUTES.toSeconds(defaultCooldown) - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                    int hours = (int) timeRem / 3600;
                                    int remainder = (int) timeRem - hours * 3600;
                                    int mins = remainder / 60;
                                    remainder = remainder - mins * 60;
                                    int secs = remainder;
                                    player.sendMessage(ChatColor.RED + "You have to wait " + mins + "m " + secs + "s before you can use this again.");
                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have enough money!");
                        }
                    } else if(event.getClick().isRightClick() == true) {
                        Long pTokens = TokenManagerPlugin.getInstance().getTokens(player).getAsLong();
                        if(pTokens >= config.getInt("track-cost.global-token-cost")) {
                            if (!gCooldowns.containsKey(player.getUniqueId())) {
                                int x = (int) traitor.getLocation().getX();
                                int y = (int) traitor.getLocation().getY();
                                int z = (int) traitor.getLocation().getZ();
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    if (!online.equals(traitor)) {
                                        online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                + ChatColor.GRAY + "A traitors coords has been revealed "
                                                + ChatColor.YELLOW + "(X: " + x + " Y: " + y + " Z: " + z + ")");
                                    } else {
                                        traitor.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                + ChatColor.RED + "" + ChatColor.BOLD + "Your location has been revealed!");
                                    }
                                }
                                TokenManagerPlugin.getInstance().removeTokens(player, config.getInt("track-cost.global-token-cost"));
                                player.sendMessage(ChatColor.DARK_PURPLE + "Tokens" + ChatColor.DARK_GRAY + " » "
                                        + ChatColor.AQUA + config.getInt("track-cost.global-token-cost") + " tokens "
                                        + ChatColor.GRAY + "has been removed from your balance.");
                                gCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                                player.closeInventory();
                            } else {
                                long defaultCooldown = config.getLong("cooldowns.traitortrack-global-cooldown");
                                long timeLeft = System.currentTimeMillis() - gCooldowns.get(player.getUniqueId());
                                if (TimeUnit.MILLISECONDS.toMinutes(timeLeft) >= defaultCooldown) {
                                    int x = (int) traitor.getLocation().getX();
                                    int y = (int) traitor.getLocation().getY();
                                    int z = (int) traitor.getLocation().getZ();
                                    for (Player online : Bukkit.getOnlinePlayers()) {
                                        if (!online.equals(traitor)) {
                                            online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                    + ChatColor.GRAY + "A traitors coords has been revealed "
                                                    + ChatColor.YELLOW + "(X: " + x + " Y: " + y + " Z: " + z + ")");
                                        } else {
                                            traitor.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                                    + ChatColor.RED + "" + ChatColor.BOLD + "Your location has been revealed!");
                                        }
                                    }
                                    TokenManagerPlugin.getInstance().removeTokens(player, config.getInt("track-cost.global-token-cost"));
                                    player.sendMessage(ChatColor.DARK_PURPLE + "Tokens" + ChatColor.DARK_GRAY + " » "
                                            + ChatColor.AQUA + config.getInt("track-cost.global-token-cost") + " tokens "
                                            + ChatColor.GRAY + "has been removed from your balance.");
                                    gCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                                    player.closeInventory();
                                } else {
                                    long timeRem = TimeUnit.MINUTES.toSeconds(defaultCooldown) - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                    int hours = (int) timeRem / 3600;
                                    int remainder = (int) timeRem - hours * 3600;
                                    int mins = remainder / 60;
                                    remainder = remainder - mins * 60;
                                    int secs = remainder;
                                    player.sendMessage(ChatColor.RED + "You have to wait " + mins + "m " + secs + "s before you can use this again.");
                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have enough tokens!");
                        }
                    }
                } else if(event.getSlot() == 15) {
                    Long pTokens = TokenManagerPlugin.getInstance().getTokens(player).getAsLong();
                    if(pTokens >= config.getInt("track-cost.personal-token-cost")) {
                        if (!pCooldowns.containsKey(player.getUniqueId())) {
                            int x = (int) traitor.getLocation().getX();
                            int y = (int) traitor.getLocation().getY();
                            int z = (int) traitor.getLocation().getZ();
                            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                    + ChatColor.GRAY + "The coords to " + ChatColor.RED + traitor.getName() + ChatColor.GRAY + " is "
                                    + ChatColor.YELLOW + "(X: " + x + " Y: " + y + " Z: " + z + ")");
                            traitor.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                    + ChatColor.RED + "" + ChatColor.BOLD + "Your location has been revealed!");
                            TokenManagerPlugin.getInstance().removeTokens(player, config.getInt("track-cost.personal-token-cost"));
                            player.sendMessage(ChatColor.DARK_PURPLE + "Tokens" + ChatColor.DARK_GRAY + " » "
                                    + ChatColor.AQUA + config.getInt("track-cost.personal-token-cost") + " tokens "
                                    + ChatColor.GRAY + "has been removed from your balance.");
                            pCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                            player.closeInventory();
                        } else {
                            long defaultCooldown = config.getLong("cooldowns.traitortrack-personal-cooldown");
                            long timeLeft = System.currentTimeMillis() - pCooldowns.get(player.getUniqueId());
                            if (TimeUnit.MILLISECONDS.toMinutes(timeLeft) >= defaultCooldown) {
                                int x = (int) traitor.getLocation().getX();
                                int y = (int) traitor.getLocation().getY();
                                int z = (int) traitor.getLocation().getZ();
                                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                        + ChatColor.GRAY + "The coords to " + ChatColor.RED + traitor.getName() + ChatColor.GRAY + " is "
                                        + ChatColor.YELLOW + "(X: " + x + " Y: " + y + " Z: " + z + ")");
                                traitor.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "TraitorTracker" + ChatColor.DARK_GRAY + "] "
                                        + ChatColor.RED + "" + ChatColor.BOLD + "Your location has been revealed!");
                                TokenManagerPlugin.getInstance().removeTokens(player, config.getInt("track-cost.personal-token-cost"));
                                player.sendMessage(ChatColor.DARK_PURPLE + "Tokens" + ChatColor.DARK_GRAY + " » "
                                        + ChatColor.AQUA + config.getInt("track-cost.personal-token-cost") + " tokens "
                                        + ChatColor.GRAY + "has been removed from your balance.");
                                pCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                                player.closeInventory();
                            } else {
                                long timeRem = TimeUnit.MINUTES.toSeconds(defaultCooldown) - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                int hours = (int) timeRem / 3600;
                                int remainder = (int) timeRem - hours * 3600;
                                int mins = remainder / 60;
                                remainder = remainder - mins * 60;
                                int secs = remainder;
                                player.sendMessage(ChatColor.RED + "You have to wait " + mins + "m " + secs + "s before you can use this again.");
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have enough tokens!");
                    }
                }
            }
        }
    }
}