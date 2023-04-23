package gr.Romindous.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import gr.Romindous.Main;
import gr.Romindous.game.map.WXYZ;
import gr.Romindous.game.object.Build;
import gr.Romindous.game.object.Nexus;
import gr.Romindous.game.object.PlRusher;
import gr.Romindous.game.object.Rusher;
import gr.Romindous.type.BuildType;
import gr.Romindous.type.DirType;
import gr.Romindous.util.ItemUtil;
import gr.Romindous.util.LocUtil;
import gr.Romindous.util.TitleUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.EnumChatFormat;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.version.IServer;
import ru.komiss77.version.VM;

public class Arena {
	
	public final byte min;
	public final byte max;
	public final WXYZ lobby;
	public final String name;
	public final boolean bots;
	
	public final Set<Rusher> pls;
	public final Set<Rusher> spcs;
	
	public final Map<WXYZ, Nexus> orbs;
	
	private int time;
	public GameState gst;
	private BukkitTask task;
	
	public static final ItemStack sword = new ItemBuilder(Material.IRON_SWORD).setUnbreakable(true)
		.name("§4Каратель").lore(Arrays.asList(" ", "§eСокруши своих §cврагов", "§cкритическим §eударом")).build();
	public static final ItemStack pick = new ItemBuilder(Material.IRON_PICKAXE).setUnbreakable(true)
		.name("§6Кувалда").lore(Arrays.asList(" ", "§cЛКМ §e- Сокрушение", "§cПКМ §e- Строительство")).build();
	public static final ItemStack bow = new ItemBuilder(Material.BOW).setUnbreakable(true).enchantment(Enchantment.ARROW_INFINITE)
		.name("§5Цибуля").lore(Arrays.asList("§eНапичкай своих §cврагов", "§cострыми §eмаслинами")).build();
	public static final ItemStack arrow = new ItemBuilder(Material.ARROW).name("§dМетка").build();
	private static final FireworkEffect fwe = FireworkEffect.builder().with(Type.BURST).flicker(true).withColor(Color.ORANGE).withFade(Color.MAROON).build();
	private static ChatColor[] clrs = getClrs(Arrays.asList(ChatColor.AQUA, ChatColor.BLUE, ChatColor.DARK_AQUA, ChatColor.DARK_GRAY, ChatColor.DARK_GREEN, 
		ChatColor.DARK_PURPLE, ChatColor.GRAY, ChatColor.GREEN, ChatColor.LIGHT_PURPLE, ChatColor.RED, ChatColor.YELLOW, ChatColor.BLACK));

	public Arena(final String name, final byte min, final WXYZ lobby, final XYZ[] bases, final boolean bots) {
		this.min = min;
		this.name = name;
		this.lobby = lobby;
		this.bots = bots;
		this.gst = GameState.ОЖИДАНИЕ;
		this.pls = new HashSet<>();
		this.spcs = new HashSet<>();
		
		this.orbs = new HashMap<>();
		this.max = (byte) Math.min(bases.length, clrs.length);
		for (int i = max - 1; i >= 0; i--) {
			this.orbs.put(new WXYZ(lobby.w, bases[i]), new Nexus(null, clrs[i]));
		}
		
		this.task = null;
	}

	public boolean join(final Rusher rs) {
		switch (gst) {
		default:
			return spec(rs);
		case ФИНИШ:
			rs.getPlayer().sendMessage(Main.prf() + "§cКарта §6" + name + " §cуже заканчивается!");
			return false;
		case ОЖИДАНИЕ, СТАРТ:
			if (pls.size() < max) {
				rs.ifPlayer(p -> {
					pls.add(rs);
					rs.arena(this);
					Main.nrmlzPl(p);
					Main.inGameCnt();
					rs.clearInv();
					rs.item(Main.team, 2);
					rs.item(Main.leave, 7);
					p.teleport(lobby.getCenterLoc());
					p.sendMessage(Main.prf() + "§6Ты на карте §4" + name);
					final String prm = Main.getTopPerm(PM.getOplayer(p));
					TitleUtil.sendNmTg(rs, "§4[§6" + name + "§4] §6", (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)"), EnumChatFormat.g);
			        //p.playerListName(Component.text("§4[§6" + name + "§4] §6" + rs.name() + (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)")));
					for (final Rusher r : pls) {
						r.ifPlayer(rp -> {
							ApiOstrov.sendActionBarDirect(rp, amtToHB());
							if (rp.getEntityId() != p.getEntityId()) {
								rp.sendMessage(Main.prf() + "§4" + rs.name() + " §6зашел на карту!");
							}
						});
					}
					if (pls.size() == min) {
						ApiOstrov.sendArenaData(this.name, GameState.СТАРТ, "§4[§6ЗоЛа§4]", "", "", "", "", pls.size());
						countStart();
					} else if (pls.size() < min) {
						ApiOstrov.sendArenaData(this.name, GameState.ОЖИДАНИЕ, "§4[§6ЗоЛа§4]", "", "", "", "", pls.size());
						for (final Rusher r : pls) {
							r.ifPlayer(rp -> {
								if (rp.getEntityId() == p.getEntityId()) {
									startScore(r, rp);
								} else {
									Main.chgSbdTm(rp.getScoreboard(), "onwt", "", "§4" + pls.size() + " §6чел.");
									Main.chgSbdTm(rp.getScoreboard(), "rmnp", "", "§4" + String.valueOf(min - pls.size()) + " §6чел.");
								}
							});
						}
					} else {
						ApiOstrov.sendArenaData(this.name, GameState.СТАРТ, "§4[§6ЗоЛа§4]", "", "", "", "", pls.size());
						for (final Rusher r : pls) {
							r.ifPlayer(rp -> {
								if (rp.getEntityId() == p.getEntityId()) {
									startScore(r, rp);
								} else {
									Main.chgSbdTm(rp.getScoreboard(), "onwt", "", "§4" + pls.size() + " §6чел.");
								}
							});
						}
					}
				});
			} else {
				rs.getPlayer().sendMessage(Main.prf() + "§cКарта §6" + name + " §cзаполнена!");
				return false;
			}
		}
		return true;
	}

	private boolean spec(final Rusher rs) {
		spcs.add(rs);
		rs.arena(this);
		Main.inGameCnt();
		rs.teleport(rs.getEntity(), lobby.getCenterLoc());
		rs.ifPlayer(p -> {
			p.setGameMode(GameMode.SPECTATOR);
			Main.inGameCnt();
			p.sendMessage(Main.prf() + "§6Простмотр карты §4" + name);
			final String prm = Main.getTopPerm(PM.getOplayer(p));
			TitleUtil.sendNmTg(rs, "§4[§6" + name + "§4] §6", (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)"), EnumChatFormat.g);
	        //p.playerListName(Component.text("§4[§6" + name + "§4] §6" + rs.name() + (prm.isEmpty() ? "" : " §7(§e" + prm + "§7)")));
		});
		rs.clearInv();
		rs.item(Main.glow, 2);
		rs.item(Main.leave, 7);
		return true;
	}

	public void leave(final Rusher rs) {
		rs.ifPlayer(p -> {
			if (pls.remove(rs)) {
				switch (gst) {
				case ОЖИДАНИЕ:
					if (pls.size() == 0) {
						end(); return;
					}
					ApiOstrov.sendArenaData(this.name, GameState.ОЖИДАНИЕ, "§4[§6ЗоЛа§4]", "", "", "", "", pls.size());
					for (final Rusher r : pls) {
						r.ifPlayer(rp -> {
							ApiOstrov.sendActionBarDirect(rp, amtToHB());
							rp.sendMessage(Main.prf() + "§4" + rs.name() + " §6вышел с карты!");
						});
					}
					break;
				case СТАРТ:
					if (pls.size() < min) {
						if (task != null) {
							task.cancel();
							gst = GameState.ОЖИДАНИЕ;
						}
						for (final Rusher r : pls) {
							r.ifPlayer(rp -> {
								ApiOstrov.sendActionBarDirect(rp, amtToHB());
								rp.sendMessage(Main.prf() + "§4" + rs.name() + " §6вышел с карты!");
								rp.sendMessage(Main.prf() + "§6На карте недостаточно игроков для начала!");
								startScore(r, rp);
							});
						}
						ApiOstrov.sendArenaData(this.name, GameState.ОЖИДАНИЕ, "§4[§6ЗоЛа§4]", "", "", "", "", pls.size());
					} else {
						for (final Rusher r : pls) {
							r.ifPlayer(rp -> {
								ApiOstrov.sendActionBarDirect(rp, amtToHB());
								rp.sendMessage(Main.prf() + "§4" + rs.name() + " §6вышел с карты!");
								Main.chgSbdTm(rp.getScoreboard(), "onwt", "", "§4" + String.valueOf(pls.size()) + " §6чел.");
							});
						}
						ApiOstrov.sendArenaData(this.name, GameState.СТАРТ, "§4[§6ЗоЛа§4]", "", "", "", "", pls.size());
					}
					break;
				case ИГРА:
					killTeam(rs.team());
					break;
				case ФИНИШ:
					if (pls.size() == 0) {
						end(); return;
					}
					break;
				default:
					break;
				}
				Main.inGameCnt();
			} else if (spcs.remove(rs)) {
				p.sendMessage(Main.prf() + "§6Простмотр карты завершен!");
				Main.inGameCnt();
			}
			Main.lobbyPl(p, rs);
		});
	}

	private void countStart() {
		time = 30;
		gst = GameState.СТАРТ;
		lobby.w.setTime(12000l);
		ApiOstrov.sendArenaData(name, GameState.СТАРТ, "§4[§6ЗоЛа§4]", "", "", "", "", pls.size());
		
		for (final Rusher rs : pls) {
			rs.ifPlayer(p -> {
				startScore(rs, p);
			});
		}

		task = new BukkitRunnable() {
			@Override
			public void run() {
				switch (time--) {
				case 1:
					if (task != null) task.cancel();
					countGame();
					return;
				default:
					break;
				}
				
				for (final Rusher rs : pls) {
					rs.ifPlayer(p -> {
						Main.chgSbdTm(p.getScoreboard(), "time", "", "§4" + ApiOstrov.secondToTime(time));
						if (time < 6) {
							ApiOstrov.sendTitle(p, (time < 4 ? "§4" : "§6") + time, "");
							p.playSound(p.getEyeLocation(), Sound.BLOCK_COPPER_BREAK, 2f, 0.8f);
						}
					});
				}
			}
		}.runTaskTimer(Main.plug, 0, 20);
	}
	
	private void countGame() {
		time = 2436;
		gst = GameState.ИГРА;
		lobby.w.setTime(12000l);
		ApiOstrov.sendArenaData(name, GameState.ИГРА, "§4[§6ЗоЛа§4]", "", "", "", "", pls.size());
		
		for (final Rusher rs : pls) {
			final WXYZ org = LocUtil.getNr2DLoc(rs.getEntity().getLocation(), orbs.keySet());
			rs.team(new Nexus(rs, orbs.remove(org).clr));
			rs.team().blds.add(new Build(rs.team(), org, BuildType.GOLD, 1));
			rs.team().chgRecs(100, 0);
			
			rs.clearInv();
			rs.item(sword, 0);
			rs.item(bow, 1);
			rs.item(pick, 2);
			rs.item(arrow, 9);
			rs.item(DirType.FOLLOW.icn, 8);
			rs.dir(DirType.FOLLOW);
			
			rs.item(ItemUtil.color(new ItemBuilder(Material.LEATHER_HELMET).name("§eШапка")
					.setUnbreakable(true).build(), ItemUtil.ccToClr(rs.team().clr)), EquipmentSlot.HEAD);
			rs.item(ItemUtil.color(new ItemBuilder(Material.LEATHER_CHESTPLATE).name("§eКуртка")
					.setUnbreakable(true).build(), ItemUtil.ccToClr(rs.team().clr)), EquipmentSlot.CHEST);
			rs.item(ItemUtil.color(new ItemBuilder(Material.LEATHER_LEGGINGS).name("§eДжинсы")
					.setUnbreakable(true).build(), ItemUtil.ccToClr(rs.team().clr)), EquipmentSlot.LEGS);
			rs.item(ItemUtil.color(new ItemBuilder(Material.LEATHER_BOOTS).name("§eКросы")
					.setUnbreakable(true).build(), ItemUtil.ccToClr(rs.team().clr)), EquipmentSlot.FEET);
			
			rs.teleport(rs.getEntity(), new Location(org.w, org.x + Main.rngFrom(BuildType.maxXZ, 2), 
				org.y + 1, org.z + Main.rngFrom(BuildType.maxXZ, 2)));
			TitleUtil.sendNmTg(rs, "§4[§6" + name + "§4] §6", " §4(§6" + rs.klls() + "§4-§6" + 
				rs.mkls() + "§4-§6" + rs.dths() + "§4)", EnumChatFormat.a(rs.team().clr.getChar()));
		}
		
		for (final Rusher rs : pls) {
			rs.ifPlayer(p -> {
				Main.nrmlzPl(p);
				gameScore(rs, p);
				ApiOstrov.sendTitle(p, "§6Начинаем", "§6Построй §4армию §6и §4сокруши §6врагов!", 12, 40, 12);
				p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 2f, 0.6f);
			});
		}
		
		orbs.clear();
		
		task = new BukkitRunnable() {
			@Override
			public void run() {
				switch (time--) {
				case 120:
					toAllPls(p -> ApiOstrov.sendActionBarDirect(p, "§6Осталось §42 минуты §6до конца!"));
					break;
				case 60:
					toAllPls(p -> ApiOstrov.sendActionBarDirect(p, "§6Осталось §41 минута §6до конца!"));
					break;
				case 0:
					countFinish();
					return;
				default:
					break;
				}

				lobby.w.setTime((time % 375) << 6);
				for (final Rusher rs : pls) {
					final Nexus tm = rs.team();
					if (!tm.alive) continue;
					
					for (final Build b : tm.blds) {
						if ((b.tick++) % b.type.cld == 0 && b.done) {
							b.act();
						}
					}
					
					rs.ifPlayer(p -> {
						Main.chgSbdTm(p.getScoreboard(), "time", "", "§4" + ApiOstrov.secondToTime(time));
					});
				}
			}
		}.runTaskTimer(Main.plug, 0, 20);
	}

	private void countFinish() {
		if (task != null) task.cancel();
		switch (pls.size()) {
		case 0:
			end(); return;
		case 1:
			time = 10;
			gst = GameState.ФИНИШ;
			ApiOstrov.sendArenaData(name, GameState.ФИНИШ, "§4[§6ЗоЛа§4]", "", "", "", "", 1);
			final Rusher rs = pls.iterator().next();
			rs.ifPlayer(p -> ApiOstrov.sendTitle(p, "§6Победа", "§eКомманда " + rs.team().name(true) + " §eОдержала Верх!"));
			task = new BukkitRunnable() {
				@Override
				public void run() {
					switch (time--) {
					case 0:
						if (task != null) task.cancel();
						end(); return;
					default:
						final Firework fw = lobby.w.spawn(rs.getEntity().getLocation(), Firework.class);
						final FireworkMeta fm = fw.getFireworkMeta();
						fm.addEffect(fwe);
						fw.setFireworkMeta(fm);
						fm.setPower(2);
						break;
					}
				}
			}.runTaskTimer(Main.plug, 0, 20);
			break;
		default:
			time = 10;
			gst = GameState.ФИНИШ;
			ApiOstrov.sendArenaData(name, GameState.ФИНИШ, "§4[§6ЗоЛа§4]", "", "", "", "", pls.size());
			for (final Rusher r : pls) {
				r.ifPlayer(p -> ApiOstrov.sendTitle(p, "§4Ничья", "§cНикто не Сумел Одержать Верх!"));
			}
			task = new BukkitRunnable() {
				@Override
				public void run() {
					switch (time--) {
					case 0:
						if (task != null) task.cancel();
						end(); return;
					default:
						for (final Rusher r : pls) {
							final Firework fw = lobby.w.spawn(r.getEntity().getLocation(), Firework.class);
							final FireworkMeta fm = fw.getFireworkMeta();
							fm.addEffect(fwe);
							fw.setFireworkMeta(fm);
							fm.setPower(2);
						}
						break;
					}
				}
			}.runTaskTimer(Main.plug, 0, 20);
			break;
		}
	}

	public void addKill(final Rusher rs, final boolean plKll) {
		if (plKll) rs.kllsI(); else rs.mklsI();
		TitleUtil.sendNmTg(rs, "§4[§6" + name + "§4] §6", " §4(§6" + rs.klls() + "§4-§6" + 
			rs.mkls() + "§4-§6" + rs.dths() + "§4)", EnumChatFormat.a(rs.team().clr.getChar()));
	}

	public void killRs(final Rusher rs) {
		final LivingEntity le = rs.getEntity();
		if (gst == GameState.ИГРА) {
			rs.dthsI();
			TitleUtil.sendNmTg(rs, "§4[§6" + name + "§4] §6", " §4(§6" + rs.klls() + "§4-§6" + 
				rs.mkls() + "§4-§6" + rs.dths() + "§4)", EnumChatFormat.a(rs.team().clr.getChar()));
			final Location loc = le.getEyeLocation();
			if (rs.exp() != 0) {
				loc.getWorld().spawn(loc, ExperienceOrb.class).setExperience(rs.exp());
				rs.exp(0);
			}
			
			rs.ifPlayer(p -> {
				p.closeInventory();
				ApiOstrov.addStat(p, Stat.GR_death);
				Main.nrmlzPl(p);
			});
			
			final WXYZ rsp = rs.team().getCloseResp(new WXYZ(loc, false));
			if (rsp == null) {
				killTeam(rs.team());
			} else {
				rs.teleport(le, rsp.getCenterLoc());
			}
		}
	}

	public void killTeam(final Nexus nx) {
		nx.alive = false;
		for (final Build b : nx.blds) {
			b.remove(false);
		}
		nx.blds.clear();
		pls.remove(nx.rs);
		nx.rs.ifPlayer(p -> Main.lobbyPl(p, nx.rs));
		if (pls.size() == 1) {
			final Rusher wr = pls.iterator().next();
			if (wr instanceof PlRusher) {
				countFinish();
				return;
			}
			end();
			return;
		}
		for (final Rusher rs : pls) {
			rs.ifPlayer(p -> Main.chgSbdTm(p.getScoreboard(), "atms", "", getTeamLifes()));
		}
	}

	//сколько игроков из скольки
	public String amtToHB() {
		return pls.size() < min ? "§6На карте §4" + pls.size() + " §6игроков, нужно еще §4" + (min - pls.size()) + " для начала" 
			: "§6На карте §4" + pls.size() + " §6игроков, максимум: §4" + max;
	}

	private void startScore(final Rusher rs, final Player pl) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("GR", Criteria.DUMMY, Component.text("§4[§6ЗоЛа§4]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(8);
		ob.getScore("§6Карта: §4" + name)
		.setScore(7);
		ob.getScore("§4=-=-=-=-=-=-=-")
		.setScore(6);
		Main.crtSbdTm(sb, "onwt", "", "§6Игроков: ", "§4" + String.valueOf(pls.size()) + " §6чел.");
		ob.getScore("§6Игроков: ")
		.setScore(5);
		ob.getScore("   ")
		.setScore(4);
//		Main.crtSbdTm(sb, "team", "", "§6Цвет: ", rs.team() == null ? "§8Не выбран" : rs.team().name(true));
//		ob.getScore("§6Цвет: ")
//		.setScore(5);
//		ob.getScore("  ")
//		.setScore(4);
		ob.getScore("§4-=-=-=-=-=-=-")
		.setScore(3);
		if (gst == GameState.ОЖИДАНИЕ) {
			Main.crtSbdTm(sb, "rmnp", "", "§6Ждем еще ", "§4" + String.valueOf(min - pls.size()) + " §6чел.");
			ob.getScore("§6Ждем еще ")
			.setScore(2);
			ob.getScore(" ")
			.setScore(1);
		} else {
			ob.getScore("§6Начало через: ")
			.setScore(2);
			Main.crtSbdTm(sb, "time", "", "§6⌚ ", "§4" + ApiOstrov.secondToTime(time));
			ob.getScore("§6⌚ ")
			.setScore(1);
		}
		
		ob.getScore("§e     ostrov77.ru")
		.setScore(0);
		pl.setScoreboard(sb);
	}

	private void gameScore(final Rusher rs, final Player pl) {
		final Scoreboard sb = Main.smg.getNewScoreboard();
		final Objective ob = sb.registerNewObjective("GR", Criteria.DUMMY, Component.text("§4[§6ЗоЛа§4]"));
		ob.setDisplaySlot(DisplaySlot.SIDEBAR);
		ob.getScore("    ")
		.setScore(14);
		ob.getScore("§6Карта: §4" + name)
		.setScore(13);
		ob.getScore("§4=-=-=-=-=-=-=-=-")
		.setScore(12);
		Main.crtSbdTm(sb, "atms", "", "§6Комманды:", getTeamLifes());
		ob.getScore("§6Комманды:")
		.setScore(11);
		ob.getScore("§6Цвет: " + rs.team().name(true))
		.setScore(10);
		ob.getScore("   ")
		.setScore(9);
		Main.crtSbdTm(sb, "gold", "", "§eЗолото: ", "§6" + rs.team().gold + " ⛃");
		ob.getScore("§eЗолото: ")
		.setScore(7);
		Main.crtSbdTm(sb, "dust", "", "§eПыль: ", "§4" + rs.team().dust + " 🔥");
		ob.getScore("§eПыль: ")
		.setScore(6);
		ob.getScore("  ")
		.setScore(5);
		ob.getScore("§4-=-=-=-=-=-=-=-")
		.setScore(4);
		ob.getScore("§6До конца игры: ")
		.setScore(3);
		Main.crtSbdTm(sb, "time", "", "§6⌚ ", "§4" + ApiOstrov.secondToTime(time));
		ob.getScore("§6⌚ ")
		.setScore(2);
		ob.getScore(" ")
		.setScore(1);
		
		ob.getScore("§e     ostrov77.ru")
		.setScore(0);
		pl.setScoreboard(sb);
	}

	public void end() {
		if (task != null) task.cancel();
		ApiOstrov.sendArenaData(name, GameState.ОЖИДАНИЕ, "§4[§6ЗоЛа§4]", "", "", "", "", 0);
		Main.active.remove(name);
		if (!pls.isEmpty()) {
			final Iterator<Rusher> it = pls.iterator();
			while (it.hasNext()) {
				final Rusher rs = it.next();
				for (final Build bd : rs.team().blds) {
					bd.remove(false);
				}
				rs.ifPlayer(p -> {
					ApiOstrov.moneyChange(p, rs.brks() * 5 + rs.klls() * 2 + 5, "GoldRush");
					Main.lobbyPl(p, rs);
				});
				it.remove();
			}
		}
		
		if (!spcs.isEmpty()) {
			final Iterator<Rusher> it = spcs.iterator();
			while (it.hasNext()) {
				final Rusher rs = it.next();
				rs.ifPlayer(p -> Main.lobbyPl(p, rs));
				it.remove();
			}
		}
		
		if (task != null) {
			task.cancel();
		}
	}

	private String getTeamLifes() {
		final StringBuilder sb = new StringBuilder(pls.size() << 3);
		for (final Rusher rs : pls) {
			sb.append(rs.team().alive ? " §r" + rs.team().clr + "⛨" : " §m" + rs.team().clr + "⛨");
		}
		return sb.toString();
	}
	
	private void toAllPls(final Consumer<Player> cn) {
		for (final Rusher rs : pls) {
			rs.ifPlayer(p -> cn.accept(p));
		}
	}

	public boolean validateLoc(final WXYZ at) {
		final IServer is = VM.getNmsServer();
		final World w = at.w;
		final int X = at.x, Y = at.y, Z = at.z;
		for (int x = -BuildType.maxXZ; x <= BuildType.maxXZ; x++) {
			for (int z = -BuildType.maxXZ; z <= BuildType.maxXZ; z++) {
				if (is.getFastMat(w, X + x, Y, Z + z).isAir() || !is.getFastMat(w, X + x, Y + 1, Z + z).isAir()) {
					return false;
				}
			}
		}
		
		final int mnd = BuildType.maxXZ << 5;
		for (final Rusher rs : pls) {
			for (final Build bd : rs.team().blds) {
				if (bd.cLoc.dist2DSq(at) < mnd) return false;
			}
		}
		return true;
	}
	
	private static ChatColor[] getClrs(final List<ChatColor> clst) {
		Collections.shuffle(clst);
		return clst.toArray(new ChatColor[clst.size()]);
	}
}
