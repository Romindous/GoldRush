package ru.romindous;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.ScoreboardManager;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.world.WorldManager.Generator;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;
import ru.romindous.game.Arena;
import ru.romindous.game.map.Setup;
import ru.romindous.game.object.Build;
import ru.romindous.game.object.PlRusher;
import ru.romindous.game.object.Rusher;
import ru.romindous.listener.DmgLst;
import ru.romindous.listener.InterLst;
import ru.romindous.listener.InventLst;
import ru.romindous.listener.MainLst;
import ru.romindous.type.BuildType;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

public class Main extends JavaPlugin {

	public static Main plug;
	public static WXYZ lobby;
	public static ScoreboardManager smg;
	
	public static final SecureRandom srnd = new SecureRandom();
	public static final HashMap<String, Arena> active = new HashMap<>();
	public static final HashMap<String, Setup> nonactive = new HashMap<>();
	
	public static final ItemStack join = new ItemBuilder(Material.CAMPFIRE).name("§6Выбор Карты").build();
	public static final ItemStack color = new ItemBuilder(Material.POPPED_CHORUS_FRUIT).name("§eВыбор Цвета").build();
	public static final ItemStack race = new ItemBuilder(Material.END_PORTAL_FRAME).name("§яВыбор Рассы").build();
	public static final ItemStack glow = new ItemBuilder(Material.GLOWSTONE_DUST).name("§eПодсветка Комманд").build();
	public static final ItemStack leave = new ItemBuilder(Material.SLIME_BALL).name("§cВыход").build();
	public static final ItemStack hub = new ItemBuilder(Material.MAGMA_CREAM).name("§4Выход в Лобби").build();

//	public static final ItemStack toBase = new ItemBuilder(Material.ENDER_EYE).name("§5Нексус-Телепорт").build();
	
	public static String PRFX = "§7[ЗоЛа] ";
	
	@Override
	public void onEnable() {
		//Ostrov things
		PM.setOplayerFun(p -> new PlRusher(p), true);
		TCUtil.N = "§7";
		TCUtil.P = "§6";
		TCUtil.A = "§4";

		PRFX = TCUtil.N + "[" + TCUtil.P + "Зо" + TCUtil.A + "Ла" + TCUtil.N + "] ";
		
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
        	Rusher.getPlRusher(p);
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
				final String lb = ars.getString("lobby");
				lobby = lb == null ? new WXYZ(getServer().getWorlds().get(0), 0, 80, 0) : new WXYZ(XYZ.fromString(lb));

				for(final String s : ars.getConfigurationSection("arenas").getKeys(false)) {
					final Setup stp = new Setup(ars.getConfigurationSection("arenas." + s));
					if (stp.fin) {
						if (stp.lobby != null) {
							WorldManager.load(getServer().getConsoleSender(), stp.lobby.worldName, Environment.NORMAL, Generator.Empty);
							GM.sendArenaData(Game.GR, stp.nm, GameState.ОЖИДАНИЕ, 0, "§4[§6ЗоЛа§4]", "", "", "");
							nonactive.put(stp.nm, stp);
						}
					}
				}

				try {
					for (final String blc : Build.saves.getKeys()) {
							final XYZ dms = XYZ.fromString(Build.saves.getString(blc));
							final World w = dms.getCenterLoc().getWorld();
							final WXYZ lc = new WXYZ(w, XYZ.fromString(blc))
									.add(-(dms.x >> 1), 0, -(dms.z >> 1));
							for (int x = 0; x != dms.x; x++) {
								for (int y = 0; y != dms.y; y++) {
									for (int z = 0; z != dms.z; z++) {
										if (Nms.getFastMat(lc.w, lc.x + x, lc.y + y, lc.z + z).isAir()) continue;
										lc.w.setType(lc.x + x, lc.y + y, lc.z + z, Material.AIR);
									}
								}
							}

						Build.saves.removeKey(blc);
						Ostrov.log_warn("Постройка убрана на " + lc.toString() + ", сразмером " + dms.toString());
					}
				} catch (NullPointerException | IllegalArgumentException e) {
					e.printStackTrace();
				}
				Build.saves.saveConfig();
			}
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
	}
	
	public static void lobbyPl(final Player p, final PlRusher rs) {
		nrmlzPl(p);
		rs.arena(null); rs.team(null); rs.level(-rs.level());
		rs.klls0(); rs.dths0(); rs.mkls0(); rs.brks0(); rs.exp0();
		rs.clearUpgs(); rs.nearLoc(null); rs.race(null);
		final PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setItem(0, join);
		inv.setItem(8, hub);
		Main.inGameCnt();
		lobbyScore(rs);
		p.setGlowing(false);
		p.teleport(lobby.getCenterLoc());
		final String rpm = rs.getTopPerm();
		rs.taq(TCUtil.N + "<" + TCUtil.A + "ЛОББИ" + TCUtil.N + "> ", TCUtil.N,
			(rpm.isEmpty() ? "" : TCUtil.N + " (§e" + rpm + TCUtil.N + ")"));
		for (final Player pl : Bukkit.getOnlinePlayers()) {
			if (p.getWorld().getUID().equals(pl.getWorld().getUID())) {
				pl.showPlayer(Main.plug, p);
				p.showPlayer(Main.plug, pl);
			} else {
				pl.hidePlayer(Main.plug, p);
				p.hidePlayer(Main.plug, pl);
			}
		}
	}

	public static void nrmlzPl(final Player p) {
		p.setGameMode(GameMode.SURVIVAL);
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);
		p.setHealth(20d);
		p.setFireTicks(-1);
		p.setFreezeTicks(0);
		p.setExp(0f);
		p.setLevel(0);
		p.setTotalExperience(0);
		for (final PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
	}
	   
	public static void lobbyScore(final PlRusher rs) {
		rs.score.getSideBar().reset().title(Main.PRFX)
			.add(" ")
			.add(TCUtil.N + "Карта: §4ЛОББИ")
			.add(TCUtil.A + "=-=-=-=-=-=-=-")
			.add(TCUtil.P + "Игр " + TCUtil.N + "всего: " + TCUtil.P + rs.getStat(Stat.GR_game))
			.add(" ")
			.add(TCUtil.N + "Выйграно: " + TCUtil.P + rs.getStat(Stat.GR_win))
			.add(TCUtil.N + "Проиграно: " + TCUtil.P + rs.getStat(Stat.GR_loose))
			.add(TCUtil.A + "=-=-=-=-=-=-=-")
			.add(TCUtil.N + "(" + TCUtil.P + "К" + TCUtil.N + "/" + TCUtil.A + "Д" + TCUtil.N + "): " + TCUtil.P +
				StringUtil.toSigFigs((double) rs.getStat(Stat.GR_kill) / (double) rs.getStat(Stat.GR_death), (byte) 2))
			.add(" ")
			.add("§e    ostrov77.ru").build();
	}

	public static Arena createArena(final Setup stp) {
		return new Arena(stp.nm, stp.min, new WXYZ(stp.lobby), stp.bases, stp.shops, stp.bots);
	}
	
	public static void inGameCnt() {
		int i = 0;
		for (final Rusher rs : PM.getOplayers(PlRusher.class)) {
			if (rs.arena() != null) i++;
		}
		final Component c = TCUtil.form(TCUtil.N + "Сейчас в игре: " + TCUtil.P + i + TCUtil.N + " человек!");
		for (final Player pl : Bukkit.getOnlinePlayers()) pl.sendPlayerListFooter(c);
	}

	public static <G> G rndElmt(G[] arr) {
		return arr[srnd.nextInt(arr.length)];
	}

	public static char[] shuffle(final char[] ar, final Random rnd) {
		int chs = ar.length >> 2;
		for (int i = ar.length - 1; i > chs; i--) {
			final int ni = rnd.nextInt(i);
			final char ne = ar[ni];
			ar[ni] = ar[i];
			ar[i] = ne;
			chs += ((chs-ni) >> 31) + 1;
		}
		return ar;
	}
}
