package com.github.drakepork.royalasylumcore;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Modules.Economy.Economy;
import com.bencodez.votingplugin.user.UserManager;
import com.bencodez.votingplugin.user.VotingPluginUser;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.github.drakepork.royalasylumcore.Commands.EconomyCheck;
import com.github.drakepork.royalasylumcore.Commands.SecretFound;
import com.github.drakepork.royalasylumcore.Commands.SecretsGUI;
import com.github.drakepork.royalasylumcore.Commands.ShopGUI;
import com.github.drakepork.royalasylumcore.Commands.Traitor.*;
import com.github.drakepork.royalasylumcore.Commands.Chats.*;
import com.github.drakepork.royalasylumcore.Listeners.Discord;
import com.github.drakepork.royalasylumcore.Utils.*;
import com.google.inject.Inject;
import com.google.inject.Injector;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import me.realized.tokenmanager.TokenManagerPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Core extends JavaPlugin implements Listener {
    public HashMap<UUID, Long> gCooldowns = new HashMap<>();
    public HashMap<UUID, Long> pCooldowns = new HashMap<>();
    public HashMap<UUID, Long> killCooldowns = new HashMap<>();

    public HashMap<UUID, String> stickyChatEnabled = new HashMap<>();

    public HashMap<UUID, Integer> blocksChopped = new HashMap<>();

    public HashMap<String, String> hexColour = new HashMap<>();

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
    @Inject private SecretChat SecretChat;

    @Inject private ShopGUI ShopGUI;
    @Inject private SecretsGUI SecretsGUI;
    @Inject private SecretFound SecretFound;
    @Inject private EconomyCheck EconomyCheck;


    private Discord Discord = new Discord(this);

    public Integer getBlocksChopped(Player player) {
        Integer amount = 1;
        if(blocksChopped.containsKey(player.getUniqueId())) {
            amount = blocksChopped.get(player.getUniqueId());
        }
        blocksChopped.remove(player.getUniqueId());
        return amount;
    }

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

        File secretFile = new File(this.getDataFolder() + File.separator
                + "secrets.yml");
        if(!secretFile.exists()) {
            try {
                secretFile.createNewFile();
                getLogger().info("File secrets.yml successfully created!");
            } catch (IOException e) {
                getLogger().info("File secrets.yml failed to create!");
            }
        }

        File secretsDataFile = new File(this.getDataFolder() + File.separator
                + "secretsdata.yml");
        if(!secretsDataFile.exists()) {
            try {
                secretsDataFile.createNewFile();
                getLogger().info("File secrets.yml successfully created!");
            } catch (IOException e) {
                getLogger().info("File secrets.yml failed to create!");
            }
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Placeholders registered");
            new RoyalPlaceholderExpansion(this).register();
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
        getCommand("s").setExecutor(this.SecretChat);

        getCommand("royalshop").setExecutor(this.ShopGUI);
        getCommand("secrets").setExecutor(this.SecretsGUI);
        getCommand("secretfound").setExecutor(this.SecretFound);
        getCommand("econcheck").setExecutor(this.EconomyCheck);

        getLogger().info("Enabled RoyalAsylumCore v" + getDescription().getVersion());


        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.CHAT) {
                    PacketContainer packet = event.getPacket();
                    if(packet.getChatComponents().read(0).toString() != null && !packet.getChatComponents().read(0).toString().isEmpty()) {
                        if (packet.getChatComponents().read(0).toString() instanceof String) {
                            String chatMsg = packet.getChatComponents().read(0).toString();
                            if (chatMsg.contains("Next ranks:")) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        });

        String line = "";
        String splitBy = ",";
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.getDataFolder() + File.separator
                    + "colors.csv"));
            while ((line = br.readLine()) != null) {
                String[] colors = line.split(splitBy);
                hexColour.put(colors[0].toLowerCase().replaceAll(" ", ""), colors[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled RoyalAsylumCore v" + getDescription().getVersion());
        DiscordSRV.api.unsubscribe(Discord);
    }

    public String colourMessage(String message){
        message = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        return message;
    }


    public String removeColour(String message){
        message = removeColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        return message;
    }

    public void tellConsole(String message){
        Bukkit.getConsoleSender().sendMessage(message);
    }


    public String translateHexColorCodes(String message) {
        if(StringUtils.substringsBetween(message, "{#", "}") != null) {
            String[] hexNames = StringUtils.substringsBetween(message, "{#", "}");
            for (String hexName : hexNames) {
                if (hexColour.get(hexName.toLowerCase()) != null) {
                    message = message.replaceAll(hexName, hexColour.get(hexName.toLowerCase()).substring(1));
                }
            }
        }
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


    public String removeColorCodes(String message) {
        if(StringUtils.substringsBetween(message, "{#", "}") != null) {
            String[] hexNames = StringUtils.substringsBetween(message, "{#", "}");
            for (String hexName : hexNames) {
                if (hexColour.get(hexName.toLowerCase()) != null) {
                    message = message.replaceAll("\\{#" + hexName + "\\}", "");
                }
            }
        }
        final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "\\}");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, "");
        }
        return matcher.appendTail(buffer).toString();
    }

    @EventHandler
    public void stickyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(stickyChatEnabled.containsKey(player.getUniqueId())) {
            File lang = new File(this.getDataFolder() + File.separator
                    + "lang" + File.separator + this.getConfig().getString("lang-file"));
            FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
            event.setCancelled(true);
            String stickiedChat = stickyChatEnabled.get(player.getUniqueId());
            String[] split = stickiedChat.split("-");

            String message = event.getMessage();
            String format = langConf.getString("chat." + split[0] + ".format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
            message = format.replaceAll("\\[message\\]", Matcher.quoteReplacement(message));
            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                if (online.hasPermission("royalasylum.chat." + split[0])) {
                    online.sendMessage(translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));
                }
            }

            Bukkit.getConsoleSender().sendMessage(translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));

            String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
            String dMessage = dFormat.replaceAll("\\[message\\]", Matcher.quoteReplacement(event.getMessage()));
            TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(split[0] + "-chat");
            channel.sendMessage(dMessage).queue();
        }
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
    public void birchLeavesAppleDrop(LeavesDecayEvent event) {
        if(event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("prisonbuild")) {
            if(event.getBlock().getType() == Material.BIRCH_LEAVES) {
                if (Math.random() < 0.025) {
                    ItemStack apple = new ItemStack(Material.APPLE, 1);
                    event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), apple);
                }
            }
        }
    }

    @EventHandler
    public void birchBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        if(!event.isCancelled()) {
            if (b.getType() == Material.BIRCH_LOG && loc.getWorld().getName().equalsIgnoreCase("prisonbuild")) {
                ArrayList axes = new ArrayList();
                axes.add(Material.DIAMOND_AXE);
                axes.add(Material.GOLDEN_AXE);
                axes.add(Material.IRON_AXE);
                axes.add(Material.STONE_AXE);
                axes.add(Material.WOODEN_AXE);
                axes.add(Material.NETHERITE_AXE);
                if(axes.contains(event.getPlayer().getInventory().getItemInMainHand().getType())) {
                    if (!event.getPlayer().isSneaking()) {
                        Boolean birchDown = true;
                        int birchDrops = 0;
                        Location birchLoc;
                        Location saplingLoc;
                        int i = 0;
                        while (birchDown) {
                            birchLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - i, loc.getBlockZ());
                            if (birchLoc.getBlock().getType() == Material.BIRCH_LOG) {
                                birchLoc.getBlock().breakNaturally();
                                birchDrops++;
                                i++;
                            } else {
                                saplingLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - i + 1, loc.getBlockZ());
                                Location finalSaplingLoc = saplingLoc;
                                if (birchLoc.getBlock().getType() == Material.GRASS_BLOCK || birchLoc.getBlock().getType() == Material.DIRT) {
                                    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                        public void run() {
                                            finalSaplingLoc.getBlock().setType(Material.BIRCH_SAPLING);
                                        }
                                    }, 2L);
                                }
                                birchDown = false;
                            }
                        }
                        Boolean birchUp = true;
                        int x = 1;
                        while (birchUp) {
                            birchLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + x, loc.getBlockZ());
                            if (birchLoc.getBlock().getType() == Material.BIRCH_LOG) {
                                birchLoc.getBlock().breakNaturally();
                                birchDrops++;
                                x++;
                            } else {
                                birchUp = false;
                            }
                        }

                        blocksChopped.put(event.getPlayer().getUniqueId(), birchDrops);

                        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                        Damageable im = (Damageable) item.getItemMeta();
                        Material axe = item.getType();
                        int dmg = im.getDamage();
                        if (item.containsEnchantment(Enchantment.DURABILITY)) {
                            int enchantLevel = item.getEnchantmentLevel(Enchantment.DURABILITY);
                            if (birchDrops / enchantLevel + dmg > axe.getMaxDurability()) {
                                event.getPlayer().getInventory().remove(item);
                            } else {
                                im.setDamage(birchDrops / enchantLevel + dmg);
                                item.setItemMeta((ItemMeta) im);
                            }
                        } else {
                            if (birchDrops + dmg > axe.getMaxDurability()) {
                                event.getPlayer().getInventory().remove(item);
                            } else {
                                im.setDamage(birchDrops + dmg);
                                item.setItemMeta((ItemMeta) im);
                            }
                        }
                    } else {
                        Location newLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                        if (newLoc.getBlock().getType() == Material.GRASS_BLOCK || newLoc.getBlock().getType() == Material.DIRT) {
                            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                public void run() {
                                    loc.getBlock().setType(Material.BIRCH_SAPLING);
                                }
                            }, 2L);
                        }
                    }
                } else {
                    Location newLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                    if (newLoc.getBlock().getType() == Material.GRASS_BLOCK || newLoc.getBlock().getType() == Material.DIRT) {
                        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            public void run() {
                                loc.getBlock().setType(Material.BIRCH_SAPLING);
                            }
                        }, 2L);
                    }
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
            if (player.getWorld().getName().equalsIgnoreCase("traitor") && !traitorList.contains(player.getUniqueId().toString())) {
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
    public void clickGUI(InventoryClickEvent event) {
        File f = new File(this.getDataFolder() + File.separator
                + "traitors.yml");
        FileConfiguration traitors = YamlConfiguration.loadConfiguration(f);
        FileConfiguration config = this.getConfig();
        String[] pageCheck = ChatColor.stripColor(event.getView().getTitle()).split(" ");
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
        } else if (ChatColor.stripColor(event.getView().getTitle()).contains("Secrets")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
            }
            String[] title = event.getView().getTitle().split(" - ");
            HumanEntity human = event.getWhoClicked();
            if (human instanceof Player) {
                switch (title[1].toLowerCase()) {
                    case "peasant":
                    case "farmer":
                    case "craftsman":
                    case "vassal":
                    case "noble":
                    case "baron":
                    case "viscount":
                    case "earl":
                    case "other":
                        if (event.getSlot() == 40) {
                            SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "secrets");
                        }
                        break;
                    case "all":
                        switch (event.getSlot()) {
                            case 31:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "main-menu");
                                break;
                            case 11:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "peasant");
                                break;
                            case 12:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "farmer");
                                break;
                            case 13:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "craftsman");
                                break;
                            case 14:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "vassal");
                                break;
                            case 15:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "noble");
                                break;
                            case 20:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "baron");
                                break;
                            case 21:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "viscount");
                                break;
                            case 22:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "earl");
                                break;
                            case 23:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "other");
                                break;
                        }
                        break;
                    case "rewards":
                        if (event.getCurrentItem() == null) {
                            break;
                        }
                        if (event.getSlot() == 49) {
                            SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "main-menu");
                        } else if (event.getCurrentItem().getType().equals(Material.CHEST_MINECART)) {
                            Player player = Bukkit.getPlayer(human.getName());
                            File rewardsDataFile = new File(this.getDataFolder() + File.separator
                                    + "rewardsdata.yml");
                            YamlConfiguration rData = YamlConfiguration.loadConfiguration(rewardsDataFile);
                            File secretsDataFile = new File(this.getDataFolder() + File.separator
                                    + "secretsdata.yml");
                            YamlConfiguration pData = YamlConfiguration.loadConfiguration(secretsDataFile);
                            ItemStack currItem = event.getCurrentItem();
                            NamespacedKey key = new NamespacedKey(this, "reward");
                            ItemMeta itemMeta = currItem.getItemMeta();
                            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                            String foundValue;
                            if(container.has(key, PersistentDataType.STRING)) {
                                foundValue = container.get(key, PersistentDataType.STRING);
                                if(rData.getString(foundValue + ".reward-type").equalsIgnoreCase("points")) {
                                    int pointAmount = rData.getInt(foundValue + ".reward");
                                    VotingPluginUser user = UserManager.getInstance().getVotingPluginUser(player);
                                    Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                                        public void run() {
                                            user.addPoints(pointAmount);
                                        }});
                                    pData.set(player.getUniqueId().toString() + ".rewards." + foundValue + ".collected", true);
                                    try {
                                        pData.save(secretsDataFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    player.sendMessage(colourMessage("&f[&eSecrets&f] &aYou received " + pointAmount + " points!"));
                                    SecretsGUI.openGUI(player, "rewards");
                                } else if(rData.getString(foundValue + ".reward-type").equalsIgnoreCase("tokens")) {

                                }
                            }
                        }
                        break;
                    case "main":
                        switch(event.getSlot()) {
                            case 13:
                                break;
                            case 20:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "secrets");
                                break;
                            case 24:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "rewards");
                                break;
                        }
                }
            }
        } else if (pageCheck[0].equalsIgnoreCase("Shop") && pageCheck[1].equalsIgnoreCase("Log")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                if(event.getCurrentItem().getType() == Material.PAPER) {
                    if(event.getSlot() == 46) {
                        int page = Integer.parseInt(pageCheck[4])-1;
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "default");
                    } else if(event.getSlot() == 52) {
                        int page = Integer.parseInt(pageCheck[4])+1;
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "default");
                    }
                } else if(event.getCurrentItem().getType() == Material.BOOK) {
                    if(event.getSlot() == 47) {
                        int page = Integer.parseInt(pageCheck[4]);
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "amounttop");
                    } else if(event.getSlot() == 48) {
                        int page = Integer.parseInt(pageCheck[4]);
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "amountbottom");
                    } else if(event.getSlot() == 49) {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(ChatColor.RED + "/econcheck player <player>");
                    } else if(event.getSlot() == 50) {
                        int page = Integer.parseInt(pageCheck[4]);
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "moneybottom");
                    } else if(event.getSlot() == 51) {
                        int page = Integer.parseInt(pageCheck[4]);
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "moneytop");
                    }
                }
            }
        }
    }
}