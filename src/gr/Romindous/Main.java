package gr.Romindous;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import gr.Romindous.game.Arena;
import gr.Romindous.game.map.Setup;
import gr.Romindous.game.map.WXYZ;
import gr.Romindous.game.object.Build;
import gr.Romindous.game.object.PlRusher;
import gr.Romindous.game.object.Rusher;
import gr.Romindous.listener.DmgLst;
import gr.Romindous.listener.InterLst;
import gr.Romindous.listener.InventLst;
import gr.Romindous.listener.MainLst;
import gr.Romindous.type.BuildType;
import gr.Romindous.util.TitleUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.EnumChatFormat;
import net.minecraft.server.dedicated.DedicatedServer;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.world.WorldManager.Generator;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;

public class Main extends JavaPlugin {

	public static Main plug;
	public static WXYZ lobby;
	public static DedicatedServer ds;
	public static ScoreboardManager smg;

	public static final SecureRandom srnd = new SecureRandom();
	public static final HashMap<String, PlRusher> rushs = new HashMap<>();
	public static final HashMap<String, Arena> active = new HashMap<>();
	public static final HashMap<String, Setup> nonactive = new HashMap<>();
	
	public static final ItemStack air = new ItemStack(Material.AIR);
	
	public static final ItemStack join = new ItemBuilder(Material.CAMPFIRE).name("§6Выбор Карты").build();
	public static final ItemStack team = new ItemBuilder(Material.CONDUIT).name("§eВыбор Цвета").build();
	public static final ItemStack glow = new ItemBuilder(Material.GLOWSTONE_DUST).name("§eПодсветка Комманд").build();
	public static final ItemStack leave = new ItemBuilder(Material.SLIME_BALL).name("§cВыход").build();
	public static final ItemStack hub = new ItemBuilder(Material.MAGMA_CREAM).name("§4Выход в Лобби").build();

	public static final ItemStack toBase = new ItemBuilder(Material.ENDER_EYE).name("§5Нексус-Телепорт").build();
	
	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage("§aEnabling GoldRush!");
		
		plug = this;
		smg = getServer().getScoreboardManager();
		loadConfigs();
		
		getServer().getPluginManager().registerEvents(new MainLst(), this);
		getServer().getPluginManager().registerEvents(new DmgLst(), this);
		getServer().getPluginManager().registerEvents(new InterLst(), this);
		getServer().getPluginManager().registerEvents(new InventLst(), this);
		
		getCommand("gr").setExecutor(new GRCmd());
        
        BuildType.values();
        
        for (final Player p : Bukkit.getOnlinePlayers()) {
        	Rusher.getPlRusher(p.getName(), true);
        }
	}
	
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage("§cDisabling GoldRush!");
		for (final Arena ar : active.values()) {
			ar.end();
		}
	}
	
	public void loadConfigs() {
		try {
			final File cfg = new File(getDataFolder() + File.separator + "config.yml");
	        if (!cfg.exists()) {
	        	getServer().getConsoleSender().sendMessage("Config for GoldRush not found, creating a new one...");
	    		getConfig().options().copyDefaults(true);
	    		getConfig().save(cfg);
	        }
	        //значения из конфига
	        //tbl = getConfig().getString("setup.table");
	        //арены
	        final File af = new File(getDataFolder() + File.separator + "arenas.yml");
	        af.createNewFile();
	        final YamlConfiguration ars = YamlConfiguration.loadConfiguration(af);
	        
	        nonactive.clear();
	        if (!ars.contains("arenas")) {
	        	ars.createSection("arenas");
		        ars.save(af);
	        } else {
				for(final String s : ars.getConfigurationSection("arenas").getKeys(false)) {
					final Setup stp = new Setup(ars.getConfigurationSection("arenas." + s));
					if (stp.fin) {
						if (stp.lobby != null) {
							WorldManager.load(getServer().getConsoleSender(), stp.lobby.worldName, Environment.NORMAL, Generator.Empty);
							ApiOstrov.sendArenaData(stp.nm, GameState.ОЖИДАНИЕ, "§4[§6ЗоЛа§4]", "", "", "", "", 0);
							nonactive.put(stp.nm, stp);
						}
					}
				}
				
				final String lb = ars.getString("lobby");
				lobby = lb == null ? new WXYZ(getServer().getWorlds().get(0), 0, 80, 0) : new WXYZ(XYZ.fromString(lb));
			}
        }
        catch (IOException e) {
        	e.printStackTrace();
            return;
        }
	}
	
	public static void lobbyPl(final Player p, final Rusher rs) {
		nrmlzPl(p);
		rs.arena(null);
		rs.team(null);
		rs.klls0(); rs.dths0(); rs.mkls0(); rs.brks0();
		final PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setItem(0, join);
		inv.setItem(8, hub);
		Main.inGameCnt();
		lobbyScore(p);
		p.setGlowing(false);
		p.teleport(lobby.getCenterLoc());
		for (final Rusher r : rushs.values()) {
			final String prm = Main.getTopPerm(PM.getOplayer(r.name()));
			if (r.arena() == null) {
				TitleUtil.sendNmTg(r, "§4<§6ЛОББИ§4> §6", (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)"), EnumChatFormat.g);
			} else if (r.team() == null) {
				TitleUtil.sendNmTg(r, "§4[§6" + r.arena().name + "§4] §6", (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)"), EnumChatFormat.g);
			} else {
				TitleUtil.sendNmTg(r, "§4[§6" + r.arena().name + "§4] §6", " §4(§6" + rs.klls() + "§4-§6" + 
						rs.mkls() + "§4-§6" + rs.dths() + "§4)", EnumChatFormat.a(r.team().clr.getChar()));
			}
		}
	}

	public static void nrmlzPl(final Player p) {
		p.setGameMode(GameMode.SURVIVAL);
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);
		p.setHealth(20d);
		p.setFireTicks(-1);
		p.setFreezeTicks(0);
		p.setTotalExperience(0);
		for (final PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
	}
	   
	public static void lobbyScore(final Player p) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("GR", Criteria.DUMMY, Component.text("§4[§6ЗоЛа§4]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(10);
		ob.getScore("§6Карта: §4ЛОББИ")
		.setScore(9);
		ob.getScore("§4=-=-=-=-=-=-=-=-")
		.setScore(8);
		ob.getScore("§4Игр §6всего: §4" + ApiOstrov.getStat(p, Stat.GR_game))
		.setScore(7);
		ob.getScore("§6Выйграно:§4 " + ApiOstrov.getStat(p, Stat.GR_win))
		.setScore(6);
		ob.getScore("§6Проиграно: §4" + ApiOstrov.getStat(p, Stat.GR_loose))
		.setScore(5);
		ob.getScore("  ")
		.setScore(4);
		ob.getScore("§6Киллы §4/ §6Смерти")
		.setScore(3);
		final String i = String.valueOf((float) ApiOstrov.getStat(p, Stat.GR_kill) / (float) ApiOstrov.getStat(p, Stat.GR_death));
		ob.getScore("§4(§6К§4/§6Д§4): " + (i.length() > 4 ? i.substring(0, 5) : i))
		.setScore(2);
		ob.getScore("§4-=-=-=-=-=-=-=-")
		.setScore(1);
		
		ob.getScore("§e     ostrov77.ru")
		.setScore(0);
		p.setScoreboard(sb);
	}

	public static Arena createArena(final Setup stp) {
		return new Arena(stp.nm, stp.min, new WXYZ(stp.lobby), stp.bases, stp.bots);
	}
	
	public static Build getBldBlck(final WXYZ loc) {
		for (final Arena ar : active.values()) {
			for (final Rusher rs : ar.pls) {
				for (final Build bd : rs.team().blds) {
					if (bd.blks.contains(loc)) return bd;
				}
			}
		}
		return null;
	}

	/*public static void resetTab(final Rusher rs) {
		if (rs.arena() == null) {
			
		} else {
			if () {
				
			}
		}
	}*/
	
	public static void inGameCnt() {
		int i = 0;
		for (final Rusher rs : Main.rushs.values()) {
			if (rs.arena() != null) i++;
		}
		final Component c = Component.text("§6Сейчас в игре: §4" + i + "§6 человек!");
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendPlayerListFooter(c);
		}
	}
	
	public static String prf() {
		return "§4[§6GR§4] ";
	}

	public static <G> G rndElmt(G[] arr) {
		return arr[srnd.nextInt(arr.length)];
	}
	
	public static int parseInt(final String n) {
		try {
			return Integer.parseInt(n);
		} catch (final NumberFormatException e) {
			return 0;
		}
	}
	
	public static void crtSbdTm(final Scoreboard sb, final String nm, final String prf, final String val, final String sfx) {
		final Team tm = sb.registerNewTeam(nm);
		tm.addEntry(val);
		tm.prefix(Component.text(prf));
		tm.suffix(Component.text(sfx));
	}
	
	public static void chgSbdTm(final Scoreboard sb, final String nm, final String prf, final String sfx) {
		final Team tm = sb.getTeam(nm);
		if (tm == null) {
			plug.getLogger().info("Team " + nm + " is null");
		} else {
			tm.prefix(Component.text(prf));
			tm.suffix(Component.text(sfx));
		}
	}
	
	public static String getTopPerm(final Oplayer op) {
		if (op.hasGroup("xpanitely")) {
			return "Хранитель";
		} else if (op.hasGroup("supermoder")) {
			return "Архангел";
		} else if (op.hasGroup("legend")) {
			return "Легенда";
		} else if (op.hasGroup("hero")) {
			return "Герой";
		} else if (op.hasGroup("warior")) {
			return "Воин";
		}
		return "";
	}
	
	public static String transPerm(final String s) {
		switch (s) {
		case "xpanitely":
			return "Хранитель";
		case "supermoder":
			return "Архангел";
		case "legend":
			return "Легенда";
		case "hero":
			return "Герой";
		case "warior":
			return "Воин";
		default:
			return "";
		}
	}

	public static int rngFrom(int from, final int rnd) {
		if (rnd > 0) from += srnd.nextInt(rnd) + 1;
		return srnd.nextBoolean() ? from : -from;
	}
}
